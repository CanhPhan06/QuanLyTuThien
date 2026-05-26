package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
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
        AppData.getContents().addListener((ListChangeListener<SystemRecord>) change -> renderCampaignComments());

        if (!cboCampaign.getItems().isEmpty()) {
            cboCampaign.setValue(cboCampaign.getItems().get(0));
            updateCampaignSummary(AppData.findCampaign(extractCampaignId(cboCampaign.getValue())));
        }
        refreshView();
    }

    @FXML
    private void handleCampaigns() {
        tableCampaigns.requestFocus();
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

    private ActivityModel selectedCampaign() {
        return AppData.findCampaign(extractCampaignId(cboCampaign.getValue()));
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
