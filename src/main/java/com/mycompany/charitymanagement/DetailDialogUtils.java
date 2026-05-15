package com.mycompany.charitymanagement;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class DetailDialogUtils {

    private static final String DETAIL_HOST_KEY = "charity.detail.host";
    private static StackPane activeOverlay;

    private DetailDialogUtils() {
    }

    public static void showDetails(String title, String[][] rows) {
        showDetails(null, title, rows, null);
    }

    public static void showDetails(Node ownerNode, String title, String[][] rows) {
        showDetails(ownerNode, title, rows, null);
    }

    public static void showDetails(Node ownerNode, String title, String[][] rows, Node actions) {
        Scene scene = ownerNode == null ? null : ownerNode.getScene();
        if (scene == null) {
            scene = App.getScene();
        }
        StackPane overlay = createOverlay(scene);
        if (overlay == null) {
            return;
        }

        VBox card = createDetailCard(title, rows, overlay, actions);
        showOverlay(scene, overlay, card);
    }

    static StackPane showCard(Scene scene, VBox card) {
        StackPane overlay = createOverlay(scene);
        if (overlay == null) {
            return null;
        }
        showOverlay(scene, overlay, card);
        return overlay;
    }

    static void closeActiveOverlay() {
        if (activeOverlay != null) {
            closeOverlay(activeOverlay);
        }
    }

    static void closeOverlay(StackPane overlay) {
        if (overlay.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) overlay.getParent();
            parent.getChildren().remove(overlay);
        }
        if (activeOverlay == overlay) {
            activeOverlay = null;
        }
    }

    private static VBox createDetailCard(String title, String[][] rows, StackPane overlay, Node actions) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(22));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        titleLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(12);
        grid.getStyleClass().add("form-card");

        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.getStyleClass().add("muted-text");
            key.setMinWidth(150);

            Label value = new Label(empty(rows[i][1]));
            value.getStyleClass().add("dashboard-main-text");
            value.setWrapText(true);
            value.setMaxWidth(Double.MAX_VALUE);

            grid.add(key, 0, i);
            grid.add(value, 1, i);
            GridPane.setHgrow(value, Priority.ALWAYS);
        }

        root.getChildren().add(grid);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("quick-button");
        closeButton.setOnAction(event -> closeOverlay(overlay));

        Node actionNode = actions == null ? defaultActions(closeButton) : actions;
        VBox card = new VBox(16, titleLabel, scrollPane, actionNode);
        card.getStyleClass().add("detail-card");
        card.setMaxWidth(760);
        card.setMaxHeight(590);
        scrollPane.setMaxHeight(430);
        return card;
    }

    private static HBox defaultActions(Button closeButton) {
        HBox actions = new HBox(closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        return actions;
    }

    private static StackPane createOverlay(Scene scene) {
        if (scene == null) {
            return null;
        }
        closeActiveOverlay();

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("detail-overlay");
        overlay.setPickOnBounds(true);
        overlay.prefWidthProperty().bind(scene.widthProperty());
        overlay.prefHeightProperty().bind(scene.heightProperty());
        return overlay;
    }

    private static void showOverlay(Scene scene, StackPane overlay, VBox card) {
        StackPane host = detailHost(scene);
        overlay.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        activeOverlay = overlay;
        host.getChildren().add(overlay);
        overlay.toFront();
        card.requestFocus();
    }

    private static StackPane detailHost(Scene scene) {
        Parent currentRoot = scene.getRoot();
        if (currentRoot instanceof StackPane
                && Boolean.TRUE.equals(currentRoot.getProperties().get(DETAIL_HOST_KEY))) {
            return (StackPane) currentRoot;
        }

        StackPane host = new StackPane(currentRoot);
        host.getProperties().put(DETAIL_HOST_KEY, true);
        String stylesheet = DetailDialogUtils.class
                .getResource("/com/mycompany/charitymanagement/style.css")
                .toExternalForm();
        if (!host.getStylesheets().contains(stylesheet)) {
            host.getStylesheets().add(stylesheet);
        }
        scene.setRoot(host);
        return host;
    }

    private static String empty(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }
}
