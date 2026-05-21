package com.mycompany.charitymanagement;

public class ParticipantModel {

    private String maTaiKhoan;
    private String maHoSo;
    private String hoTen;
    private String mssv;
    private String soDienThoai;
    private String khoa;
    private String truong;
    private String maChienDich;
    private String trangThaiDuyet;
    private String diemDanhGia;
    private String ketQuaVangGia;
    private String ngayVangGia;
    private String ngayPhongVan;
    private String ketQuaPhongVan;
    private String screeningNote;

    public ParticipantModel(String maNguoiThamGia, String hoTen, String soDienThoai, String email, String hoatDong) {
        this(maNguoiThamGia, "HS" + maNguoiThamGia, hoTen, email, soDienThoai, "", "", hoatDong, "Chờ duyệt", "");
    }

    public ParticipantModel(String maTaiKhoan, String maHoSo, String hoTen, String mssv, String soDienThoai,
            String khoa, String truong, String maChienDich, String trangThaiDuyet, String diemDanhGia) {
        this.maTaiKhoan = maTaiKhoan;
        this.maHoSo = maHoSo;
        this.hoTen = hoTen;
        this.mssv = mssv;
        this.soDienThoai = soDienThoai;
        this.khoa = khoa;
        this.truong = truong;
        this.maChienDich = maChienDich;
        this.trangThaiDuyet = trangThaiDuyet;
        this.diemDanhGia = diemDanhGia;
        this.ketQuaVangGia = "";
        this.ngayVangGia = "";
        this.ngayPhongVan = "";
        this.ketQuaPhongVan = "";
        this.screeningNote = "";
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        this.maHoSo = maHoSo;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public String getTruong() {
        return truong;
    }

    public void setTruong(String truong) {
        this.truong = truong;
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = maChienDich;
    }

    public String getTrangThaiDuyet() {
        return trangThaiDuyet;
    }

    public void setTrangThaiDuyet(String trangThaiDuyet) {
        this.trangThaiDuyet = trangThaiDuyet;
    }

    public String getDiemDanhGia() {
        return diemDanhGia;
    }

    public void setDiemDanhGia(String diemDanhGia) {
        this.diemDanhGia = diemDanhGia;
    }

    public String getKetQuaVangGia() {
        return ketQuaVangGia;
    }

    public void setKetQuaVangGia(String ketQuaVangGia) {
        this.ketQuaVangGia = ketQuaVangGia;
    }

    public String getNgayVangGia() {
        return ngayVangGia;
    }

    public void setNgayVangGia(String ngayVangGia) {
        this.ngayVangGia = ngayVangGia;
    }

    public String getNgayPhongVan() {
        return ngayPhongVan;
    }

    public void setNgayPhongVan(String ngayPhongVan) {
        this.ngayPhongVan = ngayPhongVan;
    }

    public String getKetQuaPhongVan() {
        return ketQuaPhongVan;
    }

    public void setKetQuaPhongVan(String ketQuaPhongVan) {
        this.ketQuaPhongVan = ketQuaPhongVan;
    }

    public String getScreeningNote() {
        return screeningNote;
    }

    public void setScreeningNote(String screeningNote) {
        this.screeningNote = screeningNote;
    }

    public String getMaNguoiThamGia() {
        return maTaiKhoan;
    }

    public void setMaNguoiThamGia(String maNguoiThamGia) {
        this.maTaiKhoan = maNguoiThamGia;
    }

    public String getEmail() {
        return mssv;
    }

    public void setEmail(String email) {
        this.mssv = email;
    }

    public String getHoatDong() {
        return maChienDich;
    }

    public void setHoatDong(String hoatDong) {
        this.maChienDich = hoatDong;
    }

    public String getTenChienDich() {
        ActivityModel campaign = AppData.findCampaign(maChienDich);
        return campaign == null ? maChienDich : campaign.getTenChienDich();
    }
}
