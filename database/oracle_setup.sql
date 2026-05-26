-- ============================================================
-- ORACLE SETUP MASTER SCRIPT
-- Project: CharityManagement
--
-- Cach chay khuyen nghi:
--   CD /D C:\Users\Thanh Canh\Documents\NetBeansProjects\CharityManagement\database
--   SQLPLUS / AS SYSDBA @oracle_setup.sql
--
-- Script nay tao lai schema CHARITY, tao bang chi tiet, function,
-- trigger, procedure, du lieu mau va view dong bo voi JavaFX.
-- ============================================================

SET DEFINE OFF
SET SQLBLANKLINES ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

ALTER SESSION SET "_ORACLE_SCRIPT" = TRUE;

BEGIN
    FOR R IN (
        SELECT SID, SERIAL#
        FROM V$SESSION
        WHERE USERNAME = 'CHARITY'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER SYSTEM KILL SESSION ''' || R.SID || ',' || R.SERIAL# || ''' IMMEDIATE';
    END LOOP;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP USER CHARITY CASCADE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1918 THEN
            RAISE;
        END IF;
END;
/

CREATE USER CHARITY IDENTIFIED BY charity123;
GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE TO CHARITY;
ALTER USER CHARITY QUOTA UNLIMITED ON USERS;

CONNECT CHARITY/charity123

ALTER SESSION SET NLS_DATE_FORMAT = 'DD/MM/YYYY';
ALTER SESSION SET NLS_NUMERIC_CHARACTERS = '.,';

PROMPT ===== 01_CREATE_TABLES.sql =====
@@01_CREATE_TABLES.sql

PROMPT ===== 02_FUNCTIONS.sql =====
@@02_FUNCTIONS.sql

PROMPT ===== 03_TRIGGERS.sql =====
@@03_TRIGGERS.sql

PROMPT ===== 04_PROCEDURES.sql =====
@@04_PROCEDURES.sql

PROMPT ===== 05_SEED_DATA.sql =====
@@05_SEED_DATA.sql

PROMPT ===== 06_VIEWS_FOR_JAVA.sql =====
@@06_VIEWS_FOR_JAVA.sql

PROMPT ===== 07_FIX_VIETNAMESE_ENCODING.sql =====
@@07_FIX_VIETNAMESE_ENCODING.sql

COMMIT;

PROMPT ============================================================
PROMPT CHARITY schema da duoc tao day du bang, rang buoc, trigger,
PROMPT stored procedure, function, du lieu mau va view cho JavaFX.
PROMPT ============================================================

EXIT
