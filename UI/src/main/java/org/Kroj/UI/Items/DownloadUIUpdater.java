package org.Kroj.UI.Items;

import javafx.application.Platform;
import org.Kroj.Core.Network.Download.Handlers.DownloadListener;

public class DownloadUIUpdater implements DownloadListener {

    private final DownloadItem item;

    public DownloadUIUpdater(DownloadItem item) {
        this.item = item;
    }

    @Override
    public void onReady(String fileName, long size) {
        Platform.runLater(() -> item.updateReady(fileName, size));
    }

    @Override
    public void onProgress(long current, long total, double speed) {
        Platform.runLater(() -> item.updateProgress(current, total, speed));
    }

    @Override
    public void onPaused(long lastByte, long total) {
        Platform.runLater(() -> item.updatePaused(lastByte, total));
    }

    @Override
    public void onCompleted() {
        Platform.runLater(item::updateCompleted);
    }

    @Override
    public void onFailed(Throwable onFailure) {
        Platform.runLater(() -> item.updateFailed(onFailure));
    }
}
