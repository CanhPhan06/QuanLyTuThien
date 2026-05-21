package com.mycompany.charitymanagement;

public class Alert {

    private String maCanhBao;
    private String tieuDe;
    private String noiDung;
    private String mucDo;
    private String loai;
    private String doiTuongLienQuan;
    private String ngayTao;
    private String hanXuLy;
    private String trangThai;
    private String nguoiPhuTrach;

    public Alert(String maCanhBao, String tieuDe, String noiDung, String mucDo, String loai,
            String doiTuongLienQuan, String ngayTao, String hanXuLy, String trangThai, String nguoiPhuTrach) {
        this.maCanhBao = maCanhBao;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.mucDo = mucDo;
        this.loai = loai;
        this.doiTuongLienQuan = doiTuongLienQuan;
        this.ngayTao = ngayTao;
        this.hanXuLy = hanXuLy;
        this.trangThai = trangThai;
        this.nguoiPhuTrach = nguoiPhuTrach;
    }

    public String getMaCanhBao() { return maCanhBao; }
    public void setMaCanhBao(String v) { this.maCanhBao = v; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String v) { this.tieuDe = v; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String v) { this.noiDung = v; }
    public String getMucDo() { return mucDo; }
    public void setMucDo(String v) { this.mucDo = v; }
    public String getLoai() { return loai; }
    public void setLoai(String v) { this.loai = v; }
    public String getDoiTuongLienQuan() { return doiTuongLienQuan; }
    public void setDoiTuongLienQuan(String v) { this.doiTuongLienQuan = v; }
    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String v) { this.ngayTao = v; }
    public String getHanXuLy() { return hanXuLy; }
    public void setHanXuLy(String v) { this.hanXuLy = v; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String v) { this.trangThai = v; }
    public String getNguoiPhuTrach() { return nguoiPhuTrach; }
    public void setNguoiPhuTrach(String v) { this.nguoiPhuTrach = v; }

    public boolean isUrgent() { return "Cao".equals(mucDo); }
    public boolean isOverdue() { return "Chưa xử lý".equals(trangThai) && hanXuLy != null && !hanXuLy.isEmpty(); }
}
