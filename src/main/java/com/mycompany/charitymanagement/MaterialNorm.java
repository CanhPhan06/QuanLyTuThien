package com.mycompany.charitymanagement;

public class MaterialNorm {

    private String maDinhMuc;
    private String tenSanPham;
    private String maVatTu;
    private double soLuong;
    private String donVi;
    private String maChienDich;

    public MaterialNorm(String maDinhMuc, String tenSanPham, String maVatTu, double soLuong, String donVi, String maChienDich) {
        this.maDinhMuc = maDinhMuc;
        this.tenSanPham = tenSanPham;
        this.maVatTu = maVatTu;
        this.soLuong = soLuong;
        this.donVi = donVi;
        this.maChienDich = maChienDich;
    }

    public String getMaDinhMuc() { return maDinhMuc; }
    public void setMaDinhMuc(String v) { this.maDinhMuc = v; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String v) { this.tenSanPham = v; }
    public String getMaVatTu() { return maVatTu; }
    public void setMaVatTu(String v) { this.maVatTu = v; }
    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double v) { this.soLuong = v; }
    public String getDonVi() { return donVi; }
    public void setDonVi(String v) { this.donVi = v; }
    public String getMaChienDich() { return maChienDich; }
    public void setMaChienDich(String v) { this.maChienDich = v; }

    public String getTenVatTu() {
        for (InventoryItem item : AppData.getInventoryItems()) {
            if (item.getMaVatTu().equalsIgnoreCase(maVatTu)) return item.getTenVatTu();
        }
        return maVatTu;
    }

    public String getTenChienDich() {
        ActivityModel c = AppData.findCampaign(maChienDich);
        return c == null ? maChienDich : c.getTenChienDich();
    }
}
