package io.metersphere.jmeter;


import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.UiConfigConstants;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.service.TestResultService;
import io.metersphere.service.UiExecutionQueueService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.RetryResultUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MsApiBackendListener extends AbstractBackendListenerClient implements Serializable {

    private UiExecutionQueueService uiExecutionQueueService;
    private List<SampleResult> queues;
    private ResultDTO dto;

    /**
     * 参数初始化方法
     */
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        LoggerUtil.info("初始化监听");
        queues = new LinkedList<>();
        this.setParam(context);
        super.setupTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        LoggerUtil.info("接收到JMETER执行数据【" + sampleResults.size() + " 】", dto.getReportId());
        queues.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) {
        String reportId;
        if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
             reportId = dto.getTestPlanReportId();
        } else {
            reportId = dto.getReportId();
        }

        try {
            super.teardownTest(context);
            // 清理过程步骤
            queues = RetryResultUtil.clearLoops(queues);
            JMeterBase.resultFormatting(queues, dto);
            //处理cookie
            if (StringUtils.equalsIgnoreCase(dto.getRunMode(), UiConfigConstants.RUNMODE_COOKIE)) {
                CommonBeanFactory.getBean(TestResultService.class).processCookie(dto);
                PoolExecBlockingQueueUtil.offer(dto.getReportId());
                return;
            }
            if (dto.isRetryEnable()) {
                LoggerUtil.info("重试结果处理【" + dto.getReportId() + " 】开始");
                RetryResultUtil.mergeRetryResults(dto.getRequestResults());
                LoggerUtil.info("重试结果处理【" + dto.getReportId() + " 】结束");
            }

            dto.setConsole(FixedCapacityUtils.getJmeterLogger(reportId, !StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())));
            //获取webDriverSimpler的报错信息
            getErrorMessageFromSimpler();
            // 入库存储
            CommonBeanFactory.getBean(TestResultService.class).saveResults(dto);
            LoggerUtil.info("进入TEST-END处理报告【" + dto.getReportId() + " 】" + dto.getRunMode() + " 整体执行完成");
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(dto.getReportId());
            // 整体执行结束更新资源状态
            CommonBeanFactory.getBean(TestResultService.class).testEnded(dto);
            if (uiExecutionQueueService == null) {
                uiExecutionQueueService = CommonBeanFactory.getBean(UiExecutionQueueService.class);
            }
            if (StringUtils.isNotEmpty(dto.getQueueId())) {
                uiExecutionQueueService.queueNext(dto);
            }
            // 更新测试计划报告
            LoggerUtil.info("更新测试计划报告：" + dto.getQueueId() + "，" + dto.getTestId());
            uiExecutionQueueService.checkTestPlanUiCaseTestEnd(dto.getTestId(), dto.getRunMode(), dto.getTestPlanReportId());
            LoggerUtil.info("TEST-END处理结果集完成", dto.getReportId());
        } catch (Exception e) {
            LoggerUtil.error("结果集处理异常", dto.getReportId(), e);
        } finally {
            // 停止
            UILocalRunner.stop(dto.getReportId());
            queues.clear();
        }
    }

    private void getErrorMessageFromSimpler() {
        if (CollectionUtils.isEmpty(dto.getRequestResults())) {
            return;
        }
        for (RequestResult requestResult : dto.getRequestResults()) {
            ResponseResult responseResult = requestResult.getResponseResult();
            if (responseResult !=null && StringUtils.equalsIgnoreCase(responseResult.getResponseCode(),"500")) {
                String concat = dto.getConsole().concat(responseResult.getResponseMessage());
                dto.setConsole(concat);
                break;
            }
        }
    }

    /**
     * 初始化参数
     *
     * @param context
     */
    private void setParam(BackendListenerContext context) {
        dto = new ResultDTO();
        dto.setTestId(context.getParameter(BackendListenerConstants.TEST_ID.name()));
        dto.setRunMode(context.getParameter(BackendListenerConstants.RUN_MODE.name()));
        dto.setReportId(context.getParameter(BackendListenerConstants.REPORT_ID.name()));
        dto.setReportType(context.getParameter(BackendListenerConstants.REPORT_TYPE.name()));
        dto.setTestPlanReportId(context.getParameter(BackendListenerConstants.MS_TEST_PLAN_REPORT_ID.name()));
        if (context.getParameter(BackendListenerConstants.RETRY_ENABLE.name()) != null) {
            dto.setRetryEnable(Boolean.parseBoolean(context.getParameter(BackendListenerConstants.RETRY_ENABLE.name())));
        }
        dto.setQueueId(context.getParameter(BackendListenerConstants.QUEUE_ID.name()));
        dto.setRunType(context.getParameter(BackendListenerConstants.RUN_TYPE.name()));

        String ept = context.getParameter(BackendListenerConstants.EPT.name());
        if (StringUtils.isNotEmpty(ept)) {
            dto.setExtendedParameters(JSON.parseObject(context.getParameter(BackendListenerConstants.EPT.name()), Map.class));
        }
    }
}
