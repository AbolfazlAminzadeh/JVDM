package org.Kroj.UI.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.Kroj.Core.Tools.String.FileName;
import org.Kroj.Core.Tools.URL.URL;
import org.Kroj.UI.Items.DownloadItem;
import org.Kroj.UI.Items.DownloadUIUpdater;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class MainController {

    public Button settings;
    public VBox detailsPanel;
    public TableView<DownloadItem> tableView;
    public TableColumn<DownloadItem, String> nameCol;
    public TableColumn<DownloadItem, Double> progressCol;
    public TableColumn<DownloadItem, String> speedCol;

    public Button startButton;
    public Button pauseButton;
    public Button deleteButton;

    @FXML private Label detailName;
    @FXML private Label detailStatus;
    @FXML private Label detailProgress;
    @FXML private ProgressBar detailBar;
    @FXML private Label detailSpeed;

    private final ObservableList<DownloadItem> downloads = FXCollections.observableArrayList();
    private final Map<String, DownloadUIUpdater> updaters = new HashMap<>();

    public void initialize() {
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        speedCol.setCellValueFactory(data -> data.getValue().speedProperty());
        progressCol.setCellValueFactory(data -> data.getValue().progressProperty().asObject());

        progressCol.setCellFactory(_ -> new TableCell<>() {
            private final ProgressBar pb = new TableCollectionProgressBar();

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    pb.setProgress(item);
                    setGraphic(pb);
                }
            }
        });

        tableView.setItems(downloads);

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                clearDetailsPanel();
                disableButtons();
                return;
            }
            bindViewDetail(newValue);
            updateButtonStates(newValue);
            playDetailAnimation();
        });

        disableButtons();
    }

    private void bindViewDetail(DownloadItem newValue) {
        unbindDetails();

        detailName.textProperty().bind(newValue.nameProperty());
        detailStatus.textProperty().bind(newValue.statusProperty());
        detailSpeed.textProperty().bind(newValue.speedProperty());
        detailBar.progressProperty().bind(newValue.progressProperty());
        detailProgress.textProperty().bind(newValue.progressProperty().multiply(100).asString("%.1f%%"));
    }

    private void unbindDetails() {
        detailName.textProperty().unbind();
        detailStatus.textProperty().unbind();
        detailSpeed.textProperty().unbind();
        detailBar.progressProperty().unbind();
        detailProgress.textProperty().unbind();
    }

    private void clearDetailsPanel() {
        unbindDetails();
        detailName.setText("Select a download");
        detailStatus.setText("Status: -");
        detailProgress.setText("Progress: -");
        detailBar.setProgress(0.0);
        detailSpeed.setText("Speed: -");
    }

    private void updateButtonStates(DownloadItem item) {
        if (item == null) {
            disableButtons();
            return;
        }

        DownloadUIUpdater updater = updaters.get(item.getFileName());
        if (updater == null) {
            disableButtons();
            return;
        }

        deleteButton.setDisable(false);

        if (updater.isFinished.get()) {
            startButton.setDisable(true);
            pauseButton.setDisable(true);
            startButton.setText("Start");
            pauseButton.setText("Pause");
            return;
        }

        boolean running = updater.isStarted.get();
        boolean paused = updater.isPaused.get();

        if (!running) {
            startButton.setDisable(false);
            startButton.setText("Start");
            pauseButton.setDisable(true);
            pauseButton.setText("Pause");
        } else {
            startButton.setDisable(true);
            pauseButton.setDisable(false);

            if (paused) {
                pauseButton.setText("Resume");
                renewButtonStyles(pauseButton, "pauseButton", "resumeButton");
            } else {
                pauseButton.setText("Pause");
                renewButtonStyles(pauseButton, "resumeButton", "pauseButton");
            }
        }
    }

    private void renewButtonStyles(Button btn, String classToRemove, String classToAdd) {
        btn.getStyleClass().remove(classToRemove);
        if (!btn.getStyleClass().contains(classToAdd)) {
            btn.getStyleClass().add(classToAdd);
        }
    }

    private void disableButtons() {
        startButton.setDisable(true);
        deleteButton.setDisable(true);
        pauseButton.setDisable(true);
        startButton.setText("Start");
        pauseButton.setText("Pause");
    }

    private void playDetailAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(150), detailsPanel);
        fade.setFromValue(0.6);
        fade.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(150), detailsPanel);
        slide.setFromX(10);
        slide.setToX(0);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
    }

    public void onStart() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        DownloadUIUpdater updater = updaters.get(selected.getFileName());
        if (updater != null) {
            updater.setStatusChangeListener(() -> {
                DownloadItem current = tableView.getSelectionModel().getSelectedItem();
                if (current != null && current.getFileName().equals(selected.getFileName())) {
                    updateButtonStates(current);
                }
            });
            updater.start();
            updateButtonStates(selected);
        }
    }

    public void onPause() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        DownloadUIUpdater updater = updaters.get(selected.getFileName());
        if (updater == null) return;

        if (updater.isPaused.get()) {
            updater.resume();
        } else {
            updater.pause();
        }
        updateButtonStates(selected);
    }

    public void onDelete() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DownloadUIUpdater updater = updaters.get(selected.getFileName());
            if (updater != null) {
                updater.pause();
            }

            updaters.remove(selected.getFileName());
            downloads.remove(selected);
        }
    }

    public void addDownloadClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            addDownloadLink(clipboard.getString());
        } else {
            logger.append("Clipboard is Empty").nextLine();
        }
    }

    public void addDownloadLink(String url) {
        URI safeURL = URL.getSafeURI(url);
        if (safeURL == null || !url.contains("http")) {
            logger.append("Is this a correct url? ").append(url).nextLine();
            return;
        }
        String fileName = FileName.getFileName(safeURL, null);
        DownloadItem item = new DownloadItem(fileName);
        item.statusProperty().set("Idle (? Size)");

        if (downloads.contains(item)) {
            tableView.getSelectionModel().select(item);
            return;
        }

        DownloadUIUpdater updater = new DownloadUIUpdater(item, safeURL);
        updaters.put(fileName, updater);
        downloads.addFirst(item);

        tableView.getSelectionModel().select(item);
    }

    private static class TableCollectionProgressBar extends ProgressBar {
        public TableCollectionProgressBar() {
            setMaxWidth(Double.MAX_VALUE);
            getStyleClass().add("table-progress");
        }
    }

    public void displayAll() {}
    public void displayDownloading() {}
    public void displayComplete() {}
    public void displayPaused() {}
    public void displaySettings() {}
}