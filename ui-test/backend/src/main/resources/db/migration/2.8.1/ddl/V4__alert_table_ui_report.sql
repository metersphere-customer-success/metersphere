SET SESSION innodb_lock_wait_timeout = 7200;


SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario_report'
                   AND index_name LIKE 'ui_scenario_report_update_time_index'),
          'select 1',
          'ALTER TABLE ui_scenario_report ADD INDEX ui_scenario_report_update_time_index (update_time)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario_report'
                   AND index_name LIKE 'ui_scenario_report_create_time_index'),
          'select 1',
          'ALTER TABLE ui_scenario_report ADD INDEX ui_scenario_report_create_time_index (create_time)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


SET SESSION innodb_lock_wait_timeout = DEFAULT;