package com.mycompany.charitymanagement;

public class Expense {

    private String maChiPhi;
    private String maChienDich;
    private String moTa;
    private double soTien;
    private String danhMuc;
    private String nguoiDeXuat;
    private String nguoiDuyet;
    private String trangThai;
    private String ngayDeXuat;
    private String ngayDuyet;
    private String ghiChu;

    public Expense(String maChiPhi, String maChienDich, String moTa, double soTien,
            String danhMuc, String nguoiDeXuat, String nguoiDuyet, String trangThai,
            String ngayDeXuat, String ngayDuyet, String ghiChu) {
        this.maChiPhi = maChiPhi;
        this.maChienDich = maChienDich;
        this.moTa = moTa;
        this.soTien = soTien;
        this.danhMuc = danhMuc;
        this.nguoiDeXuat = nguoiDeXuat;
        this.nguoiDuyet = nguoiDuyet;
        this.trangThai = trangThai;
        this.ngayDeXuat = ngayDeXuat;
        this.ngayDuyet = ngayDuyet;
        this.ghiChu = ghiChu;
    }

    public String getMaChiPhi() { return maChiPhi; }
    public void setMaChiPhi(String v) { this.maChiPhi = v; }
    public String getMaChienDich() { return maChienDich; }
    public void setMaChienDich(String v) { this.maChienDich = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { this.moTa = v; }
    public double getSoTien() { return soTien; }
    public void setSoTien(double v) { this.soTien = v; }
    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String v) { this.danhMuc = v; }
    public String getNguoiDeXuat() { return nguoiDeXuat; }
    public void setNguoiDeXuat(String v) { this.nguoiDeXuat = v; }
    public String getNguoiDuyet() { return nguoiDuyet; }
    public void setNguoiDuyet(String v) { this.nguoiDuyet = v; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String v) { this.trangThai = v; }
    public String getNgayDeXuat() { return ngayDeXuat; }
    public void setNgayDeXuat(String v) { this.ngayDeXuat = v; }
    public String getNgayDuyet() { return ngayDuyet; }
    public void setNgayDuyet(String v) { this.ngayDuyet = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }

    public String getSoTienText() { return FormatUtils.money(soTien); }
    public String getTenChienDich() {
        ActivityModel c = AppData.findCampaign(maChienDich);
        return c == null ? maChienDich : c.getTenChienDich();
    }
}
