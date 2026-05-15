package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class DonationsController {

    private static final String[] FORM_LABELS = {
        "Mã quyên góp/phiếu", "Email người quyên góp (@gmail.com)", "Mã chiến dịch",
        "Ngày tiếp nhận/giao dịch", "Hình thức", "Nội dung quyên góp", "Số tiền/giá trị ước tính"
    };

    @FXML
    private Label lblTotalDonation;

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
    private void initialize() {
        colMaQuyenGop.setCellValueFactory(new PropertyValueFactory<>("maQuyenGop"));
        colNguoiQuyenGop.setCellValueFactory(new PropertyValueFactory<>("nguoiQuyenGop"));
        colHoatDong.setCellValueFactory(new PropertyValueFactory<>("hoatDong"));
        colNgayQuyenGop.setCellValueFactory(new PropertyValueFactory<>("ngayQuyenGop"));
        colHinhThuc.setCellValueFactory(new PropertyValueFactory<>("hinhThuc"));
        colNoiDungQuyenGop.setCellValueFactory(new PropertyValueFactory<>("noiDungQuyenGop"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTienText"));

        tableDonations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDonations.setItems(AppData.getDonations());
        tableDonations.setRowFactory(table -> {
            TableRow<DonationModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    tableDonations.getSelectionModel().select(row.getItem());
                    showDonationDetail(row.getItem());
                }
            });
            return row;
        });
        updateTotal();
    }

    @FXML
    private void handleAddDonation() {
        DonationModel donation = showDonationDialog("Thêm quyên góp", null);
        if (donation == null) {
            return;
        }
        if (existsById(donation.getMaQuyenGop(), null)) {
            DialogUtils.warning("Mã quyên góp đã tồn tại.");
            return;
        }

        AppData.getDonations().add(donation);
        tableDonations.getSelectionModel().clearSelection();
        updateTotal();
        DialogUtils.info("Đã thêm khoản quyên góp.");
    }

    @FXML
    private void handleUpdateDonation() {
        DonationModel selected = tableDonations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn khoản quyên góp cần cập nhật.");
            return;
        }

        DonationModel form = showDonationDialog("Cập nhật quyên góp", selected);
        if (form == null) {
            return;
        }
        if (existsById(form.getMaQuyenGop(), selected)) {
            DialogUtils.warning("Mã quyên góp đã tồn tại.");
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
        updateTotal();
        DialogUtils.info("Đã cập nhật khoản quyên góp.");
    }

    @FXML
    private void handleDeleteDonation() {
        DonationModel selected = tableDonations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn khoản quyên góp cần xóa.");
            return;
        }

        AppData.getDonations().remove(selected);
        tableDonations.getSelectionModel().clearSelection();
        updateTotal();
        DialogUtils.info("Đã xóa khoản quyên góp.");
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

    private DonationModel showDonationDialog(String title, DonationModel current) {
        String[] values = current == null ? new String[]{AppData.nextDonationId(), "nguoiquyengop@gmail.com", "CD001", AppData.todayText(), "Tiền", "", "0"}
                : new String[]{
                    current.getMaQuyenGop(), current.getNguoiQuyenGop(), current.getHoatDong(),
                    current.getNgayQuyenGop(), current.getHinhThuc(), current.getNoiDungQuyenGop(),
                    current.getSoTien() == 0 ? "" : String.format("%.0f", current.getSoTien())
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildDonation(result);
    }

    private DonationModel buildDonation(String[] values) {
        if (values[0].isEmpty() || values[1].isEmpty() || values[2].isEmpty()
                || values[3].isEmpty() || values[4].isEmpty() || values[5].isEmpty()) {
            DialogUtils.warning("Vui lòng nhập đầy đủ mã, tài khoản, chiến dịch, ngày, hình thức và nội dung quyên góp.");
            return null;
        }
        if (!values[1].toLowerCase().endsWith("@gmail.com")) {
            DialogUtils.warning("Email người quyên góp phải có dạng @gmail.com.");
            return null;
        }

        try {
            double soTien = values[6].isEmpty() ? 0 : FormatUtils.parseMoney(values[6]);
            return new DonationModel(values[0], values[1], values[2], values[3], values[4], values[5], soTien);
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
        DetailDialogUtils.showDetails(tableDonations, "Chi tiết quyên góp - " + donation.getMaQuyenGop(), new String[][]{
            {"Mã quyên góp / phiếu", donation.getMaQuyenGop()},
            {"Email người quyên góp", donation.getNguoiQuyenGop()},
            {"Mã chiến dịch", donation.getHoatDong()},
            {"Tên chiến dịch", campaign == null ? "" : campaign.getTenChienDich()},
            {"Ngày tiếp nhận / giao dịch", donation.getNgayQuyenGop()},
            {"Hình thức", donation.getHinhThuc()},
            {"Nội dung quyên góp", donation.getNoiDungQuyenGop()},
            {"Số tiền / giá trị ước tính", FormatUtils.money(donation.getSoTien())},
            {"Loại đóng góp", donation.getSoTien() > 0 ? "Đóng góp bằng tiền" : "Đóng góp vật tư/vật phẩm/vật dụng"},
            {"Trạng thái xử lý", "Đã ghi nhận trong hệ thống mẫu"}
        });
    }

    private void updateTotal() {
        lblTotalDonation.setText(FormatUtils.money(AppData.getTotalDonationAmount()));
    }
}
