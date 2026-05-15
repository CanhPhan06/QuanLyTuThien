package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class ContentController {

    private static final String[] FORM_LABELS = {
        "Nhóm bảng", "Mã chính", "Mã liên kết", "Tiêu đề",
        "Nội dung", "Ngày", "Trạng thái", "Ghi chú"
    };

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
    private void initialize() {
        colNhomBang.setCellValueFactory(new PropertyValueFactory<>("nhomBang"));
        colMaChinh.setCellValueFactory(new PropertyValueFactory<>("maChinh"));
        colMaLienKet.setCellValueFactory(new PropertyValueFactory<>("maLienKet"));
        colTieuDe.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colNoiDung.setCellValueFactory(new PropertyValueFactory<>("noiDung"));
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        tableRecords.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableRecords.setItems(AppData.getContents());
        tableRecords.setRowFactory(table -> {
            TableRow<SystemRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    tableRecords.getSelectionModel().select(row.getItem());
                    showRecordDetail(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleAddRecord() {
        SystemRecord record = showRecordDialog("Thêm nội dung", null);
        if (record == null) {
            return;
        }
        if (existsById(record.getMaChinh(), null)) {
            DialogUtils.warning("Mã chính đã tồn tại trong nhóm nội dung.");
            return;
        }

        AppData.getContents().add(record);
        tableRecords.getSelectionModel().clearSelection();
        DialogUtils.info("Đã thêm bản ghi nội dung.");
    }

    @FXML
    private void handleUpdateRecord() {
        SystemRecord selected = tableRecords.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn bản ghi cần cập nhật.");
            return;
        }

        SystemRecord form = showRecordDialog("Cập nhật nội dung", selected);
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
        DialogUtils.info("Đã cập nhật bản ghi nội dung.");
    }

    @FXML
    private void handleDeleteRecord() {
        SystemRecord selected = tableRecords.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn bản ghi cần xóa.");
            return;
        }

        AppData.getContents().remove(selected);
        tableRecords.getSelectionModel().clearSelection();
        DialogUtils.info("Đã xóa bản ghi nội dung.");
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
        String[] values = current == null ? new String[]{"TinTuc", "", "CD001", "", "", AppData.todayText(), "Đã đăng", ""}
                : new String[]{
                    current.getNhomBang(), current.getMaChinh(), current.getMaLienKet(), current.getTieuDe(),
                    current.getNoiDung(), current.getNgay(), current.getTrangThai(), current.getGhiChu()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildRecord(result);
    }

    private SystemRecord buildRecord(String[] values) {
        if (values[0].isEmpty() || values[1].isEmpty() || values[3].isEmpty()) {
            DialogUtils.warning("Vui lòng nhập nhóm bảng, mã chính và tiêu đề.");
            return null;
        }

        return new SystemRecord(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
    }

    private boolean existsById(String id, SystemRecord current) {
        return AppData.getContents().stream()
                .anyMatch(item -> item != current && item.getMaChinh().equalsIgnoreCase(id));
    }

    private void showRecordDetail(SystemRecord record) {
        DetailDialogUtils.showDetails(tableRecords, "Chi tiết nội dung - " + record.getMaChinh(), new String[][]{
            {"Nhóm bảng", record.getNhomBang()},
            {"Mã chính", record.getMaChinh()},
            {"Mã liên kết", record.getMaLienKet()},
            {"Tiêu đề", record.getTieuDe()},
            {"Nội dung", record.getNoiDung()},
            {"Ngày", record.getNgay()},
            {"Trạng thái", record.getTrangThai()},
            {"Ghi chú / bảng liên quan", record.getGhiChu()},
            {"Ý nghĩa dữ liệu", describeRecord(record)},
            {"Luồng sử dụng", "Dữ liệu nội dung/thông báo/tin tức dùng để hiển thị và theo dõi trong hệ thống"}
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
}
