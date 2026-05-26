package com.mycompany.charitymanagement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class DatabaseDataLoader {

    private DatabaseDataLoader() {
    }

    public static boolean loadIntoMemory() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            ObservableList<UserAccount> accounts = FXCollections.observableArrayList();
            ObservableList<ActivityModel> activities = FXCollections.observableArrayList();
            ObservableList<ParticipantModel> participants = FXCollections.observableArrayList();
            ObservableList<SponsorModel> sponsors = FXCollections.observableArrayList();
            ObservableList<DonationModel> donations = FXCollections.observableArrayList();
            ObservableList<SystemRecord> operations = FXCollections.observableArrayList();
            ObservableList<SystemRecord> contents = FXCollections.observableArrayList();

            loadAccounts(connection, accounts);
            loadCampaigns(connection, activities);
            loadParticipants(connection, participants);
            loadSponsors(connection, sponsors);
            loadDonations(connection, donations);
            loadOperations(connection, operations);
            loadContents(connection, contents);

            if (accounts.isEmpty() || activities.isEmpty()) {
                throw new SQLException("Database khong co du lieu tai_khoan hoac chien_dich.");
            }

            javafx.application.Platform.runLater(() -> {
                AppData.getAccounts().setAll(accounts);
                AppData.getActivities().setAll(activities);
                replaceWhenDatabaseHasData(AppData.getParticipants(), participants);
                replaceWhenDatabaseHasData(AppData.getSponsors(), sponsors);
                replaceWhenDatabaseHasData(AppData.getDonations(), donations);
                replaceWhenDatabaseHasData(AppData.getOperations(), operations);
                replaceWhenDatabaseHasData(AppData.getContents(), contents);
                AppData.ensureReportSampleData();
            });
            return true;
        } catch (SQLException ex) {
            System.err.println("Khong the nap du lieu Oracle (" + DatabaseConfig.connectionLabel() + "): " + ex.getMessage());
            return false;
        }
    }

    private static <T> void replaceWhenDatabaseHasData(ObservableList<T> target, ObservableList<T> source) {
        if (!source.isEmpty()) {
            target.setAll(source);
        }
    }

    private static void loadAccounts(Connection connection, ObservableList<UserAccount> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select ten_dang_nhap, mat_khau, vai_tro, ho_ten_hien_thi, ma_lien_ket "
                        + "from tai_khoan where trang_thai = 'ACTIVE' order by ten_dang_nhap")) {
            while (rs.next()) {
                target.add(new UserAccount(
                        text(rs, "ten_dang_nhap"),
                        text(rs, "mat_khau"),
                        text(rs, "vai_tro"),
                        text(rs, "ho_ten_hien_thi"),
                        text(rs, "ma_lien_ket")
                ));
            }
        }
    }

    private static void loadCampaigns(Connection connection, ObservableList<ActivityModel> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "SELECT ma_chien_dich, ten_chien_dich, mo_ta, dia_diem, "
                        + "TO_CHAR(ngay_bat_dau, 'DD/MM/YYYY') AS ngay_bat_dau, "
                        + "TO_CHAR(ngay_ket_thuc, 'DD/MM/YYYY') AS ngay_ket_thuc, "
                        + "muc_tieu_tien, trang_thai, ma_nguoi_tao "
                        + "FROM chien_dich ORDER BY ma_chien_dich")) {
            while (rs.next()) {
                target.add(new ActivityModel(
                        text(rs, "ma_chien_dich"),
                        text(rs, "ten_chien_dich"),
                        text(rs, "mo_ta"),
                        text(rs, "dia_diem"),
                        text(rs, "ngay_bat_dau"),
                        text(rs, "ngay_ket_thuc"),
                        rs.getDouble("muc_tieu_tien"),
                        text(rs, "trang_thai"),
                        text(rs, "ma_nguoi_tao")
                ));
            }
        }
    }

    private static void loadParticipants(Connection connection, ObservableList<ParticipantModel> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select ma_tai_khoan, ma_ho_so, ho_ten, mssv, so_dien_thoai, khoa, truong, "
                        + "ma_chien_dich, trang_thai_duyet, diem_danh_gia from ho_so_tnv order by ma_tai_khoan")) {
            while (rs.next()) {
                target.add(new ParticipantModel(
                        text(rs, "ma_tai_khoan"),
                        text(rs, "ma_ho_so"),
                        text(rs, "ho_ten"),
                        text(rs, "mssv"),
                        text(rs, "so_dien_thoai"),
                        text(rs, "khoa"),
                        text(rs, "truong"),
                        text(rs, "ma_chien_dich"),
                        text(rs, "trang_thai_duyet"),
                        text(rs, "diem_danh_gia")
                ));
            }
        }
    }

    private static void loadSponsors(Connection connection, ObservableList<SponsorModel> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select ma_doi_tac, ten_doi_tac, linh_vuc, so_dien_thoai, email, dia_chi, "
                        + "ma_chien_dich, gia_tri_tai_tro, ngay_ky_ket from doi_tac_tai_tro order by ma_doi_tac")) {
            while (rs.next()) {
                target.add(new SponsorModel(
                        text(rs, "ma_doi_tac"),
                        text(rs, "ten_doi_tac"),
                        text(rs, "linh_vuc"),
                        text(rs, "so_dien_thoai"),
                        text(rs, "email"),
                        text(rs, "dia_chi"),
                        text(rs, "ma_chien_dich"),
                        rs.getDouble("gia_tri_tai_tro"),
                        text(rs, "ngay_ky_ket")
                ));
            }
        }
    }

    private static void loadDonations(Connection connection, ObservableList<DonationModel> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select ma_quyen_gop, nguoi_quyen_gop, ma_chien_dich, ngay_quyen_gop, hinh_thuc, "
                        + "noi_dung_quyen_gop, so_tien from quyen_gop order by ma_quyen_gop")) {
            while (rs.next()) {
                target.add(new DonationModel(
                        text(rs, "ma_quyen_gop"),
                        text(rs, "nguoi_quyen_gop"),
                        text(rs, "ma_chien_dich"),
                        text(rs, "ngay_quyen_gop"),
                        text(rs, "hinh_thuc"),
                        text(rs, "noi_dung_quyen_gop"),
                        rs.getDouble("so_tien")
                ));
            }
        }
    }

    private static void loadOperations(Connection connection, ObservableList<SystemRecord> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "SELECT nhom_bang, ma_chinh, ma_chien_dich, ma_lien_ket, tieu_de, noi_dung, "
                        + "TO_CHAR(ngay_tao, 'DD/MM/YYYY') AS ngay_tao, "
                        + "TO_CHAR(ngay_xu_ly, 'DD/MM/YYYY') AS ngay_xu_ly, "
                        + "trang_thai, nguoi_tao, nguoi_xu_ly, ghi_chu "
                        + "FROM van_hanh ORDER BY ma_chinh")) {
            while (rs.next()) {
                target.add(new SystemRecord(
                        text(rs, "nhom_bang"),
                        text(rs, "ma_chinh"),
                        text(rs, "ma_chien_dich"),
                        text(rs, "ma_lien_ket"),
                        text(rs, "tieu_de"),
                        text(rs, "noi_dung"),
                        text(rs, "ngay_tao"),
                        text(rs, "ngay_xu_ly"),
                        text(rs, "trang_thai"),
                        text(rs, "nguoi_tao"),
                        text(rs, "nguoi_xu_ly"),
                        text(rs, "ghi_chu")
                ));
            }
        }
    }

    private static void loadContents(Connection connection, ObservableList<SystemRecord> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "SELECT nd.nhom_bang, nd.ma_chinh, "
                        + "CASE "
                        + "WHEN nd.nhom_bang = 'TinTuc' AND nd.ma_lien_ket LIKE 'CD%' THEN nd.ma_lien_ket "
                        + "WHEN nd.nhom_bang = 'BinhLuan' AND nd.ma_lien_ket LIKE 'TT%' THEN "
                        + "(SELECT MAX(tt.ma_chien_dich) FROM tin_tuc tt WHERE tt.ma_tin_tuc = nd.ma_lien_ket) "
                        + "ELSE NULL END AS ma_chien_dich, "
                        + "nd.ma_lien_ket, nd.tieu_de, nd.noi_dung, "
                        + "TO_CHAR(nd.ngay_tao, 'DD/MM/YYYY') AS ngay_tao, nd.trang_thai, nd.ghi_chu "
                        + "FROM noi_dung nd ORDER BY nd.ma_chinh")) {
            while (rs.next()) {
                target.add(new SystemRecord(
                        text(rs, "nhom_bang"),
                        text(rs, "ma_chinh"),
                        text(rs, "ma_chien_dich"),
                        text(rs, "ma_lien_ket"),
                        text(rs, "tieu_de"),
                        text(rs, "noi_dung"),
                        text(rs, "ngay_tao"),
                        "",
                        text(rs, "trang_thai"),
                        "ADMIN",
                        "",
                        text(rs, "ghi_chu")
                ));
            }
        }
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        return UiText.clean(rs.getString(column));
    }
}
