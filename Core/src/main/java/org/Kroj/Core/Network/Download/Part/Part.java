package org.Kroj.Core.Network.Download.Part;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Part {
    private final int id;
    private final String device;
    private final long start;
    private final AtomicLong end;
    private final LongAdder queued = new LongAdder();
    private final LongAdder written = new LongAdder();
    private volatile URI uri;

    public Part(int id, URI uri, String device, long start, long end) {
        this.id = id;
        this.uri = uri;
        this.device = device;
        this.start = start;
        this.end = new AtomicLong(end);
    }

    public int getId() { return id; }
    public URI getUri() { return uri; }
    public String getDevice() { return device; }
    public long getStart() { return start; }

    public long getEnd() {
        return end.get();
    }

    public void setEnd(long end) {
        this.end.set(end);
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public void addBytes(long bytes) {
        queued.add(bytes);
    }

    public void addWrittenBytes(long bytes) {
        written.add(bytes);
    }

    public long getInQueueBytes() {
        return queued.sum();
    }

    public long getWrittenBytes() {
        return written.sum();
    }

    public void queuedToWritten() {
        queued.reset();
        queued.add(getWrittenBytes());
    }

    public long getCurrentBytes() {
        return getWrittenBytes();
    }

    public long getWritePos() {
        return start + getInQueueBytes();
    }

    public long getRemainingBytes() {
        long endValue = getEnd();
        if (endValue < 0) return Long.MAX_VALUE;
        long remaining = (endValue - (start + getInQueueBytes())) + 1;
        return Math.max(0, remaining);
    }

    public boolean isCompleted() {
        long endValue = getEnd();
        if (endValue < 0) return false;
        return (start + getWrittenBytes()) > endValue;
    }
}