package org.Kroj.UI.Items;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.Kroj.Core.Network.Download.Download;
import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.NI.NetworkInterfaces;
import org.Kroj.Core.Tools.String.SizeManager;
import org.Kroj.UI.Config.AnimationConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DownloadItem extends VBox {

    private final String url;
    private Download download;
    private final Label titleLabel;
    private final Label statusLabel = new Label("Idle - Select devices and click Start");
    private final Label speedLabel = new Label("0.00 B/s");

    private final VBox progressContainer = new VBox(5);
    private final ProgressBar progressBar = new ProgressBar(0);
    private final HBox singleLinePartedBar = new HBox(0); // IDM continuous connected fill

    private final Button startBtn = new Button("Start");
    private final Button pauseResumeBtn = new Button("Pause");
    private final Button toggleViewBtn = new Button("🔀 Parted View");
    private final MenuButton deviceMenuButton = new MenuButton("🌐 Devices");
    private final List<CheckBox> deviceCheckBoxes = new ArrayList<>();

    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isPartedView = false;
    private String detectedFileName = "";
    private double lastKnownProgress = 0.0;
    private long lastPartedUpdateTime = 0;

    public DownloadItem(String url, VBox parentContainer) {
        this.url = url;
        this.titleLabel = new Label(url);

        getStyleClass().add("download-card");
        setSpacing(10);
        setPadding(new Insets(15));

        titleLabel.getStyleClass().add("card-title");
        statusLabel.getStyleClass().add("card-status");
        speedLabel.getStyleClass().add("card-speed");

        progressBar.getStyleClass().add("neon-progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        singleLinePartedBar.getStyleClass().add("single-progress-track");
        singleLinePartedBar.setAlignment(Pos.CENTER_LEFT);
        singleLinePartedBar.setMaxWidth(Double.MAX_VALUE);
        singleLinePartedBar.setPrefHeight(14);

        progressContainer.getChildren().add(progressBar);

        // Setup Device Checkbox Menu
        VBox menuBox = new VBox(5);
        menuBox.setPadding(new Insets(5));
        try {
            List<String> devices = NetworkInterfaces.getDevices();
            if (devices != null && !devices.isEmpty()) {
                for (String dev : devices) {
                    CheckBox cb = new CheckBox(dev);
                    cb.setSelected(true);
                    cb.getStyleClass().add("settings-checkbox");
                    deviceCheckBoxes.add(cb);
                    menuBox.getChildren().add(cb);
                }
            } else {
                CheckBox cb = new CheckBox("Default");
                cb.setSelected(true);
                deviceCheckBoxes.add(cb);
                menuBox.getChildren().add(cb);
            }
        } catch (Exception e) {
            CheckBox cb = new CheckBox("Default");
            cb.setSelected(true);
            deviceCheckBoxes.add(cb);
            menuBox.getChildren().add(cb);
        }

        CustomMenuItem menuItem = new CustomMenuItem(menuBox, false);
        deviceMenuButton.getItems().add(menuItem);
        deviceMenuButton.getStyleClass().add("action-button");

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        VBox labelBox = new VBox(3, titleLabel, statusLabel);
        HBox.setHgrow(labelBox, Priority.ALWAYS);

        startBtn.getStyleClass().add("neon-button");
        startBtn.setOnAction(e -> startDownload());

        pauseResumeBtn.getStyleClass().add("action-button");
        pauseResumeBtn.setDisable(true);
        pauseResumeBtn.setOnAction(e -> togglePauseResume());

        toggleViewBtn.getStyleClass().add("action-button");
        toggleViewBtn.setOnAction(e -> toggleProgressView());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("cancel-button");
        cancelBtn.setOnAction(e -> cancelDownload(parentContainer));

        topBox.getChildren().addAll(labelBox, speedLabel, deviceMenuButton, toggleViewBtn, startBtn, pauseResumeBtn, cancelBtn);

        getChildren().addAll(topBox, progressContainer);

        // Lightweight entrance animation
        setOpacity(0);
        setTranslateY(-15);
        double animDur = 250 * AnimationConfig.getSpeedMultiplier();

        Timeline entrance = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(opacityProperty(), 0.0),
                new KeyValue(translateYProperty(), -15)
            ),
            new KeyFrame(Duration.millis(animDur),
                new KeyValue(opacityProperty(), 1.0),
                new KeyValue(translateYProperty(), 0, Interpolator.EASE_OUT)
            )
        );
        entrance.play();
    }

    private void startDownload() {
        if (isStarted) return;
        isStarted = true;
        startBtn.setDisable(true);
        pauseResumeBtn.setDisable(false);
        statusLabel.setText("Connecting & initializing...");

        List<String> selectedDevices = new ArrayList<>();
        for (CheckBox cb : deviceCheckBoxes) {
            if (cb.isSelected()) {
                selectedDevices.add(cb.getText());
            }
        }
        if (selectedDevices.isEmpty()) {
            selectedDevices.add("");
        }
        String[] deviceArray = selectedDevices.toArray(new String[0]);

        final DownloadItem itemRef = this;
        org.Kroj.Core.Network.Download.Handlers.DownloadListener listener = new org.Kroj.Core.Network.Download.Handlers.DownloadListener() {
            @Override
            public void onReady(String fileName, long size) {
                javafx.application.Platform.runLater(() -> itemRef.updateReady(fileName, size));
            }

            @Override
            public void onProgress(long current, long total, double speed) {
                javafx.application.Platform.runLater(() -> itemRef.updateProgress(current, total, speed));
            }

            @Override
            public void onPaused(long lastByte, long total) {
                javafx.application.Platform.runLater(() -> itemRef.updatePaused(lastByte, total));
            }

            @Override
            public void onCompleted() {
                javafx.application.Platform.runLater(itemRef::updateCompleted);
            }

            @Override
            public void onFailed(Throwable onFailure) {
                javafx.application.Platform.runLater(() -> itemRef.updateFailed(onFailure));
            }
        };

        this.download = Manager.getInstance().makeDownload(url, Initializer.DOWNLOAD_FOLDER, listener, deviceArray);
        this.download.start();
    }

    private void cancelDownload(VBox parentContainer) {
        if (download != null) {
            try {
                download.pause();
            } catch (Exception ignored) {}
        }
        if (!detectedFileName.isEmpty()) {
            try {
                Path targetPath = Path.of(Initializer.DOWNLOAD_FOLDER).resolve(detectedFileName);
                Files.deleteIfExists(targetPath);
            } catch (IOException ignored) {}
        }

        // Optimized smooth fade & slide up exit animation
        double animDur = 200 * AnimationConfig.getSpeedMultiplier();
        Timeline exitAnim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(opacityProperty(), 1.0),
                new KeyValue(translateYProperty(), 0)
            ),
            new KeyFrame(Duration.millis(animDur),
                new KeyValue(opacityProperty(), 0.0),
                new KeyValue(translateYProperty(), -20, Interpolator.EASE_OUT)
            )
        );
        exitAnim.setOnFinished(e -> parentContainer.getChildren().remove(this));
        exitAnim.play();
    }

    private void toggleProgressView() {
        isPartedView = !isPartedView;
        progressContainer.getChildren().clear();
        if (isPartedView) {
            progressContainer.getChildren().add(singleLinePartedBar);
            toggleViewBtn.setText("📊 Normal View");
        } else {
            progressContainer.getChildren().add(progressBar);
            toggleViewBtn.setText("🔀 Parted View");
        }
    }

    private void togglePauseResume() {
        if (download == null) return;
        if (isPaused) {
            download.resume();
            pauseResumeBtn.setText("Pause");
            statusLabel.setText("Resumed...");
            isPaused = false;
        } else {
            download.pause();
            pauseResumeBtn.setText("Resume");
            statusLabel.setText("Paused");
            isPaused = true;
        }
    }

    public void updateReady(String fileName, long size) {
        this.detectedFileName = fileName;
        titleLabel.setText(fileName);
        statusLabel.setText("Size: " + SizeManager.formatSize(size));
    }

    public void updateProgress(long current, long total, double speed) {
        if (total > 0) {
            double targetProgress = (double) current / total;

            progressBar.setProgress(targetProgress);
            lastKnownProgress = targetProgress;

            statusLabel.setText(String.format("Progress: %.1f%% (%s / %s)",
                    targetProgress * 100.0,
                    SizeManager.formatSize(current),
                    SizeManager.formatSize(total)));

            long now = System.currentTimeMillis();
            if (isPartedView && download != null && (now - lastPartedUpdateTime > 400)) {
                lastPartedUpdateTime = now;
                updatePartedSegments(total);
            }
        } else {
            progressBar.setProgress(-1);
            statusLabel.setText("Downloaded: " + SizeManager.formatSize(current));
        }
        speedLabel.setText(SizeManager.formatSpeed(speed));
    }

    private void updatePartedSegments(long total) {
        try {
            Field partsField = Download.class.getDeclaredField("parts");
            partsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Part> parts = (List<Part>) partsField.get(download);

            if (parts != null && !parts.isEmpty()) {
                if (singleLinePartedBar.getChildren().size() != parts.size()) {
                    singleLinePartedBar.getChildren().clear();
                    for (int i = 0; i < parts.size(); i++) {
                        Part part = parts.get(i);
                        long pStart = part.getStart();
                        long pEnd = part.getEnd();
                        long pLen = (pEnd > pStart) ? (pEnd - pStart + 1) : (total / Math.max(1, parts.size()));
                        double weight = Math.max(0.05, (double) pLen / total);

                        StackPane segment = new StackPane();
                        segment.getStyleClass().add("idm-part-segment");
                        HBox.setHgrow(segment, Priority.ALWAYS);
                        segment.setPrefWidth(weight * 400);

                        Region fill = new Region();
                        fill.getStyleClass().add("part-fill-" + getDeviceColorIndex(part.getDevice()));
                        fill.setMaxWidth(Double.MAX_VALUE);
                        long pCur = part.getCurrentBytes();
                        double pProg = pEnd > pStart ? Math.min(1.0, (double) pCur / pLen) : 0.5;
                        fill.setScaleX(Math.max(0.01, pProg));
                        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

                        segment.getChildren().add(fill);
                        singleLinePartedBar.getChildren().add(segment);
                    }
                } else {
                    for (int i = 0; i < parts.size() && i < singleLinePartedBar.getChildren().size(); i++) {
                        Part part = parts.get(i);
                        StackPane segment = (StackPane) singleLinePartedBar.getChildren().get(i);
                        if (!segment.getChildren().isEmpty() && segment.getChildren().get(0) instanceof Region fill) {
                            long pStart = part.getStart();
                            long pEnd = part.getEnd();
                            long pLen = (pEnd > pStart) ? (pEnd - pStart + 1) : (total / Math.max(1, parts.size()));
                            long pCur = part.getCurrentBytes();
                            double targetScale = pEnd > pStart ? Math.min(1.0, (double) pCur / pLen) : 0.5;
                            fill.setScaleX(Math.max(0.01, targetScale));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }
    }

    private int getDeviceColorIndex(String device) {
        String dev = device != null ? device : "";
        return Math.abs(dev.hashCode()) % 4;
    }

    public void updatePaused(long lastByte, long total) {
        statusLabel.setText("Paused at " + SizeManager.formatSize(lastByte));
        speedLabel.setText("0.00 B/s");
    }

    public void updateCompleted() {
        progressBar.setProgress(1.0);
        statusLabel.setText("Completed Successfully!");
        speedLabel.setText("0.00 B/s");
        startBtn.setDisable(true);
        pauseResumeBtn.setDisable(true);
    }

    public void updateFailed(Throwable t) {
        statusLabel.setText("Failed: " + t.getMessage());
        speedLabel.setText("ERR");
        startBtn.setDisable(false);
        pauseResumeBtn.setDisable(true);
    }
}
