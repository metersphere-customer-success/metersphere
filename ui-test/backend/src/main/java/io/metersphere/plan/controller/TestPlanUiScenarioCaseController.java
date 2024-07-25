package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.base.domain.UiScenario;
import io.metersphere.base.domain.UiScenarioReportWithBLOBs;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.track.RunTestPlanScenarioRequest;
import io.metersphere.dto.track.TestPlanScenarioCaseBatchRequest;
import io.metersphere.dto.track.TestPlanScenarioRequest;
import io.metersphere.dto.track.TestPlanUiScenarioDTO;
import io.metersphere.plan.dto.TestPlanScenarioStepCountSimpleDTO;
import io.metersphere.plan.dto.UiPlanReportDTO;
import io.metersphere.plan.request.UiPlanReportRequest;
import io.metersphere.plan.service.TestPlanUiScenarioCaseService;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.api.ApiPlanService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("/test/plan/uiScenario/case")
@RestController
public class TestPlanUiScenarioCaseController {

    @Resource
    TestPlanUiScenarioCaseService testPlanUiScenarioCaseService;
    @Resource
    ApiPlanService apiPlanService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<UiScenarioDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestPlanScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanUiScenarioCaseService.list(request));
    }

    @GetMapping("/list/{planId}")
    public List<TestPlanUiScenario> list(@PathVariable String planId) {
        return testPlanUiScenarioCaseService.list(planId);
    }

    @PostMapping("/list/all/{planId}")
    public List<TestPlanUiScenarioDTO> getAllList(@PathVariable String planId,
                                                  @RequestBody(required = false) List<String> statusList) {
        return testPlanUiScenarioCaseService.getAllCasesByStatusList(planId, statusList);
    }

    @PostMapping("/selectAllTableRows")
    public List<UiScenarioDTO> selectAllTableRows(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        return testPlanUiScenarioCaseService.selectAllTableRows(request);
    }

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<UiScenarioDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody UiScenarioRequest request) {
        return testPlanUiScenarioCaseService.relevanceList(request, goPage, pageSize);
    }

    @PostMapping("/relevance/list/ids")
    public List<String> relevanceListIds(@RequestBody UiScenarioRequest request) {
        return testPlanUiScenarioCaseService.relevanceListIds(request);
    }

    @PostMapping("/relevance/{planId}")
    public void relevanceByTestIds(@PathVariable String planId, @RequestBody List<String> uiScenarioIds) {
        testPlanUiScenarioCaseService.relevanceByTestIds(planId, uiScenarioIds);
    }

    @GetMapping("/delete/{id}")
//    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestPlanScenarioCaseService.class)
    public int deleteTestCase(@PathVariable String id) {
        return testPlanUiScenarioCaseService.delete(id);
    }

    @PostMapping("/batch/delete")
//    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.UN_ASSOCIATE_CASE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public void deleteApiCaseBath(@RequestBody TestPlanScenarioCaseBatchRequest request) {
        testPlanUiScenarioCaseService.deleteApiCaseBath(request);
    }

    @PostMapping(value = "/run")
//    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.planCaseIds)", msClass = TestPlanScenarioCaseService.class)
    public List<MsExecResponseDTO> run(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Completed.name());
        if (request.getConfig() == null) {
            RunModeConfigDTO config = new RunModeConfigDTO();
            config.setMode(RunModeConstants.PARALLEL.toString());
            config.setEnvMap(new HashMap<>());
            request.setConfig(config);
        }
        return testPlanUiScenarioCaseService.run(request);
    }

    @PostMapping(value = "/jenkins/run")
//    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.ids)", msClass = TestPlanScenarioCaseService.class)
    public List<MsExecResponseDTO> runByRun(@RequestBody RunTestPlanScenarioRequest request) {
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(ApiRunMode.API.name());
        request.setRunMode(ApiRunMode.SCENARIO.name());
        return testPlanUiScenarioCaseService.run(request);
    }

    @PostMapping("/edit/order")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        testPlanUiScenarioCaseService.updateOrder(request);
    }

    @PostMapping("/plan/report")
    public UiPlanReportDTO buildApiReport(@RequestBody UiPlanReportRequest request) {
        return testPlanUiScenarioCaseService.buildUiReport(request);
    }

    @GetMapping("/plan/exec/result/{planId}")
    public List<String> getExecResultByPlanId(@PathVariable String planId) {
        return testPlanUiScenarioCaseService.getExecResultByPlanId(planId);
    }

    @GetMapping("/get/report/status/{planId}")
    public List<PlanReportCaseDTO> selectStatusForPlanReport(@PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.selectStatusForPlanReport(planId);
    }

    @GetMapping("/plan/copy/{sourcePlanId}/{targetPlanId}")
    public void getStatusByTestPlanId(@PathVariable("sourcePlanId") String sourcePlanId, @PathVariable("targetPlanId") String targetPlanId) {
        testPlanUiScenarioCaseService.copyPlan(sourcePlanId, targetPlanId);
    }

    @GetMapping("/have/ui/case/{planId}")
    public boolean haveUiCase(@PathVariable String planId) {
        return testPlanUiScenarioCaseService.haveUiCase(planId);
    }

    @GetMapping("/is/executing/{planId}")
    public Boolean isExecuting(@PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.isExecuting(planId);
    }

    @PostMapping("/failure/list")
    public List<TestPlanUiScenarioDTO> getFailureListByIds(@RequestBody Set<String> planApiCaseIds) {
        return testPlanUiScenarioCaseService.getFailureListByIds(planApiCaseIds);
    }

    @PostMapping("/list/module/{planId}")
    public List<ModuleNodeDTO> getNodeByPlanId(@PathVariable String planId, @RequestBody List<String> projectIds) {
        return testPlanUiScenarioCaseService.getNodeByPlanId(projectIds, planId);
    }

    @PostMapping("/step/count")
    public TestPlanScenarioStepCountSimpleDTO getStepCount(@RequestBody List<PlanReportCaseDTO> planReportCaseDTOS) {
        return testPlanUiScenarioCaseService.getStepCount(planReportCaseDTOS);
    }

    @PostMapping("/build/response")
    public List<TestPlanUiScenarioDTO> buildResponse(@RequestBody List<TestPlanUiScenarioDTO> cases) {
        testPlanUiScenarioCaseService.buildUiScenarioResponse(cases);
        return cases;
    }

    @GetMapping("/get-scenario/{id}")
    public UiScenario getScenarioId(@PathVariable("id") String planScenarioId) {
        return testPlanUiScenarioCaseService.getScenarioId(planScenarioId);
    }

    @GetMapping("/get/report/ext/{planId}")
    public List<UiScenarioReportWithBLOBs> selectExtForPlanReport(@PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.selectExtForPlanReport(planId);
    }

    @PostMapping("/env")
    public Map<String, String> getScenarioCaseEnv(@RequestBody HashMap<String, String> map) {
        return testPlanUiScenarioCaseService.getScenarioCaseEnv(map);
    }

    @GetMapping("/get/env/{planId}")
    public Map<String, List<String>> getUiCaseEnv(@PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.getUiScenarioEnv(planId);
    }

    @GetMapping("/get/project/ids/{planId}")
    public List<String> getPlanProjectIds(@PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.getPlanProjectIds(planId);
    }

    @GetMapping("/get/env-project-ids/{planId}")
    public List<String> getEnvProjectIds(@PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.getEnvProjectIds(planId);
    }

    @PostMapping("/set/env/{planId}")
    public RunModeConfigDTO setScenarioEnv(@RequestBody RunModeConfigDTO runModeConfig, @PathVariable("planId") String planId) {
        return testPlanUiScenarioCaseService.setScenarioEnv(runModeConfig, planId);
    }

    @PostMapping("/get/env")
    public Map<String, List<String>> getApiCaseEnv(@RequestBody List<String> planApiScenarioIds) {
        return testPlanUiScenarioCaseService.getUiScenarioEnv(planApiScenarioIds);
    }

    /***
     * 测试计划列表获取运行的环境和模式信息
     * @param testPlanReport
     * @return
     */
    @PostMapping("/env/generate")
    public TestPlanEnvInfoDTO generateEnvironmentInfo(@RequestBody TestPlanReport testPlanReport) {
        return testPlanUiScenarioCaseService.generateEnvironmentInfo(testPlanReport);
    }

    /**
     * 获取测试计划报告ui关联用例的环境信息
     *
     * @param resourceIds
     * @return
     */
    @PostMapping("/get/plan/env/map")
    public Map<String, List<String>> getPlanProjectEnvMap(@RequestBody List<String> resourceIds) {
        return testPlanUiScenarioCaseService.getPlanProjectEnvMap(resourceIds);
    }
}
