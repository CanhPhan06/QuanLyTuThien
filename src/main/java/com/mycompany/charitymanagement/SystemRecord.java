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
        this.nhomBang = UiText.clean(nhomBang);
        this.maChinh = UiText.clean(maChinh);
        this.maChienDich = UiText.clean(maChienDich);
        this.maLienKet = UiText.clean(maLienKet);
        this.tieuDe = UiText.clean(tieuDe);
        this.noiDung = UiText.clean(noiDung);
        this.ngay = UiText.clean(ngay);
        this.ngayXuLy = UiText.clean(ngayXuLy);
        this.trangThai = UiText.clean(trangThai);
        this.nguoiTao = UiText.clean(nguoiTao);
        this.nguoiXuLy = UiText.clean(nguoiXuLy);
        this.ghiChu = UiText.clean(ghiChu);
    }

    private static String inferCampaignId(String value) {
        return value != null && value.toUpperCase().startsWith("CD") ? value : "";
    }

    public String getNhomBang() {
        return nhomBang;
    }

    public void setNhomBang(String nhomBang) {
        this.nhomBang = UiText.clean(nhomBang);
    }

    public String getLoaiNghiepVu() {
        return nhomBang;
    }

    public String getTenNhomBang() {
        String value = nhomBang == null ? "" : nhomBang.toLowerCase();
        if (value.contains("tintuc")) {
            return "Tin tức";
        }
        if (value.contains("binhluan")) {
            return "Bình luận";
        }
        if (value.contains("thongbao")) {
            return "Thông báo";
        }
        if (value.contains("nhatky")) {
            return "Nhật ký";
        }
        if (value.contains("thamso")) {
            return "Tham số";
        }
        return nhomBang;
    }

    public String getMaChinh() {
        return maChinh;
    }

    public void setMaChinh(String maChinh) {
        this.maChinh = UiText.clean(maChinh);
    }

    public String getMaVanHanh() {
        return maChinh;
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = UiText.clean(maChienDich);
    }

    public String getMaLienKet() {
        return maLienKet;
    }

    public void setMaLienKet(String maLienKet) {
        this.maLienKet = UiText.clean(maLienKet);
    }

    public String getDoiTuongLienKet() {
        return maLienKet;
    }

    public String getTenLienKet() {
        ActivityModel campaign = AppData.findCampaign(maLienKet);
        if (campaign != null) {
            return campaign.getTenChienDich();
        }
        for (UserAccount account : AppData.getAccounts()) {
            if (account.getUsername().equalsIgnoreCase(maLienKet)) {
                return account.getDisplayName();
            }
        }
        return maLienKet;
    }

    public String getTenChienDich() {
        ActivityModel campaign = AppData.findCampaign(maChienDich);
        return campaign == null ? maChienDich : campaign.getTenChienDich();
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = UiText.clean(tieuDe);
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = UiText.clean(noiDung);
    }

    public String getNgay() {
        return ngay;
    }

    public void setNgay(String ngay) {
        this.ngay = UiText.clean(ngay);
    }

    public String getNgayTao() {
        return ngay;
    }

    public String getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(String ngayXuLy) {
        this.ngayXuLy = UiText.clean(ngayXuLy);
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = UiText.clean(trangThai);
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = UiText.clean(nguoiTao);
    }

    public String getNguoiXuLy() {
        return nguoiXuLy;
    }

    public void setNguoiXuLy(String nguoiXuLy) {
        this.nguoiXuLy = UiText.clean(nguoiXuLy);
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = UiText.clean(ghiChu);
    }
}
