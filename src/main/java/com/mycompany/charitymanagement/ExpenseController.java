package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class ExpenseController {

    private static final String[] FORM_LABELS = {
        "Mô tả chi phí", "Số tiền", "Danh mục", "Chiến dịch",
        "Người đề xuất", "Người duyệt", "Trạng thái", "Ghi chú"
    };

    @FXML
    private Label lblTotalExpense;
    @FXML
    private Label lblPendingExpense;
    @FXML
    private Label lblApprovedExpense;
    @FXML
    private Label lblExpenseCount;
    @FXML
    private ComboBox<String> cboStatusFilter;
    @FXML
    private ComboBox<String> cboCategoryFilter;
    @FXML
    private ComboBox<String> cboCampaignFilter;
    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Expense> tableExpenses;
    @FXML
    private TableColumn<Expense, String> colMaChiPhi;
    @FXML
    private TableColumn<Expense, String> colMoTa;
    @FXML
    private TableColumn<Expense, String> colSoTien;
    @FXML
    private TableColumn<Expense, String> colDanhMuc;
    @FXML
    private TableColumn<Expense, String> colChienDich;
    @FXML
    private TableColumn<Expense, String> colNguoiDeXuat;
    @FXML
    private TableColumn<Expense, String> colTrangThai;
    @FXML
    private TableColumn<Expense, String> colNgayDeXuat;
    @FXML
    private TableColumn<Expense, String> colNgayDuyet;

    private FilteredList<Expense> filteredExpenses;

    @FXML
    private void initialize() {
        colMaChiPhi.setCellValueFactory(new PropertyValueFactory<>("maChiPhi"));
        colMaChiPhi.setVisible(false);
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTienText"));
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("danhMuc"));
        colChienDich.setText("Chiến dịch");
        colChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNguoiDeXuat.setCellValueFactory(new PropertyValueFactory<>("nguoiDeXuat"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colNgayDeXuat.setCellValueFactory(new PropertyValueFactory<>("ngayDeXuat"));
        colNgayDuyet.setCellValueFactory(new PropertyValueFactory<>("ngayDuyet"));

        tableExpenses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredExpenses = new FilteredList<>(AppData.getExpenses(), item -> true);
        tableExpenses.setItems(filteredExpenses);
        setupFilters();
        tableExpenses.setRowFactory(table -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableExpenses.getSelectionModel().select(row.getItem());
                    showDetail(row.getItem());
                }
            });
            return row;
        });
        refreshView();
    }

    @FXML
    private void handleAddExpense() {
        Expense expense = showDialog("Đề xuất chi phí", null);
        if (expense == null) return;
        String error = BusinessRules.validateExpense(expense);
        if (error != null) { DialogUtils.warning(error); return; }
        if (existsById(expense.getMaChiPhi(), null)) { DialogUtils.warning("Mã chi phí đã tồn tại."); return; }
        AppData.getExpenses().add(expense);
        BusinessService.audit(currentUser(), "Đề xuất chi phí", expense.getMaChiPhi() + " - " + FormatUtils.money(expense.getSoTien()));
        tableExpenses.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã đề xuất chi phí, chờ phê duyệt.");
    }

    @FXML
    private void handleUpdateExpense() {
        Expense selected = tableExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn khoản chi cần sửa."); return; }
        Expense form = showDialog("Cập nhật chi phí", selected);
        if (form == null) return;
        if (existsById(form.getMaChiPhi(), selected)) { DialogUtils.warning("Mã chi phí đã tồn tại."); return; }
        selected.setMoTa(form.getMoTa());
        selected.setSoTien(form.getSoTien());
        selected.setDanhMuc(form.getDanhMuc());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setNguoiDeXuat(form.getNguoiDeXuat());
        selected.setNguoiDuyet(form.getNguoiDuyet());
        selected.setTrangThai(form.getTrangThai());
        selected.setGhiChu(form.getGhiChu());
        if ("Đã duyệt".equals(form.getTrangThai()) && form.getNgayDuyet().isEmpty()) {
            selected.setNgayDuyet(AppData.todayText());
        }
        tableExpenses.refresh();
        tableExpenses.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã cập nhật chi phí.");
    }

    @FXML
    private void handleDeleteExpense() {
        Expense selected = tableExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn khoản chi cần xóa."); return; }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa khoản chi " + selected.getMaChiPhi() + "?")) return;
        AppData.getExpenses().remove(selected);
        tableExpenses.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã xóa khoản chi.");
    }

    @FXML
    private void handleApproveExpense() {
        Expense selected = tableExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn khoản chi cần phê duyệt."); return; }
        if (!"Chờ duyệt".equals(selected.getTrangThai())) {
            DialogUtils.warning("Chỉ có thể phê duyệt khoản chi ở trạng thái 'Chờ duyệt'.");
            return;
        }
        UserAccount user = UserSession.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            DialogUtils.warning("Chỉ quản trị viên mới có quyền phê duyệt chi phí.");
            return;
        }
        if (selected.getSoTien() > 10000000) {
            if (!DialogUtils.confirm("Số tiền > 10.000.000 VNĐ. Bạn có chắc muốn phê duyệt?")) return;
        }
        selected.setTrangThai("Đã duyệt");
        selected.setNgayDuyet(AppData.todayText());
        selected.setNguoiDuyet(currentUser());
        BusinessService.audit(currentUser(), "Phê duyệt chi phí", selected.getMaChiPhi() + " - " + FormatUtils.money(selected.getSoTien()));
        tableExpenses.refresh();
        refreshView();
        DialogUtils.info("Đã phê duyệt chi phí.");
    }

    @FXML
    private void handleRejectExpense() {
        Expense selected = tableExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn khoản chi cần từ chối."); return; }
        if (!"Chờ duyệt".equals(selected.getTrangThai())) {
            DialogUtils.warning("Chỉ có thể từ chối khoản chi ở trạng thái 'Chờ duyệt'.");
            return;
        }
        UserAccount user = UserSession.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            DialogUtils.warning("Chỉ quản trị viên mới có quyền từ chối chi phí.");
            return;
        }
        selected.setTrangThai("Từ chối");
        selected.setNgayDuyet(AppData.todayText());
        selected.setNguoiDuyet(currentUser());
        BusinessService.audit(currentUser(), "Từ chối chi phí", selected.getMaChiPhi());
        tableExpenses.refresh();
        refreshView();
        DialogUtils.info("Đã từ chối chi phí.");
    }



    @FXML
    private void handleClearFilters() {
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboCategoryFilter.setValue("Tất cả danh mục");
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        txtSearch.clear();
        applyFilters();
    }

    @FXML
    private void handleExport() {
        ExportUtils.exportTableToCsv(tableExpenses, "Xuất danh sách chi phí", "danh-sach-chi-phi.csv");
    }

    private Expense showDialog(String title, Expense current) {
        String[] values = current == null ? new String[]{"", "0", "Văn phòng phẩm", defaultCampaignOption(), currentUser(), "", "Chờ duyệt", ""}
                : new String[]{
                    current.getMoTa(), String.format("%.0f", current.getSoTien()), current.getDanhMuc(),
                    current.getMaChienDich() + " - " + current.getTenChienDich(),
                    current.getNguoiDeXuat(), current.getNguoiDuyet(), current.getTrangThai(), current.getGhiChu()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) return null;
        return buildExpense(result, current);
    }

    private Expense buildExpense(String[] values, Expense current) {
        String ma = current == null ? nextExpenseId() : current.getMaChiPhi();
        String maChienDich = codeOf(values[3]);
        if (values[0].isEmpty()) { DialogUtils.warning("Vui lòng nhập mô tả chi phí."); return null; }
        try {
            double soTien = FormatUtils.parseMoney(values[1]);
            String ngayDuyet = "";
            if (current != null && "Đã duyệt".equals(values[6])) {
                ngayDuyet = current.getNgayDuyet().isEmpty() ? AppData.todayText() : current.getNgayDuyet();
            }
            return new Expense(ma, maChienDich, values[0], soTien, values[2], values[4], values[5],
                    values[6], AppData.todayText(), ngayDuyet, values[7]);
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số tiền không hợp lệ.");
            return null;
        }
    }

    private void showDetail(Expense expense) {
        DetailDialogUtils.showDetails(tableExpenses, "Chi tiết chi phí - " + expense.getMaChiPhi(), new String[][]{
            {"Mô tả", expense.getMoTa()},
            {"Số tiền", expense.getSoTienText()},
            {"Danh mục", expense.getDanhMuc()},
            {"Chiến dịch", expense.getTenChienDich()},
            {"Người đề xuất", expense.getNguoiDeXuat()},
            {"Người duyệt", expense.getNguoiDuyet()},
            {"Trạng thái", expense.getTrangThai()},
            {"Ngày đề xuất", expense.getNgayDeXuat()},
            {"Ngày duyệt", expense.getNgayDuyet().isEmpty() ? "-" : expense.getNgayDuyet()},
            {"Ghi chú", expense.getGhiChu().isEmpty() ? "-" : expense.getGhiChu()}
        });
    }

    private void setupFilters() {
        cboStatusFilter.setItems(FXCollections.observableArrayList("Tất cả trạng thái", "Chờ duyệt", "Đã duyệt", "Từ chối"));
        cboCategoryFilter.setItems(buildCategoryChoices());
        cboCampaignFilter.setItems(buildCampaignChoices());
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboCategoryFilter.setValue("Tất cả danh mục");
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        cboStatusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboCategoryFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboCampaignFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        txtSearch.textProperty().addListener((o, a, b) -> applyFilters());
    }

    private void refreshView() {
        ObservableList<Expense> all = AppData.getExpenses();
        lblExpenseCount.setText(String.valueOf(all.size()));
        double total = all.stream().filter(e -> "Đã duyệt".equals(e.getTrangThai())).mapToDouble(Expense::getSoTien).sum();
        lblTotalExpense.setText(FormatUtils.money(total));
        long pending = all.stream().filter(e -> "Chờ duyệt".equals(e.getTrangThai())).count();
        lblPendingExpense.setText(String.valueOf(pending));
        long approved = all.stream().filter(e -> "Đã duyệt".equals(e.getTrangThai())).count();
        lblApprovedExpense.setText(String.valueOf(approved));
        applyFilters();
        tableExpenses.refresh();
    }

    private void applyFilters() {
        if (filteredExpenses == null) return;
        String status = value(cboStatusFilter);
        String cat = value(cboCategoryFilter);
        String campaignId = codeOf(cboCampaignFilter.getValue());
        String query = normalize(value(txtSearch));
        boolean allStatus = status.isEmpty() || "Tất cả trạng thái".equals(status);
        boolean allCat = cat.isEmpty() || "Tất cả danh mục".equals(cat);
        boolean allCamp = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboCampaignFilter.getValue());
        filteredExpenses.setPredicate(e
                -> (allStatus || normalize(e.getTrangThai()).equals(normalize(status)))
                && (allCat || normalize(e.getDanhMuc()).equals(normalize(cat)))
                && (allCamp || e.getMaChienDich().equalsIgnoreCase(campaignId))
                && (query.isEmpty() || normalize(e.getMoTa() + " " + e.getDanhMuc() + " " + e.getNguoiDeXuat()).contains(query)));
    }

    private ObservableList<String> buildCategoryChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả danh mục");
        AppData.getExpenses().stream().map(Expense::getDanhMuc).filter(v -> v != null && !v.isEmpty()).distinct().forEach(choices::add);
        return choices;
    }
    private ObservableList<String> buildCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel a : AppData.getActivities()) choices.add(a.getMaChienDich() + " - " + a.getTenChienDich());
        return choices;
    }
    private String defaultCampaignOption() {
        return AppData.getActivities().stream().findFirst()
                .map(a -> a.getMaChienDich() + " - " + a.getTenChienDich()).orElse("");
    }
    private boolean existsById(String id, Expense current) {
        return AppData.getExpenses().stream().anyMatch(e -> e != current && e.getMaChiPhi().equalsIgnoreCase(id));
    }
    private static String nextExpenseId() {
        int idx = AppData.getExpenses().size() + 1;
        String id; do { id = "CP" + String.format("%03d", idx++); } while (idExists(id));
        return id;
    }
    private static boolean idExists(String id) { return AppData.getExpenses().stream().anyMatch(e -> e.getMaChiPhi().equalsIgnoreCase(id)); }

    private String currentUser() { UserAccount u = UserSession.getCurrentUser(); return u == null ? "ADMIN001" : u.getUsername(); }
    private String codeOf(String opt) { if (opt == null) return ""; int s = opt.indexOf(" - "); return s >= 0 ? opt.substring(0, s).trim() : opt.trim(); }
    private String value(ComboBox<String> cb) { return cb.getValue() == null ? "" : cb.getValue(); }
    private String value(TextField tf) { return tf == null || tf.getText() == null ? "" : tf.getText().trim(); }
    private String normalize(String v) { if (v == null) return ""; return Normalizer.normalize(v.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("đ", "d"); }

    @FXML private void handleBackHome() throws IOException { App.setRoot("secondary"); }
    @FXML private void handleAlerts() throws IOException { App.setRoot("alert"); }

    @FXML
    private void handleLogout() throws IOException { UserSession.clear(); App.setRoot("primary"); }
    @FXML private void handleActivities() throws IOException { App.setRoot("activities"); }
    @FXML private void handleParticipants() throws IOException { App.setRoot("participants"); }
    @FXML private void handleScreening() throws IOException { App.setRoot("screening"); }
    @FXML private void handleTraining() throws IOException { App.setRoot("training"); }
    @FXML private void handleSponsors() throws IOException { App.setRoot("sponsors"); }
    @FXML private void handleDonations() throws IOException { App.setRoot("donations"); }
    @FXML private void handleOperations() throws IOException { App.setRoot("operations"); }
    @FXML private void handleInventory() throws IOException { App.setRoot("inventory"); }
    @FXML private void handleExpense() throws IOException { App.setRoot("expense"); }
    @FXML private void handleContent() throws IOException { App.setRoot("content"); }
    @FXML private void handleReports() throws IOException { App.setRoot("reports"); }
}
