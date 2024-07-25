--
-- v2_4_feat_ui_custom_command
-- 创建人 liuyao
SET SESSION innodb_lock_wait_timeout = 7200;

ALTER TABLE `ui_scenario` ADD COLUMN `command_view_struct` LONGTEXT NULL COMMENT 'Command debug data struct' AFTER `schedule`;

SET SESSION innodb_lock_wait_timeout = DEFAULT;