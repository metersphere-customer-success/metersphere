package io.metersphere.sechedule;

import org.quartz.JobExecutionContext;

/**
 * 情景测试Job
 *
 * @author song.tianyang
 * @Date 2020/12/22 2:59 下午
 * @Description
 */
public class TestPlanTestJob extends MsScheduleJob {
    @Override
    void businessExecute(JobExecutionContext context) {
        //todo
    }
//    private String projectID;
//
//    private TestPlanService testPlanService;
//
//    public TestPlanTestJob() {
//        this.testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
//    }
//
//    /**
//     * 情景部分的准备工作
//     *
//     * @param context
//     * @throws JobExecutionException
//     */
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        JobKey jobKey = context.getTrigger().getJobKey();
//        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
//
//        this.resourceId = jobDataMap.getString("resourceId");
//        this.userId = jobDataMap.getString("userId");
//        this.expression = jobDataMap.getString("expression");
//        this.projectID = jobDataMap.getString("projectId");
//
//
//        businessExecute(context);
//    }
//
//    @Override
//    void businessExecute(JobExecutionContext context) {
//
//        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
//        String config = jobDataMap.getString("config");
//
//        String runResourceId = this.resourceId;
//        String runProjectId = this.projectID;
//        String runUserId = this.userId;
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                LogUtil.info("Start test_plan_scehdule. test_plan_id:" + runResourceId);
//                testPlanService.run(runResourceId, runProjectId, runUserId, ReportTriggerMode.SCHEDULE.name(),null,config);
//            }
//        });
//        thread.start();
//    }
//
//    public static JobKey getJobKey(String testId) {
//        return new JobKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
//    }
//
//    public static TriggerKey getTriggerKey(String testId) {
//        return new TriggerKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
//    }
}
