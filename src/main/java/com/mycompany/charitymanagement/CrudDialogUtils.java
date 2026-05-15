package com.mycompany.charitymanagement;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class CrudDialogUtils {

    private CrudDialogUtils() {
    }

    public static String[] showForm(String title, String[] labels, String[] values) {
        Scene scene = App.getScene();
        if (scene == null || !Platform.isFxApplicationThread()) {
            return null;
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        titleLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));
        grid.getStyleClass().add("form-card");

        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("muted-text");

            TextField field = new TextField(valueAt(values, i));
            field.setPromptText(labels[i]);
            field.getStyleClass().addAll("input-field", "wide-input-field");
            field.setMaxWidth(Double.MAX_VALUE);
            fields[i] = field;

            grid.add(label, 0, i);
            grid.add(field, 1, i);
            GridPane.setHgrow(field, Priority.ALWAYS);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(Math.min(520, 58 * labels.length + 45));
        scrollPane.getStyleClass().add("content-scroll");

        Button saveButton = new Button("Lưu");
        saveButton.getStyleClass().add("primary-button");
        Button cancelButton = new Button("Hủy");
        cancelButton.getStyleClass().add("quick-button");

        HBox actions = new HBox(12, cancelButton, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, titleLabel, scrollPane, actions);
        card.getStyleClass().add("detail-card");
        card.setMaxWidth(720);
        card.setMaxHeight(620);

        StackPane overlay = DetailDialogUtils.showCard(scene, card);
        if (overlay == null) {
            return null;
        }

        saveButton.setOnAction(event -> {
            String[] result = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                result[i] = fields[i].getText() == null ? "" : fields[i].getText().trim();
            }
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, result);
        });
        cancelButton.setOnAction(event -> {
            DetailDialogUtils.closeOverlay(overlay);
            Platform.exitNestedEventLoop(overlay, null);
        });

        Platform.runLater(fields.length > 0 ? fields[0]::requestFocus : card::requestFocus);
        return (String[]) Platform.enterNestedEventLoop(overlay);
    }

    private static String valueAt(String[] values, int index) {
        return values != null && index < values.length && values[index] != null ? values[index] : "";
    }
}
