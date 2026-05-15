package com.mycompany.charitymanagement;

public class DonationModel {

    private String maQuyenGop;
    private String nguoiQuyenGop;
    private String hoatDong;
    private String ngayQuyenGop;
    private String noiDungQuyenGop;
    private double soTien;
    private String hinhThuc;

    public DonationModel(String maQuyenGop, String nguoiQuyenGop, String hoatDong, String ngayQuyenGop, double soTien, String hinhThuc) {
        this(maQuyenGop, nguoiQuyenGop, hoatDong, ngayQuyenGop, hinhThuc, "Tiền mặt", soTien);
    }

    public DonationModel(String maQuyenGop, String nguoiQuyenGop, String hoatDong, String ngayQuyenGop, String hinhThuc, String noiDungQuyenGop, double soTien) {
        this.maQuyenGop = maQuyenGop;
        this.nguoiQuyenGop = nguoiQuyenGop;
        this.hoatDong = hoatDong;
        this.ngayQuyenGop = ngayQuyenGop;
        this.noiDungQuyenGop = noiDungQuyenGop;
        this.soTien = soTien;
        this.hinhThuc = hinhThuc;
    }

    public String getMaQuyenGop() {
        return maQuyenGop;
    }

    public void setMaQuyenGop(String maQuyenGop) {
        this.maQuyenGop = maQuyenGop;
    }

    public String getNguoiQuyenGop() {
        return nguoiQuyenGop;
    }

    public void setNguoiQuyenGop(String nguoiQuyenGop) {
        this.nguoiQuyenGop = nguoiQuyenGop;
    }

    public String getHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(String hoatDong) {
        this.hoatDong = hoatDong;
    }

    public String getNgayQuyenGop() {
        return ngayQuyenGop;
    }

    public void setNgayQuyenGop(String ngayQuyenGop) {
        this.ngayQuyenGop = ngayQuyenGop;
    }

    public String getNoiDungQuyenGop() {
        return noiDungQuyenGop;
    }

    public void setNoiDungQuyenGop(String noiDungQuyenGop) {
        this.noiDungQuyenGop = noiDungQuyenGop;
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
        this.hinhThuc = hinhThuc;
    }

    public String getSoTienText() {
        return FormatUtils.money(soTien);
    }
}
