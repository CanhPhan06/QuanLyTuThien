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

public class InventoryController {

    private static final String[] FORM_LABELS = {
        "Tên vật tư", "Danh mục", "Đơn vị tính", "Số lượng tồn",
        "Số lượng tối thiểu", "Đơn giá", "Chiến dịch"
    };

    private static final String[] NORM_FORM_LABELS = {
        "Tên sản phẩm", "Vật tư (mã)", "Số lượng", "Đơn vị tính", "Chiến dịch"
    };

    @FXML
    private Label lblTotalItems;
    @FXML
    private Label lblLowStockCount;
    @FXML
    private Label lblTotalValue;
    @FXML
    private Label lblTotalCategories;
    @FXML
    private ComboBox<String> cboCategoryFilter;
    @FXML
    private ComboBox<String> cboCampaignFilter;
    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<InventoryItem> tableItems;

    @FXML
    private Label lblTotalNorms;
    @FXML
    private Label lblNormProducts;
    @FXML
    private ComboBox<String> cboNormCampaignFilter;
    @FXML
    private TextField txtNormSearch;

    @FXML
    private TableView<MaterialNorm> tableNorms;
    @FXML
    private TableColumn<MaterialNorm, String> colNormMa;
    @FXML
    private TableColumn<MaterialNorm, String> colNormSanPham;
    @FXML
    private TableColumn<MaterialNorm, String> colNormVatTu;
    @FXML
    private TableColumn<MaterialNorm, Double> colNormSoLuong;
    @FXML
    private TableColumn<MaterialNorm, String> colNormDonVi;
    @FXML
    private TableColumn<MaterialNorm, String> colNormChienDich;
    @FXML
    private TableColumn<InventoryItem, String> colMaVatTu;
    @FXML
    private TableColumn<InventoryItem, String> colTenVatTu;
    @FXML
    private TableColumn<InventoryItem, String> colDanhMuc;
    @FXML
    private TableColumn<InventoryItem, String> colDonVi;
    @FXML
    private TableColumn<InventoryItem, Integer> colSoLuong;
    @FXML
    private TableColumn<InventoryItem, String> colDonGia;
    @FXML
    private TableColumn<InventoryItem, String> colThanhTien;
    @FXML
    private TableColumn<InventoryItem, String> colChienDich;
    @FXML
    private TableColumn<InventoryItem, String> colNgayCapNhat;

    private FilteredList<InventoryItem> filteredItems;
    private FilteredList<MaterialNorm> filteredNorms;

    @FXML
    private void initialize() {
        colMaVatTu.setCellValueFactory(new PropertyValueFactory<>("maVatTu"));
        colMaVatTu.setVisible(false);
        colTenVatTu.setCellValueFactory(new PropertyValueFactory<>("tenVatTu"));
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("danhMuc"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGiaText"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTienText"));
        colChienDich.setText("Chiến dịch");
        colChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colNgayCapNhat.setCellValueFactory(new PropertyValueFactory<>("ngayCapNhat"));

        tableItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredItems = new FilteredList<>(AppData.getInventoryItems(), item -> true);
        tableItems.setItems(filteredItems);
        setupFilters();
        tableItems.setRowFactory(table -> {
            TableRow<InventoryItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableItems.getSelectionModel().select(row.getItem());
                    showDetail(row.getItem());
                }
            });
            return row;
        });
        refreshView();

        colNormMa.setCellValueFactory(new PropertyValueFactory<>("maDinhMuc"));
        colNormMa.setVisible(false);
        colNormSanPham.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colNormVatTu.setCellValueFactory(new PropertyValueFactory<>("tenVatTu"));
        colNormSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colNormDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colNormChienDich.setText("Chiến dịch");
        colNormChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));

        tableNorms.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredNorms = new FilteredList<>(AppData.getMaterialNorms(), item -> true);
        tableNorms.setItems(filteredNorms);
        setupNormFilters();
        updateNormSummary();
    }

    @FXML
    private void handleAddItem() {
        InventoryItem item = showDialog("Thêm vật tư", null);
        if (item == null) return;
        if (existsById(item.getMaVatTu(), null)) { DialogUtils.warning("Mã vật tư đã tồn tại."); return; }
        AppData.getInventoryItems().add(item);
        tableItems.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã thêm vật tư.");
    }

    @FXML
    private void handleUpdateItem() {
        InventoryItem selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn vật tư cần sửa."); return; }
        InventoryItem form = showDialog("Cập nhật vật tư", selected);
        if (form == null) return;
        if (existsById(form.getMaVatTu(), selected)) { DialogUtils.warning("Mã vật tư đã tồn tại."); return; }
        selected.setTenVatTu(form.getTenVatTu());
        selected.setDanhMuc(form.getDanhMuc());
        selected.setDonViTinh(form.getDonViTinh());
        selected.setSoLuongTon(form.getSoLuongTon());
        selected.setSoLuongToiThieu(form.getSoLuongToiThieu());
        selected.setDonGia(form.getDonGia());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setNgayCapNhat(form.getNgayCapNhat());
            tableItems.refresh();
        tableItems.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã cập nhật vật tư.");
    }

    @FXML
    private void handleDeleteItem() {
        InventoryItem selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn vật tư cần xóa."); return; }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa vật tư " + selected.getMaVatTu() + "?")) return;
        AppData.getInventoryItems().remove(selected);
        tableItems.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã xóa vật tư.");
    }

    @FXML
    private void handleImportStock() {
        InventoryItem selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn vật tư cần nhập kho."); return; }
        String[] result = CrudDialogUtils.showForm("Nhập kho", new String[]{"Số lượng nhập", "Ghi chú"}, new String[]{"", ""});
        if (result == null) return;
        try {
            int qty = Integer.parseInt(result[0].trim());
            if (qty <= 0) { DialogUtils.warning("Số lượng phải lớn hơn 0."); return; }
            selected.setSoLuongTon(selected.getSoLuongTon() + qty);
            selected.setNgayCapNhat(AppData.todayText());
            AppData.getOperations().add(new SystemRecord("Nhập kho", AppData.nextOperationId("NK"),
                    selected.getMaChienDich(), selected.getMaVatTu(), "Nhập kho " + selected.getTenVatTu(),
                    "Nhập " + qty + " " + selected.getDonViTinh() + " - " + result[1],
                    AppData.todayText(), "", "Đã nhập", currentUser(), "ADMIN001", "Bảng PhieuNhapKho"));
            tableItems.refresh();
            refreshView();
            DialogUtils.info("Đã nhập kho " + qty + " " + selected.getDonViTinh());
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số lượng không hợp lệ.");
        }
    }

    @FXML
    private void handleExportStock() {
        InventoryItem selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn vật tư cần xuất kho."); return; }
        String[] result = CrudDialogUtils.showForm("Xuất kho", new String[]{"Số lượng xuất", "Ghi chú", "Đích đến"}, new String[]{"", "", ""});
        if (result == null) return;
        try {
            int qty = Integer.parseInt(result[0].trim());
            if (qty <= 0) { DialogUtils.warning("Số lượng phải lớn hơn 0."); return; }
            if (qty > selected.getSoLuongTon()) { DialogUtils.warning("Số lượng tồn không đủ (" + selected.getSoLuongTon() + ")."); return; }
            selected.setSoLuongTon(selected.getSoLuongTon() - qty);
            selected.setNgayCapNhat(AppData.todayText());
            AppData.getOperations().add(new SystemRecord("Xuất kho", AppData.nextOperationId("XK"),
                    selected.getMaChienDich(), selected.getMaVatTu(), "Xuất kho " + selected.getTenVatTu(),
                    "Xuất " + qty + " " + selected.getDonViTinh() + " -> " + result[2] + " - " + result[1],
                    AppData.todayText(), "", "Đã xuất", currentUser(), "ADMIN001", "Bảng PhieuXuatKho"));
            if (selected.isLowStock()) {
                AppData.getAlerts().add(new Alert(AppData.nextAlertId(), "Tồn kho thấp",
                        selected.getTenVatTu() + " chỉ còn " + selected.getSoLuongTon() + " " + selected.getDonViTinh(),
                        "Trung bình", "Tồn kho", selected.getMaVatTu(), AppData.todayText(), "", "Chưa xử lý", "ADMIN001"));
            }
            tableItems.refresh();
            refreshView();
            DialogUtils.info("Đã xuất kho " + qty + " " + selected.getDonViTinh());
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số lượng không hợp lệ.");
        }
    }

    @FXML
    private void handleClearFilters() {
        cboCategoryFilter.setValue("Tất cả danh mục");
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        txtSearch.clear();
        applyFilters();
    }

    @FXML
    private void handleAddNorm() {
        String[] result = CrudDialogUtils.showForm("Thêm định mức nguyên liệu", NORM_FORM_LABELS,
                new String[]{"", "", "1", "", defaultCampaignOption()});
        if (result == null) return;
        String maVatTu = result[1].trim();
        if (result[0].isEmpty() || maVatTu.isEmpty()) { DialogUtils.warning("Vui lòng nhập tên sản phẩm và mã vật tư."); return; }
        if (!itemIdExists(maVatTu)) { DialogUtils.warning("Mã vật tư không tồn tại. Vui lòng nhập mã vật tư hợp lệ."); return; }
        try {
            double soLuong = Double.parseDouble(result[2]);
            if (soLuong <= 0) { DialogUtils.warning("Số lượng phải lớn hơn 0."); return; }
            String maChienDich = codeOf(result[4]);
            String ma = AppData.nextMaterialNormId();
            AppData.getMaterialNorms().add(new MaterialNorm(ma, result[0], maVatTu, soLuong, result[3], maChienDich));
            tableNorms.getSelectionModel().clearSelection();
            refreshView();
            DialogUtils.info("Đã thêm định mức nguyên liệu.");
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số lượng không hợp lệ.");
        }
    }

    @FXML
    private void handleUpdateNorm() {
        MaterialNorm selected = tableNorms.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn định mức cần sửa."); return; }
        String[] result = CrudDialogUtils.showForm("Cập nhật định mức", NORM_FORM_LABELS,
                new String[]{selected.getTenSanPham(), selected.getMaVatTu(), String.valueOf(selected.getSoLuong()),
                    selected.getDonVi(), selected.getMaChienDich() + " - " + selected.getTenChienDich()});
        if (result == null) return;
        String maVatTu = result[1].trim();
        if (result[0].isEmpty() || maVatTu.isEmpty()) { DialogUtils.warning("Vui lòng nhập tên sản phẩm và mã vật tư."); return; }
        if (!itemIdExists(maVatTu)) { DialogUtils.warning("Mã vật tư không tồn tại."); return; }
        try {
            double soLuong = Double.parseDouble(result[2]);
            if (soLuong <= 0) { DialogUtils.warning("Số lượng phải lớn hơn 0."); return; }
            selected.setTenSanPham(result[0]);
            selected.setMaVatTu(maVatTu);
            selected.setSoLuong(soLuong);
            selected.setDonVi(result[3]);
            selected.setMaChienDich(codeOf(result[4]));
            tableNorms.refresh();
            refreshView();
            DialogUtils.info("Đã cập nhật định mức.");
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số lượng không hợp lệ.");
        }
    }

    @FXML
    private void handleDeleteNorm() {
        MaterialNorm selected = tableNorms.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn định mức cần xóa."); return; }
        if (!DialogUtils.confirm("Xóa định mức " + selected.getMaDinhMuc() + "?")) return;
        AppData.getMaterialNorms().remove(selected);
        tableNorms.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã xóa định mức.");
    }

    @FXML
    private void handleClearNormFilters() {
        cboNormCampaignFilter.setValue("Tất cả chiến dịch");
        txtNormSearch.clear();
        applyNormFilters();
    }

    private boolean itemIdExists(String id) {
        return AppData.getInventoryItems().stream().anyMatch(i -> i.getMaVatTu().equalsIgnoreCase(id));
    }

    @FXML
    private void handleExport() {
        ExportUtils.exportTableToCsv(tableItems, "Xuất danh sách vật tư", "danh-sach-vat-tu.csv");
    }

    private InventoryItem showDialog(String title, InventoryItem current) {
        String[] values = current == null ? new String[]{"", "", "Cái", "0", "0", "0", defaultCampaignOption()}
                : new String[]{
                    current.getTenVatTu(), current.getDanhMuc(), current.getDonViTinh(),
                    String.valueOf(current.getSoLuongTon()), String.valueOf(current.getSoLuongToiThieu()),
                    String.format("%.0f", current.getDonGia()),
                    current.getMaChienDich() + " - " + current.getTenChienDich()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) return null;
        return buildItem(result, current);
    }

    private InventoryItem buildItem(String[] values, InventoryItem current) {
        String ma = current == null ? nextItemId() : current.getMaVatTu();
        String maChienDich = codeOf(values[6]);
        if (values[0].isEmpty()) { DialogUtils.warning("Vui lòng nhập tên vật tư."); return null; }
        try {
            return new InventoryItem(ma, values[0], values[1], values[2],
                    Integer.parseInt(values[3]), Integer.parseInt(values[4]),
                    FormatUtils.parseMoney(values[5]), maChienDich, AppData.todayText());
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số lượng hoặc đơn giá không hợp lệ.");
            return null;
        }
    }

    private void showDetail(InventoryItem item) {
        DetailDialogUtils.showDetails(tableItems, "Chi tiết vật tư - " + item.getTenVatTu(), new String[][]{
            {"Tên vật tư", item.getTenVatTu()},
            {"Danh mục", item.getDanhMuc()},
            {"Đơn vị tính", item.getDonViTinh()},
            {"Số lượng tồn", String.valueOf(item.getSoLuongTon())},
            {"Số lượng tối thiểu", String.valueOf(item.getSoLuongToiThieu())},
            {"Đơn giá", item.getDonGiaText()},
            {"Thành tiền", item.getThanhTienText()},
            {"Chiến dịch", item.getTenChienDich()},
            {"Ngày cập nhật", item.getNgayCapNhat()}
        });
    }

    private void setupFilters() {
        cboCategoryFilter.setItems(buildCategoryChoices());
        cboCampaignFilter.setItems(buildCampaignChoices());
        cboCategoryFilter.setValue("Tất cả danh mục");
        cboCampaignFilter.setValue("Tất cả chiến dịch");
        cboCategoryFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboCampaignFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        txtSearch.textProperty().addListener((o, a, b) -> applyFilters());
    }

    private void refreshView() {
        ObservableList<InventoryItem> all = AppData.getInventoryItems();
        lblTotalItems.setText(String.valueOf(all.size()));
        long low = all.stream().filter(InventoryItem::isLowStock).count();
        lblLowStockCount.setText(String.valueOf(low));
        double totalValue = all.stream().mapToDouble(i -> i.getSoLuongTon() * i.getDonGia()).sum();
        lblTotalValue.setText(FormatUtils.money(totalValue));
        long cats = all.stream().map(InventoryItem::getDanhMuc).filter(v -> v != null && !v.isEmpty()).distinct().count();
        lblTotalCategories.setText(String.valueOf(cats));
        applyFilters();
        tableItems.refresh();
        String nc = cboNormCampaignFilter.getValue();
        cboNormCampaignFilter.setItems(buildNormCampaignChoices());
        cboNormCampaignFilter.setValue(cboNormCampaignFilter.getItems().contains(nc) ? nc : "Tất cả chiến dịch");
        applyNormFilters();
        updateNormSummary();
    }

    private void setupNormFilters() {
        cboNormCampaignFilter.setItems(buildNormCampaignChoices());
        cboNormCampaignFilter.setValue("Tất cả chiến dịch");
        cboNormCampaignFilter.valueProperty().addListener((o, a, b) -> applyNormFilters());
        txtNormSearch.textProperty().addListener((o, a, b) -> applyNormFilters());
    }

    private void updateNormSummary() {
        ObservableList<MaterialNorm> all = AppData.getMaterialNorms();
        lblTotalNorms.setText(String.valueOf(all.size()));
        long products = all.stream().map(MaterialNorm::getTenSanPham).distinct().count();
        lblNormProducts.setText(String.valueOf(products));
    }

    private void applyNormFilters() {
        if (filteredNorms == null) return;
        String campaignId = codeOf(cboNormCampaignFilter.getValue());
        String query = normalize(value(txtNormSearch));
        boolean allCamp = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboNormCampaignFilter.getValue());
        filteredNorms.setPredicate(n
                -> (allCamp || n.getMaChienDich().equalsIgnoreCase(campaignId))
                && (query.isEmpty() || normalize(n.getTenSanPham() + " " + n.getTenVatTu() + " " + n.getMaVatTu()).contains(query)));
    }

    private ObservableList<String> buildNormCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel a : AppData.getActivities()) choices.add(a.getMaChienDich() + " - " + a.getTenChienDich());
        return choices;
    }

    private void applyFilters() {
        if (filteredItems == null) return;
        String cat = value(cboCategoryFilter);
        String campaignId = codeOf(cboCampaignFilter.getValue());
        String query = normalize(value(txtSearch));
        boolean allCat = cat.isEmpty() || "Tất cả danh mục".equals(cat);
        boolean allCamp = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboCampaignFilter.getValue());
        filteredItems.setPredicate(item
                -> (allCat || normalize(item.getDanhMuc()).contains(normalize(cat)))
                && (allCamp || item.getMaChienDich().equalsIgnoreCase(campaignId))
                && (query.isEmpty() || normalize(item.getTenVatTu() + " " + item.getDanhMuc() + " " + item.getDonViTinh()).contains(query)));
    }

    private ObservableList<String> buildCategoryChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả danh mục");
        AppData.getInventoryItems().stream().map(InventoryItem::getDanhMuc).filter(v -> v != null && !v.isEmpty()).distinct().forEach(choices::add);
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
    private boolean existsById(String id, InventoryItem current) {
        return AppData.getInventoryItems().stream().anyMatch(i -> i != current && i.getMaVatTu().equalsIgnoreCase(id));
    }
    private static String nextItemId() {
        int idx = AppData.getInventoryItems().size() + 1;
        String id; do { id = "VT" + String.format("%03d", idx++); } while (idExists(id));
        return id;
    }
    private static boolean idExists(String id) { return AppData.getInventoryItems().stream().anyMatch(i -> i.getMaVatTu().equalsIgnoreCase(id)); }

    private String getDonVi(String maVatTu) {
        for (InventoryItem item : AppData.getInventoryItems()) {
            if (item.getMaVatTu().equalsIgnoreCase(maVatTu)) return item.getDonViTinh();
        }
        return "";
    }

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
    @FXML private void handleTraining() throws IOException { App.setRoot("training"); }
    @FXML private void handleSponsors() throws IOException { App.setRoot("sponsors"); }
    @FXML private void handleDonations() throws IOException { App.setRoot("donations"); }
    @FXML private void handleOperations() throws IOException { App.setRoot("operations"); }
    @FXML private void handleInventory() throws IOException { App.setRoot("inventory"); }
    @FXML private void handleExpense() throws IOException { App.setRoot("expense"); }
    @FXML private void handleContent() throws IOException { App.setRoot("content"); }
    @FXML private void handleReports() throws IOException { App.setRoot("reports"); }
    @FXML private void handleScreening() throws IOException { App.setRoot("screening"); }
}
