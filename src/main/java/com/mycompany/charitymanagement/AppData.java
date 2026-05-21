package com.mycompany.charitymanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class AppData {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final ObservableList<UserAccount> accounts = FXCollections.observableArrayList();
    private static final ObservableList<ActivityModel> activities = FXCollections.observableArrayList();
    private static final ObservableList<ParticipantModel> participants = FXCollections.observableArrayList();
    private static final ObservableList<SponsorModel> sponsors = FXCollections.observableArrayList();
    private static final ObservableList<DonationModel> donations = FXCollections.observableArrayList();
    private static final ObservableList<SystemRecord> operations = FXCollections.observableArrayList();
    private static final ObservableList<SystemRecord> contents = FXCollections.observableArrayList();
    private static final ObservableList<TrainingCourse> trainingCourses = FXCollections.observableArrayList();
    private static final ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();
    private static final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private static final ObservableList<Alert> alerts = FXCollections.observableArrayList();
    private static final ObservableList<MaterialNorm> materialNorms = FXCollections.observableArrayList();
    private static final ObservableList<TrainingEnrollment> trainingEnrollments = FXCollections.observableArrayList();

    static {
        addAccounts();
        addCampaigns();
        addParticipants();
        addSponsors();
        addDonations();
        addOperations();
        addContents();
        addTrainingCourses();
        addInventoryItems();
        addExpenses();
        addAlerts();
        addMaterialNorms();
        addTrainingEnrollments();
    }

    private AppData() {
    }

    private static void addAccounts() {
        accounts.add(new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Quản lý hệ thống", "TK000"));

        String[] adminNames = {
            "Nguyễn Quản Trị", "Trần Minh Quân", "Lê Hoài An", "Phạm Gia Bảo", "Võ Thanh Tâm",
            "Đặng Ngọc Hân", "Bùi Quốc Việt", "Hoàng Khánh Linh", "Đỗ Hải Nam", "Mai Phương Anh"
        };
        String[] volunteerNames = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Minh Châu", "Phạm Tuấn Dũng", "Võ Ngọc Hà",
            "Đặng Hải Long", "Bùi Thảo My", "Hoàng Quốc Nam", "Đỗ Khánh Ngân", "Mai Nhật Quang"
        };
        String[] sponsorNames = {
            "Công ty An Phát", "Quỹ Thiện Tâm", "Công ty Bình Minh", "Nhóm Sẻ Chia", "Công ty Hoa Sen",
            "Quỹ Vì Trẻ Em", "Công ty Đại Phúc", "Nhóm Ánh Dương", "Công ty Hưng Thịnh", "Quỹ Cộng Đồng"
        };

        for (int i = 1; i <= 10; i++) {
            accounts.add(new UserAccount(String.format("ADMIN%03d", i), "123", UserAccount.ROLE_ADMIN,
                    adminNames[i - 1], String.format("TK%03d", i)));
            accounts.add(new UserAccount(String.format("TNV%03d", i), "123", UserAccount.ROLE_VOLUNTEER,
                    volunteerNames[i - 1], String.format("HS%03d", i)));
            accounts.add(new UserAccount(String.format("NTT%03d", i), "123", UserAccount.ROLE_SPONSOR,
                    sponsorNames[i - 1], String.format("DT%03d", i)));
        }
    }

    private static void addCampaigns() {
        activities.addAll(
                new ActivityModel("CD001", "Đông ấm cho em 2026", "Hỗ trợ áo ấm, sách vở và quà cho học sinh vùng cao",
                        "Hà Giang", "01/12/2026", "20/12/2026", 50000000, "Đang thực hiện", "ADMIN001"),
                new ActivityModel("CD002", "Tiếp sức đến trường 2026", "Trao học bổng, sách vở và dụng cụ học tập cho học sinh khó khăn",
                        "TP.HCM", "05/09/2026", "30/09/2026", 30000000, "Đã duyệt", "ADMIN002"),
                new ActivityModel("CD003", "Khám bệnh thiện nguyện 2026", "Tổ chức khám bệnh và phát thuốc miễn phí",
                        "Long An", "20/05/2026", "25/05/2026", 80000000, "Đang xét", "ADMIN003")
        );
    }

    private static void addParticipants() {
        String[] names = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Minh Châu", "Phạm Tuấn Dũng", "Võ Ngọc Hà",
            "Đặng Hải Long", "Bùi Thảo My", "Hoàng Quốc Nam", "Đỗ Khánh Ngân", "Mai Nhật Quang"
        };
        String[] majors = {
            "Khoa Công nghệ phần mềm",
            "Khoa Kinh tế",
            "Khoa Công nghệ thông tin",
            "Khoa Kỹ thuật xây dựng",
            "Khoa Quản trị kinh doanh",
            "Khoa Y",
            "Khoa Quan hệ quốc tế",
            "Khoa Kỹ thuật điện - điện tử",
            "Khoa Luật kinh tế",
            "Khoa Báo chí và truyền thông"
        };
        String[] schools = {
            "UIT",
            "UEL",
            "HCMUS",
            "HCMUT",
            "HCMIU",
            "UHS",
            "HCMUSSH",
            "HCMUT",
            "UEL",
            "HCMUSSH"
        };

        for (int i = 1; i <= 10; i++) {
            String campaignId = i % 3 == 0 ? "CD003" : (i % 2 == 0 ? "CD002" : "CD001");
            String status = i % 3 == 0 ? "Chờ duyệt" : "Đã duyệt";
            String score = "Đã duyệt".equals(status) ? String.format("%.1f", 8.0 + (i % 3) * 0.5) : "";
            participants.add(new ParticipantModel(
                    String.format("TNV%03d", i),
                    String.format("HS%03d", i),
                    names[i - 1],
                    String.format("23520%03d", i),
                    String.format("09%08d", 10000000 + i),
                    majors[i - 1],
                    schools[i - 1],
                    campaignId,
                    status,
                    score
            ));
        }
    }

    private static void addSponsors() {
        String[] names = {
            "Công ty An Phát", "Quỹ Thiện Tâm", "Công ty Bình Minh", "Nhóm Sẻ Chia", "Công ty Hoa Sen",
            "Quỹ Vì Trẻ Em", "Công ty Đại Phúc", "Nhóm Ánh Dương", "Công ty Hưng Thịnh", "Quỹ Cộng Đồng"
        };
        String[] fields = {
            "Giáo dục", "Cộng đồng", "Y tế", "Thiện nguyện", "Vật phẩm",
            "Trẻ em", "Tài chính", "Xã hội", "Xây dựng", "Cộng đồng"
        };

        for (int i = 1; i <= 10; i++) {
            String campaignId = i % 3 == 0 ? "CD003" : (i % 2 == 0 ? "CD002" : "CD001");
            sponsors.add(new SponsorModel(
                    String.format("DT%03d", i),
                    names[i - 1],
                    fields[i - 1],
                    String.format("09%08d", 20000000 + i),
                    "ntt" + String.format("%03d", i) + "@gmail.com",
                    i % 2 == 0 ? "TP.HCM" : "Hà Nội",
                    campaignId,
                    5000000 + i * 1000000,
                    String.format("%02d/04/2026", i)
            ));
        }
    }

    private static void addDonations() {
        donations.addAll(
                new DonationModel("QG001", "Công ty An Phát", "CD001", "05/04/2026", "Chuyển khoản", "Tiền mặt hỗ trợ chương trình", 3000000),
                new DonationModel("QG002", "Nguyễn Văn Bình", "CD001", "10/04/2026", "Vật phẩm", "Sách vở và đồ dùng học tập", 0),
                new DonationModel("QG003", "Quỹ Thiện Tâm", "CD003", "12/04/2026", "Vật tư", "Thuốc, khẩu trang và dụng cụ y tế", 0)
        );
    }

    private static void addOperations() {
        operations.addAll(
                new SystemRecord("Chiến dịch", "VH001", "CD001", "CD001", "Duyệt chiến dịch",
                        "Kiểm tra thông tin chiến dịch trước khi công bố", "02/04/2026", "02/04/2026",
                        "Đã duyệt", "ADMIN001", "ADMIN002", "Bảng ChienDich/DuyetChienDich"),
                new SystemRecord("Đăng ký TNV", "VH002", "CD002", "TNV002", "Duyệt đăng ký TNV",
                        "Sinh viên UEL đăng ký tham gia chiến dịch CD002", "03/04/2026", "",
                        "Chờ duyệt", "ADMIN001", "ADMIN003", "Bảng ThamGiaTNV"),
                new SystemRecord("Công việc", "VH003", "CD001", "CV001", "Đóng gói quà tặng",
                        "Cần 15 tình nguyện viên", "03/04/2026", "",
                        "Đang phân công", "ADMIN002", "ADMIN004", "Bảng CongViec/PhanCong"),
                new SystemRecord("Điểm danh", "VH004", "CD001", "TNV001", "Điểm danh TNV",
                        "Ghi nhận 4 giờ tham gia", "05/04/2026", "05/04/2026",
                        "Có mặt", "TNV001", "ADMIN001", "Bảng DiemDanh"),
                new SystemRecord("Chi tiêu", "VH005", "CD001", "CT001", "Mua áo ấm",
                        "Có minh chứng chi tiêu", "06/04/2026", "",
                        "Chờ duyệt", "ADMIN003", "ADMIN005", "Bảng ChiTieu/MinhChungChiTieu"),
                new SystemRecord("Minh chứng TNV", "VH006", "CD001", "MC001", "Minh chứng phát quà",
                        "Tình nguyện viên gửi ảnh minh chứng", "08/04/2026", "",
                        "Chờ xác nhận", "TNV001", "ADMIN006", "Bảng MinhChungTNV"),
                new SystemRecord("Xuất vật phẩm", "VH007", "CD001", "PX001", "Xuất vật phẩm",
                        "Xuất sách vở cho điểm trường", "11/04/2026", "11/04/2026",
                        "Đã xuất", "ADMIN004", "ADMIN007", "Bảng PhieuXuatVatPham/ChiTietXuatVP"),
                new SystemRecord("Quyên góp", "VH008", "CD001", "QG001", "Xác nhận quyên góp tiền",
                  "Đã đối soát giao dịch của Công ty An Phát", "05/04/2026", "05/04/2026",
                        "Đã xác nhận", "NTT001", "ADMIN001", "Bảng QuyenGopTien/ThanhToan"),
                new SystemRecord("Quyên góp", "VH009", "CD001", "QG002", "Xác nhận quyên góp vật phẩm",
                        "Chờ kiểm đếm sách vở và đồ dùng học tập", "10/04/2026", "",
                        "Chờ xác nhận", "TNV002", "ADMIN002", "Bảng PhieuQuyenGopVP/ChiTietQuyenGopVP"),
                new SystemRecord("Quyên góp", "VH010", "CD003", "QG003", "Xác nhận quyên góp vật tư",
                        "Chờ kiểm tra thuốc, khẩu trang và dụng cụ y tế", "12/04/2026", "",
                        "Chờ xác nhận", "NTT003", "ADMIN003", "Bảng PhieuQuyenGopVP/ChiTietQuyenGopVP")
        );
    }

    private static void addContents() {
        contents.addAll(
                new SystemRecord("TinTuc", "TT001", "CD001", "Cập nhật chiến dịch Đông ấm 2026",
                        "Bài đăng truyền thông chiến dịch", "07/04/2026", "Đã đăng", "Bảng TinTuc"),
                new SystemRecord("BinhLuan", "BL001", "TT001", "Bình luận của sinh viên",
                        "Mình muốn tham gia chương trình", "08/04/2026", "Hiển thị", "Bảng BinhLuan"),
                new SystemRecord("ThongBao", "TB001", "TNV001", "Thông báo duyệt tham gia",
                        "Bạn đã được duyệt tham gia CD001", "09/04/2026", "Chưa đọc", "Bảng ThongBao"),
                new SystemRecord("NhatKyHeThong", "NK001", "ADMIN001", "Tạo chiến dịch",
                        "Ghi nhận thao tác tạo CD001", "01/04/2026", "Đã ghi", "Bảng NhatKyHeThong"),
                new SystemRecord("ThamSo", "TS001", "HE_THONG", "Cấu hình quy đổi điểm",
                        "Điểm đánh giá mặc định cho TNV", "10/04/2026", "Đang dùng", "Bảng ThamSo")
        );
    }

    private static void addTrainingCourses() {
        trainingCourses.addAll(
                new TrainingCourse("KH001", "Kỹ năng làm việc nhóm", "Đào tạo kỹ năng làm việc nhóm cho TNV", "01/06/2026", "05/06/2026", 20, "Nguyễn Văn A", "CD001", 30, 15, "Đang mở"),
                new TrainingCourse("KH002", "Kỹ năng sơ cứu cơ bản", "Hướng dẫn sơ cứu ban đầu cho TNV", "10/06/2026", "12/06/2026", 12, "Trần Thị B", "CD003", 30, 20, "Đang mở"),
                new TrainingCourse("KH003", "Truyền thông xã hội cho TNV", "Kỹ năng viết bài và đăng tin", "15/07/2026", "16/07/2026", 8, "Lê Văn C", "CD001", 25, 10, "Đã kết thúc")
        );
    }

    private static void addInventoryItems() {
        inventoryItems.addAll(
                new InventoryItem("VT001", "Áo ấm trẻ em", "Quần áo", "Cái", 500, 50, 120000, "CD001", "01/12/2026"),
                new InventoryItem("VT002", "Sách giáo khoa", "Sách vở", "Quyển", 300, 30, 50000, "CD001", "01/12/2026"),
                new InventoryItem("VT003", "Khẩu trang y tế", "Y tế", "Hộp", 200, 100, 35000, "CD003", "20/05/2026"),
                new InventoryItem("VT004", "Thuốc cảm cúm", "Y tế", "Hộp", 50, 20, 85000, "CD003", "20/05/2026"),
                new InventoryItem("VT005", "Bút bi", "Văn phòng phẩm", "Cây", 1000, 100, 3000, "CD002", "05/09/2026")
        );
    }

    private static void addExpenses() {
        expenses.addAll(
                new Expense("PC001", "CD001", "Mua áo ấm cho trẻ em vùng cao", 20000000, "Vật phẩm", "ADMIN001", "ADMIN002", "Đã duyệt", "01/12/2026", "02/12/2026", ""),
                new Expense("PC002", "CD001", "Thuê xe vận chuyển hàng", 5000000, "Vận chuyển", "ADMIN001", "ADMIN002", "Đã duyệt", "03/12/2026", "04/12/2026", ""),
                new Expense("PC003", "CD003", "Mua thuốc khám bệnh", 15000000, "Y tế", "ADMIN003", "", "Chờ duyệt", "15/05/2026", "", "")
        );
    }

    private static void addAlerts() {
        alerts.addAll(
                new Alert("AL001", "Tồn kho thấp", "Thuốc cảm cúm chỉ còn 50 hộp", "Trung bình", "Tồn kho", "VT004", "21/05/2026", "", "Chưa xử lý", "ADMIN001"),
                new Alert("AL002", "Hồ sơ chờ xét duyệt", "Có 3 hồ sơ TNV mới cần xét duyệt", "Cao", "Nhân sự", "", "21/05/2026", "", "Chưa xử lý", "ADMIN001")
        );
    }

    private static void addMaterialNorms() {
        materialNorms.addAll(
                new MaterialNorm("DM001", "1 suất quà Đông ấm", "VT001", 2.0, "Cái", "CD001"),
                new MaterialNorm("DM002", "1 suất quà Đông ấm", "VT005", 5.0, "Cây", "CD001"),
                new MaterialNorm("DM003", "1 túi thuốc khám bệnh", "VT003", 2.0, "Hộp", "CD003"),
                new MaterialNorm("DM004", "1 túi thuốc khám bệnh", "VT004", 1.0, "Hộp", "CD003")
        );
    }

    private static void addTrainingEnrollments() {
        trainingEnrollments.addAll(
                new TrainingEnrollment("GH001", "KH001", "TNV001", "Nguyễn Văn An", "01/06/2026", "Đang học", ""),
                new TrainingEnrollment("GH002", "KH001", "TNV002", "Trần Thị Bình", "01/06/2026", "Hoàn thành", ""),
                new TrainingEnrollment("GH003", "KH002", "TNV003", "Lê Minh Châu", "10/06/2026", "Đang học", ""),
                new TrainingEnrollment("GH004", "KH003", "TNV004", "Phạm Tuấn Dũng", "15/07/2026", "Hoàn thành", ""),
                new TrainingEnrollment("GH005", "KH003", "TNV005", "Võ Ngọc Hà", "15/07/2026", "Đang học", "")
        );
    }

    public static UserAccount authenticate(String username, String password) {
        return accounts.stream()
                .filter(account -> account.getUsername().equalsIgnoreCase(username)
                && account.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public static ObservableList<UserAccount> getAccounts() {
        return accounts;
    }

    public static ObservableList<ActivityModel> getActivities() {
        return activities;
    }

    public static ObservableList<ParticipantModel> getParticipants() {
        return participants;
    }

    public static ObservableList<SponsorModel> getSponsors() {
        return sponsors;
    }

    public static ObservableList<DonationModel> getDonations() {
        return donations;
    }

    public static ObservableList<SystemRecord> getOperations() {
        return operations;
    }

    public static ObservableList<SystemRecord> getContents() {
        return contents;
    }

    public static ObservableList<TrainingCourse> getTrainingCourses() {
        return trainingCourses;
    }

    public static ObservableList<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public static ObservableList<Expense> getExpenses() {
        return expenses;
    }

    public static ObservableList<Alert> getAlerts() {
        return alerts;
    }

    public static ObservableList<MaterialNorm> getMaterialNorms() {
        return materialNorms;
    }

    public static ObservableList<TrainingEnrollment> getTrainingEnrollments() {
        return trainingEnrollments;
    }

    public static double getTotalSponsorAmount() {
        return sponsors.stream()
                .mapToDouble(SponsorModel::getGiaTriTaiTro)
                .sum();
    }

    public static double getTotalDonationAmount() {
        return donations.stream()
                .mapToDouble(DonationModel::getSoTien)
                .sum();
    }

    public static double getTotalExpenseAmount() {
        return expenses.stream()
                .filter(e -> "Đã duyệt".equals(e.getTrangThai()))
                .mapToDouble(Expense::getSoTien)
                .sum();
    }

    public static double getCampaignExpenseTotal(String campaignId) {
        return expenses.stream()
                .filter(e -> e.getMaChienDich().equalsIgnoreCase(campaignId)
                        && "Đã duyệt".equals(e.getTrangThai()))
                .mapToDouble(Expense::getSoTien)
                .sum();
    }

    public static double getCampaignMoneyTotal(String campaignId) {
        double donationTotal = donations.stream()
                .filter(item -> item.getHoatDong().equalsIgnoreCase(campaignId))
                .mapToDouble(DonationModel::getSoTien)
                .sum();
        double sponsorTotal = sponsors.stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .mapToDouble(SponsorModel::getGiaTriTaiTro)
                .sum();
        return donationTotal + sponsorTotal;
    }

    public static long getCampaignParticipantCount(String campaignId) {
        return participants.stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .count();
    }

    public static String getCampaignNewsSummary(String campaignId) {
        StringBuilder builder = new StringBuilder();
        contents.stream()
                .filter(item -> item.getNhomBang().equalsIgnoreCase("TinTuc")
                && item.getMaLienKet().equalsIgnoreCase(campaignId))
                .forEach(item -> {
                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    builder.append("- ").append(item.getTieuDe());
                });
        return builder.length() == 0 ? "Chưa có tin tức liên quan" : builder.toString();
    }

    public static ActivityModel findCampaign(String campaignId) {
        return activities.stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .findFirst()
                .orElse(null);
    }

    public static String todayText() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String nextDonationId() {
        int index = donations.size() + 1;
        String id;
        do {
            id = String.format("QG%03d", index++);
        } while (donationIdExists(id));
        return id;
    }

    public static String nextCampaignId() {
        int index = activities.size() + 1;
        String id;
        do {
            id = String.format("CD%03d", index++);
        } while (campaignIdExists(id));
        return id;
    }

    public static String nextProfileId() {
        int index = participants.size() + 1;
        String id;
        do {
            id = String.format("HS%03d", index++);
        } while (profileIdExists(id));
        return id;
    }

    public static String nextSponsorId() {
        int index = sponsors.size() + 1;
        String id;
        do {
            id = String.format("DT%03d", index++);
        } while (sponsorIdExists(id));
        return id;
    }

    public static String nextOperationId(String prefix) {
        int index = operations.size() + 1;
        String id;
        do {
            id = prefix + String.format("%03d", index++);
        } while (operationIdExists(id));
        return id;
    }

    public static String nextTrainingCourseId() {
        int index = trainingCourses.size() + 1;
        String id;
        do {
            id = "KH" + String.format("%03d", index++);
        } while (trainingCourseIdExists(id));
        return id;
    }

    public static String nextInventoryItemId() {
        int index = inventoryItems.size() + 1;
        String id;
        do {
            id = "VT" + String.format("%03d", index++);
        } while (inventoryItemIdExists(id));
        return id;
    }

    public static String nextExpenseId() {
        int index = expenses.size() + 1;
        String id;
        do {
            id = "PC" + String.format("%03d", index++);
        } while (expenseIdExists(id));
        return id;
    }

    public static String nextAlertId() {
        int index = alerts.size() + 1;
        String id;
        do {
            id = "AL" + String.format("%03d", index++);
        } while (alertIdExists(id));
        return id;
    }

    public static String nextMaterialNormId() {
        int index = materialNorms.size() + 1;
        String id;
        do {
            id = "DM" + String.format("%03d", index++);
        } while (materialNormIdExists(id));
        return id;
    }

    public static String nextEnrollmentId() {
        int index = trainingEnrollments.size() + 1;
        String id;
        do {
            id = "GH" + String.format("%03d", index++);
        } while (enrollmentIdExists(id));
        return id;
    }

    public static String nextContentId(String prefix) {
        int index = contents.size() + 1;
        String id;
        do {
            id = prefix + String.format("%03d", index++);
        } while (contentIdExists(id));
        return id;
    }

    private static boolean donationIdExists(String id) {
        for (DonationModel item : donations) {
            if (item.getMaQuyenGop().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean campaignIdExists(String id) {
        for (ActivityModel item : activities) {
            if (item.getMaChienDich().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean profileIdExists(String id) {
        for (ParticipantModel item : participants) {
            if (item.getMaHoSo().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sponsorIdExists(String id) {
        for (SponsorModel item : sponsors) {
            if (item.getMaDoiTac().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean operationIdExists(String id) {
        for (SystemRecord item : operations) {
            if (item.getMaChinh().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean trainingCourseIdExists(String id) {
        for (TrainingCourse item : trainingCourses) {
            if (item.getMaKhoaHoc().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inventoryItemIdExists(String id) {
        for (InventoryItem item : inventoryItems) {
            if (item.getMaVatTu().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean expenseIdExists(String id) {
        for (Expense item : expenses) {
            if (item.getMaChiPhi().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean alertIdExists(String id) {
        for (Alert item : alerts) {
            if (item.getMaCanhBao().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean materialNormIdExists(String id) {
        for (MaterialNorm item : materialNorms) {
            if (item.getMaDinhMuc().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean enrollmentIdExists(String id) {
        for (TrainingEnrollment item : trainingEnrollments) {
            if (item.getMaGhiDanh().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contentIdExists(String id) {
        for (SystemRecord item : contents) {
            if (item.getMaChinh().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
}
