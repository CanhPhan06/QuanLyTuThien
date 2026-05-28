package com.mycompany.charitymanagement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DatabaseRepository {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final String APP_META_PREFIX = "[APP_META";
    private static final String GENERIC_ITEM_TYPE = "LVPAPP";
    private static final Set<String> CAMPAIGN_STATUSES = new HashSet<>(Arrays.asList(
            "Chờ duyệt", "Đang xét", "Đã duyệt", "Đang thực hiện", "Hoàn thành", "Tạm dừng", "Từ chối"
    ));
    private static final Set<String> PARTICIPANT_STATUSES = new HashSet<>(Arrays.asList(
            "Chờ duyệt", "Đang xét", "Đã duyệt", "Từ chối"
    ));
    private static final Set<String> MONEY_DONATION_STATUSES = new HashSet<>(Arrays.asList(
            "Chờ xác nhận", "Đã xác nhận", "Từ chối"
    ));
    private static final Set<String> SCHOOLS = new HashSet<>(Arrays.asList(
            "UIT", "UEL", "HCMUS", "HCMUT", "HCMIU", "UHS", "HCMUSSH"
    ));
    private static final ExecutorService DB_WRITER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "charity-db-writer");
        thread.setDaemon(true);
        return thread;
    });

    private DatabaseRepository() {
    }

    public static void saveActivity(ActivityModel activity) {
        if (activity == null) {
            return;
        }
        runAsync("lưu chiến dịch " + activity.getMaChienDich(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                Date start = sqlDateOrToday(activity.getNgayBatDau());
                Date end = sqlDate(activity.getNgayKetThuc());
                if (end == null || end.before(start)) {
                    end = start;
                }
                String status = allowed(activity.getTrangThai(), CAMPAIGN_STATUSES, "Chờ duyệt");
                String owner = fallback(activity.getMaNguoiTao(), "ADMIN");
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE CHIEN_DICH SET TEN_CHIEN_DICH = ?, MO_TA = ?, DIA_DIEM = ?, "
                        + "NGAY_BAT_DAU = ?, NGAY_KET_THUC = ?, MUC_TIEU_TIEN = ?, TRANG_THAI = ?, "
                        + "MA_NGUOI_TAO = ? WHERE MA_CHIEN_DICH = ?")) {
                    update.setString(1, requiredText(activity.getTenChienDich(), "Chiến dịch"));
                    update.setString(2, text(activity.getMoTa()));
                    update.setString(3, requiredText(activity.getDiaDiem(), "TP.HCM"));
                    update.setDate(4, start);
                    update.setDate(5, end);
                    update.setDouble(6, Math.max(0, activity.getMucTieuTien()));
                    update.setString(7, status);
                    update.setString(8, owner);
                    update.setString(9, activity.getMaChienDich());
                    if (update.executeUpdate() > 0) {
                        return;
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO CHIEN_DICH (MA_CHIEN_DICH, TEN_CHIEN_DICH, MO_TA, DIA_DIEM, "
                        + "NGAY_BAT_DAU, NGAY_KET_THUC, MUC_TIEU_TIEN, TRANG_THAI, MA_NGUOI_TAO, NGAY_TAO) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)")) {
                    insert.setString(1, activity.getMaChienDich());
                    insert.setString(2, requiredText(activity.getTenChienDich(), "Chiến dịch"));
                    insert.setString(3, text(activity.getMoTa()));
                    insert.setString(4, requiredText(activity.getDiaDiem(), "TP.HCM"));
                    insert.setDate(5, start);
                    insert.setDate(6, end);
                    insert.setDouble(7, Math.max(0, activity.getMucTieuTien()));
                    insert.setString(8, status);
                    insert.setString(9, owner);
                    insert.executeUpdate();
                }
            }
        });
    }

    public static void deleteActivity(ActivityModel activity) {
        if (activity == null) {
            return;
        }
        runAsync("xóa chiến dịch " + activity.getMaChienDich(), () -> {
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM CHIEN_DICH WHERE MA_CHIEN_DICH = ?")) {
                statement.setString(1, activity.getMaChienDich());
                statement.executeUpdate();
            }
        });
    }

    public static void updateAccountPassword(UserAccount account) {
        if (account == null) {
            return;
        }
        runAsync("đổi mật khẩu " + account.getUsername(), () -> {
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE TAI_KHOAN SET MAT_KHAU = ? WHERE MA_TAI_KHOAN = ? OR TEN_DANG_NHAP = ?")) {
                statement.setString(1, account.getPassword());
                statement.setString(2, account.getUsername());
                statement.setString(3, account.getUsername());
                statement.executeUpdate();
            }
        });
    }

    public static void saveParticipant(ParticipantModel participant) {
        if (participant == null) {
            return;
        }
        runAsync("lưu TNV " + participant.getMaTaiKhoan(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                connection.setAutoCommit(false);
                upsertStudentProfile(connection, participant);
                upsertVolunteerRegistration(connection, participant);
                connection.commit();
            }
        });
    }

    public static void deleteParticipant(ParticipantModel participant) {
        if (participant == null) {
            return;
        }
        runAsync("xóa đăng ký TNV " + participant.getMaTaiKhoan(), () -> {
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM THAM_GIA_TNV WHERE MA_TAI_KHOAN = ? AND MA_CHIEN_DICH = ?")) {
                statement.setString(1, participant.getMaTaiKhoan());
                statement.setString(2, participant.getMaChienDich());
                statement.executeUpdate();
            }
        });
    }

    public static void saveSponsor(SponsorModel sponsor) {
        if (sponsor == null) {
            return;
        }
        runAsync("lưu nhà tài trợ " + sponsor.getMaDoiTac(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                connection.setAutoCommit(false);
                updateSponsorAccount(connection, sponsor);
                upsertPartner(connection, sponsor);
                upsertSponsorship(connection, sponsor);
                connection.commit();
            }
        });
    }

    public static void deleteSponsor(SponsorModel sponsor) {
        if (sponsor == null) {
            return;
        }
        runAsync("xóa nhà tài trợ " + sponsor.getMaDoiTac(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement sponsorship = connection.prepareStatement(
                        "DELETE FROM TAI_TRO WHERE MA_DOI_TAC = ? AND MA_CHIEN_DICH = ?")) {
                    sponsorship.setString(1, sponsor.getMaDoiTac());
                    sponsorship.setString(2, sponsor.getMaChienDich());
                    sponsorship.executeUpdate();
                }
                try (PreparedStatement partner = connection.prepareStatement(
                        "DELETE FROM DOI_TAC WHERE MA_DOI_TAC = ? "
                        + "AND NOT EXISTS (SELECT 1 FROM TAI_TRO WHERE MA_DOI_TAC = ?)")) {
                    partner.setString(1, sponsor.getMaDoiTac());
                    partner.setString(2, sponsor.getMaDoiTac());
                    partner.executeUpdate();
                }
                connection.commit();
            }
        });
    }

    public static void saveDonation(DonationModel donation) {
        if (donation == null) {
            return;
        }
        runAsync("lưu quyên góp " + donation.getMaQuyenGop(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                connection.setAutoCommit(false);
                if (isItemDonation(donation)) {
                    deleteMoneyDonation(connection, donation.getMaQuyenGop());
                    upsertItemDonation(connection, donation);
                } else {
                    deleteItemDonation(connection, donation.getMaQuyenGop());
                    upsertMoneyDonation(connection, donation);
                }
                connection.commit();
            }
        });
    }

    public static void updateDonationStatus(String donationId, String status) {
        if (isBlank(donationId)) {
            return;
        }
        runAsync("cập nhật trạng thái quyên góp " + donationId, () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                String value = allowed(status, MONEY_DONATION_STATUSES, "Chờ xác nhận");
                try (PreparedStatement money = connection.prepareStatement(
                        "UPDATE QUYEN_GOP_TIEN SET TRANG_THAI = ? WHERE MA_QUYEN_GOP = ?")) {
                    money.setString(1, value);
                    money.setString(2, donationId);
                    money.executeUpdate();
                }
                try (PreparedStatement items = connection.prepareStatement(
                        "UPDATE PHIEU_QUYEN_GOP_VP SET TRANG_THAI = ? WHERE MA_PHIEU_QG = ?")) {
                    items.setString(1, value);
                    items.setString(2, donationId);
                    items.executeUpdate();
                }
            }
        });
    }

    public static void deleteDonation(DonationModel donation) {
        if (donation == null) {
            return;
        }
        runAsync("xóa quyên góp " + donation.getMaQuyenGop(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                connection.setAutoCommit(false);
                deleteMoneyDonation(connection, donation.getMaQuyenGop());
                deleteItemDonation(connection, donation.getMaQuyenGop());
                connection.commit();
            }
        });
    }

    public static void saveOperation(SystemRecord record) {
        if (record == null) {
            return;
        }
        runAsync("lưu vận hành " + record.getMaChinh(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE VAN_HANH SET NHOM_BANG = ?, MA_CHIEN_DICH = ?, MA_LIEN_KET = ?, "
                        + "TIEU_DE = ?, NOI_DUNG = ?, NGAY_TAO = ?, NGAY_XU_LY = ?, TRANG_THAI = ?, "
                        + "NGUOI_TAO = ?, NGUOI_XU_LY = ?, GHI_CHU = ? WHERE MA_CHINH = ?")) {
                    setOperationFields(update, record, false);
                    if (update.executeUpdate() > 0) {
                        return;
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO VAN_HANH (NHOM_BANG, MA_CHINH, MA_CHIEN_DICH, MA_LIEN_KET, "
                        + "TIEU_DE, NOI_DUNG, NGAY_TAO, NGAY_XU_LY, TRANG_THAI, NGUOI_TAO, NGUOI_XU_LY, GHI_CHU) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    setOperationFields(insert, record, true);
                    insert.executeUpdate();
                }
            }
        });
    }

    public static void deleteOperation(SystemRecord record) {
        if (record == null) {
            return;
        }
        runAsync("xóa vận hành " + record.getMaChinh(), () -> {
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM VAN_HANH WHERE MA_CHINH = ?")) {
                statement.setString(1, record.getMaChinh());
                statement.executeUpdate();
            }
        });
    }

    public static void saveContent(SystemRecord record) {
        if (record == null) {
            return;
        }
        runAsync("lưu nội dung " + record.getMaChinh(), () -> {
            try (Connection connection = DatabaseConfig.getConnection()) {
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE NOI_DUNG SET NHOM_BANG = ?, MA_LIEN_KET = ?, TIEU_DE = ?, NOI_DUNG = ?, "
                        + "NGAY_TAO = ?, TRANG_THAI = ?, GHI_CHU = ? WHERE MA_CHINH = ?")) {
                    setContentFields(update, record, false);
                    if (update.executeUpdate() > 0) {
                        return;
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO NOI_DUNG (NHOM_BANG, MA_CHINH, MA_LIEN_KET, TIEU_DE, NOI_DUNG, NGAY_TAO, TRANG_THAI, GHI_CHU) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    setContentFields(insert, record, true);
                    insert.executeUpdate();
                }
            }
        });
    }

    public static void deleteContent(SystemRecord record) {
        if (record == null) {
            return;
        }
        runAsync("xóa nội dung " + record.getMaChinh(), () -> {
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM NOI_DUNG WHERE MA_CHINH = ?")) {
                statement.setString(1, record.getMaChinh());
                statement.executeUpdate();
            }
        });
    }

    public static String stripMetadata(String note) {
        if (note == null || note.isBlank()) {
            return "";
        }
        String value = UiText.clean(note);
        int start = value.indexOf(APP_META_PREFIX);
        while (start >= 0) {
            int end = value.indexOf(']', start);
            if (end < 0) {
                break;
            }
            value = (value.substring(0, start) + value.substring(end + 1)).trim();
            start = value.indexOf(APP_META_PREFIX);
        }
        return value.trim();
    }

    public static String metadataValue(String note, String key) {
        if (note == null || key == null) {
            return "";
        }
        int start = note.indexOf(APP_META_PREFIX);
        if (start < 0) {
            return "";
        }
        int end = note.indexOf(']', start);
        if (end < 0) {
            return "";
        }
        String target = key.trim().toUpperCase(Locale.ROOT) + "=";
        String meta = note.substring(start + APP_META_PREFIX.length(), end).trim();
        for (String token : meta.split("\\s+")) {
            if (token.toUpperCase(Locale.ROOT).startsWith(target)) {
                return token.substring(target.length());
            }
        }
        return "";
    }

    private static void upsertStudentProfile(Connection connection, ParticipantModel participant) throws SQLException {
        updateVolunteerAccount(connection, participant);
        String accountId = participant.getMaTaiKhoan();
        String profileId = profileId(participant);
        String fullName = requiredText(participant.getHoTen(), accountId);
        String studentCode = studentCode(participant);
        String phone = phone(participant.getSoDienThoai());
        String email = emailFromStudentCode(accountId, studentCode);
        String department = requiredText(participant.getKhoa(), "Khoa Công nghệ thông tin");
        String school = school(participant.getTruong());

        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE HO_SO_SINH_VIEN SET MA_HO_SO = ?, HO_TEN = ?, MSSV = ?, SO_DIEN_THOAI = ?, "
                + "EMAIL = ?, KHOA = ?, TRUONG = ? WHERE MA_TAI_KHOAN = ?")) {
            update.setString(1, profileId);
            update.setString(2, fullName);
            update.setString(3, studentCode);
            update.setString(4, phone);
            setNullableString(update, 5, email);
            update.setString(6, department);
            update.setString(7, school);
            update.setString(8, accountId);
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO HO_SO_SINH_VIEN (MA_HO_SO, MA_TAI_KHOAN, HO_TEN, MSSV, SO_DIEN_THOAI, EMAIL, KHOA, TRUONG, NGAY_TAO) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)")) {
            insert.setString(1, profileId);
            insert.setString(2, accountId);
            insert.setString(3, fullName);
            insert.setString(4, studentCode);
            insert.setString(5, phone);
            setNullableString(insert, 6, email);
            insert.setString(7, department);
            insert.setString(8, school);
            insert.executeUpdate();
        }
    }

    private static void upsertVolunteerRegistration(Connection connection, ParticipantModel participant) throws SQLException {
        String status = allowed(participant.getTrangThaiDuyet(), PARTICIPANT_STATUSES, "Chờ duyệt");
        Double score = score(participant.getDiemDanhGia());
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE THAM_GIA_TNV SET TRANG_THAI_DUYET = ?, DIEM_DANH_GIA = ?, GHI_CHU = ? "
                + "WHERE MA_TAI_KHOAN = ? AND MA_CHIEN_DICH = ?")) {
            update.setString(1, status);
            setNullableDouble(update, 2, score);
            update.setString(3, "Đồng bộ từ giao diện JavaFX");
            update.setString(4, participant.getMaTaiKhoan());
            update.setString(5, participant.getMaChienDich());
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO THAM_GIA_TNV (MA_THAM_GIA, MA_TAI_KHOAN, MA_CHIEN_DICH, NGAY_DANG_KY, "
                + "TRANG_THAI_DUYET, DIEM_DANH_GIA, GHI_CHU) "
                + "VALUES (FN_NEXT_ID('TG', SEQ_TG.NEXTVAL), ?, ?, SYSDATE, ?, ?, ?)")) {
            insert.setString(1, participant.getMaTaiKhoan());
            insert.setString(2, participant.getMaChienDich());
            insert.setString(3, status);
            setNullableDouble(insert, 4, score);
            insert.setString(5, "Đồng bộ từ giao diện JavaFX");
            insert.executeUpdate();
        }
    }

    private static void updateVolunteerAccount(Connection connection, ParticipantModel participant) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE TAI_KHOAN SET HO_TEN_HIEN_THI = ?, SO_DIEN_THOAI = ? "
                + "WHERE MA_TAI_KHOAN = ? OR TEN_DANG_NHAP = ?")) {
            update.setString(1, requiredText(participant.getHoTen(), participant.getMaTaiKhoan()));
            update.setString(2, phone(participant.getSoDienThoai()));
            update.setString(3, participant.getMaTaiKhoan());
            update.setString(4, participant.getMaTaiKhoan());
            update.executeUpdate();
        }
    }

    private static void updateSponsorAccount(Connection connection, SponsorModel sponsor) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE TAI_KHOAN SET HO_TEN_HIEN_THI = ?, EMAIL = ?, SO_DIEN_THOAI = ? "
                + "WHERE MA_LIEN_KET = ?")) {
            update.setString(1, requiredText(sponsor.getTenDoiTac(), sponsor.getMaDoiTac()));
            update.setString(2, gmailOrNull(sponsor.getEmail()));
            update.setString(3, phone(sponsor.getSoDienThoai()));
            update.setString(4, sponsor.getMaDoiTac());
            update.executeUpdate();
        }
    }

    private static void upsertPartner(Connection connection, SponsorModel sponsor) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE DOI_TAC SET TEN_DOI_TAC = ?, LINH_VUC = ?, SO_DIEN_THOAI = ?, EMAIL = ?, DIA_CHI = ? "
                + "WHERE MA_DOI_TAC = ?")) {
            update.setString(1, requiredText(sponsor.getTenDoiTac(), sponsor.getMaDoiTac()));
            update.setString(2, requiredText(sponsor.getLinhVuc(), "Thiện nguyện"));
            update.setString(3, phone(sponsor.getSoDienThoai()));
            update.setString(4, gmailOrNull(sponsor.getEmail()));
            update.setString(5, text(sponsor.getDiaChi()));
            update.setString(6, sponsor.getMaDoiTac());
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO DOI_TAC (MA_DOI_TAC, TEN_DOI_TAC, LINH_VUC, SO_DIEN_THOAI, EMAIL, DIA_CHI) "
                + "VALUES (?, ?, ?, ?, ?, ?)")) {
            insert.setString(1, sponsor.getMaDoiTac());
            insert.setString(2, requiredText(sponsor.getTenDoiTac(), sponsor.getMaDoiTac()));
            insert.setString(3, requiredText(sponsor.getLinhVuc(), "Thiện nguyện"));
            insert.setString(4, phone(sponsor.getSoDienThoai()));
            insert.setString(5, gmailOrNull(sponsor.getEmail()));
            insert.setString(6, text(sponsor.getDiaChi()));
            insert.executeUpdate();
        }
    }

    private static void upsertSponsorship(Connection connection, SponsorModel sponsor) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE TAI_TRO SET GIA_TRI_TAI_TRO = ?, NGAY_KY_KET = ?, HINH_THUC = ?, TRANG_THAI = ? "
                + "WHERE MA_DOI_TAC = ? AND MA_CHIEN_DICH = ?")) {
            update.setDouble(1, Math.max(0, sponsor.getGiaTriTaiTro()));
            update.setDate(2, sqlDateOrToday(sponsor.getNgayKyKet()));
            update.setString(3, sponsorshipType(sponsor));
            update.setString(4, "Đã xác nhận");
            update.setString(5, sponsor.getMaDoiTac());
            update.setString(6, sponsor.getMaChienDich());
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO TAI_TRO (MA_TAI_TRO, MA_DOI_TAC, MA_CHIEN_DICH, GIA_TRI_TAI_TRO, NGAY_KY_KET, HINH_THUC, TRANG_THAI) "
                + "VALUES (FN_NEXT_ID('TTRO', SEQ_TTRO.NEXTVAL), ?, ?, ?, ?, ?, ?)")) {
            insert.setString(1, sponsor.getMaDoiTac());
            insert.setString(2, sponsor.getMaChienDich());
            insert.setDouble(3, Math.max(0, sponsor.getGiaTriTaiTro()));
            insert.setDate(4, sqlDateOrToday(sponsor.getNgayKyKet()));
            insert.setString(5, sponsorshipType(sponsor));
            insert.setString(6, "Đã xác nhận");
            insert.executeUpdate();
        }
    }

    private static void upsertMoneyDonation(Connection connection, DonationModel donation) throws SQLException {
        String status = allowed(donation.getTrangThaiXuLy(), MONEY_DONATION_STATUSES, "Chờ xác nhận");
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE QUYEN_GOP_TIEN SET MA_TAI_KHOAN = ?, MA_CHIEN_DICH = ?, NGUOI_QUYEN_GOP = ?, "
                + "SO_TIEN = ?, NGAY_GIAO_DICH = ?, PHUONG_THUC = ?, TRANG_THAI = ? WHERE MA_QUYEN_GOP = ?")) {
            setNullableString(update, 1, accountForDonor(donation.getNguoiQuyenGop()));
            update.setString(2, donation.getHoatDong());
            update.setString(3, donation.getNguoiQuyenGop());
            update.setDouble(4, Math.max(1, donation.getSoTien()));
            update.setDate(5, sqlDateOrToday(donation.getNgayQuyenGop()));
            update.setString(6, moneyMethod(donation.getHinhThuc()));
            update.setString(7, status);
            update.setString(8, donation.getMaQuyenGop());
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO QUYEN_GOP_TIEN (MA_QUYEN_GOP, MA_TAI_KHOAN, MA_CHIEN_DICH, NGUOI_QUYEN_GOP, "
                + "SO_TIEN, NGAY_GIAO_DICH, PHUONG_THUC, TRANG_THAI) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            insert.setString(1, donation.getMaQuyenGop());
            setNullableString(insert, 2, accountForDonor(donation.getNguoiQuyenGop()));
            insert.setString(3, donation.getHoatDong());
            insert.setString(4, donation.getNguoiQuyenGop());
            insert.setDouble(5, Math.max(1, donation.getSoTien()));
            insert.setDate(6, sqlDateOrToday(donation.getNgayQuyenGop()));
            insert.setString(7, moneyMethod(donation.getHinhThuc()));
            insert.setString(8, status);
            insert.executeUpdate();
        }
    }

    private static void upsertItemDonation(Connection connection, DonationModel donation) throws SQLException {
        ensureGenericItemType(connection);
        String status = allowed(donation.getTrangThaiXuLy(), MONEY_DONATION_STATUSES, "Chờ xác nhận");
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE PHIEU_QUYEN_GOP_VP SET MA_TAI_KHOAN = ?, MA_CHIEN_DICH = ?, NGUOI_QUYEN_GOP = ?, "
                + "NGAY_TIEP_NHAN = ?, TRANG_THAI = ?, GHI_CHU = ? WHERE MA_PHIEU_QG = ?")) {
            setNullableString(update, 1, accountForDonor(donation.getNguoiQuyenGop()));
            update.setString(2, donation.getHoatDong());
            update.setString(3, donation.getNguoiQuyenGop());
            update.setDate(4, sqlDateOrToday(donation.getNgayQuyenGop()));
            update.setString(5, status);
            update.setString(6, truncate(requiredText(donation.getNoiDungQuyenGop(), donation.getHinhThuc()), 400));
            update.setString(7, donation.getMaQuyenGop());
            if (update.executeUpdate() > 0) {
                upsertGenericItemDetail(connection, donation.getMaQuyenGop());
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO PHIEU_QUYEN_GOP_VP (MA_PHIEU_QG, MA_TAI_KHOAN, MA_CHIEN_DICH, NGUOI_QUYEN_GOP, "
                + "NGAY_TIEP_NHAN, TRANG_THAI, GHI_CHU) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            insert.setString(1, donation.getMaQuyenGop());
            setNullableString(insert, 2, accountForDonor(donation.getNguoiQuyenGop()));
            insert.setString(3, donation.getHoatDong());
            insert.setString(4, donation.getNguoiQuyenGop());
            insert.setDate(5, sqlDateOrToday(donation.getNgayQuyenGop()));
            insert.setString(6, status);
            insert.setString(7, truncate(requiredText(donation.getNoiDungQuyenGop(), donation.getHinhThuc()), 400));
            insert.executeUpdate();
        }
        upsertGenericItemDetail(connection, donation.getMaQuyenGop());
    }

    private static void ensureGenericItemType(Connection connection) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE LOAI_VAT_PHAM SET TEN_LOAI = ?, DON_VI_TINH = ? WHERE MA_LOAI = ?")) {
            update.setString(1, "Hỗ trợ khác");
            update.setString(2, "Gói");
            update.setString(3, GENERIC_ITEM_TYPE);
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO LOAI_VAT_PHAM (MA_LOAI, TEN_LOAI, DON_VI_TINH, SO_LUONG_TON) VALUES (?, ?, ?, 0)")) {
            insert.setString(1, GENERIC_ITEM_TYPE);
            insert.setString(2, "Hỗ trợ khác");
            insert.setString(3, "Gói");
            insert.executeUpdate();
        }
    }

    private static void upsertGenericItemDetail(Connection connection, String donationId) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE CHI_TIET_QUYEN_GOP_VP SET SO_LUONG = 1 WHERE MA_PHIEU_QG = ? AND MA_LOAI = ?")) {
            update.setString(1, donationId);
            update.setString(2, GENERIC_ITEM_TYPE);
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO CHI_TIET_QUYEN_GOP_VP (MA_PHIEU_QG, MA_LOAI, SO_LUONG) VALUES (?, ?, 1)")) {
            insert.setString(1, donationId);
            insert.setString(2, GENERIC_ITEM_TYPE);
            insert.executeUpdate();
        }
    }

    private static void deleteMoneyDonation(Connection connection, String donationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM QUYEN_GOP_TIEN WHERE MA_QUYEN_GOP = ?")) {
            statement.setString(1, donationId);
            statement.executeUpdate();
        }
    }

    private static void deleteItemDonation(Connection connection, String donationId) throws SQLException {
        try (PreparedStatement detail = connection.prepareStatement(
                "DELETE FROM CHI_TIET_QUYEN_GOP_VP WHERE MA_PHIEU_QG = ?")) {
            detail.setString(1, donationId);
            detail.executeUpdate();
        }
        try (PreparedStatement voucher = connection.prepareStatement(
                "DELETE FROM PHIEU_QUYEN_GOP_VP WHERE MA_PHIEU_QG = ?")) {
            voucher.setString(1, donationId);
            voucher.executeUpdate();
        }
    }

    private static void setOperationFields(PreparedStatement statement, SystemRecord record, boolean insert)
            throws SQLException {
        if (insert) {
            statement.setString(1, record.getNhomBang());
            statement.setString(2, record.getMaChinh());
            setNullableString(statement, 3, record.getMaChienDich());
            setNullableString(statement, 4, record.getMaLienKet());
            statement.setString(5, requiredText(record.getTieuDe(), record.getNhomBang()));
            statement.setString(6, truncate(record.getNoiDung(), 1000));
            statement.setDate(7, sqlDateOrToday(record.getNgayTao()));
            setNullableDate(statement, 8, sqlDate(record.getNgayXuLy()));
            statement.setString(9, requiredText(record.getTrangThai(), "Chờ duyệt"));
            setNullableString(statement, 10, record.getNguoiTao());
            setNullableString(statement, 11, record.getNguoiXuLy());
            statement.setString(12, truncate(record.getGhiChu(), 400));
            return;
        }
        statement.setString(1, record.getNhomBang());
        setNullableString(statement, 2, record.getMaChienDich());
        setNullableString(statement, 3, record.getMaLienKet());
        statement.setString(4, requiredText(record.getTieuDe(), record.getNhomBang()));
        statement.setString(5, truncate(record.getNoiDung(), 1000));
        statement.setDate(6, sqlDateOrToday(record.getNgayTao()));
        setNullableDate(statement, 7, sqlDate(record.getNgayXuLy()));
        statement.setString(8, requiredText(record.getTrangThai(), "Chờ duyệt"));
        setNullableString(statement, 9, record.getNguoiTao());
        setNullableString(statement, 10, record.getNguoiXuLy());
        statement.setString(11, truncate(record.getGhiChu(), 400));
        statement.setString(12, record.getMaChinh());
    }

    private static void setContentFields(PreparedStatement statement, SystemRecord record, boolean insert)
            throws SQLException {
        String note = contentNote(record);
        if (insert) {
            statement.setString(1, requiredText(record.getNhomBang(), "ThongBao"));
            statement.setString(2, record.getMaChinh());
            setNullableString(statement, 3, record.getMaLienKet());
            statement.setString(4, requiredText(record.getTieuDe(), record.getNhomBang()));
            statement.setString(5, truncate(record.getNoiDung(), 1200));
            statement.setDate(6, sqlDateOrToday(record.getNgayTao()));
            statement.setString(7, requiredText(record.getTrangThai(), "Chưa đọc"));
            statement.setString(8, note);
            return;
        }
        statement.setString(1, requiredText(record.getNhomBang(), "ThongBao"));
        setNullableString(statement, 2, record.getMaLienKet());
        statement.setString(3, requiredText(record.getTieuDe(), record.getNhomBang()));
        statement.setString(4, truncate(record.getNoiDung(), 1200));
        statement.setDate(5, sqlDateOrToday(record.getNgayTao()));
        statement.setString(6, requiredText(record.getTrangThai(), "Chưa đọc"));
        statement.setString(7, note);
        statement.setString(8, record.getMaChinh());
    }

    private static String contentNote(SystemRecord record) {
        StringBuilder meta = new StringBuilder(APP_META_PREFIX);
        appendMeta(meta, "CAMPAIGN", record.getMaChienDich());
        appendMeta(meta, "AUTHOR", record.getNguoiTao());
        appendMeta(meta, "TARGET", record.getNguoiXuLy());
        meta.append(']');
        String note = stripMetadata(record.getGhiChu());
        String value = note.isBlank() ? meta.toString() : meta + " " + note;
        return truncate(value, 400);
    }

    private static void appendMeta(StringBuilder builder, String key, String value) {
        if (!isBlank(value)) {
            builder.append(' ').append(key).append('=').append(value.trim().replace(' ', '_'));
        }
    }

    private static Date sqlDate(String value) {
        LocalDate date = localDate(value);
        return date == null ? null : Date.valueOf(date);
    }

    private static Date sqlDateOrToday(String value) {
        Date date = sqlDate(value);
        return date == null ? Date.valueOf(LocalDate.now()) : date;
    }

    private static LocalDate localDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
            DISPLAY_DATE, SHORT_DATE, DateTimeFormatter.ISO_LOCAL_DATE
        }) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ex) {
                // Thu tiep dinh dang khac.
            }
        }
        return null;
    }

    private static void setNullableDate(PreparedStatement statement, int index, Date value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, value);
        }
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (isBlank(value)) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

    private static void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.NUMERIC);
        } else {
            statement.setDouble(index, value);
        }
    }

    private static Double score(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            double score = Double.parseDouble(value.trim().replace(',', '.'));
            return Math.max(0, Math.min(10, score));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String profileId(ParticipantModel participant) {
        if (!isBlank(participant.getMaHoSo())) {
            return truncate(participant.getMaHoSo(), 20);
        }
        for (UserAccount account : AppData.getAccounts()) {
            if (account.getUsername().equalsIgnoreCase(participant.getMaTaiKhoan())
                    && !isBlank(account.getLinkedId())) {
                return truncate(account.getLinkedId(), 20);
            }
        }
        return truncate("HS" + participant.getMaTaiKhoan(), 20);
    }

    private static String studentCode(ParticipantModel participant) {
        String code = participant.getMssv();
        if (isBlank(code)) {
            return truncate("99" + Math.abs(participant.getMaTaiKhoan().hashCode()), 20);
        }
        return truncate(code.trim(), 20);
    }

    private static String emailFromStudentCode(String accountId, String studentCode) {
        if (studentCode != null && studentCode.toLowerCase(Locale.ROOT).endsWith("@gmail.com")) {
            return studentCode;
        }
        return accountId == null ? null : accountId.toLowerCase(Locale.ROOT) + "@gmail.com";
    }

    private static String school(String value) {
        if (value == null) {
            return "UIT";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return SCHOOLS.contains(normalized) ? normalized : "UIT";
    }

    private static String phone(String value) {
        return BusinessRules.isPhone(value) ? value.trim() : "0900000000";
    }

    private static String gmailOrNull(String value) {
        return BusinessRules.isGmail(value) ? value.trim() : null;
    }

    private static String sponsorshipType(SponsorModel sponsor) {
        String field = safeLower(sponsor.getLinhVuc());
        if (field.contains("vật phẩm") || field.contains("vat pham")) {
            return "Vật phẩm";
        }
        if (field.contains("vật tư") || field.contains("vat tu")) {
            return "Vật tư";
        }
        if (field.contains("dịch vụ") || field.contains("dich vu")) {
            return "Dịch vụ";
        }
        return sponsor.getGiaTriTaiTro() > 0 ? "Tiền mặt" : "Dịch vụ";
    }

    private static String moneyMethod(String type) {
        String value = safeLower(type);
        return value.contains("chuyển khoản") || value.contains("chuyen khoan") ? "Chuyển khoản" : "Tiền mặt";
    }

    private static boolean isItemDonation(DonationModel donation) {
        if (donation.getSoTien() <= 0) {
            return true;
        }
        String type = safeLower(donation.getHinhThuc());
        return type.contains("vật") || type.contains("vat");
    }

    private static String accountForDonor(String donorName) {
        if (isBlank(donorName)) {
            return null;
        }
        for (UserAccount account : AppData.getAccounts()) {
            if (account.getDisplayName().equalsIgnoreCase(donorName)
                    || account.getUsername().equalsIgnoreCase(donorName)) {
                return account.getUsername();
            }
        }
        return null;
    }

    private static String allowed(String value, Set<String> allowedValues, String fallback) {
        if (allowedValues.contains(value)) {
            return value;
        }
        return fallback;
    }

    private static String requiredText(String value, String fallback) {
        String text = text(value);
        return text.isBlank() ? fallback : text;
    }

    private static String text(String value) {
        return value == null ? "" : UiText.clean(value).trim();
    }

    private static String fallback(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private static String truncate(String value, int maxLength) {
        String text = text(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void runAsync(String label, SqlWork work) {
        DB_WRITER.submit(() -> {
            try {
                work.run();
            } catch (SQLException ex) {
                System.err.println("Không thể " + label + " vào Oracle (" + DatabaseConfig.connectionLabel() + "): " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.err.println("Lỗi khi " + label + " vào Oracle: " + ex.getMessage());
            }
        });
    }

    @FunctionalInterface
    private interface SqlWork {

        void run() throws SQLException;
    }
}
