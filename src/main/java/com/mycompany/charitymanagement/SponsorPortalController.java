package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SponsorPortalController {

    private static final String REPLY_MARKER = "REPLY_TO=";

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblPartnerId;
    @FXML
    private Label lblSupportCount;
    @FXML
    private Label lblSupportTotal;
    @FXML
    private Label lblSponsorProfileName;
    @FXML
    private Label lblSponsorProfileRole;
    @FXML
    private Label lblSponsorOrgId;
    @FXML
    private Label lblSponsorOrgField;
    @FXML
    private Label lblSponsorOrgPhone;
    @FXML
    private Label lblSponsorOrgEmail;
    @FXML
    private Label lblSponsorOrgAddress;
    @FXML
    private Label lblSponsorContributionLevel;
    @FXML
    private Label lblSponsorCampaignCount;
    @FXML
    private Label lblSponsorDonationCount;
    @FXML
    private Label lblSponsorTotalCash;
    @FXML
    private Label lblSponsorTopCampaign;
    @FXML
    private Label lblSelectedCampaign;
    @FXML
    private Label lblCampaignGoal;
    @FXML
    private Label lblCampaignProgress;
    @FXML
    private Label lblSelectedCommentThread;
    @FXML
    private ComboBox<String> cboCampaign;
    @FXML
    private ComboBox<String> cboSupportType;
    @FXML
    private TextField txtSupportContent;
    @FXML
    private TextField txtSupportValue;
    @FXML
    private TextField txtCampaignComment;
    @FXML
    private VBox campaignCommentList;
    @FXML
    private VBox campaignPortalBox;
    @FXML
    private VBox overviewSection;
    @FXML
    private VBox profileSection;

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

    @FXML
    private TableView<SponsorModel> tableSponsorPartnerHistory;
    @FXML
    private TableColumn<SponsorModel, String> colSponsorPartnerCampaign;
    @FXML
    private TableColumn<SponsorModel, String> colSponsorPartnerField;
    @FXML
    private TableColumn<SponsorModel, String> colSponsorPartnerDate;
    @FXML
    private TableColumn<SponsorModel, String> colSponsorPartnerValue;

    @FXML
    private TableView<DonationModel> tableSponsorContributionHistory;
    @FXML
    private TableColumn<DonationModel, String> colSponsorHistoryCampaign;
    @FXML
    private TableColumn<DonationModel, String> colSponsorHistoryDate;
    @FXML
    private TableColumn<DonationModel, String> colSponsorHistoryType;
    @FXML
    private TableColumn<DonationModel, String> colSponsorHistoryContent;
    @FXML
    private TableColumn<DonationModel, String> colSponsorHistoryValue;
    @FXML
    private TableColumn<DonationModel, String> colSponsorHistoryStatus;

    private final ObservableList<DonationModel> sponsorSupport = FXCollections.observableArrayList();
    private final ObservableList<SponsorModel> sponsorPartnerHistory = FXCollections.observableArrayList();
    private final ObservableList<DonationModel> sponsorContributionHistory = FXCollections.observableArrayList();
    private UserAccount currentUser;

    @FXML
    private void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        colMaChienDich.setVisible(false);
        colMaChienDich.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaChienDich()));
        colTenChienDich.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colMucTieu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMucTieuTienText()));
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));

        colMaQuyenGop.setVisible(false);
        colMaQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaQuyenGop()));
        colHoatDong.setText("Chiến dịch");
        colHoatDong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colNgayQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayQuyenGop()));
        colHinhThuc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHinhThuc()));
        colNoiDungQuyenGop.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNoiDungQuyenGop()));
        colSoTien.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoTienText()));
        colSponsorPartnerCampaign.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colSponsorPartnerField.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLinhVuc()));
        colSponsorPartnerDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayKyKet()));
        colSponsorPartnerValue.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGiaTriTaiTroText()));
        colSponsorHistoryCampaign.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colSponsorHistoryDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayQuyenGop()));
        colSponsorHistoryType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHinhThuc()));
        colSponsorHistoryContent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNoiDungQuyenGop()));
        colSponsorHistoryValue.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoTienText()));
        colSponsorHistoryStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThaiXuLy()));

        tableCampaigns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSupport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSponsorPartnerHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSponsorContributionHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCampaigns.setFixedCellSize(32.0);
        tableSupport.setFixedCellSize(32.0);
        tableSponsorPartnerHistory.setFixedCellSize(32.0);
        tableSponsorContributionHistory.setFixedCellSize(32.0);
        tableCampaigns.setItems(AppData.getActivities());
        tableSupport.setItems(sponsorSupport);
        tableSponsorPartnerHistory.setItems(sponsorPartnerHistory);
        tableSponsorContributionHistory.setItems(sponsorContributionHistory);

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
        AppData.getContents().addListener((ListChangeListener<SystemRecord>) change -> {
            renderCampaignComments();
            renderCampaignPortal();
        });
        AppData.getActivities().addListener((ListChangeListener<ActivityModel>) change -> {
            refreshCampaignChoices();
            renderCampaignPortal();
        });

        if (!cboCampaign.getItems().isEmpty()) {
            cboCampaign.setValue(cboCampaign.getItems().get(0));
            updateCampaignSummary(AppData.findCampaign(extractCampaignId(cboCampaign.getValue())));
        }
        refreshView();
        renderCampaignPortal();
        showSection(overviewSection);
    }

    @FXML
    private void handleOverview() {
        refreshView();
        renderCampaignPortal();
        showSection(overviewSection);
    }

    @FXML
    private void handleShowProfile() {
        refreshView();
        showSection(profileSection);
    }

    @FXML
    private void handleCampaigns() {
        showSection(overviewSection);
        renderCampaignPortal();
        if (campaignPortalBox != null) {
            campaignPortalBox.requestFocus();
        } else {
            tableCampaigns.requestFocus();
        }
    }

    @FXML
    private void handleJoinCampaign() {
        ActivityModel campaign = selectedCampaign();
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch muốn tham gia tài trợ.");
            return;
        }

        String type = value(cboSupportType);
        String content = value(txtSupportContent);
        String valueText = value(txtSupportValue);
        if (content.isEmpty() && valueText.isEmpty()) {
            type = "Vật phẩm";
            content = "Đăng ký đồng hành chiến dịch " + campaign.getTenChienDich();
        } else if (content.isEmpty()) {
            content = "Đăng ký tài trợ chiến dịch " + campaign.getTenChienDich();
        }
        if (type.isEmpty()) {
            type = valueText.isEmpty() ? "Vật phẩm" : "Tiền";
        }

        try {
            double amount = valueText.isEmpty() ? 0 : FormatUtils.parseMoney(valueText);
            String error = BusinessService.recordDonation(currentUser, campaign.getMaChienDich(), type, content, amount);
            if (error != null) {
                DialogUtils.warning(error);
                return;
            }
            txtSupportContent.clear();
            txtSupportValue.clear();
            refreshView();
            updateCampaignSummary(campaign);
            renderCampaignPortal();
            DialogUtils.info("Đã gửi đăng ký tham gia tài trợ chiến dịch.");
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Giá trị tài trợ không hợp lệ.");
        }
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
            renderCampaignPortal();
            DialogUtils.info("Đã gửi đề xuất tài trợ để quản lý ghi nhận.");
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Giá trị tài trợ không hợp lệ.");
        }
    }

    @FXML
    private void handleSendCampaignComment() {
        ActivityModel campaign = selectedCampaign();
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch trước khi bình luận.");
            return;
        }
        String text = value(txtCampaignComment);
        if (text.isEmpty()) {
            DialogUtils.warning("Vui lòng nhập nội dung bình luận.");
            return;
        }

        AppData.getContents().add(new SystemRecord(
                "BinhLuan",
                AppData.nextContentId("BL"),
                campaign.getMaChienDich(),
                currentUser.getUsername(),
                "Bình luận của nhà tài trợ",
                text,
                AppData.todayText(),
                "",
                "Chờ duyệt",
                currentUser.getUsername(),
                "ADMIN",
                "Tạo từ cổng nhà tài trợ"
        ));
        txtCampaignComment.clear();
        renderCampaignComments();
        DialogUtils.info("Đã gửi bình luận. Admin sẽ thấy trong phần Nội dung.");
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
        renderCampaignPortal();
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
        refreshProfile();
        double total = totalSponsorCash();
        lblSupportTotal.setText(FormatUtils.money(total));
    }

    private void showSection(VBox activeSection) {
        VBox[] sections = {overviewSection, profileSection};
        for (VBox section : sections) {
            if (section != null) {
                boolean active = section == activeSection;
                section.setVisible(active);
                section.setManaged(active);
            }
        }
    }

    private void refreshProfile() {
        SponsorModel sponsor = findSponsorProfile();
        sponsorPartnerHistory.setAll(AppData.getSponsors().filtered(this::isCurrentSponsorRecord));
        sponsorContributionHistory.setAll(AppData.getDonations().filtered(this::isCurrentSponsorDonation));

        String sponsorName = sponsor == null ? currentUser.getDisplayName() : sponsor.getTenDoiTac();
        lblSponsorProfileName.setText(sponsorName);
        lblSponsorProfileRole.setText("Nhà tài trợ");
        lblSponsorOrgId.setText(sponsor == null ? emptyAsDash(currentUser.getLinkedId()) : sponsor.getMaDoiTac());
        lblSponsorOrgField.setText(sponsor == null ? "-" : emptyAsDash(sponsor.getLinhVuc()));
        lblSponsorOrgPhone.setText(sponsor == null ? "-" : emptyAsDash(sponsor.getSoDienThoai()));
        lblSponsorOrgEmail.setText(sponsor == null ? "-" : emptyAsDash(sponsor.getEmail()));
        lblSponsorOrgAddress.setText(sponsor == null ? "-" : emptyAsDash(sponsor.getDiaChi()));

        int campaignCount = sponsorCampaignCount();
        int donationCount = sponsorPartnerHistory.size() + sponsorContributionHistory.size();
        double totalCash = totalSponsorCash();
        lblSponsorCampaignCount.setText(String.valueOf(campaignCount));
        lblSponsorDonationCount.setText(String.valueOf(donationCount));
        lblSponsorTotalCash.setText(FormatUtils.money(totalCash));
        lblSponsorContributionLevel.setText(sponsorLevel(totalCash, campaignCount));
        lblSponsorTopCampaign.setText(topSponsorCampaign());
    }

    private SponsorModel findSponsorProfile() {
        for (SponsorModel sponsor : AppData.getSponsors()) {
            if (isCurrentSponsorRecord(sponsor)) {
                return sponsor;
            }
        }
        return null;
    }

    private boolean isCurrentSponsorRecord(SponsorModel sponsor) {
        return sponsor.getMaDoiTac().equalsIgnoreCase(currentUser.getLinkedId())
                || sponsor.getTenDoiTac().equalsIgnoreCase(currentUser.getDisplayName());
    }

    private boolean isCurrentSponsorDonation(DonationModel donation) {
        return donation.getNguoiQuyenGop().equalsIgnoreCase(currentUser.getDisplayName());
    }

    private double totalSponsorCash() {
        double sponsorTotal = sponsorPartnerHistory.stream()
                .mapToDouble(SponsorModel::getGiaTriTaiTro)
                .sum();
        double donationTotal = sponsorContributionHistory.stream()
                .mapToDouble(DonationModel::getSoTien)
                .sum();
        return sponsorTotal + donationTotal;
    }

    private int sponsorCampaignCount() {
        return (int) java.util.stream.Stream.concat(
                sponsorPartnerHistory.stream().map(SponsorModel::getMaChienDich),
                sponsorContributionHistory.stream().map(DonationModel::getHoatDong)
        )
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .count();
    }

    private String sponsorLevel(double totalCash, int campaignCount) {
        if (totalCash >= 25000000 || campaignCount >= 4) {
            return "Đối tác chiến lược";
        }
        if (totalCash >= 10000000 || campaignCount >= 2) {
            return "Tài trợ tích cực";
        }
        if (totalCash > 0 || campaignCount > 0) {
            return "Đồng hành";
        }
        return "Mới";
    }

    private String topSponsorCampaign() {
        String bestCampaignId = "";
        double bestValue = -1;
        for (SponsorModel sponsor : sponsorPartnerHistory) {
            if (sponsor.getGiaTriTaiTro() > bestValue) {
                bestValue = sponsor.getGiaTriTaiTro();
                bestCampaignId = sponsor.getMaChienDich();
            }
        }
        for (DonationModel donation : sponsorContributionHistory) {
            if (donation.getSoTien() > bestValue) {
                bestValue = donation.getSoTien();
                bestCampaignId = donation.getHoatDong();
            }
        }
        ActivityModel campaign = AppData.findCampaign(bestCampaignId);
        return campaign == null ? "-" : campaign.getTenChienDich();
    }

    private String emptyAsDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
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
            setLabelText(lblSelectedCommentThread, "Chọn chiến dịch để xem bình luận");
            renderCampaignComments();
            return;
        }
        lblSelectedCampaign.setText(campaign.getTenChienDich());
        lblCampaignGoal.setText(FormatUtils.money(campaign.getMucTieuTien()));
        lblCampaignProgress.setText(FormatUtils.money(AppData.getCampaignMoneyTotal(campaign.getMaChienDich())));
        setLabelText(lblSelectedCommentThread, campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
        renderCampaignComments();
    }

    private void refreshCampaignChoices() {
        String current = cboCampaign.getValue();
        cboCampaign.setItems(buildCampaignChoices());
        if (cboCampaign.getItems().contains(current)) {
            cboCampaign.setValue(current);
        } else if (!cboCampaign.getItems().isEmpty()) {
            cboCampaign.setValue(cboCampaign.getItems().get(0));
        }
    }

    private void showCampaignDetail(ActivityModel campaign) {
        if (campaign == null) {
            DialogUtils.warning("Vui lòng chọn chiến dịch cần xem.");
            return;
        }
        cboCampaign.setValue(campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
        updateCampaignSummary(campaign);
        CampaignDialogUtils.showCampaignDialog(campaignPortalBox, campaign, currentUser,
                "Tham gia tài trợ",
                selected -> {
                    cboCampaign.setValue(selected.getMaChienDich() + " - " + selected.getTenChienDich());
                    handleJoinCampaign();
                },
                "Bình luận của nhà tài trợ",
                "Tạo từ cổng nhà tài trợ",
                () -> {
                    refreshView();
                    renderCampaignComments();
                    renderCampaignPortal();
                });
    }

    private ActivityModel selectedCampaign() {
        return AppData.findCampaign(extractCampaignId(cboCampaign.getValue()));
    }

    private void renderCampaignPortal() {
        if (campaignPortalBox == null) {
            return;
        }
        campaignPortalBox.getChildren().clear();
        if (AppData.getActivities().isEmpty()) {
            Label empty = new Label("Chưa có chiến dịch nào.");
            empty.getStyleClass().add("muted-text");
            campaignPortalBox.getChildren().add(empty);
            return;
        }

        ActivityModel featured = selectedCampaign();
        if (featured == null) {
            featured = AppData.getActivities().get(0);
        }
        campaignPortalBox.getChildren().add(featuredCampaignCard(featured));

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label title = new Label("Hoạt động sắp diễn ra");
        title.getStyleClass().add("portal-section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button viewAll = new Button("Xem tất cả");
        viewAll.getStyleClass().add("link-button");
        header.getChildren().addAll(title, spacer, viewAll);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        int index = 0;
        for (ActivityModel activity : AppData.getActivities()) {
            grid.add(campaignCard(activity), index % 2, index / 2);
            index++;
        }

        VBox section = new VBox(12, header, grid);
        section.getStyleClass().add("portal-section");
        campaignPortalBox.getChildren().add(section);
    }

    private HBox featuredCampaignCard(ActivityModel campaign) {
        HBox card = new HBox(18);
        card.getStyleClass().add("featured-news-card");
        card.setOnMouseClicked(event -> showCampaignDetail(campaign));

        StackPane visual = campaignVisual("NỔI BẬT", "Tình nguyện kết nối", "featured-visual");
        visual.setOnMouseClicked(event -> {
            event.consume();
            showCampaignDetail(campaign);
        });

        VBox body = new VBox(10);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label category = new Label("CHIẾN DỊCH");
        category.getStyleClass().add("content-category");
        Label title = new Label(campaign.getTenChienDich());
        title.getStyleClass().add("featured-title");
        title.setWrapText(true);
        Label summary = new Label(campaign.getMoTa());
        summary.getStyleClass().add("featured-summary");
        summary.setWrapText(true);
        HBox meta = new HBox(18,
                muted(campaign.getNgayBatDau() + " - " + campaign.getNgayKetThuc()),
                muted(campaign.getDiaDiem()),
                muted(FormatUtils.money(campaign.getMucTieuTien()))
        );
        HBox actions = new HBox(14);
        Button joinButton = new Button("Tham gia tài trợ");
        joinButton.getStyleClass().add("primary-button");
        joinButton.setOnAction(event -> {
            event.consume();
            cboCampaign.setValue(campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
            handleJoinCampaign();
        });
        Button commentButton = new Button("Bình luận");
        commentButton.getStyleClass().add("quick-button");
        commentButton.setOnAction(event -> {
            event.consume();
            showCampaignDetail(campaign);
        });
        actions.getChildren().addAll(joinButton, commentButton,
                muted(campaignCommentCount(campaign.getMaChienDich()) + " bình luận"));
        body.getChildren().addAll(category, title, summary, meta, actions);
        card.getChildren().addAll(visual, body);
        return card;
    }

    private HBox campaignCard(ActivityModel campaign) {
        HBox card = new HBox(12);
        card.getStyleClass().add("upcoming-card");
        card.setOnMouseClicked(event -> showCampaignDetail(campaign));

        StackPane visual = campaignVisual("", iconForCampaign(campaign), "activity-thumb");
        visual.setOnMouseClicked(event -> {
            event.consume();
            showCampaignDetail(campaign);
        });

        VBox body = new VBox(5);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label category = new Label(categoryForCampaign(campaign));
        category.getStyleClass().add("content-category");
        Label title = new Label(campaign.getTenChienDich());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        Label summary = new Label(campaign.getMoTa());
        summary.getStyleClass().add("card-summary");
        summary.setWrapText(true);
        Label meta = new Label(campaign.getNgayBatDau() + "     " + FormatUtils.money(campaign.getMucTieuTien()));
        meta.getStyleClass().add("muted-text");
        body.getChildren().addAll(category, title, summary, meta);
        card.getChildren().addAll(visual, body);
        return card;
    }

    private StackPane campaignVisual(String badgeText, String text, String styleClass) {
        StackPane visual = new StackPane();
        visual.getStyleClass().add(styleClass);
        VBox box = new VBox();
        box.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        box.setSpacing("featured-visual".equals(styleClass) ? 90 : 0);
        if (badgeText != null && !badgeText.isBlank()) {
            Label badge = new Label(badgeText);
            badge.getStyleClass().add("featured-badge");
            box.getChildren().add(badge);
        }
        Label label = new Label(text);
        label.getStyleClass().add("featured-visual".equals(styleClass)
                ? "featured-image-title" : "activity-thumb-icon");
        box.getChildren().add(label);
        visual.getChildren().add(box);
        return visual;
    }

    private long campaignCommentCount(String campaignId) {
        return AppData.getContents().stream()
                .filter(record -> groupCode(record).contains("binhluan"))
                .filter(record -> !isReplyRecord(record))
                .filter(record -> campaignId.equalsIgnoreCase(campaignId(record)))
                .count();
    }

    private String categoryForCampaign(ActivityModel activity) {
        String text = normalized(activity.getTenChienDich() + " " + activity.getMoTa());
        if (text.contains("kham") || text.contains("benh") || text.contains("y te")) {
            return "SỨC KHỎE";
        }
        if (text.contains("sach") || text.contains("truong") || text.contains("hoc")) {
            return "GIÁO DỤC";
        }
        if (text.contains("moi truong") || text.contains("xanh") || text.contains("nuoc sach")) {
            return "MÔI TRƯỜNG";
        }
        return "CỘNG ĐỒNG";
    }

    private String iconForCampaign(ActivityModel activity) {
        String category = categoryForCampaign(activity);
        if ("SỨC KHỎE".equals(category)) {
            return "+";
        }
        if ("GIÁO DỤC".equals(category)) {
            return "▤";
        }
        if ("MÔI TRƯỜNG".equals(category)) {
            return "♧";
        }
        return "♥";
    }

    private Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private void renderCampaignComments() {
        if (campaignCommentList == null) {
            return;
        }
        campaignCommentList.getChildren().clear();
        ActivityModel campaign = selectedCampaign();
        if (campaign == null) {
            Label empty = new Label("Chưa có chiến dịch để hiển thị bình luận.");
            empty.getStyleClass().add("muted-text");
            campaignCommentList.getChildren().add(empty);
            return;
        }

        int count = 0;
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan")
                    || isReplyRecord(record)
                    || !campaign.getMaChienDich().equalsIgnoreCase(campaignId(record))) {
                continue;
            }
            campaignCommentList.getChildren().add(commentCard(record));
            count++;
        }
        if (count == 0) {
            Label empty = new Label("Chưa có bình luận cho chiến dịch này.");
            empty.getStyleClass().add("muted-text");
            campaignCommentList.getChildren().add(empty);
        }
    }

    private VBox commentCard(SystemRecord record) {
        VBox wrapper = new VBox(6);
        wrapper.getStyleClass().add("comment-card");

        HBox row = new HBox(10);
        row.getStyleClass().add("comment-row");
        Label avatar = new Label(initials(record.getTenLienKet()));
        avatar.getStyleClass().add("comment-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label author = new Label(record.getTenLienKet() + "  ·  " + record.getNgay());
        author.getStyleClass().add("comment-author");
        Label content = new Label(record.getNoiDung());
        content.setWrapText(true);
        content.getStyleClass().add("comment-text");
        Label status = new Label(record.getTrangThai());
        status.getStyleClass().add("muted-text");

        VBox replies = new VBox(6);
        replies.getStyleClass().add("reply-list");
        renderReplies(record, replies);

        body.getChildren().addAll(author, content, status, replies);
        row.getChildren().addAll(avatar, body);
        wrapper.getChildren().add(row);
        return wrapper;
    }

    private void renderReplies(SystemRecord parent, VBox replies) {
        for (SystemRecord record : AppData.getContents()) {
            if (!groupCode(record).contains("binhluan") || !isReplyTo(record, parent)) {
                continue;
            }
            replies.getChildren().add(replyRow(record));
        }
    }

    private HBox replyRow(SystemRecord reply) {
        HBox row = new HBox(8);
        row.getStyleClass().add("reply-row");

        Label avatar = new Label(initials(reply.getTenLienKet()));
        avatar.getStyleClass().add("reply-avatar");

        VBox body = new VBox(2);
        HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);
        Label author = new Label(reply.getTenLienKet() + "  ·  " + reply.getNgay());
        author.getStyleClass().add("comment-author");
        Label content = new Label(reply.getNoiDung());
        content.setWrapText(true);
        content.getStyleClass().add("comment-text");
        body.getChildren().addAll(author, content);

        row.getChildren().addAll(avatar, body);
        return row;
    }

    private boolean isReplyRecord(SystemRecord record) {
        return replyParentId(record).length() > 0;
    }

    private boolean isReplyTo(SystemRecord reply, SystemRecord parent) {
        return parent.getMaChinh().equalsIgnoreCase(replyParentId(reply));
    }

    private String replyParentId(SystemRecord record) {
        String note = record.getGhiChu() == null ? "" : record.getGhiChu();
        int start = note.indexOf(REPLY_MARKER);
        if (start < 0) {
            return "";
        }
        int idStart = start + REPLY_MARKER.length();
        int idEnd = idStart;
        while (idEnd < note.length() && Character.isLetterOrDigit(note.charAt(idEnd))) {
            idEnd++;
        }
        return note.substring(idStart, idEnd);
    }

    private String campaignId(SystemRecord record) {
        if (record.getMaChienDich() != null && !record.getMaChienDich().isBlank()) {
            return record.getMaChienDich();
        }
        if (record.getMaLienKet() != null && record.getMaLienKet().toUpperCase().startsWith("CD")) {
            return record.getMaLienKet();
        }
        return "";
    }

    private String groupCode(SystemRecord record) {
        return record.getNhomBang() == null ? "" : normalized(record.getNhomBang());
    }

    private String initials(String value) {
        if (value == null || value.isBlank()) {
            return "?";
        }
        String[] parts = value.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length == 1 ? "" : parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    private String normalized(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }
}
