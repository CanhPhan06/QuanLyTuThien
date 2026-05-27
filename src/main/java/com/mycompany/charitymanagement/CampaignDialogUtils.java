package com.mycompany.charitymanagement;

import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

final class CampaignDialogUtils {

    private static final String REPLY_MARKER = "REPLY_TO=";

    private CampaignDialogUtils() {
    }

    static void showCampaignDialog(Node ownerNode, ActivityModel campaign, UserAccount user,
            String joinButtonText, Consumer<ActivityModel> joinHandler,
            String commentTitle, String noteSource, Runnable afterChange) {
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần xem.");
            return;
        }

        Scene scene = ownerNode == null ? App.getScene() : ownerNode.getScene();
        if (scene == null) {
            scene = App.getScene();
        }
        if (scene == null) {
            return;
        }

        VBox commentsBox = new VBox(8);
        commentsBox.getStyleClass().add("campaign-dialog-comments");
        renderComments(campaign, commentsBox);

        TextField commentInput = new TextField();
        commentInput.setPromptText("Nhập bình luận cho chiến dịch...");
        commentInput.getStyleClass().addAll("input-field", "campaign-dialog-comment-input");
        Button sendButton = new Button("Gửi bình luận");
        sendButton.getStyleClass().add("primary-button");
        sendButton.setOnAction(event -> sendComment(campaign, user, commentInput, commentTitle, noteSource, commentsBox, afterChange));
        commentInput.setOnAction(event -> sendComment(campaign, user, commentInput, commentTitle, noteSource, commentsBox, afterChange));

        HBox commentComposer = new HBox(10, commentInput, sendButton);
        commentComposer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(commentInput, Priority.ALWAYS);

        ScrollPane commentScroll = new ScrollPane(commentsBox);
        commentScroll.setFitToWidth(true);
        commentScroll.setPannable(true);
        commentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        commentScroll.setPrefHeight(250);
        commentScroll.getStyleClass().addAll("content-scroll", "comment-scroll");

        VBox content = new VBox(16,
                hero(campaign),
                detailGrid(campaign),
                sectionHeader("Bình luận chiến dịch"),
                commentScroll,
                commentComposer
        );
        content.getStyleClass().add("campaign-dialog-content");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMaxHeight(620);
        scrollPane.getStyleClass().add("content-scroll");

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("quick-button");
        Button joinButton = new Button(joinButtonText);
        joinButton.getStyleClass().add("primary-button");
        joinButton.setOnAction(event -> {
            if (joinHandler != null) {
                joinHandler.accept(campaign);
            }
        });

        HBox actions = new HBox(12, closeButton, joinButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, scrollPane, actions);
        card.getStyleClass().addAll("detail-card", "campaign-dialog-card");
        card.setMaxWidth(940);
        card.setMaxHeight(700);

        StackPane overlay = DetailDialogUtils.showCard(scene, card);
        if (overlay != null) {
            closeButton.setOnAction(event -> DetailDialogUtils.closeOverlay(overlay));
        }
    }

    private static HBox hero(ActivityModel campaign) {
        HBox hero = new HBox(18);
        hero.getStyleClass().add("campaign-dialog-hero");

        StackPane visual = new StackPane();
        visual.getStyleClass().add("featured-visual");
        VBox visualText = new VBox();
        visualText.setAlignment(Pos.TOP_LEFT);
        visualText.setSpacing(90);
        Label badge = new Label("NỔI BẬT");
        badge.getStyleClass().add("featured-badge");
        Label title = new Label("Tình nguyện kết nối");
        title.getStyleClass().add("featured-image-title");
        visualText.getChildren().addAll(badge, title);
        visual.getChildren().add(visualText);

        VBox body = new VBox(10);
        HBox.setHgrow(body, Priority.ALWAYS);
        Label category = new Label("CHIẾN DỊCH");
        category.getStyleClass().add("content-category");
        Label name = new Label(campaign.getTenChienDich());
        name.getStyleClass().add("featured-title");
        name.setWrapText(true);
        Label summary = new Label(campaign.getMoTa());
        summary.getStyleClass().add("featured-summary");
        summary.setWrapText(true);

        HBox meta = new HBox(18,
                muted(campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc()),
                muted(campaign.getDiaDiem()),
                muted(AppData.getCampaignParticipantCount(campaign.getMaChienDich()) + " TNV")
        );

        body.getChildren().addAll(category, name, summary, meta);
        hero.getChildren().addAll(visual, body);
        return hero;
    }

    private static GridPane detailGrid(ActivityModel campaign) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.getStyleClass().add("form-card");
        addDetailRow(grid, 0, "Mã chiến dịch", campaign.getMaChienDich());
        addDetailRow(grid, 1, "Địa điểm", campaign.getDiaDiem());
        addDetailRow(grid, 2, "Thời gian", campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc());
        addDetailRow(grid, 3, "Mục tiêu", FormatUtils.money(campaign.getMucTieuTien()));
        addDetailRow(grid, 4, "Đã ghi nhận", FormatUtils.money(AppData.getCampaignMoneyTotal(campaign.getMaChienDich())));
        addDetailRow(grid, 5, "Tình nguyện viên", AppData.getCampaignParticipantCount(campaign.getMaChienDich()) + " người");
        addDetailRow(grid, 6, "Trạng thái", campaign.getTrangThai());
        return grid;
    }

    private static void addDetailRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("muted-text");
        Label valueLabel = new Label(value == null || value.isBlank() ? "-" : value);
        valueLabel.getStyleClass().add("dashboard-main-text");
        valueLabel.setWrapText(true);
        grid.add(keyLabel, 0, row);
        grid.add(valueLabel, 1, row);
        GridPane.setHgrow(valueLabel, Priority.ALWAYS);
    }

    private static HBox sectionHeader(String text) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(text);
        label.getStyleClass().add("portal-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(label, spacer);
        return row;
    }

    private static void renderComments(ActivityModel campaign, VBox target) {
        target.getChildren().clear();
        int count = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan")
                    || isReplyRecord(record)
                    || !campaign.getMaChienDich().equalsIgnoreCase(campaignId(record))) {
                continue;
            }
            target.getChildren().add(commentCard(record));
            count++;
        }
        if (count == 0) {
            Label empty = new Label("Chưa có bình luận cho chiến dịch này.");
            empty.getStyleClass().add("muted-text");
            target.getChildren().add(empty);
        }
    }

    private static VBox commentCard(SystemRecord record) {
        VBox wrapper = new VBox(6);
        wrapper.getStyleClass().add("comment-card");

        HBox row = new HBox(10);
        row.getStyleClass().add("comment-row");
        Label avatar = new Label(initials(record.getTenLienKet()));
        avatar.getStyleClass().add("comment-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, Priority.ALWAYS);
        Label author = new Label(record.getTenLienKet() + "  ·  " + record.getNgay());
        author.getStyleClass().add("comment-author");
        Label content = new Label(record.getNoiDung());
        content.setWrapText(true);
        content.getStyleClass().add("comment-text");
        Label status = new Label(record.getTrangThai());
        status.getStyleClass().add("muted-text");

        VBox replies = new VBox(6);
        replies.getStyleClass().add("reply-list");
        renderReplies(record, replies);

        body.getChildren().addAll(author, content, status, replies);
        row.getChildren().addAll(avatar, body);
        wrapper.getChildren().add(row);
        return wrapper;
    }

    private static void renderReplies(SystemRecord parent, VBox replies) {
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan") || !isReplyTo(record, parent)) {
                continue;
            }
            replies.getChildren().add(replyRow(record));
        }
    }

    private static HBox replyRow(SystemRecord reply) {
        HBox row = new HBox(8);
        row.getStyleClass().add("reply-row");
        Label avatar = new Label(initials(reply.getTenLienKet()));
        avatar.getStyleClass().add("reply-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, Priority.ALWAYS);
        Label author = new Label(reply.getTenLienKet() + "  ·  " + reply.getNgay());
        author.getStyleClass().add("comment-author");
        Label content = new Label(reply.getNoiDung());
        content.setWrapText(true);
        content.getStyleClass().add("comment-text");
        body.getChildren().addAll(author, content);

        row.getChildren().addAll(avatar, body);
        return row;
    }

    private static void sendComment(ActivityModel campaign, UserAccount user, TextField input,
            String title, String noteSource, VBox commentsBox, Runnable afterChange) {
        String text = input == null || input.getText() == null ? "" : input.getText().trim();
        if (text.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập nội dung bình luận.");
            return;
        }
        UserAccount actor = user == null
                ? new UserAccount("GUEST", "", UserAccount.ROLE_VOLUNTEER, "Người dùng", "")
                : user;
        AppData.getContents().add(new SystemRecord(
                "BinhLuan",
                AppData.nextContentId("BL"),
                campaign.getMaChienDich(),
                actor.getUsername(),
                title,
                text,
                AppData.todayText(),
                "",
                "Chờ duyệt",
                actor.getUsername(),
                "ADMIN",
                noteSource
        ));
        BusinessService.notifyAdmins("Bình luận chiến dịch mới",
                actor.getDisplayName() + " bình luận trong " + campaign.getTenChienDich() + ": " + snippet(text));
        input.clear();
        renderComments(campaign, commentsBox);
        if (afterChange != null) {
            afterChange.run();
        }
        DialogUtils.info("Đã gửi bình luận. Admin sẽ thấy trong phần Nội dung.");
    }

    private static String snippet(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 80 ? text : text.substring(0, 77) + "...";
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private static boolean isReplyRecord(SystemRecord record) {
        return replyParentId(record).length() > 0;
    }

    private static boolean isReplyTo(SystemRecord reply, SystemRecord parent) {
        return parent.getMaChinh().equalsIgnoreCase(replyParentId(reply));
    }

    private static String replyParentId(SystemRecord record) {
        String note = record.getGhiChu() == null ? "" : record.getGhiChu();
        int start = note.indexOf(REPLY_MARKER);
        if (start < 0) {
            return "";
        }
        int idStart = start + REPLY_MARKER.length();
        int idEnd = idStart;
        while (idEnd < note.length() && Character.isLetterOrDigit(note.charAt(idEnd))) {
            idEnd++;
        }
        return note.substring(idStart, idEnd);
    }

    private static String campaignId(SystemRecord record) {
        if (record.getMaChienDich() != null && !record.getMaChienDich().isBlank()) {
            return record.getMaChienDich();
        }
        if (record.getMaLienKet() != null && record.getMaLienKet().toUpperCase().startsWith("CD")) {
            return record.getMaLienKet();
        }
        return "";
    }

    private static String groupCode(SystemRecord record) {
        return record.getNhomBang() == null ? "" : normalized(record.getNhomBang());
    }

    private static String initials(String value) {
        if (value == null || value.isBlank()) {
            return "?";
        }
        String[] parts = value.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length == 1 ? "" : parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    private static String normalized(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }
}
