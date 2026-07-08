package org.Kroj.Core.Network.Download.Beta;

public interface DownloadListener {
    void onHeadersReceived();
    void onDownloadComplete();
}
