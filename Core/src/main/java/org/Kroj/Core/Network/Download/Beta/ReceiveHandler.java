package org.Kroj.Core.Network.Download.Beta;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http3.Http3DataFrame;
import io.netty.handler.codec.http3.Http3HeadersFrame;

public class ReceiveHandler extends ChannelInboundHandlerAdapter {

    private final Downloader downloader;
    public ReceiveHandler(Downloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        switch (object) {
            case Http3HeadersFrame h3 -> downloader.onH3Headers(h3);
            case Http2HeadersFrame h2 -> downloader.onH2Headers(h2);
            case HttpResponse h1 -> downloader.onH1Headers(h1);
            case Http3DataFrame h3 -> downloader.onContent(h3.content());
            case Http2DataFrame h2 -> downloader.onContent(h2.content());
            case HttpContent h1 -> downloader.onContent(h1.content());
            default -> System.out.println(object.getClass());
        }
    }


}
