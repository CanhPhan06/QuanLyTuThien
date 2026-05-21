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

public class ContentController {

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
    private ComboBox<String> cboContentTypeFilter;
    @FXML
    private ComboBox<String> cboContentStatusFilter;
    @FXML
    private TextField txtContentSearch;

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

    private FilteredList<SystemRecord> filteredContents;

    @FXML
    private void initialize() {
        colNhomBang.setCellValueFactory(new PropertyValueFactory<>("tenNhomBang"));
        colMaChinh.setCellValueFactory(new PropertyValueFactory<>("maChinh"));
        colMaChinh.setVisible(false);
        colMaLienKet.setText("Đối tượng liên quan");
        colMaLienKet.setCellValueFactory(new PropertyValueFactory<>("tenLienKet"));
        colTieuDe.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colNoiDung.setCellValueFactory(new PropertyValueFactory<>("noiDung"));
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        tableRecords.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredContents = new FilteredList<>(AppData.getContents(), item -> true);
        tableRecords.setItems(filteredContents);
        cboContentTypeFilter.setItems(FXCollections.observableArrayList(
                "Tất cả nội dung", "Tin tức", "Bình luận", "Thông báo", "Nhật ký hệ thống", "Tham số"
        ));
        cboContentStatusFilter.setItems(buildStatusFilterChoices());
        cboContentTypeFilter.setValue("Tất cả nội dung");
        cboContentStatusFilter.setValue("Tất cả trạng thái");
        cboContentTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyContentFilters());
        cboContentStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyContentFilters());
        txtContentSearch.textProperty().addListener((observable, oldValue, newValue) -> applyContentFilters());
        tableRecords.setRowFactory(table -> {
            TableRow<SystemRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableRecords.getSelectionModel().select(row.getItem());
                    showRecordDetail(row.getItem());
                }
            });
            return row;
        });
        updateContentDashboard();
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
        tableRecords.getSelectionModel().clearSelection();
        refreshContentView();
        DialogUtils.info("Đã tạo nội dung mới.");
    }

    @FXML
    private void handleUpdateRecord() {
        SystemRecord selected = tableRecords.getSelectionModel().getSelectedItem();
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
        selected.setMaLienKet(form.getMaLienKet());
        selected.setTieuDe(form.getTieuDe());
        selected.setNoiDung(form.getNoiDung());
        selected.setNgay(form.getNgay());
        selected.setTrangThai(form.getTrangThai());
        selected.setGhiChu(form.getGhiChu());
        tableRecords.refresh();
        tableRecords.getSelectionModel().clearSelection();
        refreshContentView();
        DialogUtils.info("Đã chỉnh sửa nội dung.");
    }

    @FXML
    private void handleDeleteRecord() {
        SystemRecord selected = tableRecords.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa bản ghi nội dung " + selected.getMaChinh() + "?")) {
            return;
        }

        AppData.getContents().remove(selected);
        tableRecords.getSelectionModel().clearSelection();
        refreshContentView();
        DialogUtils.info("Đã xóa nội dung.");
    }

    @FXML
    private void handleClearContentFilters() {
        cboContentTypeFilter.setValue("Tất cả nội dung");
        cboContentStatusFilter.setValue("Tất cả trạng thái");
        txtContentSearch.clear();
        applyContentFilters();
    }

    @FXML
    private void handleExportContent() {
        ExportUtils.exportTableToCsv(tableRecords, "Xuất danh sách nội dung", "danh-sach-noi-dung.csv");
    }

    @FXML
    private void handleBackHome() throws IOException {
        App.setRoot("secondary");
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
        App.setRoot("primary");
    }

    private SystemRecord showRecordDialog(String title, SystemRecord current) {
        String[] values = current == null ? new String[]{"Tin tức", defaultRelatedOption(), "", "", AppData.todayText(), "Đã đăng", ""}
                : new String[]{
                    current.getTenNhomBang(), current.getMaLienKet() + " - " + current.getTenLienKet(), current.getTieuDe(),
                    current.getNoiDung(), current.getNgay(), current.getTrangThai(), current.getGhiChu()
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

        return new SystemRecord(groupCode, maChinh, maLienKet, values[2], values[3], values[4], values[5], values[6]);
    }

    private boolean existsById(String id, SystemRecord current) {
        return AppData.getContents().stream()
                .anyMatch(item -> item != current && item.getMaChinh().equalsIgnoreCase(id));
    }

    private void showRecordDetail(SystemRecord record) {
        DetailDialogUtils.showDetails(tableRecords, record.getTenNhomBang() + " - " + record.getTieuDe(), new String[][]{
            {"Loại nội dung", record.getTenNhomBang()},
            {"Liên quan đến", record.getTenLienKet()},
            {"Tiêu đề", record.getTieuDe()},
            {"Nội dung", record.getNoiDung()},
            {"Ngày đăng/gửi", record.getNgay()},
            {"Trạng thái", record.getTrangThai()},
            {"Ghi chú nội bộ", record.getGhiChu()},
            {"Ý nghĩa", describeRecord(record)}
        });
    }

    private String describeRecord(SystemRecord record) {
        String group = record.getNhomBang() == null ? "" : record.getNhomBang().toLowerCase();
        if (group.contains("tintuc")) {
            return "Bài tin tức liên quan đến chiến dịch.";
        }
        if (group.contains("binhluan")) {
            return "Bình luận/phản hồi của người dùng.";
        }
        if (group.contains("thongbao")) {
            return "Thông báo gửi đến tài khoản trong hệ thống.";
        }
        if (group.contains("nhatky")) {
            return "Nhật ký thao tác hệ thống.";
        }
        return "Bản ghi nội dung/hệ thống.";
    }

    private void refreshContentView() {
        updateContentDashboard();
        String currentStatus = cboContentStatusFilter.getValue();
        cboContentStatusFilter.setItems(buildStatusFilterChoices());
        if (cboContentStatusFilter.getItems().contains(currentStatus)) {
            cboContentStatusFilter.setValue(currentStatus);
        } else {
            cboContentStatusFilter.setValue("Tất cả trạng thái");
        }
        applyContentFilters();
        tableRecords.refresh();
    }

    private void updateContentDashboard() {
        long newsCount = AppData.getContents().stream()
                .filter(record -> record.getNhomBang().equalsIgnoreCase("TinTuc")
                || record.getNhomBang().equalsIgnoreCase("BinhLuan"))
                .count();
        long noticeCount = AppData.getContents().stream()
                .filter(record -> record.getNhomBang().equalsIgnoreCase("ThongBao"))
                .count();
        long visibleCount = AppData.getContents().stream()
                .filter(record -> "Đã đăng".equals(record.getTrangThai())
                || "Hiển thị".equals(record.getTrangThai())
                || "Đang dùng".equals(record.getTrangThai()))
                .count();
        long pendingCount = AppData.getContents().stream()
                .filter(record -> "Chờ duyệt".equals(record.getTrangThai())
                || "Bản nháp".equals(record.getTrangThai())
                || "Chưa đọc".equals(record.getTrangThai()))
                .count();
        lblNewsCount.setText(String.valueOf(newsCount));
        lblNoticeCount.setText(String.valueOf(noticeCount));
        lblPendingContent.setText(String.valueOf(pendingCount));
        lblVisibleCount.setText(String.valueOf(visibleCount));
    }

    private void applyContentFilters() {
        if (filteredContents == null) {
            return;
        }
        String type = cboContentTypeFilter.getValue() == null ? "" : cboContentTypeFilter.getValue();
        String status = cboContentStatusFilter.getValue() == null ? "" : cboContentStatusFilter.getValue();
        String query = normalized(value(txtContentSearch));
        boolean allTypes = type.isEmpty() || "Tất cả nội dung".equals(type);
        boolean allStatuses = status.isEmpty() || "Tất cả trạng thái".equals(status);

        filteredContents.setPredicate(record
                -> (allTypes || type.equals(record.getTenNhomBang()))
                && (allStatuses || status.equals(record.getTrangThai()))
                && (query.isEmpty() || normalized(record.getTenNhomBang() + " "
                + record.getTenLienKet() + " " + record.getTieuDe() + " "
                + record.getNoiDung() + " " + record.getTrangThai()).contains(query)));
    }

    private ObservableList<String> buildStatusFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trạng thái");
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

    private String normalized(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }
}
