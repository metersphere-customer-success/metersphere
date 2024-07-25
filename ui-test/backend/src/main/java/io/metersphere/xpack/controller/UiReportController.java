package io.metersphere.xpack.controller;


import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.DeleteAPIReportRequest;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.security.CheckOwner;
import io.metersphere.service.UiReportService;
import io.metersphere.service.UiScenarioReportService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RequestMapping("/ui/report")
@RestController
public class UiReportController {

    @Resource
    UiReportService uiReportService;

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION_REPORT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = UiScenarioReportService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.UI_REPORT_TASK, event = NoticeConstants.Event.DELETE,
            target = "#targetClass.getReport(#request)", targetClass = UiReportService.class, subject = "UI测试报告通知")
    @CheckOwner(resourceId = "#request.getId()", resourceType = "ui_scenario_report")
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        uiReportService.delete(request);
    }
}
