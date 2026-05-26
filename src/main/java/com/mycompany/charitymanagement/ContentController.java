package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ContentController {

    private static final String ALL_CAMPAIGNS = "Tất cả chiến dịch";
    private static final String ALL_CAMPAIGN_CONTENT = "Tin tức và bình luận";
    private static final String ALL_STATUSES = "Tất cả trạng thái";
    private static final int MAX_VISIBLE_COMMENTS = 8;
    private static final int MAX_VISIBLE_ALL_COMMENTS = 12;
    private static final int MAX_VISIBLE_REPLIES = 4;
    private static final String REPLY_MARKER = "REPLY_TO=";
    private static final String REACTION_PREFIX = "REACTION_";

    private static final String[] FORM_LABELS = {
        "Nhóm nội dung", "Liên quan đến", "Tiêu đề hiển thị",
        "Nội dung", "Ngày đăng/gửi", "Trạng thái hiển thị", "Ghi chú nội bộ"
    };

    @FXML
    private Label lblNewsCount;
    @FXML
    private Label lblNoticeCount;
    @FXML
    private Label lblPendingContent;
    @FXML
    private Label lblVisibleCount;
    @FXML
    private Label lblFeaturedCategory;
    @FXML
    private Label lblFeaturedTitle;
    @FXML
    private Label lblFeaturedSummary;
    @FXML
    private Label lblFeaturedDate;
    @FXML
    private Label lblFeaturedPlace;
    @FXML
    private Label lblFeaturedOrg;
    @FXML
    private Label lblFeaturedParticipant;
    @FXML
    private Label lblFeaturedLikes;
    @FXML
    private Label lblFeaturedComments;
    @FXML
    private Label lblSelectedCommentCampaign;
    @FXML
    private Label lblSaveFeatured;
    @FXML
    private HBox featuredCard;
    @FXML
    private GridPane upcomingGrid;
    @FXML
    private VBox commentList;
    @FXML
    private VBox allCommentList;
    @FXML
    private VBox quickStatsBox;
    @FXML
    private VBox topActivitiesBox;
    @FXML
    private HBox trendsBox;
    @FXML
    private ComboBox<String> cboCampaignFilter;
    @FXML
    private ComboBox<String> cboContentTypeFilter;
    @FXML
    private ComboBox<String> cboContentStatusFilter;
    @FXML
    private TextField txtContentSearch;
    @FXML
    private TextField txtQuickComment;
    @FXML
    private TextField txtRegisterContact;
    @FXML
    private TabPane tabContentArea;

    @FXML
    private TableView<SystemRecord> tableRecords;
    @FXML
    private TableColumn<SystemRecord, String> colNhomBang;
    @FXML
    private TableColumn<SystemRecord, String> colMaChinh;
    @FXML
    private TableColumn<SystemRecord, String> colMaLienKet;
    @FXML
    private TableColumn<SystemRecord, String> colTieuDe;
    @FXML
    private TableColumn<SystemRecord, String> colNoiDung;
    @FXML
    private TableColumn<SystemRecord, String> colNgay;
    @FXML
    private TableColumn<SystemRecord, String> colTrangThai;
    @FXML
    private TableColumn<SystemRecord, String> colGhiChu;
    @FXML
    private TableColumn<SystemRecord, String> colThaoTac;

    @FXML
    private TableView<SystemRecord> tableNotifications;
    @FXML
    private TableColumn<SystemRecord, String> colNotifyTarget;
    @FXML
    private TableColumn<SystemRecord, String> colNotifyTitle;
    @FXML
    private TableColumn<SystemRecord, String> colNotifyContent;
    @FXML
    private TableColumn<SystemRecord, String> colNotifyDate;
    @FXML
    private TableColumn<SystemRecord, String> colNotifyStatus;
    @FXML
    private TableColumn<SystemRecord, String> colNotifyAction;

    @FXML
    private TableView<SystemRecord> tableLogs;
    @FXML
    private TableColumn<SystemRecord, String> colLogActor;
    @FXML
    private TableColumn<SystemRecord, String> colLogTitle;
    @FXML
    private TableColumn<SystemRecord, String> colLogContent;
    @FXML
    private TableColumn<SystemRecord, String> colLogDate;
    @FXML
    private TableColumn<SystemRecord, String> colLogStatus;
    @FXML
    private TableColumn<SystemRecord, String> colLogNote;

    private FilteredList<SystemRecord> filteredCampaignContents;
    private FilteredList<SystemRecord> filteredNotifications;
    private FilteredList<SystemRecord> filteredLogs;
    private boolean refreshingView;
    private String selectedCampaignId;

    @FXML
    private void initialize() {
        configureCampaignContentTable();
        configureNotificationTable();
        configureLogTable();

        filteredCampaignContents = new FilteredList<>(AppData.getContents(), this::isCampaignContent);
        filteredNotifications = new FilteredList<>(AppData.getContents(), this::isNotification);
        filteredLogs = new FilteredList<>(AppData.getContents(), this::isLogRecord);
        tableRecords.setItems(filteredCampaignContents);
        tableNotifications.setItems(filteredNotifications);
        tableLogs.setItems(filteredLogs);

        cboCampaignFilter.setItems(buildCampaignChoices());
        cboCampaignFilter.setValue(ALL_CAMPAIGNS);
        cboContentTypeFilter.setItems(FXCollections.observableArrayList(
                ALL_CAMPAIGN_CONTENT, "Tin tức", "Bình luận"
        ));
        cboContentTypeFilter.setValue(ALL_CAMPAIGN_CONTENT);
        cboContentStatusFilter.setItems(buildStatusFilterChoices());
        cboContentStatusFilter.setValue(ALL_STATUSES);

        cboCampaignFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !ALL_CAMPAIGNS.equals(newValue)) {
                selectCampaignById(codeOf(newValue), false);
            }
            applyContentFilters();
        });
        cboContentTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyContentFilters());
        cboContentStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyContentFilters());
        txtContentSearch.textProperty().addListener((observable, oldValue, newValue) -> applyContentFilters());
        AppData.getActivities().addListener((ListChangeListener<ActivityModel>) change -> {
            refreshCampaignChoices();
            renderPortal();
        });
        AppData.getContents().addListener((ListChangeListener<SystemRecord>) change -> {
            updateContentDashboard();
            renderPortal();
            applyContentFilters();
        });
        AppData.getParticipants().addListener((ListChangeListener<ParticipantModel>) change -> renderPortal());
        AppData.getSponsors().addListener((ListChangeListener<SponsorModel>) change -> renderPortal());
        AppData.getDonations().addListener((ListChangeListener<DonationModel>) change -> renderPortal());

        setDetailRowFactory(tableRecords);
        setDetailRowFactory(tableNotifications);
        setDetailRowFactory(tableLogs);
        tableRecords.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !campaignId(newValue).isEmpty()) {
                selectCampaignById(campaignId(newValue), false);
            }
        });
        if (featuredCard != null) {
            featuredCard.setOnMouseClicked(event -> showCampaignDetail(featuredCampaign()));
        }
        if (lblSaveFeatured != null) {
            lblSaveFeatured.setOnMouseClicked(event -> {
                event.consume();
                saveFeaturedCampaign();
            });
        }
        selectedCampaignId = defaultSelectedCampaignId();
        updateContentDashboard();
        renderPortal();
        applyContentFilters();
    }

    private void configureCampaignContentTable() {
        colNhomBang.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenNhomBang()));
        colMaChinh.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaChinh()));
        colMaChinh.setVisible(false);
        colMaLienKet.setText("Chiến dịch / người bình luận");
        colMaLienKet.setCellValueFactory(cell -> new SimpleStringProperty(relatedText(cell.getValue())));
        colTieuDe.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTieuDe()));
        colNoiDung.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNoiDung()));
        colNgay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgay()));
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colGhiChu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGhiChu()));
        colThaoTac.setCellValueFactory(cell -> new SimpleStringProperty(actionText(cell.getValue())));
        configureTable(tableRecords);
    }

    private void configureNotificationTable() {
        colNotifyTarget.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenLienKet()));
        colNotifyTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTieuDe()));
        colNotifyContent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNoiDung()));
        colNotifyDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgay()));
        colNotifyStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colNotifyAction.setCellValueFactory(cell -> new SimpleStringProperty(actionText(cell.getValue())));
        configureTable(tableNotifications);
    }

    private void configureLogTable() {
        colLogActor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenLienKet()));
        colLogTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTieuDe()));
        colLogContent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNoiDung()));
        colLogDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgay()));
        colLogStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colLogNote.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGhiChu()));
        configureTable(tableLogs);
    }

    private void configureTable(TableView<SystemRecord> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(36.0);
    }

    private void setDetailRowFactory(TableView<SystemRecord> table) {
        table.setRowFactory(view -> {
            TableRow<SystemRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    clearOtherSelections(table);
                    if (event.getClickCount() == 2) {
                        table.getSelectionModel().select(row.getItem());
                        showRecordDetail(row.getItem());
                    }
                }
            });
            return row;
        });
    }

    @FXML
    private void handleAddRecord() {
        SystemRecord record = showRecordDialog("Tạo nội dung", null);
        if (record == null) {
            return;
        }
        if (existsById(record.getMaChinh(), null)) {
            DialogUtils.warning("Mã chính đã tồn tại trong nhóm nội dung.");
            return;
        }

        AppData.getContents().add(record);
        clearAllSelections();
        DialogUtils.info("Đã tạo nội dung mới.");
    }

    @FXML
    private void handleUpdateRecord() {
        SystemRecord selected = selectedRecord();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn bản ghi cần cập nhật.");
            return;
        }

        SystemRecord form = showRecordDialog("Chỉnh sửa nội dung", selected);
        if (form == null) {
            return;
        }
        if (existsById(form.getMaChinh(), selected)) {
            DialogUtils.warning("Mã chính đã tồn tại trong nhóm nội dung.");
            return;
        }

        selected.setNhomBang(form.getNhomBang());
        selected.setMaChinh(form.getMaChinh());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setMaLienKet(form.getMaLienKet());
        selected.setTieuDe(form.getTieuDe());
        selected.setNoiDung(form.getNoiDung());
        selected.setNgay(form.getNgay());
        selected.setTrangThai(form.getTrangThai());
        selected.setGhiChu(form.getGhiChu());
        refreshContentView();
        clearAllSelections();
        DialogUtils.info("Đã chỉnh sửa nội dung.");
    }

    @FXML
    private void handleDeleteRecord() {
        SystemRecord selected = selectedRecord();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa bản ghi nội dung " + selected.getMaChinh() + "?")) {
            return;
        }

        AppData.getContents().remove(selected);
        clearAllSelections();
        DialogUtils.info("Đã xóa nội dung.");
    }

    @FXML
    private void handleClearContentFilters() {
        cboCampaignFilter.setValue(ALL_CAMPAIGNS);
        cboContentTypeFilter.setValue(ALL_CAMPAIGN_CONTENT);
        cboContentStatusFilter.setValue(ALL_STATUSES);
        txtContentSearch.clear();
        selectedCampaignId = defaultSelectedCampaignId();
        renderComments();
        applyContentFilters();
    }

    @FXML
    private void handleViewAllCampaigns() {
        if (tabContentArea != null) {
            tabContentArea.getSelectionModel().select(0);
        }
        cboCampaignFilter.setValue(ALL_CAMPAIGNS);
        cboContentTypeFilter.setValue(ALL_CAMPAIGN_CONTENT);
        cboContentStatusFilter.setValue(ALL_STATUSES);
        txtContentSearch.clear();
        applyContentFilters();
        DetailDialogUtils.showDetails(featuredCard, "Tất cả chiến dịch", allCampaignRows());
    }

    @FXML
    private void handleOpenNotifications() {
        if (tabContentArea != null) {
            tabContentArea.getSelectionModel().select(1);
        }
        DetailDialogUtils.showDetails(quickStatsBox, "Thông báo hệ thống", notificationRows());
    }

    @FXML
    private void handleSendQuickComment() {
        ActivityModel campaign = selectedCampaign();
        if (campaign == null) {
            DialogUtils.warning("Chưa có chiến dịch để gửi bình luận.");
            return;
        }
        String text = value(txtQuickComment);
        if (text.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập nội dung bình luận.");
            return;
        }
        UserAccount user = currentUser();
        String note = "Gửi nhanh từ Trung tâm nội dung";
        AppData.getContents().add(new SystemRecord(
                "BinhLuan",
                AppData.nextContentId("BL"),
                campaign.getMaChienDich(),
                user.getUsername(),
                "Bình luận mới",
                text,
                AppData.todayText(),
                "",
                "Chờ duyệt",
                user.getUsername(),
                "ADMIN",
                note
        ));
        txtQuickComment.clear();
        if (tabContentArea != null) {
            tabContentArea.getSelectionModel().select(0);
        }
        DialogUtils.info("Đã ghi nhận bình luận. Nội dung đang chờ duyệt.");
    }

    @FXML
    private void handleJoinNow() {
        String contact = value(txtRegisterContact);
        if (contact.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập email hoặc số điện thoại.");
            return;
        }
        if (!isValidContact(contact)) {
            DialogUtils.warning("Email cần có dạng @gmail.com hoặc số điện thoại bắt đầu bằng 09/08/07/03.");
            return;
        }
        AppData.getContents().add(new SystemRecord(
                "ThongBao",
                AppData.nextContentId("TB"),
                "",
                "ADMIN",
                "Đăng ký nhận tin mới",
                "Người dùng để lại liên hệ: " + contact,
                AppData.todayText(),
                "",
                "Chưa đọc",
                "HE_THONG",
                "ADMIN",
                "Tạo từ khung Đăng ký tham gia"
        ));
        txtRegisterContact.clear();
        if (tabContentArea != null) {
            tabContentArea.getSelectionModel().select(1);
        }
        DialogUtils.info("Đã lưu thông tin đăng ký nhận tin.");
    }

    private void saveFeaturedCampaign() {
        ActivityModel campaign = featuredCampaign();
        if (campaign == null) {
            DialogUtils.warning("Chưa có chiến dịch để lưu.");
            return;
        }
        UserAccount user = currentUser();
        AppData.getContents().add(new SystemRecord(
                "ThongBao",
                AppData.nextContentId("TB"),
                "",
                user.getUsername(),
                "Đã lưu chiến dịch",
                "Bạn đã lưu chiến dịch: " + campaign.getTenChienDich(),
                AppData.todayText(),
                "",
                "Chưa đọc",
                user.getUsername(),
                "ADMIN",
                "Tạo từ nút Lưu tin"
        ));
        DialogUtils.info("Đã lưu chiến dịch nổi bật vào thông báo của bạn.");
    }

    @FXML
    private void handleExportContent() {
        TableView<SystemRecord> table = activeTable();
        int tabIndex = activeTabIndex();
        String fileName;
        if (tabIndex == 1) {
            fileName = "danh-sach-thong-bao.csv";
        } else if (tabIndex == 2) {
            fileName = "nhat-ky-he-thong.csv";
        } else {
            fileName = "tin-tuc-binh-luan-chien-dich.csv";
        }
        ExportUtils.exportTableToCsv(table, "Xuất danh sách nội dung", fileName);
    }

    @FXML
    private void handleBackHome() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_DASHBOARD);
    }

    @FXML
    private void handleActivities() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleParticipants() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_PARTICIPANTS);
    }

    @FXML
    private void handleSponsors() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_SPONSORS);
    }

    @FXML
    private void handleDonations() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_DONATIONS);
    }

    @FXML
    private void handleOperations() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_OPERATIONS);
    }

    @FXML
    private void handleContent() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_CONTENT);
    }

    @FXML
    private void handleReports() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_REPORTS);
    }

    @FXML
    private void handleLogout() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_LOGIN);
    }

    private SystemRecord showRecordDialog(String title, SystemRecord current) {
        String[] values = current == null
                ? new String[]{"Tin tức", defaultRelatedOption(), "", "", AppData.todayText(), "Đã đăng", ""}
                : new String[]{
                    current.getTenNhomBang(), current.getMaLienKet() + " - " + current.getTenLienKet(),
                    current.getTieuDe(), current.getNoiDung(), current.getNgay(), current.getTrangThai(),
                    current.getGhiChu()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildRecord(result, current);
    }

    private SystemRecord buildRecord(String[] values, SystemRecord current) {
        String groupCode = contentGroupCode(values[0]);
        String maChinh = current == null ? AppData.nextContentId(prefixFor(groupCode)) : current.getMaChinh();
        String maLienKet = codeOf(values[1]);
        if (values[0].isEmpty() || maLienKet.isEmpty() || values[2].isEmpty()) {
            DialogUtils.warning("Vui lòng chọn loại nội dung, đối tượng liên quan và nhập tiêu đề.");
            return null;
        }

        String campaignId = resolveCampaignForRecord(groupCode, maLienKet, current);
        return new SystemRecord(groupCode, maChinh, campaignId, maLienKet,
                values[2], values[3], values[4], "", values[5], "ADMIN", "", values[6]);
    }

    private boolean existsById(String id, SystemRecord current) {
        return AppData.getContents().stream()
                .anyMatch(item -> item != current && item.getMaChinh().equalsIgnoreCase(id));
    }

    private void showRecordDetail(SystemRecord record) {
        DetailDialogUtils.showDetails(activeTable(), record.getTenNhomBang() + " - " + record.getTieuDe(), new String[][]{
            {"Loại nội dung", record.getTenNhomBang()},
            {"Chiến dịch", campaignName(record)},
            {"Liên quan đến", record.getTenLienKet()},
            {"Tiêu đề", record.getTieuDe()},
            {"Nội dung", record.getNoiDung()},
            {"Ngày đăng/gửi", record.getNgay()},
            {"Trạng thái", record.getTrangThai()},
            {"Ghi chú nội bộ", record.getGhiChu()},
            {"Ý nghĩa", describeRecord(record)}
        });
    }

    private void showCampaignDetail(ActivityModel activity) {
        if (activity == null) {
            DialogUtils.warning("Chưa có chiến dịch để xem chi tiết.");
            return;
        }
        selectCampaign(activity, true);
        String campaignId = activity.getMaChienDich();
        DetailDialogUtils.showDetails(featuredCard, activity.getTenChienDich(), new String[][]{
            {"Mã chiến dịch", campaignId},
            {"Mô tả", activity.getMoTa()},
            {"Thời gian", activity.getNgayBatDau() + " - " + activity.getNgayKetThuc()},
            {"Địa điểm", activity.getDiaDiem()},
            {"Mục tiêu quyên góp", FormatUtils.money(activity.getMucTieuTien())},
            {"Đã ghi nhận", FormatUtils.money(AppData.getCampaignMoneyTotal(campaignId))},
            {"Tình nguyện viên", AppData.getCampaignParticipantCount(campaignId) + " người"},
            {"Trạng thái", activity.getTrangThai()},
            {"Tin tức liên quan", campaignNewsLines(campaignId)},
            {"Bình luận liên quan", campaignCommentLines(campaignId)}
        });
    }

    private String[][] allCampaignRows() {
        if (AppData.getActivities().isEmpty()) {
            return new String[][]{{"Danh sách", "Chưa có chiến dịch nào."}};
        }
        String[][] rows = new String[AppData.getActivities().size()][2];
        int index = 0;
        for (ActivityModel activity : AppData.getActivities()) {
            String campaignId = activity.getMaChienDich();
            rows[index][0] = campaignId + " - " + activity.getTenChienDich();
            rows[index][1] = activity.getNgayBatDau() + " - " + activity.getNgayKetThuc()
                    + "\nĐịa điểm: " + activity.getDiaDiem()
                    + "\nMục tiêu: " + FormatUtils.money(activity.getMucTieuTien())
                    + "\nĐã ghi nhận: " + FormatUtils.money(AppData.getCampaignMoneyTotal(campaignId))
                    + "\nTNV tham gia: " + AppData.getCampaignParticipantCount(campaignId)
                    + "\nTrạng thái: " + activity.getTrangThai();
            index++;
        }
        return rows;
    }

    private String[][] notificationRows() {
        long count = AppData.getContents().stream().filter(this::isNotification).count();
        if (count == 0) {
            return new String[][]{{"Thông báo", "Chưa có thông báo nào."}};
        }
        String[][] rows = new String[(int) count][2];
        int index = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!isNotification(record)) {
                continue;
            }
            rows[index][0] = record.getNgay() + " - " + record.getTenLienKet();
            rows[index][1] = record.getTieuDe()
                    + "\n" + record.getNoiDung()
                    + "\nTrạng thái: " + record.getTrangThai();
            index++;
        }
        return rows;
    }

    private String campaignNewsLines(String campaignId) {
        StringBuilder builder = new StringBuilder();
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("tintuc") || !campaignId.equalsIgnoreCase(campaignId(record))) {
                continue;
            }
            appendLine(builder, "- " + record.getTieuDe() + ": " + record.getNoiDung());
        }
        return builder.length() == 0 ? "Chưa có tin tức cho chiến dịch này." : builder.toString();
    }

    private String campaignCommentLines(String campaignId) {
        StringBuilder builder = new StringBuilder();
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan") || !campaignId.equalsIgnoreCase(campaignId(record))) {
                continue;
            }
            appendLine(builder, "- " + record.getTenLienKet() + ": " + record.getNoiDung()
                    + " (" + record.getTrangThai() + ")");
        }
        return builder.length() == 0 ? "Chưa có bình luận cho chiến dịch này." : builder.toString();
    }

    private void appendLine(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(line);
    }

    private String describeRecord(SystemRecord record) {
        String group = record.getNhomBang() == null ? "" : record.getNhomBang().toLowerCase();
        if (group.contains("tintuc")) {
            return "Tin tức công khai của một chiến dịch.";
        }
        if (group.contains("binhluan")) {
            return "Bình luận/phản hồi nằm trong luồng tin tức của chiến dịch.";
        }
        if (group.contains("thongbao")) {
            return "Thông báo gửi đến tài khoản trong hệ thống.";
        }
        if (group.contains("nhatky")) {
            return "Nhật ký ghi lại thao tác quản trị và thay đổi dữ liệu.";
        }
        return "Bản ghi nội dung/hệ thống.";
    }

    private String actionText(SystemRecord record) {
        String status = record.getTrangThai() == null ? "" : record.getTrangThai().toLowerCase();
        if (status.contains("ẩn")) {
            return "Xem   Sửa   Hiện";
        }
        if (status.contains("chờ") || status.contains("nháp")) {
            return "Xem   Sửa   Duyệt";
        }
        return "Xem   Sửa   Ẩn";
    }

    private void refreshContentView() {
        if (refreshingView) {
            return;
        }
        refreshingView = true;
        try {
            updateContentDashboard();
            renderPortal();
            refreshStatusChoices();
            applyContentFilters();
            tableRecords.refresh();
            tableNotifications.refresh();
            tableLogs.refresh();
        } finally {
            refreshingView = false;
        }
    }

    private void refreshStatusChoices() {
        String currentStatus = cboContentStatusFilter.getValue();
        cboContentStatusFilter.setItems(buildStatusFilterChoices());
        if (cboContentStatusFilter.getItems().contains(currentStatus)) {
            cboContentStatusFilter.setValue(currentStatus);
        } else {
            cboContentStatusFilter.setValue(ALL_STATUSES);
        }
    }

    private void refreshCampaignChoices() {
        String currentCampaign = cboCampaignFilter.getValue();
        cboCampaignFilter.setItems(buildCampaignChoices());
        if (cboCampaignFilter.getItems().contains(currentCampaign)) {
            cboCampaignFilter.setValue(currentCampaign);
        } else {
            cboCampaignFilter.setValue(ALL_CAMPAIGNS);
        }
    }

    private void updateContentDashboard() {
        long newsCount = AppData.getContents().stream().filter(this::isCampaignContent).count();
        long noticeCount = AppData.getContents().stream().filter(this::isNotification).count();
        long visibleCount = AppData.getContents().stream()
                .filter(record -> "Đã đăng".equals(record.getTrangThai())
                || "Hiển thị".equals(record.getTrangThai())
                || "Đã đọc".equals(record.getTrangThai())
                || "Đã ghi".equals(record.getTrangThai())
                || "Đang dùng".equals(record.getTrangThai()))
                .count();
        long pendingCount = AppData.getContents().stream()
                .filter(record -> "Chờ duyệt".equals(record.getTrangThai())
                || "Bản nháp".equals(record.getTrangThai())
                || "Chưa đọc".equals(record.getTrangThai()))
                .count();
        setLabelText(lblNewsCount, String.valueOf(newsCount));
        setLabelText(lblNoticeCount, String.valueOf(noticeCount));
        setLabelText(lblPendingContent, String.valueOf(pendingCount));
        setLabelText(lblVisibleCount, String.valueOf(visibleCount));
    }

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private void renderPortal() {
        ensureSelectedCampaign();
        renderFeaturedCampaign();
        renderUpcomingCards();
        renderComments();
        renderQuickStats();
        renderTopActivities();
        renderTrends();
    }

    private void renderFeaturedCampaign() {
        ActivityModel featured = featuredCampaign();
        if (featured == null) {
            lblFeaturedCategory.setText("CHIẾN DỊCH");
            lblFeaturedTitle.setText("Chưa có chiến dịch nổi bật");
            lblFeaturedSummary.setText("Hãy thêm chiến dịch và tin tức để hiển thị ở khu vực này.");
            lblFeaturedDate.setText("Chưa có lịch");
            lblFeaturedPlace.setText("Chưa có địa điểm");
            lblFeaturedOrg.setText("Đoàn TNCS HCM");
            lblFeaturedParticipant.setText("0 tham gia");
            lblFeaturedLikes.setText("0 lượt thích");
            lblFeaturedComments.setText("0 bình luận");
            return;
        }

        long participantCount = AppData.getCampaignParticipantCount(featured.getMaChienDich());
        long commentCount = campaignCommentCount(featured.getMaChienDich());
        int reactionCount = (int) Math.max(24, Math.round(AppData.getCampaignMoneyTotal(featured.getMaChienDich()) / 150000.0));
        lblFeaturedCategory.setText("CHIẾN DỊCH");
        lblFeaturedTitle.setText(featured.getTenChienDich());
        lblFeaturedSummary.setText(featured.getMoTa());
        lblFeaturedDate.setText(featured.getNgayBatDau() + " - " + featured.getNgayKetThuc());
        lblFeaturedPlace.setText(featured.getDiaDiem());
        lblFeaturedOrg.setText("Đoàn TNCS HCM");
        lblFeaturedParticipant.setText(participantCount + " tham gia");
        lblFeaturedLikes.setText(reactionCount + " lượt thích");
        lblFeaturedComments.setText(commentCount + " bình luận");
    }

    private ActivityModel featuredCampaign() {
        ActivityModel best = null;
        double bestScore = -1;
        for (ActivityModel activity : AppData.getActivities()) {
            double score = AppData.getCampaignMoneyTotal(activity.getMaChienDich())
                    + AppData.getCampaignParticipantCount(activity.getMaChienDich()) * 1000000.0;
            if (score > bestScore) {
                best = activity;
                bestScore = score;
            }
        }
        return best;
    }

    private void renderUpcomingCards() {
        upcomingGrid.getChildren().clear();
        int index = 0;
        for (ActivityModel activity : AppData.getActivities()) {
            if (index >= 6) {
                break;
            }
            upcomingGrid.add(campaignCard(activity, index), index % 2, index / 2);
            index++;
        }
    }

    private HBox campaignCard(ActivityModel activity, int index) {
        HBox card = new HBox(12);
        card.getStyleClass().add("upcoming-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseClicked(event -> showCampaignDetail(activity));

        StackPane thumb = new StackPane();
        thumb.getStyleClass().add("activity-thumb");
        Label icon = new Label(iconForCampaign(activity));
        icon.getStyleClass().add("activity-thumb-icon");
        thumb.getChildren().add(icon);

        VBox body = new VBox(4);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label category = new Label(categoryForCampaign(activity));
        category.getStyleClass().add("content-category");
        Label title = new Label(activity.getTenChienDich());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        Label summary = new Label(activity.getMoTa());
        summary.getStyleClass().add("card-summary");
        summary.setWrapText(true);
        Label meta = new Label(activity.getNgayBatDau() + "     " + AppData.getCampaignParticipantCount(activity.getMaChienDich()) + " TNV");
        meta.getStyleClass().add("muted-text");
        body.getChildren().addAll(category, title, summary, meta);
        card.getChildren().addAll(thumb, body);
        return card;
    }

    private void renderComments() {
        renderSelectedCampaignComments();
        renderAllCampaignComments();
    }

    private void renderSelectedCampaignComments() {
        if (commentList == null) {
            return;
        }
        commentList.getChildren().clear();
        ActivityModel campaign = selectedCampaign();
        String campaignId = campaign == null ? "" : campaign.getMaChienDich();
        if (lblSelectedCommentCampaign != null) {
            lblSelectedCommentCampaign.setText(campaign == null
                    ? "Chọn chiến dịch để xem bình luận"
                    : campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
        }

        int count = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan")
                    || isReplyRecord(record)
                    || !campaignId.equalsIgnoreCase(campaignId(record))) {
                continue;
            }
            commentList.getChildren().add(commentCard(record, false));
            count++;
            if (count >= MAX_VISIBLE_COMMENTS) {
                break;
            }
        }
        if (count == 0) {
            Label empty = new Label(campaign == null
                    ? "Chưa có chiến dịch để hiển thị bình luận."
                    : "Chưa có bình luận cho chiến dịch này.");
            empty.getStyleClass().add("muted-text");
            commentList.getChildren().add(empty);
        }
    }

    private void renderAllCampaignComments() {
        if (allCommentList == null) {
            return;
        }
        allCommentList.getChildren().clear();
        int count = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan") || isReplyRecord(record)) {
                continue;
            }
            allCommentList.getChildren().add(commentCard(record, true));
            count++;
            if (count >= MAX_VISIBLE_ALL_COMMENTS) {
                break;
            }
        }
        if (count == 0) {
            Label empty = new Label("Chưa có bình luận nào từ các chiến dịch.");
            empty.getStyleClass().add("muted-text");
            allCommentList.getChildren().add(empty);
        }
    }

    private VBox commentCard(SystemRecord record, boolean showCampaignContext) {
        VBox wrapper = new VBox(8);
        wrapper.getStyleClass().add("comment-card");

        HBox row = new HBox(10);
        row.getStyleClass().add("comment-row");
        row.setOnMouseClicked(event -> {
            if (showCampaignContext) {
                selectCampaignById(campaignId(record), true);
            }
            showRecordDetail(record);
        });
        Label avatar = new Label(initials(record.getTenLienKet()));
        avatar.getStyleClass().add("comment-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label name = new Label(record.getTenLienKet() + "  ·  " + record.getNgay());
        name.getStyleClass().add("comment-author");
        Label campaignLabel = new Label(campaignContextText(record));
        campaignLabel.getStyleClass().add("comment-campaign-chip");
        Label content = new Label(record.getNoiDung());
        content.setWrapText(true);
        content.getStyleClass().add("comment-text");
        Label status = new Label(record.getTrangThai());
        status.getStyleClass().add("muted-text");

        HBox reactions = new HBox(6);
        reactions.getStyleClass().add("reaction-row");
        reactions.getChildren().addAll(
                reactionButton(record, "LIKE", "Like"),
                reactionButton(record, "HEART", "Tym"),
                reactionButton(record, "SMILE", "Vui")
        );

        HBox replyBox = new HBox(8);
        replyBox.getStyleClass().add("reply-box");
        TextField replyInput = new TextField();
        replyInput.setPromptText("Nhập phản hồi bình luận...");
        replyInput.getStyleClass().addAll("input-field", "reply-input");
        HBox.setHgrow(replyInput, javafx.scene.layout.Priority.ALWAYS);
        Button replyButton = new Button("Phản hồi");
        replyButton.getStyleClass().add("quick-button");
        replyButton.setOnAction(event -> {
            event.consume();
            sendReply(record, replyInput);
        });
        replyInput.setOnAction(event -> {
            event.consume();
            sendReply(record, replyInput);
        });
        replyBox.getChildren().addAll(replyInput, replyButton);

        VBox replies = new VBox(6);
        replies.getStyleClass().add("reply-list");
        renderReplies(record, replies);

        body.getChildren().add(name);
        if (showCampaignContext) {
            body.getChildren().add(campaignLabel);
        }
        body.getChildren().addAll(content, status, reactions, replies, replyBox);
        row.getChildren().addAll(avatar, body);
        wrapper.getChildren().add(row);
        return wrapper;
    }

    private void renderReplies(SystemRecord parent, VBox replies) {
        int count = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan") || !isReplyTo(record, parent)) {
                continue;
            }
            replies.getChildren().add(replyRow(record));
            count++;
            if (count >= MAX_VISIBLE_REPLIES) {
                break;
            }
        }
    }

    private HBox replyRow(SystemRecord reply) {
        HBox row = new HBox(8);
        row.getStyleClass().add("reply-row");
        row.setOnMouseClicked(event -> showRecordDetail(reply));

        Label avatar = new Label(initials(reply.getTenLienKet()));
        avatar.getStyleClass().add("reply-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label name = new Label(reply.getTenLienKet() + "  ·  " + reply.getNgay());
        name.getStyleClass().add("comment-author");
        Label content = new Label(reply.getNoiDung());
        content.getStyleClass().add("comment-text");
        content.setWrapText(true);
        body.getChildren().addAll(name, content);

        row.getChildren().addAll(avatar, body);
        return row;
    }

    private Button reactionButton(SystemRecord record, String key, String label) {
        Button button = new Button(label + " " + reactionCount(record, key));
        button.getStyleClass().add("reaction-button");
        button.setOnAction(event -> {
            event.consume();
            incrementReaction(record, key);
        });
        return button;
    }

    private void sendReply(SystemRecord parent, TextField replyInput) {
        String text = value(replyInput);
        if (text.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập nội dung phản hồi.");
            return;
        }
        String campaignId = campaignId(parent);
        ActivityModel campaign = AppData.findCampaign(campaignId);
        if (campaign == null) {
            DialogUtils.warning("Không xác định được chiến dịch của bình luận này.");
            return;
        }

        UserAccount user = currentUser();
        String note = appendNote("Phản hồi cho " + parent.getMaChinh(), REPLY_MARKER + parent.getMaChinh());
        AppData.getContents().add(new SystemRecord(
                "BinhLuan",
                AppData.nextContentId("BL"),
                campaignId,
                user.getUsername(),
                "Phản hồi bình luận",
                text,
                AppData.todayText(),
                "",
                "Hiển thị",
                user.getUsername(),
                parent.getNguoiTao(),
                note
        ));
        replyInput.clear();
        selectCampaign(campaign, false);
        DialogUtils.info("Đã gửi phản hồi bình luận.");
    }

    private void incrementReaction(SystemRecord record, String key) {
        record.setGhiChu(bumpCounter(record.getGhiChu(), reactionMarker(key)));
        refreshContentView();
    }

    private int reactionCount(SystemRecord record, String key) {
        return markerCount(record.getGhiChu(), reactionMarker(key));
    }

    private String reactionMarker(String key) {
        return REACTION_PREFIX + key + "=";
    }

    private int markerCount(String note, String marker) {
        String value = note == null ? "" : note;
        int start = value.indexOf(marker);
        if (start < 0) {
            return 0;
        }
        int numberStart = start + marker.length();
        int numberEnd = numberStart;
        while (numberEnd < value.length() && Character.isDigit(value.charAt(numberEnd))) {
            numberEnd++;
        }
        if (numberEnd == numberStart) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(numberStart, numberEnd));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String bumpCounter(String note, String marker) {
        String value = note == null ? "" : note;
        int start = value.indexOf(marker);
        if (start < 0) {
            return appendNote(value, marker + "1");
        }

        int numberStart = start + marker.length();
        int numberEnd = numberStart;
        while (numberEnd < value.length() && Character.isDigit(value.charAt(numberEnd))) {
            numberEnd++;
        }
        int next = markerCount(value, marker) + 1;
        return value.substring(0, numberStart) + next + value.substring(numberEnd);
    }

    private String appendNote(String note, String addition) {
        String value = note == null ? "" : note.trim();
        if (value.isEmpty()) {
            return addition;
        }
        if (value.contains(addition)) {
            return value;
        }
        return value + " | " + addition;
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

    private ActivityModel selectedCampaign() {
        ensureSelectedCampaign();
        ActivityModel campaign = selectedCampaignId == null ? null : AppData.findCampaign(selectedCampaignId);
        return campaign == null ? featuredCampaign() : campaign;
    }

    private void ensureSelectedCampaign() {
        if (selectedCampaignId != null && AppData.findCampaign(selectedCampaignId) != null) {
            return;
        }
        selectedCampaignId = defaultSelectedCampaignId();
    }

    private String defaultSelectedCampaignId() {
        ActivityModel campaign = featuredCampaign();
        return campaign == null ? "" : campaign.getMaChienDich();
    }

    private void selectCampaignById(String campaignId, boolean syncFilters) {
        ActivityModel campaign = AppData.findCampaign(campaignId);
        if (campaign != null) {
            selectCampaign(campaign, syncFilters);
        }
    }

    private void selectCampaign(ActivityModel campaign, boolean syncFilters) {
        if (campaign == null) {
            return;
        }
        selectedCampaignId = campaign.getMaChienDich();
        if (syncFilters && cboCampaignFilter != null) {
            cboCampaignFilter.setValue(campaignOption(campaign));
            if (tabContentArea != null) {
                tabContentArea.getSelectionModel().select(0);
            }
            applyContentFilters();
        }
        renderComments();
    }

    private String campaignOption(ActivityModel campaign) {
        return campaign.getMaChienDich() + " - " + campaign.getTenChienDich();
    }

    private void renderQuickStats() {
        HBox campaigns = statRow("Chiến dịch đang mở", String.valueOf(openCampaignCount()), "⚑", "green-soft");
        campaigns.setOnMouseClicked(event -> handleViewAllCampaigns());

        HBox volunteers = statRow("Tình nguyện viên", String.valueOf(AppData.getParticipants().size()), "♟", "blue-soft");
        volunteers.setOnMouseClicked(event -> navigateQuietly(NavigationService.VIEW_PARTICIPANTS));

        HBox hours = statRow("Giờ công cộng đồng", String.valueOf(estimatedCommunityHours()), "◷", "orange-soft");
        hours.setOnMouseClicked(event -> navigateQuietly(NavigationService.VIEW_OPERATIONS));

        HBox donations = statRow("Đợt quyên góp", String.valueOf(AppData.getDonations().size()), "♥", "purple-soft");
        donations.setOnMouseClicked(event -> navigateQuietly(NavigationService.VIEW_DONATIONS));

        quickStatsBox.getChildren().setAll(campaigns, volunteers, hours, donations);
    }

    private HBox statRow(String title, String value, String iconText, String iconClass) {
        HBox row = new HBox(10);
        row.getStyleClass().add("portal-stat-row");
        Label icon = new Label(iconText);
        icon.getStyleClass().addAll("portal-stat-icon", iconClass);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("portal-stat-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("portal-stat-value");
        row.getChildren().addAll(icon, titleLabel, spacer, valueLabel);
        return row;
    }

    private void renderTopActivities() {
        topActivitiesBox.getChildren().clear();
        int index = 1;
        for (ActivityModel activity : AppData.getActivities()) {
            if (index > 5) {
                break;
            }
            topActivitiesBox.getChildren().add(topActivityRow(activity, index));
            index++;
        }
    }

    private HBox topActivityRow(ActivityModel activity, int index) {
        HBox row = new HBox(10);
        row.getStyleClass().add("top-activity-row");
        row.setOnMouseClicked(event -> showCampaignDetail(activity));
        Label number = new Label(String.valueOf(index));
        number.getStyleClass().add("top-rank");
        StackPane thumb = new StackPane();
        thumb.getStyleClass().add("mini-thumb");
        thumb.getChildren().add(new Label(iconForCampaign(activity)));
        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label title = new Label(activity.getTenChienDich());
        title.getStyleClass().add("top-title");
        title.setWrapText(true);
        Label subtitle = new Label(AppData.getCampaignParticipantCount(activity.getMaChienDich()) + " lượt tham gia");
        subtitle.getStyleClass().add("muted-text");
        body.getChildren().addAll(title, subtitle);
        row.getChildren().addAll(number, thumb, body);
        return row;
    }

    private void renderTrends() {
        trendsBox.getChildren().clear();
        String[] tags = {"#MuaHeXanh", "#HiếnMáu", "#BảoVệMôiTrường", "#TrẻEmVùngCao", "#GâyQuỹ"};
        for (String tag : tags) {
            Label label = new Label(tag);
            label.getStyleClass().add("trend-chip");
            label.setOnMouseClicked(event -> {
                txtContentSearch.setText(tag.substring(1));
                if (tabContentArea != null) {
                    tabContentArea.getSelectionModel().select(0);
                }
                applyContentFilters();
            });
            trendsBox.getChildren().add(label);
        }
    }

    private long openCampaignCount() {
        return AppData.getActivities().stream()
                .filter(activity -> !"Hoàn thành".equalsIgnoreCase(activity.getTrangThai()))
                .count();
    }

    private long estimatedCommunityHours() {
        long attendanceOps = AppData.getOperations().stream()
                .filter(record -> "Điểm danh".equalsIgnoreCase(record.getNhomBang()))
                .count();
        return AppData.getParticipants().size() * 6L + attendanceOps * 4L;
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
            return "✚";
        }
        if ("GIÁO DỤC".equals(category)) {
            return "▤";
        }
        if ("MÔI TRƯỜNG".equals(category)) {
            return "♧";
        }
        return "♥";
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

    private void applyContentFilters() {
        if (filteredCampaignContents == null) {
            return;
        }
        filteredCampaignContents.setPredicate(record -> isCampaignContent(record)
                && matchesCampaign(record)
                && matchesCampaignContentType(record)
                && matchesStatus(record)
                && matchesSearch(record));
        filteredNotifications.setPredicate(record -> isNotification(record)
                && matchesStatus(record)
                && matchesSearch(record));
        filteredLogs.setPredicate(record -> isLogRecord(record)
                && matchesStatus(record)
                && matchesSearch(record));
    }

    private boolean matchesCampaign(SystemRecord record) {
        String selected = cboCampaignFilter.getValue();
        if (selected == null || selected.isEmpty() || ALL_CAMPAIGNS.equals(selected)) {
            return true;
        }
        return codeOf(selected).equalsIgnoreCase(campaignId(record));
    }

    private boolean matchesCampaignContentType(SystemRecord record) {
        String type = cboContentTypeFilter.getValue();
        return type == null || type.isEmpty() || ALL_CAMPAIGN_CONTENT.equals(type)
                || type.equals(record.getTenNhomBang());
    }

    private boolean matchesStatus(SystemRecord record) {
        String status = cboContentStatusFilter.getValue();
        return status == null || status.isEmpty() || ALL_STATUSES.equals(status)
                || status.equals(record.getTrangThai());
    }

    private boolean matchesSearch(SystemRecord record) {
        String query = normalized(value(txtContentSearch));
        return query.isEmpty() || normalized(record.getTenNhomBang() + " "
                + campaignName(record) + " " + record.getTenLienKet() + " "
                + record.getTieuDe() + " " + record.getNoiDung() + " "
                + record.getTrangThai() + " " + record.getGhiChu()).contains(query);
    }

    private ObservableList<String> buildCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList(ALL_CAMPAIGNS);
        AppData.getActivities().stream()
                .map(activity -> activity.getMaChienDich() + " - " + activity.getTenChienDich())
                .forEach(choices::add);
        return choices;
    }

    private ObservableList<String> buildStatusFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList(ALL_STATUSES);
        AppData.getContents().stream()
                .map(SystemRecord::getTrangThai)
                .filter(status -> status != null && !status.trim().isEmpty())
                .distinct()
                .forEach(choices::add);
        return choices;
    }

    private String defaultRelatedOption() {
        return AppData.getActivities().stream()
                .findFirst()
                .map(activity -> activity.getMaChienDich() + " - " + activity.getTenChienDich())
                .orElse("");
    }

    private boolean isCampaignContent(SystemRecord record) {
        String group = groupCode(record);
        return group.contains("tintuc") || group.contains("binhluan");
    }

    private boolean isNotification(SystemRecord record) {
        return groupCode(record).contains("thongbao");
    }

    private boolean isLogRecord(SystemRecord record) {
        String group = groupCode(record);
        return group.contains("nhatky") || group.contains("thamso");
    }

    private String groupCode(SystemRecord record) {
        return record.getNhomBang() == null ? "" : normalized(record.getNhomBang());
    }

    private String relatedText(SystemRecord record) {
        if (groupCode(record).contains("binhluan")) {
            return campaignName(record) + " / " + record.getTenLienKet();
        }
        return campaignName(record);
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

    private String campaignName(SystemRecord record) {
        String id = campaignId(record);
        if (id.isEmpty()) {
            return "Không gắn chiến dịch";
        }
        ActivityModel campaign = AppData.findCampaign(id);
        return campaign == null ? id : campaign.getTenChienDich();
    }

    private String campaignContextText(SystemRecord record) {
        String id = campaignId(record);
        return id.isEmpty()
                ? "Không gắn chiến dịch"
                : id + " - " + campaignName(record);
    }

    private String resolveCampaignForRecord(String groupCode, String maLienKet, SystemRecord current) {
        if (maLienKet != null && maLienKet.toUpperCase().startsWith("CD")) {
            return maLienKet;
        }
        if (current != null && current.getMaChienDich() != null && !current.getMaChienDich().isBlank()) {
            return current.getMaChienDich();
        }
        String selectedCampaign = cboCampaignFilter == null ? "" : cboCampaignFilter.getValue();
        if (selectedCampaign != null && !selectedCampaign.isEmpty() && !ALL_CAMPAIGNS.equals(selectedCampaign)) {
            return codeOf(selectedCampaign);
        }
        return groupCode != null && groupCode.equalsIgnoreCase("BinhLuan")
                ? AppData.getActivities().stream().findFirst().map(ActivityModel::getMaChienDich).orElse("")
                : "";
    }

    private TableView<SystemRecord> activeTable() {
        int tabIndex = activeTabIndex();
        if (tabIndex == 1) {
            return tableNotifications;
        }
        if (tabIndex == 2) {
            return tableLogs;
        }
        return tableRecords;
    }

    private int activeTabIndex() {
        return tabContentArea == null ? 0 : tabContentArea.getSelectionModel().getSelectedIndex();
    }

    private SystemRecord selectedRecord() {
        return activeTable().getSelectionModel().getSelectedItem();
    }

    private void clearAllSelections() {
        tableRecords.getSelectionModel().clearSelection();
        tableNotifications.getSelectionModel().clearSelection();
        tableLogs.getSelectionModel().clearSelection();
    }

    private void clearOtherSelections(TableView<SystemRecord> selectedTable) {
        if (selectedTable != tableRecords) {
            tableRecords.getSelectionModel().clearSelection();
        }
        if (selectedTable != tableNotifications) {
            tableNotifications.getSelectionModel().clearSelection();
        }
        if (selectedTable != tableLogs) {
            tableLogs.getSelectionModel().clearSelection();
        }
    }

    private String prefixFor(String group) {
        String value = group == null ? "" : group.toLowerCase();
        if (value.contains("tintuc")) {
            return "TT";
        }
        if (value.contains("binhluan")) {
            return "BL";
        }
        if (value.contains("thongbao")) {
            return "TB";
        }
        if (value.contains("nhatky")) {
            return "NK";
        }
        if (value.contains("thamso")) {
            return "TS";
        }
        return "ND";
    }

    private String contentGroupCode(String label) {
        String value = label == null ? "" : label.toLowerCase();
        if (value.contains("tin")) {
            return "TinTuc";
        }
        if (value.contains("bình") || value.contains("binh")) {
            return "BinhLuan";
        }
        if (value.contains("thông") || value.contains("thong")) {
            return "ThongBao";
        }
        if (value.contains("nhật") || value.contains("nhat")) {
            return "NhatKyHeThong";
        }
        if (value.contains("tham")) {
            return "ThamSo";
        }
        return label;
    }

    private String codeOf(String option) {
        if (option == null) {
            return "";
        }
        int split = option.indexOf(" - ");
        return split >= 0 ? option.substring(0, split).trim() : option.trim();
    }

    private String value(TextField textField) {
        return textField == null || textField.getText() == null ? "" : textField.getText().trim();
    }

    private UserAccount currentUser() {
        UserAccount user = UserSession.getCurrentUser();
        return user == null
                ? new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Người quản lý hệ thống", "TK001")
                : user;
    }

    private void navigateQuietly(String viewName) {
        try {
            NavigationService.navigateTo(viewName);
        } catch (IOException ex) {
            DialogUtils.warning("Không mở được màn hình được chọn: " + ex.getMessage());
        }
    }

    private boolean isValidContact(String contact) {
        String value = contact.trim().toLowerCase();
        if (value.endsWith("@gmail.com") && value.indexOf('@') > 0) {
            return true;
        }
        return value.matches("0(9|8|7|3)[0-9]{8}");
    }

    private String normalized(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }
}
