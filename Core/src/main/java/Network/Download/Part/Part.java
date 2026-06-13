package Network.Download.Part;

import java.net.URI;

public class Part implements Progress {

    private URI uri;
    private final String device;
    private final long start;
    private final long end;

    private volatile long downloaded;
    private volatile boolean paused;

    public Part(URI uri, String device, long start, long end) {
        this.uri = uri;
        this.device = device;
        this.start = start;
        this.end = end;
    }

    public void addBytes(long bytes) {
        downloaded += bytes;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    @Override
    public long getTotalBytes() {
        return end - start + 1;
    }

    @Override
    public long getCurrentBytes() {
        return downloaded;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public URI getUri() { return uri; }
    public long getStart() { return start; }
    public long getEnd() { return end; }
    public String getDevice() { return device; }

    @Override
    public String toString() {
        return "D="+downloaded+",S="+start+",E="+end;
    }
}
