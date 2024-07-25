package io.metersphere.plan.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.TestPlanUiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanUiScenarioCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.EnvironmentTypeEnum;
import io.metersphere.constants.TestPlanUiResultStatus;
import io.metersphere.constants.UiScenarioType;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.dto.report.TestPlanExecuteReportDTO;
import io.metersphere.dto.request.plan.TestPlanScenarioStepCountDTO;
import io.metersphere.dto.testcase.UiRunModeConfigDTO;
import io.metersphere.dto.track.*;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.plan.dto.TestPlanScenarioStepCountSimpleDTO;
import io.metersphere.plan.dto.UiPlanReportDTO;
import io.metersphere.plan.request.UiPlanReportRequest;
import io.metersphere.plan.service.remote.TestPlanService;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanUiScenarioCaseService {

    @Resource
    UiAutomationService uiAutomationService;
    @Resource
    TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    ExtTestPlanUiScenarioCaseMapper extTestPlanUiScenarioCaseMapper;
    @Resource
    UiScenarioReportService uiScenarioReportService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private UiScenarioModuleService uiScenarioModuleService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private UiScenarioEnvService uiScenarioEnvService;
    @Resource
    private BaseEnvGroupProjectService envGroupProjectService;

    private static final String UN_EXECUTE = "UnExecute";

    public List<UiScenarioDTO> list(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<UiScenarioDTO> apiTestCases = extTestPlanUiScenarioCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildProjectInfo(apiTestCases);
        buildUserInfo(apiTestCases);
        return buildEnvironment(apiTestCases);
    }

    private List<UiScenarioDTO> buildEnvironment(List<UiScenarioDTO> scenarioDTOList) {
        List<UiScenarioDTO> returnData = new ArrayList<>();

        scenarioDTOList.forEach(scenario -> {
            if (StringUtils.equalsIgnoreCase(scenario.getEnvironmentType(), EnvironmentType.GROUP.name())) {
                if (StringUtils.isNotEmpty(scenario.getEnvironmentGroupId())) {
                    Map<String, String> map = this.getEnvNameMap(scenario.getEnvironmentGroupId());
                    scenario.setEnvironmentMap(map);
                }
            } else if (StringUtils.equalsIgnoreCase(scenario.getEnvironmentType(), EnvironmentType.JSON.name())) {
                try {
                    if (StringUtils.isNotEmpty(scenario.getEnvironment())) {
                        Map<String, String> environmentMap = this.getScenarioCaseEnv(JSON.parseMap(scenario.getEnvironment()));
                        scenario.setEnvironmentMap(environmentMap);
                    }
                } catch (Exception e) {
                    LogUtil.error("测试计划场景环境解析报错!", e);
                }
            }
            returnData.add(scenario);
        });
        return returnData;
    }

    public Map<String, String> getEnvNameMap(String groupId) {
        Map<String, String> map = envGroupProjectService.getEnvMap(groupId);
        Set<String> set = map.keySet();
        HashMap<String, String> envMap = new HashMap<>(16);
        if (set.isEmpty()) {
            return envMap;
        }
        for (String projectId : set) {
            String envId = map.get(projectId);
            if (StringUtils.isBlank(envId)) {
                continue;
            }
            Project project = baseProjectService.getProjectById(projectId);
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
            if (project == null || environment == null) {
                continue;
            }
            String projectName = project.getName();
            String envName = environment.getName();
            if (StringUtils.isBlank(projectName) || StringUtils.isBlank(envName)) {
                continue;
            }
            envMap.put(projectName, envName);
        }
        return envMap;
    }

    public List<TestPlanUiScenario> list(String planId) {
        return extTestPlanUiScenarioCaseMapper.selectLegalDataByTestPlanId(planId);
    }

    public void buildProjectInfo(List<? extends UiScenarioDTO> apiTestCases) {
        List<String> projectIds = apiTestCases.stream()
                .map(UiScenarioDTO::getProjectId)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        Map<String, Project> projectMap = baseProjectService.getProjectByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        apiTestCases.forEach(item -> {
            Project project = projectMap.get(item.getProjectId());
            if (project != null) {
                ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());
                boolean custom = config.getScenarioCustomNum();
                if (custom) {
                    item.setCustomNum(item.getCustomNum());
                } else {
                    item.setCustomNum(item.getNum().toString());
                }
            } else {
                item.setCustomNum(item.getNum().toString());
            }
        });
    }

    public void buildUserInfo(List<? extends UiScenarioDTO> apiTestCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(UiScenarioDTO::getUserId).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(UiScenarioDTO::getPrincipal).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            apiTestCases.forEach(caseResult -> {
                caseResult.setCreatorName(userMap.get(caseResult.getCreateUser()));
                caseResult.setPrincipalName(userMap.get(caseResult.getPrincipal()));
            });
        }
    }

    public List<String> selectIds(TestPlanScenarioRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<String> idList = extTestPlanUiScenarioCaseMapper.selectIds(request);
        return idList;
    }

    public Pager<List<UiScenarioDTO>> relevanceList(UiScenarioRequest request, int goPage, int pageSize) {
        request.setNotInTestPlan(true);
        if (request.getAllowedRepeatCase()) {
            request.setNotInTestPlan(false);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, uiAutomationService.list(request));
    }

    public int delete(String id) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria()
                .andIdEqualTo(id);

        return testPlanUiScenarioMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanScenarioCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria()
                .andIdIn(ids);
        testPlanUiScenarioMapper.deleteByExample(example);
    }

    public List<MsExecResponseDTO> run(RunTestPlanScenarioRequest testPlanScenarioRequest) {
        StringBuilder idStr = new StringBuilder();
        List<String> planCaseIdList = testPlanScenarioRequest.getPlanCaseIds();
        if (testPlanScenarioRequest.getCondition() != null && testPlanScenarioRequest.getCondition().isSelectAll()) {
            planCaseIdList = this.selectIds(testPlanScenarioRequest.getCondition());
            if (testPlanScenarioRequest.getCondition().getUnSelectIds() != null) {
                planCaseIdList.removeAll(testPlanScenarioRequest.getCondition().getUnSelectIds());
            }
        }
        testPlanScenarioRequest.setPlanCaseIds(planCaseIdList);
        if (CollectionUtils.isEmpty(planCaseIdList)) {
            MSException.throwException("未找到执行场景！");
        }
        planCaseIdList.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<TestPlanUiScenario> testPlanApiScenarioList = extTestPlanUiScenarioCaseMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + StringUtils.join(testPlanScenarioRequest.getPlanCaseIds(), ",") + "\"");
        List<String> scenarioIds = new ArrayList<>();
        Map<String, String> scenarioPlanIdMap = new LinkedHashMap<>();
        for (TestPlanUiScenario apiScenario : testPlanApiScenarioList) {
            scenarioIds.add(apiScenario.getUiScenarioId());
            scenarioPlanIdMap.put(apiScenario.getId(), apiScenario.getUiScenarioId());
        }
        if (scenarioPlanIdMap.isEmpty()) {
            MSException.throwException("未找到执行场景！");
        }
        RunUiScenarioRequest request = new RunUiScenarioRequest();
        request.setIds(scenarioIds);
        request.setReportId(testPlanScenarioRequest.getId());
        request.setScenarioTestPlanIdMap(scenarioPlanIdMap);
        request.setRunMode(ApiRunMode.UI_SCENARIO_PLAN.name());
        request.setId(testPlanScenarioRequest.getId());
        request.setExecuteType(ExecuteType.Saved.name());
        request.setTriggerMode(testPlanScenarioRequest.getTriggerMode());
        request.setConfig(testPlanScenarioRequest.getConfig());
        request.setProjectId(testPlanScenarioRequest.getProjectId());
        UiRunModeConfigDTO configDTO = new UiRunModeConfigDTO();
        BeanUtils.copyBean(configDTO, testPlanScenarioRequest.getConfig());
        request.setUiConfig(configDTO);
        request.setPlanCaseIds(planCaseIdList);
        request.setRequestOriginator("TEST_PLAN");
        return uiAutomationService.run(request);
    }

    public List<TestPlanUiScenario> getCasesByPlanId(String planId) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanUiScenarioMapper.selectByExample(example);
    }

    public List<String> getExecResultByPlanId(String planId) {
        List<String> status = extTestPlanUiScenarioCaseMapper.getExecResultByPlanId(planId);
        return convertStatus(status);
    }

    public void deleteByPlanId(String planId) {
        TestPlanScenarioCaseBatchRequest request = new TestPlanScenarioCaseBatchRequest();
        List<String> ids = extTestPlanUiScenarioCaseMapper.getIdsByPlanId(planId);
        request.setIds(ids);
        deleteApiCaseBath(request);
    }

    public void deleteByPlanIds(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return;
        }
        TestPlanUiScenarioExample testPlanUiScenarioExample = new TestPlanUiScenarioExample();
        testPlanUiScenarioExample.createCriteria().andTestPlanIdIn(planIds);
        testPlanUiScenarioMapper.deleteByExample(testPlanUiScenarioExample);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanScenarioCaseBatchRequest request = new TestPlanScenarioCaseBatchRequest();
        request.setIds(extTestPlanUiScenarioCaseMapper.getNotRelevanceCaseIds(planId, relevanceProjectIds));
        request.setPlanId(planId);
        deleteApiCaseBath(request);
    }

    public void bathDeleteByScenarioIds(List<String> ids) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andUiScenarioIdIn(ids);
        testPlanUiScenarioMapper.deleteByExample(example);
    }

    public void deleteByScenarioId(String id) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andUiScenarioIdEqualTo(id);
        testPlanUiScenarioMapper.deleteByExample(example);
    }

    public List<UiScenarioDTO> selectAllTableRows(TestPlanScenarioCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        TestPlanScenarioRequest tableRequest = new TestPlanScenarioRequest();
        tableRequest.setIds(ids);
        return extTestPlanUiScenarioCaseMapper.list(tableRequest);
    }

    public String getLogDetails(String id) {
        TestPlanUiScenario scenario = testPlanUiScenarioMapper.selectByPrimaryKey(id);
        if (scenario != null) {
            UiScenarioWithBLOBs testCase = uiScenarioMapper.selectByPrimaryKey(scenario.getUiScenarioId());
            TestPlan testPlan = testPlanService.get(scenario.getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testPlan.getProjectId(), testCase.getName(), scenario.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanUiScenario> nodes = testPlanUiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            UiScenarioExample scenarioExample = new UiScenarioExample();
            scenarioExample.createCriteria().andIdIn(nodes.stream().map(TestPlanUiScenario::getUiScenarioId).collect(Collectors.toList()));
            List<UiScenario> scenarios = uiScenarioMapper.selectByExample(scenarioExample);
            if (CollectionUtils.isNotEmpty(scenarios)) {
                List<String> names = scenarios.stream().map(UiScenario::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), scenarios.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public TestPlanUiScenario get(String id) {
        return testPlanUiScenarioMapper.selectByPrimaryKey(id);
    }

    public Boolean hasFailCase(String planId, List<String> automationIds) {
        if (CollectionUtils.isEmpty(automationIds)) {
            return false;
        }
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andUiScenarioIdIn(automationIds)
                .andLastResultEqualTo("Fail");
        return testPlanUiScenarioMapper.countByExample(example) > 0 ? true : false;
    }

    public List<String> getUiReportStatusList(Map config) {
        List<String> statusList = new ArrayList<>();
        if (ServiceUtils.checkConfigEnable(config, "ui", "all")) {
            return statusList;
        }
        if (ServiceUtils.checkConfigEnable(config, "ui", "failure")) {
            statusList.add(TestPlanUiResultStatus.Error.name());
        }
        if (ServiceUtils.checkConfigEnable(config, "ui", "unExecute")) {
            statusList.add(TestPlanUiResultStatus.UnExecute.name());
        }
        return statusList.size() > 0 ? statusList : null;
    }

    public void buildUiScenarioResponse(List<TestPlanUiScenarioDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            cases.forEach((item) -> {
                item.setResponse(uiScenarioReportService.get(item.getReportId(), true));
            });
        }
    }

    public UiPlanReportDTO buildUiReport(UiPlanReportRequest request) {
        Map config = request.getConfig();
        TestPlanExecuteReportDTO testPlanExecuteReportDTO = request.getTestPlanExecuteReportDTO();
        Boolean saveResponse = request.getSaveResponse();
        String planId = request.getPlanId();
        UiPlanReportDTO report = new UiPlanReportDTO();
        if (ServiceUtils.checkConfigEnable(config, "ui")) {
            List<TestPlanUiScenarioDTO> allCases;
            List<String> statusList = getUiReportStatusList(config);
            if (statusList != null) {
                // 不等于null，说明配置了用例，根据配置的状态查询用例
                if (testPlanExecuteReportDTO != null) {
                    allCases = getAllCases(testPlanExecuteReportDTO.getTestPlanUiScenarioIdAndReportIdMap(), testPlanExecuteReportDTO.getUiScenarioInfoDTOMap());
                } else {
                    allCases = getAllCasesByStatusList(planId, statusList);
                }
                report.setUiAllCases(allCases);
                if (saveResponse) {
                    buildUiScenarioResponse(allCases);
                }

                if (ServiceUtils.checkConfigEnable(config, "ui", "failure")) {
                    List<TestPlanUiScenarioDTO> failureCases = null;
                    if (!CollectionUtils.isEmpty(allCases)) {
                        failureCases = allCases.stream()
                                .filter(i -> StringUtils.isNotBlank(i.getStatus())
                                        && StringUtils.equalsAnyIgnoreCase(i.getStatus(), "Error"))
                                .collect(Collectors.toList());
                    }
                    report.setUiFailureCases(failureCases);
                }
            }
        }
        return report;
    }

    private int getUnderwayStepsCounts(List<String> underwayIds) {
        if (CollectionUtils.isNotEmpty(underwayIds)) {
            List<Integer> underwayStepsCounts = extTestPlanUiScenarioCaseMapper.getUnderwaySteps(underwayIds);
            return underwayStepsCounts.stream().filter(Objects::nonNull).reduce(0, Integer::sum);
        }
        return 0;
    }

    private void calculateScenarioResultDTO(PlanReportCaseDTO item,
                                            TestPlanScenarioStepCountDTO stepCount) {
        if (StringUtils.isNotBlank(item.getReportId())) {
            ScenarioReportResultWrapper uiScenarioReportResult = uiScenarioReportService.get(item.getReportId(), false);

            if (uiScenarioReportResult != null) {
                String content = uiScenarioReportResult.getContent();
                if (StringUtils.isNotBlank(content)) {
                    Map jsonObject = JSON.parseMap(content);

                    stepCount.setScenarioStepSuccess(stepCount.getScenarioStepSuccess() + (Integer) jsonObject.get("scenarioStepSuccess"));
                    stepCount.setScenarioStepError(stepCount.getScenarioStepError() + (Integer) jsonObject.get("scenarioStepError"));
                    stepCount.setScenarioStepErrorReport(stepCount.getScenarioStepErrorReport() + (Integer) jsonObject.get("scenarioStepErrorReport"));

                    if (!StringUtils.equalsIgnoreCase("STOP", uiScenarioReportResult.getStatus())) {
                        stepCount.setScenarioStepTotal(stepCount.getScenarioStepTotal() + (Integer) jsonObject.get("scenarioStepTotal"));
                        stepCount.setScenarioStepUnExecute(stepCount.getScenarioStepUnExecute() + (Integer) jsonObject.get("scenarioStepUnExecuteReport"));
                    } else {
                        //串行执行的报告 勾选了失败停止 后续的所有场景的未禁用步骤都统计到总数和未执行里面去
                        UiScenarioWithBLOBs stoppedScenario = uiScenarioMapper.selectByPrimaryKey(uiScenarioReportResult.getScenarioId());
                        if (stoppedScenario == null) {
                            TestPlanUiScenario testPlanUiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(uiScenarioReportResult.getScenarioId());
                            stoppedScenario = uiScenarioMapper.selectByPrimaryKey(testPlanUiScenario.getUiScenarioId());
                        }
                        if (stoppedScenario == null) {
                            return;
                        }
                        int totalSteps = getTotalSteps(stoppedScenario);
                        stepCount.setScenarioStepTotal(stepCount.getScenarioStepTotal() + totalSteps);
                        stepCount.setScenarioStepUnExecute(stepCount.getScenarioStepUnExecute() + totalSteps);
                    }
                }
            } else {
                stepCount.getUnderwayIds().add(item.getCaseId());
            }
        } else {
            stepCount.getUnderwayIds().add(item.getCaseId());
        }
    }

    /**
     * 获取一个ui场景所有未禁用的步骤数
     *
     * @param stoppedScenario
     * @return
     */
    private int getTotalSteps(UiScenarioWithBLOBs stoppedScenario) {
        if (StringUtils.isNotBlank(stoppedScenario.getScenarioDefinition())) {
            Map definition = JSON.parseMap(stoppedScenario.getScenarioDefinition());
            if (definition.containsKey("hashTree")) {
                List<Object> hashTree = ((List) definition.get("hashTree"));
                return hashTree.stream()
                        .filter(cmd -> (Boolean) (((Map) cmd).get("enable")))
                        .collect(Collectors.toList()).size();
            }
        }
        return 0;
    }

    private void getScenarioCaseReportStatusResultDTO(String status, int count, List<TestCaseReportStatusResultDTO> scenarioCaseList) {
        if (count > 0) {
            TestCaseReportStatusResultDTO scenarioCase = new TestCaseReportStatusResultDTO();
            scenarioCase.setStatus(status);
            scenarioCase.setCount(count);
            scenarioCaseList.add(scenarioCase);
        }
    }

    public List<TestPlanUiScenarioDTO> getAllCasesByStatusList(String planId, List<String> statusList) {
        List<TestPlanUiScenarioDTO> uiTestCases =
                extTestPlanUiScenarioCaseMapper.getPlanUiScenarioByStatusList(planId, statusList);
        return buildCases(uiTestCases);
    }

    public List<TestPlanUiScenarioDTO> getAllCases(Map<String, String> idMap, Map<String, TestPlanUiScenarioDTO> scenarioInfoDTOMap) {
        String defaultStatus = "Error";
        Map<String, String> reportStatus = uiScenarioReportService.getReportStatusByReportIds(idMap.values());
        Map<String, String> savedReportMap = new HashMap<>(idMap);
        List<TestPlanUiScenarioDTO> apiTestCases = new ArrayList<>();
        for (TestPlanUiScenarioDTO dto : scenarioInfoDTOMap.values()) {
            String reportId = savedReportMap.get(dto.getId());
            savedReportMap.remove(dto.getId());
            dto.setReportId(reportId);
            if (StringUtils.isNotEmpty(reportId)) {
                String status = reportStatus.get(reportId);
                if (status == null) {
                    status = defaultStatus;
                }
                dto.setLastResult(status);
                dto.setStatus(status);
            }
            apiTestCases.add(dto);
        }
        return buildCases(apiTestCases);
    }

    public List<TestPlanUiScenarioDTO> buildCases(List<TestPlanUiScenarioDTO> apiTestCases) {
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildProjectInfo(apiTestCases);
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public TestPlanUiScenario selectByReportId(String reportId) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<TestPlanUiScenario> testPlanApiScenarios = testPlanUiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testPlanApiScenarios)) {
            return testPlanApiScenarios.get(0);
        }
        return null;
    }

    public String getProjectIdById(String testPlanScenarioId) {
        return extTestPlanUiScenarioCaseMapper.getProjectIdById(testPlanScenarioId);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanUiScenario.class, TestPlanUiScenarioMapper.class,
                extTestPlanUiScenarioCaseMapper::selectPlanIds,
                extTestPlanUiScenarioCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanUiScenario.class,
                testPlanUiScenarioMapper::selectByPrimaryKey,
                extTestPlanUiScenarioCaseMapper::getPreOrder,
                extTestPlanUiScenarioCaseMapper::getLastOrder,
                testPlanUiScenarioMapper::updateByPrimaryKeySelective);
    }

    public List<String> relevanceListIds(UiScenarioRequest request) {
        request.setNotInTestPlan(true);
        if (request.getAllowedRepeatCase()) {
            request.setNotInTestPlan(false);
        }
        return uiAutomationService.list(request).stream().map(UiScenarioDTO::getId).collect(Collectors.toList());
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return extTestPlanUiScenarioCaseMapper.selectForPlanReport(planId);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanUiScenarioExample testPlanUiScenarioExample = new TestPlanUiScenarioExample();
            testPlanUiScenarioExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanUiScenario> uiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(testPlanUiScenarioExample);
            TestPlanUiScenarioMapper uiScenarioMapper = sqlSession.getMapper(TestPlanUiScenarioMapper.class);
            if (!CollectionUtils.isEmpty(uiScenarios)) {
                Long nextScenarioOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanUiScenarioCaseMapper::getLastOrder);
                for (TestPlanUiScenario uiScenario : uiScenarios) {
                    TestPlanUiScenario planScenario = new TestPlanUiScenario();
                    planScenario.setId(UUID.randomUUID().toString());
                    planScenario.setTestPlanId(targetPlanId);
                    planScenario.setUiScenarioId(uiScenario.getUiScenarioId());
                    planScenario.setEnvironment(uiScenario.getEnvironment());
                    if (uiScenario.getEnvironmentType() != null) {
                        planScenario.setEnvironmentType(uiScenario.getEnvironmentType());
                    }
                    planScenario.setCreateTime(System.currentTimeMillis());
                    planScenario.setUpdateTime(System.currentTimeMillis());
                    planScenario.setCreateUser(SessionUtils.getUserId());
                    planScenario.setOrder(nextScenarioOrder);
                    nextScenarioOrder += 5000;
                    uiScenarioMapper.insert(planScenario);
                }
            }
            sqlSession.flushStatements();
        }
    }

    public boolean haveUiCase(String planId) {
        if (StringUtils.isBlank(planId)) {
            return false;
        }

        List<String> ids = new ArrayList<>();
        ids.add(planId);
        List<TestPlanUiScenario> testPlanUiScenarios = extTestPlanUiScenarioCaseMapper.selectPlanByIdsAndStatusIsNotTrash(ids);
        return !CollectionUtils.isEmpty(testPlanUiScenarios);
    }

    private List<String> convertStatus(List<String> status) {
        List<String> resultStatus = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(status)) {
            status.forEach(i -> {
                if (UN_EXECUTE.equals(i)) {
                    resultStatus.add(TestPlanStatus.Prepare.name());
                } else {
                    resultStatus.add(i);
                }
            });
        }
        return resultStatus;
    }

    public Boolean isExecuting(String planId) {
        return !extTestPlanUiScenarioCaseMapper.selectLegalDataByTestPlanId(planId).isEmpty();
    }

    public List<TestPlanUiScenarioDTO> getFailureListByIds(Set<String> planApiCaseIds) {
        return extTestPlanUiScenarioCaseMapper.getFailureListByIds(planApiCaseIds, null);
    }

    public List<ModuleNodeDTO> getNodeByPlanId(List<String> projectIds, String planId) {
        List<ModuleNodeDTO> list = new ArrayList<>();
        projectIds.forEach(id -> {
            Project project = baseProjectService.getProjectById(id);
            String name = project.getName();
            List<ModuleNodeDTO> nodeList = getNodeDTO(id, planId);
            ModuleNodeDTO apiModuleDTO = new ModuleNodeDTO();
            apiModuleDTO.setId(project.getId());
            apiModuleDTO.setName(name);
            apiModuleDTO.setLabel(name);
            apiModuleDTO.setChildren(nodeList);
            if (!org.springframework.util.CollectionUtils.isEmpty(nodeList)) {
                list.add(apiModuleDTO);
            }
        });
        return list;
    }

    private List<ModuleNodeDTO> getNodeDTO(String projectId, String planId) {
        List<TestPlanUiScenario> relatedScenarios = uiScenarioModuleService.getUiScenarioByPlanId(planId);
        if (relatedScenarios.isEmpty()) {
            return null;
        }
        List<ModuleNodeDTO> testCaseNodes = uiScenarioModuleService.getNodeTreeByProjectId(projectId, UiScenarioType.SCENARIO.getType());

        List<UiScenario> scenarios = uiAutomationService.selectByIds(relatedScenarios.stream()
                .map(TestPlanUiScenario::getUiScenarioId)
                .collect(Collectors.toList()));

        List<String> dataNodeIds = scenarios.stream()
                .filter(uiScenario -> uiScenario.getStatus() == null || !CommonConstants.TrashStatus.equals(uiScenario.getStatus()))
                .map(UiScenario::getModuleId)
                .collect(Collectors.toList());

        List<ModuleNodeDTO> nodeTrees = uiScenarioModuleService.getNodeTrees(testCaseNodes);

        Iterator<ModuleNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            ModuleNodeDTO rootNode = iterator.next();
            if (uiScenarioModuleService.pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }

    public TestPlanScenarioStepCountSimpleDTO getStepCount(List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanScenarioStepCountDTO stepCount = new TestPlanScenarioStepCountDTO();
        for (PlanReportCaseDTO item : planReportCaseDTOS) {
            calculateScenarioResultDTO(item, stepCount);
        }
        int underwayStepsCounts = getUnderwayStepsCounts(stepCount.getUnderwayIds());
        TestPlanScenarioStepCountSimpleDTO stepResult = new TestPlanScenarioStepCountSimpleDTO();
        stepResult.setStepCount(stepCount);
        stepResult.setUnderwayStepsCounts(underwayStepsCounts);
        return stepResult;
    }

    public void relevanceByTestIds(String planId, List<String> ids) {
        Long nextLoadOrder = ServiceUtils.getNextOrder(planId, extTestPlanUiScenarioCaseMapper::getLastOrder);
        for (String id : ids) {
            TestPlanUiScenario t = new TestPlanUiScenario();
            t.setId(UUID.randomUUID().toString());
            t.setTestPlanId(planId);
            t.setUiScenarioId(id);
            t.setCreateTime(System.currentTimeMillis());
            t.setUpdateTime(System.currentTimeMillis());
            t.setOrder(nextLoadOrder);
            nextLoadOrder += 5000;

            TestPlanUiScenarioExample testPlanUiScenarioExample = new TestPlanUiScenarioExample();
            testPlanUiScenarioExample.createCriteria().andTestPlanIdEqualTo(planId).andUiScenarioIdEqualTo(t.getUiScenarioId());
            if (testPlanUiScenarioMapper.countByExample(testPlanUiScenarioExample) <= 0) {
                testPlanUiScenarioMapper.insert(t);
            }
        }
    }

    public UiScenario getScenarioId(String planScenarioId) {
        TestPlanUiScenario planApiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(planScenarioId);
        if (planApiScenario != null) {
            return uiScenarioMapper.selectByPrimaryKey(planApiScenario.getUiScenarioId());
        } else {
            return uiScenarioMapper.selectByPrimaryKey(planScenarioId);
        }
    }

    public List<UiScenarioReportWithBLOBs> selectExtForPlanReport(String planId) {
        return uiScenarioReportService.selectExtForPlanReport(planId);
    }

    public Map<String, String> getScenarioCaseEnv(Map<String, String> map) {
        Set<String> set = map.keySet();
        HashMap<String, String> envMap = new HashMap<>(16);
        if (set.isEmpty()) {
            return envMap;
        }
        for (String projectId : set) {
            String envId = map.get(projectId);
            if (StringUtils.isBlank(envId)) {
                continue;
            }
            Project project = baseProjectService.getProjectById(projectId);
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
            if (project == null || environment == null) {
                continue;
            }
            String projectName = project.getName();
            String envName = environment.getName();
            if (StringUtils.isBlank(projectName) || StringUtils.isBlank(envName)) {
                continue;
            }
            envMap.put(projectName, envName);
        }
        return envMap;
    }

    public Map<String, List<String>> getUiScenarioEnv(String planId) {
        TestPlanUiScenarioExample scenarioExample = new TestPlanUiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanUiScenario> testPlanApiScenarios = testPlanUiScenarioMapper.selectByExample(scenarioExample);
        List<String> scenarioIds = testPlanApiScenarios.stream().map(TestPlanUiScenario::getId).collect(Collectors.toList());
        return getUiScenarioEnv(scenarioIds);
    }

    public Map<String, List<String>> getUiScenarioEnv(List<String> planApiScenarioIds) {
        Map<String, List<String>> envMap = new HashMap<>();
        if (CollectionUtils.isEmpty(planApiScenarioIds)) {
            return envMap;
        }

        TestPlanUiScenarioExample scenarioExample = new TestPlanUiScenarioExample();
        scenarioExample.createCriteria().andIdIn(planApiScenarioIds);
        List<TestPlanUiScenario> testPlanApiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);

        for (TestPlanUiScenario TestPlanUiScenario : testPlanApiScenarios) {
            String env = TestPlanUiScenario.getEnvironment();
            if (StringUtils.isBlank(env)) {
                continue;
            }
            Map<String, String> map = JSON.parseObject(env, Map.class);
            if (!map.isEmpty()) {
                Set<String> set = map.keySet();
                for (String projectId : set) {
                    String envInProject = map.get(projectId);
                    if (envMap.containsKey(projectId) && StringUtils.isNotEmpty(envInProject)) {
                        List<String> list = envMap.get(projectId);
                        if (!list.contains(envInProject)) {
                            list.add(envInProject);
                        }
                    } else {
                        List<String> envs = new ArrayList<>();
                        if (StringUtils.isNotBlank(envInProject)) {
                            envs.add(envInProject);
                        }
                        envMap.put(projectId, envs);
                    }
                }
            }
        }
        return envMap;
    }

    public List<String> getPlanProjectIds(String planId) {
        TestPlanUiScenarioExample testPlanUiScenarioExample = new TestPlanUiScenarioExample();
        testPlanUiScenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanUiScenario> testPlanUiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(testPlanUiScenarioExample);
        if (CollectionUtils.isNotEmpty(testPlanUiScenarios)) {
            List<String> scenarioIds = testPlanUiScenarios.stream().map(t -> t.getUiScenarioId()).collect(Collectors.toList());
            UiScenarioExample scenarioExample = new UiScenarioExample();
            scenarioExample.createCriteria().andIdIn(scenarioIds);
            List<UiScenario> scenarios = uiScenarioMapper.selectByExample(scenarioExample);
            return scenarios.stream().map(s -> s.getProjectId()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<String> getEnvProjectIds(String planId) {
        TestPlanUiScenarioExample testPlanUiScenarioExample = new TestPlanUiScenarioExample();
        testPlanUiScenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanUiScenario> testPlanUiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(testPlanUiScenarioExample);
        List<String> projectIds = new ArrayList<>();
        for (TestPlanUiScenario testPlanApiScenario : testPlanUiScenarios) {
            if (StringUtils.isNotEmpty(testPlanApiScenario.getEnvironment())) {
                Map<String, String> envMap = JSON.parseMap(testPlanApiScenario.getEnvironment());
                envMap.forEach((k, v) -> {
                    if (StringUtils.isNotEmpty(k) && !projectIds.contains(k)) {
                        projectIds.add(k);
                    }
                });
            }
        }
        return projectIds;
    }

    public RunModeConfigDTO setScenarioEnv(RunModeConfigDTO runModeConfig, String planId) {
        TestPlanUiScenarioExample scenarioExample = new TestPlanUiScenarioExample();
        scenarioExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanUiScenario> testPlanApiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);
        List<String> planScenarioIds = testPlanApiScenarios.stream().map(TestPlanUiScenario::getId).collect(Collectors.toList());
        setScenarioEnv(testPlanApiScenarios, planScenarioIds, runModeConfig);
        return runModeConfig;
    }

    public void setScenarioEnv(List<TestPlanUiScenario> testPlanUiScenarios, List<String> planScenarioIds, RunModeConfigDTO runModeConfig) {
        if (CollectionUtils.isEmpty(planScenarioIds)) return;

        if (CollectionUtils.isEmpty(testPlanUiScenarios)) {
            TestPlanUiScenarioExample testPlanApiScenarioExample = new TestPlanUiScenarioExample();
            testPlanApiScenarioExample.createCriteria().andIdIn(planScenarioIds);
            testPlanUiScenarios = testPlanUiScenarioMapper.selectByExampleWithBLOBs(testPlanApiScenarioExample);
        }
        if (CollectionUtils.isEmpty(planScenarioIds)) {
            return;
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanUiScenarioMapper mapper = sqlSession.getMapper(TestPlanUiScenarioMapper.class);

        String environmentType = runModeConfig.getEnvironmentType();

        if (StringUtils.equals(environmentType, EnvironmentTypeEnum.JSON.toString())) {
            Map<String, String> envMap = runModeConfig.getEnvMap();
            for (TestPlanUiScenario testPlanUiScenario : testPlanUiScenarios) {
                String env = testPlanUiScenario.getEnvironment();
                if (StringUtils.isBlank(env)) {
                    if (envMap != null && !envMap.isEmpty()) {
                        env = JSON.toJSONString(envMap);
                    }
                } else {
                    Map<String, String> existMap = JSON.parseObject(env, Map.class);
                    if (existMap.isEmpty()) {
                        if (envMap != null && !envMap.isEmpty()) {
                            env = JSON.toJSONString(envMap);
                        }
                    }
                }
                Map<String, String> map = JSON.parseObject(env, Map.class);
                if (map.isEmpty()) {
                    continue;
                }

                Set<String> set = map.keySet();
                for (String s : set) {
                    if (StringUtils.isNotBlank(envMap.get(s))) {
                        map.put(s, envMap.get(s));
                    }
                }
                String envJsonStr = JSON.toJSONString(map);
                if (!StringUtils.equals(envJsonStr, testPlanUiScenario.getEnvironment())) {
                    testPlanUiScenario.setEnvironmentType(EnvironmentTypeEnum.JSON.toString());
                    testPlanUiScenario.setEnvironment(JSON.toJSONString(map));
                    mapper.updateByPrimaryKeyWithBLOBs(testPlanUiScenario);
                }
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }

    }

    public TestPlanEnvInfoDTO generateEnvironmentInfo(TestPlanReport testPlanReport) {
        TestPlanEnvInfoDTO testPlanEnvInfoDTO = new TestPlanEnvInfoDTO();
        TestPlanReportRunInfoDTO runInfoDTO = null;
        if (StringUtils.isNotEmpty(testPlanReport.getRunInfo())) {
            try {
                runInfoDTO = JSON.parseObject(testPlanReport.getRunInfo(), TestPlanReportRunInfoDTO.class);
            } catch (Exception e) {
                LogUtil.error("解析测试计划报告记录的运行环境信息[" + testPlanReport.getRunInfo() + "]时出错!", e);
            }
        }
        if (runInfoDTO != null) {
            testPlanEnvInfoDTO.setRunMode(StringUtils.equalsIgnoreCase(runInfoDTO.getRunMode(), "serial") ? Translator.get("serial") : Translator.get("parallel"));
            Map<String, Set<String>> projectEnvMap = new LinkedHashMap<>();
            if (MapUtils.isNotEmpty(runInfoDTO.getUiScenarioRunInfo())) {
                this.setScenarioProjectEnvMap(projectEnvMap, runInfoDTO.getUiScenarioRunInfo());
            }
            Map<String, List<String>> showProjectEnvMap = new LinkedHashMap<>();
            for (Map.Entry<String, Set<String>> entry : projectEnvMap.entrySet()) {
                String projectId = entry.getKey();
                Set<String> envIdSet = entry.getValue();
                Project project = baseProjectService.getProjectById(projectId);
                String projectName = project == null ? null : project.getName();
                List<String> envNames = CommonBeanFactory.getBean(BaseEnvironmentService.class).selectNameByIds(envIdSet);
                if (StringUtils.isNotEmpty(projectName) && CollectionUtils.isNotEmpty(envNames)) {
                    showProjectEnvMap.put(projectName, envNames);
                }
            }
            if (MapUtils.isNotEmpty(showProjectEnvMap)) {
                testPlanEnvInfoDTO.setProjectEnvMap(showProjectEnvMap);
            }
        }
        return testPlanEnvInfoDTO;
    }

    private void setScenarioProjectEnvMap(Map<String, Set<String>> projectEnvMap, Map<String, Map<String, List<String>>> caseEnvironmentMap) {
        if (MapUtils.isEmpty(caseEnvironmentMap)) {
            return;
        }
        for (Map<String, List<String>> map : caseEnvironmentMap.values()) {
            if (MapUtils.isEmpty(map)) {
                continue;
            }
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String projectId = entry.getKey();
                List<String> envIdList = entry.getValue();
                if (CollectionUtils.isNotEmpty(envIdList)) {
                    envIdList.forEach(envId -> {
                        if (projectEnvMap.containsKey(projectId)) {
                            projectEnvMap.get(projectId).add(envId);
                        } else {
                            projectEnvMap.put(projectId, new LinkedHashSet<>() {{
                                this.add(envId);
                            }});
                        }
                    });
                }
            }
        }
    }

    public Map<String, List<String>> getPlanProjectEnvMap(List<String> resourceIds) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (!com.alibaba.nacos.common.utils.CollectionUtils.isEmpty(resourceIds)) {
            Map<String, List<String>> projectEnvMap = uiScenarioEnvService.selectProjectEnvMapByTestPlanScenarioIds(resourceIds);
            this.setProjectEnvMap(result, projectEnvMap);
        }
        return result;
    }

    public void setProjectEnvMap(Map<String, List<String>> result, Map<String, List<String>> projectEnvMap) {
        if (MapUtils.isNotEmpty(projectEnvMap)) {
            for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                String projectName = entry.getKey();
                List<String> envNameList = entry.getValue();
                if (result.containsKey(projectName)) {
                    envNameList.forEach(envName -> {
                        if (!result.get(projectName).contains(envName)) {
                            result.get(projectName).add(envName);
                        }
                    });
                } else {
                    result.put(projectName, envNameList);
                }
            }
        }
    }
}
