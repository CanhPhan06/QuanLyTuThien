package com.mycompany.charitymanagement;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SidebarController {

    @FXML
    private Button btnHome;
    @FXML
    private Button btnActivities;
    @FXML
    private Button btnScreening;
    @FXML
    private Button btnParticipants;
    @FXML
    private Button btnTraining;
    @FXML
    private Button btnSponsors;
    @FXML
    private Button btnDonations;
    @FXML
    private Button btnOperations;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnExpense;
    @FXML
    private Button btnContent;
    @FXML
    private Button btnReports;
    @FXML
    private Button btnAlerts;
    @FXML
    private Button btnLogout;

    private final Map<String, Button> navMap = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        navMap.put("secondary", btnHome);
        navMap.put("activities", btnActivities);
        navMap.put("screening", btnScreening);
        navMap.put("participants", btnParticipants);
        navMap.put("training", btnTraining);
        navMap.put("sponsors", btnSponsors);
        navMap.put("donations", btnDonations);
        navMap.put("operations", btnOperations);
        navMap.put("inventory", btnInventory);
        navMap.put("expense", btnExpense);
        navMap.put("content", btnContent);
        navMap.put("reports", btnReports);
        navMap.put("alert", btnAlerts);
    }

    void setActive(String screen) {
        for (Map.Entry<String, Button> entry : navMap.entrySet()) {
            entry.getValue().getStyleClass().remove("menu-button-active");
        }
        Button active = navMap.get(screen);
        if (active != null) {
            active.getStyleClass().add("menu-button-active");
        }
    }

    @FXML
    private void handleHome() throws IOException { App.setRoot("secondary"); }
    @FXML
    private void handleActivities() throws IOException { App.setRoot("activities"); }
    @FXML
    private void handleScreening() throws IOException { App.setRoot("screening"); }
    @FXML
    private void handleParticipants() throws IOException { App.setRoot("participants"); }
    @FXML
    private void handleTraining() throws IOException { App.setRoot("training"); }
    @FXML
    private void handleSponsors() throws IOException { App.setRoot("sponsors"); }
    @FXML
    private void handleDonations() throws IOException { App.setRoot("donations"); }
    @FXML
    private void handleOperations() throws IOException { App.setRoot("operations"); }
    @FXML
    private void handleInventory() throws IOException { App.setRoot("inventory"); }
    @FXML
    private void handleExpense() throws IOException { App.setRoot("expense"); }
    @FXML
    private void handleContent() throws IOException { App.setRoot("content"); }
    @FXML
    private void handleReports() throws IOException { App.setRoot("reports"); }
    @FXML
    private void handleAlerts() throws IOException { App.setRoot("alert"); }
    @FXML
    private void handleLogout() throws IOException { UserSession.clear(); App.setRoot("primary"); }
}
