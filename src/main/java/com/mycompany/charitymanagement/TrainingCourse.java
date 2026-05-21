package com.mycompany.charitymanagement;

public class TrainingCourse {

    private String maKhoaHoc;
    private String tenKhoaHoc;
    private String moTa;
    private String ngayBatDau;
    private String ngayKetThuc;
    private int soGio;
    private String giangVien;
    private String maChienDich;
    private int siSoToiDa;
    private int siSoHienTai;
    private String trangThai;

    public TrainingCourse(String maKhoaHoc, String tenKhoaHoc, String moTa, String ngayBatDau,
            String ngayKetThuc, int soGio, String giangVien, String maChienDich,
            int siSoToiDa, int siSoHienTai, String trangThai) {
        this.maKhoaHoc = maKhoaHoc;
        this.tenKhoaHoc = tenKhoaHoc;
        this.moTa = moTa;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.soGio = soGio;
        this.giangVien = giangVien;
        this.maChienDich = maChienDich;
        this.siSoToiDa = siSoToiDa;
        this.siSoHienTai = siSoHienTai;
        this.trangThai = trangThai;
    }

    public String getMaKhoaHoc() { return maKhoaHoc; }
    public void setMaKhoaHoc(String v) { this.maKhoaHoc = v; }
    public String getTenKhoaHoc() { return tenKhoaHoc; }
    public void setTenKhoaHoc(String v) { this.tenKhoaHoc = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { this.moTa = v; }
    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String v) { this.ngayBatDau = v; }
    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String v) { this.ngayKetThuc = v; }
    public int getSoGio() { return soGio; }
    public void setSoGio(int v) { this.soGio = v; }
    public String getGiangVien() { return giangVien; }
    public void setGiangVien(String v) { this.giangVien = v; }
    public String getMaChienDich() { return maChienDich; }
    public void setMaChienDich(String v) { this.maChienDich = v; }
    public int getSiSoToiDa() { return siSoToiDa; }
    public void setSiSoToiDa(int v) { this.siSoToiDa = v; }
    public int getSiSoHienTai() { return siSoHienTai; }
    public void setSiSoHienTai(int v) { this.siSoHienTai = v; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String v) { this.trangThai = v; }

    public String getTenChienDich() {
        ActivityModel c = AppData.findCampaign(maChienDich);
        return c == null ? maChienDich : c.getTenChienDich();
    }

    public String getTienDoText() {
        if (siSoToiDa <= 0) return "0/" + siSoToiDa;
        return siSoHienTai + "/" + siSoToiDa;
    }
}
