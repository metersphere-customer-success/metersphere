package io.metersphere.jmeter;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.service.UiExecutionQueueService;
import io.metersphere.service.UiExecutionQueueTimeOutService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FixedTask {
    private UiExecutionQueueService queueService;

    private UiExecutionQueueTimeOutService queueTimeOutService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void execute() {
        if (queueService == null) {
            queueService = CommonBeanFactory.getBean(UiExecutionQueueService.class);
        }
        queueService.defendQueue();
    }

    /**
     * 执行超时补偿执行
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void executeTimeOut() {
        if (queueTimeOutService == null) {
            queueTimeOutService = CommonBeanFactory.getBean(UiExecutionQueueTimeOutService.class);
        }
        queueTimeOutService.defendQueue();
    }
}
