INSERT INTO `test_plan`(`id`, `num`, `project_id`, `group_id`, `module_id`, `name`, `status`, `type`, `tags`, `create_time`, `create_user`, `update_time`, `update_user`, `planned_start_time`, `planned_end_time`, `actual_start_time`, `actual_end_time`, `description`)
VALUES
    ('wx_test_plan_id_1', 5000, 'wx', 'NONE', '1', '测试一下计划', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_2', 10000, 'wx', 'NONE', '1', '测试一下组', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_3', 15000, 'wx', 'NONE', '1', '测试一下组2', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_4', 20000, 'wx', 'wx_test_plan_id_3', '1', '测试一下计划2', 'PREPARED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_5', 25000, 'wx', 'NONE', '1', '测试一下组3', 'PREPARED', 'GROUP', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11'),
    ('wx_test_plan_id_6', 30000, 'wx', 'wx_test_plan_id_5', '1', '测试组3下计划', 'COMPLETED', 'TEST_PLAN', NULL, 1714980158000, 'WX', 1714980158000, 'WX', 1714980158000, 1714980158000, 1714980158000, 1714980158000, '11');