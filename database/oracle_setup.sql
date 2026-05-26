set define off
whenever sqlerror continue

alter session set "_ORACLE_SCRIPT"=true;
drop user CHARITY cascade;

whenever sqlerror exit sql.sqlcode

create user CHARITY identified by charity123;
grant connect, resource, create session, create table, create sequence to CHARITY;
alter user CHARITY quota unlimited on users;

conn CHARITY/charity123

create table tai_khoan (
    ten_dang_nhap varchar2(30) primary key,
    mat_khau varchar2(100) not null,
    vai_tro varchar2(20) not null,
    trang_thai varchar2(20) default 'ACTIVE' not null,
    ho_ten_hien_thi nvarchar2(120) not null,
    ma_lien_ket varchar2(30)
);

create table chien_dich (
    ma_chien_dich varchar2(20) primary key,
    ten_chien_dich nvarchar2(200) not null,
    mo_ta nvarchar2(1000),
    dia_diem nvarchar2(120),
    ngay_bat_dau varchar2(20),
    ngay_ket_thuc varchar2(20),
    muc_tieu_tien number(15,2) default 0,
    trang_thai nvarchar2(50),
    ma_nguoi_tao varchar2(30)
);

create table ho_so_tnv (
    ma_tai_khoan varchar2(30),
    ma_ho_so varchar2(30),
    ho_ten nvarchar2(120) not null,
    mssv varchar2(20),
    so_dien_thoai varchar2(20),
    khoa nvarchar2(160),
    truong varchar2(30),
    ma_chien_dich varchar2(20),
    trang_thai_duyet nvarchar2(50),
    diem_danh_gia varchar2(20),
    constraint pk_ho_so_tnv primary key (ma_tai_khoan, ma_chien_dich)
);

create table doi_tac_tai_tro (
    ma_doi_tac varchar2(30) primary key,
    ten_doi_tac nvarchar2(160) not null,
    linh_vuc nvarchar2(80),
    so_dien_thoai varchar2(20),
    email varchar2(120),
    dia_chi nvarchar2(180),
    ma_chien_dich varchar2(20),
    gia_tri_tai_tro number(15,2) default 0,
    ngay_ky_ket varchar2(20)
);

create table quyen_gop (
    ma_quyen_gop varchar2(30) primary key,
    nguoi_quyen_gop nvarchar2(160) not null,
    ma_chien_dich varchar2(20),
    ngay_quyen_gop varchar2(20),
    hinh_thuc nvarchar2(80),
    noi_dung_quyen_gop nvarchar2(500),
    so_tien number(15,2) default 0
);

create table van_hanh (
    nhom_bang nvarchar2(80),
    ma_chinh varchar2(30) primary key,
    ma_chien_dich varchar2(20),
    ma_lien_ket varchar2(30),
    tieu_de nvarchar2(200),
    noi_dung nvarchar2(1000),
    ngay_tao varchar2(20),
    ngay_xu_ly varchar2(20),
    trang_thai nvarchar2(80),
    nguoi_tao varchar2(30),
    nguoi_xu_ly varchar2(30),
    ghi_chu nvarchar2(300)
);

create table noi_dung (
    nhom_bang varchar2(50),
    ma_chinh varchar2(30) primary key,
    ma_lien_ket varchar2(30),
    tieu_de nvarchar2(200),
    noi_dung nvarchar2(1000),
    ngay_tao varchar2(20),
    trang_thai nvarchar2(80),
    ghi_chu nvarchar2(300)
);

insert into tai_khoan values ('ADMIN', '123', 'ADMIN', 'ACTIVE', N'Quản lý hệ thống', 'TK000');
insert into tai_khoan values ('ADMIN001', '123', 'ADMIN', 'ACTIVE', N'Nguyễn Quản Trị', 'TK001');
insert into tai_khoan values ('ADMIN002', '123', 'ADMIN', 'ACTIVE', N'Trần Minh Quân', 'TK002');
insert into tai_khoan values ('ADMIN003', '123', 'ADMIN', 'ACTIVE', N'Lê Hoài An', 'TK003');
insert into tai_khoan values ('TNV001', '123', 'VOLUNTEER', 'ACTIVE', N'Nguyễn Văn An', 'HS001');
insert into tai_khoan values ('TNV002', '123', 'VOLUNTEER', 'ACTIVE', N'Trần Thị Bình', 'HS002');
insert into tai_khoan values ('TNV003', '123', 'VOLUNTEER', 'ACTIVE', N'Lê Minh Châu', 'HS003');
insert into tai_khoan values ('TNV004', '123', 'VOLUNTEER', 'ACTIVE', N'Phạm Tuấn Dũng', 'HS004');
insert into tai_khoan values ('NTT001', '123', 'SPONSOR', 'ACTIVE', N'Công ty An Phát', 'DT001');
insert into tai_khoan values ('NTT002', '123', 'SPONSOR', 'ACTIVE', N'Quỹ Thiện Tâm', 'DT002');
insert into tai_khoan values ('NTT003', '123', 'SPONSOR', 'ACTIVE', N'Công ty Bình Minh', 'DT003');

insert into chien_dich values ('CD001', N'Đông ấm cho em 2026', N'Hỗ trợ áo ấm, sách vở và quà cho học sinh vùng cao', N'Hà Giang', '01/12/2026', '20/12/2026', 50000000, N'Đang thực hiện', 'ADMIN001');
insert into chien_dich values ('CD002', N'Tiếp sức đến trường 2026', N'Trao học bổng, sách vở và dụng cụ học tập cho học sinh khó khăn', N'TP.HCM', '05/09/2026', '30/09/2026', 30000000, N'Đã duyệt', 'ADMIN002');
insert into chien_dich values ('CD003', N'Khám bệnh thiện nguyện 2026', N'Tổ chức khám bệnh và phát thuốc miễn phí', N'Long An', '20/05/2026', '25/05/2026', 80000000, N'Đang xét', 'ADMIN003');

insert into ho_so_tnv values ('TNV001', 'HS001', N'Nguyễn Văn An', '23520001', '0910000001', N'Khoa Công nghệ phần mềm', 'UIT', 'CD001', N'Đã duyệt', '8.5');
insert into ho_so_tnv values ('TNV002', 'HS002', N'Trần Thị Bình', '23520002', '0910000002', N'Khoa Kinh tế', 'UEL', 'CD002', N'Chờ duyệt', '');
insert into ho_so_tnv values ('TNV003', 'HS003', N'Lê Minh Châu', '23520003', '0910000003', N'Khoa Công nghệ thông tin', 'HCMUS', 'CD003', N'Chờ duyệt', '');
insert into ho_so_tnv values ('TNV004', 'HS004', N'Phạm Tuấn Dũng', '23520004', '0910000004', N'Khoa Kỹ thuật xây dựng', 'HCMUT', 'CD001', N'Đã duyệt', '9.0');

insert into doi_tac_tai_tro values ('DT001', N'Công ty An Phát', N'Giáo dục', '0920000001', 'ntt001@gmail.com', N'TP.HCM', 'CD001', 6000000, '01/04/2026');
insert into doi_tac_tai_tro values ('DT002', N'Quỹ Thiện Tâm', N'Cộng đồng', '0920000002', 'ntt002@gmail.com', N'Hà Nội', 'CD002', 7000000, '02/04/2026');
insert into doi_tac_tai_tro values ('DT003', N'Công ty Bình Minh', N'Y tế', '0920000003', 'ntt003@gmail.com', N'TP.HCM', 'CD003', 8000000, '03/04/2026');

insert into quyen_gop values ('QG001', N'Công ty An Phát', 'CD001', '05/04/2026', N'Chuyển khoản', N'Tiền mặt hỗ trợ chương trình', 3000000);
insert into quyen_gop values ('QG002', N'Nguyễn Văn Bình', 'CD001', '10/04/2026', N'Vật phẩm', N'Sách vở và đồ dùng học tập', 0);
insert into quyen_gop values ('QG003', N'Quỹ Thiện Tâm', 'CD003', '12/04/2026', N'Vật tư', N'Thuốc, khẩu trang và dụng cụ y tế', 0);

insert into van_hanh values (N'Chiến dịch', 'VH001', 'CD001', 'CD001', N'Duyệt chiến dịch', N'Kiểm tra thông tin chiến dịch trước khi công bố', '02/04/2026', '02/04/2026', N'Đã duyệt', 'ADMIN001', 'ADMIN002', N'Bảng ChienDich/DuyetChienDich');
insert into van_hanh values (N'Đăng ký TNV', 'VH002', 'CD002', 'TNV002', N'Duyệt đăng ký TNV', N'Sinh viên UEL đăng ký tham gia chiến dịch CD002', '03/04/2026', '', N'Chờ duyệt', 'ADMIN001', 'ADMIN003', N'Bảng ThamGiaTNV');
insert into van_hanh values (N'Công việc', 'VH003', 'CD001', 'CV001', N'Đóng gói quà tặng', N'Cần 15 tình nguyện viên', '03/04/2026', '', N'Đang phân công', 'ADMIN002', 'ADMIN004', N'Bảng CongViec/PhanCong');
insert into van_hanh values (N'Quyên góp', 'VH004', 'CD001', 'QG001', N'Xác nhận quyên góp tiền', N'Đã đối soát giao dịch của Công ty An Phát', '05/04/2026', '05/04/2026', N'Đã xác nhận', 'NTT001', 'ADMIN001', N'Bảng QuyenGopTien/ThanhToan');
insert into van_hanh values (N'Quyên góp', 'VH005', 'CD001', 'QG002', N'Xác nhận quyên góp vật phẩm', N'Chờ kiểm đếm sách vở và đồ dùng học tập', '10/04/2026', '', N'Chờ xác nhận', 'TNV002', 'ADMIN002', N'Bảng PhieuQuyenGopVP/ChiTietQuyenGopVP');

insert into noi_dung values ('TinTuc', 'TT001', 'CD001', N'Cập nhật chiến dịch Đông ấm 2026', N'Bài đăng truyền thông chiến dịch', '07/04/2026', N'Đã đăng', N'Bảng TinTuc');
insert into noi_dung values ('BinhLuan', 'BL001', 'TT001', N'Bình luận của sinh viên', N'Mình muốn tham gia chương trình', '08/04/2026', N'Hiển thị', N'Bảng BinhLuan');
insert into noi_dung values ('ThongBao', 'TB001', 'TNV001', N'Thông báo duyệt tham gia', N'Bạn đã được duyệt tham gia CD001', '09/04/2026', N'Chưa đọc', N'Bảng ThongBao');
insert into noi_dung values ('NhatKyHeThong', 'NK001', 'ADMIN001', N'Tạo chiến dịch', N'Ghi nhận thao tác tạo CD001', '01/04/2026', N'Đã ghi', N'Bảng NhatKyHeThong');
insert into noi_dung values ('ThamSo', 'TS001', 'HE_THONG', N'Cấu hình quy đổi điểm', N'Điểm đánh giá mặc định cho TNV', '10/04/2026', N'Đang dùng', N'Bảng ThamSo');

commit;

prompt CHARITY schema created and seeded.
prompt Run oracle_business_logic.sql after this file to install triggers, procedures and functions.
exit
