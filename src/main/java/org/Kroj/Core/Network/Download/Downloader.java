package org.Kroj.Core.Network.Download;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.NetUtil;
import org.Kroj.Core.Network.DNS.DNS;
import org.Kroj.Core.Network.Download.Handlers.DownloadHandler;
import org.Kroj.Core.Network.Download.Handlers.ProtocolSwitchHandler;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Network.Download.Security.TLS;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.FileManagement.FileWriter;
import org.Kroj.Core.Tools.URL.URL;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class Downloader {

    public enum Status {
        INIT,
        DOWNLOADING,
        DOWNLOADED,
        REDIRECTED,
        CANCELED,
        TIMEOUT;
    }

    private Status status = Status.INIT;
    private ChannelFuture cf;
    private byte redirectedTimes = 0;

    private final Part part;
    private final FileWriter fw;
    private final Downloader downloader = this;

    public final CompletableFuture<Long> contentLength = new CompletableFuture<>();
    public final CompletableFuture<String> fileName = new CompletableFuture<>();
    public final CompletableFuture<Void> downloadFuture = new CompletableFuture<>();

    public boolean supportMultiThread;
    private final boolean isHead;

    private record Speed(long bytes, long time) {}

    private final Queue<Speed> speeds = new ArrayDeque<>();
    private double currentSpeed = 0;

    public Downloader(Part part) {
        this.part = part;
        fw = null;
        isHead = true;
    }

    public Downloader(Part part, FileWriter fw) {
        this.part = part;
        this.fw = fw;
        isHead = false;
    }

    public synchronized void redirect(String location) {
        if (status == Status.CANCELED || status == Status.DOWNLOADED)
            return;

        if (redirectedTimes++ >= Initializer.MAX_REDIRECTIONS) {
            status = Status.CANCELED;
            return;
        }

        URI target = getRedirect(part.getUri(), location);
        part.setURI(target);

        status = Status.REDIRECTED;

        if (cf != null) {
            cf.channel().eventLoop().execute(() -> {
                cf.channel().close();
                start();
            });
        } else {
            start();
        }
    }

    private URI getRedirect(URI uri, String location) {
        URI loc = URL.getSafeURI(location);

        if (loc.isAbsolute()) return loc;

        return uri.resolve(loc);
    }

    public void start() {
        URI uri = part.getUri();

        if (uri.getHost() == null) {
            IOException ex = new IOException("Inavlid Host");
            logger.append(ex.getMessage()).nextLine();
            if (!contentLength.isDone()) {
                contentLength.completeExceptionally(ex);
            }
            if (!downloadFuture.isDone()) {
                downloadFuture.completeExceptionally(ex);
            }
            return;
        }

        boolean secure = uri.getScheme().equals("https");

        boolean isIP = NetUtil.isValidIpV4Address(uri.getHost())
                || NetUtil.isValidIpV6Address(uri.getHost());

        int port = uri.getPort() == -1 ? (secure ? 443 : 80) : uri.getPort();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(Manager.getInstance().mainGroup).channel(NettyUtil.getTCPClass())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Initializer.CONNECTION_TIMEOUT)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.RECVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1 << 16, 1 << 17, 1 << 20))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addFirst(new BindToDeviceHandler(part.getDevice()));

                        pipe.addLast(new ReadTimeoutHandler(Initializer.RECEIVE_TIMEOUT,TimeUnit.MILLISECONDS));

                        if (secure) {
//                            logger.append("TLS Activated");
                            pipe.addLast(TLS.ssl.newHandler(ch.alloc(), uri.getHost(), port));
                            pipe.addLast(new ProtocolSwitchHandler(part,downloader,isHead));
                        } else {
                            pipe.addLast(new HttpClientCodec());
                            pipe.addLast(new DownloadHandler(part, downloader, isHead));
                        }
                    }
                });
        InetAddress addr = isIP ? InetAddress.ofLiteral(uri.getHost()) : DNS.getInstance().resolve(uri.getHost());
        cf = bootstrap.connect(addr, port);
        cf.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                Throwable cause = future.cause();
                if (!contentLength.isDone()) {
                    contentLength.completeExceptionally(cause);
                }
                if (!downloadFuture.isDone()) {
                    downloadFuture.completeExceptionally(cause);
                }
                logger.error().append(cause.getMessage()).nextLine();
            }
        });
    }

    public double getSpeed() {
        long currentByte = part.getCurrentBytes();
        long now = System.nanoTime();

        speeds.add(new Speed(currentByte, now));

        while (speeds.size() > 2) {
            Speed oldSpeed = speeds.peek();
            if (now - oldSpeed.time > 1_000_000_000L) {
                speeds.poll();
            } else {
                break;
            }
        }

        if (speeds.size() < 2) {
            currentSpeed = 0;
            return 0;
        }

        Speed oldSpeed = speeds.peek();
        long byteDif = currentByte - oldSpeed.bytes;
        long timeDif = now - oldSpeed.time;

        currentSpeed = (timeDif > 0 && byteDif > 0) ? (byteDif * 1_000_000_000.0 / timeDif) : 0;
        return currentSpeed;
    }

    public FileWriter getWriter() {return fw;}

    public Part getPart() {return part;}

    public void setDone(Status status) {
        this.status = status;
    }

}
