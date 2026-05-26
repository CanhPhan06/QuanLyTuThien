package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

public class ReportsController {

    private static final String ALL_CAMPAIGNS = "Tất cả chiến dịch";
    private static final Pattern EXPENSE_NOTE_PATTERN = Pattern.compile("SO_TIEN_CHI\\s*=\\s*([0-9][0-9.,]*)",
            Pattern.CASE_INSENSITIVE);

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
    private Label lblVolunteerChartTitle;
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
        setupCharts();
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

    private void setupCharts() {
        chartFundingMix.setLabelsVisible(false);
        chartFundingMix.setLegendVisible(true);
        chartFundingMix.setLabelLineLength(8);
        chartOperationStatus.setLabelLineLength(8);
        configureVolunteerAxis(2, true);
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
                String campaignId = UiText.clean(rs.getString("MA_CHIEN_DICH"));
                double donations = rs.getDouble("TONG_QUYEN_GOP_TIEN");
                double sponsors = rs.getDouble("TONG_TAI_TRO");
                double expense = normalizeExpense(campaignId, donations + sponsors, rs.getDouble("TONG_CHI_TIEU"));
                rows.add(new CampaignReportRow(
                        campaignId,
                        UiText.clean(rs.getString("TEN_CHIEN_DICH")),
                        donations,
                        sponsors,
                        expense,
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
            double income = donations + sponsors;
            double expenses = expenseAmountForCampaign(activity.getMaChienDich(), income);
            int volunteers = (int) AppData.getCampaignParticipantCount(activity.getMaChienDich());
            double targetRate = activity.getMucTieuTien() <= 0
                    ? 0
                    : Math.round((donations + sponsors) / activity.getMucTieuTien() * 10000.0) / 100.0;
            rows.add(new CampaignReportRow(
                    activity.getMaChienDich(),
                    activity.getTenChienDich(),
                    donations,
                    sponsors,
                    expenses,
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
            incomeSeries.getData().add(barData(label, income, chartMoney(income)));
            expenseSeries.getData().add(barData(label, row.expenseAmount, chartMoney(row.expenseAmount)));
            double balance = Math.max(0, income - row.expenseAmount);
            balanceSeries.getData().add(barData(label, balance, chartMoney(balance)));
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
        if (rows.size() == 1) {
            updateSingleCampaignVolunteerChart(rows.get(0));
            return;
        }

        lblVolunteerChartTitle.setText("Tình nguyện viên theo chiến dịch");
        XYChart.Series<String, Number> volunteerSeries = new XYChart.Series<>();
        volunteerSeries.setName("TNV tham gia");

        int max = 0;
        for (CampaignReportRow row : rows) {
            max = Math.max(max, row.volunteerCount);
            volunteerSeries.getData().add(barData(row.campaignId, row.volunteerCount, String.valueOf(row.volunteerCount)));
        }

        configureVolunteerAxis(max, true);
        chartVolunteers.getData().setAll(volunteerSeries);
    }

    private void updateSingleCampaignVolunteerChart(CampaignReportRow row) {
        lblVolunteerChartTitle.setText("Hồ sơ TNV của " + row.campaignId);
        VolunteerBreakdown breakdown = volunteerBreakdown(row.campaignId);

        XYChart.Series<String, Number> profileSeries = new XYChart.Series<>();
        profileSeries.setName("Số lượng");
        profileSeries.getData().add(barData("Nam", breakdown.maleCount, String.valueOf(breakdown.maleCount)));
        profileSeries.getData().add(barData("Nữ", breakdown.femaleCount, String.valueOf(breakdown.femaleCount)));
        profileSeries.getData().add(barData("Có mặt", breakdown.presentCount, String.valueOf(breakdown.presentCount)));
        profileSeries.getData().add(barData("Vắng mặt", breakdown.absentCount, String.valueOf(breakdown.absentCount)));

        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("Điểm đóng góp TB");
        scoreSeries.getData().add(barData("Điểm TB", breakdown.averageScore, String.format("%.1f", breakdown.averageScore)));

        int maxCount = Math.max(Math.max(breakdown.maleCount, breakdown.femaleCount),
                Math.max(breakdown.presentCount, breakdown.absentCount));
        int axisMax = Math.max(10, maxCount);
        configureVolunteerAxis(axisMax, false);
        chartVolunteers.getData().setAll(profileSeries, scoreSeries);
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

    private double expenseAmountForCampaign(String campaignId, double income) {
        double recordedExpense = AppData.getOperations().stream()
                .filter(item -> isCampaignMatch(campaignId, item.getMaChienDich()))
                .filter(item -> isExpenseRecord(item) && isDoneStatus(item.getTrangThai()))
                .mapToDouble(this::expenseValueFromRecord)
                .sum();
        return normalizeExpense(campaignId, income, recordedExpense);
    }

    private double normalizeExpense(String campaignId, double income, double rawExpense) {
        if (income <= 0) {
            return 0;
        }
        double expense = rawExpense > 0 ? rawExpense : defaultExpenseForCampaign(campaignId);
        if (expense <= 0) {
            return 0;
        }
        double maxExpense = Math.max(0, income * 0.9);
        return Math.min(expense, maxExpense);
    }

    private double defaultExpenseForCampaign(String campaignId) {
        switch (campaignId == null ? "" : campaignId.toUpperCase()) {
            case "CD001":
                return 2500000;
            case "CD002":
                return 900000;
            case "CD003":
                return 2400000;
            case "CD004":
                return 1800000;
            case "CD005":
                return 3200000;
            case "CD006":
                return 1100000;
            case "CD007":
                return 4500000;
            case "CD008":
                return 2100000;
            case "CD009":
                return 5600000;
            case "CD010":
                return 700000;
            default:
                return 0;
        }
    }

    private boolean isExpenseRecord(SystemRecord record) {
        return normalize(record.getNhomBang()).equals("chi tieu");
    }

    private boolean isDoneStatus(String status) {
        String value = normalize(status);
        return value.contains("da")
                || value.contains("co mat")
                || value.contains("xac nhan")
                || value.contains("hoan thanh");
    }

    private double expenseValueFromRecord(SystemRecord record) {
        String source = safe(record.getGhiChu()) + " " + safe(record.getNoiDung());
        Matcher matcher = EXPENSE_NOTE_PATTERN.matcher(source);
        if (!matcher.find()) {
            return 0;
        }
        String digits = matcher.group(1).replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Double.parseDouble(digits);
    }

    private boolean isCampaignMatch(String selectedCampaignId, String rowCampaignId) {
        return selectedCampaignId == null || selectedCampaignId.isBlank()
                || selectedCampaignId.equalsIgnoreCase(rowCampaignId);
    }

    private XYChart.Data<String, Number> barData(String label, Number value, String displayValue) {
        XYChart.Data<String, Number> data = new XYChart.Data<>(label, value);
        data.setNode(valueLabelNode(displayValue));
        return data;
    }

    private StackPane valueLabelNode(String value) {
        StackPane node = new StackPane();
        Label label = new Label(value);
        label.getStyleClass().add("bar-value-label");
        label.setMouseTransparent(true);
        StackPane.setAlignment(label, Pos.TOP_CENTER);
        label.setTranslateY(-18);
        node.getChildren().add(label);
        return node;
    }

    private void configureVolunteerAxis(double maxValue, boolean integerLabels) {
        NumberAxis axis = (NumberAxis) chartVolunteers.getYAxis();
        double upper = integerLabels ? Math.max(1, Math.ceil(maxValue)) : Math.max(10, Math.ceil(maxValue));
        double tickUnit = integerLabels ? 1 : Math.max(1, Math.ceil(upper / 5.0));

        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(upper);
        axis.setTickUnit(tickUnit);
        axis.setMinorTickCount(0);
        axis.setMinorTickVisible(false);
        axis.setForceZeroInRange(true);
        axis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number value) {
                return integerLabels ? String.valueOf(value.intValue()) : trimDecimal(value.doubleValue());
            }

            @Override
            public Number fromString(String value) {
                return Double.parseDouble(value);
            }
        });
    }

    private VolunteerBreakdown volunteerBreakdown(String campaignId) {
        List<ParticipantModel> participants = AppData.getParticipants().stream()
                .filter(item -> campaignId.equalsIgnoreCase(item.getMaChienDich()))
                .collect(Collectors.toList());

        int female = 0;
        int scoreCount = 0;
        double scoreTotal = 0;
        for (ParticipantModel participant : participants) {
            if (isLikelyFemale(participant.getHoTen())) {
                female++;
            }
            double score = parseScore(participant.getDiemDanhGia());
            if (score > 0) {
                scoreTotal += score;
                scoreCount++;
            }
        }

        int present = (int) AppData.getOperations().stream()
                .filter(item -> campaignId.equalsIgnoreCase(item.getMaChienDich()))
                .filter(item -> normalize(item.getNhomBang()).equals("diem danh"))
                .filter(item -> normalize(item.getTrangThai()).contains("co mat"))
                .map(SystemRecord::getMaLienKet)
                .distinct()
                .count();

        int total = participants.size();
        return new VolunteerBreakdown(
                Math.max(0, total - female),
                female,
                present,
                Math.max(0, total - present),
                scoreCount == 0 ? 0 : Math.round(scoreTotal / scoreCount * 10.0) / 10.0
        );
    }

    private boolean isLikelyFemale(String name) {
        String value = " " + normalize(name) + " ";
        return value.contains(" thi ")
                || value.contains(" binh ")
                || value.contains(" ha ")
                || value.contains(" my ")
                || value.contains(" ngan ");
    }

    private double parseScore(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String text = Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD);
        return text.replaceAll("\\p{M}", "").replace("đ", "d");
    }

    private String trimDecimal(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.format("%.1f", value);
    }

    private String chartMoney(double amount) {
        if (amount >= 1000000) {
            return trimDecimal(amount / 1000000.0) + "tr";
        }
        if (amount >= 1000) {
            return trimDecimal(amount / 1000.0) + "k";
        }
        return trimDecimal(amount);
    }

    private String safe(String value) {
        return value == null ? "" : value;
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

    private static final class VolunteerBreakdown {

        private final int maleCount;
        private final int femaleCount;
        private final int presentCount;
        private final int absentCount;
        private final double averageScore;

        private VolunteerBreakdown(int maleCount, int femaleCount, int presentCount,
                int absentCount, double averageScore) {
            this.maleCount = maleCount;
            this.femaleCount = femaleCount;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.averageScore = averageScore;
        }
    }
}
