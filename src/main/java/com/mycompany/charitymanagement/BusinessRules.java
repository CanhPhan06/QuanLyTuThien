package com.mycompany.charitymanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class BusinessRules {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Set<String> OPEN_CAMPAIGN_STATUSES = new HashSet<>(Arrays.asList(
            "Đã duyệt", "Đang thực hiện"
    ));
    private static final Set<String> CLOSED_CAMPAIGN_STATUSES = new HashSet<>(Arrays.asList(
            "Hoàn thành", "Đã kết thúc", "Đã hủy"
    ));
    private static final Set<String> VALID_DONATION_TYPES = new HashSet<>(Arrays.asList(
            "Tiền", "Vật phẩm", "Vật tư", "Vật dụng", "Tài trợ tiền", "Tài trợ vật phẩm", "Chuyển khoản"
    ));
    private static final Set<String> VALID_PARTICIPANT_STATUSES = new HashSet<>(Arrays.asList(
            "Chờ duyệt", "Đang xét", "Đã duyệt"
    ));
    private static final Set<String> VNU_HCM_SCHOOLS = new HashSet<>(Arrays.asList(
            "UIT", "UEL", "HCMUS", "HCMUT", "HCMIU", "UHS", "HCMUSSH"
    ));

    private BusinessRules() {
    }

    public static String validateCampaign(ActivityModel activity) {
        if (isBlank(activity.getMaChienDich()) || isBlank(activity.getTenChienDich())) {
            return "Chiến dịch phải có mã và tên.";
        }
        if (isBlank(activity.getDiaDiem())) {
            return "Chiến dịch phải có địa điểm.";
        }
        if (activity.getMucTieuTien() < 0) {
            return "Mục tiêu tiền không được âm.";
        }
        LocalDate start = parseDate(activity.getNgayBatDau());
        LocalDate end = parseDate(activity.getNgayKetThuc());
        if (start == null || end == null) {
            return "Ngày bắt đầu và ngày kết thúc phải có dạng dd/MM/yyyy.";
        }
        if (end.isBefore(start)) {
            return "Ngày kết thúc không được trước ngày bắt đầu.";
        }
        return null;
    }

    public static String validateParticipant(ParticipantModel participant) {
        if (isBlank(participant.getMaTaiKhoan()) || isBlank(participant.getMaHoSo())
                || isBlank(participant.getHoTen()) || isBlank(participant.getMaChienDich())) {
            return "Sinh viên/TNV phải có mã tài khoản, mã hồ sơ, họ tên và chiến dịch.";
        }
        if (!isBlank(participant.getSoDienThoai()) && !isPhone(participant.getSoDienThoai())) {
            return "Số điện thoại TNV phải bắt đầu bằng 09 và có 10 chữ số.";
        }
        if (!isBlank(participant.getMssv()) && !participant.getMssv().matches("\\d{8,10}")) {
            return "MSSV phải gồm 8 đến 10 chữ số.";
        }
        if (isBlank(participant.getKhoa()) || isBlank(participant.getTruong())) {
            return "Hồ sơ TNV phải có khoa và trường thuộc ĐHQG-TPHCM.";
        }
        if (!VNU_HCM_SCHOOLS.contains(participant.getTruong().trim().toUpperCase())) {
            return "Trường của TNV phải thuộc ĐHQG-TPHCM: UIT, UEL, HCMUS, HCMUT, HCMIU, UHS hoặc HCMUSSH.";
        }
        if (!VALID_PARTICIPANT_STATUSES.contains(participant.getTrangThaiDuyet())) {
            return "Trạng thái duyệt TNV không hợp lệ.";
        }
        if (AppData.findCampaign(participant.getMaChienDich()) == null) {
            return "Chiến dịch của TNV không tồn tại.";
        }
        return null;
    }

    public static String validateSponsor(SponsorModel sponsor) {
        if (isBlank(sponsor.getMaDoiTac()) || isBlank(sponsor.getTenDoiTac())
                || isBlank(sponsor.getMaChienDich())) {
            return "Nhà tài trợ phải có mã, tên và chiến dịch.";
        }
        if (isBlank(sponsor.getLinhVuc()) || isBlank(sponsor.getSoDienThoai())
                || isBlank(sponsor.getEmail()) || isBlank(sponsor.getNgayKyKet())) {
            return "Nhà tài trợ phải có lĩnh vực, số điện thoại, email và ngày ký kết.";
        }
        if (!isPhone(sponsor.getSoDienThoai())) {
            return "Số điện thoại nhà tài trợ phải bắt đầu bằng 09 và có 10 chữ số.";
        }
        if (!isGmail(sponsor.getEmail())) {
            return "Email nhà tài trợ phải có dạng @gmail.com.";
        }
        if (parseDate(sponsor.getNgayKyKet()) == null) {
            return "Ngày ký kết tài trợ phải có dạng dd/MM/yyyy.";
        }
        if (sponsor.getGiaTriTaiTro() < 0) {
            return "Giá trị tài trợ không được âm.";
        }
        if (AppData.findCampaign(sponsor.getMaChienDich()) == null) {
            return "Chiến dịch tài trợ không tồn tại.";
        }
        return null;
    }

    public static String validateDonation(DonationModel donation) {
        if (isBlank(donation.getMaQuyenGop()) || isBlank(donation.getNguoiQuyenGop())
                || isBlank(donation.getHoatDong()) || isBlank(donation.getNgayQuyenGop())
                || isBlank(donation.getHinhThuc())) {
            return "Quyên góp phải có mã, người quyên góp, chiến dịch, ngày ghi nhận và hình thức.";
        }
        if (donation.getNguoiQuyenGop().length() < 2) {
            return "Người đóng góp phải là tên người, công ty hoặc tổ chức hợp lệ.";
        }
        if (parseDate(donation.getNgayQuyenGop()) == null) {
            return "Ngày ghi nhận quyên góp phải có dạng dd/MM/yyyy.";
        }
        if (!VALID_DONATION_TYPES.contains(donation.getHinhThuc())) {
            return "Hình thức quyên góp không hợp lệ.";
        }
        if (isMoneyType(donation.getHinhThuc()) && donation.getSoTien() <= 0) {
            return "Quyên góp bằng tiền phải có số tiền lớn hơn 0.";
        }
        if (!isMoneyType(donation.getHinhThuc()) && isBlank(donation.getNoiDungQuyenGop())) {
            return "Quyên góp vật phẩm/vật tư/vật dụng phải có nội dung mô tả.";
        }
        if (donation.getSoTien() < 0) {
            return "Giá trị quyên góp không được âm.";
        }
        ActivityModel campaign = AppData.findCampaign(donation.getHoatDong());
        if (campaign == null) {
            return "Chiến dịch nhận quyên góp không tồn tại.";
        }
        if (!canReceiveSupport(campaign)) {
            return "Chiến dịch hiện không nhận quyên góp/tài trợ.";
        }
        return null;
    }

    public static String validateVolunteerRegistration(UserAccount user, ActivityModel campaign) {
        if (user == null || !user.isVolunteer()) {
            return "Chỉ tài khoản tình nguyện viên mới được đăng ký chiến dịch.";
        }
        if (campaign == null) {
            return "Vui lòng chọn chiến dịch muốn đăng ký.";
        }
        if (!canRegister(campaign)) {
            return "Chỉ được đăng ký chiến dịch đã duyệt hoặc đang thực hiện.";
        }
        boolean existed = AppData.getParticipants().stream()
                .anyMatch(item -> item.getMaTaiKhoan().equalsIgnoreCase(user.getUsername())
                && item.getMaChienDich().equalsIgnoreCase(campaign.getMaChienDich()));
        if (existed) {
            return "Bạn đã có hồ sơ tham gia chiến dịch này.";
        }
        return null;
    }

    public static String validateCheckIn(UserAccount user, ParticipantModel profile) {
        if (user == null || !user.isVolunteer()) {
            return "Chỉ tài khoản tình nguyện viên mới được điểm danh.";
        }
        if (profile == null) {
            return "Bạn chưa có chiến dịch được ghi nhận.";
        }
        if (!"Đã duyệt".equals(profile.getTrangThaiDuyet())) {
            return "Chỉ TNV đã được duyệt mới được điểm danh.";
        }
        boolean checkedToday = AppData.getOperations().stream()
                .anyMatch(record -> "Điểm danh".equals(record.getNhomBang())
                && record.getMaLienKet().equalsIgnoreCase(user.getUsername())
                && record.getMaChienDich().equalsIgnoreCase(profile.getMaChienDich())
                && record.getNgayTao().equals(AppData.todayText()));
        if (checkedToday) {
            return "Bạn đã điểm danh chiến dịch này hôm nay.";
        }
        return null;
    }

    public static String validateProof(UserAccount user, ParticipantModel profile, String proofType, String note) {
        if (user == null || !user.isVolunteer()) {
            return "Chỉ tài khoản tình nguyện viên mới được gửi minh chứng.";
        }
        if (profile == null) {
            return "Bạn cần đăng ký hoặc được ghi nhận vào một chiến dịch trước khi gửi minh chứng.";
        }
        if (!"Đã duyệt".equals(profile.getTrangThaiDuyet())) {
            return "Chỉ TNV đã được duyệt mới được gửi minh chứng.";
        }
        if (isBlank(proofType)) {
            return "Vui lòng chọn loại minh chứng.";
        }
        if (isBlank(note)) {
            return "Vui lòng nhập nội dung/ghi chú minh chứng.";
        }
        return null;
    }

    public static String validateOperation(SystemRecord record) {
        if (record == null) {
            return "Bản ghi vận hành không hợp lệ.";
        }
        if (isBlank(record.getNhomBang()) || isBlank(record.getMaChinh())
                || isBlank(record.getMaChienDich()) || isBlank(record.getMaLienKet())
                || isBlank(record.getTieuDe()) || isBlank(record.getNoiDung()) || isBlank(record.getTrangThai())
                || isBlank(record.getNguoiTao()) || isBlank(record.getNguoiXuLy())) {
            return "Vận hành phải có nghiệp vụ, chiến dịch, đối tượng liên kết, tiêu đề, nội dung, trạng thái, người tạo và người xử lý.";
        }
        if (parseDate(record.getNgayTao()) == null) {
            return "Ngày tạo vận hành phải có dạng dd/MM/yyyy.";
        }
        if (!isBlank(record.getNgayXuLy()) && parseDate(record.getNgayXuLy()) == null) {
            return "Ngày xử lý vận hành phải có dạng dd/MM/yyyy.";
        }
        ActivityModel campaign = AppData.findCampaign(record.getMaChienDich());
        if (campaign == null) {
            return "Chiến dịch trong bản ghi vận hành không tồn tại.";
        }
        if (!allowedStatusesFor(record.getNhomBang()).contains(record.getTrangThai())) {
            return "Trạng thái không phù hợp với loại nghiệp vụ đã chọn.";
        }
        if ("Chiến dịch".equals(record.getNhomBang())
                && AppData.findCampaign(record.getMaLienKet()) == null) {
            return "Chiến dịch cần xử lý không tồn tại.";
        }
        if ("Đăng ký TNV".equals(record.getNhomBang())
                && findParticipant(record.getMaLienKet(), record.getMaChienDich()) == null) {
            return "Hồ sơ đăng ký TNV liên kết không tồn tại.";
        }
        if ("Điểm danh".equals(record.getNhomBang())) {
            ParticipantModel participant = findParticipant(record.getMaLienKet(), record.getMaChienDich());
            if (participant == null || !"Đã duyệt".equals(participant.getTrangThaiDuyet())) {
                return "Chỉ có thể điểm danh TNV đã được duyệt.";
            }
        }
        if ("Quyên góp".equals(record.getNhomBang())) {
            DonationModel donation = findDonation(record.getMaLienKet());
            if (donation == null) {
                return "Khoản quyên góp liên kết không tồn tại.";
            }
            if (!donation.getHoatDong().equalsIgnoreCase(record.getMaChienDich())) {
                return "Khoản quyên góp không thuộc chiến dịch đang xử lý.";
            }
        }
        return null;
    }

    public static String validateTrainingCourse(TrainingCourse course) {
        if (course == null) {
            return "Khóa học không hợp lệ.";
        }
        if (isBlank(course.getMaKhoaHoc()) || isBlank(course.getTenKhoaHoc())) {
            return "Khóa học phải có mã và tên.";
        }
        if (parseDate(course.getNgayBatDau()) == null) {
            return "Ngày bắt đầu phải có dạng dd/MM/yyyy.";
        }
        if (parseDate(course.getNgayKetThuc()) == null) {
            return "Ngày kết thúc phải có dạng dd/MM/yyyy.";
        }
        if (parseDate(course.getNgayKetThuc()).isBefore(parseDate(course.getNgayBatDau()))) {
            return "Ngày kết thúc không được trước ngày bắt đầu.";
        }
        if (course.getSoGio() <= 0) {
            return "Số giờ đào tạo phải lớn hơn 0.";
        }
        if (isBlank(course.getGiangVien())) {
            return "Khóa học phải có giảng viên.";
        }
        if (AppData.findCampaign(course.getMaChienDich()) == null) {
            return "Chiến dịch liên kết không tồn tại.";
        }
        return null;
    }

    public static String validateExpense(Expense expense) {
        if (expense == null) {
            return "Phiếu chi không hợp lệ.";
        }
        if (isBlank(expense.getMaChiPhi()) || isBlank(expense.getMoTa())) {
            return "Phiếu chi phải có mã và mô tả.";
        }
        if (expense.getSoTien() <= 0) {
            return "Số tiền chi phải lớn hơn 0.";
        }
        if (isBlank(expense.getDanhMuc())) {
            return "Vui lòng chọn danh mục chi.";
        }
        if (parseDate(expense.getNgayDeXuat()) == null) {
            return "Ngày đề xuất phải có dạng dd/MM/yyyy.";
        }
        if (!isBlank(expense.getNgayDuyet()) && parseDate(expense.getNgayDuyet()) == null) {
            return "Ngày duyệt phải có dạng dd/MM/yyyy.";
        }
        if (AppData.findCampaign(expense.getMaChienDich()) == null) {
            return "Chiến dịch liên kết không tồn tại.";
        }
        return null;
    }

    public static String canDeleteCampaign(ActivityModel campaign) {
        if (campaign == null) {
            return "Chiến dịch không tồn tại.";
        }
        String campaignId = campaign.getMaChienDich();
        long participants = AppData.getParticipants().stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .count();
        long donations = AppData.getDonations().stream()
                .filter(item -> item.getHoatDong().equalsIgnoreCase(campaignId))
                .count();
        long sponsors = AppData.getSponsors().stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .count();
        long operations = AppData.getOperations().stream()
                .filter(item -> item.getMaChienDich().equalsIgnoreCase(campaignId))
                .count();
        if (participants + donations + sponsors + operations > 0) {
            return "Không thể xóa chiến dịch đã có TNV, tài trợ, quyên góp hoặc bản ghi vận hành liên quan.";
        }
        return null;
    }

    public static String canDeleteParticipant(ParticipantModel participant) {
        if (participant == null) {
            return "TNV không tồn tại.";
        }
        boolean hasOperation = AppData.getOperations().stream()
                .anyMatch(item -> item.getMaLienKet().equalsIgnoreCase(participant.getMaTaiKhoan())
                && item.getMaChienDich().equalsIgnoreCase(participant.getMaChienDich()));
        if (hasOperation) {
            return "Không thể xóa TNV đã có đăng ký, phân công, điểm danh hoặc minh chứng liên quan.";
        }
        return null;
    }

    public static String canDeleteDonation(DonationModel donation) {
        if (donation == null) {
            return "Khoản quyên góp không tồn tại.";
        }
        boolean hasOperation = AppData.getOperations().stream()
                .anyMatch(item -> item.getMaLienKet().equalsIgnoreCase(donation.getMaQuyenGop()));
        if (hasOperation) {
            return "Không thể xóa khoản quyên góp đã có bản ghi vận hành/xác nhận liên quan.";
        }
        return null;
    }

    public static void applyOperation(SystemRecord record) {
        if (record == null) {
            return;
        }
        if ("Chiến dịch".equals(record.getNhomBang())) {
            ActivityModel campaign = AppData.findCampaign(record.getMaLienKet());
            if (campaign == null) {
                campaign = AppData.findCampaign(record.getMaChienDich());
            }
            if (campaign != null) {
                campaign.setTrangThai(record.getTrangThai());
            }
            return;
        }
        if ("Đăng ký TNV".equals(record.getNhomBang())) {
            ParticipantModel participant = findParticipant(record.getMaLienKet(), record.getMaChienDich());
            if (participant != null) {
                participant.setTrangThaiDuyet(record.getTrangThai());
            }
            return;
        }
        if ("Điểm danh".equals(record.getNhomBang()) && "Có mặt".equals(record.getTrangThai())) {
            ParticipantModel participant = findParticipant(record.getMaLienKet(), record.getMaChienDich());
            if (participant != null && isBlank(participant.getDiemDanhGia())) {
                participant.setDiemDanhGia("8.5");
            }
        }
    }

    public static boolean canRegister(ActivityModel campaign) {
        return campaign != null && OPEN_CAMPAIGN_STATUSES.contains(campaign.getTrangThai());
    }

    public static boolean canReceiveSupport(ActivityModel campaign) {
        return campaign != null && !CLOSED_CAMPAIGN_STATUSES.contains(campaign.getTrangThai());
    }

    public static boolean isGmail(String value) {
        return value != null && value.toLowerCase().matches("^[a-z0-9._%+-]+@gmail\\.com$");
    }

    public static boolean isPhone(String value) {
        return value != null && value.matches("^09\\d{8}$");
    }

    private static boolean isMoneyType(String type) {
        return "Tiền".equals(type) || "Tài trợ tiền".equals(type) || "Chuyển khoản".equals(type);
    }

    private static ParticipantModel findParticipant(String accountId, String campaignId) {
        return AppData.getParticipants().stream()
                .filter(item -> item.getMaTaiKhoan().equalsIgnoreCase(accountId)
                && item.getMaChienDich().equalsIgnoreCase(campaignId))
                .findFirst()
                .orElse(null);
    }

    private static DonationModel findDonation(String donationId) {
        return AppData.getDonations().stream()
                .filter(item -> item.getMaQuyenGop().equalsIgnoreCase(donationId))
                .findFirst()
                .orElse(null);
    }

    private static Set<String> allowedStatusesFor(String operationType) {
        if ("Điểm danh".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Chờ duyệt", "Có mặt"));
        }
        if ("Công việc".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Đang phân công", "Đã phân công"));
        }
        if ("Đăng ký TNV".equals(operationType) || "Chiến dịch".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Chờ duyệt", "Đang xét", "Đã duyệt"));
        }
        if ("Chi tiêu".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Chờ duyệt", "Đã duyệt"));
        }
        if ("Minh chứng TNV".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Chờ xác nhận", "Xác nhận"));
        }
        if ("Xuất vật phẩm".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Chờ xác nhận", "Đã xuất"));
        }
        if ("Quyên góp".equals(operationType)) {
            return new HashSet<>(Arrays.asList("Chờ xác nhận", "Đã xác nhận", "Từ chối"));
        }
        return new HashSet<>(Arrays.asList("Chờ duyệt", "Đã duyệt"));
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException | NullPointerException ex) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
