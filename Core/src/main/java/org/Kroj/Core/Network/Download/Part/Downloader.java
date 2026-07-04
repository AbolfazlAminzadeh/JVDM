package org.Kroj.Core.Network.Download.Part;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http3.Http3Headers;
import io.netty.handler.codec.http3.Http3HeadersFrame;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AsciiString;
import org.Kroj.Core.Network.DNS.DNS;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Handlers.H1.DownloadHandler;
import org.Kroj.Core.Network.Download.Handlers.H1.HeaderHandler;
import org.Kroj.Core.Network.Download.Handlers.HttpVersionSwitch;
import org.Kroj.Core.Network.Download.Security.TLS;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Tools.Exceptions.AlreadyConnectedException;
import org.Kroj.Core.Tools.Exceptions.DiskQueueFailedException;
import org.Kroj.Core.Tools.Exceptions.TooMuchRedirections;
import org.Kroj.Core.Tools.URL.URL;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


import static org.Kroj.Core.Network.Download.Part.Downloader.State.*;
import static org.Kroj.Core.Tools.Logger.Logger.logger;
import static org.Kroj.Core.Statics.Initializer.*;

public class Downloader {

    public enum State {
        IDLE,
        DOWNLOADING,
        PAUSED,
        COMPLETE,
        FAILED;

    }
    private final Part part;

    private final Download download;
    private final EventLoopGroup io;
    private final AtomicReference<State> state = new AtomicReference<>(IDLE);

    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final AtomicInteger redirectCount = new AtomicInteger(0);
    private volatile Channel ch;
    public Downloader(Part part, Download download, EventLoopGroup io) {
        this.part = part;
        this.download = download;
        this.io = io;
    }

    public synchronized void start() {
        if (!state.compareAndSet(IDLE, DOWNLOADING) && !state.compareAndSet(FAILED, DOWNLOADING)) {
            return;
        }
        connect();
    }

    private boolean connect() throws AlreadyConnectedException {
        if (state.get() != DOWNLOADING) {
            throw new AlreadyConnectedException("The Downloader is already connected, Issi");
        }
        ;

        URI uri = part.getUri();
        boolean secure = "https".equalsIgnoreCase(uri.getScheme());
        int port = uri.getPort() == -1 ? (secure ? 443 : 80) : uri.getPort();

        Bootstrap b = new Bootstrap()
                .group(io)
                .channel(NettyUtil.getTCPClass())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RECVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(MINIMUM_BUFFER_SIZE, INITIAL_BUFFER_SIZE, MAXIMUM_BUFFER_SIZE))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addFirst(new BindToDeviceHandler(part.getDevice()));
                        pipe.addLast(new ReadTimeoutHandler(RECEIVE_TIMEOUT, TimeUnit.MILLISECONDS));
                        if (secure) {
                            pipe.addLast(TLS.ssl.newHandler(ch.alloc(), uri.getHost(), port));
                        }
                        pipe.addLast(new HttpClientCodec());
                        pipe.addLast(new HeaderHandler(Downloader.this));
                        pipe.addLast(new DownloadHandler(part, Downloader.this, download));
                    }
                });

        CompletableFuture.supplyAsync(() -> DNS.getInstance().resolve(uri.getHost()))
                .whenCompleteAsync(((inetAddress, throwable) -> {
                    if (throwable != null) {
                        onFailure(throwable);
                        return;
                    }
                    try {
                        b.connect(inetAddress, port).addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                ch = future.channel();
//                                sendHTTP1Request(ch);
                                logger.append("Req Sent!").nextLine();
                                retryCount.set(0);
                            } else {
                                onNetworkFailed(future.cause());
                            }
                        });
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }), io);

        return secure;
    }

    private void startDownload(Channel ch) {
        URI uri = part.getUri();
        String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String reqTarget = uri.getRawQuery() != null ? path + "?" + uri.getRawQuery() : path;

        HttpRequest req = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                reqTarget
        );
        req.headers().set(HttpHeaderNames.HOST, uri.getHost());
        req.headers().set(HttpHeaderNames.USER_AGENT, USER_AGENT);

        long start = part.getWritePos();
        long end = part.getEnd();

        if (end >= 0) {
            req.headers().set(HttpHeaderNames.RANGE, "bytes=" + start + '-' + end);
        } else if (start > 0) {
            req.headers().set(HttpHeaderNames.RANGE, "bytes=" + start + '-');
        }
        logger.append("Req Sending!").nextLine();
        ch.writeAndFlush(req);
    }


    private String extractSalam(Supplier<Headers> headersSupplier) {
        return "";
    }

    public synchronized void pause() {
        if (state.compareAndSet(DOWNLOADING, PAUSED)) {
            if (ch != null && ch.isOpen()) {
                ch.close();
            }
        }
    }

    public void onComplete() {
        if (state.compareAndSet(DOWNLOADING, COMPLETE)) {
            download.onComplete(part);
        }
    }

    public void onRedirect(String targetLocation) {
        try {
            if (redirectCount.incrementAndGet() >= MAX_REDIRECTIONS)
                throw new TooMuchRedirections("More than "+MAX_REDIRECTIONS+" Redirections");
            URI url = URL.getSafeURI(targetLocation);
            this.part.setURI(url);
            if (ch != null && ch.isOpen()) {
                ch.close();
            }
            io.execute(this::connect);
        } catch (Exception e) {
            onFailure(e);
        }
    }

    public void onFailure(Throwable e) {
        if (state.get() == COMPLETE || state.get() == PAUSED) return;
        if (e instanceof DiskQueueFailedException) {
            logger.append(e.getClass()).append(":").append(e).nextLine();
            if (state.compareAndSet(DOWNLOADING, FAILED)) {
                download.onFailure(e);
            }
        } else if (e instanceof TooMuchRedirections) download.onFailure(e);
        else onNetworkFailed(e);

    }
    private void onNetworkFailed(Throwable e) {
        if (state.get() == PAUSED || state.get() == COMPLETE) return;
        logger.append("Retrying Part (").append(part).append("), Because of:").append(e).nextLine();
        int tries = retryCount.incrementAndGet();
        if (tries <= MAX_RETRIES) {
            io.schedule(this::connect, RETRY_DELAY, TimeUnit.MILLISECONDS);
        } else {
            if (state.compareAndSet(DOWNLOADING, FAILED)) {
                download.onFailure(e);
            }
        }
    }

    public Part getPart() { return part; }
    public State getState() { return state.get(); }
}