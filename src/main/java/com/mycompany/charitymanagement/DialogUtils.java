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

    public static boolean confirm(String message) {
        return confirmWithDetails(null, message);
    }

    public static boolean confirmWithDetails(String details, String message) {
        Scene scene = App.getScene();
        if (scene == null || !Platform.isFxApplicationThread()) {
            return false;
        }

        Label titleLabel = new Label("Xác nhận");
        titleLabel.getStyleClass().addAll("page-title", "dialog-warning-title");
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("dashboard-main-text");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(520);

        VBox contentBox = new VBox(8, messageLabel);
        if (details != null && !details.isEmpty()) {
            Label detailLabel = new Label(details);
            detailLabel.getStyleClass().add("delete-detail-text");
            contentBox.getChildren().add(detailLabel);
        }

        Button cancelButton = new Button("Hủy");
        cancelButton.getStyleClass().add("quick-button");
        Button okButton = new Button("Đồng ý");
        okButton.getStyleClass().add("danger-button");

        HBox actions = new HBox(12, cancelButton, okButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, titleLabel, contentBox, actions);
        card.getStyleClass().add("detail-card");

        StackPane overlay = DetailDialogUtils.showCard(scene, card);
        if (overlay == null) {
            return false;
        }

        cancelButton.setOnAction(event -> {
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, Boolean.FALSE);
        });
        okButton.setOnAction(event -> {
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, Boolean.TRUE);
        });
        Platform.runLater(cancelButton::requestFocus);
        return Boolean.TRUE.equals(Platform.enterNestedEventLoop(overlay));
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
