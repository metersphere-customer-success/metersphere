package io.metersphere.config;

import io.metersphere.scenario.request.dubbo.ConfigCenter;
import io.metersphere.scenario.request.dubbo.ConsumerAndService;
import io.metersphere.scenario.request.dubbo.RegistryCenter;
import lombok.Data;

@Data
public class DubboConfig {
    private ConfigCenter configCenter;
    private RegistryCenter registryCenter;
    private ConsumerAndService consumerAndService;
}
