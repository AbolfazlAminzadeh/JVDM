package org.Kroj.UI.Items;

import javafx.application.Platform;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Handlers.DownloadListener;
import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.NI.NetworkInterfaces;
import org.Kroj.Core.Tools.String.SizeManager;

import java.net.URI;

public class DownloadUIUpdater implements DownloadListener {

    private final DownloadItem dlItem;
    private final Download dl;
    private long lastUIUpdateTime = 0;

    public DownloadUIUpdater(DownloadItem dlItem, URI url) {
        this.dlItem = dlItem;
        this.dl = Manager.getInstance().makeDownload(url, Initializer.DOWNLOAD_FOLDER, this, NetworkInterfaces.getDevices().toArray(String[]::new));
    }

    public void start() {
        dl.start();
    }

    public void pause() {
        dl.pause();
    }

    public void resume() {
        dl.resume();
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
    public void onPaused(long lastByte, long total) {
        Platform.runLater(() -> {
            dlItem.statusProperty().set("Paused");
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
