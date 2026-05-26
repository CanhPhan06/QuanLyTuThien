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

    public ParticipantModel(String maNguoiThamGia, String hoTen, String soDienThoai, String email, String hoatDong) {
        this(maNguoiThamGia, "HS" + maNguoiThamGia, hoTen, email, soDienThoai, "", "", hoatDong, "Chờ duyệt", "");
    }

    public ParticipantModel(String maTaiKhoan, String maHoSo, String hoTen, String mssv, String soDienThoai,
            String khoa, String truong, String maChienDich, String trangThaiDuyet, String diemDanhGia) {
        this.maTaiKhoan = UiText.clean(maTaiKhoan);
        this.maHoSo = UiText.clean(maHoSo);
        this.hoTen = UiText.clean(hoTen);
        this.mssv = UiText.clean(mssv);
        this.soDienThoai = UiText.clean(soDienThoai);
        this.khoa = UiText.clean(khoa);
        this.truong = UiText.clean(truong);
        this.maChienDich = UiText.clean(maChienDich);
        this.trangThaiDuyet = UiText.clean(trangThaiDuyet);
        this.diemDanhGia = UiText.clean(diemDanhGia);
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = UiText.clean(maTaiKhoan);
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        this.maHoSo = UiText.clean(maHoSo);
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = UiText.clean(hoTen);
    }

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = UiText.clean(mssv);
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = UiText.clean(soDienThoai);
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = UiText.clean(khoa);
    }

    public String getTruong() {
        return truong;
    }

    public void setTruong(String truong) {
        this.truong = UiText.clean(truong);
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = UiText.clean(maChienDich);
    }

    public String getTrangThaiDuyet() {
        return trangThaiDuyet;
    }

    public void setTrangThaiDuyet(String trangThaiDuyet) {
        this.trangThaiDuyet = UiText.clean(trangThaiDuyet);
    }

    public String getDiemDanhGia() {
        return diemDanhGia;
    }

    public void setDiemDanhGia(String diemDanhGia) {
        this.diemDanhGia = UiText.clean(diemDanhGia);
    }

    public String getMaNguoiThamGia() {
        return maTaiKhoan;
    }

    public void setMaNguoiThamGia(String maNguoiThamGia) {
        this.maTaiKhoan = UiText.clean(maNguoiThamGia);
    }

    public String getEmail() {
        return mssv;
    }

    public void setEmail(String email) {
        this.mssv = UiText.clean(email);
    }

    public String getHoatDong() {
        return maChienDich;
    }

    public void setHoatDong(String hoatDong) {
        this.maChienDich = UiText.clean(hoatDong);
    }

    public String getTenChienDich() {
        ActivityModel campaign = AppData.findCampaign(maChienDich);
        return campaign == null ? maChienDich : campaign.getTenChienDich();
    }
}
