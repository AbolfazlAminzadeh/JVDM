package org.Kroj.Core.Network.Download.Beta;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http3.Http3DataFrame;
import io.netty.handler.codec.http3.Http3HeadersFrame;
import io.netty.handler.codec.quic.QuicStreamResetException;

public class ReceiveHandler extends ChannelInboundHandlerAdapter {

    private final Downloader downloader;
    private final HeaderListener listener;
    public ReceiveHandler(Downloader downloader, HeaderListener listener) {
        this.downloader = downloader;
        this.listener = listener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        switch (object) {
            case Http3HeadersFrame h3 -> {
                if (listener != null) listener.onH3Headers(h3.headers());
            }
            case Http2HeadersFrame h2 -> {
                if (listener != null) listener.onH2Headers(h2.headers());
            }
            case HttpResponse h1 -> {
                if (listener != null) listener.onH1Headers(h1.headers());
            }

            case LastHttpContent h1 -> downloader.finish(h1.content());

            case Http3DataFrame h3 -> downloader.onContent(h3.content());
            case Http2DataFrame h2 -> {
                if (h2.isEndStream()) downloader.finish(h2.content()); else downloader.onContent(h2.content());
            }
            case HttpContent h1 -> downloader.onContent(h1.content());
            default -> System.out.println(object.getClass());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof QuicStreamResetException reset) return;
        cause.printStackTrace();
    }

}
