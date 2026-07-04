package org.Kroj.Core.Network.Download.Handlers.H1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;
import org.Kroj.Core.Network.Download.Part.Downloader;
import org.Kroj.Core.Tools.URL.ResponseCodes;

public class HeaderHandler extends ChannelInboundHandlerAdapter {
    private final Downloader downloader;

    public HeaderHandler(Downloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse response) {
            int code = response.status().code();
            if (ResponseCodes.isRedirection(code)) {
                if (ctx.pipeline().get(DownloadHandler.class) != null) {
                    ctx.pipeline().remove(DownloadHandler.class);
                }
                ctx.close();
                return;
            }
            ctx.pipeline().remove(this);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        downloader.onFailure(cause);
        ctx.close();
    }
}