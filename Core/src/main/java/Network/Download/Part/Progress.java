package Network.Download.Part;

public interface Progress {
    long getTotalBytes();
    long getCurrentBytes();
    default double getProgress() {
        long total = getTotalBytes();
        if (total <= 0) return 0.0;
        return ((double) getCurrentBytes()) / total;
    }}
