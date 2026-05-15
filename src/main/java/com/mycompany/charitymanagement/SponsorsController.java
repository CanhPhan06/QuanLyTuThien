package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class SponsorsController {

    private static final String[] FORM_LABELS = {
        "Mã đối tác", "Tên đối tác", "Lĩnh vực", "Số điện thoại", "Email",
        "Địa chỉ", "Mã chiến dịch", "Giá trị tài trợ", "Ngày ký kết"
    };

    @FXML
    private Label lblTotalSponsor;

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

    @FXML
    private void initialize() {
        colMaDoiTac.setCellValueFactory(new PropertyValueFactory<>("maDoiTac"));
        colTenDoiTac.setCellValueFactory(new PropertyValueFactory<>("tenDoiTac"));
        colLinhVuc.setCellValueFactory(new PropertyValueFactory<>("linhVuc"));
        colSoDienThoai.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colGiaTriTaiTro.setCellValueFactory(new PropertyValueFactory<>("giaTriTaiTroText"));
        colNgayKyKet.setCellValueFactory(new PropertyValueFactory<>("ngayKyKet"));

        tableSponsors.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSponsors.setItems(AppData.getSponsors());
        tableSponsors.setRowFactory(table -> {
            TableRow<SponsorModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    tableSponsors.getSelectionModel().select(row.getItem());
                    showSponsorDetail(row.getItem());
                }
            });
            return row;
        });
        updateTotal();
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
        updateTotal();
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
        updateTotal();
        DialogUtils.info("Đã cập nhật đối tác/tài trợ.");
    }

    @FXML
    private void handleDeleteSponsor() {
        SponsorModel selected = tableSponsors.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn đối tác/tài trợ cần xóa.");
            return;
        }

        AppData.getSponsors().remove(selected);
        tableSponsors.getSelectionModel().clearSelection();
        updateTotal();
        DialogUtils.info("Đã xóa đối tác/tài trợ.");
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

    private SponsorModel showSponsorDialog(String title, SponsorModel current) {
        String[] values = current == null ? new String[]{"", "", "", "", "", "", "CD001", "0", AppData.todayText()}
                : new String[]{
                    current.getMaDoiTac(), current.getTenDoiTac(), current.getLinhVuc(), current.getSoDienThoai(),
                    current.getEmail(), current.getDiaChi(), current.getMaChienDich(),
                    current.getGiaTriTaiTro() == 0 ? "" : String.format("%.0f", current.getGiaTriTaiTro()),
                    current.getNgayKyKet()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildSponsor(result);
    }

    private SponsorModel buildSponsor(String[] values) {
        String maDoiTac = values[0];
        String tenDoiTac = values[1];
        String maChienDich = values[6];

        if (maDoiTac.isEmpty() || tenDoiTac.isEmpty() || maChienDich.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập mã đối tác, tên đối tác và mã chiến dịch.");
            return null;
        }
        if (!values[3].isEmpty() && !values[3].startsWith("09")) {
            DialogUtils.warning("Số điện thoại nên bắt đầu bằng 09.");
            return null;
        }
        if (!values[4].isEmpty() && !values[4].toLowerCase().endsWith("@gmail.com")) {
            DialogUtils.warning("Email nhà tài trợ phải có dạng @gmail.com.");
            return null;
        }

        try {
            double giaTriTaiTro = values[7].isEmpty() ? 0 : FormatUtils.parseMoney(values[7]);
            return new SponsorModel(maDoiTac, tenDoiTac, values[2], values[3], values[4],
                    values[5], maChienDich, giaTriTaiTro, values[8]);
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
        DetailDialogUtils.showDetails(tableSponsors, "Chi tiết nhà tài trợ - " + sponsor.getMaDoiTac(), new String[][]{
            {"Mã đối tác", sponsor.getMaDoiTac()},
            {"Tên đối tác / nhà tài trợ", sponsor.getTenDoiTac()},
            {"Lĩnh vực", sponsor.getLinhVuc()},
            {"Số điện thoại", sponsor.getSoDienThoai()},
            {"Email", sponsor.getEmail()},
            {"Địa chỉ", sponsor.getDiaChi()},
            {"Mã chiến dịch tài trợ", sponsor.getMaChienDich()},
            {"Tên chiến dịch", campaign == null ? "" : campaign.getTenChienDich()},
            {"Giá trị tài trợ", FormatUtils.money(sponsor.getGiaTriTaiTro())},
            {"Ngày ký kết", sponsor.getNgayKyKet()},
            {"Vai trò", "Đối tác / Nhà tài trợ đồng hành cùng chiến dịch"},
            {"Trạng thái hồ sơ", "Đã ghi nhận trong danh sách tài trợ"}
        });
    }

    private void updateTotal() {
        lblTotalSponsor.setText(FormatUtils.money(AppData.getTotalSponsorAmount()));
    }
}
