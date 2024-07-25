SET SESSION innodb_lock_wait_timeout = 7200;


SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario_module'
                   AND index_name LIKE 'ui_scenario_module_name_index'),
          'select 1',
          'ALTER TABLE ui_scenario_module ADD INDEX ui_scenario_module_name_index (name)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario_module'
                   AND index_name LIKE 'ui_scenario_module_scenario_type_index'),
          'select 1',
          'ALTER TABLE ui_scenario_module ADD INDEX ui_scenario_module_scenario_type_index (scenario_type)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario'
                   AND index_name LIKE 'ui_scenario_scenario_type_index'),
          'select 1',
          'ALTER TABLE ui_scenario ADD INDEX ui_scenario_scenario_type_index (scenario_type)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario'
                   AND index_name LIKE 'ui_scenario_status_index'),
          'select 1',
          'ALTER TABLE ui_scenario ADD INDEX ui_scenario_status_index (status)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SELECT IF(EXISTS(SELECT DISTINCT index_name
                 FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'ui_scenario'
                   AND index_name LIKE 'ui_scenario_module_id_index'),
          'select 1',
          'ALTER TABLE ui_scenario ADD INDEX ui_scenario_module_id_index (module_id)')
INTO @a;
PREPARE stmt1 FROM @a;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


SET SESSION innodb_lock_wait_timeout = DEFAULT;