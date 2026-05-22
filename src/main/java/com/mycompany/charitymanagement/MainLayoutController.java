package com.mycompany.charitymanagement;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

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
    private void handleHome() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_DASHBOARD);
    }

    @FXML
    private void handleActivities() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleParticipants() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_PARTICIPANTS);
    }

    @FXML
    private void handleSponsors() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_SPONSORS);
    }

    @FXML
    private void handleDonations() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_DONATIONS);
    }

    @FXML
    private void handleOperations() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_OPERATIONS);
    }

    @FXML
    private void handleContent() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_CONTENT);
    }

    @FXML
    private void handleReports() throws IOException {
        NavigationService.loadContentInLayout(NavigationService.VIEW_REPORTS);
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
}
