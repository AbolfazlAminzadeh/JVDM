package org.Kroj.Core.Network.Download;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.Kroj.Core.Network.DNS.DNS;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Tools.Logger.Logger;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static org.Kroj.Core.Tools.Logger.Logger.logger;
public class Test {

// public static String url = "https://ndl1.hollowofthealley.space/anime/2017/Fall/Black%20Clover/1080/Black%20Clover%20-%20011.%5BSS%5D%5B1080%5D%5BMixFlixTop%5D.mkv";
    public static String url = "https://dl2.soft98.ir/soft/m/Microsoft.Edge.148.0.3967.96.x64.zip";

    public static final IoEventLoopGroup mainGroup = NettyUtil.getEventLoopGroup();

    public static void main(String[] args) throws SSLException, URISyntaxException, InterruptedException, ExecutionException {
        String[] devices = {"eno1","enp0s20f0u1"};
        for (String device : devices) {
            for (int i = 0; i < 16; i++) {
                startDownload(0, Integer.MAX_VALUE,device);
            }
        }
    }

    public static void startDownload(long start, long end, String device) throws URISyntaxException {
        String safeURL = url.replace("[","%5b").replace("]","%5D");
        URI uri = new URI(safeURL);

        if (uri.getHost() == null) {
            logger.append("MEW");
            return;
        }

        boolean secure = url.startsWith("https://");

        boolean isIP = uri.getHost().split("\\.").length == 4;

        int port = uri.getPort() == -1 ? (secure ? 443 : 80) : uri.getPort();


        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(mainGroup).channel(NettyUtil.getTCPClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    public final SslContext ssl;

                    {
                        try {
                            ssl = SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .sslProvider(SslProvider.JDK)
                                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                                            ApplicationProtocolConfig.Protocol.ALPN,
                                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                            ApplicationProtocolNames.HTTP_2,
                                            ApplicationProtocolNames.SPDY_3_1,
                                            ApplicationProtocolNames.HTTP_1_1
                                    ))
                                    .build();
                        } catch (SSLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void initChannel(SocketChannel ch) {
                        Logger.logger.info();
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast(new BindToDeviceHandler(device));
                        if (secure) {
                            pipe.addLast(ssl.newHandler(ch.alloc(), uri.getHost(), port));
                            pipe.addLast(new ApplicationProtocolNegotiationHandler("h1") {
                                @Override
                                protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                                    if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                                        Http2FrameCodec handler = Http2FrameCodecBuilder.forClient()
                                                .autoAckPingFrame(true)
                                                .build();
                                        Http2MultiplexHandler multiplexHandler = new Http2MultiplexHandler(new ChannelInboundHandlerAdapter());
                                        ch.pipeline().addLast(handler);
                                        ch.pipeline().addLast(multiplexHandler);
                                        Http2StreamChannelBootstrap bootstrap = new Http2StreamChannelBootstrap(ch);
                                        bootstrap.handler(new ChannelInitializer<Http2StreamChannel>() {

                                            @Override
                                            protected void initChannel(Http2StreamChannel ch) throws Exception {
                                                Http2StreamFrameToHttpObjectCodec codec = new Http2StreamFrameToHttpObjectCodec(false);
                                                ch.pipeline().addLast(codec);
                                                ch.pipeline().addLast(new DownloadHandler(uri,start,end, device));
                                            }

                                        });

                                        bootstrap.open().addListener(lis -> {
                                            if (!lis.isSuccess()) {
                                                Logger.logger.info().append("H2 Isnt Okay").nextLine();
                                                return;
                                            }
                                            Logger.logger.info().append("H2 Is Okay").nextLine();
                                        });

                                    } else {
                                        ctx.pipeline().addLast(new HttpClientCodec());
                                        ctx.pipeline().addLast(new DownloadHandler(uri,start,end, device));
                                    }
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    Logger.logger.error().append("Handshake failed: ").append(cause.getMessage()).nextLine();
                                    ctx.close();
                                }
                            });
                        } else {
                            pipe.addLast(new HttpClientCodec());
                            pipe.addLast(new DownloadHandler(uri,start,end,device));
                        }
                    }
                })
        ;

        InetAddress addr = isIP ? InetAddress.ofLiteral(uri.getHost()) : DNS.getInstance().resolve(uri.getHost());
        Logger.logger.append(addr.getHostAddress()).append(':').append(port).append(" ").append(uri.getRawPath()).nextLine();
        ChannelFuture cf = bootstrap.connect(addr, port);

    }

}

class DownloadHandler extends SimpleChannelInboundHandler<HttpObject> {

    private volatile boolean reqSent = false;

    private final URI uri;

    private final long startIndex;
    private final long stopIndex;

    private final String device;

    public DownloadHandler(URI uri, long startIndex, long stopIndex, String device) {
        this.uri = uri;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.device = device;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            sendRequest(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendRequest(ctx);
        super.channelActive(ctx);
    }

    public void sendRequest(ChannelHandlerContext ctx) {
        if (reqSent) return;
        reqSent = true;
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().add(HttpHeaderNames.RANGE,"bytes="+startIndex+"-"+stopIndex);
        request.headers().set(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        ctx.writeAndFlush(request).addListener(future -> {
            if (future.isSuccess()) {
                logger.append("GET Sent").nextLine();
            } else {
                logger.append("GET Failed").nextLine();
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse response) handleHeaders(ctx,response);
        else if (msg instanceof HttpContent content) {
            handleBody(content);
            if (content instanceof LastHttpContent) {
                Logger.logger.append("Download Finished!").nextLine();
                ctx.close();
            }
        }
    }

    public void handleHeaders(ChannelHandlerContext ctx, HttpResponse response) throws InterruptedException {
        HttpHeaders headers = response.headers();
        int contentLength = headers.getInt(HttpHeaderNames.CONTENT_LENGTH);
        switch (response.status().code()) {
            case 206:
                Logger.logger.append("Partial Content").nextLine();
                break;
            case 301, 302, 303, 307, 308: {
                String location = headers.get(HttpHeaderNames.LOCATION);
                Logger.logger.append("Redirected: ").append(location).nextLine();
                ctx.channel().eventLoop().execute(() -> {
                    Test.url = location;
                    try {
                        Test.startDownload(startIndex, stopIndex, device);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
                break;
            }
        }
    }
    public void handleBody(HttpContent content) {}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logger.logger.append(cause.getMessage()).nextLine();
        ctx.close();
    }

}