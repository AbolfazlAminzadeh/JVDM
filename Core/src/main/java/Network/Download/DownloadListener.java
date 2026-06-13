package Network.Download;

public interface DownloadListener {
    void onReady(String fileName, long size);
    void onProgress(long current, long total, double speed);
    void onCompleted();
    void onFailed(Throwable onFailure);
}
