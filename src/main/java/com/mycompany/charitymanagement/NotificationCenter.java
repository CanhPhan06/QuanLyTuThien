package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

final class NotificationCenter {

    private NotificationCenter() {
    }

    static List<SystemRecord> notificationsFor(UserAccount user) {
        List<SystemRecord> result = new ArrayList<>();
        if (user == null) {
            return result;
        }
        for (SystemRecord record : AppData.getContents()) {
            if (isNotification(record) && isForUser(record, user)) {
                result.add(record);
            }
        }
        Collections.reverse(result);
        return result;
    }

    static long unreadCount(UserAccount user) {
        return notificationsFor(user).stream()
                .filter(NotificationCenter::isUnread)
                .count();
    }

    static void updateBell(Button button, UserAccount user) {
        if (button == null) {
            return;
        }
        long count = unreadCount(user);
        button.setText(count > 0 ? "🔔 " + count : "🔔");
    }

    static void show(Node owner, UserAccount user) {
        List<SystemRecord> notifications = notificationsFor(user);
        if (notifications.isEmpty()) {
            DialogUtils.info("Chưa có thông báo nào.");
            return;
        }

        Scene scene = owner == null ? App.getScene() : owner.getScene();
        if (scene == null) {
            scene = App.getScene();
        }
        if (scene == null) {
            return;
        }

        Label title = new Label("Thông báo");
        title.getStyleClass().add("page-title");

        VBox list = new VBox(10);
        int count = Math.min(notifications.size(), 15);
        for (int i = 0; i < count; i++) {
            list.getChildren().add(notificationRow(owner, user, notifications.get(i)));
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(440);
        scrollPane.getStyleClass().add("content-scroll");

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("quick-button");
        HBox actions = new HBox(closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, title, scrollPane, actions);
        card.getStyleClass().add("detail-card");
        card.setMaxWidth(760);
        card.setMaxHeight(620);

        StackPane overlay = DetailDialogUtils.showCard(scene, card);
        if (overlay != null) {
            closeButton.setOnAction(event -> DetailDialogUtils.closeOverlay(overlay));
        }
    }

    private static HBox notificationRow(Node owner, UserAccount user, SystemRecord record) {
        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label title = new Label(record.getNgay() + " • " + record.getTieuDe());
        title.getStyleClass().add("comment-author");
        Label content = new Label(notificationSummary(record));
        content.getStyleClass().add("comment-text");
        content.setWrapText(true);
        Label status = new Label(record.getTrangThai());
        status.getStyleClass().add("muted-text");
        body.getChildren().addAll(title, content, status);

        Button openButton = new Button(actionLabel(record, user));
        openButton.getStyleClass().add("primary-button");
        openButton.setOnAction(event -> {
            event.consume();
            openNotification(owner, user, record);
        });

        HBox row = new HBox(12, body, openButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("comment-card");
        row.setOnMouseClicked(event -> openNotification(owner, user, record));
        return row;
    }

    private static void openNotification(Node owner, UserAccount user, SystemRecord record) {
        record.setTrangThai("Đã đọc");
        updateBell(owner instanceof Button ? (Button) owner : null, user);

        if (shouldOpenContent(record)) {
            showCampaignPopup(owner, user, record);
            return;
        }
        if (user != null && user.isAdmin() && shouldOpenOperations(record)) {
            navigateToOperations(record);
            return;
        }
        if (record.getMaChienDich() != null && !record.getMaChienDich().isBlank()) {
            showCampaignPopup(owner, user, record);
            return;
        }

        DetailDialogUtils.closeActiveOverlay();
        DetailDialogUtils.showDetails(owner, record.getTieuDe(), new String[][]{
            {"Ngày", record.getNgay()},
            {"Nội dung", record.getNoiDung()},
            {"Chiến dịch", record.getMaChienDich().isBlank() ? "-" : record.getTenChienDich()},
            {"Trạng thái", record.getTrangThai()}
        });
    }

    private static void showCampaignPopup(Node owner, UserAccount user, SystemRecord record) {
        ActivityModel campaign = AppData.findCampaign(record.getMaChienDich());
        if (campaign == null) {
            DetailDialogUtils.showDetails(owner, record.getTieuDe(), new String[][]{
                {"Ngày", record.getNgay()},
                {"Nội dung", record.getNoiDung()},
                {"Trạng thái", record.getTrangThai()}
            });
            return;
        }
        DetailDialogUtils.closeActiveOverlay();
        CampaignDialogUtils.showNotificationCampaignDialog(owner, campaign, user,
                () -> updateBell(owner instanceof Button ? (Button) owner : null, user));
    }

    private static void navigateToOperations(SystemRecord record) {
        try {
            DetailDialogUtils.closeActiveOverlay();
            NavigationIntent.focusOperations(operationType(record), record.getMaChienDich(), operationStatus(record), "");
            NavigationService.invalidateContent(NavigationService.VIEW_OPERATIONS);
            NavigationService.loadContentInLayout(NavigationService.VIEW_OPERATIONS);
        } catch (IOException ex) {
            DialogUtils.warning("Không mở được màn hình Vận hành.");
        }
    }

    private static void markRead(List<SystemRecord> notifications) {
        for (SystemRecord record : notifications) {
            if (isUnread(record)) {
                record.setTrangThai("Đã đọc");
            }
        }
    }

    private static boolean isNotification(SystemRecord record) {
        return normalize(record.getNhomBang()).contains("thongbao");
    }

    private static boolean shouldOpenContent(SystemRecord record) {
        return "CONTENT_COMMENTS".equals(marker(record, "ACTION"))
                || normalize(record.getTieuDe() + " " + record.getNoiDung()).contains("binh luan");
    }

    private static boolean shouldOpenOperations(SystemRecord record) {
        String action = marker(record, "ACTION");
        if ("OPERATIONS".equals(action)) {
            return true;
        }
        String text = normalize(record.getTieuDe() + " " + record.getNoiDung());
        return text.contains("dang ky")
                || text.contains("diem danh")
                || text.contains("minh chung")
                || text.contains("quyen gop")
                || text.contains("cho duyet")
                || text.contains("cho xac nhan")
                || text.contains("can duyet");
    }

    private static String operationType(SystemRecord record) {
        String type = marker(record, "TYPE");
        if (!type.isBlank()) {
            return type;
        }
        String text = normalize(record.getTieuDe() + " " + record.getNoiDung());
        if (text.contains("diem danh")) {
            return "Điểm danh";
        }
        if (text.contains("minh chung")) {
            return "Minh chứng TNV";
        }
        if (text.contains("quyen gop") || text.contains("tai tro")) {
            return "Quyên góp";
        }
        return "Đăng ký TNV";
    }

    private static String operationStatus(SystemRecord record) {
        String status = marker(record, "STATUS");
        if (!status.isBlank()) {
            return status;
        }
        String text = normalize(record.getTieuDe() + " " + record.getNoiDung());
        return text.contains("xac nhan") ? "Chờ xác nhận" : "Chờ duyệt";
    }

    private static String actionLabel(SystemRecord record, UserAccount user) {
        if (user != null && user.isAdmin() && shouldOpenContent(record)) {
            return "Mở nội dung";
        }
        if (user != null && user.isAdmin() && shouldOpenOperations(record)) {
            return "Mở vận hành";
        }
        return "Chi tiết";
    }

    private static String notificationSummary(SystemRecord record) {
        String campaign = record.getMaChienDich() == null || record.getMaChienDich().isBlank()
                ? ""
                : " • " + record.getTenChienDich();
        return record.getNoiDung() + campaign;
    }

    private static String marker(SystemRecord record, String key) {
        String note = record.getGhiChu() == null ? "" : record.getGhiChu();
        String marker = key + "=";
        int start = note.indexOf(marker);
        if (start < 0) {
            return "";
        }
        int valueStart = start + marker.length();
        int valueEnd = note.indexOf(';', valueStart);
        if (valueEnd < 0) {
            valueEnd = note.length();
        }
        return note.substring(valueStart, valueEnd).trim();
    }

    private static boolean isForUser(SystemRecord record, UserAccount user) {
        String target = safe(record.getMaLienKet());
        if (target.equalsIgnoreCase(user.getUsername())) {
            return true;
        }
        if (!user.isAdmin()) {
            return false;
        }
        return target.equalsIgnoreCase("ADMIN")
                || safe(record.getNguoiXuLy()).equalsIgnoreCase(user.getUsername());
    }

    private static boolean isUnread(SystemRecord record) {
        return normalize(record.getTrangThai()).contains("chua doc");
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
