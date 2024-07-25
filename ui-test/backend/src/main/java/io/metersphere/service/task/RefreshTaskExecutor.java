package io.metersphere.service.task;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(99)
public class RefreshTaskExecutor implements ApplicationRunner {

    @Autowired
    private List<AbstractTask> taskList;

    @Override
    public void run(ApplicationArguments args) {
        if (CollectionUtils.isNotEmpty(taskList)) {
            for (AbstractTask task : taskList) {
                task.doTask();
            }
        }
    }
}
