# CharityManagement Oracle Database

Chạy trong Command Prompt hoặc PowerShell:

```bat
CD /D "C:\Users\Thanh Canh\Documents\NetBeansProjects\CharityManagement\database"
sqlplus / as sysdba @oracle_setup.sql
```

Thông tin kết nối JavaFX/NetBeans:

```text
Username: CHARITY
Password: charity123
Host: localhost
Port: 1522
Service name: XE
JDBC URL: jdbc:oracle:thin:@//localhost:1522/XE
```

Thứ tự file:

```text
01_CREATE_TABLES.sql      Bảng, khóa chính, khóa ngoại, CHECK, UNIQUE, SEQUENCE
02_FUNCTIONS.sql          Function kiểm tra, thống kê, tính tổng
03_TRIGGERS.sql           Trigger validate, log, tồn kho, thanh toán, thông báo
04_PROCEDURES.sql         Stored procedure cho các giao tác nghiệp vụ
05_SEED_DATA.sql          Dữ liệu mẫu năm 2026
06_VIEWS_FOR_JAVA.sql     View đồng bộ schema chi tiết với JavaFX
oracle_preview.sql        In nhanh toàn bộ database để kiểm tra
```

Sau khi tạo xong, xem database:

```bat
sqlplus CHARITY/charity123@//localhost:1522/XE @oracle_preview.sql
```
