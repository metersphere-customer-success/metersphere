package io.metersphere.xpack.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.UiScenarioReport;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportResultMapper;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.dto.report.UiReportStepResultDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.security.CheckOwner;
import io.metersphere.service.UiScenarioReportService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/ui/scenario/report")
public class UiScenarioReportController {

    @Resource
    private UiScenarioReportService uiScenarioReportService;
    @Resource
    private ExtUiScenarioReportResultMapper extUiScenarioReportResultMapper;

    @GetMapping("/get/{reportId}")
    public ScenarioReportResultWrapper get(@PathVariable String reportId) {
        return uiScenarioReportService.get(reportId, true);
    }

    @GetMapping("/get/detail/{reportId}")
    public ScenarioReportResultWrapper getAll(@PathVariable String reportId) {
        return uiScenarioReportService.get(reportId, true);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_UI_REPORT:READ")
    public Pager<List<ScenarioReportResultWrapper>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryUiReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setLimit("LIMIT " + (goPage - 1) * pageSize + "," + pageSize * 50);
        return PageUtils.setPageInfo(page, uiScenarioReportService.list(request));
    }

    @PostMapping("/update")
    public String update(@RequestBody ScenarioReportResultWrapper node) {
        node.setExecuteType(ExecuteType.Saved.name());
        return uiScenarioReportService.update(node);
    }

    @GetMapping("/get/step/detail/{stepId}")
    public RequestResult selectReportContent(@PathVariable String stepId) {
        return uiScenarioReportService.selectReportContent(stepId);
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION_REPORT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = UiScenarioReportService.class)
//    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.get(#request.id, false)", targetClass = UiScenarioReportService.class,
//            subject = "接口报告通知")
    @CheckOwner(resourceId = "#request.getId()", resourceType = "ui_scenario_report")
    public void delete(@RequestBody DeleteAPIReportRequest request) {
        uiScenarioReportService.delete(request);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_REPORT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION_REPORT, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = UiScenarioReportService.class)
//    @SendNotice(taskType = NoticeConstants.TaskType.API_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getByIds(#request.ids)", targetClass = UiScenarioReportService.class,
//            subject = "接口报告通知")
    @CheckOwner(resourceId = "#request.getIds()", resourceType = "ui_scenario_report")
    public void deleteAPIReportBatch(@RequestBody UiReportBatchRequest request) {
        uiScenarioReportService.deleteAPIReportBatch(request);
    }

    @PostMapping("/rename")
    public void reName(@RequestBody UiScenarioReport reportRequest) {
        uiScenarioReportService.reName(reportRequest);
    }

    @PostMapping("/plan/status/map")
    public Map<String, String> selectReportResultByReportIds(@RequestBody List<String> apiReportIds) {
        return uiScenarioReportService.getReportStatusByReportIds(apiReportIds);
    }

    @PostMapping("/plan/report")
    public List<PlanReportCaseDTO> selectForPlanReport(@RequestBody List<String> reportIds) {
        return uiScenarioReportService.selectForPlanReport(reportIds);
    }

    @PostMapping("/step/{reportId}/{stepId}")
    public List<UiReportStepResultDTO> getStepResult(@PathVariable("reportId") String reportId, @PathVariable("stepId") String stepId) {
        return extUiScenarioReportResultMapper.getStepResult(reportId, stepId);
    }
}
