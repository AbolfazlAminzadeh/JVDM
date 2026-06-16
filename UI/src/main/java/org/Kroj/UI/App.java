package org.Kroj;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.Kroj.Controllers.MainController;

import java.io.IOException;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class App extends Application {

    private MainController controller;

    private static App app;

    public static App getInstance() {
        return app;
    }

    @Override
    public void start(Stage primaryStage) {
        app = this;
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/UI/MainLayout.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            controller = loader.getController();

            setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN),() -> Platform.runLater(controller::addDownloadClipboard));

            primaryStage.setMinHeight(400);
            primaryStage.setMinWidth(600);

            primaryStage.setTitle("JVDM");
            primaryStage.setScene(scene);

            primaryStage.setOnHidden(ev -> System.exit(0));

            primaryStage.show();

        } catch (IOException e) {
            logger.info().append("Error while loading file").nextLine().append(e).nextLine();
            if (e.getStackTrace().length > 0) logger.debug().append(e.getStackTrace()).nextLine();
        }
    }

    public MainController getController() {
        return controller;
    }

}
