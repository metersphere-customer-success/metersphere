package io.metersphere.sechedule;


import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.dto.RunUiScenarioRequest;
import io.metersphere.dto.UiScenarioDTO;
import io.metersphere.dto.automation.ExecuteType;
import io.metersphere.dto.testcase.UiRunModeConfigDTO;
import io.metersphere.service.UiAutomationService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.quartz.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 情景测试Job
 *
 * @author song.tianyang
 * @Date 2020/12/22 2:59 下午
 * @Description
 */
public class UiTestJob extends MsScheduleJob {

    private String projectID;

    private String resourceId;

    private List<String> scenarioIds;

    private UiAutomationService uiAutomationService;

    public UiTestJob() {
        uiAutomationService = CommonBeanFactory.getBean(UiAutomationService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        this.resourceId = resourceId;
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        this.projectID = jobDataMap.getString("projectId");
        if (resourceId != null) {
            scenarioIds = new ArrayList<>();
            scenarioIds.add(resourceId);
        }
        LogUtil.info(jobKey.getGroup() + " Running: " + resourceId);
        LogUtil.info("CronExpression: " + expression);
        businessExecute(context);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        RunUiScenarioRequest request = getRunUiScenarioRequest();
        setConfig(context.getJobDetail().getJobDataMap(), request);
        uiAutomationService.run(request);
    }

    private RunUiScenarioRequest getRunUiScenarioRequest() {
        RunUiScenarioRequest request = new RunUiScenarioRequest();
        String id = UUID.randomUUID().toString();
        request.setId(id);
        request.setReportId(id);
        request.setProjectId(projectID);
        request.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        request.setExecuteType(ExecuteType.Saved.name());
        request.setIds(this.scenarioIds);
        request.setReportUserID(this.userId);
        request.setRunMode(ApiRunMode.UI_SCENARIO.name());
        return request;
    }

    private void setConfig(JobDataMap jobDataMap, RunUiScenarioRequest request) {
        Boolean headlessEnabled = true;
        String browser = "";
        UiScenarioDTO uiScenarioDTO = null;
        try{
            uiScenarioDTO = uiAutomationService.getNewUiScenario(resourceId);
            String scenarioDefinition = uiScenarioDTO.getScenarioDefinition();
            JSONObject element = new JSONObject(scenarioDefinition);
            headlessEnabled = element.getBoolean("headlessEnabled");
            browser = element.getString("browser");
        } catch (Exception e) {
            LogUtil.error(e, e.getMessage());
        }
        String config = jobDataMap.getString("config");
        if (StringUtils.isNotBlank(config)) {
            UiRunModeConfigDTO runModeConfig = JSON.parseObject(config, UiRunModeConfigDTO.class);
            runModeConfig.setHeadlessEnabled(headlessEnabled);
            runModeConfig.setBrowser(browser);
            request.setConfig(runModeConfig);
            request.setUiConfig(runModeConfig);
        } else {
            UiRunModeConfigDTO runModeConfigDTO = new UiRunModeConfigDTO();
            runModeConfigDTO.setMode(RunModeConstants.PARALLEL.toString());
            runModeConfigDTO.setHeadlessEnabled(headlessEnabled);
            runModeConfigDTO.setBrowser(browser);
            request.setConfig(runModeConfigDTO);
            request.setUiConfig(runModeConfigDTO);
        }
        this.handleJobEnvironment(uiScenarioDTO, request.getUiConfig());
    }

    private void handleJobEnvironment(UiScenarioDTO originScenario, RunModeConfigDTO conf) {
        if (originScenario != null) {
            try {
                Map<String, String> originEnvMap = JSON.parseMap(originScenario.getEnvironmentJson());
                // 定时任务未配置环境，使用场景环境
                if (StringUtils.isBlank(conf.getEnvironmentType())
                        || conf.getEnvMap() == null
                        || conf.getEnvMap().values().stream().noneMatch(StringUtils::isNotBlank)) {
                    conf.setEnvironmentType(originScenario.getEnvironmentType());
                    conf.setEnvironmentGroupId(originScenario.getEnvironmentGroupId());
                    conf.setEnvMap(originEnvMap);
                }
                // 定时任务配置部分环境，未配置部分使用场景环境
                else {
                    List<String> projectIds = conf.getEnvMap()
                            .keySet()
                            .stream()
                            .filter(k -> StringUtils.isBlank(conf.getEnvMap().get(k)))
                            .toList();
                    for (String projectId : projectIds) {
                        conf.getEnvMap().put(projectId, originEnvMap.getOrDefault(projectId, ""));
                    }
                }
            } catch (Exception e) {
                LogUtil.error("use ui scenario environment error.");
            }
        }
    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.UI_SCENARIO_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.UI_SCENARIO_TEST.name());
    }
}
