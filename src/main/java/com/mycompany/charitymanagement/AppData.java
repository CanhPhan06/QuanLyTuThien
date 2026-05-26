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

    static {
        seedFallbackData();
        javafx.application.Platform.runLater(() -> {
            Thread loader = new Thread(() -> DatabaseDataLoader.loadIntoMemory());
            loader.start();
        });
    }

    private AppData() {
    }

    private static void seedFallbackData() {
        seedAccounts();
        seedCampaigns();
        seedParticipants();
        seedSponsors();
        seedDonations();
        seedOperations();
        seedContents();
    }

    private static void seedAccounts() {
        accounts.setAll(
                new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Quản lý hệ thống", "TK000"),
                new UserAccount("ADMIN001", "123", UserAccount.ROLE_ADMIN, "Nguyễn Quản Trị", "TK001"),
                new UserAccount("ADMIN002", "123", UserAccount.ROLE_ADMIN, "Trần Minh Quân", "TK002"),
                new UserAccount("ADMIN003", "123", UserAccount.ROLE_ADMIN, "Lê Hoài An", "TK003"),
                new UserAccount("ADMIN004", "123", UserAccount.ROLE_ADMIN, "Phạm Gia Bảo", "TK004"),
                new UserAccount("ADMIN005", "123", UserAccount.ROLE_ADMIN, "Võ Thanh Tâm", "TK005"),
                new UserAccount("ADMIN006", "123", UserAccount.ROLE_ADMIN, "Đặng Ngọc Hân", "TK006"),
                new UserAccount("ADMIN007", "123", UserAccount.ROLE_ADMIN, "Bùi Quốc Việt", "TK007"),
                new UserAccount("ADMIN008", "123", UserAccount.ROLE_ADMIN, "Hoàng Khánh Linh", "TK008"),
                new UserAccount("ADMIN009", "123", UserAccount.ROLE_ADMIN, "Đỗ Hải Nam", "TK009"),
                new UserAccount("ADMIN010", "123", UserAccount.ROLE_ADMIN, "Mai Phương Anh", "TK010"),
                new UserAccount("TNV001", "123", UserAccount.ROLE_VOLUNTEER, "Nguyễn Văn An", "HS001"),
                new UserAccount("TNV002", "123", UserAccount.ROLE_VOLUNTEER, "Trần Thị Bình", "HS002"),
                new UserAccount("TNV003", "123", UserAccount.ROLE_VOLUNTEER, "Lê Minh Châu", "HS003"),
                new UserAccount("TNV004", "123", UserAccount.ROLE_VOLUNTEER, "Phạm Tuấn Dũng", "HS004"),
                new UserAccount("TNV005", "123", UserAccount.ROLE_VOLUNTEER, "Võ Ngọc Hà", "HS005"),
                new UserAccount("TNV006", "123", UserAccount.ROLE_VOLUNTEER, "Đặng Hải Long", "HS006"),
                new UserAccount("TNV007", "123", UserAccount.ROLE_VOLUNTEER, "Bùi Thảo My", "HS007"),
                new UserAccount("TNV008", "123", UserAccount.ROLE_VOLUNTEER, "Hoàng Quốc Nam", "HS008"),
                new UserAccount("TNV009", "123", UserAccount.ROLE_VOLUNTEER, "Đỗ Khánh Ngân", "HS009"),
                new UserAccount("TNV010", "123", UserAccount.ROLE_VOLUNTEER, "Mai Nhật Quang", "HS010"),
                new UserAccount("NTT001", "123", UserAccount.ROLE_SPONSOR, "Công ty An Phát", "DT001"),
                new UserAccount("NTT002", "123", UserAccount.ROLE_SPONSOR, "Quỹ Thiện Tâm", "DT002"),
                new UserAccount("NTT003", "123", UserAccount.ROLE_SPONSOR, "Công ty Bình Minh", "DT003"),
                new UserAccount("NTT004", "123", UserAccount.ROLE_SPONSOR, "Nhóm Sẻ Chia", "DT004"),
                new UserAccount("NTT005", "123", UserAccount.ROLE_SPONSOR, "Công ty Hoa Sen", "DT005"),
                new UserAccount("NTT006", "123", UserAccount.ROLE_SPONSOR, "Quỹ Vì Trẻ Em", "DT006"),
                new UserAccount("NTT007", "123", UserAccount.ROLE_SPONSOR, "Công ty Đại Phúc", "DT007"),
                new UserAccount("NTT008", "123", UserAccount.ROLE_SPONSOR, "Nhóm Ánh Dương", "DT008"),
                new UserAccount("NTT009", "123", UserAccount.ROLE_SPONSOR, "Công ty Hưng Thịnh", "DT009"),
                new UserAccount("NTT010", "123", UserAccount.ROLE_SPONSOR, "Quỹ Cộng Đồng", "DT010")
        );
    }

    private static void seedCampaigns() {
        activities.setAll(
                new ActivityModel("CD001", "Đông ấm cho em 2026", "Hỗ trợ áo ấm, sách vở và quà cho học sinh vùng cao.", "Hà Giang", "01/12/2026", "20/12/2026", 50000000, "Đang thực hiện", "ADMIN001"),
                new ActivityModel("CD002", "Tiếp sức đến trường 2026", "Trao học bổng, sách vở và dụng cụ học tập cho học sinh khó khăn.", "TP.HCM", "05/09/2026", "30/09/2026", 30000000, "Đã duyệt", "ADMIN002"),
                new ActivityModel("CD003", "Khám bệnh thiện nguyện 2026", "Tổ chức khám bệnh và phát thuốc miễn phí.", "Long An", "20/05/2026", "25/05/2026", 80000000, "Đang xét", "ADMIN003"),
                new ActivityModel("CD004", "Bữa cơm yêu thương 2026", "Nấu và trao suất ăn miễn phí cho bệnh nhân khó khăn.", "TP.HCM", "10/06/2026", "15/06/2026", 20000000, "Đang thực hiện", "ADMIN004"),
                new ActivityModel("CD005", "Mùa hè xanh 2026", "Tổ chức hoạt động cộng đồng tại địa phương.", "Bến Tre", "01/07/2026", "20/07/2026", 65000000, "Đã duyệt", "ADMIN005"),
                new ActivityModel("CD006", "Sách cho em 2026", "Quyên góp sách, truyện và thiết bị học tập.", "Đắk Lắk", "01/08/2026", "18/08/2026", 25000000, "Chờ duyệt", "ADMIN006"),
                new ActivityModel("CD007", "Nước sạch học đường 2026", "Lắp đặt thiết bị lọc nước cho điểm trường vùng xa.", "Gia Lai", "05/10/2026", "28/10/2026", 90000000, "Đang thực hiện", "ADMIN007"),
                new ActivityModel("CD008", "Xuân yêu thương 2026", "Trao quà Tết cho hộ gia đình khó khăn.", "Tây Ninh", "15/01/2026", "25/01/2026", 45000000, "Hoàn thành", "ADMIN008"),
                new ActivityModel("CD009", "Máy tính cho em 2026", "Vận động laptop cũ và thiết bị học trực tuyến.", "Bình Phước", "01/11/2026", "25/11/2026", 120000000, "Đang xét", "ADMIN009"),
                new ActivityModel("CD010", "Chung tay vì môi trường 2026", "Trồng cây, phân loại rác và truyền thông xanh.", "TP.HCM", "20/04/2026", "30/04/2026", 15000000, "Đã duyệt", "ADMIN010")
        );
    }

    private static void seedParticipants() {
        participants.setAll(
                new ParticipantModel("TNV001", "HS001", "Nguyễn Văn An", "23520001", "0910000001", "Khoa Công nghệ phần mềm", "UIT", "CD001", "Đã duyệt", "8.5"),
                new ParticipantModel("TNV002", "HS002", "Trần Thị Bình", "23520002", "0910000002", "Khoa Kinh tế", "UEL", "CD002", "Chờ duyệt", ""),
                new ParticipantModel("TNV003", "HS003", "Lê Minh Châu", "23520003", "0910000003", "Khoa Công nghệ thông tin", "HCMUS", "CD003", "Đang xét", ""),
                new ParticipantModel("TNV004", "HS004", "Phạm Tuấn Dũng", "23520004", "0910000004", "Khoa Kỹ thuật xây dựng", "HCMUT", "CD001", "Đã duyệt", "9"),
                new ParticipantModel("TNV005", "HS005", "Võ Ngọc Hà", "23520005", "0910000005", "Khoa Quản trị kinh doanh", "HCMIU", "CD004", "Đã duyệt", "8"),
                new ParticipantModel("TNV006", "HS006", "Đặng Hải Long", "23520006", "0910000006", "Khoa Y", "UHS", "CD003", "Đã duyệt", "9.5"),
                new ParticipantModel("TNV007", "HS007", "Bùi Thảo My", "23520007", "0910000007", "Khoa Quan hệ quốc tế", "HCMUSSH", "CD005", "Chờ duyệt", ""),
                new ParticipantModel("TNV008", "HS008", "Hoàng Quốc Nam", "23520008", "0910000008", "Khoa Kỹ thuật điện - điện tử", "HCMUT", "CD007", "Đã duyệt", "8.5"),
                new ParticipantModel("TNV009", "HS009", "Đỗ Khánh Ngân", "23520009", "0910000009", "Khoa Luật kinh tế", "UEL", "CD010", "Đã duyệt", "8"),
                new ParticipantModel("TNV010", "HS010", "Mai Nhật Quang", "23520010", "0910000010", "Khoa Báo chí và truyền thông", "HCMUSSH", "CD008", "Đã duyệt", "9")
        );
    }

    private static void seedSponsors() {
        sponsors.setAll(
                new SponsorModel("DT001", "Công ty An Phát", "Giáo dục", "0920000001", "ntt001@gmail.com", "TP.HCM", "CD001", 6000000, "01/04/2026"),
                new SponsorModel("DT002", "Quỹ Thiện Tâm", "Cộng đồng", "0920000002", "ntt002@gmail.com", "Hà Nội", "CD002", 7000000, "02/04/2026"),
                new SponsorModel("DT003", "Công ty Bình Minh", "Y tế", "0920000003", "ntt003@gmail.com", "TP.HCM", "CD003", 8000000, "03/04/2026"),
                new SponsorModel("DT004", "Nhóm Sẻ Chia", "Thiện nguyện", "0920000004", "ntt004@gmail.com", "Đà Nẵng", "CD004", 5000000, "04/04/2026"),
                new SponsorModel("DT005", "Công ty Hoa Sen", "Vật phẩm", "0920000005", "ntt005@gmail.com", "Bình Dương", "CD001", 9000000, "05/04/2026"),
                new SponsorModel("DT006", "Quỹ Vì Trẻ Em", "Trẻ em", "0920000006", "ntt006@gmail.com", "TP.HCM", "CD005", 12000000, "06/04/2026"),
                new SponsorModel("DT007", "Công ty Đại Phúc", "Tài chính", "0920000007", "ntt007@gmail.com", "Đồng Nai", "CD007", 15000000, "07/04/2026"),
                new SponsorModel("DT008", "Nhóm Ánh Dương", "Xã hội", "0920000008", "ntt008@gmail.com", "Cần Thơ", "CD008", 4000000, "10/01/2026"),
                new SponsorModel("DT009", "Công ty Hưng Thịnh", "Xây dựng", "0920000009", "ntt009@gmail.com", "TP.HCM", "CD009", 20000000, "08/04/2026"),
                new SponsorModel("DT010", "Quỹ Cộng Đồng", "Cộng đồng", "0920000010", "ntt010@gmail.com", "Huế", "CD010", 3000000, "09/04/2026")
        );
    }

    private static void seedDonations() {
        donations.setAll(
                new DonationModel("QG001", "Công ty An Phát", "CD001", "05/04/2026", "Chuyển khoản", "Quyên góp tiền cho chiến dịch Đông ấm", 3000000),
                new DonationModel("QG002", "Nguyễn Văn Bình", "CD001", "10/04/2026", "Tiền mặt", "Ủng hộ trực tiếp tại văn phòng", 1200000),
                new DonationModel("QG003", "Công ty Bình Minh", "CD003", "12/04/2026", "Chuyển khoản", "Quyên góp tiền cho vật tư y tế", 5000000),
                new DonationModel("QG004", "Nhóm Sẻ Chia", "CD004", "12/04/2026", "Tiền mặt", "Hỗ trợ bữa ăn thiện nguyện", 2500000),
                new DonationModel("QG005", "Quỹ Vì Trẻ Em", "CD005", "13/04/2026", "Chuyển khoản", "Đồng hành cùng Mùa hè xanh", 4000000),
                new DonationModel("QG006", "Câu lạc bộ Xanh", "CD010", "14/04/2026", "Tiền mặt", "Hỗ trợ truyền thông môi trường", 1500000),
                new DonationModel("QG007", "Quỹ Cộng Đồng", "CD008", "15/01/2026", "Chuyển khoản", "Trao quà Tết cho hộ khó khăn", 3500000),
                new DonationModel("VP001", "Công ty Hoa Sen", "CD001", "05/04/2026", "Vật phẩm", "Áo ấm: 500 cái", 0),
                new DonationModel("VP002", "Nhóm sinh viên UIT", "CD002", "07/04/2026", "Vật phẩm", "Sách vở: 800 bộ", 0),
                new DonationModel("VP003", "Công ty Bình Minh", "CD003", "08/04/2026", "Vật phẩm", "Khẩu trang: 300 hộp; thuốc cơ bản: 120 hộp", 0),
                new DonationModel("VP004", "Công ty Hưng Thịnh", "CD009", "15/04/2026", "Vật phẩm", "Máy tính cũ: 25 bộ", 0),
                new DonationModel("VP005", "Quỹ Vì Trẻ Em", "CD006", "18/04/2026", "Vật phẩm", "Sách truyện thiếu nhi: 600 quyển", 0)
        );
    }

    private static void seedOperations() {
        operations.setAll(
                new SystemRecord("Chiến dịch", "VH001", "CD001", "CD001", "Duyệt chiến dịch", "Kiểm tra thông tin chiến dịch trước khi công bố", "02/04/2026", "02/04/2026", "Đã duyệt", "ADMIN001", "ADMIN002", "Bảng CHIEN_DICH / DUYET_CHIEN_DICH"),
                new SystemRecord("Đăng ký TNV", "VH002", "CD002", "TNV002", "Duyệt đăng ký TNV", "Sinh viên UEL đăng ký tham gia chiến dịch CD002", "03/04/2026", "", "Chờ duyệt", "TNV002", "ADMIN003", "Bảng THAM_GIA_TNV"),
                new SystemRecord("Công việc", "VH003", "CD001", "CV001", "Đóng gói quà tặng", "Cần 15 tình nguyện viên", "03/04/2026", "", "Đang phân công", "ADMIN002", "ADMIN004", "Bảng CONG_VIEC / PHAN_CONG"),
                new SystemRecord("Điểm danh", "VH004", "CD001", "TNV001", "Điểm danh TNV", "Ghi nhận 4 giờ tham gia", "05/04/2026", "05/04/2026", "Có mặt", "TNV001", "ADMIN001", "Bảng DIEM_DANH"),
                new SystemRecord("Chi tiêu", "VH005", "CD001", "CT001", "Mua áo ấm", "Có minh chứng chi tiêu", "06/04/2026", "06/04/2026", "Đã duyệt", "ADMIN003", "ADMIN005", "Bảng CHI_TIEU / MINH_CHUNG_CHI_TIEU"),
                new SystemRecord("Minh chứng TNV", "VH006", "CD001", "MC001", "Minh chứng phát quà", "Tình nguyện viên gửi ảnh minh chứng", "08/04/2026", "08/04/2026", "Đã xác nhận", "TNV001", "ADMIN006", "Bảng MINH_CHUNG_TNV"),
                new SystemRecord("Xuất vật phẩm", "VH007", "CD001", "PX001", "Xuất vật phẩm", "Xuất áo ấm cho điểm trường", "11/04/2026", "11/04/2026", "Đã xuất", "ADMIN004", "ADMIN007", "Bảng PHIEU_XUAT_VAT_PHAM / CHI_TIET_XUAT_VP"),
                new SystemRecord("Quyên góp", "VH008", "CD001", "QG001", "Xác nhận quyên góp tiền", "Đã đối soát giao dịch của Công ty An Phát", "05/04/2026", "05/04/2026", "Đã xác nhận", "NTT001", "ADMIN001", "Bảng QUYEN_GOP_TIEN / THANH_TOAN"),
                new SystemRecord("Quyên góp", "VH009", "CD001", "VP001", "Xác nhận quyên góp vật phẩm", "Áo ấm cho học sinh vùng cao", "05/04/2026", "05/04/2026", "Đã xác nhận", "NTT005", "ADMIN002", "Bảng PHIEU_QUYEN_GOP_VP / CHI_TIET_QUYEN_GOP_VP"),
                new SystemRecord("Quyên góp", "VH010", "CD009", "VP004", "Xác nhận máy tính cũ", "Chờ kiểm tra chất lượng thiết bị", "15/04/2026", "", "Chờ xác nhận", "NTT009", "ADMIN003", "Bảng PHIEU_QUYEN_GOP_VP / CHI_TIET_QUYEN_GOP_VP")
        );
    }

    private static void seedContents() {
        contents.setAll(
                new SystemRecord("TinTuc", "ND001", "CD001", "Cập nhật tiến độ Đông ấm 2026", "Đã tiếp nhận thêm áo ấm và sách vở từ các nhà tài trợ.", "07/04/2026", "Đã đăng", "Bảng TIN_TUC"),
                new SystemRecord("TinTuc", "ND002", "CD003", "Lịch khám bệnh thiện nguyện", "Chiến dịch khám bệnh sẽ tổ chức tại Long An trong tháng 05/2026.", "08/04/2026", "Đã đăng", "Bảng TIN_TUC"),
                new SystemRecord("TinTuc", "ND003", "CD010", "Ngày hội môi trường xanh", "Mời sinh viên tham gia phân loại rác và trồng cây.", "10/04/2026", "Chờ duyệt", "Bảng TIN_TUC"),
                new SystemRecord("BinhLuan", "ND004", "CD001", "TNV001", "Bình luận của sinh viên", "Em muốn đăng ký hỗ trợ đóng gói quà.", "08/04/2026", "", "Hiển thị", "TNV001", "ADMIN001", "Bảng BINH_LUAN"),
                new SystemRecord("BinhLuan", "ND005", "CD003", "TNV006", "Hỏi lịch khám bệnh", "Em có thể hỗ trợ khâu hướng dẫn người dân.", "09/04/2026", "", "Hiển thị", "TNV006", "ADMIN003", "Bảng BINH_LUAN"),
                new SystemRecord("BinhLuan", "ND006", "CD010", "TNV009", "Câu hỏi về điểm tập trung", "Cho em hỏi lịch tập trung ở đâu?", "11/04/2026", "", "Chờ duyệt", "TNV009", "ADMIN010", "Bảng BINH_LUAN"),
                new SystemRecord("ThongBao", "ND007", "TNV001", "Thông báo duyệt tham gia", "Bạn đã được duyệt tham gia chiến dịch Đông ấm cho em 2026.", "04/04/2026", "Chưa đọc", "Bảng THONG_BAO"),
                new SystemRecord("ThongBao", "ND008", "NTT001", "Cảm ơn nhà tài trợ", "Cảm ơn Công ty An Phát đã đồng hành cùng chiến dịch.", "05/04/2026", "Đã đọc", "Bảng THONG_BAO"),
                new SystemRecord("ThongBao", "ND009", "ADMIN001", "Hồ sơ cần duyệt", "Có hồ sơ tình nguyện viên mới cần kiểm tra.", "10/04/2026", "Chưa đọc", "Bảng THONG_BAO"),
                new SystemRecord("NhatKyHeThong", "ND010", "ADMIN001", "Tạo chiến dịch", "Ghi nhận thao tác tạo chiến dịch CD001.", "01/04/2026", "Đã ghi", "Bảng NHAT_KY_HE_THONG"),
                new SystemRecord("NhatKyHeThong", "ND011", "ADMIN004", "Cập nhật vận hành", "Phân công công việc cho chiến dịch Bữa cơm yêu thương.", "08/04/2026", "Đã ghi", "Bảng NHAT_KY_HE_THONG"),
                new SystemRecord("ThamSo", "ND012", "HE_THONG", "Điểm quy đổi một giờ tham gia", "Một giờ tham gia tương ứng 10 điểm đóng góp.", "10/04/2026", "Đang dùng", "Bảng THAM_SO")
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

    private static boolean contentIdExists(String id) {
        for (SystemRecord item : contents) {
            if (item.getMaChinh().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
}
