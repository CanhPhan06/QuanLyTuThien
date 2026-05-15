package com.mycompany.charitymanagement;

public class ActivityModel {

    private String maChienDich;
    private String tenChienDich;
    private String moTa;
    private String diaDiem;
    private String ngayBatDau;
    private String ngayKetThuc;
    private double mucTieuTien;
    private String trangThai;
    private String maNguoiTao;

    public ActivityModel(String maChienDich, String tenChienDich, String moTa, String ngayBatDau) {
        this(maChienDich, tenChienDich, moTa, "", ngayBatDau, "", 0, "Đang chuẩn bị", "");
    }

    public ActivityModel(String maChienDich, String tenChienDich, String moTa, String ngayBatDau,
            String ngayKetThuc, double mucTieuTien, String trangThai, String maNguoiTao) {
        this(maChienDich, tenChienDich, moTa, "", ngayBatDau, ngayKetThuc, mucTieuTien, trangThai, maNguoiTao);
    }

    public ActivityModel(String maChienDich, String tenChienDich, String moTa, String diaDiem,
            String ngayBatDau, String ngayKetThuc, double mucTieuTien, String trangThai, String maNguoiTao) {
        this.maChienDich = maChienDich;
        this.tenChienDich = tenChienDich;
        this.moTa = moTa;
        this.diaDiem = diaDiem;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.mucTieuTien = mucTieuTien;
        this.trangThai = trangThai;
        this.maNguoiTao = maNguoiTao;
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = maChienDich;
    }

    public String getTenChienDich() {
        return tenChienDich;
    }

    public void setTenChienDich(String tenChienDich) {
        this.tenChienDich = tenChienDich;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public String getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(String ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public String getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(String ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public double getMucTieuTien() {
        return mucTieuTien;
    }

    public void setMucTieuTien(double mucTieuTien) {
        this.mucTieuTien = mucTieuTien;
    }

    public String getMucTieuTienText() {
        return FormatUtils.money(mucTieuTien);
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaNguoiTao() {
        return maNguoiTao;
    }

    public void setMaNguoiTao(String maNguoiTao) {
        this.maNguoiTao = maNguoiTao;
    }

    public String getMaHoatDong() {
        return maChienDich;
    }

    public void setMaHoatDong(String maHoatDong) {
        this.maChienDich = maHoatDong;
    }

    public String getTenHoatDong() {
        return tenChienDich;
    }

    public void setTenHoatDong(String tenHoatDong) {
        this.tenChienDich = tenHoatDong;
    }
}
