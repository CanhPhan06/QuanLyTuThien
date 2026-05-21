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
    private Label lblTotalExpense;
    @FXML
    private Label lblNetFund;
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
        double totalExpense = AppData.getTotalExpenseAmount();
        double netFund = totalFund - totalExpense;

        lblTotalFund.setText(FormatUtils.money(totalFund));
        lblTotalExpense.setText(FormatUtils.money(totalExpense));
        lblNetFund.setText(FormatUtils.money(netFund));
        lblOperationCount.setText(String.valueOf(AppData.getOperations().size()));
        lblContentCount.setText(String.valueOf(AppData.getContents().size()));

        txtReport.setText(buildReport(sponsorAmount, donationAmount, totalFund, totalExpense, netFund));
    }

    @FXML
    private void handleExportReport() {
        ExportUtils.exportTextToCsv("Xuất báo cáo tổng hợp", "bao-cao-tong-hop.csv", txtReport.getText());
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
    private void handleTraining() throws IOException {
        App.setRoot("training");
    }

    @FXML
    private void handleScreening() throws IOException {
        App.setRoot("screening");
    }

    @FXML
    private void handleInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML
    private void handleExpense() throws IOException {
        App.setRoot("expense");
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

    @FXML private void handleAlerts() throws IOException { App.setRoot("alert"); }

    @FXML
    private void handleLogout() throws IOException {
        App.setRoot("primary");
    }

    private String buildReport(double sponsorAmount, double donationAmount, double totalFund, double totalExpense, double netFund) {
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
        builder.append("Tổng nguồn lực tiền tệ ghi nhận: ").append(FormatUtils.money(totalFund)).append('\n');
        builder.append("Tổng chi phí đã duyệt: ").append(FormatUtils.money(totalExpense)).append('\n');
        builder.append("Số dư ròng: ").append(FormatUtils.money(netFund)).append("\n\n");
        builder.append("QUYẾT TOÁN THEO CHIẾN DỊCH\n\n");
        for (ActivityModel campaign : AppData.getActivities()) {
            String cid = campaign.getMaChienDich();
            double income = AppData.getCampaignMoneyTotal(cid);
            double expense = AppData.getCampaignExpenseTotal(cid);
            builder.append(campaign.getTenChienDich()).append(" (").append(cid).append("):\n");
            builder.append("  Thu: ").append(FormatUtils.money(income)).append('\n');
            builder.append("  Chi: ").append(FormatUtils.money(expense)).append('\n');
            builder.append("  Cân đối: ").append(FormatUtils.money(income - expense)).append("\n\n");
        }
        builder.append("Ghi chú: dữ liệu hiện đang là dữ liệu mẫu trong bộ nhớ để phục vụ demo.");
        return builder.toString();
    }
}
