package io.metersphere.service;

import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.RunModeDataDTO;
import io.metersphere.dto.UiJmeterRunRequestDTO;
import io.metersphere.dto.api.UiExecutionQueueParam;
import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.jmeter.JMeterService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioParallelService {

    @Resource
    private JMeterService jMeterService;
    @Resource
    private SystemParameterService systemParameterService;

    public void parallel(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId, DBTestQueue executionQueue) {
        BaseSystemConfigDTO baseInfo = systemParameterService.getBaseInfo();
        for (String reportId : executeQueue.keySet()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            RunModeDataDTO dataDTO = executeQueue.get(reportId);
            UiJmeterRunRequestDTO runRequest = getJmeterRunRequestDTO(request, serialReportId, executionQueue, baseInfo, reportId, dataDTO);
            if (StringUtils.equals(runRequest.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                runRequest.setReportId(StringUtils.join(runRequest.getTestPlanReportId(), " ", runRequest.getReportId()));
            }
            String evnMap = getValidEnvMap(executionQueue.getDetail().getEvnMap(), dataDTO, request);
            if (StringUtils.isNotBlank(evnMap)) {
                runRequest.setUiExecutionQueueParam(JSON.parseObject(evnMap, UiExecutionQueueParam.class));
            }

            LoggerUtil.info("进入并行模式，准备执行场景：[ " + executeQueue.get(reportId).getReport().getName() + " ], 报告ID [ " + reportId + " ]");
            // 并行独立报告需要重置开始时间
            if (StringUtils.equals(runRequest.getRunType(), RunModeConstants.PARALLEL.toString()) &&
                    StringUtils.equals(runRequest.getReportType(), RunModeConstants.INDEPENDENCE.toString())) {
                runRequest.setPoolId(reportId);
            }

            jMeterService.run(runRequest);
        }
    }

    /**
     * 并行执行时环境Map的获取
     * 1.看request requestOriginator是否是来自 TEST_PLAN
     *
     * @param envMap
     * @param dataDTO
     * @param request
     * @return
     */
    private String getValidEnvMap(String envMap, RunModeDataDTO dataDTO, RunScenarioRequest request) {
        if (StringUtils.isNotBlank(envMap)) {
            UiExecutionQueueParam param = JSON.parseObject(envMap, UiExecutionQueueParam.class);
            if (StringUtils.equalsIgnoreCase(request.getRequestOriginator(), "TEST_PLAN")) {
                param.setEnvMap(dataDTO.getPlanEnvMap());
            }
            return JSON.toJSONString(param);
        }
        return envMap;
    }

    protected UiJmeterRunRequestDTO getJmeterRunRequestDTO(RunScenarioRequest request, String serialReportId, DBTestQueue executionQueue,
                                                           BaseSystemConfigDTO baseInfo, String reportId, RunModeDataDTO dataDTO) {
        UiJmeterRunRequestDTO runRequest = new UiJmeterRunRequestDTO(dataDTO.getTestId(), reportId, request.getRunMode(), null);
        runRequest.setTestPlanReportId(StringUtils.isNotEmpty(serialReportId) ? serialReportId : request.getTestPlanReportId());
        runRequest.setReportType(StringUtils.isNotEmpty(serialReportId) ? RunModeConstants.SET_REPORT.toString() : RunModeConstants.INDEPENDENCE.toString());
        runRequest.setQueueId(executionQueue.getId());
        runRequest.setRunType(RunModeConstants.PARALLEL.toString());
        runRequest.setRetryNum(request.getConfig().getRetryNum());
        runRequest.setRetryEnable(request.getConfig().isRetryEnable());
        runRequest.setSeleniumOption(request.getConfig().getDriverConfig());
        return runRequest;
    }
}
