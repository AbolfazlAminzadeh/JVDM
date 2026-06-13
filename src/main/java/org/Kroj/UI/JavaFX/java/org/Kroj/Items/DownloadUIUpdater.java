package org.Kroj.UI.JavaFX.Items;

import javafx.application.Platform;
import org.Kroj.Core.Network.Download.DownloadListener;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.String.SizeManager;

public class DownloadUIUpdater implements DownloadListener {

    private final DownloadItem dlItem;
    private long lastUIUpdateTime = 0;

    public DownloadUIUpdater(DownloadItem dlItem) {
        this.dlItem = dlItem;
    }

    @Override
    public void onReady(String fileName, long size) {
        Platform.runLater(() -> {
            dlItem.nameProperty().set(fileName);
            dlItem.statusProperty().set("Connected (Size: "+SizeManager.formatSize(size)+")");
        });
    }

    @Override
    public void onProgress(long current, long total, double speed) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUIUpdateTime < Initializer.MIN_UI_UPDATE && current < total) return;

        lastUIUpdateTime = currentTime;

        double progress = (total > 0) ? ((double)current / total) : 0.0;

        Platform.runLater(() -> {
            dlItem.progressProperty().set(progress);
            dlItem.speedProperty().set(SizeManager.formatSpeed(speed));
            dlItem.statusProperty().set("Downloading ("+SizeManager.formatSize(current)+" / "+SizeManager.formatSize(total)+")");
        });
    }

    @Override
    public void onCompleted() {
        Platform.runLater(() -> {
            dlItem.progressProperty().set(1);
            dlItem.speedProperty().set("0 B/S");
            dlItem.statusProperty().set("Download Complete!");
        });
    }

    @Override
    public void onFailed(Throwable onFailure) {
        Platform.runLater(() -> {
            dlItem.progressProperty().set(0);
            dlItem.speedProperty().set("0 B/S");
            dlItem.statusProperty().set("Download Failed");
        });
    }
}
