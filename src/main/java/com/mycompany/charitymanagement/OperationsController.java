package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class OperationsController {

    private static final String TYPE_CAMPAIGN = "Chiến dịch";
    private static final String TYPE_REGISTRATION = "Đăng ký TNV";
    private static final String TYPE_WORK = "Công việc";
    private static final String TYPE_ATTENDANCE = "Điểm danh";
    private static final String TYPE_EXPENSE = "Chi tiêu";
    private static final String TYPE_VOLUNTEER_PROOF = "Minh chứng TNV";
    private static final String TYPE_ITEM_EXPORT = "Xuất vật phẩm";

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
                TYPE_ITEM_EXPORT
        ));
        cboNguoiXuLy.setItems(buildUserOptions());
        cboChienDich.setItems(buildCampaignOptions());

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

        tablePendingRecords.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDoneRecords.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablePendingRecords.setItems(pendingRecords);
        tableDoneRecords.setItems(doneRecords);

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

        AppData.getOperations().remove(selected);
        refreshTables();
        handleClearForm();
        DialogUtils.info("Đã xóa bản ghi vận hành.");
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
        UserSession.clear();
        App.setRoot("primary");
    }

    private void saveNewRecord(SystemRecord record) {
        if (existsById(record.getMaChinh(), null)) {
            DialogUtils.warning("Mã vận hành đã tồn tại.");
            return;
        }

        AppData.getOperations().add(record);
        syncRelatedData(record);
        refreshTables();
        handleClearForm();
        DialogUtils.info("Đã thêm bản ghi vận hành.");
    }

    private void updateRecord(SystemRecord selected, SystemRecord form) {
        if (existsById(form.getMaChinh(), selected)) {
            DialogUtils.warning("Mã vận hành đã tồn tại.");
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
        colLoai.setCellValueFactory(new PropertyValueFactory<>("nhomBang"));
        colMa.setCellValueFactory(new PropertyValueFactory<>("maChinh"));
        colChienDich.setCellValueFactory(new PropertyValueFactory<>("maChienDich"));
        colDoiTuong.setCellValueFactory(new PropertyValueFactory<>("maLienKet"));
        colTieuDe.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colNguoiXuLy.setCellValueFactory(new PropertyValueFactory<>("nguoiXuLy"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colNgayXuLy.setCellValueFactory(new PropertyValueFactory<>("ngayXuLy"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
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
        tablePendingRecords.refresh();
        tableDoneRecords.refresh();
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
            return FXCollections.observableArrayList("Chờ duyệt", "Đang xét", "Đã duyệt");
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
        } else {
            addOperationLinkOptions(options, type, campaignId);
        }

        if (options.isEmpty() && !campaignId.isEmpty()) {
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
        return text.replaceAll("\\p{M}", "").replace('đ', 'd');
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
}
