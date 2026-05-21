package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ActivitiesController {

    private static final String[] FORM_LABELS = {
        "Tên chiến dịch", "Mô tả", "Địa điểm",
        "Ngày bắt đầu", "Ngày kết thúc", "Mục tiêu tiền", "Trạng thái chiến dịch"
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
    private ComboBox<String> cboActivityStatusFilter;
    @FXML
    private ComboBox<String> cboActivityLocationFilter;
    @FXML
    private TextField txtActivitySearch;

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
    private FilteredList<ActivityModel> filteredActivities;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            currentUser = new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Người quản lý hệ thống", "TK001");
        }

        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colMaChienDich.setVisible(false);
        colTenChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNgayBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colMucTieuTien.setCellValueFactory(new PropertyValueFactory<>("mucTieuTienText"));
        colDiaDiem.setCellValueFactory(new PropertyValueFactory<>("diaDiem"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        tableActivities.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredActivities = new FilteredList<>(AppData.getActivities(), item -> true);
        tableActivities.setItems(filteredActivities);
        setupActivityFilters();
        tableActivities.setRowFactory(table -> {
            TableRow<ActivityModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
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
        refreshActivityView();
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
        refreshActivityView();
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
        String deleteError = BusinessRules.canDeleteCampaign(selected);
        if (deleteError != null) {
            DialogUtils.warning(deleteError);
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa chiến dịch " + selected.getMaChienDich() + "?")) {
            return;
        }

        AppData.getActivities().remove(selected);
        tableActivities.getSelectionModel().clearSelection();
        closeCampaignDetailWindow();
        refreshActivityView();
        DialogUtils.info("Đã xóa chiến dịch.");
    }

    @FXML
    private void handleExportActivities() {
        ExportUtils.exportTableToCsv(tableActivities, "Xuất danh sách chiến dịch", "danh-sach-chien-dich.csv");
    }

    @FXML
    private void handleClearForm() {
        tableActivities.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleClearActivityFilters() {
        cboActivityStatusFilter.setValue("Tất cả trạng thái");
        cboActivityLocationFilter.setValue("Tất cả địa điểm");
        txtActivitySearch.clear();
        applyActivityFilters();
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
        String error = BusinessService.registerVolunteer(currentUser, selected);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
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

        String error = BusinessService.followCampaign(currentUser, selected);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
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

        ParticipantModel profile = AppData.getParticipants().stream()
                .filter(item -> item.getMaTaiKhoan().equalsIgnoreCase(currentUser.getUsername())
                && item.getMaChienDich().equalsIgnoreCase(selected.getMaChienDich()))
                .findFirst()
                .orElse(null);
        String error = BusinessService.submitProof(currentUser, profile, "Ghi chú sau hoạt động",
                "Minh chứng gửi từ màn hình chi tiết chiến dịch " + selected.getTenChienDich());
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
        DialogUtils.info("Đã gửi minh chứng, chờ quản lý xác nhận.");
    }

    @FXML
    private void handleSponsorCampaign() {
        ActivityModel selected = selectedCampaign();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn tài trợ.");
            return;
        }

        String error = BusinessService.recordDonation(currentUser, selected.getMaChienDich(),
                "Tài trợ vật phẩm", "Đề xuất tài trợ cho " + selected.getTenChienDich(), 0);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
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

        String error = BusinessService.recordDonation(currentUser, selected.getMaChienDich(),
                "Vật phẩm", "Đề xuất quyên góp vật phẩm cho " + selected.getTenChienDich(), 0);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
        DialogUtils.info("Đã ghi nhận đề xuất quyên góp vật phẩm, chờ quản lý xác nhận.");
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

    private void setupActivityFilters() {
        cboActivityStatusFilter.setItems(buildStatusFilterChoices());
        cboActivityLocationFilter.setItems(buildLocationFilterChoices());
        cboActivityStatusFilter.setValue("Tất cả trạng thái");
        cboActivityLocationFilter.setValue("Tất cả địa điểm");
        cboActivityStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyActivityFilters());
        cboActivityLocationFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyActivityFilters());
        txtActivitySearch.textProperty().addListener((observable, oldValue, newValue) -> applyActivityFilters());
    }

    private void refreshActivityView() {
        String currentStatus = cboActivityStatusFilter.getValue();
        String currentLocation = cboActivityLocationFilter.getValue();
        cboActivityStatusFilter.setItems(buildStatusFilterChoices());
        cboActivityLocationFilter.setItems(buildLocationFilterChoices());
        cboActivityStatusFilter.setValue(cboActivityStatusFilter.getItems().contains(currentStatus) ? currentStatus : "Tất cả trạng thái");
        cboActivityLocationFilter.setValue(cboActivityLocationFilter.getItems().contains(currentLocation) ? currentLocation : "Tất cả địa điểm");
        applyActivityFilters();
        tableActivities.refresh();
    }

    private void applyActivityFilters() {
        if (filteredActivities == null) {
            return;
        }
        String status = cboActivityStatusFilter.getValue() == null ? "" : cboActivityStatusFilter.getValue();
        String location = cboActivityLocationFilter.getValue() == null ? "" : cboActivityLocationFilter.getValue();
        String query = normalize(value(txtActivitySearch));
        boolean allStatuses = status.isEmpty() || "Tất cả trạng thái".equals(status);
        boolean allLocations = location.isEmpty() || "Tất cả địa điểm".equals(location);
        filteredActivities.setPredicate(activity
                -> (allStatuses || safe(activity.getTrangThai()).equalsIgnoreCase(status))
                && (allLocations || safe(activity.getDiaDiem()).equalsIgnoreCase(location))
                && (query.isEmpty() || normalize(safe(activity.getTenChienDich()) + " "
                + safe(activity.getMoTa()) + " " + safe(activity.getDiaDiem()) + " "
                + safe(activity.getTrangThai())).contains(query)));
    }

    private ObservableList<String> buildStatusFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trạng thái");
        AppData.getActivities().stream()
                .map(ActivityModel::getTrangThai)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .forEach(choices::add);
        return choices;
    }

    private ObservableList<String> buildLocationFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả địa điểm");
        AppData.getActivities().stream()
                .map(ActivityModel::getDiaDiem)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .forEach(choices::add);
        return choices;
    }

    private ActivityModel showActivityDialog(String title, ActivityModel current) {
        String[] values = current == null ? new String[]{"", "", "", AppData.todayText(), "", "0", "Đang xét"}
                : new String[]{
                    current.getTenChienDich(), current.getMoTa(), current.getDiaDiem(),
                    current.getNgayBatDau(), current.getNgayKetThuc(),
                    current.getMucTieuTien() == 0 ? "" : String.format("%.0f", current.getMucTieuTien()),
                    current.getTrangThai()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildActivity(result, current);
    }

    private ActivityModel buildActivity(String[] values, ActivityModel current) {
        String ma = current == null ? AppData.nextCampaignId() : current.getMaChienDich();
        String ten = values[0];
        String moTa = values[1];
        String diaDiem = values[2];
        String ngayBatDau = values[3];
        String ngayKetThuc = values[4];
        String mucTieuTienText = values[5];
        String trangThai = values[6];
        String maNguoiTao = current == null ? currentUser.getUsername() : current.getMaNguoiTao();

        if (ten.isEmpty() || ngayBatDau.isEmpty() || trangThai.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập tên, ngày bắt đầu và trạng thái chiến dịch.");
            return null;
        }

        try {
            double mucTieuTien = mucTieuTienText.isEmpty() ? 0 : FormatUtils.parseMoney(mucTieuTienText);
            ActivityModel activity = new ActivityModel(ma, ten, moTa, diaDiem, ngayBatDau, ngayKetThuc, mucTieuTien, trangThai, maNguoiTao);
            String error = BusinessRules.validateCampaign(activity);
            if (error != null) {
                DialogUtils.warning(error);
                return null;
            }
            return activity;
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
        DetailDialogUtils.showDetails(tableActivities, "Chi tiết chiến dịch - " + selected.getTenChienDich(), new String[][]{
            {"Tên chiến dịch", selected.getTenChienDich()},
            {"Mô tả", selected.getMoTa()},
            {"Thời gian", selected.getNgayBatDau() + " - " + selected.getNgayKetThuc()},
            {"Địa điểm", selected.getDiaDiem()},
            {"Mục tiêu quyên góp", FormatUtils.money(selected.getMucTieuTien())},
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

    private String value(TextField textField) {
        return textField == null || textField.getText() == null ? "" : textField.getText().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }

    @FunctionalInterface
    private interface NavigationAction {

        void run() throws IOException;
    }
}
