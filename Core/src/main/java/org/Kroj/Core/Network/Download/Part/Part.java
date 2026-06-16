package org.Kroj.Core.Network.Download.Part;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Part {
    private final int id;
    private final String device;
    private final long start;
    private final AtomicLong end;
    private final LongAdder downloaded = new LongAdder();
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
        downloaded.add(bytes);
    }

    public long getCurrentBytes() {
        return downloaded.sum();
    }

    public long getWritePos() {
        return start + getCurrentBytes();
    }

    public long getRemainingBytes() {
        long end = getEnd();
        if (end < 0) return Long.MAX_VALUE;
        long remaining = (end - getWritePos()) + 1;
        return Math.max(0, remaining);
    }

    public boolean isCompleted() {
        long end = getEnd();
        if (end < 0) return false;
        return getWritePos() > end;
    }
}