package io.metersphere.service;

import com.google.common.collect.Lists;
import groovy.lang.Lazy;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportResultMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.PropertyConstant;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.*;
import io.metersphere.dto.api.UiScenarioReportInitDTO;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.dto.testcase.UiRunModeConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.jmeter.FixedCapacityUtils;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.utils.JSONUtil;
import io.metersphere.utils.PassRateUtil;
import io.metersphere.utils.TemplateUtils;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import io.metersphere.xmind.utils.FileUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioReportService {
    @Resource
    private ExtUiScenarioReportMapper extUiScenarioReportMapper;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Resource
    private UiScenarioReportDetailMapper uiScenarioReportDetailMapper;
    @Resource
    private UiScenarioReportResultMapper uiScenarioReportResultMapper;
    @Resource
    private UiScenarioReportResultService uiScenarioReportResultService;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private UiScenarioReportStructureService uiScenarioReportStructureService;
    @Resource
    private UiScenarioReportStructureMapper uiScenarioReportStructureMapper;
    @Resource
    private UiScenarioExecutionInfoService uiScenarioExecutionInfoService;
    @Lazy
    @Resource
    private UiReportService uiReportService;
    @Resource
    private ExtUiScenarioReportResultMapper extUiScenarioReportResultMapper;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    private MicroService microService;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseShareInfoService baseShareInfoService;
    @Resource
    private UserService userService;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;

    public void saveResult(ResultDTO dto) {
        // 报告详情内容
        uiScenarioReportResultService.save(dto.getReportId(), dto.getRequestResults());
    }

    public void batchSaveResult(List<ResultDTO> dtos) {
        uiScenarioReportResultService.batchSave(dtos);
    }

    public void saveUiResult(List<RequestResult> requestResults, ResultDTO dto) {
        // 报告详情内容
        String reportId=dto.getReportId();
        if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            reportId = dto.getTestPlanReportId();
        }
        uiReportService.saveUiResult(reportId, requestResults);
    }

    public UiScenarioReport testEnded(ResultDTO dto) {
        if (!StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            // 更新控制台信息
            uiScenarioReportStructureService.update(dto.getReportId(), dto.getConsole());
        }
        // 优化当前执行携带结果作为状态判断依据
        UiScenarioReport scenarioReport;
        UiScenarioReportResultExample example = new UiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(dto.getReportId());
        scenarioReport = updateUiScenario(uiScenarioReportResultMapper.selectByExampleWithBLOBs(example), dto);
        // 串行队列
        return scenarioReport;
    }

    public ScenarioReportResultWrapper get(String reportId, boolean selectReportContent) {
        ScenarioReportResultWrapper reportResult = extUiScenarioReportMapper.get(reportId);
        if (reportResult != null) {
            if (reportResult.getReportVersion() != null && reportResult.getReportVersion() > 1) {
                if (SystemConstants.DataOriginEnum.NEW.value() == reportResult.getType()) {
                    reportResult.setContent(JSON.toJSONString(uiScenarioReportStructureService.assembleReport(reportId, selectReportContent)));
                } else {
                    return microService.getForData(MicroServiceName.API_TEST, StringUtils.join("/api/scenario/report/get/", reportId), ScenarioReportResultWrapper.class);
                }
            } else {
                UiScenarioReportDetail detail = uiScenarioReportDetailMapper.selectByPrimaryKey(reportId);
                if (detail != null && reportResult != null) {
                    reportResult.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
                }
            }
            return reportResult;
        }
        return null;
    }

    public List<ScenarioReportResultWrapper> list(QueryUiReportRequest request) {
        request = this.initRequest(request);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ScenarioReportResultWrapper> list = extUiScenarioReportMapper.list(request);
        List<String> userIds = list.stream().map(ScenarioReportResultWrapper::getUserId)
                .collect(Collectors.toList());
        Map<String, User> userMap = ServiceUtils.getUserMap(userIds);
        list.forEach(item -> {
            User user = userMap.get(item.getUserId());
            if (user != null)
                item.setUserName(user.getName());
        });
        return list;
    }

    public QueryUiReportRequest initRequest(QueryUiReportRequest request) {
        if (request != null) {
            request.setName(TemplateUtils.escapeSqlSpecialChars(request.getName()));
            //初始化triggerMode的查询条件： 如果查询API的话，增加 JENKINS_RUN_TEST_PLAN(jenkins调用测试计划时执行的场景) 查询条件
            if (MapUtils.isNotEmpty(request.getFilters()) && request.getFilters().containsKey("trigger_mode")
                    && CollectionUtils.isNotEmpty(request.getFilters().get("trigger_mode"))
                    && request.getFilters().get("trigger_mode").contains("API") && !request.getFilters().get("trigger_mode").contains(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name())) {
                request.getFilters().get("trigger_mode").add(ReportTriggerMode.JENKINS_RUN_TEST_PLAN.name());
            }
            try{
                Map<String, Object> combine = request.getCombine();
                if (combine != null && combine.get("triggerMode") != null){
                    Object triggerMode = combine.get("triggerMode");
                    Map<String, Object> map = (Map<String, Object>) triggerMode;
                    List<String> values = (List<String>) map.get("value");
                    if(values.contains("API")){
                        values.add("JENKINS_RUN_TEST_PLAN");
                    }
                    map.put("value", values);
                }
            }catch (Exception e){
            }
        }
        return request;
    }

    public List<String> idList(QueryUiReportRequest request) {
        request = this.initRequest(request);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        //检查必填参数caseType
        if (request.getIsUi()) {
            //ui报告对应的用例类型也是scenario
            request.setCaseType(ReportTypeConstants.SCENARIO.name());
        }
        if (StringUtils.equalsAny(request.getCaseType(), ReportTypeConstants.API.name(), ReportTypeConstants.SCENARIO.name())) {
            return extUiScenarioReportMapper.idList(request);
        } else {
            return new ArrayList<>(0);
        }
    }

    private void checkNameExist(ScenarioReportResultWrapper request) {
        UiScenarioReportExample example = new UiScenarioReportExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andExecuteTypeEqualTo(ExecuteType.Saved.name()).andIdNotEqualTo(request.getId());
        if (uiScenarioReportMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public ScenarioReportResultWrapper init(String scenarioIds, String reportName, String status, String scenarioNames, String triggerMode, String projectId, String userID) {
        ScenarioReportResultWrapper report = new ScenarioReportResultWrapper();
        if (triggerMode.equals(ApiRunMode.SCENARIO.name()) || triggerMode.equals(ApiRunMode.DEFINITION.name())) {
            triggerMode = ReportTriggerMode.MANUAL.name();
        }
        report.setId(UUID.randomUUID().toString());
        report.setName(reportName);
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(status);
        if (StringUtils.isNotEmpty(userID)) {
            report.setUserId(userID);
        } else {
            report.setUserId(SessionUtils.getUserId());
        }
        report.setTriggerMode(triggerMode);
        report.setExecuteType(ExecuteType.Saved.name());
        report.setProjectId(projectId);
        report.setScenarioName(scenarioNames);
        report.setScenarioId(scenarioIds);
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        report.setType(SystemConstants.DataOriginEnum.NEW.value());
        uiScenarioReportMapper.insert(report);
        return report;
    }

    public UiScenarioReportWithBLOBs editReport(ResultDTO dto, String status) {
        UiScenarioReportWithBLOBs report = uiScenarioReportMapper.selectByPrimaryKey(dto.getReportId());

        if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            report = uiScenarioReportMapper.selectByPrimaryKey(dto.getTestPlanReportId());
            return report;
        }

        if (report == null) {
            report = new UiScenarioReportWithBLOBs();
            report.setId(dto.getReportId());
        }
        if (StringUtils.equals(dto.getRunMode(), "CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        report.setStatus(status);
        report.setName(report.getScenarioName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setEndTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        // UI 调试类型报告不记录更新状态
        if (ExecuteType.Debug.name().equals(report.getExecuteType()) &&
                ReportTypeConstants.UI_INDEPENDENT.name().equals(report.getReportType())) {
            return report;
        }
        uiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    public UiScenarioReportWithBLOBs updateReport(ScenarioReportResultWrapper test) {
        checkNameExist(test);
        UiScenarioReportWithBLOBs report = new UiScenarioReportWithBLOBs();
        report.setId(test.getId());
        report.setProjectId(test.getProjectId());
        report.setName(test.getName());
        report.setScenarioName(test.getScenarioName());
        report.setScenarioId(test.getScenarioId());
        report.setTriggerMode(test.getTriggerMode());
        report.setDescription(test.getDescription());
        report.setEndTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(test.getStatus());
        report.setUserId(test.getUserId());
        report.setExecuteType(test.getExecuteType());
        if (StringUtils.isNotEmpty(report.getTriggerMode()) && report.getTriggerMode().equals("CASE")) {
            report.setTriggerMode(TriggerMode.MANUAL.name());
        }
        uiScenarioReportMapper.updateByPrimaryKeySelective(report);
        return report;
    }

    private String getIntegrationReportStatus(List<String> reportStatus) {
        boolean hasError = false, hasErrorReport = false, hasUnExecute = false, hasOtherStatus = false, hasStop = false;

        if (CollectionUtils.isEmpty(reportStatus)) {
            //查不到任何结果，按照未执行来处理
            hasUnExecute = true;
        } else {
            for (String status : reportStatus) {
                if (StringUtils.equalsIgnoreCase(status, ReportStatus.ERROR.toString())) {
                    hasError = true;
                } else if (StringUtils.equalsIgnoreCase(status, ReportStatus.STOPPED.toString())) {
                    hasStop = true;
                } else if (StringUtils.equalsIgnoreCase(status, ReportStatus.PENDING.toString())) {
                    hasUnExecute = true;
                } else {
                    hasOtherStatus = true;
                }
            }
            if (hasError || hasErrorReport || hasOtherStatus) {
                //根据状态优先级判定，只要存在失败/误报/其他待定状态 的数据， 则未执行和停止都为false （优先级最低）
                hasUnExecute = false;
                hasStop = false;
            }
        }

        return hasError ? ReportStatus.ERROR.toString() :
                hasStop ? ReportStatus.STOPPED.toString() :
                        hasUnExecute ? ReportStatus.PENDING.toString() : ReportStatus.SUCCESS.toString();
    }


    public UiScenarioReport updateUiScenario(List<UiScenarioReportResultWithBLOBs> requestResults, ResultDTO dto) {
        long errorSize = getUiErrorSize(dto);
        // 更新报告状态
        String status = getStatus(dto);

        UiScenarioReport report = editReport(dto, status);
        // 更新场景状态
        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (scenario == null) {
            scenario = uiScenarioMapper.selectByPrimaryKey(report.getScenarioId());
        }
        //场景模式，只更新场景,调试的不更新直接返回
        if (scenario != null) {
            boolean whetherUpdateScenario = updateUiScenario(requestResults, dto, errorSize, status, report, scenario);
            if (!whetherUpdateScenario) return report;
        }

        //测试计划模式 更新玩测试计划最新结果再更新场景
        TestPlanUiScenario testPlanUiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(dto.getTestId());
        if (testPlanUiScenario != null) {
            report.setScenarioId(testPlanUiScenario.getUiScenarioId());
            report.setEndTime(System.currentTimeMillis());
            if (StringUtils.equalsAnyIgnoreCase(status, ReportStatus.PENDING.toString())) {
                testPlanUiScenario.setLastResult(ReportStatus.ERROR.toString());
            } else {
                testPlanUiScenario.setLastResult(errorSize > 0 ? ReportStatus.ERROR.toString() : ReportStatus.SUCCESS.toString());
            }
            testPlanUiScenario.setPassRate(PassRateUtil.calculatePassRate(requestResults, report));
            testPlanUiScenario.setReportId(report.getId());
            report.setEndTime(System.currentTimeMillis());
            testPlanUiScenario.setUpdateTime(System.currentTimeMillis());
            testPlanUiScenarioMapper.updateByPrimaryKeySelective(testPlanUiScenario);
            if (scenario == null) {
                scenario = uiScenarioMapper.selectByPrimaryKey(testPlanUiScenario.getUiScenarioId());
            }
            updateUiScenario(requestResults, dto, errorSize, status, report, scenario);
        }

        //发送通知
        if (Optional.ofNullable(scenario).isPresent() && Optional.ofNullable(report).isPresent() && !StringUtils.equals(report.getExecuteType(), "Debug")) {
            if (StringUtils.isBlank(dto.getTestPlanReportId())) {
                this.sendNotice(scenario, report);
                this.sendTask(report, report, dto, scenario);
            }
        }
        return report;
    }

    @Nullable
    private boolean updateUiScenario(List<UiScenarioReportResultWithBLOBs> requestResults, ResultDTO dto, long errorSize, String status, UiScenarioReport report, UiScenarioWithBLOBs scenario) {
        if (StringUtils.equalsAnyIgnoreCase(status, ReportStatus.PENDING.toString())) {
            scenario.setLastResult(ReportStatus.ERROR.toString());
        } else {
            scenario.setLastResult(errorSize > 0 ? ReportStatus.ERROR.toString() : ReportStatus.SUCCESS.toString());
        }

        if (!StringUtils.equalsIgnoreCase(UiGenerateHashTreeUtil.SET_REPORT, dto.getReportType())) {
            //集合报告的通过率不计入单个场景的通过率里面
            scenario.setPassRate(PassRateUtil.calculatePassRate(requestResults, report));
            scenario.setReportId(dto.getReportId());
        }
        int executeTimes = 0;
        if (scenario.getExecuteTimes() != null) {
            executeTimes = scenario.getExecuteTimes().intValue();
        }
        scenario.setExecuteTimes(executeTimes + 1);
        // 针对 UI 调试类型的不需要更新
        String custom = "customCommand";
        if (StringUtils.equals(report.getExecuteType(), ExecuteType.Debug.name()) &&
                report.getReportType().equals(ReportTypeConstants.UI_INDEPENDENT.name()) &&
                !StringUtils.equalsIgnoreCase(scenario.getScenarioType(), custom)
        ) {
            return false;
        }
        uiScenarioMapper.updateByPrimaryKey(scenario);
        return true;
    }

    private void sendTask(UiScenarioReport report, UiScenarioReport result, ResultDTO dto, UiScenarioWithBLOBs scenario) {
        if (report == null) {
            return;
        }
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/ui/report/view/"+ result.getId()+"?showCancelButton=false";

        String subject = StringUtils.EMPTY;
        String event = StringUtils.EMPTY;
        String successContext = "${operator}执行UI自动化成功: ${name}" + ", 报告: ${reportUrl}";
        String failedContext = "${operator}执行UI自动化失败: ${name}" + ", 报告: ${reportUrl}";
        String shareUrl = getScenarioShareUrl(report.getId(), report.getUserId());
        if (StringUtils.equals(ReportTriggerMode.MANUAL.name(), report.getTriggerMode())) {
            subject = "Jenkins任务通知";
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
            subject = "任务通知";
        }
        if (StringUtils.equalsIgnoreCase(ReportStatus.SUCCESS.name(), report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (StringUtils.equalsIgnoreCase(ReportStatus.ERROR.name(), report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        UserDTO userDTO = userService.getUserDTO(result.getCreateUser());
        Map paramMap = new HashMap<>();
        paramMap.put(PropertyConstant.TYPE, "ui");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("reportUrl", reportUrl);
        paramMap.put("operator", userDTO.getName());
        paramMap.put("scenarioShareUrl", baseSystemConfigDTO.getUrl() + "/ui/shareUiReport" + shareUrl);
        paramMap.putAll(new BeanMap(report));
        paramMap.putAll(new BeanMap(scenario));
        NoticeModel noticeModel = NoticeModel.builder().operator(report.getUserId()).successContext(successContext).failedContext(failedContext).testId(dto.getTestId()).status(report.getStatus()).event(event).subject(subject).paramMap(paramMap).build();
        noticeSendService.send(report.getTriggerMode(), NoticeConstants.TaskType.UI_AUTOMATION_TASK, noticeModel);
    }

    private void sendNotice(UiScenarioWithBLOBs scenario, UiScenarioReport result) {
        BeanMap beanMap = new BeanMap(scenario);

        String event;
        String status;
        AtomicReference<String> deleteUser= new AtomicReference<>("");
        if (StringUtils.endsWithIgnoreCase(scenario.getLastResult(), ReportStatus.SUCCESS.name())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
            status = "成功";
        } else {
            event = NoticeConstants.Event.EXECUTE_FAILED;
            status = "失败";
        }
        String userId = result.getCreateUser();
        UserDTO userDTO = userService.getUserDTO(userId);
        String deleteUserId = scenario.getDeleteUserId();
        UserDTO delUserDto = userService.getUserDTO(deleteUserId);
        Optional.ofNullable(delUserDto).ifPresentOrElse(s -> deleteUser.set(s.getName()), () -> deleteUser.set(StringUtils.EMPTY));
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        assert systemParameterService != null;
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", userDTO.getName());
        paramMap.put("deleteUser", deleteUser.get());
        paramMap.put("status", scenario.getLastResult());
        paramMap.put("environment", getEnvironment(scenario));
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/ui/report/view/"+ result.getId()+"?showCancelButton=false";
        paramMap.put("reportUrl", reportUrl);
        String shareUrl = getScenarioShareUrl(result.getId(), userId);
        paramMap.put("scenarioShareUrl", baseSystemConfigDTO.getUrl() + "/ui/shareUiReport" + shareUrl);
        String context = "${operator}执行UI自动化" + status + ": ${name}";
        NoticeModel noticeModel = NoticeModel.builder().operator(userId).context(context).subject("UI自动化通知").paramMap(paramMap).event(event).build();
        Project project = projectMapper.selectByPrimaryKey(scenario.getProjectId());
        noticeSendService.send(project, NoticeConstants.TaskType.UI_AUTOMATION_TASK, noticeModel);
    }

    public String getScenarioShareUrl(String scenarioReportId, String userId) {
        ShareInfo shareRequest = new ShareInfo();
        shareRequest.setCustomData(scenarioReportId);
        shareRequest.setShareType("UI_REPORT");
        shareRequest.setCreateUserId(userId);
        ShareInfo shareInfo = baseShareInfoService.generateShareInfo(shareRequest);
        return baseShareInfoService.conversionShareInfoToDTO(shareInfo).getShareUrl();
    }

    public String getEnvironment(UiScenarioWithBLOBs apiScenario) {
        String environment = "未配置";
        String environmentType = apiScenario.getEnvironmentType();
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotEmpty(apiScenario.getEnvironmentJson())) {
            String environmentJson = apiScenario.getEnvironmentJson();
            JSONObject jsonObject = JSONUtil.parseObject(environmentJson);
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            List<String> collect = jsonObject.toMap().values().stream().map(Object::toString).collect(Collectors.toList());
            collect.add("-1");// 防止没有配置环境导致不能发送的问题
            example.createCriteria().andIdIn(collect);
            List<ApiTestEnvironment> envs = apiTestEnvironmentMapper.selectByExample(example);
            String env = envs.stream().map(ApiTestEnvironment::getName).collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(env)) {
                environment = env;
            }
        }

        if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
            String environmentGroupId = apiScenario.getEnvironmentGroupId();
            EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(environmentGroupId);
            if (environmentGroup != null) {
                environment = environmentGroup.getName();
            }
        }
        return environment;
    }

    public String update(ScenarioReportResultWrapper test) {
        UiScenarioReportWithBLOBs report = updateReport(test);
        UiScenarioReportDetail detail = uiScenarioReportDetailMapper.selectByPrimaryKey(test.getId());
        if (detail == null) {
            detail = new UiScenarioReportDetail();
            detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(test.getProjectId());
            uiScenarioReportDetailMapper.insert(detail);
        } else {
            detail.setContent(test.getContent().getBytes(StandardCharsets.UTF_8));
            detail.setReportId(report.getId());
            detail.setProjectId(test.getProjectId());
            uiScenarioReportDetailMapper.updateByPrimaryKey(detail);
        }
        return report.getId();
    }

    public List<String> getReportIds(String content) {
        try {
            return JSON.parseObject(content, List.class);
        } catch (Exception e) {
            return null;
        }
    }


    public void deleteScenarioReportResource(String id) {
        uiScenarioReportMapper.deleteByPrimaryKey(id);

        uiScenarioReportDetailMapper.deleteByPrimaryKey(id);

        UiScenarioReportResultExample example = new UiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(id);
        uiScenarioReportResultMapper.deleteByExample(example);

        UiScenarioReportStructureExample structureExample = new UiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(id);
        uiScenarioReportStructureMapper.deleteByExample(structureExample);
    }

    public void deleteScenarioReportByIds(List<String> ids) {
        UiScenarioReportDetailExample detailExample = new UiScenarioReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        uiScenarioReportDetailMapper.deleteByExample(detailExample);

        UiScenarioReportExample apiTestReportExample = new UiScenarioReportExample();
        apiTestReportExample.createCriteria().andIdIn(ids);
        uiScenarioReportMapper.deleteByExample(apiTestReportExample);

        UiScenarioReportResultExample reportResultExample = new UiScenarioReportResultExample();
        reportResultExample.createCriteria().andReportIdIn(ids);
        uiScenarioReportResultMapper.deleteByExample(reportResultExample);

        UiScenarioReportStructureExample structureExample = new UiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdIn(ids);
        uiScenarioReportStructureMapper.deleteByExample(structureExample);
    }

    public List<String> getIdsByDeleteBatchRequest(UiReportBatchRequest reportRequest) {
        List<String> ids = reportRequest.getIds();
        if (reportRequest.isSelectAllDate()) {
            ids = this.idList(reportRequest);
            if (reportRequest.getUnSelectIds() != null) {
                ids.removeAll(reportRequest.getUnSelectIds());
            }
        }
        return ids;
    }

    public long countByProjectIdAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extUiScenarioReportMapper.countByProjectIdAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public String getLogDetails(String id) {
        UiScenarioReport bloBs = uiScenarioReportMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ModuleReference.moduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            UiScenarioReportExample example = new UiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            List<UiScenarioReport> reportList = uiScenarioReportMapper.selectByExample(example);
            List<String> names = reportList.stream().map(UiScenarioReport::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), reportList.get(0).getProjectId(), String.join(",", names), reportList.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public Map<String, String> getReportStatusByReportIds(Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        List<UiScenarioReport> reportList = extUiScenarioReportMapper.selectStatusByIds(values);
        for (UiScenarioReport report : reportList) {
            map.put(report.getId(), report.getStatus());
        }
        return map;
    }

    public ScenarioReportResultWrapper init(UiScenarioReportInitDTO initModel) {
        if (initModel == null) {
            initModel = new UiScenarioReportInitDTO();
        }
        ScenarioReportResultWrapper report = new ScenarioReportResultWrapper();
        if (StringUtils.equalsAny(initModel.getTriggerMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.DEFINITION.name())) {
            initModel.setTriggerMode(ReportTriggerMode.MANUAL.name());
        }
        report.setId(initModel.getId());
        report.setTestId(initModel.getId());
        String scenarioName = initModel.getScenarioName();
        if (StringUtils.isNotEmpty(scenarioName)) {
            scenarioName = scenarioName.length() >= 3000 ? scenarioName.substring(0, 2000) : scenarioName;
            report.setName(scenarioName);
        } else {
            report.setName("场景调试");
        }
        report.setUpdateTime(System.currentTimeMillis());
        report.setCreateTime(System.currentTimeMillis());

        String status = initModel.getConfig() != null && StringUtils.equals(initModel.getConfig().getMode(), RunModeConstants.SERIAL.toString())
                ? ReportStatus.PENDING.name() : ReportStatus.RUNNING.name();
        report.setStatus(status);
        if (StringUtils.isNotEmpty(initModel.getUserId())) {
            report.setUserId(initModel.getUserId());
            report.setCreateUser(initModel.getUserId());
        } else {
            report.setUserId(SessionUtils.getUserId());
            report.setCreateUser(SessionUtils.getUserId());
        }
        if (initModel.getConfig() != null && StringUtils.isNotBlank(initModel.getConfig().getResourcePoolId())) {
            report.setActuator(initModel.getConfig().getResourcePoolId());
        } else {
            report.setActuator("LOCAL");
        }
        report.setTriggerMode(initModel.getTriggerMode());
        report.setReportVersion(2);
        report.setExecuteType(initModel.getExecType());
        report.setProjectId(initModel.getProjectId());
        report.setScenarioName(scenarioName);
        report.setScenarioId(initModel.getScenarioId());
        if (initModel.getConfig() != null) {
            report.setEnvConfig(JSON.toJSONString(initModel.getConfig()));
        }
        report.setRelevanceTestPlanReportId(initModel.getRelevanceTestPlanReportId());
        report.setReportType(ReportTypeConstants.UI_INDEPENDENT.name());
        report.setType(SystemConstants.DataOriginEnum.NEW.value());
        return report;
    }

    public ScenarioReportResultWrapper init(UiScenarioReportInitDTO initModel, String environmentId) {
        if (initModel == null) {
            initModel = new UiScenarioReportInitDTO();
        }
        ScenarioReportResultWrapper report = new ScenarioReportResultWrapper();
        if (StringUtils.equalsAny(initModel.getTriggerMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.DEFINITION.name())) {
            initModel.setTriggerMode(ReportTriggerMode.MANUAL.name());
        }
        report.setId(initModel.getId());
        report.setTestId(initModel.getId());
        String scenarioName = initModel.getScenarioName();
        if (StringUtils.isNotEmpty(scenarioName)) {
            scenarioName = scenarioName.length() >= 3000 ? scenarioName.substring(0, 2000) : scenarioName;
            report.setName(scenarioName);
        } else {
            report.setName("场景调试");
        }
        report.setUpdateTime(System.currentTimeMillis());
        report.setCreateTime(System.currentTimeMillis());

        String status = initModel.getConfig() != null && StringUtils.equals(initModel.getConfig().getMode(), RunModeConstants.SERIAL.toString())
                ? ReportStatus.PENDING.name() : ReportStatus.RUNNING.name();
        report.setStatus(status);
        if (StringUtils.isNotEmpty(initModel.getUserId())) {
            report.setUserId(initModel.getUserId());
            report.setCreateUser(initModel.getUserId());
        } else {
            report.setUserId(SessionUtils.getUserId());
            report.setCreateUser(SessionUtils.getUserId());
        }
        if (initModel.getConfig() != null && StringUtils.isNotBlank(initModel.getConfig().getResourcePoolId())) {
            report.setActuator(initModel.getConfig().getResourcePoolId());
        } else {
            report.setActuator("LOCAL");
        }
        report.setTriggerMode(initModel.getTriggerMode());
        report.setReportVersion(2);
        report.setExecuteType(initModel.getExecType());
        report.setProjectId(initModel.getProjectId());
        report.setScenarioName(scenarioName);
        report.setScenarioId(initModel.getScenarioId());
        if (initModel.getConfig() != null) {
            report.setEnvConfig(JSON.toJSONString(initModel.getConfig()));
        } else  if (StringUtils.isNotEmpty(environmentId)) {
            RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
            UiScenarioReportInitDTO finalInitModel = initModel;
            runModeConfigDTO.setEnvMap(new HashMap<>() {{
                this.put(finalInitModel.getProjectId(), environmentId);
            }});
            report.setEnvConfig(JSON.toJSONString(runModeConfigDTO));
        }

        report.setRelevanceTestPlanReportId(initModel.getRelevanceTestPlanReportId());
        if (initModel.getConfig() instanceof UiRunModeConfigDTO) {
            report.setReportType(ReportTypeConstants.UI_INDEPENDENT.name());
        } else {
            report.setReportType(ReportTypeConstants.SCENARIO_INDEPENDENT.name());
        }
        report.setType(SystemConstants.DataOriginEnum.NEW.value());
        return report;
    }

    public ScenarioReportResultWrapper getUiScenarioReportResult(RunScenarioRequest request, String serialReportId,
                                                                 String scenarioNames, String reportScenarioIds) {
        ScenarioReportResultWrapper report = this.init(new UiScenarioReportInitDTO(request.getConfig().getReportId(), reportScenarioIds,
                scenarioNames, request.getTriggerMode(), ExecuteType.Saved.name(), request.getProjectId(),
                request.getReportUserID(), request.getConfig(), request.getTestPlanReportId()));
        report.setName(request.getConfig().getReportName());
        report.setId(serialReportId);
        report.setReportType(ReportTypeConstants.SCENARIO_INTEGRATED.name());
        request.getConfig().setAmassReport(serialReportId);
        if (request.getConfig() != null) {
            report.setEnvConfig(JSON.toJSONString(request.getConfig()));
        }
        report.setStatus(ReportStatus.RUNNING.name());
        return report;
    }

    /**
     * 返回正确的报告状态
     *
     * @param dto jmeter返回
     * @return
     */
    private String getStatus(ResultDTO dto) {
        long errorSize = 0;
        if (StringUtils.isNotEmpty(dto.getRunMode()) && dto.getRunMode().startsWith("UI")) {
            try {
                errorSize = getUiErrorSize(dto);
            } catch (Exception e) {
                // UI 返回的结果在 headers 里面，格式不符合规范的直接认定结果为失败
                errorSize = 1;
            }
        }
        String status = ReportStatus.SUCCESS.toString();
        if (errorSize > 0) {
            status = ReportStatus.ERROR.toString();
        }
        return status;
    }

    public List<PlanReportCaseDTO> selectForPlanReport(List<String> reportIds) {
        return extUiScenarioReportMapper.selectForPlanReport(reportIds);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchSave(Map<String, RunModeDataDTO> executeQueue, String serialReportId, String runMode, List<MsExecResponseDTO> responseDTOS) {
        List<ScenarioReportResultWrapper> list = new LinkedList<>();
        if (StringUtils.isEmpty(serialReportId)) {
            for (String reportId : executeQueue.keySet()) {
                ScenarioReportResultWrapper report = executeQueue.get(reportId).getReport();
                if (ApiRunMode.UI_SCENARIO_PLAN.name().equals(runMode)) {
                    report.setReportType(ReportTypeConstants.TEST_PLAN.name());
                }
                list.add(report);
                responseDTOS.add(new MsExecResponseDTO(executeQueue.get(reportId).getTestId(), reportId, runMode));
            }
            if (CollectionUtils.isNotEmpty(list)) {
                extUiScenarioReportMapper.sqlInsert(list);
            }
        }
    }


    public RequestResult selectReportContent(String stepId) {
        return uiScenarioReportStructureService.selectReportContent(stepId);
    }


    /**
     * 删除 UI 报告产生的截图 及 报告
     */
    public void cleanUpUiReportImg(long backupTime, String projectId) {
        try {
            // 属于定时任务删除调试报告情况
            // 获取昨天的当前时间
            // 清理类型为 UI 报告类型，且时间为昨天之前的 UI 调试类型报告截图
            UiScenarioReportExample example = new UiScenarioReportExample();
            example.createCriteria()
                    .andProjectIdEqualTo(projectId)
                    .andCreateTimeLessThan(backupTime).andReportTypeIn(Lists.newArrayList(ReportTypeConstants.UI_INDEPENDENT.name(),
                    ReportTypeConstants.UI_INTEGRATED.name()))
                    .andExecuteTypeIn(Lists.newArrayList(ExecuteType.Debug.name(), ExecuteType.Saved.name()));
            List<UiScenarioReport> uiScenarioReports = uiScenarioReportMapper.selectByExample(example);
            // 删除调试报告的截图
            for (UiScenarioReport apiScenarioReport : uiScenarioReports) {
                // 删除调试报告
                UiScenarioReportResultExample resultExample = new UiScenarioReportResultExample();
                resultExample.createCriteria().andReportIdEqualTo(apiScenarioReport.getId());
                UiScenarioReportStructureExample structureExample = new UiScenarioReportStructureExample();
                structureExample.createCriteria().andReportIdEqualTo(apiScenarioReport.getId());

                uiScenarioReportDetailMapper.deleteByPrimaryKey(apiScenarioReport.getId());
                uiScenarioReportResultMapper.deleteByExample(resultExample);
                uiScenarioReportStructureMapper.deleteByExample(structureExample);
                uiScenarioReportMapper.deleteByPrimaryKey(apiScenarioReport.getId());
                //如果有截图数据则删除
                if (FileUtil.deleteDir(new File(FileUtils.UI_IMAGE_DIR + "/" + apiScenarioReport.getId()))) {
                    LogUtil.info("删除 UI 调试报告截图成功，报告 ID 为 ：" + apiScenarioReport.getId());
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    public ScenarioReportResultWrapper initResult(String reportId, String testPlanScenarioId, String name, RunScenarioRequest request) {
        return this.init(new UiScenarioReportInitDTO(reportId, testPlanScenarioId, name, request.getTriggerMode(),
                request.getExecuteType(), request.getProjectId(), request.getReportUserID(), request.getConfig(), request.getTestPlanReportId()));
    }

    public ScenarioReportResultWrapper initDebugResult(RunDefinitionRequest request) {
        return this.init(new UiScenarioReportInitDTO(request.getId(),
                request.getScenarioId(),
                request.getScenarioName(),
                ReportTriggerMode.MANUAL.name(),
                request.getExecuteType(),
                request.getProjectId(),
                SessionUtils.getUserId(),
                request.getConfig(), null));
    }

    public void deleteByRelevanceTestPlanReportIds(List<String> testPlanReportIdList) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            UiScenarioReportExample apiScenarioReportExample = new UiScenarioReportExample();
            apiScenarioReportExample.createCriteria().andRelevanceTestPlanReportIdIn(testPlanReportIdList);
            uiScenarioReportMapper.deleteByExample(apiScenarioReportExample);
        }
    }

    public List<String> batchDeleteReportResource(UiReportBatchRequest reportRequest, List<String> ids, boolean deleteApiResult) {
        List<String> myList = reportRequest.getIds().stream().distinct().collect(Collectors.toList());
        reportRequest.setIds(myList);
        //为预防数量太多，调用删除方法时引起SQL过长的Bug，此处采取分批执行的方式。
        //每次处理的数据数量
        int handleCount = 2000;
        //每次处理的集合
        while (ids.size() > handleCount) {
            List<String> handleIdList = new ArrayList<>(handleCount);
            List<String> otherIdList = new ArrayList<>();
            for (int index = 0; index < ids.size(); index++) {
                if (index < handleCount) {
                    handleIdList.add(ids.get(index));
                } else {
                    otherIdList.add(ids.get(index));
                }
            }
            //处理本次的数据
            deleteScenarioReportByIds(handleIdList);

            //转存剩余的数据
            ids = otherIdList;
        }
        return ids;
    }

    public void margeReport(String reportId, String runMode, String console) {
        // 更新场景状态

        UiScenarioReport report = uiScenarioReportMapper.selectByPrimaryKey(reportId);
        if (report != null) {
            if (!StringUtils.equalsAnyIgnoreCase(report.getStatus(), APITestStatus.Rerunning.name())) {
                report.setEndTime(System.currentTimeMillis());
            }
            List<String> statusList = extUiScenarioReportResultMapper.selectDistinctStatusByReportId(reportId);
            report.setStatus(this.getIntegrationReportStatus(statusList));
            // 更新报告
            uiScenarioReportMapper.updateByPrimaryKey(report);
            //场景集合报告，按照集合报告的结果作为场景的最后执行结果
            uiScenarioExecutionInfoService.insertExecutionInfoByScenarioIds(report.getScenarioId(), report.getStatus(), report.getTriggerMode());
        }


        console = StringUtils.isNotEmpty(console) ? console : FixedCapacityUtils.getJmeterLogger(reportId, true);
        if (StringUtils.isNotEmpty(console)) {
            uiScenarioReportStructureService.update(reportId, console);
        }
        // 更新控制台信息
        if (FixedCapacityUtils.containsKey(reportId)) {
            FixedCapacityUtils.remove(reportId);
        }
    }


    /**
     * 主流程或者有断言失败的就算失败
     *
     * @param dto
     * @return
     */
    public static long getUiErrorSize(ResultDTO dto) {
        int errorSize = 0;
        int successSize = 0;
        try {
            boolean success;
            String processType;
            String cmdName;
            RequestResult r = null;
            if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                r = dto.getRequestResults().get(dto.getRequestResults().size() - 1);
            }
            if (!StringUtils.contains(r.getName(), "WebDriverSampler")) {
                return 1;
            }
            if (StringUtils.isNotEmpty(r.getResponseResult().getHeaders())) {
                JSONArray responseArr = new JSONArray(r.getResponseResult().getHeaders());
                for (int i = 0; i < responseArr.length(); i++) {
                    JSONObject stepResult = responseArr.getJSONObject(i);
                    success = Optional.ofNullable(stepResult.getBoolean("success")).orElse(Boolean.FALSE);
                    processType = Optional.ofNullable(stepResult.getString("processType")).orElse("");
                    cmdName = Optional.ofNullable(stepResult.getString("cmdName")).orElse("");
                    if (!success && (StringUtils.equalsIgnoreCase("MAIN", processType) || cmdName.startsWith("verify") || cmdName.startsWith("assert"))) {
                        errorSize++;
                    } else {
                        successSize++;
                    }
                }
            }
        } catch (Exception e) {
            errorSize = 1;
        }
        if (successSize == 0 && errorSize == 0) {
            return 1;
        }
        return errorSize;
    }

    public void delete(DeleteAPIReportRequest request) {

        UiScenarioReport report = uiScenarioReportMapper.selectByPrimaryKey(request.getId());

        deleteScenarioReportResource(request.getId());

        // 为 UI 类型报告，需要删除报告产生的截图
        uiReportService.cleanUpUiReportImg(Arrays.asList(request.getId()));

        // 补充逻辑，如果是集成报告则把零时报告全部删除
        if (report != null && StringUtils.isNotEmpty(report.getScenarioId())) {
            List<String> list = getReportIds(report.getScenarioId());
            if (CollectionUtils.isNotEmpty(list)) {
                UiReportBatchRequest reportRequest = new UiReportBatchRequest();
                reportRequest.setIsUi(request.getIsUi());
                reportRequest.setIds(list);
                reportRequest.setCaseType(ReportTypeConstants.SCENARIO.name());
                this.deleteAPIReportBatch(reportRequest);
            }
        }
    }

    public void deleteAPIReportBatch(UiReportBatchRequest reportRequest) {
        List<String> ids = getIdsByDeleteBatchRequest(reportRequest);
        ids = batchDeleteReportResource(reportRequest, ids, true);
        //处理报告关联数据
        if (!ids.isEmpty()) {
            ids.forEach(id -> {
                // 为 UI 类型报告，需要删除报告产生的截图
                uiReportService.cleanUpUiReportImg(Arrays.asList(id));
            });
            deleteScenarioReportByIds(ids);
        }
    }

    public void reName(UiScenarioReport reportRequest) {
        UiScenarioReportWithBLOBs uiTestReport = uiScenarioReportMapper.selectByPrimaryKey(reportRequest.getId());
        if (uiTestReport != null) {
            uiTestReport.setName(reportRequest.getName());
            uiScenarioReportMapper.updateByPrimaryKey(uiTestReport);
        }
    }

    public List<UiScenarioReportWithBLOBs> selectExtForPlanReport(String planId) {
        UiScenarioReportExample scenarioReportExample = new UiScenarioReportExample();
        scenarioReportExample.createCriteria().andRelevanceTestPlanReportIdEqualTo(planId);
        List<UiScenarioReportWithBLOBs> uiScenarioReports = uiScenarioReportMapper.selectByExampleWithBLOBs(scenarioReportExample);
        return uiScenarioReports;
    }
}
