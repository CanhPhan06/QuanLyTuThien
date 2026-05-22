package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseButton;

public class DonationsController {

    private static final String[] FORM_LABELS = {
        "Người đóng góp / công ty / tổ chức", "Đóng góp cho chiến dịch",
        "Ngày ghi nhận", "Hình thức đóng góp", "Mô tả khoản đóng góp", "Giá trị tiền (nếu có)"
    };

    @FXML
    private Label lblTotalDonation;
    @FXML
    private Label lblDonationCount;
    @FXML
    private Label lblMaterialDonation;
    @FXML
    private Label lblPendingDonation;
    @FXML
    private ComboBox<String> cboCampaignFilter;
    @FXML
    private ComboBox<String> cboTypeFilter;
    @FXML
    private TextField txtDonationSearch;

    @FXML
    private TableView<DonationModel> tableDonations;
    @FXML
    private TableColumn<DonationModel, String> colMaQuyenGop;
    @FXML
    private TableColumn<DonationModel, String> colNguoiQuyenGop;
    @FXML
    private TableColumn<DonationModel, String> colHoatDong;
    @FXML
    private TableColumn<DonationModel, String> colNgayQuyenGop;
    @FXML
    private TableColumn<DonationModel, String> colHinhThuc;
    @FXML
    private TableColumn<DonationModel, String> colNoiDungQuyenGop;
    @FXML
    private TableColumn<DonationModel, String> colSoTien;
    @FXML
    private TableColumn<DonationModel, String> colTrangThaiXuLy;

    private FilteredList<DonationModel> filteredDonations;

    @FXML
    private void initialize() {
        colMaQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaQuyenGop()));
        colMaQuyenGop.setVisible(false);
        colNguoiQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNguoiQuyenGop()));
        colHoatDong.setText("Chiến dịch");
        colHoatDong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colNgayQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayQuyenGop()));
        colHinhThuc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHinhThuc()));
        colNoiDungQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNoiDungQuyenGop()));
        colSoTien.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoTienText()));
        colTrangThaiXuLy.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThaiXuLy()));

        tableDonations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDonations.setFixedCellSize(32.0);
        filteredDonations = new FilteredList<>(AppData.getDonations(), item -> true);
        tableDonations.setItems(filteredDonations);
        cboCampaignFilter.setItems(buildCampaignFilterChoices());
        cboTypeFilter.setItems(buildTypeFilterChoices());
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        cboTypeFilter.setValue("Tất cả hình thức");
        cboCampaignFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyDonationFilters());
        cboTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyDonationFilters());
        txtDonationSearch.textProperty().addListener((observable, oldValue, newValue) -> applyDonationFilters());
        tableDonations.setRowFactory(table -> {
            TableRow<DonationModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableDonations.getSelectionModel().select(row.getItem());
                    showDonationDetail(row.getItem());
                }
            });
            return row;
        });
        updateDonationDashboard();
    }

    @FXML
    private void handleAddDonation() {
        DonationModel donation = showDonationDialog("Ghi nhận đóng góp", null);
        if (donation == null) {
            return;
        }
        if (existsById(donation.getMaQuyenGop(), null)) {
            DialogUtils.warning("Mã quyên góp đã tồn tại.");
            return;
        }

        String error = BusinessService.recordDonation(donation, currentUsername());
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }
        tableDonations.getSelectionModel().clearSelection();
        refreshDonationView();
        DialogUtils.info("Đã ghi nhận khoản đóng góp và chuyển sang Vận hành để xác nhận.");
    }

    @FXML
    private void handleUpdateDonation() {
        DonationModel selected = tableDonations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn khoản quyên góp cần cập nhật.");
            return;
        }

        DonationModel form = showDonationDialog("Cập nhật thông tin đóng góp", selected);
        if (form == null) {
            return;
        }
        if (existsById(form.getMaQuyenGop(), selected)) {
            DialogUtils.warning("Mã quyên góp đã tồn tại.");
            return;
        }
        String syncError = BusinessService.syncDonationOperation(form, currentUsername());
        if (syncError != null) {
            DialogUtils.warning(syncError);
            return;
        }

        selected.setMaQuyenGop(form.getMaQuyenGop());
        selected.setNguoiQuyenGop(form.getNguoiQuyenGop());
        selected.setHoatDong(form.getHoatDong());
        selected.setNgayQuyenGop(form.getNgayQuyenGop());
        selected.setHinhThuc(form.getHinhThuc());
        selected.setNoiDungQuyenGop(form.getNoiDungQuyenGop());
        selected.setSoTien(form.getSoTien());
        tableDonations.refresh();
        tableDonations.getSelectionModel().clearSelection();
        refreshDonationView();
        DialogUtils.info("Đã cập nhật thông tin đóng góp.");
    }

    @FXML
    private void handleDeleteDonation() {
        DonationModel selected = tableDonations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn khoản quyên góp cần xóa.");
            return;
        }
        String deleteError = BusinessRules.canDeleteDonation(selected);
        if (deleteError != null) {
            DialogUtils.warning(deleteError);
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa khoản quyên góp " + selected.getMaQuyenGop() + "?")) {
            return;
        }

        AppData.getDonations().remove(selected);
        tableDonations.getSelectionModel().clearSelection();
        refreshDonationView();
        DialogUtils.info("Đã xóa ghi nhận đóng góp.");
    }

    @FXML
    private void handleClearDonationFilters() {
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        cboTypeFilter.setValue("Tất cả hình thức");
        txtDonationSearch.clear();
        applyDonationFilters();
    }

    @FXML
    private void handleExportDonations() {
        ExportUtils.exportTableToCsv(tableDonations, "Xuất danh sách quyên góp", "danh-sach-quyen-gop.csv");
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

    private DonationModel showDonationDialog(String title, DonationModel current) {
        String[] values = current == null ? new String[]{"Công ty An Phát", defaultCampaignOption(), AppData.todayText(), "Tiền", "", "0"}
                : new String[]{
                    current.getNguoiQuyenGop(), current.getHoatDong() + " - " + campaignName(current.getHoatDong()),
                    current.getNgayQuyenGop(), current.getHinhThuc(), current.getNoiDungQuyenGop(),
                    current.getSoTien() == 0 ? "" : String.format("%.0f", current.getSoTien())
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildDonation(result, current);
    }

    private DonationModel buildDonation(String[] values, DonationModel current) {
        String maQuyenGop = current == null ? AppData.nextDonationId() : current.getMaQuyenGop();
        String maChienDich = codeOf(values[1]);
        if (values[0].isEmpty() || maChienDich.isEmpty()
                || values[2].isEmpty() || values[3].isEmpty() || values[4].isEmpty()) {
            DialogUtils.warning("Vui lòng nhập tên người/công ty/tổ chức, chọn chiến dịch, ngày ghi nhận, hình thức và mô tả khoản đóng góp.");
            return null;
        }
        try {
            double soTien = values[5].isEmpty() ? 0 : FormatUtils.parseMoney(values[5]);
            DonationModel donation = new DonationModel(maQuyenGop, values[0], maChienDich, values[2], values[3], values[4], soTien);
            String error = BusinessRules.validateDonation(donation);
            if (error != null) {
                DialogUtils.warning(error);
                return null;
            }
            return donation;
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Giá trị ước tính không hợp lệ.");
            return null;
        }
    }

    private boolean existsById(String id, DonationModel current) {
        return AppData.getDonations().stream()
                .anyMatch(item -> item != current && item.getMaQuyenGop().equalsIgnoreCase(id));
    }

    private void showDonationDetail(DonationModel donation) {
        ActivityModel campaign = AppData.findCampaign(donation.getHoatDong());
        DetailDialogUtils.showDetails(tableDonations, "Chi tiết đóng góp - " + donation.getNguoiQuyenGop(), new String[][]{
            {"Người đóng góp", donation.getNguoiQuyenGop()},
            {"Tên chiến dịch", campaign == null ? "" : campaign.getTenChienDich()},
            {"Ngày ghi nhận", donation.getNgayQuyenGop()},
            {"Hình thức", donation.getHinhThuc()},
            {"Mô tả khoản đóng góp", donation.getNoiDungQuyenGop()},
            {"Giá trị tiền", FormatUtils.money(donation.getSoTien())},
            {"Loại đóng góp", donation.getSoTien() > 0 ? "Đóng góp bằng tiền" : "Đóng góp vật tư/vật phẩm/vật dụng"},
            {"Trạng thái xử lý", donation.getTrangThaiXuLy()}
        });
    }

    private void refreshDonationView() {
        updateDonationDashboard();
        String currentType = cboTypeFilter.getValue();
        cboTypeFilter.setItems(buildTypeFilterChoices());
        if (cboTypeFilter.getItems().contains(currentType)) {
            cboTypeFilter.setValue(currentType);
        } else {
            cboTypeFilter.setValue("Tất cả hình thức");
        }
        applyDonationFilters();
        tableDonations.refresh();
    }

    private void updateDonationDashboard() {
        lblTotalDonation.setText(FormatUtils.money(AppData.getTotalDonationAmount()));
        lblDonationCount.setText(String.valueOf(AppData.getDonations().size()));
        long materialCount = AppData.getDonations().stream()
                .filter(item -> item.getSoTien() <= 0)
                .count();
        lblMaterialDonation.setText(String.valueOf(materialCount));
        long pendingCount = AppData.getOperations().stream()
                .filter(record -> "Quyên góp".equals(record.getNhomBang())
                && "Chờ xác nhận".equals(record.getTrangThai()))
                .count();
        lblPendingDonation.setText(String.valueOf(pendingCount));
    }

    private void applyDonationFilters() {
        if (filteredDonations == null) {
            return;
        }
        String campaignId = codeOf(cboCampaignFilter.getValue());
        String type = cboTypeFilter.getValue() == null ? "" : cboTypeFilter.getValue();
        String query = normalized(value(txtDonationSearch));
        boolean allCampaigns = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboCampaignFilter.getValue());
        boolean allTypes = type.isEmpty() || "Tất cả hình thức".equals(type);

        filteredDonations.setPredicate(donation
                -> (allCampaigns || donation.getHoatDong().equalsIgnoreCase(campaignId))
                && (allTypes || donation.getHinhThuc().equalsIgnoreCase(type))
                && (query.isEmpty() || normalized(donation.getNguoiQuyenGop() + " "
                + donation.getTenChienDich() + " " + donation.getHinhThuc() + " "
                + donation.getNoiDungQuyenGop() + " " + donation.getTrangThaiXuLy()).contains(query)));
    }

    private ObservableList<String> buildCampaignFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel activity : AppData.getActivities()) {
            choices.add(activity.getMaChienDich() + " - " + activity.getTenChienDich());
        }
        return choices;
    }

    private ObservableList<String> buildTypeFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả hình thức");
        AppData.getDonations().stream()
                .map(DonationModel::getHinhThuc)
                .distinct()
                .forEach(choices::add);
        return choices;
    }

    private String currentUsername() {
        UserAccount user = UserSession.getCurrentUser();
        return user == null ? "ADMIN001" : user.getUsername();
    }

    private String defaultCampaignOption() {
        return AppData.getActivities().stream()
                .findFirst()
                .map(activity -> activity.getMaChienDich() + " - " + activity.getTenChienDich())
                .orElse("");
    }

    private String campaignName(String campaignId) {
        ActivityModel campaign = AppData.findCampaign(campaignId);
        return campaign == null ? campaignId : campaign.getTenChienDich();
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

    private String normalized(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }
}
