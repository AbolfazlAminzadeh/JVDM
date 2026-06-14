package org.Kroj.Core.Network.Download.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import org.Kroj.Core.Network.Download.Downloader;
import org.Kroj.Core.Network.Download.Part.Part;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class ProtocolSwitchHandler extends ApplicationProtocolNegotiationHandler {
    private final Part part;
    private final Downloader downloader;
    private final boolean isHead;

    public ProtocolSwitchHandler(Part part, Downloader downloader,boolean isHead) {
        this.part = part;
        this.downloader = downloader;
        this.isHead = isHead;
        super("");
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws InterruptedException {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            Http2FrameCodec handler = Http2FrameCodecBuilder.forClient()
                    .autoAckPingFrame(true)
                    .build();

            Http2MultiplexHandler multiplexHandler = new Http2MultiplexHandler(new ChannelInboundHandlerAdapter());
            ctx.pipeline().addLast(handler);
            ctx.pipeline().addLast(multiplexHandler);

            Http2StreamChannelBootstrap bootstrap = new Http2StreamChannelBootstrap(ctx.channel());
            bootstrap.handler(new ChannelInitializer<Http2StreamChannel>() {

                @Override
                protected void initChannel(Http2StreamChannel ch) {

                    Http2StreamFrameToHttpObjectCodec codec = new Http2StreamFrameToHttpObjectCodec(false);
                    ch.pipeline().addLast(codec);
                    ch.pipeline().addLast(new DownloadHandler(part,downloader,isHead));
                }
            });

            bootstrap.open().addListener(lis -> {
                if (lis.isSuccess()) {
//                    logger.append("H2 Successfully");
                } else {
                    logger.append("H2 Failed");
                    logger.debug().append(": ").append(lis.cause().getClass().getName()).append(":").append(lis.cause().getMessage());
                }
                logger.nextLine();
            });
        } else {
            ctx.pipeline().addLast(new HttpClientCodec());
//            logger.append("H1").nextLine();
            ctx.pipeline().addLast(new DownloadHandler(part, downloader,isHead));
        }
    }

    @Override
    protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) {
        logger.append("Failed To TLS Hanshake: ").append(cause.getMessage()).nextLine().append(cause.getStackTrace()).nextLine();
        if (!downloader.contentLength.isDone()) {
            downloader.contentLength.completeExceptionally(cause);
        }
        if (!downloader.downloadFuture.isDone()) {
            downloader.downloadFuture.completeExceptionally(cause);
        }        ctx.close();
    }
}