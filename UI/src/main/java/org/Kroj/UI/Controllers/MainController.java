package org.Kroj.UI.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.Kroj.UI.Items.DownloadItem;

public class MainController {

    @FXML
    private TextField urlField;

    @FXML
    private VBox downloadListContainer;

    @FXML
    public void initialize() {
        // Setup Ctrl+V clipboard listener on scene when ready
        javafx.application.Platform.runLater(() -> {
            if (urlField.getScene() != null) {
                Scene scene = urlField.getScene();
                KeyCombination ctrlV = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
                scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (ctrlV.match(event) && !(event.getTarget() instanceof TextField)) {
                        tryAddFromClipboard();
                        event.consume();
                    }
                });
            }
        });
    }

    @FXML
    private void tryAddFromClipboard() {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                String text = clipboard.getString().trim();
                if (text.startsWith("http://") || text.startsWith("https://")) {
                    urlField.setText(text);
                    handleStartDownload();
                }
            }
        } catch (Exception ignored) {}
    }

    @FXML
    private void handleOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/SettingsLayout.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("JVDM Settings");
            stage.setScene(new Scene(root, 450, 420));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStartDownload() {
        String urlStr = urlField.getText();
        if (urlStr == null || urlStr.trim().isEmpty()) {
            return;
        }

        DownloadItem item = new DownloadItem(urlStr.trim(), downloadListContainer);
        downloadListContainer.getChildren().add(0, item);
        urlField.clear();
    }
}
