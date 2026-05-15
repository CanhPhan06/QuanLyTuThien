package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private TextField txtCampaignId;
    @FXML
    private TextField txtSupportType;
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

        colMaChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colTenChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colMucTieu.setCellValueFactory(new PropertyValueFactory<>("mucTieuTienText"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colMaQuyenGop.setCellValueFactory(new PropertyValueFactory<>("maQuyenGop"));
        colHoatDong.setCellValueFactory(new PropertyValueFactory<>("hoatDong"));
        colNgayQuyenGop.setCellValueFactory(new PropertyValueFactory<>("ngayQuyenGop"));
        colHinhThuc.setCellValueFactory(new PropertyValueFactory<>("hinhThuc"));
        colNoiDungQuyenGop.setCellValueFactory(new PropertyValueFactory<>("noiDungQuyenGop"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTienText"));

        tableCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSupport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCampaigns.setItems(AppData.getActivities());
        tableSupport.setItems(sponsorSupport);
        tableCampaigns.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected != null) {
                        txtCampaignId.setText(selected.getMaChienDich());
                    }
                }
        );

        txtSupportType.setText("Tài trợ tiền");
        refreshView();
    }

    @FXML
    private void handleCampaigns() throws IOException {
        App.setRoot("activities");
    }

    @FXML
    private void handleCreateSupport() {
        String campaignId = value(txtCampaignId);
        String type = value(txtSupportType);
        String content = value(txtSupportContent);
        String valueText = value(txtSupportValue);

        if (campaignId.isEmpty() || type.isEmpty() || content.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập mã chiến dịch, hình thức và nội dung tài trợ.");
            return;
        }
        if (AppData.findCampaign(campaignId) == null) {
            DialogUtils.warning("Mã chiến dịch không tồn tại.");
            return;
        }

        try {
            double amount = valueText.isEmpty() ? 0 : FormatUtils.parseMoney(valueText);
            AppData.getDonations().add(new DonationModel(
                    AppData.nextDonationId(),
                    userGmail(),
                    campaignId,
                    AppData.todayText(),
                    type,
                    content,
                    amount
            ));
            txtSupportContent.clear();
            txtSupportValue.clear();
            refreshView();
            DialogUtils.info("Đã gửi đề xuất tài trợ để quản lý ghi nhận.");
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Giá trị tài trợ không hợp lệ.");
        }
    }

    @FXML
    private void handleRefresh() {
        refreshView();
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.clear();
        App.setRoot("primary");
    }

    private void refreshView() {
        lblWelcome.setText("Xin chào, " + currentUser.getDisplayName());
        lblPartnerId.setText(currentUser.getLinkedId());

        sponsorSupport.setAll(AppData.getDonations().filtered(item ->
                item.getNguoiQuyenGop().equalsIgnoreCase(userGmail())
        ));
        lblSupportCount.setText(String.valueOf(sponsorSupport.size()));
        double total = sponsorSupport.stream()
                .mapToDouble(DonationModel::getSoTien)
                .sum();
        lblSupportTotal.setText(FormatUtils.money(total));
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String userGmail() {
        return currentUser.getUsername().toLowerCase() + "@gmail.com";
    }
}
