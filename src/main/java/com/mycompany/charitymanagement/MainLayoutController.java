package com.mycompany.charitymanagement;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainLayoutController {

    @FXML
    private Label lblSidebarTitle;

    @FXML
    private Button btnHome;
    @FXML
    private Button btnActivities;
    @FXML
    private Button btnParticipants;
    @FXML
    private Button btnSponsors;
    @FXML
    private Button btnDonations;
    @FXML
    private Button btnOperations;
    @FXML
    private Button btnContent;
    @FXML
    private Button btnReports;
    @FXML
    private Button btnLogout;

    @FXML
    private StackPane contentArea;

    private UserAccount currentUser;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            currentUser = new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Người quản lý hệ thống", "TK001");
        }
        NavigationService.setMainLayoutController(this);
        configureRole();
    }

    private void configureRole() {
        boolean isAdmin = currentUser.isAdmin();

        setVisibleManaged(btnParticipants, isAdmin);
        setVisibleManaged(btnSponsors, isAdmin);
        setVisibleManaged(btnDonations, isAdmin);
        setVisibleManaged(btnOperations, isAdmin);
        setVisibleManaged(btnContent, isAdmin);
        setVisibleManaged(btnReports, isAdmin);
    }

    public void setContent(Node content) {
        contentArea.getChildren().setAll(content);
    }

    public void updateActiveButton(String viewName) {
        List<Button> allButtons = Arrays.asList(
            btnHome, btnActivities, btnParticipants, btnSponsors,
            btnDonations, btnOperations, btnContent, btnReports
        );

        for (Button btn : allButtons) {
            btn.getStyleClass().remove("menu-button-active");
            btn.getStyleClass().add("menu-button");
        }

        Button activeButton = getButtonForView(viewName);
        if (activeButton != null) {
            activeButton.getStyleClass().remove("menu-button");
            activeButton.getStyleClass().add("menu-button-active");
        }
    }

    private Button getButtonForView(String viewName) {
        switch (viewName) {
            case "dashboard":
            case "secondary":
                return btnHome;
            case NavigationService.VIEW_ACTIVITIES:
                return btnActivities;
            case NavigationService.VIEW_PARTICIPANTS:
                return btnParticipants;
            case NavigationService.VIEW_SPONSORS:
                return btnSponsors;
            case NavigationService.VIEW_DONATIONS:
                return btnDonations;
            case NavigationService.VIEW_OPERATIONS:
                return btnOperations;
            case NavigationService.VIEW_CONTENT:
                return btnContent;
            case NavigationService.VIEW_REPORTS:
                return btnReports;
            default:
                return null;
        }
    }

    @FXML
    private void handleHome() {
        loadViewSafely(NavigationService.VIEW_DASHBOARD);
    }

    @FXML
    private void handleActivities() {
        loadViewSafely(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleParticipants() {
        loadViewSafely(NavigationService.VIEW_PARTICIPANTS);
    }

    @FXML
    private void handleSponsors() {
        loadViewSafely(NavigationService.VIEW_SPONSORS);
    }

    @FXML
    private void handleDonations() {
        loadViewSafely(NavigationService.VIEW_DONATIONS);
    }

    @FXML
    private void handleOperations() {
        loadViewSafely(NavigationService.VIEW_OPERATIONS);
    }

    @FXML
    private void handleContent() {
        loadViewSafely(NavigationService.VIEW_CONTENT);
    }

    @FXML
    private void handleReports() {
        loadViewSafely(NavigationService.VIEW_REPORTS);
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.clear();
        NavigationService.navigateTo(NavigationService.VIEW_LOGIN);
    }

    private void setVisibleManaged(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void loadViewSafely(String viewName) {
        try {
            DetailDialogUtils.closeActiveOverlay();
            NavigationService.loadContentInLayout(viewName);
        } catch (Exception ex) {
            ex.printStackTrace();
            showInlineLoadError(viewName, rootCauseMessage(ex));
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private void showInlineLoadError(String viewName, String message) {
        Label title = new Label("Không mở được màn hình");
        title.getStyleClass().add("section-title");
        Label detail = new Label(message);
        detail.getStyleClass().add("dashboard-main-text");
        detail.setWrapText(true);
        Button retryButton = new Button("Thử lại");
        retryButton.getStyleClass().add("primary-button");
        retryButton.setOnAction(event -> loadViewSafely(viewName));
        VBox card = new VBox(14, title, detail, retryButton);
        card.getStyleClass().add("dashboard-large-card");
        card.setMaxWidth(720);
        contentArea.getChildren().setAll(card);
        updateActiveButton(viewName);
    }
}
