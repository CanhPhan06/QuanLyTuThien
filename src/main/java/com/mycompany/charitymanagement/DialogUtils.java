package com.mycompany.charitymanagement;

import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class DialogUtils {

    private DialogUtils() {
    }

    public static void info(String message) {
        show("Thông báo", message, "dialog-info-title");
    }

    public static void warning(String message) {
        show("Cảnh báo", message, "dialog-warning-title");
    }

    private static void show(String title, String message, String titleStyle) {
        Scene scene = App.getScene();
        if (scene == null || !Platform.isFxApplicationThread()) {
            return;
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("page-title", titleStyle);
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("dashboard-main-text");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(520);

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("primary-button");

        HBox actions = new HBox(closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, titleLabel, messageLabel, actions);
        card.getStyleClass().add("detail-card");
        card.setMaxWidth(600);

        StackPane overlay = DetailDialogUtils.showCard(scene, card);
        if (overlay == null) {
            return;
        }
        closeButton.setOnAction(event -> {
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, null);
        });
        Platform.runLater(closeButton::requestFocus);
        Platform.enterNestedEventLoop(overlay);
    }
}
