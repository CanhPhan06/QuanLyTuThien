set pagesize 200
set linesize 220
column table_name format a28
column object_type format a16
column object_name format a38
column status format a10
column ten_dang_nhap format a14
column vai_tro format a12
column ho_ten_hien_thi format a28
column ma_chien_dich format a14
column ten_chien_dich format a34
column trang_thai format a22
column ho_ten format a24
column truong format a10
column ten_doi_tac format a24
column nguoi_quyen_gop format a24
column hinh_thuc format a18

prompt ===== TABLES =====
select table_name from user_tables order by table_name;

prompt ===== ROW COUNTS =====
select 'TAI_KHOAN' table_name, count(*) rows_count from tai_khoan
union all select 'CHIEN_DICH', count(*) from chien_dich
union all select 'HO_SO_TNV', count(*) from ho_so_tnv
union all select 'DOI_TAC_TAI_TRO', count(*) from doi_tac_tai_tro
union all select 'QUYEN_GOP', count(*) from quyen_gop
union all select 'VAN_HANH', count(*) from van_hanh
union all select 'NOI_DUNG', count(*) from noi_dung;

prompt ===== BUSINESS OBJECTS =====
select object_type, object_name, status
from user_objects
where object_type in ('TRIGGER', 'PROCEDURE', 'FUNCTION', 'SEQUENCE')
order by object_type, object_name;

prompt ===== SAMPLE ACCOUNTS =====
select ten_dang_nhap, vai_tro, ho_ten_hien_thi, ma_lien_ket
from tai_khoan
order by ten_dang_nhap
fetch first 8 rows only;

prompt ===== SAMPLE CAMPAIGNS =====
select ma_chien_dich, ten_chien_dich, muc_tieu_tien, trang_thai,
       fn_tong_nguon_luc_chien_dich(ma_chien_dich) tong_da_ghi_nhan,
       fn_so_tnv_chien_dich(ma_chien_dich) so_tnv
from chien_dich
order by ma_chien_dich;

prompt ===== SAMPLE VOLUNTEERS =====
select ma_tai_khoan, ho_ten, mssv, so_dien_thoai, truong, ma_chien_dich, trang_thai_duyet
from ho_so_tnv
order by ma_tai_khoan
fetch first 8 rows only;

prompt ===== SAMPLE SPONSORS =====
select ma_doi_tac, ten_doi_tac, so_dien_thoai, email, ma_chien_dich, gia_tri_tai_tro
from doi_tac_tai_tro
order by ma_doi_tac;

prompt ===== SAMPLE DONATIONS =====
select ma_quyen_gop, nguoi_quyen_gop, ma_chien_dich, hinh_thuc, so_tien
from quyen_gop
order by ma_quyen_gop;

exit
