package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ReportsController {

    @FXML
    private Label lblTotalActivities;
    @FXML
    private Label lblTotalParticipants;
    @FXML
    private Label lblTotalSponsors;
    @FXML
    private Label lblTotalDonations;
    @FXML
    private Label lblSponsorAmount;
    @FXML
    private Label lblDonationAmount;
    @FXML
    private Label lblTotalFund;
    @FXML
    private Label lblOperationCount;
    @FXML
    private Label lblContentCount;
    @FXML
    private TextArea txtReport;

    @FXML
    private void initialize() {
        handleRefreshReport();
    }

    @FXML
    private void handleRefreshReport() {
        double sponsorAmount = AppData.getTotalSponsorAmount();
        double donationAmount = AppData.getTotalDonationAmount();
        double totalFund = sponsorAmount + donationAmount;

        lblTotalActivities.setText(String.valueOf(AppData.getActivities().size()));
        lblTotalParticipants.setText(String.valueOf(AppData.getParticipants().size()));
        lblTotalSponsors.setText(String.valueOf(AppData.getSponsors().size()));
        lblTotalDonations.setText(String.valueOf(AppData.getDonations().size()));
        lblSponsorAmount.setText(FormatUtils.money(sponsorAmount));
        lblDonationAmount.setText(FormatUtils.money(donationAmount));
        lblTotalFund.setText(FormatUtils.money(totalFund));
        lblOperationCount.setText(String.valueOf(AppData.getOperations().size()));
        lblContentCount.setText(String.valueOf(AppData.getContents().size()));

        txtReport.setText(buildReport(sponsorAmount, donationAmount, totalFund));
    }

    @FXML
    private void handleExportReport() {
        ExportUtils.exportTextToCsv("Xuất báo cáo tổng hợp", "bao-cao-tong-hop.csv", txtReport.getText());
    }

    @FXML
    private void handleBackHome() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_DASHBOARD);
    }

    @FXML
    private void handleActivities() throws IOException {
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
    private void handleLogout() throws IOException {
        NavigationService.navigateTo(NavigationService.VIEW_LOGIN);
    }

    private String buildReport(double sponsorAmount, double donationAmount, double totalFund) {
        StringBuilder builder = new StringBuilder();
        builder.append("BÁO CÁO TỔNG HỢP\n\n");
        builder.append("Chiến dịch: ").append(AppData.getActivities().size()).append('\n');
        builder.append("Sinh viên/TNV tham gia: ").append(AppData.getParticipants().size()).append('\n');
        builder.append("Đối tác tài trợ: ").append(AppData.getSponsors().size()).append('\n');
        builder.append("Phiếu/khoản quyên góp: ").append(AppData.getDonations().size()).append('\n');
        builder.append("Bản ghi vận hành: ").append(AppData.getOperations().size()).append('\n');
        builder.append("Bản ghi nội dung - hệ thống: ").append(AppData.getContents().size()).append("\n\n");
        builder.append("Tổng tiền tài trợ: ").append(FormatUtils.money(sponsorAmount)).append('\n');
        builder.append("Tổng giá trị quyên góp bằng tiền: ").append(FormatUtils.money(donationAmount)).append('\n');
        builder.append("Tổng nguồn lực tiền tệ ghi nhận: ").append(FormatUtils.money(totalFund)).append("\n\n");
        builder.append("Ghi chú: dữ liệu hiện đang là dữ liệu mẫu trong bộ nhớ để phục vụ demo.");
        return builder.toString();
    }
}
