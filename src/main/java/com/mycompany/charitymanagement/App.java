package com.mycompany.charitymanagement;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private static final double LOGIN_WIDTH = 420;
    private static final double LOGIN_HEIGHT = 560;
    private static final double APP_WIDTH = 1180;
    private static final double APP_HEIGHT = 700;

    private static Scene scene;
    private static Stage primaryStage;

    private static BorderPane shell;
    private static SidebarController sidebarController;
    private static boolean shellReady;

    private static final Set<String> ADMIN_SCREENS = Set.of(
        "secondary", "activities", "participants", "sponsors", "donations",
        "operations", "content", "reports", "training", "inventory",
        "expense", "screening", "alert"
    );

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("primary"), LOGIN_WIDTH, LOGIN_HEIGHT);
        scene.getStylesheets().add(App.class.getResource("/com/mycompany/charitymanagement/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Hệ thống quản lý hoạt động từ thiện");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        if (ADMIN_SCREENS.contains(fxml)) {
            navigateAdmin(fxml);
        } else {
            scene.setRoot(loadFXML(fxml));
        }
        updateStageSize(fxml);
    }

    private static void navigateAdmin(String fxml) throws IOException {
        if (!shellReady) {
            shell = new BorderPane();
            URL sidebarUrl = App.class.getResource("/fxml/sidebar.fxml");
            if (sidebarUrl == null) throw new IOException("Không tìm thấy sidebar.fxml");
            FXMLLoader loader = new FXMLLoader(sidebarUrl);
            VBox sidebar = loader.load();
            sidebarController = loader.getController();
            shell.setLeft(sidebar);
            shellReady = true;
        }
        Parent loaded = loadFXML(fxml);
        Node center = loaded instanceof BorderPane ? ((BorderPane) loaded).getCenter() : loaded;
        shell.setCenter(center);
        sidebarController.setActive(fxml);
        if (scene.getRoot() != shell) {
            scene.setRoot(shell);
        }
    }

    static Scene getScene() {
        return scene;
    }

    private static void updateStageSize(String fxml) {
        if (primaryStage == null) return;
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
        if (resource == null) throw new IOException("Không tìm thấy giao diện: " + path);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
