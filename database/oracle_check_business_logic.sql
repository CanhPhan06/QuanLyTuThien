set pagesize 100
set linesize 200
column object_type format a20
column object_name format a40
select object_type, object_name, status
from user_objects
where object_type in ('TRIGGER', 'PROCEDURE', 'FUNCTION', 'SEQUENCE')
order by object_type, object_name;
exit
