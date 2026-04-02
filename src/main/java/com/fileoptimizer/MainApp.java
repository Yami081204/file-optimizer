package com.fileoptimizer;

import com.fileoptimizer.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            AppConfig appConfig = new AppConfig();

            String fxmlPath = "/fxml/main-view.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML Resource missing: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(appConfig::resolveController);

            Scene scene = new Scene(loader.load(), 1100, 650);

            String cssPath = "/css/style.css";
            URL cssUrl = getClass().getResource(cssPath);
            
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setTitle("File Optimizer");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
