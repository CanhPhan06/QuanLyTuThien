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
        accounts.add(new UserAccount("ADMIN", "123", UserAccount.ROLE_ADMIN, "Quản lý hệ thống", "TK000"));
        Thread loader = new Thread(() -> DatabaseDataLoader.loadIntoMemory());
        loader.setDaemon(true);
        loader.start();
    }

    private AppData() {
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
