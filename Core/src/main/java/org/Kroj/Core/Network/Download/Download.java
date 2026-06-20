package org.Kroj.Core.Network.Download;

import io.netty.channel.EventLoopGroup;
import org.Kroj.Core.Network.Disk.DiskWriter;
import org.Kroj.Core.Network.Download.Handlers.DownloadListener;
import org.Kroj.Core.Network.Download.Part.Downloader;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Network.Download.Progress.Speed;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.Exceptions.DownloadCompletionException;
import org.Kroj.Core.Tools.FileManagement.SafeFileChannel;
import org.Kroj.Core.Tools.String.FileName;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Download {

    private final URI uri;
    private final Path targetDir;
    private final DownloadListener listener;
    private final List<String> devices;
    private final int concurrency;

    private final EventLoopGroup io;

    private final List<Part> parts = new CopyOnWriteArrayList<>();
    private final List<Downloader> downloaders = new CopyOnWriteArrayList<>();
    private final AtomicInteger downloadings = new AtomicInteger(0);
    private final AtomicBoolean headersReceived = new AtomicBoolean(false);
    private final AtomicBoolean isFinished = new AtomicBoolean(false);
    private final Speed speed = new Speed(2500);

    private final DiskWriter writer;
    private final AtomicInteger pendings = new AtomicInteger(0);

    private volatile SafeFileChannel channel;
    private volatile Path targetFile;
    private ScheduledFuture<?> progressScheduler;
    private ScheduledFuture<?> partSplitterScheduler;

    private volatile long totalSize = -1;

    public Download(URI uri, Path targetDir, int concurrency, List<String> devices, EventLoopGroup io, DownloadListener listener) {
        this.uri = uri;
        this.targetDir = targetDir;
        this.concurrency = concurrency;
        this.devices = devices;
        this.io = io;
        this.listener = listener;
        this.writer = new DiskWriter(this);
    }

    public void start() {
        writer.start();

        Part firstPart = new Part(0, uri, devices.getFirst(), 0, -1);
        Downloader head = new Downloader(firstPart, this, io);
        downloaders.add(head);

        downloadings.incrementAndGet();
        head.start();
    }

    public void onHeadersReceive(Downloader head, long size, boolean supportRange, String rawFileName, String etag) {
        if (!headersReceived.compareAndSet(false, true)) return;

        String fileName = FileName.getFileName(uri, rawFileName);
        totalSize = size;

        CompletableFuture.runAsync(() -> {
            channel = new SafeFileChannel(targetFile = targetDir.resolve(fileName));
            try {
                if (totalSize > 0) channel.allocate(totalSize);
            } catch (Exception e) {
                throw new DownloadCompletionException(e);
            }
        }).whenCompleteAsync((unused, throwable) -> {
            if (throwable != null) {
                head.onFailure(throwable.getCause() != null ? throwable.getCause() : throwable);
                return;
            }

            if (listener != null) {
                listener.onReady(fileName, totalSize);
            }

            Part headPart = head.getPart();
            parts.add(headPart);

            if (supportRange && size > 0 && concurrency > 1) {
                long partSize = totalSize / concurrency;
                headPart.setEnd(partSize - 1);

                URI finalURI = headPart.getUri();

                for (int i = 1; i < concurrency; i++) {
                    long start = (long) i * partSize;
                    long end = i == concurrency - 1 ? size - 1 : start + partSize - 1;

                    Part part = new Part(i, finalURI, devices.get(i % devices.size()), start, end);
                    parts.add(part);
                    addDownloader(part);
                }
            }

            startSchedulers();
        }, io);
    }

    public void addDownloader(Part part) {
        Downloader downloader = new Downloader(part, this, io);
        downloaders.add(downloader);
        downloadings.incrementAndGet();
        downloader.start();
    }

    private void startSchedulers() {
        progressScheduler = io.scheduleWithFixedDelay(this::calcProgress, Initializer.PROGRESS_INTERVAL, Initializer.PROGRESS_INTERVAL, TimeUnit.MILLISECONDS);
        partSplitterScheduler = io.scheduleWithFixedDelay(this::splitParts, Initializer.SPLIT_PART_INTERVAL, Initializer.SPLIT_PART_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void calcProgress() {
        long current = parts.stream().mapToLong(Part::getCurrentBytes).sum();
        double currentSpeed = speed.updateAndGetSpeed(current);
        if (listener != null) {
            listener.onProgress(current, totalSize, currentSpeed);
        }
    }

    private synchronized void splitParts() {
        if (isFinished.get()) return;
        if (downloadings.get() >= concurrency) return;

        Downloader target = null;
        long maxRemaining = Initializer.SPLIT_PART_MIN_THRESHOLD_BYTE;

        for (Downloader d : downloaders) {
            if (d.getState() == Downloader.State.DOWNLOADING) {
                long remaining = d.getPart().getRemainingBytes();
                if (maxRemaining < remaining) {
                    maxRemaining = remaining;
                    target = d;
                }
            }
        }

        if (target != null) {
            final Part part = target.getPart();
            synchronized (part) {
                final long pos = part.getWritePos();
                final long oldEnd = part.getEnd();
                final long remaining = oldEnd - pos + 1;

                if (remaining > Initializer.SPLIT_PART_MIN_THRESHOLD_BYTE) {
                    final long half = pos + (remaining / 2);
                    part.setEnd(half - 1);

                    int id = parts.size();
                    String device = devices.get(id % devices.size());

                    Part newPart = new Part(id, part.getUri(), device, half, oldEnd);
                    parts.add(newPart);
                    addDownloader(newPart);
                }
            }
        }
    }

    public synchronized void pause() {
        stopSchedulers();
        speed.reset();
        for (Downloader d : downloaders) {
            d.pause();
        }
        writer.stop();
        for (Part part : parts) {
            part.queuedToWritten(); // Roll back any unwritten chunks
        }
        if (listener != null) {
            long current = parts.stream().mapToLong(Part::getCurrentBytes).sum();
            listener.onPaused(current, totalSize);
        }
    }

    public synchronized void resume() {
        downloaders.clear();
        downloadings.set(0);
        speed.reset();
        if (headersReceived.get() && (channel == null || channel.isClosed())) {
            channel = new SafeFileChannel(targetFile);
        }
        writer.start();
        for (Part part : parts) {
            part.queuedToWritten();
            if (!part.isCompleted()) addDownloader(part);
        }
        startSchedulers();
    }

    public void onComplete() {
        if (downloadings.decrementAndGet() == 0) {
            checkComplete();
        } else {
            io.execute(this::splitParts);
        }
    }

    private void completeDownload() {
        stopSchedulers();
        try {
            if (channel != null) {
                channel.close();
            }
            if (listener != null) listener.onCompleted();
        } catch (Exception e) {
            if (listener != null) listener.onFailed(e);
        }
    }

    private synchronized void failAll(Throwable t) {
        stopSchedulers();
        for (Downloader d : downloaders) {
            d.pause();
        }
        downloadings.set(0);
        downloaders.clear();
        try {
            if (channel != null && !channel.isClosed()) channel.close();
        } catch (Exception _) {}

        if (listener != null) {
            listener.onFailed(t);
        }
    }

    private void stopSchedulers() {
        if (progressScheduler != null) progressScheduler.cancel(false);
        if (partSplitterScheduler != null) partSplitterScheduler.cancel(false);
    }

    public void onFailure(Throwable e) {
        failAll(e);
    }

    public SafeFileChannel getChannel() {
        return channel;
    }

    public void decreasePendingWrite() {
        pendings.decrementAndGet();
    }

    public void increasePendingWrite() {
        pendings.incrementAndGet(); // Fixed: increment instead of decrement
    }

    public void checkComplete() {
        if (downloadings.get() == 0 && pendings.get() == 0) {
            if (isFinished.compareAndSet(false, true)) {
                completeDownload();
            }
        }
    }

    public DiskWriter getWriter() {
        return writer;
    }
}