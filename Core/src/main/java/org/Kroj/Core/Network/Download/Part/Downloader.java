package org.Kroj.Core.Network.Download.Part;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.Kroj.Core.Network.DNS.DNS;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Handlers.DownloadHandler;
import org.Kroj.Core.Network.Download.Handlers.HeaderHandler;
import org.Kroj.Core.Network.Download.Security.TLS;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Tools.Exceptions.DiskQueueFailedException;
import org.Kroj.Core.Tools.Exceptions.TooMuchRedirections;
import org.Kroj.Core.Tools.URL.URL;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


import static org.Kroj.Core.Network.Download.Part.Downloader.State.*;
import static org.Kroj.Core.Tools.Logger.Logger.logger;
import static org.Kroj.Core.Statics.Initializer.*;

public class Downloader {

    public enum State {
        IDLE,
        DOWNLOADING,
        PAUSED,
        COMPLETE,
        FAILED
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

    private void connect() {
        if (state.get() != DOWNLOADING) return;

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
                .option(ChannelOption.SO_RCVBUF, RECEIVE_BUFFER_SIZE)
                .option(ChannelOption.SO_SNDBUF, SEND_BUFFER_SIZE)
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
                                sendRequest(ch);
                                retryCount.set(0);
                            } else {
                                onNetworkFailed(future.cause());
                            }
                        });
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }), io);
    }

    private void sendRequest(Channel ch) {
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

        ch.writeAndFlush(req);
    }

    public void onHeadersReceived(HttpResponse response) {
        int code = response.status().code();

        if (code >= 300 && code < 400) {
            String location = response.headers().get(HttpHeaderNames.LOCATION);
            onRedirect(location);
            return;
        }

        if (code < 200 || code >= 300) {
            if (ch != null && ch.isOpen()) {
                ch.close();
            }
            onFailure(new IOException("Server responded with HTTP error: " + code));
            return;
        }

        long start = part.getWritePos();
        long end = part.getEnd();
        boolean hasRange = (end >= 0 || start > 0);
        if (hasRange && code != 206) {
            if (ch != null && ch.isOpen()) {
                ch.close();
            }
            onFailure(new IOException("Server ignored requested range (Status: " + code + ")"));
            return;
        }

        boolean rangeSupport = (code == 206) || "bytes".equalsIgnoreCase(response.headers().get(HttpHeaderNames.ACCEPT_RANGES));
        long length = -1;

        if (code == 206) {
            String contentRange = response.headers().get(HttpHeaderNames.CONTENT_RANGE);
            if (contentRange != null) {
                try {
                    length = Long.parseLong(contentRange.substring(contentRange.lastIndexOf("/") + 1));
                } catch (Exception _) {}
            }
        } else {
            String contentLength = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
            if (contentLength != null) {
                try {
                    length = Long.parseLong(contentLength);
                } catch (Exception _) {}
            }
        }

        String disposition = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
        String etag = response.headers().get(HttpHeaderNames.ETAG);

        download.onHeadersReceive(this, length, rangeSupport, disposition, etag);
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