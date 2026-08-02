package org.Kroj.UI.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.UI.Config.AnimationConfig;

public class SettingsController {

    @FXML
    private Spinner<Integer> threadsSpinner;

    @FXML
    private Spinner<Integer> progressIntervalSpinner;

    @FXML
    private Spinner<Integer> timeoutSpinner;

    @FXML
    private Spinner<Double> animSpeedSpinner;

    @FXML
    private CheckBox h2CheckBox;

    @FXML
    public void initialize() {
        threadsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 128, Initializer.DOWNLOADER_THREADS));
        progressIntervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(50, 2000, Initializer.PROGRESS_INTERVAL));
        timeoutSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 60000, Initializer.CONNECTION_TIMEOUT));
        animSpeedSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.25, 3.0, AnimationConfig.getSpeedMultiplier(), 0.25));
        h2CheckBox.setSelected(Initializer.H2_PREFER);
    }

    @FXML
    private void handleSave() {
        AnimationConfig.setSpeedMultiplier(animSpeedSpinner.getValue());
        Stage stage = (Stage) threadsSpinner.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) threadsSpinner.getScene().getWindow();
        stage.close();
    }
}
