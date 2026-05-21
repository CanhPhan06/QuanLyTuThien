package com.mycompany.charitymanagement;

public class InventoryItem {

    private String maVatTu;
    private String tenVatTu;
    private String danhMuc;
    private String donViTinh;
    private int soLuongTon;
    private int soLuongToiThieu;
    private double donGia;
    private String maChienDich;
    private String ngayCapNhat;

    public InventoryItem(String maVatTu, String tenVatTu, String danhMuc, String donViTinh,
            int soLuongTon, int soLuongToiThieu, double donGia, String maChienDich, String ngayCapNhat) {
        this.maVatTu = maVatTu;
        this.tenVatTu = tenVatTu;
        this.danhMuc = danhMuc;
        this.donViTinh = donViTinh;
        this.soLuongTon = soLuongTon;
        this.soLuongToiThieu = soLuongToiThieu;
        this.donGia = donGia;
        this.maChienDich = maChienDich;
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getMaVatTu() { return maVatTu; }
    public void setMaVatTu(String v) { this.maVatTu = v; }
    public String getTenVatTu() { return tenVatTu; }
    public void setTenVatTu(String v) { this.tenVatTu = v; }
    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String v) { this.danhMuc = v; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String v) { this.donViTinh = v; }
    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int v) { this.soLuongTon = v; }
    public int getSoLuongToiThieu() { return soLuongToiThieu; }
    public void setSoLuongToiThieu(int v) { this.soLuongToiThieu = v; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double v) { this.donGia = v; }
    public String getMaChienDich() { return maChienDich; }
    public void setMaChienDich(String v) { this.maChienDich = v; }
    public String getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(String v) { this.ngayCapNhat = v; }

    public String getDonGiaText() { return FormatUtils.money(donGia); }
    public String getThanhTienText() { return FormatUtils.money(soLuongTon * donGia); }

    public String getTenChienDich() {
        ActivityModel c = AppData.findCampaign(maChienDich);
        return c == null ? maChienDich : c.getTenChienDich();
    }

    public boolean isLowStock() { return soLuongTon <= soLuongToiThieu; }
}
