SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE `ui_scenario_report`
(
    `id`                            varchar(50) NOT NULL COMMENT 'Test report ID',
    `project_id`                    varchar(50) NOT NULL COMMENT 'scenario ID this test report belongs to',
    `name`                          varchar(3000) DEFAULT NULL,
    `description`                   longtext COMMENT 'ui scenario report description',
    `create_time`                   bigint(13) NOT NULL COMMENT 'Create timestamp',
    `update_time`                   bigint(13) NOT NULL COMMENT 'Update timestamp',
    `status`                        varchar(64) NOT NULL COMMENT 'Status of this test run',
    `user_id`                       varchar(64)   DEFAULT NULL,
    `trigger_mode`                  varchar(64)   DEFAULT NULL,
    `execute_type`                  varchar(200)  DEFAULT NULL,
    `scenario_name`                 varchar(3000) DEFAULT NULL,
    `scenario_id`                   varchar(3000) DEFAULT NULL,
    `create_user`                   varchar(100)  DEFAULT NULL,
    `actuator`                      varchar(100)  DEFAULT NULL,
    `end_time`                      bigint(13) DEFAULT NULL,
    `report_version`                int(11) DEFAULT NULL,
    `version_id`                    varchar(50)   DEFAULT NULL,
    `report_type`                   varchar(100)  DEFAULT NULL,
    `env_config`                    longtext COMMENT '执行环境配置',
    `relevance_test_plan_report_id` varchar(50)   DEFAULT NULL COMMENT '关联的测试计划报告ID（可以为空)',
    `type`                          int(11) not null DEFAULT 1 COMMENT '数据类型：0-来自拆分前 api_scenario_report 数据，1-代表新数据',
    PRIMARY KEY (`id`),
    KEY                             `ui_scenario_report_version_id_index` (`version_id`) USING BTREE,
    KEY                             `projectIdIndex` (`project_id`) USING BTREE,
    KEY                             `projectIdexectypeIndex` (`project_id`,`execute_type`) USING BTREE,
    KEY                             `index_relevance_test_plan_report_id` (`relevance_test_plan_report_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE TABLE `ui_execution_info`
(
    `id`          varchar(50) NOT NULL,
    `source_id`   varchar(50) NOT NULL COMMENT 'ui definition id',
    `result`      varchar(50) NOT NULL,
    `create_time` bigint(13) NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`),
    KEY           `source_id` (`source_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE TABLE `ui_scenario_report_result`
(
    `id`               varchar(50) NOT NULL COMMENT 'ID',
    `resource_id`      varchar(200) DEFAULT NULL COMMENT '请求资源 id',
    `report_id`        varchar(50)  DEFAULT NULL COMMENT '报告 id',
    `create_time`      bigint(13) DEFAULT NULL COMMENT '创建时间',
    `status`           varchar(100) DEFAULT NULL COMMENT '结果状态',
    `request_time`     bigint(13) DEFAULT NULL COMMENT '请求时间',
    `total_assertions` bigint(13) DEFAULT NULL COMMENT '总断言数',
    `pass_assertions`  bigint(13) DEFAULT NULL COMMENT '失败断言数',
    `content`          longblob COMMENT '执行结果',
    `error_code`       varchar(255) DEFAULT NULL,
    `base_info`        longtext,
    PRIMARY KEY (`id`),
    KEY                `index_resource_id` (`resource_id`) USING BTREE,
    KEY                `ui_scenario_report_result_report_id_IDX` (`report_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE TABLE `ui_scenario_report_structure`
(
    `id`            varchar(50) NOT NULL COMMENT 'ID',
    `report_id`     varchar(50) DEFAULT NULL COMMENT '请求资源 id',
    `create_time`   bigint(13) DEFAULT NULL COMMENT '创建时间',
    `resource_tree` longblob COMMENT '资源步骤结构树',
    `console`       longtext COMMENT '执行日志',
    PRIMARY KEY (`id`),
    KEY             `index_report_id` (`report_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC
    COLLATE = utf8mb4_general_ci;

CREATE TABLE `ui_scenario_report_detail`
(
    `report_id`  varchar(64) NOT NULL COMMENT 'API Test Report ID',
    `project_id` varchar(64) NOT NULL COMMENT 'scenario ID',
    `content`    longblob COMMENT 'Report Content',
    PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE TABLE `ui_scenario_execution_info`
(
    `id`           varchar(50)  NOT NULL,
    `source_id`    varchar(255) NOT NULL COMMENT 'api scenario id',
    `result`       varchar(50)  NOT NULL,
    `trigger_mode` varchar(50) DEFAULT NULL,
    `create_time`  bigint(13) NOT NULL COMMENT 'Create timestamp',
    PRIMARY KEY (`id`),
    KEY            `source_id` (`source_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_general_ci;

SELECT *
FROM ui_scenario_report;

INSERT INTO ui_scenario_report (id,
                                project_id,
                                NAME,
                                description,
                                create_time,
                                update_time,
                                STATUS,
                                user_id,
                                trigger_mode,
                                execute_type,
                                scenario_name,
                                scenario_id,
                                create_user,
                                actuator,
                                end_time,
                                report_version,
                                version_id,
                                report_type,
                                env_config,
                                relevance_test_plan_report_id,
                                type)
SELECT id,
       project_id,
       NAME,
       description,
       create_time,
       update_time,
       STATUS,
       user_id,
       trigger_mode,
       execute_type,
       scenario_name,
       scenario_id,
       create_user,
       actuator,
       end_time,
       report_version,
       version_id,
       report_type,
       env_config,
       relevance_test_plan_report_id,
       0
FROM api_scenario_report
WHERE api_scenario_report.report_type LIKE '%UI%';

-- 场景报告状态统一调整
-- 失败状态
UPDATE ui_scenario_report
SET STATUS = 'ERROR'
WHERE STATUS IN ('Error', 'Timeout', 'Fail');

UPDATE ui_scenario_report_result
SET STATUS = 'ERROR'
WHERE STATUS IN ('Error', 'Timeout', 'Fail');

UPDATE ui_scenario
SET last_result = 'ERROR'
WHERE last_result IN ('Error', 'Timeout', 'Fail');

UPDATE test_plan_ui_scenario
SET last_result = 'ERROR'
WHERE last_result IN ('Error', 'Timeout', 'Fail');

-- 成功状态
UPDATE ui_scenario_report
SET STATUS = 'SUCCESS'
WHERE STATUS = 'Success';

UPDATE ui_scenario_report_result
SET STATUS = 'SUCCESS'
WHERE STATUS = 'Success';

UPDATE ui_scenario
SET last_result = 'SUCCESS'
WHERE last_result = 'Success';

UPDATE test_plan_ui_scenario
SET last_result = 'SUCCESS'
WHERE last_result = 'Success';

-- 未执行
UPDATE ui_scenario_report
SET STATUS = 'PENDING'
WHERE STATUS = 'unexecute';

UPDATE ui_scenario_report
SET STATUS = 'PENDING'
WHERE STATUS is null;

UPDATE ui_scenario_report
SET STATUS = 'PENDING'
WHERE STATUS = 'Waiting';

UPDATE ui_scenario_report_result
SET STATUS = 'PENDING'
WHERE STATUS = 'unexecute';

UPDATE ui_scenario
SET last_result = 'PENDING'
WHERE last_result = 'unexecute';

UPDATE ui_scenario
SET last_result = 'PENDING'
WHERE last_result is null;

UPDATE test_plan_ui_scenario
SET last_result = 'PENDING'
WHERE last_result = 'unexecute';

UPDATE test_plan_ui_scenario
SET last_result = 'PENDING'
WHERE last_result is null;

UPDATE test_plan_ui_scenario
SET last_result = 'PENDING'
WHERE last_result = 'UnExecute';

-- 已停止
UPDATE ui_scenario_report
SET STATUS = 'STOPPED'
WHERE STATUS = 'STOP';

UPDATE ui_scenario_report_result
SET STATUS = 'STOPPED'
WHERE STATUS = 'STOP';

UPDATE ui_scenario
SET last_result = 'STOPPED'
WHERE last_result = 'STOP';

UPDATE test_plan_ui_scenario
SET last_result = 'STOPPED'
WHERE last_result = 'STOP';

-- 执行中
UPDATE ui_scenario_report
SET STATUS = 'RUNNING'
WHERE STATUS = 'Running';

UPDATE ui_scenario_report_result
SET STATUS = 'RUNNING'
WHERE STATUS = 'Running';

UPDATE ui_scenario
SET last_result = 'RUNNING'
WHERE last_result = 'Running';

UPDATE test_plan_ui_scenario
SET last_result = 'RUNNING'
WHERE last_result = 'Running';


-- 测试计划场景结果状态
update test_plan_ui_scenario set last_result ='ERROR' where last_result in ('Error','Timeout','Fail');
-- 成功
update test_plan_ui_scenario set last_result ='SUCCESS' where last_result = 'Success';
-- 未执行
update test_plan_ui_scenario set last_result ='PENDING' where last_result = 'unexecute';
update test_plan_ui_scenario set last_result ='PENDING' where last_result is null;
-- 已停止
update test_plan_ui_scenario set last_result ='STOPPED' where last_result = 'STOP';
-- 误报
update test_plan_ui_scenario set last_result ='FAKE_ERROR' where last_result in ('errorReportResult','errorReport');
-- 执行中
update test_plan_ui_scenario set last_result ='RUNNING' where last_result = 'Running';

SET SESSION innodb_lock_wait_timeout = DEFAULT;
