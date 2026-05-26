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

            AppData.getAccounts().setAll(accounts);
            AppData.getActivities().setAll(activities);
            AppData.getParticipants().setAll(participants);
            AppData.getSponsors().setAll(sponsors);
            AppData.getDonations().setAll(donations);
            AppData.getOperations().setAll(operations);
            AppData.getContents().setAll(contents);
            return true;
        } catch (SQLException ex) {
            System.err.println("Khong the nap du lieu Oracle (" + DatabaseConfig.connectionLabel() + "): " + ex.getMessage());
            return false;
        }
    }

    private static void loadAccounts(Connection connection, ObservableList<UserAccount> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select ten_dang_nhap, mat_khau, vai_tro, ho_ten_hien_thi, ma_lien_ket "
                        + "from tai_khoan where trang_thai = 'ACTIVE' order by ten_dang_nhap")) {
            while (rs.next()) {
                target.add(new UserAccount(
                        rs.getString("ten_dang_nhap"),
                        rs.getString("mat_khau"),
                        rs.getString("vai_tro"),
                        rs.getString("ho_ten_hien_thi"),
                        rs.getString("ma_lien_ket")
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
                        rs.getString("ma_chien_dich"),
                        rs.getString("ten_chien_dich"),
                        rs.getString("mo_ta"),
                        rs.getString("dia_diem"),
                        rs.getString("ngay_bat_dau"),
                        rs.getString("ngay_ket_thuc"),
                        rs.getDouble("muc_tieu_tien"),
                        rs.getString("trang_thai"),
                        rs.getString("ma_nguoi_tao")
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
                        rs.getString("ma_tai_khoan"),
                        rs.getString("ma_ho_so"),
                        rs.getString("ho_ten"),
                        rs.getString("mssv"),
                        rs.getString("so_dien_thoai"),
                        rs.getString("khoa"),
                        rs.getString("truong"),
                        rs.getString("ma_chien_dich"),
                        rs.getString("trang_thai_duyet"),
                        rs.getString("diem_danh_gia")
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
                        rs.getString("ma_doi_tac"),
                        rs.getString("ten_doi_tac"),
                        rs.getString("linh_vuc"),
                        rs.getString("so_dien_thoai"),
                        rs.getString("email"),
                        rs.getString("dia_chi"),
                        rs.getString("ma_chien_dich"),
                        rs.getDouble("gia_tri_tai_tro"),
                        rs.getString("ngay_ky_ket")
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
                        rs.getString("ma_quyen_gop"),
                        rs.getString("nguoi_quyen_gop"),
                        rs.getString("ma_chien_dich"),
                        rs.getString("ngay_quyen_gop"),
                        rs.getString("hinh_thuc"),
                        rs.getString("noi_dung_quyen_gop"),
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
                        rs.getString("nhom_bang"),
                        rs.getString("ma_chinh"),
                        rs.getString("ma_chien_dich"),
                        rs.getString("ma_lien_ket"),
                        rs.getString("tieu_de"),
                        rs.getString("noi_dung"),
                        rs.getString("ngay_tao"),
                        rs.getString("ngay_xu_ly"),
                        rs.getString("trang_thai"),
                        rs.getString("nguoi_tao"),
                        rs.getString("nguoi_xu_ly"),
                        rs.getString("ghi_chu")
                ));
            }
        }
    }

    private static void loadContents(Connection connection, ObservableList<SystemRecord> target) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "SELECT nhom_bang, ma_chinh, ma_lien_ket, tieu_de, noi_dung, "
                        + "TO_CHAR(ngay_tao, 'DD/MM/YYYY') AS ngay_tao, trang_thai, ghi_chu "
                        + "FROM noi_dung ORDER BY ma_chinh")) {
            while (rs.next()) {
                target.add(new SystemRecord(
                        rs.getString("nhom_bang"),
                        rs.getString("ma_chinh"),
                        rs.getString("ma_lien_ket"),
                        rs.getString("tieu_de"),
                        rs.getString("noi_dung"),
                        rs.getString("ngay_tao"),
                        rs.getString("trang_thai"),
                        rs.getString("ghi_chu")
                ));
            }
        }
    }
}
