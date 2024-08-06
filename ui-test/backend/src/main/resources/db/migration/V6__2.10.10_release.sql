--
-- 创建人 ycr
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE api_execution_queue
    ADD ui_driver VARCHAR(50) NULL;

SET SESSION innodb_lock_wait_timeout = DEFAULT;