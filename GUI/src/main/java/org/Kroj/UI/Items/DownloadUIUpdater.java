package org.Kroj.UI.Items;

import javafx.application.Platform;
import org.kroj.Core.Download.Download;
import org.kroj.Core.Download.DownloadListener;
import org.kroj.Core.Download.Manager;
import org.kroj.Core.Config.Initializer;
import org.kroj.Core.Network.Interface.NetworkInterfaces;
import org.kroj.Core.Tools.String.SizeManager;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadUIUpdater implements DownloadListener {

    private final DownloadItem dlItem;
    private final Download dl;
    private long lastUIUpdateTime = 0;

    public final AtomicBoolean isPaused = new AtomicBoolean(false);
    public final AtomicBoolean isStarted = new AtomicBoolean(false);
    public final AtomicBoolean isFinished = new AtomicBoolean(false);

    private Runnable statusChangeListener;

    public DownloadUIUpdater(DownloadItem dlItem, URI url) {
        this.dlItem = dlItem;
        this.dl = Manager.getInstance().makeDownload(
                url,
                Initializer.DOWNLOAD_FOLDER,
                this,
                NetworkInterfaces.getDevices().toArray(String[]::new)
        );
    }

    public void setStatusChangeListener(Runnable listener) {
        this.statusChangeListener = listener;
    }

    private void onStatusChanged() {
        if (statusChangeListener != null) {
            Platform.runLater(statusChangeListener);
        }
    }

    public void start() {
        if (isStarted.compareAndSet(false, true)) {
            dl.start();
            onStatusChanged();
        }
    }

    public void pause() {
        if (isStarted.get() && isPaused.compareAndSet(false, true)) {
            dl.pause();
        }
    }

    public void resume() {
        if (isStarted.get() && isPaused.compareAndSet(true, false)) {
            dl.resume();
            onStatusChanged();
        }
    }

    @Override
    public void onReady(String fileName, long size) {
        Platform.runLater(() -> {
            dlItem.nameProperty().set(fileName);
            dlItem.statusProperty().set("Connected (" + SizeManager.formatSize(size) + ")");
        });
    }

    @Override
    public void onProgress(long current, long total, double speed) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUIUpdateTime < Initializer.MIN_UI_UPDATE && current < total) {
            return;
        }
        lastUIUpdateTime = currentTime;
        double progress = (total > 0) ? ((double) current / total) : 0.0;

        Platform.runLater(() -> {
            dlItem.progressProperty().set(progress);
            dlItem.speedProperty().set(SizeManager.formatSpeed(speed*8));
            dlItem.statusProperty().set("Downloading (" + SizeManager.formatSize(current) + " / " + SizeManager.formatSize(total) + ")");
        });
    }

    @Override
    public void onPaused(long lastByte, long total) {
        Platform.runLater(() -> {
            dlItem.speedProperty().set("0 B/s");
            dlItem.statusProperty().set("Paused");
            onStatusChanged();
        });
    }

    @Override
    public void onCompleted() {
        isFinished.set(true);
        Platform.runLater(() -> {
            dlItem.progressProperty().set(1.0);
            dlItem.speedProperty().set("0 B/s");
            dlItem.statusProperty().set("Download Complete!");
            onStatusChanged();
        });
    }

    @Override
    public void onFailed(Throwable onFailure) {
        isFinished.set(true);
        Platform.runLater(() -> {
            dlItem.progressProperty().set(0.0);
            dlItem.speedProperty().set("0 B/s");
            dlItem.statusProperty().set("Download Failed");
            onStatusChanged();
        });
    }
}