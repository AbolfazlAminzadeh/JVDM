package org.Kroj.Core.Network.Download.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.Kroj.Core.Network.Download.Downloader;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Tools.FileManagement.SafeFileChannel;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DownloadHandler extends SimpleChannelInboundHandler<HttpContent> {
    private final Part part;
    private final Downloader downloader;
    private final SafeFileChannel channel;

    public DownloadHandler(Part part, Downloader downloader,SafeFileChannel channel) {
        this.part = part;
        this.downloader = downloader;
        this.channel = channel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
        int readableBytes;
        ByteBuf buffer = msg.content();
        if ((readableBytes = buffer.readableBytes()) == 0) return;
        long pos = part.getWritePos();
        long end = part.getEnd();

        if (end >= 0 && pos >= end+1) {
            ctx.close();
            downloader.onPartComplete();
            return;
        }

        int partLength = readableBytes;
        if (end >= 0 && pos + readableBytes > end + 1) {
            partLength = (int) ((end - 1) + pos);
        }

        int wroteBytes = 0;

        ByteBuffer[] buffers = buffer.nioBuffers(buffer.readerIndex(), partLength);
        for (ByteBuffer buf : buffers) {
            while (buf.hasRemaining()) {
                wroteBytes += channel.write(buf,pos+wroteBytes);
            }
        }

        part.addBytes(wroteBytes);

        if (part.isCompleted()) {
            ctx.close();
            downloader.onPartComplete();
        }

        if (!ctx.channel().isWritable()) {
            ctx.channel().config().setAutoRead(false);
        }

        if (msg instanceof LastHttpContent) {
            ctx.close();
            downloader.onPartCompleted();
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
