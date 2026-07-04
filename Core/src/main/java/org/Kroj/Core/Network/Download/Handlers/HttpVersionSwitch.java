package org.Kroj.Core.Network.Download.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import org.Kroj.Core.Tools.Logger.Logger;

public class HttpVersionSwitch extends ApplicationProtocolNegotiationHandler {

    public HttpVersionSwitch() {
        super(ApplicationProtocolNames.HTTP_1_1);
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        ChannelPipeline pipe = ctx.pipeline();
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            Http2FrameCodec codec = Http2FrameCodecBuilder.forClient()
                    // More Options To Add
                    .build();
            pipe.addLast(codec);
            pipe.addLast(new Http2MultiplexHandler(new ChannelInboundHandlerAdapter()));
            Logger.logger.append("H2, Faster Connection :)").nextLine();
        } else {
            pipe.addLast(new HttpClientCodec());
        }
    }
}
