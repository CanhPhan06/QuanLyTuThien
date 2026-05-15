package com.mycompany.charitymanagement;

public class SystemRecord {

    private String nhomBang;
    private String maChinh;
    private String maChienDich;
    private String maLienKet;
    private String tieuDe;
    private String noiDung;
    private String ngay;
    private String ngayXuLy;
    private String trangThai;
    private String nguoiTao;
    private String nguoiXuLy;
    private String ghiChu;

    public SystemRecord(String nhomBang, String maChinh, String maLienKet, String tieuDe,
            String noiDung, String ngay, String trangThai, String ghiChu) {
        this(nhomBang, maChinh, inferCampaignId(maLienKet), maLienKet, tieuDe, noiDung,
                ngay, "", trangThai, "ADMIN", "", ghiChu);
    }

    public SystemRecord(String nhomBang, String maChinh, String maChienDich, String maLienKet,
            String tieuDe, String noiDung, String ngay, String ngayXuLy, String trangThai,
            String nguoiTao, String nguoiXuLy, String ghiChu) {
        this.nhomBang = nhomBang;
        this.maChinh = maChinh;
        this.maChienDich = maChienDich;
        this.maLienKet = maLienKet;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngay = ngay;
        this.ngayXuLy = ngayXuLy;
        this.trangThai = trangThai;
        this.nguoiTao = nguoiTao;
        this.nguoiXuLy = nguoiXuLy;
        this.ghiChu = ghiChu;
    }

    private static String inferCampaignId(String value) {
        return value != null && value.toUpperCase().startsWith("CD") ? value : "";
    }

    public String getNhomBang() {
        return nhomBang;
    }

    public void setNhomBang(String nhomBang) {
        this.nhomBang = nhomBang;
    }

    public String getLoaiNghiepVu() {
        return nhomBang;
    }

    public String getMaChinh() {
        return maChinh;
    }

    public void setMaChinh(String maChinh) {
        this.maChinh = maChinh;
    }

    public String getMaVanHanh() {
        return maChinh;
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = maChienDich;
    }

    public String getMaLienKet() {
        return maLienKet;
    }

    public void setMaLienKet(String maLienKet) {
        this.maLienKet = maLienKet;
    }

    public String getDoiTuongLienKet() {
        return maLienKet;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNgay() {
        return ngay;
    }

    public void setNgay(String ngay) {
        this.ngay = ngay;
    }

    public String getNgayTao() {
        return ngay;
    }

    public String getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(String ngayXuLy) {
        this.ngayXuLy = ngayXuLy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getNguoiXuLy() {
        return nguoiXuLy;
    }

    public void setNguoiXuLy(String nguoiXuLy) {
        this.nguoiXuLy = nguoiXuLy;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
