package org.Kroj.Core.Network.Download.Beta;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import org.Kroj.Core.Network.DNS.DNS;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Network.Download.Security.TLS;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Tools.URL.URL;

import java.net.InetAddress;
import java.net.URI;

public class Test {

    public static void main(String[] args) {
        URI url = URL.getSafeURI("https://1906714720.rsc.cdn77.org/img/cdn77-test-14mb.jpg");
        IoEventLoopGroup group = NettyUtil.getEventLoopGroup(16);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NettyUtil.getTCPClass())
                .group(group)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast(TLS.ssl.newHandler(ch.alloc(),url.getHost(),url.getPort()));
                        Http2Settings settings = new Http2Settings()
                                .initialWindowSize(8 * 1024 * 1024);

                        Http2FrameCodec codec = Http2FrameCodecBuilder.forClient()
                                .initialSettings(settings)
                                .autoAckSettingsFrame(true)
                                .build();
                        pipe.addLast(codec);
                        pipe.addLast(new Http2MultiplexHandler(new ChannelInboundHandlerAdapter()));
                    }
                });
        InetAddress address = DNS.getInstance().resolve(url.getHost());
        bootstrap.connect(address,443).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("SALA");
                Channel parent = future.channel();

                Http2StreamChannelBootstrap bt = new Http2StreamChannelBootstrap(parent);
                bt.handler(new ChannelInitializer<Http2StreamChannel>() {
                    @Override
                    protected void initChannel(Http2StreamChannel ch) throws Exception {
                        ch.pipeline().addLast(new ReceiveHandler(new Downloader(new Part(url,"eno1",0,-1))));
                    }
                });
                for (int i = 0; i < 32; i ++)
                bt.open().addListener( fr -> {
                    if (fr.isSuccess()) {
                        Http2StreamChannel sc = (Http2StreamChannel) fr.getNow();
                        String path = (url.getRawPath() == null || url.getRawPath().isEmpty()) ? "/" : url.getRawPath();

                        Http2Headers headers = new DefaultHttp2Headers()
                                .method("GET")
                                .path(path)
                                .scheme(url.getScheme())
                                .authority(url.getHost());

                        Http2HeadersFrame frame = new DefaultHttp2HeadersFrame(headers,true);

                        sc.writeAndFlush(frame);
                    }
                });

            }
        });
    }
}
class CustomHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        if (object instanceof Http2DataFrame frame) {
            frame.release();
            if (frame.isEndStream()) {
                System.out.println("Finished!");
                ctx.close();
                System.exit(0);
            };
        } else System.out.println("Frame Type: " + object.getClass().getSimpleName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Error inside stream pipeline:");
        cause.printStackTrace();
        ctx.close();
    }
}