package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
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

public class ScreeningController {

    private static final String[] FORM_LABELS = {
        "Họ tên ứng viên", "Số điện thoại", "Email", "Chiến dịch đăng ký",
        "Trạng thái hồ sơ", "Kết quả vãng gia", "Ngày phỏng vấn", "Kết quả phỏng vấn",
        "Ghi chú / Nhận xét"
    };

    @FXML
    private Label lblTotalApplicants;
    @FXML
    private Label lblPendingReview;
    @FXML
    private Label lblInterviewed;
    @FXML
    private Label lblApproved;
    @FXML
    private ComboBox<String> cboStatusFilter;
    @FXML
    private ComboBox<String> cboCampaignFilter;
    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<ParticipantModel> tableApplicants;
    @FXML
    private TableColumn<ParticipantModel, String> colHoTen;
    @FXML
    private TableColumn<ParticipantModel, String> colSoDienThoai;
    @FXML
    private TableColumn<ParticipantModel, String> colEmail;
    @FXML
    private TableColumn<ParticipantModel, String> colChienDich;
    @FXML
    private TableColumn<ParticipantModel, String> colTrangThai;
    @FXML
    private TableColumn<ParticipantModel, String> colKetQuaVangGia;
    @FXML
    private TableColumn<ParticipantModel, String> colNgayPhongVan;
    @FXML
    private TableColumn<ParticipantModel, String> colKetQuaPhongVan;
    @FXML
    private TableColumn<ParticipantModel, String> colGhiChu;

    private FilteredList<ParticipantModel> filteredApplicants;

    @FXML
    private void initialize() {
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colSoDienThoai.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThaiDuyet"));
        colKetQuaVangGia.setCellValueFactory(new PropertyValueFactory<>("ketQuaVangGia"));
        colNgayPhongVan.setCellValueFactory(new PropertyValueFactory<>("ngayPhongVan"));
        colKetQuaPhongVan.setCellValueFactory(new PropertyValueFactory<>("ketQuaPhongVan"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("screeningNote"));

        tableApplicants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredApplicants = new FilteredList<>(AppData.getParticipants(), item -> true);
        tableApplicants.setItems(filteredApplicants);
        setupFilters();
        tableApplicants.setRowFactory(table -> {
            TableRow<ParticipantModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableApplicants.getSelectionModel().select(row.getItem());
                    showDetail(row.getItem());
                }
            });
            return row;
        });
        refreshView();
    }

    @FXML
    private void handleRecordHomeVisit() {
        ParticipantModel selected = tableApplicants.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn hồ sơ cần ghi nhận vãng gia."); return; }
        String[] result = CrudDialogUtils.showForm("Vãng gia - " + selected.getHoTen(),
                new String[]{"Kết quả vãng gia", "Ngày vãng gia", "Ghi chú"},
                new String[]{"Đạt", AppData.todayText(), ""});
        if (result == null) return;
        selected.setKetQuaVangGia(result[0]);
        selected.setNgayVangGia(result[1]);
        selected.setScreeningNote(result[2]);
        BusinessService.audit(currentUser(), "Vãng gia", selected.getMaTaiKhoan() + " - " + result[0]);
        tableApplicants.refresh();
        refreshView();
        DialogUtils.info("Đã ghi nhận kết quả vãng gia.");
    }

    @FXML
    private void handleRecordInterview() {
        ParticipantModel selected = tableApplicants.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn hồ sơ cần phỏng vấn."); return; }
        String[] result = CrudDialogUtils.showForm("Phỏng vấn - " + selected.getHoTen(),
                new String[]{"Ngày phỏng vấn", "Kết quả", "Đánh giá của hội đồng"},
                new String[]{AppData.todayText(), "Đạt", ""});
        if (result == null) return;
        selected.setNgayPhongVan(result[0]);
        selected.setKetQuaPhongVan(result[1]);
        selected.setScreeningNote(selected.getScreeningNote() + " | HĐ: " + result[2]);
        if ("Đạt".equals(result[1])) {
            selected.setTrangThaiDuyet("Đã duyệt");
        } else {
            selected.setTrangThaiDuyet("Từ chối");
        }
        BusinessService.audit(currentUser(), "Phỏng vấn", selected.getMaTaiKhoan() + " - " + result[1]);
        AppData.getOperations().add(new SystemRecord("Xét duyệt", AppData.nextOperationId("XD"),
                selected.getMaChienDich(), selected.getMaTaiKhoan(), "Hội đồng xét duyệt",
                "Kết quả: " + result[1] + " - " + result[2],
                AppData.todayText(), "", "Đã xét", currentUser(), "ADMIN001", "Bảng HoiDongXetDuyet"));
        tableApplicants.refresh();
        refreshView();
        DialogUtils.info("Đã ghi nhận kết quả phỏng vấn.");
    }

    @FXML
    private void handleSubmitToCommittee() {
        ParticipantModel selected = tableApplicants.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn hồ sơ cần trình hội đồng."); return; }
        if (!"Đã duyệt".equals(selected.getTrangThaiDuyet())) {
            DialogUtils.warning("Chỉ có thể trình hội đồng hồ sơ đã phỏng vấn đạt.");
            return;
        }
        selected.setTrangThaiDuyet("Chờ hội đồng");
        selected.setScreeningNote(selected.getScreeningNote() + " | Trình HĐ: " + AppData.todayText());
        BusinessService.audit(currentUser(), "Trình hội đồng", selected.getMaTaiKhoan());
        AppData.getOperations().add(new SystemRecord("Xét duyệt", AppData.nextOperationId("XD"),
                selected.getMaChienDich(), selected.getMaTaiKhoan(), "Trình hội đồng xét duyệt",
                "Hồ sơ trình hội đồng ngày " + AppData.todayText(),
                AppData.todayText(), "", "Chờ xét", currentUser(), "ADMIN001", "Bảng HoiDongXetDuyet"));
        tableApplicants.refresh();
        refreshView();
        DialogUtils.info("Đã trình hội đồng xét duyệt.");
    }

    @FXML
    private void handleCommitteeReview() {
        ParticipantModel selected = tableApplicants.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn hồ sơ cần hội đồng xét duyệt."); return; }
        if (!"Chờ hội đồng".equals(selected.getTrangThaiDuyet())) {
            DialogUtils.warning("Chỉ có thể xét duyệt hồ sơ đang chờ hội đồng.");
            return;
        }
        String[] result = CrudDialogUtils.showForm("Hội đồng xét duyệt - " + selected.getHoTen(),
                new String[]{"Kết quả xét duyệt", "Nhận xét của hội đồng"},
                new String[]{"Đạt", ""});
        if (result == null) return;
        if ("Đạt".equals(result[0])) {
            selected.setTrangThaiDuyet("Đã duyệt hội đồng");
        } else {
            selected.setTrangThaiDuyet("Từ chối");
        }
        selected.setScreeningNote(selected.getScreeningNote() + " | HĐ xét: " + result[1]);
        BusinessService.audit(currentUser(), "Hội đồng xét duyệt", selected.getMaTaiKhoan() + " - " + result[0]);
        AppData.getOperations().add(new SystemRecord("Xét duyệt", AppData.nextOperationId("XD"),
                selected.getMaChienDich(), selected.getMaTaiKhoan(), "Kết luận hội đồng",
                "Kết quả: " + result[0] + " - " + result[1],
                AppData.todayText(), "", "Đã xét", currentUser(), "ADMIN001", "Bảng HoiDongXetDuyet"));
        tableApplicants.refresh();
        refreshView();
        DialogUtils.info("Đã ghi nhận kết quả hội đồng xét duyệt.");
    }

    @FXML
    private void handleClearFilters() {
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        txtSearch.clear();
        applyFilters();
    }

    @FXML
    private void handleExport() {
        ExportUtils.exportTableToCsv(tableApplicants, "Xuất danh sách ứng viên", "danh-sach-ung-vien.csv");
    }

    private void showDetail(ParticipantModel p) {
        DetailDialogUtils.showDetails(tableApplicants, "Hồ sơ ứng viên - " + p.getHoTen(), new String[][]{
            {"Họ tên", p.getHoTen()},
            {"Số điện thoại", p.getSoDienThoai()},
            {"Email", p.getMssv()},
            {"Chiến dịch đăng ký", p.getTenChienDich()},
            {"Trường/Khoa", p.getTruong() + " - " + p.getKhoa()},
            {"Trạng thái hồ sơ", p.getTrangThaiDuyet()},
            {"Vãng gia", p.getKetQuaVangGia().isEmpty() ? "Chưa thực hiện" : p.getKetQuaVangGia()},
            {"Ngày vãng gia", p.getNgayVangGia().isEmpty() ? "-" : p.getNgayVangGia()},
            {"Phỏng vấn", p.getKetQuaPhongVan().isEmpty() ? "Chưa thực hiện" : p.getKetQuaPhongVan()},
            {"Ngày phỏng vấn", p.getNgayPhongVan().isEmpty() ? "-" : p.getNgayPhongVan()},
            {"Ghi chú", p.getScreeningNote().isEmpty() ? "-" : p.getScreeningNote()}
        });
    }

    private void setupFilters() {
        cboStatusFilter.setItems(buildStatusChoices());
        cboCampaignFilter.setItems(buildCampaignChoices());
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        cboStatusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboCampaignFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        txtSearch.textProperty().addListener((o, a, b) -> applyFilters());
    }

    private void refreshView() {
        ObservableList<ParticipantModel> all = AppData.getParticipants();
        lblTotalApplicants.setText(String.valueOf(all.size()));
        long pending = all.stream().filter(p -> "Chờ duyệt".equals(p.getTrangThaiDuyet()) || "Chờ hội đồng".equals(p.getTrangThaiDuyet())).count();
        lblPendingReview.setText(String.valueOf(pending));
        long interviewed = all.stream().filter(p -> !p.getNgayPhongVan().isEmpty()).count();
        lblInterviewed.setText(String.valueOf(interviewed));
        long approved = all.stream().filter(p -> "Đã duyệt".equals(p.getTrangThaiDuyet()) || "Đã duyệt hội đồng".equals(p.getTrangThaiDuyet())).count();
        lblApproved.setText(String.valueOf(approved));
        String cs = cboStatusFilter.getValue();
        String cc = cboCampaignFilter.getValue();
        cboStatusFilter.setItems(buildStatusChoices());
        cboCampaignFilter.setItems(buildCampaignChoices());
        cboStatusFilter.setValue(cboStatusFilter.getItems().contains(cs) ? cs : "Tất cả trạng thái");
        cboCampaignFilter.setValue(cboCampaignFilter.getItems().contains(cc) ? cc : "Tất cả chiến dịch");
        applyFilters();
        tableApplicants.refresh();
    }

    private void applyFilters() {
        if (filteredApplicants == null) return;
        String status = value(cboStatusFilter);
        String campaignId = codeOf(cboCampaignFilter.getValue());
        String query = normalize(value(txtSearch));
        boolean allStatus = status.isEmpty() || "Tất cả trạng thái".equals(status);
        boolean allCamp = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboCampaignFilter.getValue());
        filteredApplicants.setPredicate(p
                -> (allStatus || normalize(p.getTrangThaiDuyet()).equals(normalize(status)))
                && (allCamp || p.getMaChienDich().equalsIgnoreCase(campaignId))
                && (query.isEmpty() || normalize(p.getHoTen() + " " + p.getSoDienThoai() + " " + p.getMssv()).contains(query)));
    }

    private ObservableList<String> buildStatusChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trạng thái");
        AppData.getParticipants().stream().map(ParticipantModel::getTrangThaiDuyet)
                .filter(v -> v != null && !v.isEmpty()).distinct().forEach(choices::add);
        return choices;
    }
    private ObservableList<String> buildCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel a : AppData.getActivities()) choices.add(a.getMaChienDich() + " - " + a.getTenChienDich());
        return choices;
    }

    private String currentUser() { UserAccount u = UserSession.getCurrentUser(); return u == null ? "ADMIN001" : u.getUsername(); }
    private String codeOf(String opt) { if (opt == null) return ""; int s = opt.indexOf(" - "); return s >= 0 ? opt.substring(0, s).trim() : opt.trim(); }
    private String value(ComboBox<String> cb) { return cb.getValue() == null ? "" : cb.getValue(); }
    private String value(TextField tf) { return tf == null || tf.getText() == null ? "" : tf.getText().trim(); }
    private String normalize(String v) { if (v == null) return ""; return Normalizer.normalize(v.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("đ", "d"); }

    @FXML private void handleBackHome() throws IOException { App.setRoot("secondary"); }
    @FXML private void handleAlerts() throws IOException { App.setRoot("alert"); }

    @FXML
    private void handleLogout() throws IOException { UserSession.clear(); App.setRoot("primary"); }
    @FXML private void handleActivities() throws IOException { App.setRoot("activities"); }
    @FXML private void handleParticipants() throws IOException { App.setRoot("participants"); }
    @FXML private void handleScreening() throws IOException { App.setRoot("screening"); }
    @FXML private void handleTraining() throws IOException { App.setRoot("training"); }
    @FXML private void handleSponsors() throws IOException { App.setRoot("sponsors"); }
    @FXML private void handleDonations() throws IOException { App.setRoot("donations"); }
    @FXML private void handleOperations() throws IOException { App.setRoot("operations"); }
    @FXML private void handleInventory() throws IOException { App.setRoot("inventory"); }
    @FXML private void handleExpense() throws IOException { App.setRoot("expense"); }
    @FXML private void handleContent() throws IOException { App.setRoot("content"); }
    @FXML private void handleReports() throws IOException { App.setRoot("reports"); }
}
