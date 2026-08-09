package org.Kroj.Core.Network.Download.Progress;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class Speed {

    private record Moment(long time, long bytes) { }

    private final Queue<Moment> buffer = new LinkedList<>();
    private final long bufferLength;
    private final AtomicLong last = new AtomicLong(-1);
    public Speed(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    public double updateAndGetSpeed(long current) {
        long now = System.currentTimeMillis();

        if (last.compareAndSet(-1, current)) return 0;

        long distance = current - last.get();
        last.set(distance);

        buffer.add(new Moment(now, distance));

        while (!buffer.isEmpty() && (now - buffer.peek().time) > bufferLength) buffer.poll();

        long bytes = 0;
        for (Moment m : buffer) bytes += m.bytes();
        if (buffer.isEmpty()) return 0;

        long lastestTime = buffer.peek().time();
        long elapsed = now - lastestTime;

        if (elapsed == 0) return 0.0;

        return (bytes*1000.0)/elapsed;
    }

    public void reset() {
        buffer.clear();
    }
}