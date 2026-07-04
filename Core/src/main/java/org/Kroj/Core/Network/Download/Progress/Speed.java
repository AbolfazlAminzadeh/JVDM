package org.Kroj.Core.Network.Download.Progress;

import java.util.LinkedList;
import java.util.Queue;

public class Speed {

    private record Moment(long time, long bytes) { }

    private final Queue<Moment> buffer = new LinkedList<>();
    private final long bufferLength;
    private long last = -1;
    public Speed(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    public synchronized double updateAndGetSpeed(long current) {
        long now = System.currentTimeMillis();

        if (last == -1) {
            last = current;
            return 0;
        }

        long distance = current - last;
        last = current;

        buffer.add(new Moment(now, distance));

        while (!buffer.isEmpty() && (now - buffer.peek().time) > bufferLength) {
            buffer.poll();
        }

        long bytes = 0;
        for (Moment m : buffer) {
            bytes += m.bytes();
        }

        if (buffer.isEmpty()) return 0;

        long lastestTime = buffer.peek().time();
        long elapsed = now - lastestTime;

        if (elapsed == 0) return 0.0;

        return (bytes*1000.0)/elapsed;
    }

    public synchronized void reset() {
        buffer.clear();
    }
}