package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class SponsorPortalController {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblPartnerId;
    @FXML
    private Label lblSupportCount;
    @FXML
    private Label lblSupportTotal;
    @FXML
    private Label lblSelectedCampaign;
    @FXML
    private Label lblCampaignGoal;
    @FXML
    private Label lblCampaignProgress;
    @FXML
    private ComboBox<String> cboCampaign;
    @FXML
    private ComboBox<String> cboSupportType;
    @FXML
    private TextField txtSupportContent;
    @FXML
    private TextField txtSupportValue;

    @FXML
    private TableView<ActivityModel> tableCampaigns;
    @FXML
    private TableColumn<ActivityModel, String> colMaChienDich;
    @FXML
    private TableColumn<ActivityModel, String> colTenChienDich;
    @FXML
    private TableColumn<ActivityModel, String> colMucTieu;
    @FXML
    private TableColumn<ActivityModel, String> colTrangThai;

    @FXML
    private TableView<DonationModel> tableSupport;
    @FXML
    private TableColumn<DonationModel, String> colMaQuyenGop;
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

    private final ObservableList<DonationModel> sponsorSupport = FXCollections.observableArrayList();
    private UserAccount currentUser;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        colMaChienDich.setVisible(false);
        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colTenChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colMucTieu.setCellValueFactory(new PropertyValueFactory<>("mucTieuTienText"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colMaQuyenGop.setVisible(false);
        colMaQuyenGop.setCellValueFactory(new PropertyValueFactory<>("maQuyenGop"));
        colHoatDong.setText("Chiến dịch");
        colHoatDong.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNgayQuyenGop.setCellValueFactory(new PropertyValueFactory<>("ngayQuyenGop"));
        colHinhThuc.setCellValueFactory(new PropertyValueFactory<>("hinhThuc"));
        colNoiDungQuyenGop.setCellValueFactory(new PropertyValueFactory<>("noiDungQuyenGop"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTienText"));

        tableCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSupport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCampaigns.setFixedCellSize(32.0);
        tableSupport.setFixedCellSize(32.0);
        tableCampaigns.setItems(AppData.getActivities());
        tableSupport.setItems(sponsorSupport);

        cboCampaign.setItems(buildCampaignChoices());
        cboSupportType.setItems(FXCollections.observableArrayList(
                "Tiền",
                "Vật phẩm",
                "Vật tư",
                "Vật dụng",
                "Tài trợ tiền",
                "Tài trợ vật phẩm"
        ));
        cboSupportType.setValue("Tiền");

        tableCampaigns.setRowFactory(table -> {
            TableRow<ActivityModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    ActivityModel selected = row.getItem();
                    cboCampaign.setValue(selected.getMaChienDich() + " - " + selected.getTenChienDich());
                    updateCampaignSummary(selected);
                    if (event.getClickCount() == 2) {
                        showCampaignDetail(selected);
                    }
                }
            });
            return row;
        });
        cboCampaign.valueProperty().addListener((observable, oldValue, value) ->
                updateCampaignSummary(AppData.findCampaign(extractCampaignId(value))));

        if (!cboCampaign.getItems().isEmpty()) {
            cboCampaign.setValue(cboCampaign.getItems().get(0));
            updateCampaignSummary(AppData.findCampaign(extractCampaignId(cboCampaign.getValue())));
        }
        refreshView();
    }

    @FXML
    private void handleCampaigns() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleCreateSupport() {
        String campaignId = extractCampaignId(cboCampaign.getValue());
        String type = value(cboSupportType);
        String content = value(txtSupportContent);
        String valueText = value(txtSupportValue);

        if (campaignId.isEmpty() || type.isEmpty() || content.isEmpty()) {
            DialogUtils.warning("Vui lòng chọn chiến dịch, hình thức quyên góp và nhập nội dung tài trợ.");
            return;
        }
        if (AppData.findCampaign(campaignId) == null) {
            DialogUtils.warning("Chiến dịch không tồn tại.");
            return;
        }

        try {
            double amount = valueText.isEmpty() ? 0 : FormatUtils.parseMoney(valueText);
            String error = BusinessService.recordDonation(currentUser, campaignId, type, content, amount);
            if (error != null) {
                DialogUtils.warning(error);
                return;
            }
            txtSupportContent.clear();
            txtSupportValue.clear();
            refreshView();
            updateCampaignSummary(AppData.findCampaign(campaignId));
            DialogUtils.info("Đã gửi đề xuất tài trợ để quản lý ghi nhận.");
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Giá trị tài trợ không hợp lệ.");
        }
    }

    @FXML
    private void handleViewCampaignDetail() {
        ActivityModel campaign = AppData.findCampaign(extractCampaignId(cboCampaign.getValue()));
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần xem.");
            return;
        }
        showCampaignDetail(campaign);
    }

    @FXML
    private void handleExportSupport() {
        ExportUtils.exportTableToCsv(tableSupport, "Xuất lịch sử tài trợ", "lich-su-tai-tro.csv");
    }

    @FXML
    private void handleRefresh() {
        refreshView();
    }

    @FXML
    private void handleLogout() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_LOGIN);
    }

    private void refreshView() {
        lblWelcome.setText("Xin chào, " + currentUser.getDisplayName());
        lblPartnerId.setText(currentUser.getDisplayName());

        sponsorSupport.setAll(AppData.getDonations().filtered(item ->
                item.getNguoiQuyenGop().equalsIgnoreCase(currentUser.getDisplayName())
        ));
        lblSupportCount.setText(String.valueOf(sponsorSupport.size()));
        double total = sponsorSupport.stream()
                .mapToDouble(DonationModel::getSoTien)
                .sum();
        lblSupportTotal.setText(FormatUtils.money(total));
    }

    private ObservableList<String> buildCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList();
        for (ActivityModel activity : AppData.getActivities()) {
            choices.add(activity.getMaChienDich() + " - " + activity.getTenChienDich());
        }
        return choices;
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String value(ComboBox<String> comboBox) {
        return comboBox.getValue() == null ? "" : comboBox.getValue().trim();
    }

    private String extractCampaignId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.split(" - ", 2)[0].trim();
    }

    private void updateCampaignSummary(ActivityModel campaign) {
        if (campaign == null) {
            lblSelectedCampaign.setText("Chưa chọn chiến dịch");
            lblCampaignGoal.setText("-");
            lblCampaignProgress.setText("-");
            return;
        }
        lblSelectedCampaign.setText(campaign.getTenChienDich());
        lblCampaignGoal.setText(FormatUtils.money(campaign.getMucTieuTien()));
        lblCampaignProgress.setText(FormatUtils.money(AppData.getCampaignMoneyTotal(campaign.getMaChienDich())));
    }

    private void showCampaignDetail(ActivityModel campaign) {
        DetailDialogUtils.showDetails(tableCampaigns, "Chi tiết chiến dịch - " + campaign.getMaChienDich(), new String[][]{
            {"Tên chiến dịch", campaign.getTenChienDich()},
            {"Mô tả", campaign.getMoTa()},
            {"Địa điểm", campaign.getDiaDiem()},
            {"Thời gian", campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc()},
            {"Mục tiêu", FormatUtils.money(campaign.getMucTieuTien())},
            {"Đã ghi nhận", FormatUtils.money(AppData.getCampaignMoneyTotal(campaign.getMaChienDich()))},
            {"Số TNV tham gia", String.valueOf(AppData.getCampaignParticipantCount(campaign.getMaChienDich()))},
            {"Trạng thái", campaign.getTrangThai()}
        });
    }

}
