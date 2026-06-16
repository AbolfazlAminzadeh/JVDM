package org.Kroj.Core.Network.Download.Progress;

import java.util.LinkedList;
import java.util.Queue;

public class Speed {

    private record Moment(long time, long bytes) { }

    private final Queue<Moment> buffer = new LinkedList<>();
    private final long bufferLength;

    public Speed(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    public synchronized double updateAndGetSpeed(long current) {
        long now = System.currentTimeMillis();
        buffer.add(new Moment(now, current));

        while (!buffer.isEmpty() && (now - buffer.peek().time) > bufferLength) {
            buffer.poll();
        }

        if (buffer.size() < 2) {
            return 0.0;
        }

        Moment oldest = buffer.peek();
        long elapsed = now-oldest.time;
        long bytes = current-oldest.bytes;

        if (elapsed == 0) return 0.0;

        return (bytes*1000.0)/elapsed;
    }

    public synchronized void reset() {
        buffer.clear();
    }
}