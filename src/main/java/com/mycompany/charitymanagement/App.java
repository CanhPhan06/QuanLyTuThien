package com.mycompany.charitymanagement;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final double LOGIN_WIDTH = 420;
    private static final double LOGIN_HEIGHT = 560;
    private static final double APP_WIDTH = 1180;
    private static final double APP_HEIGHT = 700;

    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("primary"), LOGIN_WIDTH, LOGIN_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Hệ thống quản lý hoạt động từ thiện");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        updateStageSize(fxml);
    }

    static Scene getScene() {
        return scene;
    }

    private static void updateStageSize(String fxml) {
        if (primaryStage == null) {
            return;
        }

        if (fxml.equals("primary")) {
            Platform.runLater(() -> {
                primaryStage.setFullScreen(false);
                primaryStage.setMaximized(false);
                primaryStage.setMinWidth(LOGIN_WIDTH);
                primaryStage.setMinHeight(LOGIN_HEIGHT);
                primaryStage.setWidth(LOGIN_WIDTH);
                primaryStage.setHeight(LOGIN_HEIGHT);
                primaryStage.centerOnScreen();
            });
            return;
        }

        primaryStage.setMinWidth(APP_WIDTH);
        primaryStage.setMinHeight(APP_HEIGHT);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreenExitHint("");
        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(true);
        });
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String path;
        switch (fxml) {
            case "activities":
            case "participants":
            case "sponsors":
            case "donations":
            case "operations":
            case "content":
            case "reports":
            case "training":
            case "inventory":
            case "expense":
            case "screening":
            case "alert":
            case "volunteer":
            case "sponsorportal":
                path = "/fxml/" + fxml + ".fxml";
                break;
            default:
                path = fxml + ".fxml";
                break;
        }

        URL resource = App.class.getResource(path);
        if (resource == null) {
            throw new IOException("Không tìm thấy giao diện: " + path);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
