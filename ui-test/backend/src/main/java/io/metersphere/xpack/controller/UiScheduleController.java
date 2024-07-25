package io.metersphere.xpack.controller;

import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.sechedule.UiTestJob;
import io.metersphere.service.BaseScheduleService;
import io.metersphere.service.UiScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ui/automation/schedule")
public class UiScheduleController {
    @Autowired
    UiScheduleService scheduleService;

    @Autowired
    BaseScheduleService baseScheduleService;

    @PostMapping(value = "/create")
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION_SCHEDULE, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#schedule)", msClass = UiScheduleService.class)
    public void create(@RequestBody Schedule schedule) {
        schedule.setUserId(SessionUtils.getUser().getId());
        schedule.setJob(UiTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.UI_SCENARIO_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setKey(schedule.getResourceId());
        scheduleService.addSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule, UiTestJob.getJobKey(schedule.getResourceId()), UiTestJob.getTriggerKey(schedule.getResourceId()), UiTestJob.class);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION_SCHEDULE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = UiScheduleService.class)
    public void updateSchedule(@RequestBody Schedule request) {
        scheduleService.updateSchedule(request);
    }

    @PostMapping(value = "/status/update")
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION_SCHEDULE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = UiScheduleService.class)
    public void updateScheduleStatus(@RequestBody Schedule request) {
        scheduleService.updateScheduleStatus(request);
    }

    @GetMapping("/get/{testId}/{group}")
    public Schedule schedule(@PathVariable String testId, @PathVariable String group) {
        Schedule schedule = baseScheduleService.getScheduleByResource(testId, group);
        return schedule;
    }
}
