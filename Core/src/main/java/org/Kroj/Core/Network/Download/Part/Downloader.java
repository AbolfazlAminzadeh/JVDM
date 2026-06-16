package org.Kroj.Core.Network.Download.Part;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.NetUtil;
import org.Kroj.Core.Network.DNS.DNS;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Handlers.DownloadHandler;
import org.Kroj.Core.Network.Download.Handlers.HeaderHandler;
import org.Kroj.Core.Network.Download.Security.TLS;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.URL.URL;

import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.Kroj.Core.Tools.Logger.Logger.logger;
import static org.Kroj.Core.Network.Download.Part.Downloader.State.*;

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

        boolean isIP = NetUtil.isValidIpV4Address(uri.getHost())
                || NetUtil.isValidIpV6Address(uri.getHost());

        Bootstrap b = new Bootstrap()
                .group(io)
                .channel(NettyUtil.getTCPClass())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Initializer.CONNECTION_TIMEOUT)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.RECVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1 << 16, 1 << 17, 1 << 20))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addFirst(new BindToDeviceHandler(part.getDevice()));
                        pipe.addLast(new ReadTimeoutHandler(Initializer.RECEIVE_TIMEOUT, TimeUnit.MILLISECONDS));

                        if (secure) {
                            pipe.addLast(TLS.ssl.newHandler(ch.alloc(), uri.getHost(), port));
                        }
                        pipe.addLast(new HttpClientCodec());

                        pipe.addLast(new HeaderHandler(Downloader.this));
                        pipe.addLast(new DownloadHandler(part, Downloader.this, download));
                    }
                });

        try {
            InetAddress addr = isIP ? InetAddress.ofLiteral(uri.getHost()) : DNS.getInstance().resolve(uri.getHost());
            b.connect(addr, port).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ch = future.channel();
                    sendRequest(ch);
                    retryCount.set(0);
                } else {
                    onNetworkFailed((Exception) future.cause());
                }
            });
        } catch (Exception e) {
            onFailure(e);
        }
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
        req.headers().set(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), uri.getScheme());

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

        boolean supportsRange = (code == 206) || "bytes".equalsIgnoreCase(response.headers().get(HttpHeaderNames.ACCEPT_RANGES));
        long length = -1;

        if (code == 206) {
            String contentRange = response.headers().get(HttpHeaderNames.CONTENT_RANGE);
            if (contentRange != null) {
                try {
                    length = Long.parseLong(contentRange.substring(contentRange.lastIndexOf("/") + 1));
                } catch (Exception ignored) {}
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

        download.onHeadersReceive(this, length, supportsRange, disposition, etag);
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
            download.onComplete();
        }
    }

    public void onRedirect(String targetLocation) {
        try {
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

    public void onFailure(Exception e) {
        if (state.get() == COMPLETE || state.get() == PAUSED) return;
        if (e instanceof java.nio.channels.ClosedChannelException) return;
        logger.append(e.getClass()).append(":").append(e.getMessage()).nextLine();
        onNetworkFailed(e);
    }

    private void onNetworkFailed(Exception e) {
        if (state.get() == PAUSED || state.get() == COMPLETE) return;

        int tries = retryCount.incrementAndGet();
        if (tries <= Initializer.MAX_RETRIES) {
            io.schedule(this::connect, Initializer.RETRY_DELAY, TimeUnit.MILLISECONDS);
        } else {
            if (state.compareAndSet(DOWNLOADING, FAILED)) {
                download.onFailure(e);
            }
        }
    }

    public Part getPart() { return part; }
    public State getState() { return state.get(); }
}