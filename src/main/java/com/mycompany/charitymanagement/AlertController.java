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

public class AlertController {

    @FXML
    private Label lblTotalAlerts;
    @FXML
    private Label lblUrgentAlerts;
    @FXML
    private Label lblUnresolvedAlerts;
    @FXML
    private Label lblOverdueAlerts;
    @FXML
    private ComboBox<String> cboLevelFilter;
    @FXML
    private ComboBox<String> cboTypeFilter;
    @FXML
    private ComboBox<String> cboStatusFilter;
    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Alert> tableAlerts;
    @FXML
    private TableColumn<Alert, String> colMaCanhBao;
    @FXML
    private TableColumn<Alert, String> colTieuDe;
    @FXML
    private TableColumn<Alert, String> colNoiDung;
    @FXML
    private TableColumn<Alert, String> colMucDo;
    @FXML
    private TableColumn<Alert, String> colLoai;
    @FXML
    private TableColumn<Alert, String> colNgayTao;
    @FXML
    private TableColumn<Alert, String> colHanXuLy;
    @FXML
    private TableColumn<Alert, String> colTrangThai;
    @FXML
    private TableColumn<Alert, String> colNguoiPhuTrach;

    private FilteredList<Alert> filteredAlerts;

    @FXML
    private void initialize() {
        colMaCanhBao.setCellValueFactory(new PropertyValueFactory<>("maCanhBao"));
        colMaCanhBao.setVisible(false);
        colTieuDe.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colNoiDung.setCellValueFactory(new PropertyValueFactory<>("noiDung"));
        colMucDo.setCellValueFactory(new PropertyValueFactory<>("mucDo"));
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loai"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colHanXuLy.setCellValueFactory(new PropertyValueFactory<>("hanXuLy"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colNguoiPhuTrach.setCellValueFactory(new PropertyValueFactory<>("nguoiPhuTrach"));

        tableAlerts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredAlerts = new FilteredList<>(AppData.getAlerts(), item -> true);
        tableAlerts.setItems(filteredAlerts);
        setupFilters();
        tableAlerts.setRowFactory(table -> {
            TableRow<Alert> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableAlerts.getSelectionModel().select(row.getItem());
                    showDetail(row.getItem());
                }
            });
            return row;
        });
        refreshView();
    }

    @FXML
    private void handleMarkResolved() {
        Alert selected = tableAlerts.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn cảnh báo cần xử lý."); return; }
        if (!"Chưa xử lý".equals(selected.getTrangThai())) {
            DialogUtils.warning("Cảnh báo này đã được xử lý.");
            return;
        }
        if (!DialogUtils.confirm("Đánh dấu cảnh báo \"" + selected.getTieuDe() + "\" đã xử lý?")) return;
        selected.setTrangThai("Đã xử lý");
        BusinessService.audit(currentUser(), "Xử lý cảnh báo", selected.getMaCanhBao() + " - " + selected.getTieuDe());
        tableAlerts.refresh();
        refreshView();
        DialogUtils.info("Đã đánh dấu cảnh báo đã xử lý.");
    }

    @FXML
    private void handleDeleteAlert() {
        Alert selected = tableAlerts.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn cảnh báo cần xóa."); return; }
        if (!DialogUtils.confirm("Xóa cảnh báo \"" + selected.getTieuDe() + "\"?")) return;
        AppData.getAlerts().remove(selected);
        tableAlerts.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã xóa cảnh báo.");
    }

    @FXML
    private void handleClearFilters() {
        cboLevelFilter.setValue("Tất cả mức độ");
        cboTypeFilter.setValue("Tất cả loại");
        cboStatusFilter.setValue("Tất cả trạng thái");
        txtSearch.clear();
        applyFilters();
    }

    @FXML
    private void handleExport() {
        ExportUtils.exportTableToCsv(tableAlerts, "Xuất danh sách cảnh báo", "danh-sach-canh-bao.csv");
    }

    private void showDetail(Alert a) {
        DetailDialogUtils.showDetails(tableAlerts, "Chi tiết cảnh báo - " + a.getTieuDe(), new String[][]{
            {"Tiêu đề", a.getTieuDe()},
            {"Nội dung", a.getNoiDung()},
            {"Mức độ", a.getMucDo()},
            {"Loại", a.getLoai()},
            {"Đối tượng liên quan", a.getDoiTuongLienQuan().isEmpty() ? "-" : a.getDoiTuongLienQuan()},
            {"Ngày tạo", a.getNgayTao()},
            {"Hạn xử lý", a.getHanXuLy().isEmpty() ? "-" : a.getHanXuLy()},
            {"Trạng thái", a.getTrangThai()},
            {"Người phụ trách", a.getNguoiPhuTrach()}
        });
    }

    private void setupFilters() {
        cboLevelFilter.setItems(FXCollections.observableArrayList("Tất cả mức độ", "Cao", "Trung bình"));
        cboTypeFilter.setItems(FXCollections.observableArrayList("Tất cả loại", "Tồn kho", "Nhân sự"));
        cboStatusFilter.setItems(FXCollections.observableArrayList("Tất cả trạng thái", "Chưa xử lý", "Đã xử lý"));
        cboLevelFilter.setValue("Tất cả mức độ");
        cboTypeFilter.setValue("Tất cả loại");
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboLevelFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboTypeFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboStatusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        txtSearch.textProperty().addListener((o, a, b) -> applyFilters());
    }

    private void refreshView() {
        ObservableList<Alert> all = AppData.getAlerts();
        lblTotalAlerts.setText(String.valueOf(all.size()));
        long urgent = all.stream().filter(Alert::isUrgent).count();
        lblUrgentAlerts.setText(String.valueOf(urgent));
        long unresolved = all.stream().filter(a -> "Chưa xử lý".equals(a.getTrangThai())).count();
        lblUnresolvedAlerts.setText(String.valueOf(unresolved));
        long overdue = all.stream().filter(Alert::isOverdue).count();
        lblOverdueAlerts.setText(String.valueOf(overdue));
        applyFilters();
        tableAlerts.refresh();
    }

    private void applyFilters() {
        if (filteredAlerts == null) return;
        String level = value(cboLevelFilter);
        String type = value(cboTypeFilter);
        String status = value(cboStatusFilter);
        String query = normalize(value(txtSearch));
        boolean allLevel = level.isEmpty() || "Tất cả mức độ".equals(level);
        boolean allType = type.isEmpty() || "Tất cả loại".equals(type);
        boolean allStatus = status.isEmpty() || "Tất cả trạng thái".equals(status);
        filteredAlerts.setPredicate(a
                -> (allLevel || normalize(a.getMucDo()).contains(normalize(level)))
                && (allType || normalize(a.getLoai()).contains(normalize(type)))
                && (allStatus || normalize(a.getTrangThai()).contains(normalize(status)))
                && (query.isEmpty() || normalize(a.getTieuDe() + " " + a.getNoiDung() + " " + a.getDoiTuongLienQuan() + " " + a.getNguoiPhuTrach()).contains(query)));
    }

    private String currentUser() { UserAccount u = UserSession.getCurrentUser(); return u == null ? "ADMIN001" : u.getUsername(); }
    private String value(ComboBox<String> cb) { return cb.getValue() == null ? "" : cb.getValue(); }
    private String value(TextField tf) { return tf == null || tf.getText() == null ? "" : tf.getText().trim(); }
    private String normalize(String v) { if (v == null) return ""; return Normalizer.normalize(v.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("đ", "d"); }

    @FXML private void handleBackHome() throws IOException { App.setRoot("secondary"); }
    @FXML private void handleLogout() throws IOException { UserSession.clear(); App.setRoot("primary"); }
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
    @FXML private void handleAlerts() throws IOException { App.setRoot("alert"); }
}
