package com.mycompany.charitymanagement;

public final class BusinessService {

    private BusinessService() {
    }

    public static String registerVolunteer(UserAccount user, ActivityModel campaign) {
        String error = BusinessRules.validateVolunteerRegistration(user, campaign);
        if (error != null) {
            return error;
        }

        ParticipantModel existingProfile = findLatestProfile(user.getUsername());
        AppData.getParticipants().add(new ParticipantModel(
                user.getUsername(),
                valueOrFallback(user.getLinkedId(), existingProfile == null ? "" : existingProfile.getMaHoSo()),
                valueOrFallback(user.getDisplayName(), existingProfile == null ? "" : existingProfile.getHoTen()),
                existingProfile == null ? "" : existingProfile.getMssv(),
                existingProfile == null ? "" : existingProfile.getSoDienThoai(),
                existingProfile == null ? "Khoa Công nghệ thông tin" : existingProfile.getKhoa(),
                existingProfile == null ? "UIT" : existingProfile.getTruong(),
                campaign.getMaChienDich(),
                "Chờ duyệt",
                ""
        ));
        AppData.getOperations().add(new SystemRecord("Đăng ký TNV", AppData.nextOperationId("VH"),
                campaign.getMaChienDich(), user.getUsername(), "Đăng ký tham gia chiến dịch",
                user.getDisplayName() + " đăng ký tham gia " + campaign.getTenChienDich(),
                AppData.todayText(), "", "Chờ duyệt", user.getUsername(), defaultAdmin(), "Bảng ThamGiaTNV"));
        notifyUser(user.getUsername(), "Đã gửi đăng ký", "Hồ sơ tham gia " + campaign.getTenChienDich() + " đang chờ quản lý duyệt.");
        audit(user.getUsername(), "Đăng ký chiến dịch", user.getDisplayName() + " đăng ký " + campaign.getMaChienDich());
        return null;
    }

    public static String syncVolunteerRegistration(ParticipantModel participant, String actor) {
        String error = BusinessRules.validateParticipant(participant);
        if (error != null) {
            return error;
        }

        SystemRecord record = findRegistrationOperation(participant.getMaTaiKhoan(), participant.getMaChienDich());
        String owner = valueOrFallback(actor, participant.getMaTaiKhoan());
        String detail = participant.getHoTen() + " - " + participant.getTruong()
                + " đăng ký tham gia chiến dịch " + participant.getMaChienDich();
        String processedDate = "Đã duyệt".equals(participant.getTrangThaiDuyet()) ? AppData.todayText() : "";

        if (record == null) {
            AppData.getOperations().add(new SystemRecord("Đăng ký TNV", AppData.nextOperationId("VH"),
                    participant.getMaChienDich(), participant.getMaTaiKhoan(),
                    "Đăng ký tham gia chiến dịch", detail,
                    AppData.todayText(), processedDate, participant.getTrangThaiDuyet(),
                    owner, defaultAdmin(), "Bảng ThamGiaTNV"));
        } else {
            record.setNhomBang("Đăng ký TNV");
            record.setMaChienDich(participant.getMaChienDich());
            record.setMaLienKet(participant.getMaTaiKhoan());
            record.setTieuDe("Đăng ký tham gia chiến dịch");
            record.setNoiDung(detail);
            record.setNgayXuLy(processedDate);
            record.setTrangThai(participant.getTrangThaiDuyet());
            record.setNguoiTao(owner);
            if (record.getNguoiXuLy() == null || record.getNguoiXuLy().trim().isEmpty()) {
                record.setNguoiXuLy(defaultAdmin());
            }
            record.setGhiChu("Bảng ThamGiaTNV");
        }

        if ("Đã duyệt".equals(participant.getTrangThaiDuyet())) {
            notifyUser(participant.getMaTaiKhoan(), "Đăng ký đã được duyệt",
                    "Bạn đã được duyệt tham gia chiến dịch " + participant.getMaChienDich() + ".");
        }
        audit(owner, "Cập nhật hồ sơ TNV", participant.getMaTaiKhoan() + " - " + participant.getMaChienDich());
        return null;
    }

    public static String checkIn(UserAccount user, ParticipantModel profile) {
        String error = BusinessRules.validateCheckIn(user, profile);
        if (error != null) {
            return error;
        }

        AppData.getOperations().add(new SystemRecord("Điểm danh", AppData.nextOperationId("VH"),
                profile.getMaChienDich(), user.getUsername(), "Tình nguyện viên tự điểm danh",
                "Ghi nhận điểm danh cho " + profile.getMaChienDich(), AppData.todayText(), "",
                "Chờ duyệt", user.getUsername(), defaultAdmin(), "Bảng DiemDanh"));
        notifyUser(user.getUsername(), "Đã gửi điểm danh", "Điểm danh đang chờ quản lý xác nhận.");
        audit(user.getUsername(), "Điểm danh", user.getDisplayName() + " điểm danh " + profile.getMaChienDich());
        return null;
    }

    public static String submitProof(UserAccount user, ParticipantModel profile, String proofType, String note) {
        String error = BusinessRules.validateProof(user, profile, proofType, note);
        if (error != null) {
            return error;
        }

        AppData.getOperations().add(new SystemRecord("Minh chứng TNV", AppData.nextOperationId("VH"),
                profile.getMaChienDich(), user.getUsername(), "Gửi minh chứng TNV",
                proofType + " - " + note, AppData.todayText(), "",
                "Chờ xác nhận", user.getUsername(), defaultAdmin(), "Bảng MinhChungTNV"));
        notifyUser(user.getUsername(), "Đã gửi minh chứng", "Minh chứng của bạn đang chờ quản lý xác nhận.");
        audit(user.getUsername(), "Gửi minh chứng", user.getDisplayName() + " gửi minh chứng " + profile.getMaChienDich());
        return null;
    }

    public static String recordDonation(UserAccount user, String campaignId, String type, String content, double amount) {
        String donorName = user == null ? "Nhà hảo tâm" : valueOrFallback(user.getDisplayName(), user.getUsername());
        DonationModel donation = new DonationModel(AppData.nextDonationId(), donorName, campaignId,
                AppData.todayText(), type, content, amount);
        return recordDonation(donation, user == null ? donorName : user.getUsername());
    }

    public static String recordDonation(DonationModel donation, String actor) {
        String error = BusinessRules.validateDonation(donation);
        if (error != null) {
            return error;
        }
        if (donationIdExists(donation.getMaQuyenGop())) {
            return "Mã quyên góp đã tồn tại.";
        }

        AppData.getDonations().add(donation);
        AppData.getOperations().add(new SystemRecord("Quyên góp", AppData.nextOperationId("VH"),
                donation.getHoatDong(), donation.getMaQuyenGop(), "Xác nhận quyên góp",
                donation.getNguoiQuyenGop() + " gửi " + donation.getHinhThuc() + " cho chiến dịch " + donation.getHoatDong(),
                donation.getNgayQuyenGop(), "", "Chờ xác nhận",
                valueOrFallback(actor, donation.getNguoiQuyenGop()), defaultAdmin(), "Bảng QuyenGopTien/PhieuQuyenGopVP"));
        notifyUser(valueOrFallback(actor, donation.getNguoiQuyenGop()), "Đã gửi đề xuất tài trợ",
                "Khoản tài trợ/quyên góp " + donation.getMaQuyenGop() + " đang chờ xác nhận.");
        audit(valueOrFallback(actor, donation.getNguoiQuyenGop()), "Gửi tài trợ/quyên góp",
                donation.getNguoiQuyenGop() + " gửi " + donation.getMaQuyenGop());
        return null;
    }

    public static String syncDonationOperation(DonationModel donation, String actor) {
        String error = BusinessRules.validateDonation(donation);
        if (error != null) {
            return error;
        }

        SystemRecord record = findDonationOperation(donation.getMaQuyenGop());
        String owner = valueOrFallback(actor, donation.getNguoiQuyenGop());
        String detail = donation.getNguoiQuyenGop() + " gửi " + donation.getHinhThuc()
                + " cho chiến dịch " + donation.getHoatDong();

        if (record == null) {
            AppData.getOperations().add(new SystemRecord("Quyên góp", AppData.nextOperationId("VH"),
                    donation.getHoatDong(), donation.getMaQuyenGop(), "Xác nhận quyên góp",
                    detail, donation.getNgayQuyenGop(), "", "Chờ xác nhận",
                    owner, defaultAdmin(), "Bảng QuyenGopTien/PhieuQuyenGopVP"));
        } else {
            record.setNhomBang("Quyên góp");
            record.setMaChienDich(donation.getHoatDong());
            record.setMaLienKet(donation.getMaQuyenGop());
            record.setTieuDe("Xác nhận quyên góp");
            record.setNoiDung(detail);
            record.setNgay(donation.getNgayQuyenGop());
            record.setNguoiTao(owner);
            if (record.getNguoiXuLy() == null || record.getNguoiXuLy().trim().isEmpty()) {
                record.setNguoiXuLy(defaultAdmin());
            }
            if (record.getTrangThai() == null || record.getTrangThai().trim().isEmpty()) {
                record.setTrangThai("Chờ xác nhận");
            }
            record.setGhiChu("Bảng QuyenGopTien/PhieuQuyenGopVP");
        }

        audit(owner, "Cập nhật thông tin quyên góp", donation.getMaQuyenGop() + " - " + donation.getHinhThuc());
        return null;
    }

    public static String followCampaign(UserAccount user, ActivityModel campaign) {
        if (user == null) {
            return "Vui lòng đăng nhập trước khi theo dõi chiến dịch.";
        }
        if (campaign == null) {
            return "Vui lòng chọn chiến dịch muốn theo dõi.";
        }
        String followId = "TD" + user.getUsername() + campaign.getMaChienDich();
        boolean existed = AppData.getContents().stream()
                .anyMatch(item -> item.getMaChinh().equalsIgnoreCase(followId));
        if (existed) {
            return "Bạn đã theo dõi chiến dịch này.";
        }
        AppData.getContents().add(new SystemRecord("TheoDoi", followId, campaign.getMaChienDich(), "Theo dõi chiến dịch",
                user.getDisplayName() + " theo dõi " + campaign.getTenChienDich(),
                AppData.todayText(), "Đang theo dõi", "Bảng TheoDoi"));
        notifyUser(user.getUsername(), "Đã theo dõi chiến dịch", "Bạn sẽ nhận thông báo liên quan đến " + campaign.getTenChienDich() + ".");
        return null;
    }

    public static void applyOperation(SystemRecord record) {
        BusinessRules.applyOperation(record);
        if ("Đăng ký TNV".equals(record.getNhomBang()) && "Đã duyệt".equals(record.getTrangThai())) {
            notifyUser(record.getMaLienKet(), "Đăng ký đã được duyệt", "Bạn đã được duyệt tham gia chiến dịch " + record.getMaChienDich() + ".");
        }
        if ("Điểm danh".equals(record.getNhomBang()) && "Có mặt".equals(record.getTrangThai())) {
            notifyUser(record.getMaLienKet(), "Điểm danh đã xác nhận", "Bạn đã được ghi nhận có mặt tại chiến dịch " + record.getMaChienDich() + ".");
        }
        if ("Minh chứng TNV".equals(record.getNhomBang()) && "Xác nhận".equals(record.getTrangThai())) {
            notifyUser(record.getMaLienKet(), "Minh chứng đã xác nhận", "Minh chứng của bạn đã được quản lý xác nhận.");
        }
        if ("Quyên góp".equals(record.getNhomBang())) {
            if ("Đã xác nhận".equals(record.getTrangThai())) {
                notifyUser(record.getNguoiTao(), "Quyên góp đã xác nhận", "Khoản quyên góp " + record.getMaLienKet() + " đã được quản lý xác nhận.");
            } else if ("Từ chối".equals(record.getTrangThai())) {
                notifyUser(record.getNguoiTao(), "Quyên góp bị từ chối", "Khoản quyên góp " + record.getMaLienKet() + " cần kiểm tra lại thông tin.");
            }
        }
        audit(record.getNguoiXuLy(), "Xử lý vận hành", record.getNhomBang() + " - " + record.getMaChinh() + " - " + record.getTrangThai());
    }

    public static void notifyUser(String accountId, String title, String message) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return;
        }
        AppData.getContents().add(new SystemRecord("ThongBao", AppData.nextContentId("TB"),
                accountId, title, message, AppData.todayText(), "Chưa đọc", "Bảng ThongBao"));
    }

    public static void audit(String actor, String action, String detail) {
        AppData.getContents().add(new SystemRecord("NhatKyHeThong", AppData.nextContentId("NK"),
                actor == null || actor.trim().isEmpty() ? "HE_THONG" : actor,
                action, detail, AppData.todayText(), "Đã ghi", "Bảng NhatKyHeThong"));
    }

    private static String defaultAdmin() {
        return "ADMIN001";
    }

    private static ParticipantModel findLatestProfile(String username) {
        ParticipantModel latest = null;
        for (ParticipantModel participant : AppData.getParticipants()) {
            if (participant.getMaTaiKhoan().equalsIgnoreCase(username)) {
                latest = participant;
            }
        }
        return latest;
    }

    private static String valueOrFallback(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        return fallback == null ? "" : fallback;
    }

    private static boolean donationIdExists(String donationId) {
        return AppData.getDonations().stream()
                .anyMatch(item -> item.getMaQuyenGop().equalsIgnoreCase(donationId));
    }

    private static SystemRecord findDonationOperation(String donationId) {
        return AppData.getOperations().stream()
                .filter(record -> "Quyên góp".equals(record.getNhomBang())
                && record.getMaLienKet().equalsIgnoreCase(donationId))
                .findFirst()
                .orElse(null);
    }

    private static SystemRecord findRegistrationOperation(String accountId, String campaignId) {
        return AppData.getOperations().stream()
                .filter(record -> "Đăng ký TNV".equals(record.getNhomBang())
                && record.getMaLienKet().equalsIgnoreCase(accountId)
                && record.getMaChienDich().equalsIgnoreCase(campaignId))
                .findFirst()
                .orElse(null);
    }
}
