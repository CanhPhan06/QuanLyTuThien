package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VolunteerController {

    private static final String REPLY_MARKER = "REPLY_TO=";

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblCampaign;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblScore;
    @FXML
    private Label lblNotificationCount;
    @FXML
    private Label lblSelectedCampaign;
    @FXML
    private Label lblSelectedTime;
    @FXML
    private Label lblSelectedPlace;
    @FXML
    private Label lblSelectedCommentThread;
    @FXML
    private ComboBox<String> cboCampaign;
    @FXML
    private ComboBox<String> cboProofType;
    @FXML
    private TextField txtProofNote;
    @FXML
    private TextField txtCampaignComment;
    @FXML
    private VBox campaignCommentList;
    @FXML
    private VBox campaignPortalBox;
    @FXML
    private VBox overviewSection;
    @FXML
    private VBox campaignsSection;
    @FXML
    private VBox tasksSection;
    @FXML
    private VBox proofSection;
    @FXML
    private VBox notificationsSection;

    @FXML
    private TableView<ActivityModel> tableCampaigns;
    @FXML
    private TableColumn<ActivityModel, String> colMaChienDich;
    @FXML
    private TableColumn<ActivityModel, String> colTenChienDich;
    @FXML
    private TableColumn<ActivityModel, String> colNgayBatDau;
    @FXML
    private TableColumn<ActivityModel, String> colNgayKetThuc;
    @FXML
    private TableColumn<ActivityModel, String> colTrangThai;

    @FXML
    private TableView<SystemRecord> tableTasks;
    @FXML
    private TableColumn<SystemRecord, String> colTaskType;
    @FXML
    private TableColumn<SystemRecord, String> colTaskTitle;
    @FXML
    private TableColumn<SystemRecord, String> colTaskDate;
    @FXML
    private TableColumn<SystemRecord, String> colTaskStatus;

    @FXML
    private TableView<SystemRecord> tableNotifications;
    @FXML
    private TableColumn<SystemRecord, String> colNoticeTitle;
    @FXML
    private TableColumn<SystemRecord, String> colNoticeDate;
    @FXML
    private TableColumn<SystemRecord, String> colNoticeStatus;

    private final ObservableList<SystemRecord> volunteerTasks = FXCollections.observableArrayList();
    private final ObservableList<SystemRecord> volunteerNotifications = FXCollections.observableArrayList();
    private UserAccount currentUser;
    private String selectedCampaignId;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        colMaChienDich.setVisible(false);
        colMaChienDich.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaChienDich()));
        colTenChienDich.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colNgayBatDau.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayBatDau()));
        colNgayKetThuc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayKetThuc()));
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));

        colTaskType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNhomBang()));
        colTaskTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTieuDe()));
        colTaskDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayTao()));
        colTaskStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));

        colNoticeTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTieuDe()));
        colNoticeDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayTao()));
        colNoticeStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));

        tableCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTasks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableNotifications.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCampaigns.setFixedCellSize(32.0);
        tableTasks.setFixedCellSize(32.0);
        tableNotifications.setFixedCellSize(32.0);
        tableCampaigns.setItems(AppData.getActivities());
        tableTasks.setItems(volunteerTasks);
        tableNotifications.setItems(volunteerNotifications);

        cboCampaign.setItems(buildCampaignChoices());
        cboProofType.setItems(FXCollections.observableArrayList(
                "Ảnh tham gia",
                "Ảnh phát quà",
                "Ảnh điểm danh",
                "Biên nhận vật phẩm",
                "Ghi chú sau hoạt động"
        ));
        cboProofType.setValue("Ảnh tham gia");

        tableCampaigns.setRowFactory(table -> {
            TableRow<ActivityModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    ActivityModel selected = row.getItem();
                    cboCampaign.setValue(selected.getMaChienDich() + " - " + selected.getTenChienDich());
                    updateSelectedCampaign(selected);
                    if (event.getClickCount() == 2) {
                        showCampaignDetail(selected);
                    }
                }
            });
            return row;
        });
        cboCampaign.valueProperty().addListener((observable, oldValue, value) ->
                updateSelectedCampaign(AppData.findCampaign(extractCampaignId(value))));
        AppData.getContents().addListener((ListChangeListener<SystemRecord>) change -> {
            refreshView();
            renderCampaignComments();
            renderCampaignPortal();
        });

        if (!cboCampaign.getItems().isEmpty()) {
            cboCampaign.setValue(cboCampaign.getItems().get(0));
            updateSelectedCampaign(AppData.findCampaign(extractCampaignId(cboCampaign.getValue())));
        }
        refreshView();
        renderCampaignPortal();
        showSection(overviewSection);
    }

    @FXML
    private void handleShowOverview() {
        showSection(overviewSection);
    }

    @FXML
    private void handleShowCampaigns() {
        renderCampaignPortal();
        showSection(campaignsSection);
    }

    @FXML
    private void handleShowTasks() {
        showSection(tasksSection);
    }

    @FXML
    private void handleShowProof() {
        showSection(proofSection);
    }

    @FXML
    private void handleShowNotifications() {
        showSection(notificationsSection);
    }

    @FXML
    private void handleRegisterCampaign() {
        ActivityModel selected = selectedCampaign();
        String error = BusinessRules.validateVolunteerRegistration(currentUser, selected);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }

        error = BusinessService.registerVolunteer(currentUser, selected);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
        refreshView();
        renderCampaignPortal();
        DialogUtils.info("Đã gửi đăng ký tham gia chiến dịch.");
    }

    @FXML
    private void handleSendCampaignComment() {
        ActivityModel campaign = selectedCampaign();
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch trước khi bình luận.");
            return;
        }
        String text = value(txtCampaignComment);
        if (text.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập nội dung bình luận.");
            return;
        }

        AppData.getContents().add(new SystemRecord(
                "BinhLuan",
                AppData.nextContentId("BL"),
                campaign.getMaChienDich(),
                currentUser.getUsername(),
                "Bình luận của tình nguyện viên",
                text,
                AppData.todayText(),
                "",
                "Chờ duyệt",
                currentUser.getUsername(),
                "ADMIN",
                "Tạo từ cổng tình nguyện viên"
        ));
        txtCampaignComment.clear();
        renderCampaignComments();
        DialogUtils.info("Đã gửi bình luận. Admin sẽ thấy trong phần Nội dung.");
    }

    @FXML
    private void handleCheckIn() {
        ParticipantModel profile = findParticipant();
        String error = BusinessRules.validateCheckIn(currentUser, profile);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }

        error = BusinessService.checkIn(currentUser, profile);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
        refreshView();
        DialogUtils.info("Đã ghi nhận điểm danh, chờ quản lý xác nhận.");
    }

    @FXML
    private void handleSubmitProof() {
        ParticipantModel profile = findParticipant();
        String proofType = cboProofType.getValue() == null ? "" : cboProofType.getValue();
        String note = txtProofNote.getText() == null ? "" : txtProofNote.getText().trim();
        String error = BusinessRules.validateProof(currentUser, profile, proofType, note);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }

        error = BusinessService.submitProof(currentUser, profile, proofType, note);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
        txtProofNote.clear();
        refreshView();
        DialogUtils.info("Đã gửi minh chứng, chờ quản lý xác nhận.");
    }

    @FXML
    private void handleViewCampaignDetail() {
        ActivityModel campaign = AppData.findCampaign(extractCampaignId(cboCampaign.getValue()));
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần xem.");
            return;
        }
        showCampaignDetail(campaign);
    }

    @FXML
    private void handleRefresh() {
        refreshView();
        renderCampaignPortal();
    }

    @FXML
    private void handleLogout() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_LOGIN);
    }

    private void refreshView() {
        lblWelcome.setText("Xin chào, " + currentUser.getDisplayName());
        ParticipantModel profile = findParticipant();
        String campaignId = profile == null ? "" : profile.getMaChienDich();
        ActivityModel campaign = AppData.findCampaign(campaignId);

        lblCampaign.setText(campaign == null ? "Chưa tham gia" : campaign.getTenChienDich());
        lblStatus.setText(profile == null ? "Chưa đăng ký" : profile.getTrangThaiDuyet());
        lblScore.setText(profile == null || profile.getDiemDanhGia().isEmpty() ? "Chưa có" : profile.getDiemDanhGia());

        volunteerTasks.setAll(AppData.getOperations().filtered(record ->
                record.getMaLienKet().equalsIgnoreCase(currentUser.getUsername())
                || (!campaignId.isEmpty() && record.getMaChienDich().equalsIgnoreCase(campaignId))
        ));
        volunteerNotifications.setAll(AppData.getContents().filtered(record ->
                record.getMaLienKet().equalsIgnoreCase(currentUser.getUsername())
                || (!campaignId.isEmpty() && record.getMaLienKet().equalsIgnoreCase(campaignId))
        ));
        if (lblNotificationCount != null) {
            lblNotificationCount.setText(String.valueOf(volunteerNotifications.size()));
        }
    }

    private void showSection(VBox activeSection) {
        VBox[] sections = {overviewSection, campaignsSection, tasksSection, proofSection, notificationsSection};
        for (VBox section : sections) {
            if (section != null) {
                boolean active = section == activeSection;
                section.setVisible(active);
                section.setManaged(active);
            }
        }
    }

    private ObservableList<String> buildCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList();
        for (ActivityModel activity : AppData.getActivities()) {
            choices.add(activity.getMaChienDich() + " - " + activity.getTenChienDich());
        }
        return choices;
    }

    private String extractCampaignId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.split(" - ", 2)[0].trim();
    }

    private void updateSelectedCampaign(ActivityModel campaign) {
        if (campaign == null) {
            lblSelectedCampaign.setText("Chưa chọn chiến dịch");
            lblSelectedTime.setText("-");
            lblSelectedPlace.setText("-");
            setLabelText(lblSelectedCommentThread, "Chọn chiến dịch để xem bình luận");
            renderCampaignComments();
            return;
        }
        lblSelectedCampaign.setText(campaign.getTenChienDich());
        lblSelectedTime.setText(campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc());
        lblSelectedPlace.setText(campaign.getDiaDiem());
        setLabelText(lblSelectedCommentThread, campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
        renderCampaignComments();
    }

    private void showCampaignDetail(ActivityModel campaign) {
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần xem.");
            return;
        }
        cboCampaign.setValue(campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
        updateSelectedCampaign(campaign);
        CampaignDialogUtils.showCampaignDialog(campaignsSection, campaign, currentUser,
                "Tham gia chiến dịch",
                selected -> {
                    cboCampaign.setValue(selected.getMaChienDich() + " - " + selected.getTenChienDich());
                    handleRegisterCampaign();
                },
                "Bình luận của tình nguyện viên",
                "Tạo từ cổng tình nguyện viên",
                () -> {
                    refreshView();
                    renderCampaignComments();
                    renderCampaignPortal();
                });
    }

    private ParticipantModel findParticipant() {
        return AppData.getParticipants().stream()
                .filter(item -> item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername()))
                .findFirst()
                .orElse(null);
    }

    private ActivityModel selectedCampaign() {
        return AppData.findCampaign(extractCampaignId(cboCampaign.getValue()));
    }

    private void renderCampaignPortal() {
        if (campaignPortalBox == null) {
            return;
        }
        campaignPortalBox.getChildren().clear();
        if (AppData.getActivities().isEmpty()) {
            Label empty = new Label("Chưa có chiến dịch nào.");
            empty.getStyleClass().add("muted-text");
            campaignPortalBox.getChildren().add(empty);
            return;
        }

        ActivityModel featured = selectedCampaign();
        if (featured == null) {
            featured = AppData.getActivities().get(0);
        }
        campaignPortalBox.getChildren().add(featuredCampaignCard(featured));

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label title = new Label("Hoạt động sắp diễn ra");
        title.getStyleClass().add("portal-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button viewAll = new Button("Xem tất cả");
        viewAll.getStyleClass().add("link-button");
        header.getChildren().addAll(title, spacer, viewAll);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        int index = 0;
        for (ActivityModel activity : AppData.getActivities()) {
            grid.add(campaignCard(activity), index % 2, index / 2);
            index++;
        }

        VBox section = new VBox(12, header, grid);
        section.getStyleClass().add("portal-section");
        campaignPortalBox.getChildren().add(section);
    }

    private HBox featuredCampaignCard(ActivityModel campaign) {
        HBox card = new HBox(18);
        card.getStyleClass().add("featured-news-card");
        card.setOnMouseClicked(event -> showCampaignDetail(campaign));

        StackPane visual = campaignVisual("NỔI BẬT", "Tình nguyện kết nối", "featured-visual");
        visual.setOnMouseClicked(event -> {
            event.consume();
            showCampaignDetail(campaign);
        });

        VBox body = new VBox(10);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label category = new Label("CHIẾN DỊCH");
        category.getStyleClass().add("content-category");
        Label title = new Label(campaign.getTenChienDich());
        title.getStyleClass().add("featured-title");
        title.setWrapText(true);
        Label summary = new Label(campaign.getMoTa());
        summary.getStyleClass().add("featured-summary");
        summary.setWrapText(true);
        HBox meta = new HBox(18,
                muted(campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc()),
                muted(campaign.getDiaDiem()),
                muted(AppData.getCampaignParticipantCount(campaign.getMaChienDich()) + " TNV")
        );
        HBox actions = new HBox(14);
        Button joinButton = new Button("Tham gia");
        joinButton.getStyleClass().add("primary-button");
        joinButton.setOnAction(event -> {
            event.consume();
            cboCampaign.setValue(campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
            handleRegisterCampaign();
        });
        Button commentButton = new Button("Bình luận");
        commentButton.getStyleClass().add("quick-button");
        commentButton.setOnAction(event -> {
            event.consume();
            showCampaignDetail(campaign);
        });
        actions.getChildren().addAll(joinButton, commentButton,
                muted(campaignCommentCount(campaign.getMaChienDich()) + " bình luận"));
        body.getChildren().addAll(category, title, summary, meta, actions);
        card.getChildren().addAll(visual, body);
        return card;
    }

    private HBox campaignCard(ActivityModel campaign) {
        HBox card = new HBox(12);
        card.getStyleClass().add("upcoming-card");
        card.setOnMouseClicked(event -> showCampaignDetail(campaign));

        StackPane visual = campaignVisual("", iconForCampaign(campaign), "activity-thumb");
        visual.setOnMouseClicked(event -> {
            event.consume();
            showCampaignDetail(campaign);
        });

        VBox body = new VBox(5);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label category = new Label(categoryForCampaign(campaign));
        category.getStyleClass().add("content-category");
        Label title = new Label(campaign.getTenChienDich());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        Label summary = new Label(campaign.getMoTa());
        summary.getStyleClass().add("card-summary");
        summary.setWrapText(true);
        Label meta = new Label(campaign.getNgayBatDau() + "     "
                + AppData.getCampaignParticipantCount(campaign.getMaChienDich()) + " TNV");
        meta.getStyleClass().add("muted-text");
        body.getChildren().addAll(category, title, summary, meta);
        card.getChildren().addAll(visual, body);
        return card;
    }

    private StackPane campaignVisual(String badgeText, String text, String styleClass) {
        StackPane visual = new StackPane();
        visual.getStyleClass().add(styleClass);
        VBox box = new VBox();
        box.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        box.setSpacing("featured-visual".equals(styleClass) ? 90 : 0);
        if (badgeText != null && !badgeText.isBlank()) {
            Label badge = new Label(badgeText);
            badge.getStyleClass().add("featured-badge");
            box.getChildren().add(badge);
        }
        Label label = new Label(text);
        label.getStyleClass().add("featured-visual".equals(styleClass)
                ? "featured-image-title" : "activity-thumb-icon");
        box.getChildren().add(label);
        visual.getChildren().add(box);
        return visual;
    }

    private long campaignCommentCount(String campaignId) {
        return AppData.getContents().stream()
                .filter(record -> groupCode(record).contains("binhluan"))
                .filter(record -> !isReplyRecord(record))
                .filter(record -> campaignId.equalsIgnoreCase(campaignId(record)))
                .count();
    }

    private String categoryForCampaign(ActivityModel activity) {
        String text = normalized(activity.getTenChienDich() + " " + activity.getMoTa());
        if (text.contains("kham") || text.contains("benh") || text.contains("y te")) {
            return "SỨC KHỎE";
        }
        if (text.contains("sach") || text.contains("truong") || text.contains("hoc")) {
            return "GIÁO DỤC";
        }
        if (text.contains("moi truong") || text.contains("xanh") || text.contains("nuoc sach")) {
            return "MÔI TRƯỜNG";
        }
        return "CỘNG ĐỒNG";
    }

    private String iconForCampaign(ActivityModel activity) {
        String category = categoryForCampaign(activity);
        if ("SỨC KHỎE".equals(category)) {
            return "+";
        }
        if ("GIÁO DỤC".equals(category)) {
            return "▤";
        }
        if ("MÔI TRƯỜNG".equals(category)) {
            return "♧";
        }
        return "♥";
    }

    private Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private void renderCampaignComments() {
        if (campaignCommentList == null) {
            return;
        }
        campaignCommentList.getChildren().clear();
        ActivityModel campaign = selectedCampaign();
        if (campaign == null) {
            Label empty = new Label("Chưa có chiến dịch để hiển thị bình luận.");
            empty.getStyleClass().add("muted-text");
            campaignCommentList.getChildren().add(empty);
            return;
        }

        int count = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan")
                    || isReplyRecord(record)
                    || !campaign.getMaChienDich().equalsIgnoreCase(campaignId(record))) {
                continue;
            }
            campaignCommentList.getChildren().add(commentCard(record));
            count++;
        }
        if (count == 0) {
            Label empty = new Label("Chưa có bình luận cho chiến dịch này.");
            empty.getStyleClass().add("muted-text");
            campaignCommentList.getChildren().add(empty);
        }
    }

    private VBox commentCard(SystemRecord record) {
        VBox wrapper = new VBox(6);
        wrapper.getStyleClass().add("comment-card");

        HBox row = new HBox(10);
        row.getStyleClass().add("comment-row");
        Label avatar = new Label(initials(record.getTenLienKet()));
        avatar.getStyleClass().add("comment-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
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

    private void renderReplies(SystemRecord parent, VBox replies) {
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan") || !isReplyTo(record, parent)) {
                continue;
            }
            replies.getChildren().add(replyRow(record));
        }
    }

    private HBox replyRow(SystemRecord reply) {
        HBox row = new HBox(8);
        row.getStyleClass().add("reply-row");

        Label avatar = new Label(initials(reply.getTenLienKet()));
        avatar.getStyleClass().add("reply-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label author = new Label(reply.getTenLienKet() + "  ·  " + reply.getNgay());
        author.getStyleClass().add("comment-author");
        Label content = new Label(reply.getNoiDung());
        content.setWrapText(true);
        content.getStyleClass().add("comment-text");
        body.getChildren().addAll(author, content);

        row.getChildren().addAll(avatar, body);
        return row;
    }

    private boolean isReplyRecord(SystemRecord record) {
        return replyParentId(record).length() > 0;
    }

    private boolean isReplyTo(SystemRecord reply, SystemRecord parent) {
        return parent.getMaChinh().equalsIgnoreCase(replyParentId(reply));
    }

    private String replyParentId(SystemRecord record) {
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

    private String campaignId(SystemRecord record) {
        if (record.getMaChienDich() != null && !record.getMaChienDich().isBlank()) {
            return record.getMaChienDich();
        }
        if (record.getMaLienKet() != null && record.getMaLienKet().toUpperCase().startsWith("CD")) {
            return record.getMaLienKet();
        }
        return "";
    }

    private String groupCode(SystemRecord record) {
        return record.getNhomBang() == null ? "" : normalized(record.getNhomBang());
    }

    private String initials(String value) {
        if (value == null || value.isBlank()) {
            return "?";
        }
        String[] parts = value.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length == 1 ? "" : parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    private String normalized(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }

    private String value(TextField textField) {
        return textField == null || textField.getText() == null ? "" : textField.getText().trim();
    }

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }
}
