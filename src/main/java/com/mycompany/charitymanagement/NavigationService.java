package com.mycompany.charitymanagement;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public final class NavigationService {

    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_ACTIVITIES = "activities";
    public static final String VIEW_PARTICIPANTS = "participants";
    public static final String VIEW_SPONSORS = "sponsors";
    public static final String VIEW_DONATIONS = "donations";
    public static final String VIEW_OPERATIONS = "operations";
    public static final String VIEW_CONTENT = "content";
    public static final String VIEW_REPORTS = "reports";
    public static final String VIEW_VOLUNTEER = "volunteer";
    public static final String VIEW_SPONSORPORTAL = "sponsorportal";
    public static final String VIEW_LOGIN = "primary";

    private static MainLayoutController mainLayoutController;
    private static String currentContentView;
    private static final Map<String, Node> contentCache = new HashMap<>();

    private NavigationService() {
    }

    public static void setMainLayoutController(MainLayoutController controller) {
        mainLayoutController = controller;
        contentCache.clear();
        currentContentView = null;
    }

    public static MainLayoutController getMainLayoutController() {
        return mainLayoutController;
    }

    public static String getCurrentContentView() {
        return currentContentView;
    }

    public static void clearCache() {
        contentCache.clear();
    }

    public static void navigateTo(String viewName) throws IOException {
        if (VIEW_LOGIN.equals(viewName)) {
            UserSession.clear();
            clearCache();
            mainLayoutController = null;
            currentContentView = null;
            App.setRoot(viewName);
            return;
        }

        if (VIEW_VOLUNTEER.equals(viewName) || VIEW_SPONSORPORTAL.equals(viewName)) {
            clearCache();
            mainLayoutController = null;
            currentContentView = null;
            App.setRoot(viewName);
            return;
        }

        if (mainLayoutController == null) {
            App.setRootToMainLayout(viewName);
            return;
        }

        loadContentInLayout(viewName);
    }

    public static void loadContentInLayout(String viewName) throws IOException {
        if (mainLayoutController == null) {
            navigateTo(viewName);
            return;
        }

        Node content = contentCache.get(viewName);
        if (content == null) {
            content = loadContentNode(viewName);
            contentCache.put(viewName, content);
        }

        mainLayoutController.setContent(content);
        mainLayoutController.updateActiveButton(viewName);
        currentContentView = viewName;
    }

    private static Node loadContentNode(String viewName) throws IOException {
        String path = "/fxml/" + viewName + ".fxml";
        URL resource = NavigationService.class.getResource(path);
        if (resource == null) {
            throw new IOException("Không tìm thấy giao diện: " + path);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        return loader.load();
    }

    public static void refreshCurrentContent() {
        if (currentContentView != null) {
            contentCache.remove(currentContentView);
        }
    }
}
