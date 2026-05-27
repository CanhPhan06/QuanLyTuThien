package com.mycompany.charitymanagement;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;

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

        String[][] rows = new String[Math.min(notifications.size(), 15)][2];
        for (int i = 0; i < rows.length; i++) {
            SystemRecord record = notifications.get(i);
            String campaign = record.getMaChienDich() == null || record.getMaChienDich().isBlank()
                    ? ""
                    : " • " + record.getTenChienDich();
            rows[i][0] = record.getNgay() + " • " + record.getTieuDe();
            rows[i][1] = record.getNoiDung() + campaign + " • " + record.getTrangThai();
        }
        DetailDialogUtils.showDetails(owner, "Thông báo", rows);
        markRead(notifications);
        updateBell(owner instanceof Button ? (Button) owner : null, user);
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
