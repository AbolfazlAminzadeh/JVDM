package org.Kroj.Core.Network.Download.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutException;
import org.Kroj.Core.Network.Download.Downloader;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Network.Download.Part.Request;
import org.Kroj.Core.Statics.Initializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.Kroj.Core.Tools.Logger.Logger.logger;


//TODO Better Log
public class DownloadHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final Part part;
    private final Downloader downloader;

    private volatile boolean reqSent = false;

    private volatile boolean redirected = false;

    private final FileChannel channel;

    private final boolean isHead;

    public DownloadHandler(Part part, Downloader downloader, boolean head) {
        this.part = part;
        this.downloader = downloader;
        this.isHead = head;
        if (head) {
            channel = null;
        } else {
            this.channel = downloader.getWriter().getFileChannel();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        sendRequest(ctx);
        super.handlerAdded(ctx);
    }

    private void sendRequest(ChannelHandlerContext ctx){
        if (reqSent) return;
        reqSent = true;
        ctx.channel().writeAndFlush(Request.createRangeRequest(part)).addListener(lis -> {
            if (!lis.isSuccess()) {
                logger.append("GET Failed!").nextLine();
                logger.debug().append(": ").append(lis.cause().getClass().getName()).append(":").append(lis.cause().getMessage());
                downloader.downloadFuture.completeExceptionally(lis.cause());
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws IOException {

        if (redirected) return;
        if (msg instanceof HttpResponse response) handleResponse(ctx, response);
        else if (msg instanceof HttpContent content) {
            handleContent(content);
            if (content instanceof LastHttpContent) {
                downloader.setDone(Downloader.Status.DOWNLOADED);
                ctx.close();
                downloader.downloadFuture.complete(null);
            }
        }
    }

    private void handleResponse(ChannelHandlerContext ctx, HttpResponse response) {
        int code = response.status().code();
        if (isRedirect(code)) {
            logger.info();
            redirected = true;
            String location = response.headers().get(HttpHeaderNames.LOCATION);
            ctx.channel().eventLoop().execute(() -> downloader.redirect(location));
            logger.append("Redirect to " + location).nextLine();
            return;
        }

        if (code == 206) {

            downloader.supportMultiThread = true;

            String contentRange = response.headers().get(HttpHeaderNames.CONTENT_RANGE);
            String fileName = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
            long total = parseContentRange(contentRange);
            if (total <= Initializer.SINGLE_THREAD_MAX_LENGTH) downloader.supportMultiThread = false;
            downloader.fileName.complete(fileName);
            if (!downloader.contentLength.isDone()) {downloader.contentLength.complete(total);}
        }

        else if (code == 200) {
            downloader.supportMultiThread = response.headers().get(HttpHeaderNames.CONTENT_RANGE) != null;
            String fileName = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
            String contentLengthString = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
            long length = -1;
            if (contentLengthString != null) {
                length = Long.parseLong(contentLengthString);
                downloader.supportMultiThread = false;
            }
            downloader.fileName.complete(fileName);
            if (!downloader.contentLength.isDone()) {downloader.contentLength.complete(length);}
            if (isHead) ctx.close();
        } else {
            logger.append("Unexpected response status: ").append(code).nextLine();
        }
    }

    private void handleContent(HttpContent content) throws IOException {

        if (isHead) return;
        int size = content.content().readableBytes();
        if (size == 0) return;
        long pos = part.getStart() + part.getCurrentBytes();
        int wroteBytes = 0;
        ByteBuf buffer = content.content().retain();
        try {
            for (ByteBuffer buf : buffer.nioBuffers()) {
                wroteBytes = channel.write(buf, pos+wroteBytes);
            }
        } finally {
            buffer.release();
        }
        if (size != wroteBytes) {
            logger.append("Partial Write Is Not Synced, Maybe output file be damaged").nextLine();
        }
        part.addBytes(wroteBytes);
    }

    private long parseContentRange(String header) {
        try {
            return Long.parseLong(header.substring(header.lastIndexOf("/") + 1));
        } catch (Exception e) {
            downloader.contentLength.completeExceptionally(e);
            return -1;
        }
    }

    private boolean isRedirect(int code) {
        return switch (code) {
            case 301, 302, 303, 307, 308 -> true;
            default -> false;
        };
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!redirected) {
            IOException ex = new IOException("Connection terminated");
            if (!downloader.contentLength.isDone()) {
                downloader.contentLength.completeExceptionally(ex);
            }
            if (!downloader.downloadFuture.isDone()) {
                downloader.downloadFuture.completeExceptionally(ex);
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!redirected) {
            if (cause instanceof ReadTimeoutException) {
                logger.error().append("Download request timed out!").nextLine();
            }
            if (!downloader.contentLength.isDone()) {
                downloader.contentLength.completeExceptionally(cause);
            }
            if (!downloader.downloadFuture.isDone()) {
                downloader.downloadFuture.completeExceptionally(cause);
            }
        }
        logger.append(cause.getMessage()).nextLine();
        ctx.close();
    }}