package io.metersphere.xpack.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.UiScenario;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.constants.SystemConstants;
import io.metersphere.controller.handler.ResultHolder;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.dto.automation.TaskRequest;
import io.metersphere.dto.testcase.FileOperationRequest;
import io.metersphere.dto.testcase.testcase.UiCaseRelevanceRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.security.CheckOwner;
import io.metersphere.service.UiAutomationService;
import io.metersphere.service.UiReportService;
import io.metersphere.service.task.UiTaskService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/ui/automation")
@RestController
public class UiAutomationController {

    @Resource
    private UiAutomationService uiAutomationService;
    @Resource
    private UiReportService uiReportService;
    @Resource
    private UiTaskService uiTaskService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public Pager<List<UiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody UiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // 查询场景环境
        request.setSelectEnvironment(true);
        return PageUtils.setPageInfo(page, uiAutomationService.list(request));
    }

    @PostMapping("/list")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<UiScenarioDTO> listAll(@RequestBody UiScenarioRequest request) {
        return uiAutomationService.list(request);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public UiScenarioDTO getById(@PathVariable String id) {
        return uiAutomationService.getDto(id);
    }

    @PostMapping("/list/all")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<UiScenarioDTO> listAll(@RequestBody UiScenarioBatchRequest request) {
        return uiAutomationService.listAll(request);
    }

    @PostMapping("/list/all/trash")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public int listAllTrash(@RequestBody UiScenarioBatchRequest request) {
        return uiAutomationService.listAllTrash(request);
    }

    @PostMapping("/listWithIds/all")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<UiScenarioWithBLOBs> listWithIds(@RequestBody UiScenarioBatchRequest request) {
        return uiAutomationService.listWithIds(request);
    }

    @PostMapping("/id/all")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<String> idAll(@RequestBody UiScenarioBatchRequest request) {
        return uiAutomationService.idAll(request);
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<UiScenarioDTO> list(@PathVariable String projectId) {
        UiScenarioRequest request = new UiScenarioRequest();
        request.setProjectId(projectId);
        return uiAutomationService.list(request);
    }

    @PostMapping(value = "/create")
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = UiAutomationService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_CREATE, PermissionConstants.PROJECT_UI_SCENARIO_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.UI_AUTOMATION_TASK, event = NoticeConstants.Event.CREATE, subject = "UI自动化通知")
    public UiScenario create(@RequestPart("request") SaveUiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                             @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return uiAutomationService.create(request, bodyFiles, scenarioFiles);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = UiAutomationService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_EDIT, PermissionConstants.PROJECT_UI_SCENARIO_READ_COPY}, logical = Logical.OR)
    @SendNotice(taskType = NoticeConstants.TaskType.UI_AUTOMATION_TASK, event = NoticeConstants.Event.UPDATE, subject = "UI自动化通知")
    public UiScenario update(@RequestPart("request") SaveUiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                             @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return uiAutomationService.update(request, bodyFiles, scenarioFiles);
    }


    @PostMapping(value = "/save/file")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_EDIT, PermissionConstants.PROJECT_UI_SCENARIO_READ_COPY}, logical = Logical.OR)
    public void saveFile(@RequestPart("request") SaveUiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                             @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        uiAutomationService.uploadFiles(request, bodyFiles, scenarioFiles);
    }

    @PostMapping("/edit/order")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_EDIT, PermissionConstants.PROJECT_UI_SCENARIO_READ_COPY}, logical = Logical.OR)
    public void orderCase(@RequestBody UiResetOrderRequestExt request) {
        uiAutomationService.updateOrder(request);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = UiAutomationService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "ui_scenario")
    public void delete(@PathVariable String id) {
        uiAutomationService.delete(id);
    }

    @PostMapping("/deleteBatch")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = UiAutomationService.class)
    @CheckOwner(resourceId = "#ids", resourceType = "ui_scenario")
    public void deleteBatch(@RequestBody List<String> ids) {
        uiAutomationService.deleteBatch(ids);
    }

    @PostMapping("/deleteBatchByCondition")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = UiAutomationService.class)
    @CheckOwner(resourceId = "#request.ids", resourceType = "ui_scenario")
    public void deleteBatchByCondition(@RequestBody UiScenarioBatchRequest request) {
        uiAutomationService.deleteBatchByCondition(request);
    }

    @PostMapping("/removeToGc")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = UiAutomationService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.UI_AUTOMATION_TASK, target = "#targetClass.getUiScenarioByIds(#ids)", targetClass = UiAutomationService.class, event = NoticeConstants.Event.DELETE, subject = "UI自动化通知")
    @CheckOwner(resourceId = "#ids", resourceType = "ui_scenario")
    public void removeToGc(@RequestBody List<String> ids) {
        uiAutomationService.removeToGcOpt(ids);
    }

    @PostMapping("/removeToGcByBatch")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.BATCH_GC, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = UiAutomationService.class)
    @CheckOwner(resourceId = "#request.ids", resourceType = "ui_scenario")
    public void removeToGcByBatch(@RequestBody UiScenarioBatchRequest request) {
        uiAutomationService.removeToGcByBatch(request);
    }

    @PostMapping("/checkBeforeDelete")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    public DeleteCheckResult checkBeforeDelete(@RequestBody UiScenarioBatchRequest request) {
        //暂时没有引用
        DeleteCheckResult checkResult = new DeleteCheckResult();
        checkResult.setDeleteFlag(true);
        return checkResult;
    }

    @PostMapping("/reduction")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.RESTORE, content = "#msClass.getLogDetails(#ids)", msClass = UiAutomationService.class)
    @CheckOwner(resourceId = "#ids", resourceType = "ui_scenario")
    public void reduction(@RequestBody List<String> ids) {
        uiAutomationService.reduction(ids);
    }

    @GetMapping("/getUiScenario/{id}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public UiScenarioDTO getScenarioDefinition(@PathVariable String id) {
        return uiAutomationService.getNewUiScenario(id);
    }


    @PostMapping("/getUiScenarios")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<UiScenarioDTO> getUiScenarios(@RequestBody List<String> ids) {
        return uiAutomationService.getNewUiScenarios(ids);
    }

    @PostMapping(value = "/plan/run")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_RUN)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = UiAutomationService.class)
    public List<MsExecResponseDTO> run(@RequestBody RunUiScenarioRequest request) {
        request.setRequestOriginator(SystemConstants.UIRequestOriginatorEnum.TEST_PLAN.getName());
        return uiAutomationService.run(request);
    }

    @PostMapping(value = "/run/batch")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = UiAutomationService.class)
    public List<MsExecResponseDTO> runBatch(@RequestBody RunUiScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(TriggerMode.BATCH.name());
        }
        request.setRunMode(ApiRunMode.UI_SCENARIO.name());
        return uiAutomationService.run(request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_MOVE_BATCH)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = UiAutomationService.class)
    public void bathEdit(@RequestBody UiScenarioBatchRequest request) {
        uiAutomationService.bathEditOpt(request);
    }


    @PostMapping(value = "/run/debug")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_DEBUG, PermissionConstants.PROJECT_UI_SCENARIO_READ_RUN}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.DEBUG, title = "#request.scenarioName", sourceId = "#request.scenarioId", project = "#request.projectId")
    public void runDebug(@RequestPart("request") RunDefinitionRequest request) throws JsonProcessingException {
        uiAutomationService.debug(request);
    }


    @PostMapping("/batch/copy")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_MOVE_COPY)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = UiAutomationService.class)
    public void batchCopy(@RequestBody UiScenarioBatchRequest request) {
        uiAutomationService.batchCopyOpt(request);
    }

    @PostMapping("/batch/update/env")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ)
    public void batchUpdateEnv(@RequestBody UiScenarioBatchRequest request) {
    }

    @PostMapping("/relevance")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public void testPlanRelevance(@RequestBody UiCaseRelevanceRequest request) {
        uiAutomationService.relevance(request);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> download(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = uiAutomationService.loadFileAsBytes(fileOperationRequest);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(bytes);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_IMPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.IMPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ScenarioImport scenarioImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") UiTestImportRequest request) {
        return uiAutomationService.scenarioImport(file, request);
    }

    @PostMapping(value = "/export")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_EXPORT_SCENARIO)
    @MsAuditLog(module = OperLogModule.UI_AUTOMATION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ResponseEntity<byte[]> export(@RequestBody UiScenarioBatchRequest request) {
        byte[] bytes = uiAutomationService.export(request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "UI场景集.zip")
                .body(bytes);
    }

    @GetMapping(value = "/stop/{reportId}")
    public void stop(@PathVariable String reportId) {
        if (StringUtils.isNotEmpty(reportId)) {
            List<TaskRequest> reportIds = new ArrayList<>();
            TaskRequest taskRequest = new TaskRequest();
            taskRequest.setReportId(reportId);
            taskRequest.setType("UI_SCENARIO");
            reportIds.add(taskRequest);
            uiTaskService.stop(reportIds);
        }
    }

    @PostMapping(value = "/stop/batch")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public String stopBatch(@RequestBody List<TaskRequest> reportIds) {
        return uiTaskService.stop(reportIds);
    }


    @GetMapping("versions/{scenarioId}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public List<UiScenarioDTO> getUiScenarioVersions(@PathVariable String scenarioId) {
        return uiAutomationService.getUiScenarioVersions(scenarioId);
    }

    @GetMapping("get/{version}/{refId}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public UiScenarioDTO getUiScenario(@PathVariable String version, @PathVariable String refId) {
        return uiAutomationService.getUiScenarioByVersion(refId, version);
    }

    @GetMapping("delete/{version}/{refId}")
    @RequiresPermissions(PermissionConstants.PROJECT_UI_SCENARIO_READ_DELETE)
    public void deleteUiScenario(@PathVariable String version, @PathVariable String refId) {
        uiAutomationService.deleteUiScenarioByVersion(refId, version);
    }

    @PostMapping(value = "/env")
    public List<String> getEnvProjects(@RequestBody RunScenarioRequest request) {
        return uiAutomationService.getProjects(request);
    }

    @GetMapping("/selectReportContent/{stepId}")
    public UiCommandResult selectReportContent(@PathVariable String stepId) {
        return uiReportService.selectReportContent(stepId);
    }

    /**
     * 统一校验接口，前后端通用
     *
     * @param uiScenarioStr
     * @return
     */
    @PostMapping("/validateScenario")
    public ResultHolder validateScenario(@RequestBody String uiScenarioStr) {
        return uiAutomationService.validateScenario(uiScenarioStr);
    }

    /**
     * 下载截图文件
     */
    @PostMapping("/download/screenshot")
    public void downloadScreenshot(@RequestBody DownloadScreenshotDTO request, HttpServletResponse response) {
        uiAutomationService.downloadScreenshot(response, request);
    }

    /**
     * 获取测试计划引用
     */
    @PostMapping("/testPlanRef/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public Pager<List<TestPlanRefResp>> getTestPlanRef(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody RefReq request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // 查询场景环境
        return PageUtils.setPageInfo(page, uiAutomationService.getTestPlanRef(request));
    }

    /**
     * 获取 场景/指令引用情况
     */
    @PostMapping("/ref/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public Pager<List<RefResp>> refList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody RefReq request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // 查询场景环境
        return PageUtils.setPageInfo(page, uiAutomationService.refList(request));
    }

    /**
     * 指令或场景删除前检测
     */
    @PostMapping("/check/ref")
    @RequiresPermissions("PROJECT_UI_SCENARIO:READ")
    public UiCheckRefResp checkRef(@RequestBody UiCheckRefReq request) {
        return uiAutomationService.checkRef(request);
    }

    /**
     * 校验 selenium 配置和服务
     *
     * @param type user/sys 校验个人或者系统
     * @return
     */
    @GetMapping("/verify/seleniumServer/{type}")
    public String verifySeleniumServer(@PathVariable String type) {
        return uiAutomationService.verifyUserSeleniumServer(type);
    }

    /**
     * 根据场景目前的定义获取所有关联的环境所包含的项目id信息
     *
     * @param request
     * @return
     */
    @PostMapping("/scenario-env")
    public ScenarioEnv getUiScenarioEnv(@RequestBody UiScenarioEnvRequest request) {
        return uiAutomationService.getUiScenarioEnv(request.getDefinition());
    }

    @GetMapping("/env-project-ids/{id}")
    public ScenarioEnv getApiScenarioProjectId(@PathVariable String id) {
        return uiAutomationService.getApiScenarioProjectId(id);
    }

    @PostMapping("/id-project")
    public Map<String,List<String>> getScenarioProjectMap(@RequestBody List<String> ids) {
        return uiAutomationService.getScenarioProjectMap(ids);
    }

    @PostMapping(value = "/env/map")
    public Map<String, List<String>> getProjectEnvMap(@RequestBody RunScenarioRequest request) {
        return uiAutomationService.getProjectEnvMap(request);
    }

    @GetMapping(value = "/env-valid/{scenarioId}")
    public boolean checkScenarioEnvByScenarioId(@PathVariable String scenarioId) {
        try {
            return uiAutomationService.verifyScenarioEnv(scenarioId);
        } catch (Exception e) {
            MSException.throwException(Translator.get("scenario_step_parsing_error_check"));
        }
        return false;
    }

    @PostMapping("/set-domain")
    public String setDomain(@RequestBody UiScenarioEnvRequest request) {
        return uiAutomationService.setDomain(request);
    }

    @PostMapping("/scenario/schedule")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ, PermissionConstants.PROJECT_UI_ELEMENT_READ, PermissionConstants.PROJECT_UI_SCENARIO_READ}, logical = Logical.OR)
    public Map<String, ScheduleDTO> scenarioScheduleInfo(@RequestBody List<String> scenarioIds) {
        return uiAutomationService.selectScheduleInfo(scenarioIds);
    }
}
