package io.metersphere.sechedule;

import org.quartz.JobExecutionContext;

public class ApiTestJob extends MsScheduleJob {
    @Override
    void businessExecute(JobExecutionContext context) {
        // todo
    }
//
//    private APITestService apiTestService;
//    public ApiTestJob() {
//        apiTestService = CommonBeanFactory.getBean(APITestService.class);
//    }
//
//    @Override
//    void businessExecute(JobExecutionContext context) {
//        SaveAPITestRequest request = new SaveAPITestRequest();
//        request.setId(resourceId);
//        request.setUserId(userId);
//        request.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
//        apiTestService.run(request);
//    }
//
//    public static JobKey getJobKey(String testId) {
//        return new JobKey(testId, ScheduleGroup.API_TEST.name());
//    }
//
//    public static TriggerKey getTriggerKey(String testId) {
//        return new TriggerKey(testId, ScheduleGroup.API_TEST.name());
//    }
}

