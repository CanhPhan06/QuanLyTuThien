package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class ParticipantsController {

    private static final String[] FORM_LABELS = {
        "Mã tài khoản", "Mã hồ sơ", "Họ tên", "MSSV", "Số điện thoại",
        "Khoa", "Trường", "Mã chiến dịch", "Trạng thái duyệt", "Điểm đánh giá"
    };

    @FXML
    private TableView<ParticipantModel> tableParticipants;
    @FXML
    private TableColumn<ParticipantModel, String> colMaTaiKhoan;
    @FXML
    private TableColumn<ParticipantModel, String> colMaHoSo;
    @FXML
    private TableColumn<ParticipantModel, String> colHoTen;
    @FXML
    private TableColumn<ParticipantModel, String> colMssv;
    @FXML
    private TableColumn<ParticipantModel, String> colSoDienThoai;
    @FXML
    private TableColumn<ParticipantModel, String> colKhoa;
    @FXML
    private TableColumn<ParticipantModel, String> colTruong;
    @FXML
    private TableColumn<ParticipantModel, String> colMaChienDich;
    @FXML
    private TableColumn<ParticipantModel, String> colTrangThaiDuyet;
    @FXML
    private TableColumn<ParticipantModel, String> colDiemDanhGia;

    @FXML
    private void initialize() {
        colMaTaiKhoan.setCellValueFactory(new PropertyValueFactory<>("maTaiKhoan"));
        colMaHoSo.setCellValueFactory(new PropertyValueFactory<>("maHoSo"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colMssv.setCellValueFactory(new PropertyValueFactory<>("mssv"));
        colSoDienThoai.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colKhoa.setCellValueFactory(new PropertyValueFactory<>("khoa"));
        colTruong.setCellValueFactory(new PropertyValueFactory<>("truong"));
        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colTrangThaiDuyet.setCellValueFactory(new PropertyValueFactory<>("trangThaiDuyet"));
        colDiemDanhGia.setCellValueFactory(new PropertyValueFactory<>("diemDanhGia"));

        tableParticipants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableParticipants.setItems(AppData.getParticipants());
        tableParticipants.setRowFactory(table -> {
            TableRow<ParticipantModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    tableParticipants.getSelectionModel().select(row.getItem());
                    showParticipantDetail(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleAddParticipant() {
        ParticipantModel participant = showParticipantDialog("Thêm sinh viên / tình nguyện viên", null);
        if (participant == null) {
            return;
        }
        if (existsById(participant.getMaTaiKhoan(), null)) {
            DialogUtils.warning("Mã tài khoản đã tồn tại.");
            return;
        }

        AppData.getParticipants().add(participant);
        tableParticipants.getSelectionModel().clearSelection();
        DialogUtils.info("Đã thêm sinh viên/TNV.");
    }

    @FXML
    private void handleUpdateParticipant() {
        ParticipantModel selected = tableParticipants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn sinh viên/TNV cần cập nhật.");
            return;
        }

        ParticipantModel form = showParticipantDialog("Cập nhật sinh viên / tình nguyện viên", selected);
        if (form == null) {
            return;
        }
        if (existsById(form.getMaTaiKhoan(), selected)) {
            DialogUtils.warning("Mã tài khoản đã tồn tại.");
            return;
        }

        selected.setMaTaiKhoan(form.getMaTaiKhoan());
        selected.setMaHoSo(form.getMaHoSo());
        selected.setHoTen(form.getHoTen());
        selected.setMssv(form.getMssv());
        selected.setSoDienThoai(form.getSoDienThoai());
        selected.setKhoa(form.getKhoa());
        selected.setTruong(form.getTruong());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setTrangThaiDuyet(form.getTrangThaiDuyet());
        selected.setDiemDanhGia(form.getDiemDanhGia());
        tableParticipants.refresh();
        tableParticipants.getSelectionModel().clearSelection();
        DialogUtils.info("Đã cập nhật sinh viên/TNV.");
    }

    @FXML
    private void handleDeleteParticipant() {
        ParticipantModel selected = tableParticipants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn sinh viên/TNV cần xóa.");
            return;
        }

        AppData.getParticipants().remove(selected);
        tableParticipants.getSelectionModel().clearSelection();
        DialogUtils.info("Đã xóa sinh viên/TNV.");
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

    private ParticipantModel showParticipantDialog(String title, ParticipantModel current) {
        String[] values = current == null ? new String[]{"", "", "", "", "", "Khoa Công nghệ phần mềm", "UIT", "CD001", "Chờ duyệt", ""}
                : new String[]{
                    current.getMaTaiKhoan(), current.getMaHoSo(), current.getHoTen(), current.getMssv(),
                    current.getSoDienThoai(), current.getKhoa(), current.getTruong(),
                    current.getMaChienDich(), current.getTrangThaiDuyet(), current.getDiemDanhGia()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildParticipant(result);
    }

    private ParticipantModel buildParticipant(String[] values) {
        if (values[0].isEmpty() || values[1].isEmpty() || values[2].isEmpty() || values[7].isEmpty()) {
            DialogUtils.warning("Vui lòng nhập mã tài khoản, mã hồ sơ, họ tên và mã chiến dịch.");
            return null;
        }

        return new ParticipantModel(values[0], values[1], values[2], values[3], values[4],
                values[5], values[6], values[7], values[8], values[9]);
    }

    private boolean existsById(String id, ParticipantModel current) {
        return AppData.getParticipants().stream()
                .anyMatch(item -> item != current && item.getMaTaiKhoan().equalsIgnoreCase(id));
    }

    private void showParticipantDetail(ParticipantModel participant) {
        ActivityModel campaign = AppData.findCampaign(participant.getMaChienDich());
        DetailDialogUtils.showDetails(tableParticipants, "Chi tiết tình nguyện viên - " + participant.getMaTaiKhoan(), new String[][]{
            {"Mã tài khoản", participant.getMaTaiKhoan()},
            {"Mã hồ sơ", participant.getMaHoSo()},
            {"Họ tên", participant.getHoTen()},
            {"MSSV", participant.getMssv()},
            {"Số điện thoại", participant.getSoDienThoai()},
            {"Khoa", participant.getKhoa()},
            {"Trường", participant.getTruong()},
            {"Mã chiến dịch", participant.getMaChienDich()},
            {"Tên chiến dịch", campaign == null ? "" : campaign.getTenChienDich()},
            {"Trạng thái duyệt", participant.getTrangThaiDuyet()},
            {"Điểm đánh giá", participant.getDiemDanhGia()},
            {"Vai trò trong hệ thống", "Tình nguyện viên/sinh viên tham gia chiến dịch"},
            {"Ghi chú", "Sinh viên thuộc hệ thống ĐHQG-TPHCM"}
        });
    }
}
