package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class VolunteerController {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblCampaignCount;
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
    private ComboBox<String> cboCampaign;
    @FXML
    private ComboBox<String> cboProofType;
    @FXML
    private TextField txtProofNote;
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
    private TableView<ParticipantModel> tableMyCampaigns;
    @FXML
    private TableColumn<ParticipantModel, String> colMyCampaign;
    @FXML
    private TableColumn<ParticipantModel, String> colMyCampaignStatus;
    @FXML
    private TableColumn<ParticipantModel, String> colMyCampaignScore;

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

    private final ObservableList<ParticipantModel> myCampaigns = FXCollections.observableArrayList();
    private final ObservableList<SystemRecord> volunteerTasks = FXCollections.observableArrayList();
    private final ObservableList<SystemRecord> volunteerNotifications = FXCollections.observableArrayList();
    private UserAccount currentUser;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        colMaChienDich.setVisible(false);
        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colTenChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNgayBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colMyCampaign.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colMyCampaignStatus.setCellValueFactory(new PropertyValueFactory<>("trangThaiDuyet"));
        colMyCampaignScore.setCellValueFactory(new PropertyValueFactory<>("diemDanhGia"));

        colTaskType.setCellValueFactory(new PropertyValueFactory<>("nhomBang"));
        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colTaskDate.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colNoticeTitle.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colNoticeDate.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colNoticeStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        tableCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableMyCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTasks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableNotifications.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCampaigns.setItems(AppData.getActivities());
        tableMyCampaigns.setItems(myCampaigns);
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

        if (!cboCampaign.getItems().isEmpty()) {
            cboCampaign.setValue(cboCampaign.getItems().get(0));
            updateSelectedCampaign(AppData.findCampaign(extractCampaignId(cboCampaign.getValue())));
        }
        refreshView();
        showSection(overviewSection);
    }

    @FXML
    private void handleShowOverview() {
        showSection(overviewSection);
    }

    @FXML
    private void handleShowCampaigns() {
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
        ActivityModel selected = AppData.findCampaign(extractCampaignId(cboCampaign.getValue()));
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
        DialogUtils.info("Đã gửi đăng ký tham gia chiến dịch.");
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
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.clear();
        App.setRoot("primary");
    }

    private void refreshView() {
        lblWelcome.setText("Xin chào, " + currentUser.getDisplayName());
        ObservableList<ParticipantModel> profiles = findMyParticipants();
        myCampaigns.setAll(profiles);
        lblCampaignCount.setText(String.valueOf(profiles.size()));

        ParticipantModel latestProfile = profiles.isEmpty() ? null : profiles.get(0);
        lblStatus.setText(latestProfile == null ? "Chưa đăng ký" : latestProfile.getTrangThaiDuyet());
        lblScore.setText(latestProfile == null || latestProfile.getDiemDanhGia().isEmpty() ? "Chưa có" : latestProfile.getDiemDanhGia());

        String campaignId = latestProfile == null ? "" : latestProfile.getMaChienDich();
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
            return;
        }
        lblSelectedCampaign.setText(campaign.getTenChienDich());
        lblSelectedTime.setText(campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc());
        lblSelectedPlace.setText(campaign.getDiaDiem());
    }

    private void showCampaignDetail(ActivityModel campaign) {
        DetailDialogUtils.showDetails(tableCampaigns, "Chi tiết chiến dịch - " + campaign.getMaChienDich(), new String[][]{
            {"Tên chiến dịch", campaign.getTenChienDich()},
            {"Mô tả", campaign.getMoTa()},
            {"Địa điểm", campaign.getDiaDiem()},
            {"Thời gian", campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc()},
            {"Mục tiêu", FormatUtils.money(campaign.getMucTieuTien())},
            {"Đã ghi nhận", FormatUtils.money(AppData.getCampaignMoneyTotal(campaign.getMaChienDich()))},
            {"Số TNV tham gia", String.valueOf(AppData.getCampaignParticipantCount(campaign.getMaChienDich()))},
            {"Trạng thái", campaign.getTrangThai()}
        });
    }

    private ObservableList<ParticipantModel> findMyParticipants() {
        return AppData.getParticipants().filtered(item ->
                item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername()));
    }

    private ParticipantModel findParticipant() {
        ObservableList<ParticipantModel> list = findMyParticipants();
        return list.isEmpty() ? null : list.get(0);
    }
}
