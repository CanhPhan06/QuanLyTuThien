package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class VolunteerController {

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

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colTenChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNgayBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colTaskType.setCellValueFactory(new PropertyValueFactory<>("nhomBang"));
        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colTaskDate.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colNoticeTitle.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colNoticeDate.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colNoticeStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        tableCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTasks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableNotifications.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCampaigns.setItems(AppData.getActivities());
        tableTasks.setItems(volunteerTasks);
        tableNotifications.setItems(volunteerNotifications);

        refreshView();
    }

    @FXML
    private void handleCampaigns() throws IOException {
        App.setRoot("activities");
    }

    @FXML
    private void handleRegisterCampaign() {
        ActivityModel selected = tableCampaigns.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn đăng ký.");
            return;
        }

        boolean existed = AppData.getParticipants().stream()
                .anyMatch(item -> item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername())
                && item.getMaChienDich().equalsIgnoreCase(selected.getMaChienDich()));
        if (existed) {
            DialogUtils.warning("Bạn đã có hồ sơ tham gia chiến dịch này.");
            return;
        }

        AppData.getParticipants().add(new ParticipantModel(
                currentUser.getUsername(),
                currentUser.getLinkedId(),
                currentUser.getDisplayName(),
                "",
                "",
                "",
                "",
                selected.getMaChienDich(),
                "Chờ duyệt",
                ""
        ));
        AppData.getOperations().add(new SystemRecord("Đăng ký TNV", AppData.nextOperationId("VH"),
                selected.getMaChienDich(), currentUser.getUsername(), "Đăng ký tham gia chiến dịch",
                currentUser.getDisplayName() + " đăng ký tham gia " + selected.getTenChienDich(),
                AppData.todayText(), "", "Chờ duyệt", currentUser.getUsername(), "ADMIN", "Bảng ThamGiaTNV"));
        refreshView();
        DialogUtils.info("Đã gửi đăng ký tham gia chiến dịch.");
    }

    @FXML
    private void handleCheckIn() {
        ParticipantModel profile = findParticipant();
        if (profile == null) {
            DialogUtils.warning("Bạn chưa có chiến dịch được ghi nhận.");
            return;
        }

        AppData.getOperations().add(new SystemRecord("Điểm danh", AppData.nextOperationId("VH"),
                profile.getMaChienDich(), currentUser.getUsername(), "Tình nguyện viên tự điểm danh",
                "Ghi nhận điểm danh cho " + profile.getMaChienDich(), AppData.todayText(), "",
                "Chờ xác nhận", currentUser.getUsername(), "ADMIN", "Bảng DiemDanh"));
        refreshView();
        DialogUtils.info("Đã ghi nhận điểm danh, chờ quản lý xác nhận.");
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
        lblNotificationCount.setText(String.valueOf(volunteerNotifications.size()));
    }

    private ParticipantModel findParticipant() {
        return AppData.getParticipants().stream()
                .filter(item -> item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername()))
                .findFirst()
                .orElse(null);
    }
}
