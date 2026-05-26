package com.mycompany.charitymanagement;

public class SponsorModel {

    private String maDoiTac;
    private String tenDoiTac;
    private String linhVuc;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String maChienDich;
    private double giaTriTaiTro;
    private String ngayKyKet;

    public SponsorModel(String maNhaTaiTro, String tenNhaTaiTro, String soDienThoai, String hinhThuc, double soTien) {
        this(maNhaTaiTro, tenNhaTaiTro, hinhThuc, soDienThoai, "", "", "", soTien, "");
    }

    public SponsorModel(String maDoiTac, String tenDoiTac, String linhVuc, String soDienThoai, String email,
            String diaChi, String maChienDich, double giaTriTaiTro, String ngayKyKet) {
        this.maDoiTac = UiText.clean(maDoiTac);
        this.tenDoiTac = UiText.clean(tenDoiTac);
        this.linhVuc = UiText.clean(linhVuc);
        this.soDienThoai = UiText.clean(soDienThoai);
        this.email = UiText.clean(email);
        this.diaChi = UiText.clean(diaChi);
        this.maChienDich = UiText.clean(maChienDich);
        this.giaTriTaiTro = giaTriTaiTro;
        this.ngayKyKet = UiText.clean(ngayKyKet);
    }

    public String getMaDoiTac() {
        return maDoiTac;
    }

    public void setMaDoiTac(String maDoiTac) {
        this.maDoiTac = UiText.clean(maDoiTac);
    }

    public String getTenDoiTac() {
        return tenDoiTac;
    }

    public void setTenDoiTac(String tenDoiTac) {
        this.tenDoiTac = UiText.clean(tenDoiTac);
    }

    public String getLinhVuc() {
        return linhVuc;
    }

    public void setLinhVuc(String linhVuc) {
        this.linhVuc = UiText.clean(linhVuc);
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = UiText.clean(soDienThoai);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = UiText.clean(email);
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = UiText.clean(diaChi);
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = UiText.clean(maChienDich);
    }

    public double getGiaTriTaiTro() {
        return giaTriTaiTro;
    }

    public void setGiaTriTaiTro(double giaTriTaiTro) {
        this.giaTriTaiTro = giaTriTaiTro;
    }

    public String getGiaTriTaiTroText() {
        return FormatUtils.money(giaTriTaiTro);
    }

    public String getNgayKyKet() {
        return ngayKyKet;
    }

    public void setNgayKyKet(String ngayKyKet) {
        this.ngayKyKet = UiText.clean(ngayKyKet);
    }

    public String getMaNhaTaiTro() {
        return maDoiTac;
    }

    public void setMaNhaTaiTro(String maNhaTaiTro) {
        this.maDoiTac = UiText.clean(maNhaTaiTro);
    }

    public String getTenNhaTaiTro() {
        return tenDoiTac;
    }

    public void setTenNhaTaiTro(String tenNhaTaiTro) {
        this.tenDoiTac = UiText.clean(tenNhaTaiTro);
    }

    public String getHinhThuc() {
        return linhVuc;
    }

    public void setHinhThuc(String hinhThuc) {
        this.linhVuc = UiText.clean(hinhThuc);
    }

    public double getSoTien() {
        return giaTriTaiTro;
    }

    public void setSoTien(double soTien) {
        this.giaTriTaiTro = soTien;
    }

    public String getSoTienText() {
        return FormatUtils.money(giaTriTaiTro);
    }

    public String getTenChienDich() {
        ActivityModel campaign = AppData.findCampaign(maChienDich);
        return campaign == null ? maChienDich : campaign.getTenChienDich();
    }
}
