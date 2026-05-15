package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ActivitiesController {

    private static final String[] FORM_LABELS = {
        "Mã chiến dịch", "Tên chiến dịch", "Mô tả", "Địa điểm",
        "Ngày bắt đầu", "Ngày kết thúc", "Mục tiêu tiền", "Trạng thái", "Mã người tạo"
    };

    @FXML
    private Button btnParticipantsMenu;
    @FXML
    private Button btnSponsorsMenu;
    @FXML
    private Button btnDonationsMenu;
    @FXML
    private Button btnOperationsMenu;
    @FXML
    private Button btnContentMenu;
    @FXML
    private Button btnReportsMenu;

    @FXML
    private HBox adminActionBar;

    @FXML
    private TableView<ActivityModel> tableActivities;
    @FXML
    private TableColumn<ActivityModel, String> colMaChienDich;
    @FXML
    private TableColumn<ActivityModel, String> colTenChienDich;
    @FXML
    private TableColumn<ActivityModel, String> colNgayBatDau;
    @FXML
    private TableColumn<ActivityModel, String> colNgayKetThuc;
    @FXML
    private TableColumn<ActivityModel, String> colMucTieuTien;
    @FXML
    private TableColumn<ActivityModel, String> colDiaDiem;
    @FXML
    private TableColumn<ActivityModel, String> colTrangThai;

    private UserAccount currentUser;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            currentUser = new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Người quản lý hệ thống", "TK001");
        }

        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colTenChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNgayBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colMucTieuTien.setCellValueFactory(new PropertyValueFactory<>("mucTieuTienText"));
        colDiaDiem.setCellValueFactory(new PropertyValueFactory<>("diaDiem"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        tableActivities.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableActivities.setItems(AppData.getActivities());
        tableActivities.setRowFactory(table -> {
            TableRow<ActivityModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    tableActivities.getSelectionModel().select(row.getItem());
                    openCampaignDetailWindow(row.getItem());
                }
            });
            return row;
        });

        configureRole();
    }

    @FXML
    private void handleAddActivity() {
        if (!currentUser.isAdmin()) {
            DialogUtils.warning("Bạn không có quyền thêm chiến dịch.");
            return;
        }
        ActivityModel activity = showActivityDialog("Thêm chiến dịch", null);
        if (activity == null) {
            return;
        }
        if (existsById(activity.getMaChienDich(), null)) {
            DialogUtils.warning("Mã chiến dịch đã tồn tại.");
            return;
        }

        AppData.getActivities().add(activity);
        tableActivities.getSelectionModel().clearSelection();
        DialogUtils.info("Đã thêm chiến dịch mới.");
    }

    @FXML
    private void handleUpdateActivity() {
        if (!currentUser.isAdmin()) {
            DialogUtils.warning("Bạn không có quyền sửa chiến dịch.");
            return;
        }
        ActivityModel selected = tableActivities.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần sửa.");
            return;
        }

        ActivityModel form = showActivityDialog("Cập nhật chiến dịch", selected);
        if (form == null) {
            return;
        }
        if (existsById(form.getMaChienDich(), selected)) {
            DialogUtils.warning("Mã chiến dịch đã tồn tại.");
            return;
        }

        selected.setMaChienDich(form.getMaChienDich());
        selected.setTenChienDich(form.getTenChienDich());
        selected.setMoTa(form.getMoTa());
        selected.setDiaDiem(form.getDiaDiem());
        selected.setNgayBatDau(form.getNgayBatDau());
        selected.setNgayKetThuc(form.getNgayKetThuc());
        selected.setMucTieuTien(form.getMucTieuTien());
        selected.setTrangThai(form.getTrangThai());
        selected.setMaNguoiTao(form.getMaNguoiTao());
        tableActivities.refresh();
        closeCampaignDetailWindow();
        tableActivities.getSelectionModel().clearSelection();
        DialogUtils.info("Đã cập nhật chiến dịch.");
    }

    @FXML
    private void handleDeleteActivity() {
        if (!currentUser.isAdmin()) {
            DialogUtils.warning("Bạn không có quyền xóa chiến dịch.");
            return;
        }
        ActivityModel selected = tableActivities.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần xóa.");
            return;
        }

        AppData.getActivities().remove(selected);
        tableActivities.getSelectionModel().clearSelection();
        closeCampaignDetailWindow();
        DialogUtils.info("Đã xóa chiến dịch.");
    }

    @FXML
    private void handleClearForm() {
        tableActivities.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleOpenSelectedCampaignDetail() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn một chiến dịch cần xem chi tiết.");
            return;
        }
        openCampaignDetailWindow(selected);
    }

    @FXML
    private void handleVolunteerRegister() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn đăng ký.");
            return;
        }
        if (hasJoinedCampaign(selected.getMaChienDich())) {
            DialogUtils.info("Bạn đã có hồ sơ tham gia chiến dịch này.");
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
        DialogUtils.info("Đã gửi đăng ký tham gia chiến dịch.");
        openCampaignDetailWindow(selected);
    }

    @FXML
    private void handleFollowCampaign() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn theo dõi.");
            return;
        }

        AppData.getContents().add(new SystemRecord("TheoDoi", "TD" + currentUser.getUsername() + selected.getMaChienDich(),
                selected.getMaChienDich(), "Theo dõi chiến dịch",
                currentUser.getDisplayName() + " theo dõi " + selected.getTenChienDich(),
                AppData.todayText(), "Đang theo dõi", "Bảng TheoDoi"));
        DialogUtils.info("Đã theo dõi chiến dịch.");
    }

    @FXML
    private void handleViewMyTasks() throws IOException {
        closeCampaignDetailWindow();
        App.setRoot("volunteer");
    }

    @FXML
    private void handleSubmitProof() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch trước khi gửi minh chứng.");
            return;
        }

        String proofId = "MC" + String.format("%03d", AppData.getOperations().size() + 1);
        AppData.getOperations().add(new SystemRecord("Minh chứng TNV", AppData.nextOperationId("VH"),
                selected.getMaChienDich(), proofId, "Gửi minh chứng TNV",
                currentUser.getDisplayName() + " gửi minh chứng cho " + selected.getTenChienDich(),
                AppData.todayText(), "", "Chờ xác nhận", currentUser.getUsername(), "ADMIN", "Bảng MinhChungTNV"));
        DialogUtils.info("Đã gửi minh chứng, chờ quản lý xác nhận.");
    }

    @FXML
    private void handleSponsorCampaign() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn tài trợ.");
            return;
        }

        AppData.getDonations().add(new DonationModel(AppData.nextDonationId(), userGmail(),
                selected.getMaChienDich(), AppData.todayText(), "Tài trợ chiến dịch",
                "Đề xuất tài trợ cho " + selected.getTenChienDich(), 0));
        DialogUtils.info("Đã ghi nhận đề xuất tài trợ. Bạn có thể nhập chi tiết ở cổng nhà tài trợ.");
        openCampaignDetailWindow(selected);
    }

    @FXML
    private void handleDonateMoney() throws IOException {
        closeCampaignDetailWindow();
        App.setRoot("sponsorportal");
    }

    @FXML
    private void handleDonateItems() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn quyên góp vật phẩm.");
            return;
        }

        AppData.getDonations().add(new DonationModel(AppData.nextDonationId(), userGmail(),
                selected.getMaChienDich(), AppData.todayText(), "Vật phẩm",
                "Đề xuất quyên góp vật phẩm cho " + selected.getTenChienDich(), 0));
        DialogUtils.info("Đã ghi nhận đề xuất quyên góp vật phẩm.");
        openCampaignDetailWindow(selected);
    }

    @FXML
    private void handleViewSponsorHistory() throws IOException {
        closeCampaignDetailWindow();
        App.setRoot("sponsorportal");
    }

    @FXML
    private void handleAssignWork() throws IOException {
        App.setRoot("operations");
    }

    @FXML
    private void handleViewParticipants() throws IOException {
        App.setRoot("participants");
    }

    @FXML
    private void handleViewDonations() throws IOException {
        App.setRoot("donations");
    }

    @FXML
    private void handleViewExpenses() throws IOException {
        App.setRoot("operations");
    }

    @FXML
    private void handleBackHome() throws IOException {
        closeCampaignDetailWindow();
        if (currentUser.isVolunteer()) {
            App.setRoot("volunteer");
        } else if (currentUser.isSponsor()) {
            App.setRoot("sponsorportal");
        } else {
            App.setRoot("secondary");
        }
    }

    @FXML
    private void handleActivities() throws IOException {
        App.setRoot("activities");
    }

    @FXML
    private void handleParticipants() throws IOException {
        App.setRoot("participants");
    }

    @FXML
    private void handleSponsors() throws IOException {
        App.setRoot("sponsors");
    }

    @FXML
    private void handleDonations() throws IOException {
        App.setRoot("donations");
    }

    @FXML
    private void handleOperations() throws IOException {
        App.setRoot("operations");
    }

    @FXML
    private void handleContent() throws IOException {
        App.setRoot("content");
    }

    @FXML
    private void handleReports() throws IOException {
        App.setRoot("reports");
    }

    @FXML
    private void handleLogout() throws IOException {
        closeCampaignDetailWindow();
        UserSession.clear();
        App.setRoot("primary");
    }

    private void configureRole() {
        boolean isAdmin = currentUser.isAdmin();

        setVisibleManaged(adminActionBar, isAdmin);

        setVisibleManaged(btnParticipantsMenu, isAdmin);
        setVisibleManaged(btnSponsorsMenu, isAdmin);
        setVisibleManaged(btnDonationsMenu, isAdmin);
        setVisibleManaged(btnOperationsMenu, isAdmin);
        setVisibleManaged(btnContentMenu, isAdmin);
        setVisibleManaged(btnReportsMenu, isAdmin);

    }

    private ActivityModel showActivityDialog(String title, ActivityModel current) {
        String[] values = current == null ? new String[]{"", "", "", "", AppData.todayText(), "", "0", "Đang xét", currentUser.getUsername()}
                : new String[]{
                    current.getMaChienDich(), current.getTenChienDich(), current.getMoTa(), current.getDiaDiem(),
                    current.getNgayBatDau(), current.getNgayKetThuc(),
                    current.getMucTieuTien() == 0 ? "" : String.format("%.0f", current.getMucTieuTien()),
                    current.getTrangThai(), current.getMaNguoiTao()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildActivity(result);
    }

    private ActivityModel buildActivity(String[] values) {
        String ma = values[0];
        String ten = values[1];
        String moTa = values[2];
        String diaDiem = values[3];
        String ngayBatDau = values[4];
        String ngayKetThuc = values[5];
        String mucTieuTienText = values[6];
        String trangThai = values[7];
        String maNguoiTao = values[8].isEmpty() ? currentUser.getUsername() : values[8];

        if (ma.isEmpty() || ten.isEmpty() || ngayBatDau.isEmpty() || trangThai.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập mã, tên, ngày bắt đầu và trạng thái chiến dịch.");
            return null;
        }

        try {
            double mucTieuTien = mucTieuTienText.isEmpty() ? 0 : FormatUtils.parseMoney(mucTieuTienText);
            return new ActivityModel(ma, ten, moTa, diaDiem, ngayBatDau, ngayKetThuc, mucTieuTien, trangThai, maNguoiTao);
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Mục tiêu tiền không hợp lệ.");
            return null;
        }
    }

    private void openCampaignDetailWindow(ActivityModel selected) {
        if (selected == null) {
            return;
        }

        closeCampaignDetailWindow();

        String campaignId = selected.getMaChienDich();
        DetailDialogUtils.showDetails(tableActivities, "Chi tiết chiến dịch - " + campaignId, new String[][]{
            {"Tên chiến dịch", selected.getTenChienDich()},
            {"Mã chiến dịch", campaignId},
            {"Mô tả", selected.getMoTa()},
            {"Thời gian", selected.getNgayBatDau() + " - " + selected.getNgayKetThuc()},
            {"Địa điểm", selected.getDiaDiem()},
            {"Mục tiêu quyên góp", FormatUtils.money(selected.getMucTieuTien())},
            {"Người tạo", selected.getMaNguoiTao()},
            {"Đã quyên góp / tài trợ", FormatUtils.money(AppData.getCampaignMoneyTotal(campaignId))},
            {"Số TNV tham gia", String.valueOf(AppData.getCampaignParticipantCount(campaignId))},
            {"Tin tức liên quan", AppData.getCampaignNewsSummary(campaignId)}
        }, buildDetailActions(selected));
    }

    private void addDetailRow(GridPane grid, int row, String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("muted-text");

        Label valueLabel = new Label(emptyText(value));
        valueLabel.getStyleClass().add("dashboard-main-text");
        valueLabel.setWrapText(true);

        grid.add(titleLabel, 0, row);
        grid.add(valueLabel, 1, row);
        GridPane.setHgrow(valueLabel, Priority.ALWAYS);
    }

    private HBox buildDetailActions(ActivityModel selected) {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (currentUser.isVolunteer()) {
            Button registerButton = new Button(hasJoinedCampaign(selected.getMaChienDich())
                    ? joinedStatus(selected.getMaChienDich()) : "Đăng ký tham gia");
            registerButton.getStyleClass().add("primary-button");
            registerButton.setDisable(hasJoinedCampaign(selected.getMaChienDich()));
            registerButton.setOnAction(event -> handleVolunteerRegister());

            Button followButton = new Button("Theo dõi");
            followButton.getStyleClass().add("quick-button");
            followButton.setOnAction(event -> handleFollowCampaign());

            Button taskButton = new Button("Công việc của tôi");
            taskButton.getStyleClass().add("quick-button");
            taskButton.setOnAction(event -> runNavigation(() -> handleViewMyTasks()));

            Button proofButton = new Button("Gửi minh chứng");
            proofButton.getStyleClass().add("quick-button");
            proofButton.setOnAction(event -> handleSubmitProof());

            actions.getChildren().addAll(registerButton, followButton, taskButton, proofButton);
        } else if (currentUser.isSponsor()) {
            Button sponsorButton = new Button("Tài trợ chiến dịch");
            sponsorButton.getStyleClass().add("primary-button");
            sponsorButton.setOnAction(event -> handleSponsorCampaign());

            Button moneyButton = new Button("Quyên góp tiền");
            moneyButton.getStyleClass().add("quick-button");
            moneyButton.setOnAction(event -> runNavigation(() -> handleDonateMoney()));

            Button itemButton = new Button("Quyên góp vật phẩm");
            itemButton.getStyleClass().add("quick-button");
            itemButton.setOnAction(event -> handleDonateItems());

            Button historyButton = new Button("Lịch sử tài trợ");
            historyButton.getStyleClass().add("quick-button");
            historyButton.setOnAction(event -> runNavigation(() -> handleViewSponsorHistory()));

            actions.getChildren().addAll(sponsorButton, moneyButton, itemButton, historyButton);
        }

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().add("quick-button");
        closeButton.setOnAction(event -> closeCampaignDetailWindow());
        actions.getChildren().add(closeButton);
        return actions;
    }

    private void closeCampaignDetailWindow() {
        DetailDialogUtils.closeActiveOverlay();
    }

    private void runNavigation(NavigationAction action) {
        try {
            action.run();
        } catch (IOException ex) {
            DialogUtils.warning("Không mở được màn hình yêu cầu.");
        }
    }

    private boolean hasJoinedCampaign(String campaignId) {
        return AppData.getParticipants().stream()
                .anyMatch(item -> item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername())
                && item.getMaChienDich().equalsIgnoreCase(campaignId));
    }

    private String joinedStatus(String campaignId) {
        return AppData.getParticipants().stream()
                .filter(item -> item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername())
                && item.getMaChienDich().equalsIgnoreCase(campaignId))
                .map(item -> "Đã đăng ký - " + item.getTrangThaiDuyet())
                .findFirst()
                .orElse("Đã đăng ký");
    }

    private ActivityModel selectedCampaign() {
        return tableActivities.getSelectionModel().getSelectedItem();
    }

    private boolean existsById(String id, ActivityModel current) {
        return AppData.getActivities().stream()
                .anyMatch(item -> item != current && item.getMaChienDich().equalsIgnoreCase(id));
    }

    private void setVisibleManaged(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private String emptyText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String userGmail() {
        return currentUser.getUsername().toLowerCase() + "@gmail.com";
    }

    @FunctionalInterface
    private interface NavigationAction {

        void run() throws IOException;
    }
}
