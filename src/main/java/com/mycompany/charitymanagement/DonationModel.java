package com.mycompany.charitymanagement;

public class DonationModel {

    private String maQuyenGop;
    private String nguoiQuyenGop;
    private String hoatDong;
    private String ngayQuyenGop;
    private String noiDungQuyenGop;
    private double soTien;
    private String hinhThuc;

    public DonationModel(String maQuyenGop, String nguoiQuyenGop, String hoatDong, String ngayQuyenGop,
            double soTien, String hinhThuc) {
        this(maQuyenGop, nguoiQuyenGop, hoatDong, ngayQuyenGop, hinhThuc, "Tiền mặt", soTien);
    }

    public DonationModel(String maQuyenGop, String nguoiQuyenGop, String hoatDong, String ngayQuyenGop,
            String hinhThuc, String noiDungQuyenGop, double soTien) {
        this.maQuyenGop = UiText.clean(maQuyenGop);
        this.nguoiQuyenGop = UiText.clean(nguoiQuyenGop);
        this.hoatDong = UiText.clean(hoatDong);
        this.ngayQuyenGop = UiText.clean(ngayQuyenGop);
        this.noiDungQuyenGop = UiText.clean(noiDungQuyenGop);
        this.soTien = soTien;
        this.hinhThuc = UiText.clean(hinhThuc);
    }

    public String getMaQuyenGop() {
        return maQuyenGop;
    }

    public void setMaQuyenGop(String maQuyenGop) {
        this.maQuyenGop = UiText.clean(maQuyenGop);
    }

    public String getNguoiQuyenGop() {
        return nguoiQuyenGop;
    }

    public void setNguoiQuyenGop(String nguoiQuyenGop) {
        this.nguoiQuyenGop = UiText.clean(nguoiQuyenGop);
    }

    public String getHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(String hoatDong) {
        this.hoatDong = UiText.clean(hoatDong);
    }

    public String getNgayQuyenGop() {
        return ngayQuyenGop;
    }

    public void setNgayQuyenGop(String ngayQuyenGop) {
        this.ngayQuyenGop = UiText.clean(ngayQuyenGop);
    }

    public String getNoiDungQuyenGop() {
        return noiDungQuyenGop;
    }

    public void setNoiDungQuyenGop(String noiDungQuyenGop) {
        this.noiDungQuyenGop = UiText.clean(noiDungQuyenGop);
    }

    public double getSoTien() {
        return soTien;
    }

    public void setSoTien(double soTien) {
        this.soTien = soTien;
    }

    public String getHinhThuc() {
        return hinhThuc;
    }

    public void setHinhThuc(String hinhThuc) {
        this.hinhThuc = UiText.clean(hinhThuc);
    }

    public String getSoTienText() {
        return FormatUtils.money(soTien);
    }

    public String getTenChienDich() {
        ActivityModel campaign = AppData.findCampaign(hoatDong);
        return campaign == null ? hoatDong : campaign.getTenChienDich();
    }

    public String getTrangThaiXuLy() {
        return AppData.getOperations().stream()
                .filter(record -> "Quyên góp".equals(record.getNhomBang())
                && record.getMaLienKet().equalsIgnoreCase(maQuyenGop))
                .map(SystemRecord::getTrangThai)
                .findFirst()
                .orElse("Đã ghi nhận");
    }
}
