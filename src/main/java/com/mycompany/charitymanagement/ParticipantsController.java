package com.mycompany.charitymanagement;

import java.io.IOException;
import java.text.Normalizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseButton;

public class ParticipantsController {

    private static final String[] FORM_LABELS = {
        "Tài khoản TNV", "Họ tên", "MSSV", "Số điện thoại",
        "Khoa", "Trường", "Chiến dịch tham gia", "Trạng thái duyệt", "Điểm đánh giá"
    };

    @FXML
    private TableView<ParticipantModel> tableParticipants;
    @FXML
    private TableColumn<ParticipantModel, String> colMaTaiKhoan;
    @FXML
    private TableColumn<ParticipantModel, String> colMaHoSo;
    @FXML
    private TableColumn<ParticipantModel, String> colHoTen;
    @FXML
    private TableColumn<ParticipantModel, String> colMssv;
    @FXML
    private TableColumn<ParticipantModel, String> colSoDienThoai;
    @FXML
    private TableColumn<ParticipantModel, String> colKhoa;
    @FXML
    private TableColumn<ParticipantModel, String> colTruong;
    @FXML
    private TableColumn<ParticipantModel, String> colMaChienDich;
    @FXML
    private TableColumn<ParticipantModel, String> colTrangThaiDuyet;
    @FXML
    private TableColumn<ParticipantModel, String> colDiemDanhGia;
    @FXML
    private ComboBox<String> cboParticipantCampaignFilter;
    @FXML
    private ComboBox<String> cboParticipantSchoolFilter;
    @FXML
    private ComboBox<String> cboParticipantStatusFilter;
    @FXML
    private TextField txtParticipantSearch;

    private FilteredList<ParticipantModel> filteredParticipants;

    @FXML
    private void initialize() {
        colMaTaiKhoan.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaTaiKhoan()));
        colMaTaiKhoan.setVisible(false);
        colMaHoSo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaHoSo()));
        colMaHoSo.setVisible(false);
        colHoTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHoTen()));
        colMssv.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMssv()));
        colSoDienThoai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoDienThoai()));
        colKhoa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKhoa()));
        colTruong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTruong()));
        colMaChienDich.setText("Chiến dịch");
        colMaChienDich.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenChienDich()));
        colTrangThaiDuyet.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThaiDuyet()));
        colDiemDanhGia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDiemDanhGia()));

        tableParticipants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableParticipants.setFixedCellSize(32.0);
        filteredParticipants = new FilteredList<>(AppData.getParticipants(), item -> true);
        tableParticipants.setItems(filteredParticipants);
        setupParticipantFilters();
        tableParticipants.setRowFactory(table -> {
            TableRow<ParticipantModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableParticipants.getSelectionModel().select(row.getItem());
                    showParticipantDetail(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleAddParticipant() {
        ParticipantModel participant = showParticipantDialog("Thêm sinh viên / tình nguyện viên", null);
        if (participant == null) {
            return;
        }
        if (existsParticipant(participant, null)) {
            DialogUtils.warning("TNV này đã được ghi nhận trong chiến dịch đã chọn.");
            return;
        }
        String syncError = BusinessService.syncVolunteerRegistration(participant, currentUsername());
        if (syncError != null) {
            DialogUtils.warning(syncError);
            return;
        }

        AppData.getParticipants().add(participant);
        DatabaseRepository.saveParticipant(participant);
        tableParticipants.getSelectionModel().clearSelection();
        refreshParticipantView();
        DialogUtils.info("Đã thêm sinh viên/TNV.");
    }

    @FXML
    private void handleUpdateParticipant() {
        ParticipantModel selected = tableParticipants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn sinh viên/TNV cần cập nhật.");
            return;
        }

        ParticipantModel form = showParticipantDialog("Cập nhật sinh viên / tình nguyện viên", selected);
        if (form == null) {
            return;
        }
        if (existsParticipant(form, selected)) {
            DialogUtils.warning("TNV này đã được ghi nhận trong chiến dịch đã chọn.");
            return;
        }
        String syncError = BusinessService.syncVolunteerRegistration(form, currentUsername());
        if (syncError != null) {
            DialogUtils.warning(syncError);
            return;
        }

        selected.setMaTaiKhoan(form.getMaTaiKhoan());
        selected.setMaHoSo(form.getMaHoSo());
        selected.setHoTen(form.getHoTen());
        selected.setMssv(form.getMssv());
        selected.setSoDienThoai(form.getSoDienThoai());
        selected.setKhoa(form.getKhoa());
        selected.setTruong(form.getTruong());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setTrangThaiDuyet(form.getTrangThaiDuyet());
        selected.setDiemDanhGia(form.getDiemDanhGia());
        DatabaseRepository.saveParticipant(selected);
        tableParticipants.refresh();
        tableParticipants.getSelectionModel().clearSelection();
        refreshParticipantView();
        DialogUtils.info("Đã cập nhật sinh viên/TNV.");
    }

    @FXML
    private void handleDeleteParticipant() {
        ParticipantModel selected = tableParticipants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.warning("Vui lòng chọn sinh viên/TNV cần xóa.");
            return;
        }
        String deleteError = BusinessRules.canDeleteParticipant(selected);
        if (deleteError != null) {
            DialogUtils.warning(deleteError);
            return;
        }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa sinh viên/TNV " + selected.getMaTaiKhoan() + "?")) {
            return;
        }

        AppData.getParticipants().remove(selected);
        DatabaseRepository.deleteParticipant(selected);
        tableParticipants.getSelectionModel().clearSelection();
        refreshParticipantView();
        DialogUtils.info("Đã xóa sinh viên/TNV.");
    }

    @FXML
    private void handleClearParticipantFilters() {
        cboParticipantCampaignFilter.setValue("Tất cả chiến dịch");
        cboParticipantSchoolFilter.setValue("Tất cả trường");
        cboParticipantStatusFilter.setValue("Tất cả trạng thái");
        txtParticipantSearch.clear();
        applyParticipantFilters();
    }

    @FXML
    private void handleExportParticipants() {
        ExportUtils.exportTableToCsv(tableParticipants, "Xuất danh sách tình nguyện viên", "danh-sach-tinh-nguyen-vien.csv");
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

    private ParticipantModel showParticipantDialog(String title, ParticipantModel current) {
        String[] values = current == null ? new String[]{defaultVolunteerOption(), "", "", "", "Khoa Công nghệ phần mềm", "UIT", defaultCampaignOption(), "Chờ duyệt", ""}
                : new String[]{
                    current.getMaTaiKhoan() + " - " + current.getHoTen(), current.getHoTen(), current.getMssv(),
                    current.getSoDienThoai(), current.getKhoa(), current.getTruong(),
                    current.getMaChienDich() + " - " + campaignName(current.getMaChienDich()),
                    current.getTrangThaiDuyet(), current.getDiemDanhGia()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) {
            return null;
        }
        return buildParticipant(result, current);
    }

    private ParticipantModel buildParticipant(String[] values, ParticipantModel current) {
        String maTaiKhoan = codeOf(values[0]);
        UserAccount account = findAccount(maTaiKhoan);
        String hoTen = values[1].isEmpty() && account != null ? account.getDisplayName() : values[1];
        String maHoSo = current == null ? (account == null ? AppData.nextProfileId() : account.getLinkedId()) : current.getMaHoSo();
        String maChienDich = codeOf(values[6]);

        if (maTaiKhoan.isEmpty() || maHoSo.isEmpty() || hoTen.isEmpty() || maChienDich.isEmpty()) {
            DialogUtils.warning("Vui lòng chọn tài khoản TNV, chiến dịch và nhập họ tên.");
            return null;
        }

        ParticipantModel participant = new ParticipantModel(maTaiKhoan, maHoSo, hoTen, values[2], values[3],
                values[4], values[5], maChienDich, values[7], values[8]);
        String error = BusinessRules.validateParticipant(participant);
        if (error != null) {
            DialogUtils.warning(error);
            return null;
        }
        return participant;
    }

    private boolean existsParticipant(ParticipantModel participant, ParticipantModel current) {
        return AppData.getParticipants().stream()
                .anyMatch(item -> item != current
                && item.getMaTaiKhoan().equalsIgnoreCase(participant.getMaTaiKhoan())
                && item.getMaChienDich().equalsIgnoreCase(participant.getMaChienDich()));
    }

    private void showParticipantDetail(ParticipantModel participant) {
        ActivityModel campaign = AppData.findCampaign(participant.getMaChienDich());
        DetailDialogUtils.showDetails(tableParticipants, "Chi tiết tình nguyện viên - " + participant.getHoTen(), new String[][]{
            {"Họ tên", participant.getHoTen()},
            {"MSSV", participant.getMssv()},
            {"Số điện thoại", participant.getSoDienThoai()},
            {"Khoa", participant.getKhoa()},
            {"Trường", participant.getTruong()},
            {"Tên chiến dịch", campaign == null ? "" : campaign.getTenChienDich()},
            {"Trạng thái duyệt", participant.getTrangThaiDuyet()},
            {"Điểm đánh giá", participant.getDiemDanhGia()},
            {"Vai trò trong hệ thống", "Tình nguyện viên/sinh viên tham gia chiến dịch"},
            {"Ghi chú", "Sinh viên thuộc hệ thống ĐHQG-TPHCM"}
        });
    }

    private String defaultVolunteerOption() {
        return AppData.getAccounts().stream()
                .filter(UserAccount::isVolunteer)
                .findFirst()
                .map(account -> account.getUsername() + " - " + account.getDisplayName())
                .orElse("");
    }

    private void setupParticipantFilters() {
        cboParticipantCampaignFilter.setItems(buildCampaignFilterChoices());
        cboParticipantSchoolFilter.setItems(buildSchoolFilterChoices());
        cboParticipantStatusFilter.setItems(buildStatusFilterChoices());
        cboParticipantCampaignFilter.setValue("Tất cả chiến dịch");
        cboParticipantSchoolFilter.setValue("Tất cả trường");
        cboParticipantStatusFilter.setValue("Tất cả trạng thái");
        cboParticipantCampaignFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilters());
        cboParticipantSchoolFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilters());
        cboParticipantStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilters());
        txtParticipantSearch.textProperty().addListener((observable, oldValue, newValue) -> applyParticipantFilters());
    }

    private void refreshParticipantView() {
        String currentCampaign = cboParticipantCampaignFilter.getValue();
        String currentSchool = cboParticipantSchoolFilter.getValue();
        String currentStatus = cboParticipantStatusFilter.getValue();
        cboParticipantCampaignFilter.setItems(buildCampaignFilterChoices());
        cboParticipantSchoolFilter.setItems(buildSchoolFilterChoices());
        cboParticipantStatusFilter.setItems(buildStatusFilterChoices());
        cboParticipantCampaignFilter.setValue(cboParticipantCampaignFilter.getItems().contains(currentCampaign) ? currentCampaign : "Tất cả chiến dịch");
        cboParticipantSchoolFilter.setValue(cboParticipantSchoolFilter.getItems().contains(currentSchool) ? currentSchool : "Tất cả trường");
        cboParticipantStatusFilter.setValue(cboParticipantStatusFilter.getItems().contains(currentStatus) ? currentStatus : "Tất cả trạng thái");
        applyParticipantFilters();
        tableParticipants.refresh();
    }

    private void applyParticipantFilters() {
        if (filteredParticipants == null) {
            return;
        }
        String campaignId = codeOf(cboParticipantCampaignFilter.getValue());
        String school = cboParticipantSchoolFilter.getValue() == null ? "" : cboParticipantSchoolFilter.getValue();
        String status = cboParticipantStatusFilter.getValue() == null ? "" : cboParticipantStatusFilter.getValue();
        String query = normalize(value(txtParticipantSearch));
        boolean allCampaigns = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboParticipantCampaignFilter.getValue());
        boolean allSchools = school.isEmpty() || "Tất cả trường".equals(school);
        boolean allStatuses = status.isEmpty() || "Tất cả trạng thái".equals(status);
        filteredParticipants.setPredicate(participant
                -> (allCampaigns || participant.getMaChienDich().equalsIgnoreCase(campaignId))
                && (allSchools || safe(participant.getTruong()).equalsIgnoreCase(school))
                && (allStatuses || safe(participant.getTrangThaiDuyet()).equalsIgnoreCase(status))
                && (query.isEmpty() || normalize(safe(participant.getHoTen()) + " "
                + safe(participant.getMssv()) + " " + safe(participant.getSoDienThoai()) + " "
                + safe(participant.getKhoa()) + " " + safe(participant.getTruong()) + " "
                + safe(participant.getTenChienDich()) + " " + safe(participant.getTrangThaiDuyet())).contains(query)));
    }

    private ObservableList<String> buildCampaignFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel activity : AppData.getActivities()) {
            choices.add(activity.getMaChienDich() + " - " + activity.getTenChienDich());
        }
        return choices;
    }

    private ObservableList<String> buildSchoolFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trường");
        AppData.getParticipants().stream()
                .map(ParticipantModel::getTruong)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .forEach(choices::add);
        return choices;
    }

    private ObservableList<String> buildStatusFilterChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trạng thái");
        AppData.getParticipants().stream()
                .map(ParticipantModel::getTrangThaiDuyet)
                .filter(value -> value != null && !value.trim().isEmpty())
                .distinct()
                .forEach(choices::add);
        return choices;
    }

    private String defaultCampaignOption() {
        return AppData.getActivities().stream()
                .findFirst()
                .map(activity -> activity.getMaChienDich() + " - " + activity.getTenChienDich())
                .orElse("");
    }

    private UserAccount findAccount(String username) {
        return AppData.getAccounts().stream()
                .filter(account -> account.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    private String campaignName(String campaignId) {
        ActivityModel campaign = AppData.findCampaign(campaignId);
        return campaign == null ? campaignId : campaign.getTenChienDich();
    }

    private String currentUsername() {
        UserAccount user = UserSession.getCurrentUser();
        return user == null ? "ADMIN001" : user.getUsername();
    }

    private String codeOf(String option) {
        if (option == null) {
            return "";
        }
        int split = option.indexOf(" - ");
        return split >= 0 ? option.substring(0, split).trim() : option.trim();
    }

    private String value(TextField textField) {
        return textField == null || textField.getText() == null ? "" : textField.getText().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }
}
