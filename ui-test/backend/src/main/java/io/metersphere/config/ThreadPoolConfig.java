package io.metersphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    public static final int COUNT_DOWN_NUM = 8;

    /**
     * spring默认任务线程池配置
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(4);
        executor.setThreadNamePrefix("ui-test-task-executor");
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor asyncEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(4096);
        executor.setThreadNamePrefix("AsyncEvent-");
        executor.initialize();
        return executor;
    }
}