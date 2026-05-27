package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;

public class OperationsController {

    private static final String TYPE_CAMPAIGN = "Chiến dịch";
    private static final String TYPE_REGISTRATION = "Đăng ký TNV";
    private static final String TYPE_WORK = "Công việc";
    private static final String TYPE_ATTENDANCE = "Điểm danh";
    private static final String TYPE_EXPENSE = "Chi tiêu";
    private static final String TYPE_VOLUNTEER_PROOF = "Minh chứng TNV";
    private static final String TYPE_ITEM_EXPORT = "Xuất vật phẩm";
    private static final String TYPE_DONATION = "Quyên góp";

    @FXML
    private VBox formSection;
    @FXML
    private Button btnSaveRecord;
    @FXML
    private Button btnCloseForm;

    @FXML
    private TextField txtMaVanHanh;
    @FXML
    private TextField txtNguoiTao;
    @FXML
    private TextField txtNgayTao;
    @FXML
    private TextField txtNgayXuLy;
    @FXML
    private ComboBox<String> cboLoaiNghiepVu;
    @FXML
    private ComboBox<String> cboChienDich;
    @FXML
    private ComboBox<String> cboDoiTuongLienKet;
    @FXML
    private ComboBox<String> cboTrangThai;
    @FXML
    private ComboBox<String> cboNguoiXuLy;
    @FXML
    private TextField txtTieuDe;
    @FXML
    private TextField txtNoiDung;
    @FXML
    private TextField txtGhiChu;
    @FXML
    private ComboBox<String> cboOperationTypeFilter;
    @FXML
    private ComboBox<String> cboOperationCampaignFilter;
    @FXML
    private ComboBox<String> cboOperationStatusFilter;
    @FXML
    private TextField txtOperationSearch;

    @FXML
    private TableView<SystemRecord> tablePendingRecords;
    @FXML
    private TableColumn<SystemRecord, String> colPendingLoai;
    @FXML
    private TableColumn<SystemRecord, String> colPendingMa;
    @FXML
    private TableColumn<SystemRecord, String> colPendingChienDich;
    @FXML
    private TableColumn<SystemRecord, String> colPendingDoiTuong;
    @FXML
    private TableColumn<SystemRecord, String> colPendingTieuDe;
    @FXML
    private TableColumn<SystemRecord, String> colPendingNguoiXuLy;
    @FXML
    private TableColumn<SystemRecord, String> colPendingNgayTao;
    @FXML
    private TableColumn<SystemRecord, String> colPendingNgayXuLy;
    @FXML
    private TableColumn<SystemRecord, String> colPendingTrangThai;

    @FXML
    private TableView<SystemRecord> tableDoneRecords;
    @FXML
    private TableColumn<SystemRecord, String> colDoneLoai;
    @FXML
    private TableColumn<SystemRecord, String> colDoneMa;
    @FXML
    private TableColumn<SystemRecord, String> colDoneChienDich;
    @FXML
    private TableColumn<SystemRecord, String> colDoneDoiTuong;
    @FXML
    private TableColumn<SystemRecord, String> colDoneTieuDe;
    @FXML
    private TableColumn<SystemRecord, String> colDoneNguoiXuLy;
    @FXML
    private TableColumn<SystemRecord, String> colDoneNgayTao;
    @FXML
    private TableColumn<SystemRecord, String> colDoneNgayXuLy;
    @FXML
    private TableColumn<SystemRecord, String> colDoneTrangThai;

    private final ObservableList<SystemRecord> pendingRecords = FXCollections.observableArrayList();
    private final ObservableList<SystemRecord> doneRecords = FXCollections.observableArrayList();
    private FilteredList<SystemRecord> filteredPendingRecords;
    private FilteredList<SystemRecord> filteredDoneRecords;
    private boolean addMode;

    @FXML
    private void initialize() {
        cboLoaiNghiepVu.setItems(FXCollections.observableArrayList(
                TYPE_CAMPAIGN,
                TYPE_REGISTRATION,
                TYPE_WORK,
                TYPE_ATTENDANCE,
                TYPE_EXPENSE,
                TYPE_VOLUNTEER_PROOF,
                TYPE_ITEM_EXPORT,
                TYPE_DONATION
        ));
        cboNguoiXuLy.setItems(buildUserOptions());
        cboChienDich.setItems(buildCampaignOptions());
        setupOperationFilters();

        cboLoaiNghiepVu.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateStatusOptions(cboTrangThai.getValue());
            updateLinkedObjects();
        });
        cboChienDich.valueProperty().addListener((observable, oldValue, newValue) -> updateLinkedObjects());
        cboTrangThai.valueProperty().addListener((observable, oldValue, newValue) -> updateProcessingDate());

        configureColumns(colPendingLoai, colPendingMa, colPendingChienDich, colPendingDoiTuong,
                colPendingTieuDe, colPendingNguoiXuLy, colPendingNgayTao, colPendingNgayXuLy, colPendingTrangThai);
        configureColumns(colDoneLoai, colDoneMa, colDoneChienDich, colDoneDoiTuong,
                colDoneTieuDe, colDoneNguoiXuLy, colDoneNgayTao, colDoneNgayXuLy, colDoneTrangThai);
        txtMaVanHanh.setVisible(false);
        txtMaVanHanh.setManaged(false);

        tablePendingRecords.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDoneRecords.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablePendingRecords.setFixedCellSize(32.0);
        tableDoneRecords.setFixedCellSize(32.0);
        filteredPendingRecords = new FilteredList<>(pendingRecords, item -> true);
        filteredDoneRecords = new FilteredList<>(doneRecords, item -> true);
        tablePendingRecords.setItems(filteredPendingRecords);
        tableDoneRecords.setItems(filteredDoneRecords);
        syncMissingOperationRecords();
        AppData.getOperations().addListener((ListChangeListener<SystemRecord>) change -> {
            refreshTables();
            updateLinkedObjects();
        });
        AppData.getParticipants().addListener((ListChangeListener<ParticipantModel>) change -> {
            syncMissingOperationRecords();
            updateLinkedObjects();
            refreshTables();
        });
        AppData.getDonations().addListener((ListChangeListener<DonationModel>) change -> {
            syncMissingOperationRecords();
            updateLinkedObjects();
            refreshTables();
        });
        AppData.getActivities().addListener((ListChangeListener<ActivityModel>) change -> refreshOperationChoices());

        tablePendingRecords.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected != null) {
                        tableDoneRecords.getSelectionModel().clearSelection();
                        fillForm(selected);
                    }
                }
        );
        tableDoneRecords.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected != null) {
                        tablePendingRecords.getSelectionModel().clearSelection();
                        fillForm(selected);
                    }
                }
        );

        refreshTables();
        resetForm();
        hideForm();
        applyPendingNavigationFocus();
    }

    @FXML
    private void handleShowAddForm() {
        tablePendingRecords.getSelectionModel().clearSelection();
        tableDoneRecords.getSelectionModel().clearSelection();
        clearManualFields();
        resetForm();
        showForm(true);
    }

    @FXML
    private void handleSaveRecord() {
        if (!isFormOpen()) {
            DialogUtils.warning("Bấm Thêm mới hoặc chọn một dòng trong bảng trước khi lưu.");
            return;
        }

        SystemRecord form = readForm();
        if (form == null) {
            return;
        }

        SystemRecord selected = getSelectedRecord();
        if (addMode || selected == null) {
            saveNewRecord(form);
            return;
        }

        updateRecord(selected, form);
    }

    @FXML
    private void handleAddRecord() {
        handleShowAddForm();
    }

    @FXML
    private void handleUpdateRecord() {
        handleSaveRecord();
    }

    @FXML
    private void handleDeleteRecord() {
        SystemRecord selected = getSelectedRecord();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa bản ghi vận hành " + selected.getMaChinh() + "?")) {
            return;
        }

        AppData.getOperations().remove(selected);
        DatabaseRepository.deleteOperation(selected);
        refreshTables();
        handleClearForm();
        DialogUtils.info("Đã xóa bản ghi vận hành.");
    }

    @FXML
    private void handleExportPendingRecords() {
        ExportUtils.exportTableToCsv(tablePendingRecords, "Xuất danh sách chờ xử lý", "van-hanh-cho-xu-ly.csv");
    }

    @FXML
    private void handleExportDoneRecords() {
        ExportUtils.exportTableToCsv(tableDoneRecords, "Xuất danh sách đã xử lý", "van-hanh-da-xu-ly.csv");
    }

    @FXML
    private void handleClearForm() {
        tablePendingRecords.getSelectionModel().clearSelection();
        tableDoneRecords.getSelectionModel().clearSelection();
        clearManualFields();
        resetForm();
        hideForm();
    }

    @FXML
    private void handleClearOperationFilters() {
        cboOperationTypeFilter.setValue("Tất cả nghiệp vụ");
        cboOperationCampaignFilter.setValue("Tất cả chiến dịch");
        cboOperationStatusFilter.setValue("Tất cả trạng thái");
        txtOperationSearch.clear();
        applyOperationFilters();
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

    private void saveNewRecord(SystemRecord record) {
        if (existsById(record.getMaChinh(), null)) {
            DialogUtils.warning("Mã vận hành đã tồn tại.");
            return;
        }
        String error = BusinessRules.validateOperation(record);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }

        AppData.getOperations().add(record);
        syncRelatedData(record);
        refreshTables();
        handleClearForm();
        focusOperationRecord(record);
        DialogUtils.info("Đã thêm bản ghi vận hành.");
    }

    private void updateRecord(SystemRecord selected, SystemRecord form) {
        if (existsById(form.getMaChinh(), selected)) {
            DialogUtils.warning("Mã vận hành đã tồn tại.");
            return;
        }
        String error = BusinessRules.validateOperation(form);
        if (error != null) {
            DialogUtils.warning(error);
            return;
        }

        selected.setNhomBang(form.getNhomBang());
        selected.setMaChinh(form.getMaChinh());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setMaLienKet(form.getMaLienKet());
        selected.setTieuDe(form.getTieuDe());
        selected.setNoiDung(form.getNoiDung());
        selected.setNgay(form.getNgay());
        selected.setNgayXuLy(form.getNgayXuLy());
        selected.setTrangThai(form.getTrangThai());
        selected.setNguoiTao(form.getNguoiTao());
        selected.setNguoiXuLy(form.getNguoiXuLy());
        selected.setGhiChu(form.getGhiChu());
        syncRelatedData(selected);
        refreshTables();
        handleClearForm();
        focusOperationRecord(selected);
        DialogUtils.info("Đã cập nhật bản ghi vận hành.");
    }

    private void configureColumns(TableColumn<SystemRecord, String> colLoai,
            TableColumn<SystemRecord, String> colMa,
            TableColumn<SystemRecord, String> colChienDich,
            TableColumn<SystemRecord, String> colDoiTuong,
            TableColumn<SystemRecord, String> colTieuDe,
            TableColumn<SystemRecord, String> colNguoiXuLy,
            TableColumn<SystemRecord, String> colNgayTao,
            TableColumn<SystemRecord, String> colNgayXuLy,
            TableColumn<SystemRecord, String> colTrangThai) {
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNhomBang()));
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaChinh()));
        colMa.setVisible(false);
        colChienDich.setText("Chiến dịch");
        colChienDich.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colDoiTuong.setText("Đối tượng liên quan");
        colDoiTuong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenLienKet()));
        colTieuDe.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTieuDe()));
        colNguoiXuLy.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNguoiXuLy()));
        colNgayTao.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayTao()));
        colNgayXuLy.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNgayXuLy()));
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
    }

    private void resetForm() {
        txtMaVanHanh.setText(AppData.nextOperationId("VH"));
        txtNguoiTao.setText(currentUsername());
        txtNgayTao.setText(AppData.todayText());
        txtNgayXuLy.clear();

        if (!cboLoaiNghiepVu.getItems().isEmpty()) {
            cboLoaiNghiepVu.setValue(cboLoaiNghiepVu.getItems().get(0));
        }
        if (!cboChienDich.getItems().isEmpty()) {
            cboChienDich.setValue(cboChienDich.getItems().get(0));
        }
        if (!cboNguoiXuLy.getItems().isEmpty()) {
            cboNguoiXuLy.setValue(cboNguoiXuLy.getItems().get(0));
        }
        updateStatusOptions(null);
        updateLinkedObjects();
        updateProcessingDate();
    }

    private void clearManualFields() {
        txtTieuDe.clear();
        txtNoiDung.clear();
        txtGhiChu.clear();
    }

    private void showForm(boolean addMode) {
        this.addMode = addMode;
        formSection.setVisible(true);
        formSection.setManaged(true);
        btnSaveRecord.setDisable(false);
        btnCloseForm.setDisable(false);
    }

    private void hideForm() {
        addMode = false;
        formSection.setVisible(false);
        formSection.setManaged(false);
        btnSaveRecord.setDisable(true);
        btnCloseForm.setDisable(true);
    }

    private boolean isFormOpen() {
        return formSection != null && formSection.isVisible();
    }

    private void refreshTables() {
        pendingRecords.clear();
        doneRecords.clear();
        for (SystemRecord record : AppData.getOperations()) {
            if (isDoneStatus(record.getTrangThai())) {
                doneRecords.add(record);
            } else {
                pendingRecords.add(record);
            }
        }
        applyOperationFilters();
        tablePendingRecords.refresh();
        tableDoneRecords.refresh();
    }

    private void focusOperationRecord(SystemRecord record) {
        if (record == null) {
            return;
        }
        if (cboOperationTypeFilter.getItems().contains(record.getNhomBang())) {
            cboOperationTypeFilter.setValue(record.getNhomBang());
        }
        String campaignOption = findOption(cboOperationCampaignFilter.getItems(), record.getMaChienDich());
        if (campaignOption != null) {
            cboOperationCampaignFilter.setValue(campaignOption);
        }
        if (cboOperationStatusFilter.getItems().contains(record.getTrangThai())) {
            cboOperationStatusFilter.setValue(record.getTrangThai());
        } else {
            cboOperationStatusFilter.setValue("Tất cả trạng thái");
        }
        txtOperationSearch.clear();
        applyOperationFilters();

        if (isDoneStatus(record.getTrangThai())) {
            tablePendingRecords.getSelectionModel().clearSelection();
            tableDoneRecords.getSelectionModel().select(record);
            tableDoneRecords.scrollTo(record);
        } else {
            tableDoneRecords.getSelectionModel().clearSelection();
            tablePendingRecords.getSelectionModel().select(record);
            tablePendingRecords.scrollTo(record);
        }
    }

    private void syncMissingOperationRecords() {
        for (ParticipantModel participant : AppData.getParticipants()) {
            if (!operationExists(TYPE_REGISTRATION, participant.getMaTaiKhoan(), participant.getMaChienDich())) {
                BusinessService.syncVolunteerRegistration(participant, participant.getMaTaiKhoan());
            }
        }
        for (DonationModel donation : AppData.getDonations()) {
            if (!operationExists(TYPE_DONATION, donation.getMaQuyenGop(), donation.getHoatDong())) {
                BusinessService.syncDonationOperation(donation, donationActor(donation));
            }
        }
    }

    private boolean operationExists(String type, String linkId, String campaignId) {
        return AppData.getOperations().stream()
                .anyMatch(record -> sameType(record.getNhomBang(), type)
                && record.getMaLienKet().equalsIgnoreCase(linkId)
                && record.getMaChienDich().equalsIgnoreCase(campaignId));
    }

    private String donationActor(DonationModel donation) {
        for (UserAccount account : AppData.getAccounts()) {
            if (account.getDisplayName().equalsIgnoreCase(donation.getNguoiQuyenGop())) {
                return account.getUsername();
            }
        }
        return donation.getNguoiQuyenGop();
    }

    private void refreshOperationChoices() {
        String selectedCampaign = extractCode(cboChienDich.getValue());
        cboChienDich.setItems(buildCampaignOptions());
        String campaignOption = findOption(cboChienDich.getItems(), selectedCampaign);
        if (campaignOption != null) {
            cboChienDich.setValue(campaignOption);
        } else if (!cboChienDich.getItems().isEmpty()) {
            cboChienDich.setValue(cboChienDich.getItems().get(0));
        }

        String selectedFilter = extractCode(cboOperationCampaignFilter.getValue());
        cboOperationCampaignFilter.setItems(buildCampaignFilterOptions());
        String filterOption = findOption(cboOperationCampaignFilter.getItems(), selectedFilter);
        if (filterOption != null) {
            cboOperationCampaignFilter.setValue(filterOption);
        } else {
            cboOperationCampaignFilter.setValue("Tất cả chiến dịch");
        }
        updateLinkedObjects();
        applyOperationFilters();
    }

    private void setupOperationFilters() {
        cboOperationTypeFilter.setItems(FXCollections.observableArrayList(
                "Tất cả nghiệp vụ",
                TYPE_CAMPAIGN,
                TYPE_REGISTRATION,
                TYPE_WORK,
                TYPE_ATTENDANCE,
                TYPE_EXPENSE,
                TYPE_VOLUNTEER_PROOF,
                TYPE_ITEM_EXPORT,
                TYPE_DONATION
        ));
        cboOperationCampaignFilter.setItems(buildCampaignFilterOptions());
        cboOperationStatusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả trạng thái", "Chờ duyệt", "Đang xét", "Đang phân công",
                "Chờ xác nhận", "Đã duyệt", "Đã phân công", "Có mặt",
                "Xác nhận", "Đã xác nhận", "Đã xuất", "Từ chối"
        ));
        cboOperationTypeFilter.setValue("Tất cả nghiệp vụ");
        cboOperationCampaignFilter.setValue("Tất cả chiến dịch");
        cboOperationStatusFilter.setValue("Tất cả trạng thái");
        cboOperationTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyOperationFilters());
        cboOperationCampaignFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyOperationFilters());
        cboOperationStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyOperationFilters());
        txtOperationSearch.textProperty().addListener((observable, oldValue, newValue) -> applyOperationFilters());
    }

    private ObservableList<String> buildCampaignFilterOptions() {
        ObservableList<String> options = FXCollections.observableArrayList("Tất cả chiến dịch");
        options.addAll(buildCampaignOptions());
        return options;
    }

    private void applyOperationFilters() {
        if (filteredPendingRecords == null || filteredDoneRecords == null) {
            return;
        }
        filteredPendingRecords.setPredicate(this::matchesOperationFilters);
        filteredDoneRecords.setPredicate(this::matchesOperationFilters);
    }

    private boolean matchesOperationFilters(SystemRecord record) {
        String type = cboOperationTypeFilter.getValue();
        String campaignId = extractCode(cboOperationCampaignFilter.getValue());
        String status = cboOperationStatusFilter.getValue();
        String query = normalize(value(txtOperationSearch));

        boolean typeMatches = type == null || type.equals("Tất cả nghiệp vụ") || sameType(type, record.getNhomBang());
        boolean campaignMatches = campaignId.isEmpty() || record.getMaChienDich().equalsIgnoreCase(campaignId);
        boolean statusMatches = status == null || status.equals("Tất cả trạng thái")
                || normalize(record.getTrangThai()).equals(normalize(status));
        boolean queryMatches = query.isEmpty() || normalize(String.join(" ",
                safe(record.getNhomBang()),
                safe(record.getTenChienDich()),
                safe(record.getTenLienKet()),
                safe(record.getTieuDe()),
                safe(record.getNoiDung()),
                safe(record.getNguoiXuLy()),
                safe(record.getTrangThai())
        )).contains(query);
        return typeMatches && campaignMatches && statusMatches && queryMatches;
    }

    private ObservableList<String> buildCampaignOptions() {
        ObservableList<String> options = FXCollections.observableArrayList();
        for (ActivityModel campaign : AppData.getActivities()) {
            options.add(campaign.getMaChienDich() + " - " + campaign.getTenChienDich());
        }
        return options;
    }

    private ObservableList<String> buildUserOptions() {
        ObservableList<String> options = FXCollections.observableArrayList();
        for (UserAccount account : AppData.getAccounts()) {
            options.add(account.getUsername() + " - " + account.getDisplayName());
        }
        return options;
    }

    private void updateStatusOptions(String preferredStatus) {
        ObservableList<String> options = statusOptionsFor(cboLoaiNghiepVu.getValue());
        cboTrangThai.setItems(options);

        String selected = preferredStatus;
        if (selected == null || !options.contains(selected)) {
            selected = options.isEmpty() ? "" : options.get(0);
        }
        cboTrangThai.setValue(selected);
        updateProcessingDate();
    }

    private ObservableList<String> statusOptionsFor(String type) {
        if (sameType(type, TYPE_ATTENDANCE)) {
            return FXCollections.observableArrayList("Chờ duyệt", "Có mặt");
        }
        if (sameType(type, TYPE_WORK)) {
            return FXCollections.observableArrayList("Đang phân công", "Đã phân công");
        }
        if (sameType(type, TYPE_REGISTRATION)) {
            return FXCollections.observableArrayList("Chờ duyệt", "Đang xét", "Đã duyệt", "Từ chối");
        }
        if (sameType(type, TYPE_CAMPAIGN)) {
            return FXCollections.observableArrayList("Chờ duyệt", "Đang xét", "Đã duyệt");
        }
        if (sameType(type, TYPE_EXPENSE)) {
            return FXCollections.observableArrayList("Chờ duyệt", "Đã duyệt");
        }
        if (sameType(type, TYPE_VOLUNTEER_PROOF)) {
            return FXCollections.observableArrayList("Chờ xác nhận", "Xác nhận");
        }
        if (sameType(type, TYPE_ITEM_EXPORT)) {
            return FXCollections.observableArrayList("Chờ xác nhận", "Đã xuất");
        }
        if (sameType(type, TYPE_DONATION)) {
            return FXCollections.observableArrayList("Chờ xác nhận", "Đã xác nhận", "Từ chối");
        }
        return FXCollections.observableArrayList("Chờ duyệt", "Đã duyệt");
    }

    private void updateLinkedObjects() {
        String type = cboLoaiNghiepVu.getValue();
        String campaignId = extractCode(cboChienDich.getValue());
        ObservableList<String> options = FXCollections.observableArrayList();

        if (sameType(type, TYPE_CAMPAIGN)) {
            options.setAll(buildCampaignOptions());
        } else if (sameType(type, TYPE_REGISTRATION) || sameType(type, TYPE_ATTENDANCE)) {
            addParticipantOptions(options, campaignId);
        } else if (sameType(type, TYPE_DONATION)) {
            addDonationOptions(options, campaignId);
        } else {
            addOperationLinkOptions(options, type, campaignId);
        }

        if (options.isEmpty() && !campaignId.isEmpty() && !sameType(type, TYPE_DONATION)) {
            String code = defaultLinkCode(type);
            options.add(code + " - Tạo liên kết mới cho " + campaignName(campaignId));
        }

        String previous = cboDoiTuongLienKet.getValue();
        cboDoiTuongLienKet.setItems(options);
        String preserved = findOption(options, extractCode(previous));
        cboDoiTuongLienKet.setValue(preserved != null ? preserved : (options.isEmpty() ? "" : options.get(0)));
    }

    private void addParticipantOptions(ObservableList<String> options, String campaignId) {
        for (ParticipantModel participant : AppData.getParticipants()) {
            if (campaignId.isEmpty() || participant.getMaChienDich().equalsIgnoreCase(campaignId)) {
                options.add(participant.getMaTaiKhoan() + " - " + participant.getHoTen()
                        + " - " + participant.getMaChienDich());
            }
        }
    }

    private void addOperationLinkOptions(ObservableList<String> options, String type, String campaignId) {
        for (SystemRecord record : AppData.getOperations()) {
            if (sameType(record.getNhomBang(), type)
                    && (campaignId.isEmpty() || record.getMaChienDich().equalsIgnoreCase(campaignId))) {
                options.add(record.getMaLienKet() + " - " + record.getTieuDe());
            }
        }
        if (!options.isEmpty() || campaignId.isEmpty()) {
            return;
        }
        for (SystemRecord record : AppData.getOperations()) {
            if (sameType(record.getNhomBang(), type)) {
                options.add(record.getMaLienKet() + " - " + record.getTieuDe()
                        + " - " + record.getMaChienDich());
            }
        }
    }

    private void addDonationOptions(ObservableList<String> options, String campaignId) {
        for (DonationModel donation : AppData.getDonations()) {
            if (campaignId.isEmpty() || donation.getHoatDong().equalsIgnoreCase(campaignId)) {
                options.add(donation.getMaQuyenGop() + " - " + donation.getHinhThuc()
                        + " - " + FormatUtils.money(donation.getSoTien()));
            }
        }
    }

    private void updateProcessingDate() {
        if (isDoneStatus(cboTrangThai.getValue())) {
            txtNgayXuLy.setText(AppData.todayText());
        } else {
            txtNgayXuLy.clear();
        }
    }

    private SystemRecord readForm() {
        String maVanHanh = value(txtMaVanHanh);
        String loaiNghiepVu = cboLoaiNghiepVu.getValue();
        String maChienDich = extractCode(cboChienDich.getValue());
        String doiTuongLienKet = extractCode(cboDoiTuongLienKet.getValue());
        String trangThai = cboTrangThai.getValue();
        String nguoiXuLy = extractCode(cboNguoiXuLy.getValue());
        String tieuDe = value(txtTieuDe);
        String noiDung = value(txtNoiDung);
        String ghiChu = value(txtGhiChu);

        if (maVanHanh.isEmpty()) {
            maVanHanh = AppData.nextOperationId("VH");
        }
        if (loaiNghiepVu == null || loaiNghiepVu.isEmpty()
                || maChienDich.isEmpty() || doiTuongLienKet.isEmpty()
                || trangThai == null || trangThai.isEmpty()
                || nguoiXuLy.isEmpty() || tieuDe.isEmpty()) {
            DialogUtils.warning("Vui lòng chọn loại nghiệp vụ, chiến dịch, đối tượng liên kết, trạng thái, người xử lý và nhập tiêu đề.");
            return null;
        }

        String ngayXuLy = isDoneStatus(trangThai) ? AppData.todayText() : "";
        txtNgayXuLy.setText(ngayXuLy);
        return new SystemRecord(loaiNghiepVu, maVanHanh, maChienDich, doiTuongLienKet,
                tieuDe, noiDung, value(txtNgayTao), ngayXuLy, trangThai,
                value(txtNguoiTao), nguoiXuLy, ghiChu);
    }

    private void fillForm(SystemRecord selected) {
        txtMaVanHanh.setText(selected.getMaChinh());
        txtNguoiTao.setText(selected.getNguoiTao());
        txtNgayTao.setText(selected.getNgayTao());
        txtTieuDe.setText(selected.getTieuDe());
        txtNoiDung.setText(selected.getNoiDung());
        txtGhiChu.setText(selected.getGhiChu());

        cboLoaiNghiepVu.setValue(selected.getNhomBang());
        cboChienDich.setValue(findOption(cboChienDich.getItems(), selected.getMaChienDich()));
        updateStatusOptions(selected.getTrangThai());
        updateLinkedObjects();
        ensureLinkedOption(selected);
        cboDoiTuongLienKet.setValue(findOption(cboDoiTuongLienKet.getItems(), selected.getMaLienKet()));
        cboTrangThai.setValue(selected.getTrangThai());
        cboNguoiXuLy.setValue(findOption(cboNguoiXuLy.getItems(), selected.getNguoiXuLy()));
        txtNgayXuLy.setText(selected.getNgayXuLy());
        showForm(false);
    }

    private void ensureLinkedOption(SystemRecord selected) {
        if (findOption(cboDoiTuongLienKet.getItems(), selected.getMaLienKet()) != null) {
            return;
        }
        ObservableList<String> options = FXCollections.observableArrayList(cboDoiTuongLienKet.getItems());
        options.add(selected.getMaLienKet() + " - " + selected.getTieuDe());
        cboDoiTuongLienKet.setItems(options);
    }

    private void syncRelatedData(SystemRecord record) {
        BusinessService.applyOperation(record);
        if (sameType(record.getNhomBang(), TYPE_CAMPAIGN)) {
            ActivityModel campaign = AppData.findCampaign(record.getMaLienKet());
            if (campaign == null) {
                campaign = AppData.findCampaign(record.getMaChienDich());
            }
            if (campaign != null) {
                campaign.setTrangThai(record.getTrangThai());
            }
            return;
        }

        if (sameType(record.getNhomBang(), TYPE_REGISTRATION)) {
            for (ParticipantModel participant : AppData.getParticipants()) {
                if (participant.getMaTaiKhoan().equalsIgnoreCase(record.getMaLienKet())
                        && participant.getMaChienDich().equalsIgnoreCase(record.getMaChienDich())) {
                    participant.setTrangThaiDuyet(record.getTrangThai());
                }
            }
        }
    }

    private void applyPendingNavigationFocus() {
        NavigationIntent.OperationFocus focus = NavigationIntent.consumeOperationFocus();
        if (focus == null) {
            return;
        }
        if (!focus.type().isBlank() && cboOperationTypeFilter.getItems().contains(focus.type())) {
            cboOperationTypeFilter.setValue(focus.type());
        }
        if (!focus.campaignId().isBlank()) {
            String option = findOption(cboOperationCampaignFilter.getItems(), focus.campaignId());
            if (option != null) {
                cboOperationCampaignFilter.setValue(option);
            }
        }
        if (!focus.status().isBlank() && cboOperationStatusFilter.getItems().contains(focus.status())) {
            cboOperationStatusFilter.setValue(focus.status());
        }
        if (!focus.query().isBlank()) {
            txtOperationSearch.setText(focus.query());
        }
        applyOperationFilters();
    }

    private SystemRecord getSelectedRecord() {
        SystemRecord selected = tablePendingRecords.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected;
        }
        return tableDoneRecords.getSelectionModel().getSelectedItem();
    }

    private boolean existsById(String id, SystemRecord current) {
        return AppData.getOperations().stream()
                .anyMatch(item -> item != current && item.getMaChinh().equalsIgnoreCase(id));
    }

    private boolean isDoneStatus(String status) {
        String normalized = normalize(status);
        if (normalized.contains("cho") || normalized.contains("dang")) {
            return false;
        }
        return normalized.contains("da")
                || normalized.contains("co mat")
                || normalized.contains("xac nhan")
                || normalized.contains("tu choi")
                || normalized.contains("hoan tat");
    }

    private boolean sameType(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String text = Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD);
        return text.replaceAll("\\p{M}", "").replace("đ", "d");
    }

    private String extractCode(String option) {
        if (option == null) {
            return "";
        }
        int split = option.indexOf(" - ");
        return split >= 0 ? option.substring(0, split).trim() : option.trim();
    }

    private String findOption(ObservableList<String> options, String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (String option : options) {
            if (extractCode(option).equalsIgnoreCase(code)) {
                return option;
            }
        }
        return null;
    }

    private String defaultLinkCode(String type) {
        if (sameType(type, TYPE_WORK)) {
            return nextLinkCode("CV");
        }
        if (sameType(type, TYPE_EXPENSE)) {
            return nextLinkCode("CT");
        }
        if (sameType(type, TYPE_VOLUNTEER_PROOF)) {
            return nextLinkCode("MC");
        }
        if (sameType(type, TYPE_ITEM_EXPORT)) {
            return nextLinkCode("PX");
        }
        if (sameType(type, TYPE_DONATION)) {
            return AppData.nextDonationId();
        }
        return extractCode(cboChienDich.getValue());
    }

    private String nextLinkCode(String prefix) {
        int index = 1;
        String id;
        do {
            id = prefix + String.format("%03d", index++);
        } while (linkCodeExists(id));
        return id;
    }

    private boolean linkCodeExists(String id) {
        for (SystemRecord record : AppData.getOperations()) {
            if (record.getMaLienKet().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private String campaignName(String campaignId) {
        ActivityModel campaign = AppData.findCampaign(campaignId);
        return campaign == null ? "Chiến dịch" : campaign.getTenChienDich();
    }

    private String currentUsername() {
        UserAccount user = UserSession.getCurrentUser();
        return user == null ? "ADMIN" : user.getUsername();
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
