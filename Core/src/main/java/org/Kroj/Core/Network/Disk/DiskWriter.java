package org.Kroj.Core.Network.Disk;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import org.Kroj.Core.Statics.Initializer;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;

public class DiskWriter implements AutoCloseable {
    private final LinkedTransferQueue<ByteBuf> queue = new LinkedTransferQueue<>();
    private final AtomicLong pending = new AtomicLong(0);
    private final AtomicReference<Throwable> error = new AtomicReference<>(null);

    private volatile Channel channel;
    private volatile boolean isFinished = false;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void put(ByteBuf buf) {
        if (isFinished) {
            buf.release(); // 10NS
            return;
        }

        buf.retain(); // 7NS

        int readableBytes = buf.readableBytes();
        long totalPending = pending.addAndGet(readableBytes);

        if (totalPending >= Initializer.DISK_QUEUE_PAUSE_READ && channel != null) {
            ChannelConfig config = channel.config();
            if (config.isAutoRead()) channel.config().setAutoRead(false);
        }

        queue.put(buf);
    }

    public void throwError(Throwable error) {
        if (this.error.compareAndSet(null,error)) finish();
    }

    public ByteBuf take() throws InterruptedException, IOException {
        final ByteBuf buf = queue.take();

        final Throwable error = this.error.get();
        if (error != null) {
            if (buf != EMPTY_BUFFER) {
                buf.release();
            }
            throw new IOException("Error while taking byte buffer: "+error.getMessage());
        }

        if (buf == EMPTY_BUFFER || !buf.isReadable()) return null;

        final int readableBytes = buf.readableBytes();
        final long totalPending = pending.addAndGet(-readableBytes);

        if (totalPending <= Initializer.DISK_QUEUE_RESUME_READ && channel != null) {
            final ChannelConfig config = channel.config();
            if (!config.isAutoRead()) channel.config().setAutoRead(true);
        }

        return buf;
    }

    public void finish() {
        isFinished = true;
        queue.put(EMPTY_BUFFER);
    }

    public void close() {
        finish();
        if (channel != null && channel.isOpen()) channel.close();

        ByteBuf buf;
        while ((buf = queue.poll()) != null) {
            if (buf != EMPTY_BUFFER) buf.release();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DiskWriter writer = new DiskWriter();


        for (int i = 0 ; i < 100 ; i ++) {
            Thread.ofVirtual().start(() -> {
//                writer.put(buf);
            });
        }

        for (int i = 0; i < 500;i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    writer.take();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        for (int i = 0 ; i < 800 ; i ++) {
            Thread.ofVirtual().start(() -> {
//                writer.put(buf);
            });
        }
        Thread.sleep(5000);
        writer.close();
    }
}