package com.mycompany.charitymanagement;

public class TrainingEnrollment {

    private String maGhiDanh;
    private String maKhoaHoc;
    private String maTaiKhoan;
    private String hoTen;
    private String ngayThamGia;
    private String trangThai;
    private String ghiChu;

    public TrainingEnrollment(String maGhiDanh, String maKhoaHoc, String maTaiKhoan, String hoTen,
            String ngayThamGia, String trangThai, String ghiChu) {
        this.maGhiDanh = maGhiDanh;
        this.maKhoaHoc = maKhoaHoc;
        this.maTaiKhoan = maTaiKhoan;
        this.hoTen = hoTen;
        this.ngayThamGia = ngayThamGia;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaGhiDanh() { return maGhiDanh; }
    public void setMaGhiDanh(String v) { this.maGhiDanh = v; }
    public String getMaKhoaHoc() { return maKhoaHoc; }
    public void setMaKhoaHoc(String v) { this.maKhoaHoc = v; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String v) { this.maTaiKhoan = v; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String v) { this.hoTen = v; }
    public String getNgayThamGia() { return ngayThamGia; }
    public void setNgayThamGia(String v) { this.ngayThamGia = v; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String v) { this.trangThai = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }

    public String getTenKhoaHoc() {
        for (TrainingCourse c : AppData.getTrainingCourses()) {
            if (c.getMaKhoaHoc().equalsIgnoreCase(maKhoaHoc)) return c.getTenKhoaHoc();
        }
        return maKhoaHoc;
    }
}
