package io.metersphere.service;


import com.google.common.collect.Maps;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.TestPlanUiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanUiScenarioCaseMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.*;
import io.metersphere.dto.api.UiScenarioReportInitDTO;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.dto.testcase.UiRunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.hashtree.MsUiScenario;
import io.metersphere.i18n.Translator;
import io.metersphere.jmeter.JMeterService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import io.metersphere.utils.UiGlobalConfigUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioExecuteService {

    @Resource
    private ExtUiScenarioMapper extUiScenarioMapper;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private UiExecutionQueueService uiExecutionQueueService;
    @Resource
    private UiScenarioReportStructureService uiScenarioReportStructureService;
    @Resource
    private UiScenarioSerialService uiScenarioSerialService;
    @Resource
    private UiScenarioParallelService uiScenarioParallelService;
    @Resource
    private UiScenarioReportService uiScenarioReportService;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ExtTestPlanUiScenarioCaseMapper extTestPlanUiScenarioCaseMapper;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private UiEnvironmentService uiEnvironmentService;
    @Resource
    private BaseEnvironmentService baseEnvironmentService;
    @Resource
    private UiScenarioEnvService uiScenarioEnvService;

    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("Ui Scenario run-执行脚本装载-接收到场景执行参数：【 " + JSON.toJSONString(request) + " 】");
        }
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ReportTriggerMode.MANUAL.name());
        }
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }

        // 生成集成报告
        String serialReportId = null;
        LoggerUtil.info("Scenario run-执行脚本装载-根据条件查询所有场景 ");

        List<UiScenarioWithBLOBs> uiScenarios = this.getScenario(request);
        // 只有一个场景且没有测试步骤，则提示
        if (uiScenarios != null && uiScenarios.size() == 1 && (uiScenarios.get(0).getStepTotal() == null || uiScenarios.get(0).getStepTotal() == 0)) {
            MSException.throwException((uiScenarios.get(0).getName() + "，" + Translator.get("automation_exec_info")));
        }

        // 集合报告设置
        if (UiGenerateHashTreeUtil.isSetReport(request.getConfig())) {
            if (isSerial(request)) {
                request.setExecuteType(ExecuteType.Completed.name());
            } else {
                request.setExecuteType(ExecuteType.Marge.name());
            }
            serialReportId = UUID.randomUUID().toString();
            request.setReportId(serialReportId);
        }

        for (UiScenarioWithBLOBs uiScenario : uiScenarios) {
            try {
                this.setScenarioConfig(uiScenario, request);
            } catch (Exception e) {
                LogUtil.error(e, e.getMessage());
                MSException.throwException("UI"+uiScenario.getName()+"设置场景环境失败，场景ID： " + uiScenario.getId());
            }
        }

        Map<String, RunModeDataDTO> executeQueue = new LinkedHashMap<>();

        LoggerUtil.info("Ui Scenario run-执行脚本装载-初始化执行队列");
        if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.UI_SCENARIO_PLAN.name(),
                ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name())) {
            //测试计划执行
            assemblyPlanScenario(uiScenarios, request, executeQueue);
        } else {
            // 按照场景执行
            assemblyScenario(uiScenarios, request, executeQueue,serialReportId);
        }

        LoggerUtil.info("Ui Scenario run-执行脚本装载-初始化执行队列完成：" + executeQueue.size());

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        if (executeQueue.isEmpty()) {
            return responseDTOS;
        }
        if (UiGenerateHashTreeUtil.isSetReport(request.getConfig())) {
            setReport(request, serialReportId, uiScenarios,  responseDTOS);
        }
        String reportType = request.getConfig().getReportType();
        String planReportId = StringUtils.equalsIgnoreCase(reportType,RunModeConstants.SET_REPORT.toString()) ? serialReportId  : request.getTestPlanReportId();

        // 生成执行队列
        DBTestQueue executionQueue = uiExecutionQueueService.add(executeQueue, request.getConfig().getResourcePoolId()
                , ApiRunMode.UI_SCENARIO.name(), planReportId, reportType, request.getRunMode(), request.getConfig());

        // 预生成报告
        uiScenarioReportService.batchSave(executeQueue, serialReportId, request.getRunMode(), responseDTOS);

        // 开始执行
        execute(request, serialReportId, executeQueue, executionQueue);

        return responseDTOS;
    }

    private void setReport(RunScenarioRequest request, String serialReportId, List<UiScenarioWithBLOBs> uiScenarios, List<MsExecResponseDTO> responseDTOS) {
        LoggerUtil.info("Ui Scenario run-执行脚本装载-初始化集成报告：" + serialReportId);
        request.getConfig().setReportId(UUID.randomUUID().toString());
        List<String> scenarioIds = new ArrayList<>();
        String reportScenarioIds = generateScenarioIds(scenarioIds);

        //将集合报告的环境内容整合起来
        if (MapUtils.isEmpty(request.getConfig().getEnvMap())) {
            RunModeConfigWithEnvironmentDTO runModeConfig = new RunModeConfigWithEnvironmentDTO();
            BeanUtils.copyBean(runModeConfig, request.getConfig());
            Map<String, List<String>> projectEnvMap = uiScenarioEnvService.selectUiScenarioEnv(uiScenarios);
            if (MapUtils.isNotEmpty(projectEnvMap)) {
                runModeConfig.setExecutionEnvironmentMap(projectEnvMap);
            }
            request.setConfig(runModeConfig);
        }
        // 生成集合报告
        String names = uiScenarios.stream().map(UiScenarioWithBLOBs::getName).collect(Collectors.joining(","));
        ScenarioReportResultWrapper report = uiScenarioReportService.getUiScenarioReportResult(request, serialReportId, names, reportScenarioIds);
        report.setVersionId(uiScenarios.get(0).getVersionId());

        report.setReportType(ReportTypeConstants.UI_INTEGRATED.name());

        uiScenarioReportMapper.insert(report);

        responseDTOS.add(new MsExecResponseDTO(JSON.toJSONString(scenarioIds), serialReportId, request.getRunMode()));
        uiScenarioReportStructureService.saveUiScenarios(uiScenarios, serialReportId, request.getConfig().getReportType());
    }

    protected String generateScenarioIds(List<String> scenarioIds) {
        return JSON.toJSONString(CollectionUtils.isNotEmpty(scenarioIds) && scenarioIds.size() > 50 ? scenarioIds.subList(0, 50) : scenarioIds);
    }

    protected boolean isSerial(RunScenarioRequest request) {
        return request.getConfig() != null &&
                StringUtils.equals(request.getConfig().getMode(), RunModeConstants.SERIAL.toString());
    }

    public void setScenarioConfig(UiScenarioWithBLOBs uiScenario, RunScenarioRequest request) {
        if (uiScenario == null) {
            return;
        }
        UiRunModeConfigDTO config = (UiRunModeConfigDTO) request.getConfig();
        String definition = uiScenario.getScenarioDefinition();
        definition = UiGenerateHashTreeUtil.convertJacksonType(definition);
        MsUiScenario scenario = JSON.parseObject(definition, MsUiScenario.class);
        UiGenerateHashTreeUtil.parse(definition, scenario);

        scenario.setBrowser(scenario.getBrowserByName(config.getBrowser()));
        scenario.setHeadlessEnabled(config.isHeadlessEnabled());
        if (StringUtils.isNotEmpty(request.getReportId())) {
            config.setReportId(request.getReportId());
        }
        if (request != null && request.getConfig() != null && request.getConfig().getEnvMap() != null && !request.getConfig().getEnvMap().isEmpty()) {
            scenario.setEnvironmentMap(request.getConfig().getEnvMap());
        }
        uiScenario.setScenarioDefinition(JSON.toJSONString(scenario));
    }

    public String debug(RunDefinitionRequest request) {
        //检查并同步的更新 cookie (屏蔽 免登录)
//        checkAndUpdateCookie(request);
        ParameterConfig config = new ParameterConfig();
        config.setScenarioId(request.getScenarioId());
        config.setRunLocal(request.isRunLocal());
        config.setReportId(request.getReportId());
        String lang = StringUtils.isNotEmpty(request.getBrowserLanguage()) ? request.getBrowserLanguage() : "zh-CN";
        config.setBrowserLanguage(lang);
        LogUtil.info(String.format("run debug config detail : %s", JSON.toJSONString(config)));

        RunModeConfigDTO configDTO = new RunModeConfigDTO();
        BeanUtils.copyBean(configDTO, Optional.ofNullable(request.getConfig()).orElse(new RunModeConfigDTO()));
        //初始化全局运行环境， 当前debug模式可能并没有完全保存， 故单独处理
        if (MapUtils.isNotEmpty(request.getEnvironmentMap())) {
            //获取 当前环境信息
            Map<String, EnvironmentConfig> envConfig = Maps.newConcurrentMap();
            request.getEnvironmentMap().entrySet().forEach(entry -> {
                if (StringUtils.isNotBlank(entry.getValue())) {
                    envConfig.put(entry.getKey(), CommonBeanFactory.getBean(UiEnvironmentService.class).replaceDomainVars(CommonBeanFactory.getBean(UiEnvironmentService.class).convertCnfToClazz(entry.getValue())));
                }
            });
            config.setConfig(envConfig);
            configDTO.setEnvMap(request.getEnvironmentMap());

        } else {
            UiScenarioWithBLOBs uiScenarioWithBLOBs = uiScenarioMapper.selectByPrimaryKey(request.getScenarioId());
            if (uiScenarioWithBLOBs != null && StringUtils.isNotBlank(uiScenarioWithBLOBs.getEnvironmentJson())) {
                Map<String, EnvironmentConfig> envConfig = Maps.newConcurrentMap();
                ((Map<String, String>) JSON.parseMap(uiScenarioWithBLOBs.getEnvironmentJson())).entrySet().forEach(entry -> {
                    if (StringUtils.isNotBlank(entry.getValue())) {
                        envConfig.put(entry.getKey(), CommonBeanFactory.getBean(UiEnvironmentService.class).replaceDomainVars(CommonBeanFactory.getBean(UiEnvironmentService.class).convertCnfToClazz(entry.getValue())));
                    }
                });
                config.setConfig(envConfig);
                //设置环境为数据库中场景原来的环境信息
                configDTO.setEnvMap(JSON.parseMap(uiScenarioWithBLOBs.getEnvironmentJson()));
            }
        }

        request.setConfig(configDTO);

        HashTree hashTree = null;
        try {
            if (request.isUseCookie()) {
                UiGlobalConfigUtil.getConfig().setEnableCookieShare(true);
                UiGlobalConfigUtil.getConfig().setCookie(uiEnvironmentService.getCookieConfig(request.getEnvironmentId(), "cookie"));
            }
            hashTree = request.getTestElement().generateHashTree(config);

        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        } finally {
            UiGlobalConfigUtil.removeConfig();
        }

        // 设置执行类型
        if (request.getUiRunMode() != null) {
            if (request.getUiRunMode().equals(SystemConstants.UiRunModeEnum.LOCAL.getName())) {
                request.setExecuteType(ExecuteType.Debug.name());
            }
            if (request.getUiRunMode().equals(SystemConstants.UiRunModeEnum.SERVER.getName())) {
                request.setExecuteType(ExecuteType.Debug.name());
            }
            // 生成报告方式
            if (request.getUiRunMode().equals(SystemConstants.UiRunModeEnum.REPORT.getName())) {
                request.setExecuteType(ExecuteType.Saved.name());
            }
        }
        // 本地调试和后端运行的模式也需要入库，以便清除累积截图
        if (StringUtils.isEmpty(request.getExecuteType())) {
            request.setExecuteType(ExecuteType.Saved.name());
        }
        ScenarioReportResultWrapper report = uiScenarioReportService.init(new UiScenarioReportInitDTO(request.getId(),
                request.getScenarioId(),
                request.getScenarioName(),
                ReportTriggerMode.MANUAL.name(),
                request.getExecuteType(),
                request.getProjectId(),
                SessionUtils.getUserId(),
                request.getConfig(), null), request.getEnvironmentId());

        report.setReportType(ReportTypeConstants.UI_INDEPENDENT.name());
        report.setType(SystemConstants.DataOriginEnum.NEW.value());

        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(request.getScenarioId());
        String reportType = request.getConfig() != null ? request.getConfig().getReportType() : null;
        if (scenario != null) {
            report.setVersionId(scenario.getVersionId());
            String scenarioDefinition = JSON.toJSONString(request.getTestElement().getHashTree().get(0).getHashTree().get(0));
            scenario.setScenarioDefinition(scenarioDefinition);
            uiScenarioReportStructureService.save(scenario, report.getId(), reportType);
        } else {
            if (request.getTestElement() != null && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree())) {
                UiScenarioWithBLOBs uiScenario = new UiScenarioWithBLOBs();
                uiScenario.setId(request.getScenarioId());
                MsTestElement testElement = request.getTestElement().getHashTree().get(0).getHashTree().get(0);
                if (testElement != null) {
                    uiScenario.setName(testElement.getName());
                    uiScenario.setScenarioDefinition(JSON.toJSONString(testElement));
                    uiScenarioReportStructureService.save(uiScenario, report.getId(), reportType);
                }
            }
        }
        uiScenarioReportMapper.insert(report);
        String runMode = StringUtils.isEmpty(request.getRunMode()) ? ApiRunMode.UI_SCENARIO.name() : request.getRunMode();
        // 调用执行方法
        UiJmeterRunRequestDTO runRequest = new UiJmeterRunRequestDTO(request.getId(), request.getId(), runMode, hashTree);
        runRequest.setDebug(true);
        jMeterService.run(runRequest);
        return request.getId();
    }

    /**
     * 检查并同步的更新 cookie(因为当前绝大部分网站不支持cookie登录先屏蔽，后期尝试使用其他技术方案绕过)
     *
     * @param request
     */
    private void checkAndUpdateCookie(RunDefinitionRequest request) {
        if (request.isUseCookie()) {
            if (MapUtils.isEmpty(request.getEnvironmentMap())) {
                MSException.throwException(Translator.get("pls_select_env"));
            }
            //使用 cookie 并且配置了环境
            Map<String, String> environmentMap = request.getEnvironmentMap();
            for (Map.Entry<String, String> entry : environmentMap.entrySet()) {
                ApiTestEnvironment apiTestEnvironment = apiTestEnvironmentMapper.selectByPrimaryKey(entry.getValue());
                if (apiTestEnvironment == null) {
                    MSException.throwException(Translator.get("env_not_exists"));
                }
            }
            String environmentId = environmentMap.get(request.getProjectId());
            if (StringUtils.isNotBlank(environmentId)) {
                if (uiEnvironmentService.cookieExpired(environmentId)) {
                    uiScenarioSerialService.runCookie(uiEnvironmentService.getCookieConfig(environmentId, "relevanceId"));
                }
            }
        }
    }

    protected void execute(RunScenarioRequest request, String
            serialReportId, Map<String, RunModeDataDTO> executeQueue, DBTestQueue executionQueue) {
        String finalSerialReportId = serialReportId;
        Thread thread = new Thread(() ->
        {
            Thread.currentThread().setName("SCENARIO-PARALLEL-THREAD");
            if (isSerial(request)) {
                uiScenarioSerialService.serial(executionQueue, executionQueue.getDetail());
            } else {
                uiScenarioParallelService.parallel(executeQueue, request, finalSerialReportId, executionQueue);
            }
        });
        thread.start();
    }

    /**
     * 测试计划接口场景的预执行（生成场景报告）
     */
    private void assemblyPlanScenario(List<UiScenarioWithBLOBs> apiScenarios, RunScenarioRequest
            request, Map<String, RunModeDataDTO> executeQueue) {
        String reportId = request.getId();
        Map<String, String> planScenarioIdMap = request.getScenarioTestPlanIdMap();
        if (MapUtils.isEmpty(planScenarioIdMap)) {
            return;
        }
        String projectId = request.getProjectId();
        Map<String, UiScenarioWithBLOBs> scenarioMap = apiScenarios.stream().collect(Collectors.toMap(UiScenarioWithBLOBs::getId, Function.identity(), (t1, t2) -> t1));
        for (Map.Entry<String, String> entry : planScenarioIdMap.entrySet()) {
            String testPlanScenarioId = entry.getKey();
            String scenarioId = entry.getValue();
            UiScenarioWithBLOBs scenario = scenarioMap.get(scenarioId);

            TestPlanUiScenario testPlanUiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(testPlanScenarioId);
            if (scenario.getStepTotal() == null || scenario.getStepTotal() == 0 || testPlanUiScenario == null) {
                continue;
            }

            // 获取场景用例单独地执行环境
            RunModeConfigDTO runModeConfigDTO = resetEnvInfoForReport(request, testPlanUiScenario.getEnvironment(), scenario,scenario.getProjectId());

             if (StringUtils.isEmpty(projectId)) {
                projectId = extTestPlanUiScenarioCaseMapper.getProjectIdById(testPlanScenarioId);
            }
            if (StringUtils.isEmpty(projectId)) {
                projectId = scenario.getProjectId();
            }

            ScenarioReportResultWrapper report = request.isRerun() ? request.getReportMap().get(scenarioId) :
                    uiScenarioReportService.init(new UiScenarioReportInitDTO(reportId, testPlanScenarioId, scenario.getName(), request.getTriggerMode(),
                            request.getExecuteType(), projectId, request.getReportUserID(), runModeConfigDTO, request.getTestPlanReportId()));
            if (report == null) {
                report = request.getReportMap().get(testPlanScenarioId);
            }
            if (report == null) {
                return;
            }
            report.setVersionId(scenario.getVersionId());
            RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
            //这里的testid 要用测试计划关联id而不是 ui_scenario_id 直接关联
            runModeDataDTO.setTestId(testPlanScenarioId);
            runModeDataDTO.setPlanEnvMap(runModeConfigDTO.getEnvMap());
            runModeDataDTO.setReport(report);
            runModeDataDTO.setReportId(report.getId());
            runModeDataDTO.setUiScenario(scenario);
            executeQueue.put(report.getId(), runModeDataDTO);
            // 生成文档结构
            if (!StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())) {
                uiScenarioReportStructureService.save(scenario, report.getId(), request.getConfig() != null ? request.getConfig().getReportType() : null);
            }
            // 重置报告ID
            reportId = UUID.randomUUID().toString();
        }
    }

    private boolean checkProjectHasEmptyEnv(Map<String, String> planEnvMap) {
        if (MapUtils.isNotEmpty(planEnvMap)) {
            for (Map.Entry<String, String> entry : planEnvMap.entrySet()) {
                if (StringUtils.isEmpty(entry.getValue())) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 重新设置环境信息供报告展示 报告使用的是 scenario_report 中 env_config 字段
     * 该字段使用的类是 RunModeConfigDTO（独立报告，一个项目对应一个环境） 或者 RunModeConfigWithEnvironmentDTO（集合报告，集合报告相比独立报告区别在于一个项目有多个环境）
     *
     * @param request
     * @param environment
     * @param projectId   因为 UI 场景允许不设置环境，那么原始场景没有选择环境，但是批量执行选了环境，势必造成显示的不一致，这种情况就看场景本身的 projectId
     */
    private RunModeConfigDTO resetEnvInfoForReport(RunScenarioRequest request, String environment, UiScenarioWithBLOBs uiScenario,String projectId) {
        Map<String, String> configEnvMap = null;
        if (request.getConfig() != null && MapUtils.isNotEmpty(request.getConfig().getEnvMap())) {
            configEnvMap = request.getConfig().getEnvMap();
        }
        Map<String, String> selfEnvMap = convertJSONToMap(environment);
        if (MapUtils.isEmpty(selfEnvMap)) {
            selfEnvMap = new HashMap<>();
            selfEnvMap.put(projectId, "");
        }else if(this.checkProjectHasEmptyEnv(selfEnvMap) && uiScenario!=null){
            Map<String, List<String>> projectEnvMap = uiScenarioEnvService.selectUiScenarioEnv(new ArrayList<>() {{
                this.add(uiScenario);
            }});
            for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                if (CollectionUtils.isNotEmpty(entry.getValue()) && StringUtils.isEmpty(selfEnvMap.get(entry.getKey()))) {
                    selfEnvMap.put(entry.getKey(), entry.getValue().get(0));
                }
            }
        }
        selfEnvMap = mergeLToRMap(configEnvMap, selfEnvMap);
        RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
        BeanUtils.copyBean(runModeConfigDTO, Optional.ofNullable(request.getConfig()).orElse(new RunModeConfigDTO()));
        runModeConfigDTO.setEnvMap(selfEnvMap);
        return runModeConfigDTO;
    }

    private Map<String, String> mergeLToRMap(Map<String, String> configEnvMap, Map<String, String> selfEnvMap) {
        if (MapUtils.isEmpty(configEnvMap)) {
            return selfEnvMap;
        }
        if (MapUtils.isEmpty(selfEnvMap)) {
            return new HashMap<>();
        }
        selfEnvMap.entrySet().forEach(e -> {
            if (configEnvMap.containsKey(e.getKey())) {
                e.setValue(configEnvMap.get(e.getKey()));
            }
        });
        return selfEnvMap;
    }

    private Map<String, String> convertJSONToMap(String environmentJson) {
        if (StringUtils.isBlank(environmentJson)) {
            return new HashMap<>();
        }
        Map<String, String> resultMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(environmentJson);
            jsonObject.keySet().forEach(k -> {
                resultMap.put(k, jsonObject.optString(k));
            });
        } catch (Exception e) {
        }
        return resultMap;
    }


    private void assemblyScenario(List<UiScenarioWithBLOBs> uiScenarios, RunScenarioRequest
            request, Map<String, RunModeDataDTO> executeQueue, String serialReportId) {
        String reportId = request.getId();
        for (int i = 0; i < uiScenarios.size(); i++) {
            UiScenarioWithBLOBs item = uiScenarios.get(i);
            if (item.getStepTotal() == null || item.getStepTotal() == 0) {
                continue;
            }

            // 获取场景用例单独的执行环境
            RunModeConfigDTO runModeConfigDTO = resetEnvInfoForReport(request, item.getEnvironmentJson(), null, item.getProjectId());

            ScenarioReportResultWrapper report = uiScenarioReportService.init(new UiScenarioReportInitDTO(reportId, item.getId(), item.getName(), request.getTriggerMode(),
                    request.getExecuteType(), item.getProjectId(), request.getReportUserID(), runModeConfigDTO, request.getTestPlanReportId()));
            report.setReportType(ReportTypeConstants.UI_INDEPENDENT.name());
            report.setVersionId(item.getVersionId());

            RunModeDataDTO runModeDataDTO = getRunModeDataDTO(item.getId(), report);
            runModeDataDTO.setUiScenario(item);

            executeQueue.put(report.getId(), runModeDataDTO);
            // 生成报告结构
            if (!UiGenerateHashTreeUtil.isSetReport(request.getConfig())) {
                uiScenarioReportStructureService.save(item, report.getId(), request.getConfig().getReportType());
            }
            // 重置报告ID
            reportId = UUID.randomUUID().toString();
        }
    }

    public List<UiScenarioWithBLOBs> getScenario(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<UiScenarioWithBLOBs> uiScenarios = uiScenarioMapper.selectByExampleWithBLOBs(example);
        if (isSerial(request)) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                sortById(ids, uiScenarios);
            }
        }
        return uiScenarios;
    }

    protected void sortById(List<String> ids, List apiScenarios) {
        FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(ids);
        fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
        BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
        Collections.sort(apiScenarios, beanComparator);
    }

    protected RunModeDataDTO getRunModeDataDTO(String testId, ScenarioReportResultWrapper report) {
        RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
        runModeDataDTO.setTestId(testId);
        runModeDataDTO.setPlanEnvMap(new HashMap<>());
        runModeDataDTO.setReport(report);
        runModeDataDTO.setReportId(report.getId());
        return runModeDataDTO;
    }

}
