package org.Kroj.Android;

import org.Kroj.Core.Network.Download.Download;

public class DownloadItem {
    private final String url;
    private String name;
    private int progress;
    private String status;
    private boolean isPaused = false;
    private Download download;
    public DownloadItem(String url, String name, int progress, String status) {
        this.url = url;
        this.name = name;
        this.progress = progress;
        this.status = status;
    }
    public void setDownload(Download download) {
        this.download = download;
    }
    public Download getDownload() {
        return download;
    }
    public String getUrl() { return url; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
}
