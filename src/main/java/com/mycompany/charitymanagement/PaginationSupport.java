package com.mycompany.charitymanagement;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.InvalidationListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class PaginationSupport<T> {

    private static final int DEFAULT_PAGE_SIZE = 15;

    private final TableView<T> tableView;
    private final FilteredList<T> filteredList;
    private final int pageSize;
    private int currentPage;

    private final Label pageLabel;
    private final Button prevBtn;
    private final Button nextBtn;

    public PaginationSupport(TableView<T> tableView, FilteredList<T> filteredList, int pageSize, HBox container) {
        this.tableView = tableView;
        this.filteredList = filteredList;
        this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        this.currentPage = 0;

        pageLabel = new Label();
        pageLabel.getStyleClass().add("muted-text");

        Label countLabel = new Label();
        countLabel.getStyleClass().add("muted-text");

        prevBtn = new Button("◀");
        prevBtn.getStyleClass().add("quick-button");
        nextBtn = new Button("▶");
        nextBtn.getStyleClass().add("quick-button");

        prevBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        });
        nextBtn.setOnAction(e -> {
            if ((currentPage + 1) * pageSize < filteredList.size()) {
                currentPage++;
                updatePage();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(prevBtn, pageLabel, spacer, countLabel, nextBtn);
        container.setSpacing(8);

        filteredList.addListener((InvalidationListener) obs -> Platform.runLater(() -> {
            currentPage = 0;
            updatePage();
        }));

        updatePage();
    }

    public PaginationSupport(TableView<T> tableView, FilteredList<T> filteredList, HBox container) {
        this(tableView, filteredList, DEFAULT_PAGE_SIZE, container);
    }

    private void updatePage() {
        int total = filteredList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / (double) pageSize));
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int from = currentPage * pageSize;
        int to = Math.min(from + pageSize, total);

        ObservableList<T> pageItems = FXCollections.observableArrayList();
        for (int i = from; i < to; i++) {
            pageItems.add(filteredList.get(i));
        }

        tableView.setItems(pageItems);
        tableView.refresh();

        pageLabel.setText(" Trang " + (currentPage + 1) + "/" + totalPages + " ");
        prevBtn.setDisable(currentPage <= 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);
    }
}
