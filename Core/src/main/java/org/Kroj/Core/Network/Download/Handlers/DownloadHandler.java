package org.Kroj.Core.Network.Download.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Part.Downloader;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Tools.FileManagement.SafeFileChannel;

import java.nio.ByteBuffer;

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
        SafeFileChannel fileChannel = download.getChannel();
        if (fileChannel == null) {
            return;
        }

        ByteBuf data = msg.content();
        int readableBytes = data.readableBytes();

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

                ByteBuf slice = data.slice(data.readerIndex(), length);
                ByteBuffer[] buffers = slice.nioBuffers();

                int written = 0;
                for (ByteBuffer buf : buffers) {
                    if (buf.hasRemaining()) {
                        int rem = buf.remaining();
                        fileChannel.write(buf, written + pos);
                        written += rem;
                    }
                }
                part.addBytes(written);

                if (part.isCompleted() || (part.getEnd() >= 0 && part.getWritePos() >= part.getEnd() + 1)) {
                    ctx.close();
                    downloader.onComplete();
                    return;
                }
            }

            if (!ctx.channel().isWritable()) {
                ctx.channel().config().setAutoRead(false);
            }
        }

        if (msg instanceof LastHttpContent) {
            ctx.close();
            downloader.onComplete();
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            ctx.channel().config().setAutoRead(true);
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        downloader.onFailure(cause);
        ctx.close();
    }
}