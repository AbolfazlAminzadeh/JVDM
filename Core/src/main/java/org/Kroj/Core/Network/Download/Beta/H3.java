package org.Kroj.Core.Network.Download.Beta;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.http3.*;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Network.Download.Security.TLS;
import org.Kroj.Core.Network.Netty.NettyUtil;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class H3 {

    public static void main(String[] args) throws Exception {
        URI uri = URI.create("https://dkstatics-public.digikala.com/digikala-products/af0103d79469c23779191b2d310192061bb2af40_1774359684.jpg?x-oss-process=image/resize,m_lfit,h_2400,w_2400/quality,q_100");
        String host = uri.getHost();
        int port = 443;

        EventLoopGroup io = NettyUtil.getEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap()
                .group(io)
                .channel(NettyUtil.getUDPClass())
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();

                        ChannelHandler http3Codec = Http3.newQuicClientCodecBuilder()
                                .sslContext(TLS.quicSSL)
                                .maxIdleTimeout(20, TimeUnit.SECONDS)
                                .initialMaxData(10_000_000)
                                .initialMaxStreamDataBidirectionalLocal(1_000_000)
                                .build();

                        pipe.addLast(http3Codec);
                    }
                });

        Channel c = bootstrap.bind(0).sync().channel();
        QuicChannel quicChannel = QuicChannel.newBootstrap(c)
                .handler(new Http3ClientConnectionHandler())
                .remoteAddress(new InetSocketAddress(host, port))
                .connect()
                .get();

        sendGetRequest(quicChannel,uri);

        quicChannel.close();
        c.close();
    }

    private static void sendGetRequest(QuicChannel quicChannel, URI uri) throws Exception {
        String host = uri.getHost();

        String path = uri.getRawPath();
        if (uri.getRawQuery() != null) {
            path += "?" + uri.getRawQuery();
        }

        QuicStreamChannel stream = Http3.newRequestStream(quicChannel,new ReceiveHandler(new Downloader())).sync().getNow();

        Http3Headers headers = new DefaultHttp3Headers()
                .method("GET")
                .path(path)
                .scheme("https")
                .authority(host);

        stream.writeAndFlush(new DefaultHttp3HeadersFrame(headers))
                .addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);

        stream.closeFuture().sync();
    }

}