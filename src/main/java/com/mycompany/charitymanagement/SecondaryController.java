package com.mycompany.charitymanagement;

import java.io.IOException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SecondaryController {

    @FXML
    private Label lblActivityCount;

    @FXML
    private Label lblParticipantCount;

    @FXML
    private Label lblSponsorCount;

    @FXML
    private Label lblDonationTotal;

    @FXML
    private Label lblFeaturedTitle;

    @FXML
    private Label lblFeaturedStartDate;

    @FXML
    private Label lblFeaturedLocation;

    @FXML
    private Label lblFeaturedParticipants;

    @FXML
    private Label lblFeaturedDonations;

    @FXML
    private Label lblNotice1;

    @FXML
    private Label lblNotice2;

    @FXML
    private Label lblNotice3;

    @FXML
    private Label lblNotice4;

    @FXML
    private void initialize() {
        updateDashboard();
    }

    @FXML
    private void handleHome() {
        updateDashboard();
    }

    @FXML
    private void handleActivityManagement() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleParticipants() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_PARTICIPANTS);
    }

    @FXML
    private void handleSponsors() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_SPONSORS);
    }

    @FXML
    private void handleDonations() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_DONATIONS);
    }

    @FXML
    private void handleOperations() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_OPERATIONS);
    }

    @FXML
    private void handleContent() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_CONTENT);
    }

    @FXML
    private void handleReports() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_REPORTS);
    }

    @FXML
    private void handleCreateActivity() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleAddParticipant() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_PARTICIPANTS);
    }

    @FXML
    private void handleAddSponsor() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_SPONSORS);
    }

    @FXML
    private void handleRecordDonation() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_DONATIONS);
    }

    @FXML
    private void handleManageOperations() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_OPERATIONS);
    }

    @FXML
    private void handleManageContent() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_CONTENT);
    }

    @FXML
    private void handleViewReport() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_REPORTS);
    }

    @FXML
    private void handleViewDetail() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_ACTIVITIES);
    }

    @FXML
    private void handleLogout() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_LOGIN);
    }

    private void updateDashboard() {
        lblActivityCount.setText(String.valueOf(AppData.getActivities().size()));
        lblParticipantCount.setText(String.valueOf(AppData.getParticipants().size()));
        lblSponsorCount.setText(String.valueOf(AppData.getSponsors().size()));
        lblDonationTotal.setText(FormatUtils.money(totalDonations()).replace(" VNĐ", ""));
        updateFeaturedCampaign();
        updateNotices();
    }

    private void updateFeaturedCampaign() {
        if (AppData.getActivities().isEmpty()) {
            lblFeaturedTitle.setText("Chưa có chiến dịch nổi bật");
            lblFeaturedStartDate.setText("Ngày bắt đầu: -");
            lblFeaturedLocation.setText("Địa điểm: -");
            lblFeaturedParticipants.setText("Người tham gia: 0 người");
            lblFeaturedDonations.setText("Đã quyên góp: 0 VNĐ");
            return;
        }

        ActivityModel campaign = AppData.getActivities().get(0);
        String campaignId = campaign.getMaChienDich();
        long participantCount = AppData.getParticipants().stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .count();
        double donationTotal = AppData.getCampaignMoneyTotal(campaignId);

        lblFeaturedTitle.setText("Chương trình \"" + campaign.getTenChienDich() + "\"");
        lblFeaturedStartDate.setText("Ngày bắt đầu: " + emptyText(campaign.getNgayBatDau()));
        lblFeaturedLocation.setText("Địa điểm: " + emptyText(campaign.getDiaDiem()));
        lblFeaturedParticipants.setText("Người tham gia: " + participantCount + " người");
        lblFeaturedDonations.setText("Đã quyên góp: " + FormatUtils.money(donationTotal));
    }

    private void updateNotices() {
        List<Label> noticeLabels = List.of(lblNotice1, lblNotice2, lblNotice3, lblNotice4);
        for (int i = 0; i < noticeLabels.size(); i++) {
            Label label = noticeLabels.get(i);
            if (i < AppData.getContents().size()) {
                SystemRecord record = AppData.getContents().get(i);
                label.setText("• " + record.getTieuDe() + " - " + record.getTrangThai());
            } else {
                label.setText("• Chưa có thông báo mới");
            }
        }
    }

    private double totalDonations() {
        return AppData.getTotalDonationAmount();
    }

    private String emptyText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }
}
