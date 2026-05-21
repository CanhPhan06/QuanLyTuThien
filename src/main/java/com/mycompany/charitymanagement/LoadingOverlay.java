package com.mycompany.charitymanagement;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class LoadingOverlay {

    private static StackPane activeOverlay;

    private LoadingOverlay() {
    }

    public static void show(String message) {
        Platform.runLater(() -> {
            Scene scene = App.getScene();
            if (scene == null) {
                return;
            }

            hide();

            Label msgLabel = new Label(message);
            msgLabel.getStyleClass().add("dashboard-main-text");

            ProgressIndicator indicator = new ProgressIndicator();
            indicator.setPrefSize(44, 44);

            VBox box = new VBox(16, indicator, msgLabel);
            box.getStyleClass().add("loading-card");
            box.setMaxWidth(280);
            box.setMaxHeight(160);

            activeOverlay = new StackPane(box);
            activeOverlay.getStyleClass().add("detail-overlay");
            activeOverlay.prefWidthProperty().bind(scene.widthProperty());
            activeOverlay.prefHeightProperty().bind(scene.heightProperty());

            StackPane host = DetailDialogUtils.detailHost(scene);
            host.getChildren().add(activeOverlay);
            activeOverlay.toFront();
        });
    }

    public static void hide() {
        Platform.runLater(() -> {
            if (activeOverlay != null && activeOverlay.getParent() != null) {
                ((StackPane) activeOverlay.getParent()).getChildren().remove(activeOverlay);
            }
            activeOverlay = null;
        });
    }

    public static void run(String message, Runnable action) {
        show(message);
        new Thread(() -> {
            try {
                action.run();
            } finally {
                hide();
            }
        }).start();
    }
}
