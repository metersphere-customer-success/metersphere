package io.metersphere.api.exec.queue;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.UiJmeterRunRequestDTO;
import io.metersphere.jmeter.JMeterService;
import io.metersphere.jmeter.JMeterThreadUtils;
import io.metersphere.utils.LoggerUtil;

public class ExecTask implements Runnable {
    private UiJmeterRunRequestDTO request;

    public ExecTask(UiJmeterRunRequestDTO request) {
        this.request = request;
    }

    public UiJmeterRunRequestDTO getRequest() {
        return this.request;
    }

    @Override
    public void run() {
        CommonBeanFactory.getBean(JMeterService.class).addQueue(request);
        Object res = PoolExecBlockingQueueUtil.take(request.getReportId());
        if (res == null && !JMeterThreadUtils.isRunning(request.getReportId(), request.getTestId())) {
            LoggerUtil.info("任务执行超时", request.getReportId());
        }
    }
}
