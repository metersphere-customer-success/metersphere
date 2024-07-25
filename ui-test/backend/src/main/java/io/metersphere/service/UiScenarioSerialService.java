package io.metersphere.service;


import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestPlanUiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.UiConfigConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.UiJmeterRunRequestDTO;
import io.metersphere.dto.api.UiExecutionQueueParam;
import io.metersphere.jmeter.JMeterService;
import io.metersphere.jmeter.JMeterThreadUtils;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import io.metersphere.utils.UiGlobalConfigUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioSerialService {

    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;

    public void serial(ApiExecutionQueue executionQueue, ApiExecutionQueueDetail queue) {
        String reportId = StringUtils.isNotEmpty(executionQueue.getReportId()) ? executionQueue.getReportId() : queue.getReportId();
        if (!StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.UI_SCENARIO.name())) {
            reportId = queue.getReportId();
        }
        UiJmeterRunRequestDTO runRequest = new UiJmeterRunRequestDTO(queue.getTestId(), reportId, executionQueue.getRunMode(), null);
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();

        // 判断触发资源对象是用例/场景更新对应报告状态
        updateReportToRunning(queue, runRequest);

        try {
            runRequest.setSeleniumOption(executionQueue.getUiDriver());
            runRequest.setReportType(executionQueue.getReportType());
            runRequest.setPool(UiGenerateHashTreeUtil.isResourcePool(executionQueue.getPoolId()));
            runRequest.setTestPlanReportId(executionQueue.getReportId());
            runRequest.setRunType(RunModeConstants.SERIAL.toString());
            runRequest.setQueueId(executionQueue.getId());
            runRequest.setPoolId(executionQueue.getPoolId());
            runRequest.setRetryEnable(queue.getRetryEnable());
            runRequest.setRetryNum(queue.getRetryNumber());
            UiScenarioWithBLOBs scenario = null;
            scenario = uiScenarioMapper.selectByPrimaryKey(queue.getTestId());
            if (scenario == null) {
                TestPlanUiScenario testPlanUiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(queue.getTestId());
                scenario = uiScenarioMapper.selectByPrimaryKey(testPlanUiScenario.getUiScenarioId());
            }

            String evnMap = queue.getEvnMap();

            UiExecutionQueueParam param = null;
            if (StringUtils.isNotBlank(evnMap)) {
                param = JSON.parseObject(evnMap, UiExecutionQueueParam.class);
                if (MapUtils.isNotEmpty(param.getEnvMap())) {
                    if (StringUtils.isNotBlank(scenario.getEnvironmentJson())) {
                        Map<String, String> environmentMap = JSON.parseObject(scenario.getEnvironmentJson(), Map.class);
                        for (String k : environmentMap.keySet()) {
                            //使用外部的环境配置替换默认的用于展示在报告上面
                            if (StringUtils.isNotBlank(param.getEnvMap().get(k))) {
                                environmentMap.put(k, param.getEnvMap().get(k));
                            }
                        }
                    }
                }
                runRequest.setUiExecutionQueueParam(param);
            }

            // 开始执行
            jMeterService.run(runRequest);
        } catch (Exception e) {
            LoggerUtil.error(e);
            // todo microservice
//            GenerateHashTreeUtil.remakeException(runRequest);
            LoggerUtil.error("执行队列[" + queue.getId() + "报告[" + queue.getReportId() + "入队列失败：", e);
        }
    }


    /**
     * 同步的获取 cookie 的操作，不生成任何报告，报告 id testid 都是随机
     *
     * @param relevanceScenarioId 关联的用于获取 cookie 场景id
     */
    public void runCookie(String relevanceScenarioId) {
        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(relevanceScenarioId);
        if (scenario == null) {
            MSException.throwException("env_scenario_not_exists");
        }

        String testId = relevanceScenarioId;
        String reportId = null;
        String environmentJson = scenario.getEnvironmentJson();
        if (StringUtils.isBlank(environmentJson)) {
            MSException.throwException("env_not_exists");
        }
        Map<String, String> environmentMap = JSON.parseMap(environmentJson);
        if (MapUtils.isEmpty(environmentMap) || !environmentMap.containsKey(scenario.getProjectId())) {
            MSException.throwException("env_not_exists");
        }

        //不生成报告，使用环境id作为报告id
        reportId = environmentMap.get(scenario.getProjectId());

        UiJmeterRunRequestDTO runRequest = new UiJmeterRunRequestDTO(testId, reportId, UiConfigConstants.RUNMODE_COOKIE, null);

        try {
            runRequest.setReportType(ReportTypeConstants.UI_INDEPENDENT.name());
            runRequest.setRunType(RunModeConstants.SERIAL.toString());

            UiGlobalConfigUtil.getConfig().setGenerateCookie(true);
            HashTree hashTree = UiGenerateHashTreeUtil.generateHashTree(scenario, runRequest);

            runRequest.setHashTree(hashTree);
            // 开始执行
            CommonBeanFactory.getBean(JMeterService.class).addQueue(runRequest);
            Object res = PoolExecBlockingQueueUtil.take(runRequest.getReportId());
            if (res == null && !JMeterThreadUtils.isRunning(runRequest.getReportId(), runRequest.getTestId())) {
                LoggerUtil.info("获取cookie任务执行超时", runRequest.getReportId());
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        } finally {
            UiGlobalConfigUtil.removeConfig();
        }
    }

    /**
     * 更新报告状态
     *
     * @param queue
     * @param runRequest
     */
    public void updateReportToRunning(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        if (!UiGenerateHashTreeUtil.isSetReport(runRequest.getReportType()) &&
                StringUtils.equalsAny(runRequest.getRunMode(),
                        ApiRunMode.SCENARIO.name(),
                        ApiRunMode.SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO.name(),
                        ApiRunMode.JENKINS_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_SCENARIO.name(),
                        ApiRunMode.UI_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name(),
                        ApiRunMode.UI_SCHEDULE_SCENARIO.name(),
                        ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name())
        ) {
            UiScenarioReportWithBLOBs report = uiScenarioReportMapper.selectByPrimaryKey(queue.getReportId());
            if (report != null) {
                report.setStatus(ReportStatus.RUNNING.name());
                report.setCreateTime(System.currentTimeMillis());
                report.setUpdateTime(System.currentTimeMillis());
                runRequest.setExtendedParameters(new HashMap<String, Object>() {{
                    this.put("userId", report.getCreateUser());
                }});
                uiScenarioReportMapper.updateByPrimaryKey(report);
                LoggerUtil.info("进入串行模式，准备执行资源：[ " + report.getName() + " ]", report.getId());
            }
        }
    }

}
