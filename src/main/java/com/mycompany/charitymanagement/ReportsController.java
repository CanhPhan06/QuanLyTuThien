package com.mycompany.charitymanagement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

public class ReportsController {

    private static final String ALL_CAMPAIGNS = "Tất cả chiến dịch";

    @FXML
    private ComboBox<String> cboCampaignReport;
    @FXML
    private Label lblReportScope;
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

    private List<CampaignReportRow> allCampaignRows = new ArrayList<>();

    @FXML
    private void initialize() {
        allCampaignRows = loadCampaignReportRows();
        setupCampaignSelector();
        setupIntegerVolunteerAxis();
        handleRefreshReport();
    }

    @FXML
    private void handleRefreshReport() {
        allCampaignRows = loadCampaignReportRows();
        if (cboCampaignReport.getItems().isEmpty()) {
            setupCampaignSelector();
        }

        String selectedCampaignId = selectedCampaignId();
        List<CampaignReportRow> visibleRows = filterRowsByCampaign(allCampaignRows, selectedCampaignId);

        double sponsorAmount = sum(visibleRows, ValueType.SPONSOR);
        double donationAmount = sum(visibleRows, ValueType.DONATION);
        double expenseAmount = sum(visibleRows, ValueType.EXPENSE);
        double totalFund = sponsorAmount + donationAmount;

        lblReportScope.setText(scopeTitle(selectedCampaignId));
        lblTotalActivities.setText(String.valueOf(visibleRows.size()));
        lblTotalParticipants.setText(String.valueOf(countParticipants(selectedCampaignId, visibleRows)));
        lblTotalSponsors.setText(String.valueOf(countSponsors(selectedCampaignId)));
        lblTotalDonations.setText(String.valueOf(countDonations(selectedCampaignId)));
        lblSponsorAmount.setText(FormatUtils.money(sponsorAmount));
        lblDonationAmount.setText(FormatUtils.money(donationAmount));
        lblTotalFund.setText(FormatUtils.money(totalFund));
        lblOperationCount.setText(String.valueOf(countOperationsByCampaign(selectedCampaignId)));
        lblContentCount.setText(String.valueOf(countContentsByCampaign(selectedCampaignId)));

        updateFinanceChart(visibleRows, selectedCampaignId);
        updateFundingMixChart(sponsorAmount, donationAmount, expenseAmount);
        updateVolunteerChart(visibleRows);
        updateOperationStatusChart(selectedCampaignId);

        txtReport.setText(buildReport(visibleRows, selectedCampaignId, sponsorAmount, donationAmount, expenseAmount, totalFund));
    }

    @FXML
    private void handleExportReport() {
        ExportUtils.exportTextToCsv("Xuất báo cáo thống kê", "bao-cao-thong-ke.pdf", txtReport.getText());
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

    private void setupCampaignSelector() {
        List<String> items = new ArrayList<>();
        items.add(ALL_CAMPAIGNS);
        for (CampaignReportRow row : allCampaignRows) {
            items.add(row.campaignId + " - " + row.campaignName);
        }
        cboCampaignReport.setItems(FXCollections.observableArrayList(items));
        cboCampaignReport.getSelectionModel().selectFirst();
        cboCampaignReport.valueProperty().addListener((obs, oldValue, newValue) -> handleRefreshReport());
    }

    private void setupIntegerVolunteerAxis() {
        NumberAxis axis = (NumberAxis) chartVolunteers.getYAxis();
        axis.setTickUnit(1);
        axis.setMinorTickCount(0);
        axis.setForceZeroInRange(true);
        axis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number value) {
                return String.valueOf(value.intValue());
            }

            @Override
            public Number fromString(String value) {
                return Integer.parseInt(value);
            }
        });
    }

    private String selectedCampaignId() {
        String value = cboCampaignReport.getValue();
        if (value == null || value.equals(ALL_CAMPAIGNS)) {
            return "";
        }
        int separator = value.indexOf(" - ");
        return separator > 0 ? value.substring(0, separator) : value;
    }

    private String scopeTitle(String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return "Toàn bộ chiến dịch";
        }
        return allCampaignRows.stream()
                .filter(row -> row.campaignId.equalsIgnoreCase(campaignId))
                .map(row -> row.campaignId + " - " + row.campaignName)
                .findFirst()
                .orElse(campaignId);
    }

    private List<CampaignReportRow> filterRowsByCampaign(List<CampaignReportRow> rows, String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return rows;
        }
        return rows.stream()
                .filter(row -> row.campaignId.equalsIgnoreCase(campaignId))
                .collect(Collectors.toList());
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
                        UiText.clean(rs.getString("MA_CHIEN_DICH")),
                        UiText.clean(rs.getString("TEN_CHIEN_DICH")),
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

    private void updateFinanceChart(List<CampaignReportRow> rows, String selectedCampaignId) {
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Nguồn thu");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi tiêu");
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Còn lại");

        for (CampaignReportRow row : rows) {
            String label = selectedCampaignId == null || selectedCampaignId.isBlank() ? row.campaignId : "Chiến dịch";
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

    private void updateOperationStatusChart(String campaignId) {
        int pending = countOperations(campaignId, "Chờ duyệt", "Đang xét", "Đang phân công", "Chờ xác nhận");
        int completed = countOperations(campaignId, "Đã duyệt", "Đã phân công", "Có mặt", "Đã xác nhận", "Đã xuất", "Hoàn thành");
        int rejected = countOperations(campaignId, "Từ chối", "Hủy");
        int total = countOperationsByCampaign(campaignId);
        int other = Math.max(0, total - pending - completed - rejected);

        chartOperationStatus.setData(FXCollections.observableArrayList(
                new PieChart.Data("Chờ xử lý: " + pending, pending),
                new PieChart.Data("Đã xử lý: " + completed, completed),
                new PieChart.Data("Từ chối/Hủy: " + rejected, rejected),
                new PieChart.Data("Khác: " + other, other)
        ));
    }

    private int countOperations(String campaignId, String... statuses) {
        List<String> statusList = Arrays.asList(statuses);
        return (int) AppData.getOperations().stream()
                .filter(item -> isCampaignMatch(campaignId, item.getMaChienDich()))
                .filter(item -> statusList.stream().anyMatch(status -> status.equalsIgnoreCase(item.getTrangThai())))
                .count();
    }

    private int countOperationsByCampaign(String campaignId) {
        return (int) AppData.getOperations().stream()
                .filter(item -> isCampaignMatch(campaignId, item.getMaChienDich()))
                .count();
    }

    private int countContentsByCampaign(String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return AppData.getContents().size();
        }
        return (int) AppData.getContents().stream()
                .filter(item -> campaignId.equalsIgnoreCase(item.getMaLienKet())
                || campaignId.equalsIgnoreCase(item.getMaChienDich()))
                .count();
    }

    private int countParticipants(String campaignId, List<CampaignReportRow> visibleRows) {
        if (campaignId == null || campaignId.isBlank()) {
            return AppData.getParticipants().size();
        }
        return visibleRows.isEmpty() ? 0 : visibleRows.get(0).volunteerCount;
    }

    private int countSponsors(String campaignId) {
        return (int) AppData.getSponsors().stream()
                .filter(item -> isCampaignMatch(campaignId, item.getMaChienDich()))
                .count();
    }

    private int countDonations(String campaignId) {
        return (int) AppData.getDonations().stream()
                .filter(item -> isCampaignMatch(campaignId, item.getHoatDong()))
                .count();
    }

    private boolean isCampaignMatch(String selectedCampaignId, String rowCampaignId) {
        return selectedCampaignId == null || selectedCampaignId.isBlank()
                || selectedCampaignId.equalsIgnoreCase(rowCampaignId);
    }

    private String buildReport(List<CampaignReportRow> rows, String campaignId, double sponsorAmount,
            double donationAmount, double expenseAmount, double totalFund) {
        StringBuilder builder = new StringBuilder();
        double balance = totalFund - expenseAmount;

        builder.append(campaignId == null || campaignId.isBlank()
                ? "BÁO CÁO TOÀN BỘ CHIẾN DỊCH\n\n"
                : "BÁO CÁO CHIẾN DỊCH\n\n");
        builder.append("Phạm vi: ").append(scopeTitle(campaignId)).append('\n');
        builder.append("Số chiến dịch: ").append(rows.size()).append('\n');
        builder.append("Sinh viên/TNV tham gia: ").append(countParticipants(campaignId, rows)).append('\n');
        builder.append("Nhà tài trợ liên quan: ").append(countSponsors(campaignId)).append('\n');
        builder.append("Khoản/phiếu quyên góp: ").append(countDonations(campaignId)).append('\n');
        builder.append("Bản ghi vận hành: ").append(countOperationsByCampaign(campaignId)).append('\n');
        builder.append("Bản ghi nội dung: ").append(countContentsByCampaign(campaignId)).append("\n\n");
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
