package org.kroj.Core.Download;

public interface DownloadListener {
    void onReady(String fileName, long size);
    void onProgress(long current, long total, double speed);
    void onPaused(long lastByte, long total);
    void onCompleted();
    void onFailed(Throwable onFailure);
}
