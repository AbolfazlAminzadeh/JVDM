package org.Kroj.Core.Network.Download.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Part.Downloader;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Tools.Exceptions.DiskQueueFailedException;

public class DownloadHandler extends SimpleChannelInboundHandler<HttpContent> {
    private final Part part;
    private final Downloader downloader;
    private final Download download;

    public DownloadHandler(Part part, Downloader downloader, Download download) {
        this.part = part;
        this.downloader = downloader;
        this.download = download;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) {
        ByteBuf content = msg.content();
        int readableBytes = content.readableBytes();

        if (readableBytes > 0) {
            synchronized (part) {
                long pos = part.getWritePos();
                long end = part.getEnd();

                if (end >= 0 && pos >= end + 1) {
                    ctx.close();
                    downloader.onComplete();
                    return;
                }

                long remaining = end >= 0 ? end - pos + 1 : readableBytes;
                int length = (int) Math.min(readableBytes, remaining);

                ByteBuf slice = content.slice(content.readerIndex(), length);
                slice.retain();

                if (download.getWriter().addToQueue(slice, pos, ctx.channel(), part)) {
                    part.addBytes(length);
                } else {
                    ctx.close();
                    downloader.onFailure(new DiskQueueFailedException("Failed To Queue Disk Task"));
                }

                if (part.isCompleted() || (part.getEnd() >= 0 && part.getWritePos() >= part.getEnd() + 1)) {
                    ctx.close();
                    downloader.onComplete();
                    return;
                }
            }
        }

        if (msg instanceof LastHttpContent) {
            if (part.getEnd() >= 0 && part.getWritePos() <= part.getEnd()) {
                ctx.close();
                downloader.onFailure(new java.io.IOException("Premature end of stream. Missing bytes."));
            } else {
                ctx.close();
                downloader.onComplete();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        downloader.onFailure(cause);
        ctx.close();
    }
}