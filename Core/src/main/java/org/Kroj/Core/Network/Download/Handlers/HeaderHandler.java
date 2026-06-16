package org.Kroj.Core.Network.Download.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.Kroj.Core.Network.Download.Part.Downloader;

public class HeaderHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final Downloader downloader;

    public HeaderHandler(Downloader downloader) {
        this.downloader = downloader;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse response) {
            downloader.onHeadersReceived(response);
            ctx.pipeline().remove(this);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        downloader.onFailure((Exception) cause);
        ctx.close();
    }
}