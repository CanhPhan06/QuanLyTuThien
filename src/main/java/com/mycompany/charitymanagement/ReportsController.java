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
