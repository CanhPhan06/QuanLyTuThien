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

public class TrainingController {

    private static final String[] FORM_LABELS = {
        "Tên khóa học", "Mô tả", "Ngày bắt đầu", "Ngày kết thúc",
        "Số giờ đào tạo", "Giảng viên", "Chiến dịch liên kết",
        "Sĩ số tối đa", "Trạng thái"
    };

    private static final String[] ENROLL_FORM_LABELS = {
        "Khóa học", "Học viên (mã tài khoản)", "Họ tên học viên", "Ngày tham gia", "Ghi chú"
    };

    @FXML
    private Label lblTotalCourses;
    @FXML
    private Label lblActiveCourses;
    @FXML
    private Label lblTotalStudents;
    @FXML
    private Label lblCourseHours;
    @FXML
    private ComboBox<String> cboCourseStatusFilter;
    @FXML
    private ComboBox<String> cboCourseCampaignFilter;
    @FXML
    private TextField txtCourseSearch;

    @FXML
    private TableView<TrainingCourse> tableCourses;

    @FXML
    private Label lblTotalEnrollments;
    @FXML
    private Label lblActiveEnrollments;
    @FXML
    private Label lblCompletedEnrollments;
    @FXML
    private ComboBox<String> cboEnrollStatusFilter;
    @FXML
    private ComboBox<String> cboEnrollCourseFilter;
    @FXML
    private TextField txtEnrollSearch;

    @FXML
    private TableView<TrainingEnrollment> tableEnrollments;
    @FXML
    private TableColumn<TrainingEnrollment, String> colEnrollMa;
    @FXML
    private TableColumn<TrainingEnrollment, String> colEnrollHocVien;
    @FXML
    private TableColumn<TrainingEnrollment, String> colEnrollKhoaHoc;
    @FXML
    private TableColumn<TrainingEnrollment, String> colEnrollNgayThamGia;
    @FXML
    private TableColumn<TrainingEnrollment, String> colEnrollTrangThai;
    @FXML
    private TableColumn<TrainingEnrollment, String> colEnrollGhiChu;
    @FXML
    private TableColumn<TrainingCourse, String> colMaKhoaHoc;
    @FXML
    private TableColumn<TrainingCourse, String> colTenKhoaHoc;
    @FXML
    private TableColumn<TrainingCourse, String> colNgayBatDau;
    @FXML
    private TableColumn<TrainingCourse, String> colNgayKetThuc;
    @FXML
    private TableColumn<TrainingCourse, Integer> colSoGio;
    @FXML
    private TableColumn<TrainingCourse, String> colGiangVien;
    @FXML
    private TableColumn<TrainingCourse, String> colChienDich;
    @FXML
    private TableColumn<TrainingCourse, String> colTienDo;
    @FXML
    private TableColumn<TrainingCourse, String> colTrangThai;

    private FilteredList<TrainingCourse> filteredCourses;
    private FilteredList<TrainingEnrollment> filteredEnrollments;

    @FXML
    private void initialize() {
        colMaKhoaHoc.setCellValueFactory(new PropertyValueFactory<>("maKhoaHoc"));
        colMaKhoaHoc.setVisible(false);
        colTenKhoaHoc.setCellValueFactory(new PropertyValueFactory<>("tenKhoaHoc"));
        colNgayBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colSoGio.setCellValueFactory(new PropertyValueFactory<>("soGio"));
        colGiangVien.setCellValueFactory(new PropertyValueFactory<>("giangVien"));
        colChienDich.setText("Chiến dịch");
        colChienDich.setCellValueFactory(new PropertyValueFactory<>("tenChienDich"));
        colTienDo.setCellValueFactory(new PropertyValueFactory<>("tienDoText"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        tableCourses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredCourses = new FilteredList<>(AppData.getTrainingCourses(), item -> true);
        tableCourses.setItems(filteredCourses);
        setupFilters();
        tableCourses.setRowFactory(table -> {
            TableRow<TrainingCourse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    tableCourses.getSelectionModel().select(row.getItem());
                    showDetail(row.getItem());
                }
            });
            return row;
        });
        updateSummary();

        colEnrollMa.setCellValueFactory(new PropertyValueFactory<>("maGhiDanh"));
        colEnrollMa.setVisible(false);
        colEnrollHocVien.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colEnrollKhoaHoc.setCellValueFactory(new PropertyValueFactory<>("tenKhoaHoc"));
        colEnrollNgayThamGia.setCellValueFactory(new PropertyValueFactory<>("ngayThamGia"));
        colEnrollTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colEnrollGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        tableEnrollments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filteredEnrollments = new FilteredList<>(AppData.getTrainingEnrollments(), item -> true);
        tableEnrollments.setItems(filteredEnrollments);
        setupEnrollFilters();
        updateEnrollSummary();
    }

    @FXML
    private void handleAddCourse() {
        TrainingCourse course = showDialog("Thêm khóa đào tạo", null);
        if (course == null) return;
        if (existsById(course.getMaKhoaHoc(), null)) {
            DialogUtils.warning("Mã khóa học đã tồn tại.");
            return;
        }
        AppData.getTrainingCourses().add(course);
        tableCourses.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã thêm khóa đào tạo.");
    }

    @FXML
    private void handleUpdateCourse() {
        TrainingCourse selected = tableCourses.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn khóa học cần sửa."); return; }
        TrainingCourse form = showDialog("Cập nhật khóa đào tạo", selected);
        if (form == null) return;
        if (existsById(form.getMaKhoaHoc(), selected)) {
            DialogUtils.warning("Mã khóa học đã tồn tại.");
            return;
        }
        selected.setTenKhoaHoc(form.getTenKhoaHoc());
        selected.setMoTa(form.getMoTa());
        selected.setNgayBatDau(form.getNgayBatDau());
        selected.setNgayKetThuc(form.getNgayKetThuc());
        selected.setSoGio(form.getSoGio());
        selected.setGiangVien(form.getGiangVien());
        selected.setMaChienDich(form.getMaChienDich());
        selected.setSiSoToiDa(form.getSiSoToiDa());
        selected.setTrangThai(form.getTrangThai());
        tableCourses.refresh();
        tableCourses.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã cập nhật khóa đào tạo.");
    }

    @FXML
    private void handleDeleteCourse() {
        TrainingCourse selected = tableCourses.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn khóa học cần xóa."); return; }
        if (!DialogUtils.confirm("Bạn có chắc muốn xóa khóa học " + selected.getMaKhoaHoc() + "?")) return;
        AppData.getTrainingCourses().remove(selected);
        tableCourses.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã xóa khóa đào tạo.");
    }

    @FXML
    private void handleClearFilters() {
        cboCourseStatusFilter.setValue("Tất cả trạng thái");
        cboCourseCampaignFilter.setValue("Tất cả chiến dịch");
        txtCourseSearch.clear();
        applyFilters();
    }

    @FXML
    private void handleAddEnrollment() {
        String[] result = CrudDialogUtils.showForm("Ghi danh học viên", ENROLL_FORM_LABELS,
                new String[]{defaultCourseOption(), "", "", AppData.todayText(), ""});
        if (result == null) return;
        String maKhoaHoc = codeOf(result[0]);
        if (maKhoaHoc.isEmpty()) { DialogUtils.warning("Vui lòng chọn khóa học."); return; }
        if (result[1].isEmpty() || result[2].isEmpty()) { DialogUtils.warning("Vui lòng nhập mã tài khoản và họ tên học viên."); return; }
        String ma = nextEnrollId();
        AppData.getTrainingEnrollments().add(new TrainingEnrollment(ma, maKhoaHoc, result[1], result[2], result[3], "Đang học", result[4]));
        TrainingCourse course = findCourse(maKhoaHoc);
        if (course != null) course.setSiSoHienTai(course.getSiSoHienTai() + 1);
        tableEnrollments.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã ghi danh học viên.");
    }

    @FXML
    private void handleUpdateEnrollment() {
        TrainingEnrollment selected = tableEnrollments.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn học viên cần cập nhật."); return; }
        String[] result = CrudDialogUtils.showForm("Cập nhật tiến độ",
                new String[]{"Trạng thái", "Ghi chú"},
                new String[]{selected.getTrangThai(), selected.getGhiChu()});
        if (result == null) return;
        String oldStatus = selected.getTrangThai();
        selected.setTrangThai(result[0]);
        selected.setGhiChu(result[1]);
        if (!"Đang học".equals(oldStatus) && "Đang học".equals(result[0])) {
            TrainingCourse course = findCourse(selected.getMaKhoaHoc());
            if (course != null) course.setSiSoHienTai(course.getSiSoHienTai() + 1);
        }
        if ("Đang học".equals(oldStatus) && !"Đang học".equals(result[0])) {
            TrainingCourse course = findCourse(selected.getMaKhoaHoc());
            if (course != null && course.getSiSoHienTai() > 0) course.setSiSoHienTai(course.getSiSoHienTai() - 1);
        }
        tableEnrollments.refresh();
        refreshView();
        DialogUtils.info("Đã cập nhật tiến độ.");
    }

    @FXML
    private void handleDeleteEnrollment() {
        TrainingEnrollment selected = tableEnrollments.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.warning("Vui lòng chọn học viên cần hủy ghi danh."); return; }
        if (!DialogUtils.confirm("Hủy ghi danh học viên " + selected.getHoTen() + "?")) return;
        TrainingCourse course = findCourse(selected.getMaKhoaHoc());
        if (course != null && "Đang học".equals(selected.getTrangThai()) && course.getSiSoHienTai() > 0) {
            course.setSiSoHienTai(course.getSiSoHienTai() - 1);
        }
        AppData.getTrainingEnrollments().remove(selected);
        tableEnrollments.getSelectionModel().clearSelection();
        refreshView();
        DialogUtils.info("Đã hủy ghi danh.");
    }

    @FXML
    private void handleClearEnrollFilters() {
        cboEnrollStatusFilter.setValue("Tất cả trạng thái");
        cboEnrollCourseFilter.setValue("Tất cả khóa học");
        txtEnrollSearch.clear();
        applyEnrollFilters();
    }

    @FXML
    private void handleExport() {
        ExportUtils.exportTableToCsv(tableCourses, "Xuất danh sách khóa đào tạo", "danh-sach-khoa-dao-tao.csv");
    }

    private TrainingCourse showDialog(String title, TrainingCourse current) {
        String[] values = current == null ? new String[]{"", "", AppData.todayText(), "", "0", "", defaultCampaignOption(), "30", "Đang lên kế hoạch"}
                : new String[]{
                    current.getTenKhoaHoc(), current.getMoTa(), current.getNgayBatDau(), current.getNgayKetThuc(),
                    String.valueOf(current.getSoGio()), current.getGiangVien(),
                    current.getMaChienDich() + " - " + current.getTenChienDich(),
                    String.valueOf(current.getSiSoToiDa()), current.getTrangThai()
                };
        String[] result = CrudDialogUtils.showForm(title, FORM_LABELS, values);
        if (result == null) return null;
        return buildCourse(result, current);
    }

    private TrainingCourse buildCourse(String[] values, TrainingCourse current) {
        String ma = current == null ? nextCourseId() : current.getMaKhoaHoc();
        String maChienDich = codeOf(values[6]);
        if (values[0].isEmpty()) { DialogUtils.warning("Vui lòng nhập tên khóa học."); return null; }
        try {
            int soGio = values[4].isEmpty() ? 0 : Integer.parseInt(values[4]);
            int siSo = values[7].isEmpty() ? 30 : Integer.parseInt(values[7]);
            TrainingCourse course = new TrainingCourse(ma, values[0], values[1], values[2], values[3],
                    soGio, values[5], maChienDich, siSo, current == null ? 0 : current.getSiSoHienTai(), values[8]);
            String error = BusinessRules.validateTrainingCourse(course);
            if (error != null) { DialogUtils.warning(error); return null; }
            return course;
        } catch (NumberFormatException ex) {
            DialogUtils.warning("Số giờ hoặc sĩ số không hợp lệ.");
            return null;
        }
    }

    private void showDetail(TrainingCourse course) {
        DetailDialogUtils.showDetails(tableCourses, "Chi tiết khóa đào tạo - " + course.getTenKhoaHoc(), new String[][]{
            {"Tên khóa học", course.getTenKhoaHoc()},
            {"Mô tả", course.getMoTa()},
            {"Thời gian", course.getNgayBatDau() + " - " + course.getNgayKetThuc()},
            {"Số giờ đào tạo", String.valueOf(course.getSoGio())},
            {"Giảng viên", course.getGiangVien()},
            {"Chiến dịch liên kết", course.getTenChienDich()},
            {"Sĩ số", course.getSiSoHienTai() + "/" + course.getSiSoToiDa()},
            {"Trạng thái", course.getTrangThai()}
        });
    }

    private void setupFilters() {
        cboCourseStatusFilter.setItems(buildStatusChoices());
        cboCourseCampaignFilter.setItems(buildCampaignChoices());
        cboCourseStatusFilter.setValue("Tất cả trạng thái");
        cboCourseCampaignFilter.setValue("Tất cả chiến dịch");
        cboCourseStatusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        cboCourseCampaignFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        txtCourseSearch.textProperty().addListener((o, a, b) -> applyFilters());
    }

    private void refreshView() {
        String cs = cboCourseStatusFilter.getValue();
        String cc = cboCourseCampaignFilter.getValue();
        cboCourseStatusFilter.setItems(buildStatusChoices());
        cboCourseCampaignFilter.setItems(buildCampaignChoices());
        cboCourseStatusFilter.setValue(cboCourseStatusFilter.getItems().contains(cs) ? cs : "Tất cả trạng thái");
        cboCourseCampaignFilter.setValue(cboCourseCampaignFilter.getItems().contains(cc) ? cc : "Tất cả chiến dịch");
        applyFilters();
        updateSummary();
        tableCourses.refresh();
        String es = cboEnrollStatusFilter.getValue();
        String ec = cboEnrollCourseFilter.getValue();
        cboEnrollStatusFilter.setItems(buildEnrollStatusChoices());
        cboEnrollCourseFilter.setItems(buildEnrollCourseChoices());
        cboEnrollStatusFilter.setValue(cboEnrollStatusFilter.getItems().contains(es) ? es : "Tất cả trạng thái");
        cboEnrollCourseFilter.setValue(cboEnrollCourseFilter.getItems().contains(ec) ? ec : "Tất cả khóa học");
        applyEnrollFilters();
        updateEnrollSummary();
    }

    private void updateSummary() {
        ObservableList<TrainingCourse> all = AppData.getTrainingCourses();
        lblTotalCourses.setText(String.valueOf(all.size()));
        long active = all.stream().filter(c -> "Đang mở".equals(c.getTrangThai()) || "Đang lên kế hoạch".equals(c.getTrangThai())).count();
        lblActiveCourses.setText(String.valueOf(active));
        int totalStudents = all.stream().mapToInt(TrainingCourse::getSiSoHienTai).sum();
        lblTotalStudents.setText(String.valueOf(totalStudents));
        int totalHours = all.stream().mapToInt(TrainingCourse::getSoGio).sum();
        lblCourseHours.setText(String.valueOf(totalHours));
    }

    private void applyFilters() {
        if (filteredCourses == null) return;
        String status = value(cboCourseStatusFilter);
        String campaignId = codeOf(cboCourseCampaignFilter.getValue());
        String query = normalize(value(txtCourseSearch));
        boolean allStatus = status.isEmpty() || "Tất cả trạng thái".equals(status);
        boolean allCampaign = campaignId.isEmpty() || "Tất cả chiến dịch".equals(cboCourseCampaignFilter.getValue());
        filteredCourses.setPredicate(course
                -> (allStatus || normalize(course.getTrangThai()).contains(normalize(status)))
                && (allCampaign || course.getMaChienDich().equalsIgnoreCase(campaignId))
                && (query.isEmpty() || normalize(course.getTenKhoaHoc() + " " + course.getGiangVien() + " " + course.getMoTa()).contains(query)));
    }

    private ObservableList<String> buildStatusChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trạng thái");
        AppData.getTrainingCourses().stream().map(TrainingCourse::getTrangThai).filter(v -> v != null && !v.isEmpty()).distinct().forEach(choices::add);
        return choices;
    }

    private ObservableList<String> buildCampaignChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả chiến dịch");
        for (ActivityModel a : AppData.getActivities()) choices.add(a.getMaChienDich() + " - " + a.getTenChienDich());
        return choices;
    }

    private void setupEnrollFilters() {
        cboEnrollStatusFilter.setItems(buildEnrollStatusChoices());
        cboEnrollCourseFilter.setItems(buildEnrollCourseChoices());
        cboEnrollStatusFilter.setValue("Tất cả trạng thái");
        cboEnrollCourseFilter.setValue("Tất cả khóa học");
        cboEnrollStatusFilter.valueProperty().addListener((o, a, b) -> applyEnrollFilters());
        cboEnrollCourseFilter.valueProperty().addListener((o, a, b) -> applyEnrollFilters());
        txtEnrollSearch.textProperty().addListener((o, a, b) -> applyEnrollFilters());
    }

    private void updateEnrollSummary() {
        ObservableList<TrainingEnrollment> all = AppData.getTrainingEnrollments();
        lblTotalEnrollments.setText(String.valueOf(all.size()));
        long active = all.stream().filter(e -> "Đang học".equals(e.getTrangThai())).count();
        lblActiveEnrollments.setText(String.valueOf(active));
        long completed = all.stream().filter(e -> "Hoàn thành".equals(e.getTrangThai())).count();
        lblCompletedEnrollments.setText(String.valueOf(completed));
    }

    private void applyEnrollFilters() {
        if (filteredEnrollments == null) return;
        String status = value(cboEnrollStatusFilter);
        String courseId = codeOf(cboEnrollCourseFilter.getValue());
        String query = normalize(value(txtEnrollSearch));
        boolean allStatus = status.isEmpty() || "Tất cả trạng thái".equals(status);
        boolean allCourse = courseId.isEmpty() || "Tất cả khóa học".equals(cboEnrollCourseFilter.getValue());
        filteredEnrollments.setPredicate(e
                -> (allStatus || normalize(e.getTrangThai()).contains(normalize(status)))
                && (allCourse || e.getMaKhoaHoc().equalsIgnoreCase(courseId))
                && (query.isEmpty() || normalize(e.getHoTen() + " " + e.getMaTaiKhoan() + " " + e.getGhiChu()).contains(query)));
    }

    private ObservableList<String> buildEnrollStatusChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả trạng thái", "Đang học", "Hoàn thành", "Bỏ học");
        return choices;
    }

    private ObservableList<String> buildEnrollCourseChoices() {
        ObservableList<String> choices = FXCollections.observableArrayList("Tất cả khóa học");
        for (TrainingCourse c : AppData.getTrainingCourses()) {
            choices.add(c.getMaKhoaHoc() + " - " + c.getTenKhoaHoc());
        }
        return choices;
    }

    private String defaultCourseOption() {
        return AppData.getTrainingCourses().stream().findFirst()
                .map(c -> c.getMaKhoaHoc() + " - " + c.getTenKhoaHoc()).orElse("");
    }

    private TrainingCourse findCourse(String maKhoaHoc) {
        for (TrainingCourse c : AppData.getTrainingCourses()) {
            if (c.getMaKhoaHoc().equalsIgnoreCase(maKhoaHoc)) return c;
        }
        return null;
    }

    private static String nextEnrollId() {
        int idx = AppData.getTrainingEnrollments().size() + 1;
        String id; do { id = "GH" + String.format("%03d", idx++); } while (enrollIdExists(id));
        return id;
    }
    private static boolean enrollIdExists(String id) { return AppData.getTrainingEnrollments().stream().anyMatch(e -> e.getMaGhiDanh().equalsIgnoreCase(id)); }

    private String defaultCampaignOption() {
        return AppData.getActivities().stream().findFirst()
                .map(a -> a.getMaChienDich() + " - " + a.getTenChienDich()).orElse("");
    }

    private boolean existsById(String id, TrainingCourse current) {
        return AppData.getTrainingCourses().stream().anyMatch(c -> c != current && c.getMaKhoaHoc().equalsIgnoreCase(id));
    }

    private static String nextCourseId() {
        int idx = AppData.getTrainingCourses().size() + 1;
        String id; do { id = "DT" + String.format("%03d", idx++); } while (idExists(id));
        return id;
    }
    private static boolean idExists(String id) { return AppData.getTrainingCourses().stream().anyMatch(c -> c.getMaKhoaHoc().equalsIgnoreCase(id)); }

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
