create sequence seq_cd start with 4 increment by 1;
create sequence seq_hs start with 5 increment by 1;
create sequence seq_dt start with 4 increment by 1;
create sequence seq_qg start with 4 increment by 1;
create sequence seq_vh start with 6 increment by 1;
create sequence seq_nd start with 6 increment by 1;

create or replace function fn_next_id(p_prefix varchar2, p_seq number)
return varchar2
is
begin
    return p_prefix || lpad(p_seq, 3, '0');
end;
/

create or replace function fn_chien_dich_ton_tai(p_ma_chien_dich varchar2)
return number
is
    v_count number;
begin
    select count(*) into v_count
    from chien_dich
    where ma_chien_dich = p_ma_chien_dich;

    return case when v_count > 0 then 1 else 0 end;
end;
/

create or replace function fn_tai_khoan_hop_le(p_ten_dang_nhap varchar2, p_mat_khau varchar2)
return number
is
    v_count number;
begin
    select count(*) into v_count
    from tai_khoan
    where upper(ten_dang_nhap) = upper(p_ten_dang_nhap)
      and mat_khau = p_mat_khau
      and trang_thai = 'ACTIVE';

    return case when v_count > 0 then 1 else 0 end;
end;
/

create or replace function fn_tong_quyen_gop_tien(p_ma_chien_dich varchar2)
return number
is
    v_total number;
begin
    select nvl(sum(so_tien), 0) into v_total
    from quyen_gop
    where ma_chien_dich = p_ma_chien_dich;

    return v_total;
end;
/

create or replace function fn_tong_tai_tro(p_ma_chien_dich varchar2)
return number
is
    v_total number;
begin
    select nvl(sum(gia_tri_tai_tro), 0) into v_total
    from doi_tac_tai_tro
    where ma_chien_dich = p_ma_chien_dich;

    return v_total;
end;
/

create or replace function fn_tong_nguon_luc_chien_dich(p_ma_chien_dich varchar2)
return number
is
begin
    return fn_tong_quyen_gop_tien(p_ma_chien_dich) + fn_tong_tai_tro(p_ma_chien_dich);
end;
/

create or replace function fn_so_tnv_chien_dich(p_ma_chien_dich varchar2)
return number
is
    v_total number;
begin
    select count(*) into v_total
    from ho_so_tnv
    where ma_chien_dich = p_ma_chien_dich;

    return v_total;
end;
/

create or replace function fn_so_tnv_da_duyet(p_ma_chien_dich varchar2)
return number
is
    v_total number;
begin
    select count(*) into v_total
    from ho_so_tnv
    where ma_chien_dich = p_ma_chien_dich
      and trang_thai_duyet = N'Đã duyệt';

    return v_total;
end;
/

create or replace function fn_ty_le_dat_muc_tieu(p_ma_chien_dich varchar2)
return number
is
    v_goal number;
begin
    select nvl(muc_tieu_tien, 0) into v_goal
    from chien_dich
    where ma_chien_dich = p_ma_chien_dich;

    if v_goal <= 0 then
        return 0;
    end if;

    return round(fn_tong_nguon_luc_chien_dich(p_ma_chien_dich) / v_goal * 100, 2);
exception
    when no_data_found then
        return 0;
end;
/

create or replace function fn_so_van_hanh_cho_xu_ly
return number
is
    v_total number;
begin
    select count(*) into v_total
    from van_hanh
    where trang_thai in (N'Chờ duyệt', N'Đang xét', N'Đang phân công', N'Chờ xác nhận');

    return v_total;
end;
/

create or replace function fn_so_noi_dung_can_duyet
return number
is
    v_total number;
begin
    select count(*) into v_total
    from noi_dung
    where trang_thai in (N'Chờ duyệt', N'Nháp', N'Ẩn');

    return v_total;
end;
/

create or replace function fn_trang_thai_tong_hop_cd(p_ma_chien_dich varchar2)
return nvarchar2
is
    v_percent number;
    v_status nvarchar2(50);
begin
    select trang_thai into v_status
    from chien_dich
    where ma_chien_dich = p_ma_chien_dich;

    v_percent := fn_ty_le_dat_muc_tieu(p_ma_chien_dich);

    if v_status = N'Hoàn thành' or v_percent >= 100 then
        return N'Đạt mục tiêu';
    elsif v_status = N'Đang thực hiện' then
        return N'Đang vận động';
    elsif v_status = N'Đang xét' then
        return N'Chờ phản hồi';
    else
        return v_status;
    end if;
exception
    when no_data_found then
        return N'Không tồn tại';
end;
/

create or replace trigger trg_tai_khoan_validate
before insert or update on tai_khoan
for each row
begin
    :new.ten_dang_nhap := upper(trim(:new.ten_dang_nhap));

    if :new.vai_tro not in ('ADMIN', 'VOLUNTEER', 'SPONSOR') then
        raise_application_error(-20001, 'Vai tro tai khoan khong hop le.');
    end if;

    if :new.trang_thai not in ('ACTIVE', 'LOCKED') then
        raise_application_error(-20002, 'Trang thai tai khoan khong hop le.');
    end if;
end;
/

create or replace trigger trg_chien_dich_validate
before insert or update on chien_dich
for each row
begin
    if :new.ma_chien_dich is null then
        :new.ma_chien_dich := fn_next_id('CD', seq_cd.nextval);
    end if;

    if nvl(:new.muc_tieu_tien, 0) < 0 then
        raise_application_error(-20003, 'Muc tieu tien khong duoc am.');
    end if;

    if :new.trang_thai not in (N'Chờ duyệt', N'Đang xét', N'Đã duyệt', N'Đang thực hiện', N'Hoàn thành', N'Tạm dừng') then
        raise_application_error(-20004, 'Trang thai chien dich khong hop le.');
    end if;

    if :new.ngay_bat_dau is not null and :new.ngay_ket_thuc is not null
       and to_date(:new.ngay_ket_thuc, 'DD/MM/YYYY') < to_date(:new.ngay_bat_dau, 'DD/MM/YYYY') then
        raise_application_error(-20005, 'Ngay ket thuc phai lon hon hoac bang ngay bat dau.');
    end if;
end;
/

create or replace trigger trg_ho_so_tnv_validate
before insert or update on ho_so_tnv
for each row
begin
    if :new.ma_ho_so is null then
        :new.ma_ho_so := fn_next_id('HS', seq_hs.nextval);
    end if;

    if fn_chien_dich_ton_tai(:new.ma_chien_dich) = 0 then
        raise_application_error(-20006, 'Chien dich cua tinh nguyen vien khong ton tai.');
    end if;

    if not regexp_like(:new.so_dien_thoai, '^09[0-9]{8}$') then
        raise_application_error(-20007, 'So dien thoai TNV phai co dang 09xxxxxxxx.');
    end if;

    if :new.truong not in ('UIT', 'UEL', 'HCMUS', 'HCMUT', 'HCMIU', 'UHS', 'HCMUSSH') then
        raise_application_error(-20008, 'Truong phai thuoc DHQG-TPHCM.');
    end if;

    if :new.trang_thai_duyet not in (N'Chờ duyệt', N'Đang xét', N'Đã duyệt', N'Từ chối') then
        raise_application_error(-20009, 'Trang thai TNV khong hop le.');
    end if;
end;
/

create or replace trigger trg_doi_tac_validate
before insert or update on doi_tac_tai_tro
for each row
begin
    if :new.ma_doi_tac is null then
        :new.ma_doi_tac := fn_next_id('DT', seq_dt.nextval);
    end if;

    if fn_chien_dich_ton_tai(:new.ma_chien_dich) = 0 then
        raise_application_error(-20010, 'Chien dich tai tro khong ton tai.');
    end if;

    if not regexp_like(:new.so_dien_thoai, '^09[0-9]{8}$') then
        raise_application_error(-20011, 'So dien thoai nha tai tro phai co dang 09xxxxxxxx.');
    end if;

    if :new.email is not null and not regexp_like(:new.email, '^[A-Za-z0-9._%+-]+@gmail\.com$') then
        raise_application_error(-20012, 'Email nha tai tro phai co dang @gmail.com.');
    end if;

    if nvl(:new.gia_tri_tai_tro, 0) < 0 then
        raise_application_error(-20013, 'Gia tri tai tro khong duoc am.');
    end if;
end;
/

create or replace trigger trg_quyen_gop_validate
before insert or update on quyen_gop
for each row
begin
    if :new.ma_quyen_gop is null then
        :new.ma_quyen_gop := fn_next_id('QG', seq_qg.nextval);
    end if;

    if fn_chien_dich_ton_tai(:new.ma_chien_dich) = 0 then
        raise_application_error(-20014, 'Chien dich quyen gop khong ton tai.');
    end if;

    if :new.hinh_thuc not in (N'Tiền mặt', N'Chuyển khoản', N'Vật phẩm', N'Vật tư') then
        raise_application_error(-20015, 'Hinh thuc quyen gop khong hop le.');
    end if;

    if nvl(:new.so_tien, 0) < 0 then
        raise_application_error(-20016, 'So tien quyen gop khong duoc am.');
    end if;

    if :new.hinh_thuc in (N'Tiền mặt', N'Chuyển khoản') and nvl(:new.so_tien, 0) <= 0 then
        raise_application_error(-20017, 'Quyen gop bang tien phai co so tien lon hon 0.');
    end if;
end;
/

create or replace trigger trg_van_hanh_validate
before insert or update on van_hanh
for each row
begin
    if :new.ma_chinh is null then
        :new.ma_chinh := fn_next_id('VH', seq_vh.nextval);
    end if;

    if :new.ma_chien_dich is not null and fn_chien_dich_ton_tai(:new.ma_chien_dich) = 0 then
        raise_application_error(-20018, 'Chien dich van hanh khong ton tai.');
    end if;

    if :new.nhom_bang = N'Điểm danh' and :new.trang_thai not in (N'Chờ duyệt', N'Có mặt', N'Vắng mặt') then
        raise_application_error(-20019, 'Trang thai diem danh khong hop le.');
    elsif :new.nhom_bang = N'Công việc' and :new.trang_thai not in (N'Đang phân công', N'Đã phân công', N'Hoàn thành') then
        raise_application_error(-20020, 'Trang thai cong viec khong hop le.');
    elsif :new.nhom_bang = N'Quyên góp' and :new.trang_thai not in (N'Chờ xác nhận', N'Đã xác nhận', N'Từ chối') then
        raise_application_error(-20021, 'Trang thai quyen gop khong hop le.');
    elsif :new.nhom_bang = N'Chiến dịch' and :new.trang_thai not in (N'Chờ duyệt', N'Đang xét', N'Đã duyệt', N'Từ chối') then
        raise_application_error(-20022, 'Trang thai duyet chien dich khong hop le.');
    end if;

    if :new.trang_thai in (N'Đã duyệt', N'Đã phân công', N'Có mặt', N'Đã xác nhận', N'Đã xuất')
       and (:new.ngay_xu_ly is null or trim(:new.ngay_xu_ly) is null) then
        :new.ngay_xu_ly := to_char(sysdate, 'DD/MM/YYYY');
    end if;
end;
/

create or replace trigger trg_noi_dung_validate
before insert or update on noi_dung
for each row
begin
    if :new.ma_chinh is null then
        :new.ma_chinh := fn_next_id('ND', seq_nd.nextval);
    end if;

    if :new.nhom_bang not in ('TinTuc', 'BinhLuan', 'ThongBao', 'NhatKyHeThong', 'ThamSo') then
        raise_application_error(-20023, 'Loai noi dung khong hop le.');
    end if;

    if :new.trang_thai not in (N'Nháp', N'Chờ duyệt', N'Đã đăng', N'Hiển thị', N'Chưa đọc', N'Đã ghi', N'Đang dùng', N'Ẩn') then
        raise_application_error(-20024, 'Trang thai noi dung khong hop le.');
    end if;
end;
/

create or replace trigger trg_quyen_gop_tao_van_hanh
after insert on quyen_gop
for each row
begin
    insert into van_hanh (
        nhom_bang, ma_chinh, ma_chien_dich, ma_lien_ket, tieu_de, noi_dung,
        ngay_tao, ngay_xu_ly, trang_thai, nguoi_tao, nguoi_xu_ly, ghi_chu
    ) values (
        N'Quyên góp',
        fn_next_id('VH', seq_vh.nextval),
        :new.ma_chien_dich,
        :new.ma_quyen_gop,
        N'Xác nhận quyên góp',
        :new.nguoi_quyen_gop || N' gửi ' || :new.hinh_thuc,
        nvl(:new.ngay_quyen_gop, to_char(sysdate, 'DD/MM/YYYY')),
        null,
        N'Chờ xác nhận',
        substr(:new.nguoi_quyen_gop, 1, 30),
        'ADMIN001',
        N'Tự động tạo từ bảng QuyenGop'
    );
end;
/

create or replace trigger trg_tnv_tao_van_hanh
after insert on ho_so_tnv
for each row
begin
    insert into van_hanh (
        nhom_bang, ma_chinh, ma_chien_dich, ma_lien_ket, tieu_de, noi_dung,
        ngay_tao, ngay_xu_ly, trang_thai, nguoi_tao, nguoi_xu_ly, ghi_chu
    ) values (
        N'Đăng ký TNV',
        fn_next_id('VH', seq_vh.nextval),
        :new.ma_chien_dich,
        :new.ma_tai_khoan,
        N'Duyệt đăng ký TNV',
        :new.ho_ten || N' đăng ký tham gia chiến dịch',
        to_char(sysdate, 'DD/MM/YYYY'),
        null,
        N'Chờ duyệt',
        :new.ma_tai_khoan,
        'ADMIN001',
        N'Tự động tạo từ bảng HoSoTNV'
    );
end;
/

create or replace procedure sp_tao_chien_dich(
    p_ten_chien_dich nvarchar2,
    p_mo_ta nvarchar2,
    p_dia_diem nvarchar2,
    p_ngay_bat_dau varchar2,
    p_ngay_ket_thuc varchar2,
    p_muc_tieu_tien number,
    p_ma_nguoi_tao varchar2
)
is
    v_ma varchar2(20);
begin
    v_ma := fn_next_id('CD', seq_cd.nextval);

    insert into chien_dich values (
        v_ma, p_ten_chien_dich, p_mo_ta, p_dia_diem, p_ngay_bat_dau,
        p_ngay_ket_thuc, p_muc_tieu_tien, N'Chờ duyệt', p_ma_nguoi_tao
    );

    insert into van_hanh values (
        N'Chiến dịch', fn_next_id('VH', seq_vh.nextval), v_ma, v_ma,
        N'Duyệt chiến dịch', N'Chiến dịch mới cần quản lý duyệt',
        to_char(sysdate, 'DD/MM/YYYY'), null, N'Chờ duyệt',
        p_ma_nguoi_tao, 'ADMIN001', N'Tạo từ procedure sp_tao_chien_dich'
    );
end;
/

create or replace procedure sp_dang_ky_tnv(
    p_ma_tai_khoan varchar2,
    p_ho_ten nvarchar2,
    p_mssv varchar2,
    p_so_dien_thoai varchar2,
    p_khoa nvarchar2,
    p_truong varchar2,
    p_ma_chien_dich varchar2
)
is
begin
    insert into ho_so_tnv values (
        p_ma_tai_khoan, fn_next_id('HS', seq_hs.nextval), p_ho_ten, p_mssv,
        p_so_dien_thoai, p_khoa, p_truong, p_ma_chien_dich, N'Chờ duyệt', null
    );
end;
/

create or replace procedure sp_duyet_tnv(
    p_ma_tai_khoan varchar2,
    p_ma_chien_dich varchar2,
    p_nguoi_duyet varchar2,
    p_diem_danh_gia varchar2
)
is
begin
    update ho_so_tnv
    set trang_thai_duyet = N'Đã duyệt',
        diem_danh_gia = p_diem_danh_gia
    where ma_tai_khoan = p_ma_tai_khoan
      and ma_chien_dich = p_ma_chien_dich;

    update van_hanh
    set trang_thai = N'Đã duyệt',
        ngay_xu_ly = to_char(sysdate, 'DD/MM/YYYY'),
        nguoi_xu_ly = p_nguoi_duyet
    where nhom_bang = N'Đăng ký TNV'
      and ma_lien_ket = p_ma_tai_khoan
      and ma_chien_dich = p_ma_chien_dich;
end;
/

create or replace procedure sp_ghi_nhan_tai_tro(
    p_ten_doi_tac nvarchar2,
    p_linh_vuc nvarchar2,
    p_so_dien_thoai varchar2,
    p_email varchar2,
    p_dia_chi nvarchar2,
    p_ma_chien_dich varchar2,
    p_gia_tri_tai_tro number
)
is
begin
    insert into doi_tac_tai_tro values (
        fn_next_id('DT', seq_dt.nextval), p_ten_doi_tac, p_linh_vuc,
        p_so_dien_thoai, p_email, p_dia_chi, p_ma_chien_dich,
        p_gia_tri_tai_tro, to_char(sysdate, 'DD/MM/YYYY')
    );
end;
/

create or replace procedure sp_ghi_nhan_quyen_gop(
    p_nguoi_quyen_gop nvarchar2,
    p_ma_chien_dich varchar2,
    p_hinh_thuc nvarchar2,
    p_noi_dung_quyen_gop nvarchar2,
    p_so_tien number
)
is
begin
    insert into quyen_gop values (
        fn_next_id('QG', seq_qg.nextval), p_nguoi_quyen_gop, p_ma_chien_dich,
        to_char(sysdate, 'DD/MM/YYYY'), p_hinh_thuc, p_noi_dung_quyen_gop, p_so_tien
    );
end;
/

create or replace procedure sp_cap_nhat_trang_thai_van_hanh(
    p_ma_chinh varchar2,
    p_trang_thai nvarchar2,
    p_nguoi_xu_ly varchar2,
    p_ghi_chu nvarchar2
)
is
begin
    update van_hanh
    set trang_thai = p_trang_thai,
        nguoi_xu_ly = p_nguoi_xu_ly,
        ngay_xu_ly = case
            when p_trang_thai in (N'Đã duyệt', N'Đã phân công', N'Có mặt', N'Đã xác nhận', N'Đã xuất')
            then to_char(sysdate, 'DD/MM/YYYY')
            else ngay_xu_ly
        end,
        ghi_chu = p_ghi_chu
    where ma_chinh = p_ma_chinh;
end;
/

create or replace procedure sp_tao_noi_dung(
    p_nhom_bang varchar2,
    p_ma_lien_ket varchar2,
    p_tieu_de nvarchar2,
    p_noi_dung nvarchar2,
    p_trang_thai nvarchar2,
    p_ghi_chu nvarchar2
)
is
begin
    insert into noi_dung values (
        p_nhom_bang, fn_next_id('ND', seq_nd.nextval), p_ma_lien_ket,
        p_tieu_de, p_noi_dung, to_char(sysdate, 'DD/MM/YYYY'), p_trang_thai, p_ghi_chu
    );
end;
/

create or replace procedure sp_khoa_tai_khoan(
    p_ten_dang_nhap varchar2,
    p_nguoi_thuc_hien varchar2,
    p_ly_do nvarchar2
)
is
begin
    update tai_khoan
    set trang_thai = 'LOCKED'
    where upper(ten_dang_nhap) = upper(p_ten_dang_nhap);

    insert into noi_dung values (
        'NhatKyHeThong', fn_next_id('ND', seq_nd.nextval), p_ten_dang_nhap,
        N'Khóa tài khoản', p_ly_do, to_char(sysdate, 'DD/MM/YYYY'),
        N'Đã ghi', p_nguoi_thuc_hien
    );
end;
/
