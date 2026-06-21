package org.Kroj.Core.Network.Disk;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.FileManagement.SafeFileChannel;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.Kroj.Core.Statics.Initializer.*;


//TODO Batching tasks
public class DiskWriter implements Runnable {

    private final Download download;
    private final BlockingQueue<Task> queue = new ArrayBlockingQueue<>(DISK_QUEUE_CAPACITY);
    private final Set<Channel> paused = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread thread;

    public DiskWriter(Download download) {
        this.download = download;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            thread = new Thread(this, DISK_QUEUE_THREAD_PREFIX + String.valueOf(download.hashCode()));
            thread.setDaemon(true);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (thread != null) {
                thread.interrupt();
            }
            Task task;
            while ((task = queue.poll()) != null) {
                task.buffer().release();
            }
            paused.clear();
        }
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                Task task = queue.take();
                write(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public boolean addToQueue(ByteBuf buf, long pos, Channel channel, Part part) {
        if (!running.get()) {
            buf.release();
            return false;
        }

        if (queue.size() >= DISK_QUEUE_PAUSE_READ) {
            if (channel.config().isAutoRead()) {
                channel.config().setAutoRead(false);
                paused.add(channel);
            }
        }

        Task task = new Task(buf, pos, channel, part);
        download.increasePendingWrite();

        if (queue.offer(task)) return true; else {
            buf.release();
            download.decreasePendingWrite();
            return false;
        }
    }

    private void write(Task task) {
        try {
            SafeFileChannel channel = null;

            while ((channel = download.getChannel()) == null) {
                if (!running.get()) return;
                Thread.sleep(DISK_QUEUE_WAIT_TIME);
            }

            if (!channel.isClosed()) {
                ByteBuffer[] buffers = task.buffer().nioBuffers();
                long pos = task.pos();
                int totalWritten = 0;
                for (ByteBuffer buf : buffers) {
                    if (buf.hasRemaining()) {
                        int remaining = buf.remaining();
                        channel.write(buf, pos);
                        pos += remaining;
                        totalWritten += remaining;
                    }
                }
                task.part().addWrittenBytes(totalWritten);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            download.onFailure(e);
        } finally {
            task.buffer().release();
            download.decreasePendingWrite();

            if (queue.size() <= DISK_QUEUE_RESUME_READ && !paused.isEmpty()) {
                for (Channel ch : paused) {
                    if (paused.remove(ch)) {
                        ch.config().setAutoRead(true);
                    }
                }
            }
            download.checkComplete();
        }
    }
}