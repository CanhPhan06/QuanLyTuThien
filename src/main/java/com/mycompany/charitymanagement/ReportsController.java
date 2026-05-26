package com.mycompany.charitymanagement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
    private BarChart<String, Number> chartFinance;
    @FXML
    private BarChart<String, Number> chartVolunteers;
    @FXML
    private PieChart chartFundingMix;
    @FXML
    private PieChart chartOperationStatus;
    @FXML
    private TextArea txtReport;

    @FXML
    private void initialize() {
        handleRefreshReport();
    }

    @FXML
    private void handleRefreshReport() {
        List<CampaignReportRow> campaignRows = loadCampaignReportRows();

        double sponsorAmount = sum(campaignRows, ValueType.SPONSOR);
        double donationAmount = sum(campaignRows, ValueType.DONATION);
        double expenseAmount = sum(campaignRows, ValueType.EXPENSE);
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

        updateFinanceChart(campaignRows);
        updateFundingMixChart(sponsorAmount, donationAmount, expenseAmount);
        updateVolunteerChart(campaignRows);
        updateOperationStatusChart();

        txtReport.setText(buildReport(campaignRows, sponsorAmount, donationAmount, expenseAmount, totalFund));
    }

    @FXML
    private void handleExportReport() {
        ExportUtils.exportTextToCsv("Xuất báo cáo tổng hợp", "bao-cao-tong-hop.pdf", txtReport.getText());
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

    private List<CampaignReportRow> loadCampaignReportRows() {
        List<CampaignReportRow> databaseRows = loadCampaignReportRowsFromDatabase();
        if (!databaseRows.isEmpty()) {
            return databaseRows;
        }
        return loadCampaignReportRowsFromMemory();
    }

    private List<CampaignReportRow> loadCampaignReportRowsFromDatabase() {
        List<CampaignReportRow> rows = new ArrayList<>();
        String sql = "SELECT MA_CHIEN_DICH, TEN_CHIEN_DICH, TONG_QUYEN_GOP_TIEN, TONG_TAI_TRO, "
                + "TONG_CHI_TIEU, SO_TNV, TY_LE_DAT_MUC_TIEU "
                + "FROM BAO_CAO_CHIEN_DICH ORDER BY MA_CHIEN_DICH";
        try (Connection connection = DatabaseConfig.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new CampaignReportRow(
                        rs.getString("MA_CHIEN_DICH"),
                        rs.getString("TEN_CHIEN_DICH"),
                        rs.getDouble("TONG_QUYEN_GOP_TIEN"),
                        rs.getDouble("TONG_TAI_TRO"),
                        rs.getDouble("TONG_CHI_TIEU"),
                        rs.getInt("SO_TNV"),
                        rs.getDouble("TY_LE_DAT_MUC_TIEU")
                ));
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        return rows;
    }

    private List<CampaignReportRow> loadCampaignReportRowsFromMemory() {
        List<CampaignReportRow> rows = new ArrayList<>();
        for (ActivityModel activity : AppData.getActivities()) {
            double donations = AppData.getDonations().stream()
                    .filter(item -> item.getHoatDong().equalsIgnoreCase(activity.getMaChienDich()))
                    .mapToDouble(DonationModel::getSoTien)
                    .sum();
            double sponsors = AppData.getSponsors().stream()
                    .filter(item -> item.getMaChienDich().equalsIgnoreCase(activity.getMaChienDich()))
                    .mapToDouble(SponsorModel::getGiaTriTaiTro)
                    .sum();
            int volunteers = (int) AppData.getCampaignParticipantCount(activity.getMaChienDich());
            double targetRate = activity.getMucTieuTien() <= 0
                    ? 0
                    : Math.round((donations + sponsors) / activity.getMucTieuTien() * 10000.0) / 100.0;
            rows.add(new CampaignReportRow(
                    activity.getMaChienDich(),
                    activity.getTenChienDich(),
                    donations,
                    sponsors,
                    0,
                    volunteers,
                    targetRate
            ));
        }
        return rows;
    }

    private void updateFinanceChart(List<CampaignReportRow> rows) {
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Nguồn thu");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi tiêu");
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Còn lại");

        for (CampaignReportRow row : rows) {
            String label = row.campaignId;
            double income = row.totalIncome();
            incomeSeries.getData().add(new XYChart.Data<>(label, income));
            expenseSeries.getData().add(new XYChart.Data<>(label, row.expenseAmount));
            balanceSeries.getData().add(new XYChart.Data<>(label, Math.max(0, income - row.expenseAmount)));
        }

        chartFinance.getData().setAll(incomeSeries, expenseSeries, balanceSeries);
    }

    private void updateFundingMixChart(double sponsorAmount, double donationAmount, double expenseAmount) {
        chartFundingMix.setData(FXCollections.observableArrayList(
                new PieChart.Data("Tài trợ: " + FormatUtils.money(sponsorAmount), sponsorAmount),
                new PieChart.Data("Quyên góp tiền: " + FormatUtils.money(donationAmount), donationAmount),
                new PieChart.Data("Đã chi: " + FormatUtils.money(expenseAmount), expenseAmount)
        ));
    }

    private void updateVolunteerChart(List<CampaignReportRow> rows) {
        XYChart.Series<String, Number> volunteerSeries = new XYChart.Series<>();
        volunteerSeries.setName("TNV tham gia");

        for (CampaignReportRow row : rows) {
            volunteerSeries.getData().add(new XYChart.Data<>(row.campaignId, row.volunteerCount));
        }

        chartVolunteers.getData().setAll(volunteerSeries);
    }

    private void updateOperationStatusChart() {
        int pending = countOperations("Chờ duyệt", "Đang xét", "Đang phân công", "Chờ xác nhận");
        int completed = countOperations("Đã duyệt", "Đã phân công", "Có mặt", "Đã xác nhận", "Đã xuất", "Hoàn thành");
        int rejected = countOperations("Từ chối", "Hủy");
        int other = Math.max(0, AppData.getOperations().size() - pending - completed - rejected);

        chartOperationStatus.setData(FXCollections.observableArrayList(
                new PieChart.Data("Chờ xử lý: " + pending, pending),
                new PieChart.Data("Đã xử lý: " + completed, completed),
                new PieChart.Data("Từ chối/Hủy: " + rejected, rejected),
                new PieChart.Data("Khác: " + other, other)
        ));
    }

    private int countOperations(String... statuses) {
        List<String> statusList = Arrays.asList(statuses);
        return (int) AppData.getOperations().stream()
                .filter(item -> statusList.stream().anyMatch(status -> status.equalsIgnoreCase(item.getTrangThai())))
                .count();
    }

    private String buildReport(List<CampaignReportRow> rows, double sponsorAmount,
            double donationAmount, double expenseAmount, double totalFund) {
        StringBuilder builder = new StringBuilder();
        double balance = totalFund - expenseAmount;

        builder.append("BÁO CÁO TỔNG HỢP\n\n");
        builder.append("Chiến dịch: ").append(AppData.getActivities().size()).append('\n');
        builder.append("Sinh viên/TNV tham gia: ").append(AppData.getParticipants().size()).append('\n');
        builder.append("Đối tác tài trợ: ").append(AppData.getSponsors().size()).append('\n');
        builder.append("Phiếu/khoản quyên góp: ").append(AppData.getDonations().size()).append('\n');
        builder.append("Bản ghi vận hành: ").append(AppData.getOperations().size()).append('\n');
        builder.append("Bản ghi nội dung - hệ thống: ").append(AppData.getContents().size()).append("\n\n");
        builder.append("Tổng tiền tài trợ: ").append(FormatUtils.money(sponsorAmount)).append('\n');
        builder.append("Tổng quyên góp bằng tiền: ").append(FormatUtils.money(donationAmount)).append('\n');
        builder.append("Tổng nguồn thu ghi nhận: ").append(FormatUtils.money(totalFund)).append('\n');
        builder.append("Tổng chi đã duyệt: ").append(FormatUtils.money(expenseAmount)).append('\n');
        builder.append("Số dư tài chính: ").append(FormatUtils.money(balance)).append("\n\n");

        builder.append("TÌNH HÌNH TÀI CHÍNH THEO CHIẾN DỊCH\n");
        for (CampaignReportRow row : rows) {
            builder.append("- ")
                    .append(row.campaignId)
                    .append(" | ")
                    .append(row.campaignName)
                    .append(" | Thu: ")
                    .append(FormatUtils.money(row.totalIncome()))
                    .append(" | Chi: ")
                    .append(FormatUtils.money(row.expenseAmount))
                    .append(" | Đạt mục tiêu: ")
                    .append(row.targetRate)
                    .append("% | TNV: ")
                    .append(row.volunteerCount)
                    .append('\n');
        }

        builder.append("\nNhận xét: biểu đồ thu/chi cho biết chiến dịch nào còn dư nguồn lực, chiến dịch nào cần bổ sung tài trợ hoặc kiểm soát chi tiêu.");
        return builder.toString();
    }

    private double sum(List<CampaignReportRow> rows, ValueType type) {
        double total = 0;
        for (CampaignReportRow row : rows) {
            switch (type) {
                case SPONSOR:
                    total += row.sponsorAmount;
                    break;
                case DONATION:
                    total += row.donationAmount;
                    break;
                case EXPENSE:
                    total += row.expenseAmount;
                    break;
                default:
                    break;
            }
        }
        return total;
    }

    private enum ValueType {
        SPONSOR,
        DONATION,
        EXPENSE
    }

    private static final class CampaignReportRow {

        private final String campaignId;
        private final String campaignName;
        private final double donationAmount;
        private final double sponsorAmount;
        private final double expenseAmount;
        private final int volunteerCount;
        private final double targetRate;

        private CampaignReportRow(String campaignId, String campaignName, double donationAmount,
                double sponsorAmount, double expenseAmount, int volunteerCount, double targetRate) {
            this.campaignId = campaignId;
            this.campaignName = campaignName;
            this.donationAmount = donationAmount;
            this.sponsorAmount = sponsorAmount;
            this.expenseAmount = expenseAmount;
            this.volunteerCount = volunteerCount;
            this.targetRate = targetRate;
        }

        private double totalIncome() {
            return donationAmount + sponsorAmount;
        }
    }
}
