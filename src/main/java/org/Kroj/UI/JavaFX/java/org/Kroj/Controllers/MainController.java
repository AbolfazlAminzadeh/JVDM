package org.Kroj.UI.JavaFX.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.VBox;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.String.SizeManager;
import org.Kroj.Core.Tools.URL.URL;
import org.Kroj.UI.JavaFX.Items.DownloadItem;
import org.Kroj.UI.JavaFX.Items.DownloadUIUpdater;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class MainController {

    @FXML public Button settings;
    @FXML public VBox detailsPanel;
    @FXML public TableView<DownloadItem> tableView;
    @FXML public TableColumn<DownloadItem,String> nameCol;
    @FXML public TableColumn<DownloadItem,Double> progressCol;
    @FXML public TableColumn<DownloadItem,String> speedCol;
    @FXML private Label detailName;
    @FXML private Label detailStatus;
    @FXML private Label detailProgress;
    @FXML private ProgressBar detailBar;
    @FXML private Label detailSpeed;

    private final ObservableList<DownloadItem> downloads = FXCollections.observableArrayList();

    private final Map<String, DownloadUIUpdater> updaters = new HashMap<>();
    private final Map<String, URI> links = new HashMap<>();

    public void initialize() {
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        speedCol.setCellValueFactory(data -> data.getValue().speedProperty());
        progressCol.setCellValueFactory(data -> data.getValue().progressProperty().asObject());

        progressCol.setCellFactory(_ -> new TableCell<>() {
            private final ProgressBar pb = new ProgressBar();
            {
                pb.setMaxWidth(Double.MAX_VALUE);
                pb.getStyleClass().add("table-progress");
            }
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item,empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                pb.setProgress(item);
                setGraphic(pb);
            }
        });

        tableView.setItems(downloads);

        tableView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            bindViewDetail(newValue);
            playDetailAnimation();
        }));

    }

    public void bindViewDetail(DownloadItem newValue) {
        detailName.textProperty().unbind();
        detailStatus.textProperty().unbind();
        detailSpeed.textProperty().unbind();
        detailBar.progressProperty().unbind();
        detailProgress.textProperty().unbind();

        detailName.textProperty().bind(newValue.nameProperty());
        detailStatus.textProperty().bind(newValue.statusProperty());
        detailSpeed.textProperty().bind(newValue.speedProperty());
        detailBar.progressProperty().bind(newValue.progressProperty());
        detailProgress.textProperty().bind(newValue.progressProperty().multiply(100).asString("%.1f%%"));

    }

    private void playDetailAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(250), detailsPanel);
        fade.setFromValue(0.4);
        fade.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(250), detailsPanel);
        slide.setFromX(20);
        slide.setToX(0);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
    }

    public void onStart() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            logger.append("No File Selected To Start :|");
            return;
        }
        URI uri = links.get(selected.getFileName());
        DownloadUIUpdater updater = updaters.get(selected.getFileName());

        if (uri == null || updater == null) {
            logger.append("Download Link / Updator is invalid").nextLine();
            return;
        }

        selected.statusProperty().set("Connecting To Server...");

        Manager.getInstance().startDownload(uri, Initializer.DOWNLOAD_FOLDER, updater, BindToDeviceHandler.getDevices().toArray(String[]::new));
    }

    public void onPause() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.statusProperty().set("Paused");
            selected.speedProperty().set("0 KB/s");
        }
    }
    public void onDelete() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            downloads.remove(selected);
            updaters.remove(selected.getFileName());

            detailName.textProperty().unbind();
            detailStatus.textProperty().unbind();
            detailSpeed.textProperty().unbind();
            detailBar.progressProperty().unbind();
            detailProgress.textProperty().unbind();

            detailName.setText("Select a download");
            detailStatus.setText("Status: -");
            detailProgress.setText("Progress: -");
            detailBar.setProgress(0.0);
            detailSpeed.setText("Speed: -");
        }
    }
    public void displayAll() {}
    public void displayDownloading() {}
    public void displayComplete() {}
    public void displayPaused() {}
    public void displaySettings() {}

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
            logger.append("Is this a correct url?").nextLine().append(url).nextLine();
            return;
        }
        String fileName = Manager.getFileName(safeURL,null);
        DownloadItem item = new DownloadItem(fileName);
        item.statusProperty().set("Idle (? Size)");

        if (downloads.contains(item)) {
            tableView.getSelectionModel().select(item);
            return;
        }

        links.put(fileName,safeURL);
        updaters.put(fileName,new DownloadUIUpdater(item));
        downloads.addFirst(item);

        tableView.getSelectionModel().select(item);
    }
}