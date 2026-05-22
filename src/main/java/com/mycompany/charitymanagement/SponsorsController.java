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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class SponsorsController {

    private static final String[] FORM_LABELS = {
        "Tên đối tác / nhà tài trợ", "Lĩnh vực", "Số điện thoại", "Email",
        "Địa chỉ", "Chiến dịch tài trợ", "Giá trị tài trợ", "Ngày ký kết"
    };

    @FXML
    private Label lblTotalSponsor;
    @FXML
    private Label lblSponsorCount;
    @FXML
    private Label lblSponsorCampaignCount;
    @FXML
    private ComboBox<String> cboSponsorCampaignFilter;
    @FXML
    private ComboBox<String> cboSponsorFieldFilter;
    @FXML
    private TextField txtSponsorSearch;

    @FXML
    private TableView<SponsorModel> tableSponsors;
    @FXML
    private TableColumn<SponsorModel, String> colMaDoiTac;
    @FXML
    private TableColumn<SponsorModel, String> colTenDoiTac;
    @FXML
    private TableColumn<SponsorModel, String> colLinhVuc;
    @FXML
    private TableColumn<SponsorModel, String> colSoDienThoai;
    @FXML
    private TableColumn<SponsorModel, String> colEmail;
    @FXML
    private TableColumn<SponsorModel, String> colDiaChi;
    @FXML
    private TableColumn<SponsorModel, String> colMaChienDich;
    @FXML
    private TableColumn<SponsorModel, String> colGiaTriTaiTro;
    @FXML
    private TableColumn<SponsorModel, String> colNgayKyKet;

    private FilteredList<SponsorModel> filteredSponsors;

    @FXML
    private void initialize() {
        colMaDoiTac.setCellValueFactory(new PropertyValueFactory<>("maDoiTac"));
        colMaDoiTac.setVisible(false);
        colTenDoiTac.setCellValueFactory(new PropertyValueFactory<>("tenDoiTac"));
        colLinhVuc.setCellValueFactory(new PropertyValueFactory<>("linhVuc"));
        colSoDienThoai.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colMaChienDich.setText("Chiến dịch");
        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colGiaTriTaiTro.setCellValueFactory(new PropertyValueFactory<>("giaTriTaiTroText"));
        colNgayKyKet.setCellValueFactory(new PropertyValueFactory<>("ngayKyKet"));

        tableSponsors.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSponsors.setFixedCellSize(32.0);
        filteredSponsors = new FilteredList<>(AppData.getSponsors(), item -> true);
        tableSponsors.setItems(filteredSponsors);
        setupSponsorFilters();
        tableSponsors.setRowFactory(table -> {
            TableRow<SponsorModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableSponsors.getSelectionModel().select(row.getItem());
                    showSponsorDetail(row.getItem());
                }
            });
            return row;
        });
        updateSponsorDashboard();
    }

    @FXML
    private void handleAddSponsor() {
        SponsorModel sponsor = showSponsorDialog("Thêm đối tác / tài trợ", null);
        if (sponsor == null) {
            return;
        }
        if (existsById(sponsor.getMaDoiTac(), null)) {
            DialogUtils.warning("Mã đối tác đã tồn tại.");
            return;
        }

        AppData.getSponsors().add(sponsor);
        tableSponsors.getSelectionModel().clearSelection();
        refreshSponsorView();
        DialogUtils.info("Đã thêm đối tác/tài trợ.");
    }

    @FXML
    private void handleUpdateSponsor() {
        SponsorModel selected = tableSponsors.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn đối tác/tài trợ cần cập nhật.");
            return;
        }

        SponsorModel form = showSponsorDialog("Cập nhật đối tác / tài trợ", selected);
        if (form == null) {
            return;
        }
        if (existsById(form.getMaDoiTac(), selected)) {
            DialogUtils.warning("Mã đối tác đã tồn tại.");
            return;
        }

        selected.setMaDoiTac(form.getMaDoiTac());
        selected.setTenDoiTac(form.getTenDoiTac());
        selected.setLinhVuc(form.getLinhVuc());
        selected.setSoDienThoai(form.getSoDienThoai());
        selected.setEmail(form.getEmail());
        selected.setDiaChi(form.getDiaChi());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setGiaTriTaiTro(form.getGiaTriTaiTro());
        selected.setNgayKyKet(form.getNgayKyKet());
        tableSponsors.refresh();
        tableSponsors.getSelectionModel().clearSelection();
        refreshSponsorView();
        DialogUtils.info("Đã cập nhật đối tác/tài trợ.");
    }

    @FXML
    private void handleDeleteSponsor() {
        SponsorModel selected = tableSponsors.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn đối tác/tài trợ cần xóa.");
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa đối tác/tài trợ " + selected.getMaDoiTac() + "?")) {
            return;
        }

        AppData.getSponsors().remove(selected);
        tableSponsors.getSelectionModel().clearSelection();
        refreshSponsorView();
        DialogUtils.info("Đã xóa đối tác/tài trợ.");
    }

    @FXML
    private void handleClearSponsorFilters() {
        cboSponsorCampaignFilter.setValue("Tất cả chiến dịch");
        cboSponsorFieldFilter.setValue("Tất cả lĩnh vực");
        txtSponsorSearch.clear();
        applySponsorFilters();
    }

    @FXML
    private void handleExportSponsors() {
        ExportUtils.exportTableToCsv(tableSponsors, "Xuất danh sách nhà tài trợ", "danh-sach-nha-tai-tro.csv");
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

    private SponsorModel showSponsorDialog(String title, SponsorModel current) {
        String[] values = current == null ? new String[]{"", "", "", "", "", defaultCampaignOption(), "0", AppData.todayText()}
                : new String[]{
                    current.getTenDoiTac(), current.getLinhVuc(), current.getSoDienThoai(),
                    current.getEmail(), current.getDiaChi(),
                    current.getMaChienDich() + " - " + campaignName(current.getMaChienDich()),
                    current.getGiaTriTaiTro() == 0 ? "" : String.format("%.0f", current.getGiaTriTaiTro()),
                    current.getNgayKyKet()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildSponsor(result, current);
    }

    private SponsorModel buildSponsor(String[] values, SponsorModel current) {
        String maDoiTac = current == null ? AppData.nextSponsorId() : current.getMaDoiTac();
        String tenDoiTac = values[0];
        String maChienDich = codeOf(values[5]);

        if (maDoiTac.isEmpty() || tenDoiTac.isEmpty() || maChienDich.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập tên đối tác và chọn chiến dịch tài trợ.");
            return null;
        }
        try {
            double giaTriTaiTro = values[6].isEmpty() ? 0 : FormatUtils.parseMoney(values[6]);
            SponsorModel sponsor = new SponsorModel(maDoiTac, tenDoiTac, values[1], values[2], values[3],
                    values[4], maChienDich, giaTriTaiTro, values[7]);
            String error = BusinessRules.validateSponsor(sponsor);
            if (error != null) {
                DialogUtils.warning(error);
                return null;
            }
            return sponsor;
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Giá trị tài trợ không hợp lệ.");
            return null;
        }
    }

    private boolean existsById(String id, SponsorModel current) {
        return AppData.getSponsors().stream()
                .anyMatch(item -> item != current && item.getMaDoiTac().equalsIgnoreCase(id));
    }

    private void showSponsorDetail(SponsorModel sponsor) {
        ActivityModel campaign = AppData.findCampaign(sponsor.getMaChienDich());
        DetailDialogUtils.showDetails(tableSponsors, "Chi tiết nhà tài trợ - " + sponsor.getTenDoiTac(), new String[][]{
            {"Tên đối tác / nhà tài trợ", sponsor.getTenDoiTac()},
            {"Lĩnh vực", sponsor.getLinhVuc()},
            {"Số điện thoại", sponsor.getSoDienThoai()},
            {"Email", sponsor.getEmail()},
            {"Địa chỉ", sponsor.getDiaChi()},
            {"Tên chiến dịch", campaign == null ? "" : campaign.getTenChienDich()},
            {"Giá trị tài trợ", FormatUtils.money(sponsor.getGiaTriTaiTro())},
            {"Ngày ký kết", sponsor.getNgayKyKet()},
            {"Vai trò", "Đối tác / Nhà tài trợ đồng hành cùng chiến dịch"},
            {"Trạng thái hồ sơ", "Đã ghi nhận trong danh sách tài trợ"}
        });
    }

    private void updateTotal() {
        updateSponsorDashboard();
    }

    private void setupSponsorFilters() {
        cboSponsorCampaignFilter.setItems(buildCampaignFilterChoices());
        cboSponsorFieldFilter.setItems(buildFieldFilterChoices());
        cboSponsorCampaignFilter.setValue("Tất cả chiến dịch");
        cboSponsorFieldFilter.setValue("Tất cả lĩnh vực");
        cboSponsorCampaignFilter.valueProperty().addListener((observable, oldValue, newValue) -> applySponsorFilters());
        cboSponsorFieldFilter.valueProperty().addListener((observable, oldValue, newValue) -> applySponsorFilters());
        txtSponsorSearch.textProperty().addListener((observable, oldValue, newValue) -> applySponsorFilters());
    }

    private void refreshSponsorView() {
        updateSponsorDashboard();
        String currentField = cboSponsorFieldFilter.getValue();
        cboSponsorFieldFilter.setItems(buildFieldFilterChoices());
        cboSponsorFieldFilter.setValue(cboSponsorFieldFilter.getItems().contains(currentField)
                ? currentField : "Tất cả lĩnh vực");
        applySponsorFilters();
        tableSponsors.refresh();
    }

    private void updateSponsorDashboard() {
        lblTotalSponsor.setText(FormatUtils.money(AppData.getTotalSponsorAmount()));
        lblSponsorCount.setText(String.valueOf(AppData.getSponsors().size()));
        long campaignCount = AppData.getSponsors().stream()
                .map(SponsorModel::getMaChienDich)
                .distinct()
                .count();
        lblSponsorCampaignCount.setText(String.valueOf(campaignCount));
    }

    private void applySponsorFilters() {
        if (filteredSponsors == null) {
            return;
        }
        String campaignId = codeOf(cboSponsorCampaignFilter.getValue());
        String field = cboSponsorFieldFilter.getValue() == null ? "" : cboSponsorFieldFilter.getValue();
        String query = normalized(value(txtSponsorSearch));
        boolean allCampaigns = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboSponsorCampaignFilter.getValue());
        boolean allFields = field.isEmpty() || "Tất cả lĩnh vực".equals(field);

        filteredSponsors.setPredicate(sponsor
                -> (allCampaigns || sponsor.getMaChienDich().equalsIgnoreCase(campaignId))
                && (allFields || safe(sponsor.getLinhVuc()).equalsIgnoreCase(field))
                && (query.isEmpty() || normalized(sponsor.getTenDoiTac() + " "
                + sponsor.getLinhVuc() + " " + sponsor.getSoDienThoai() + " "
                + sponsor.getEmail() + " " + sponsor.getDiaChi() + " "
                + sponsor.getTenChienDich()).contains(query)));
    }

    private ObservableList<String> buildCampaignFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel activity : AppData.getActivities()) {
            choices.add(activity.getMaChienDich() + " - " + activity.getTenChienDich());
        }
        return choices;
    }

    private ObservableList<String> buildFieldFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả lĩnh vực");
        AppData.getSponsors().stream()
                .map(SponsorModel::getLinhVuc)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .forEach(choices::add);
        return choices;
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
