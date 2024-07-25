package io.metersphere.utils;

import io.metersphere.dto.request.ParameterConfig;

import java.util.Optional;


/**
 * UI 全局配置，跟线程绑定，设置之后必须要 removeConfig
 */
public class UiGlobalConfigUtil {
    private static final ThreadLocal<ParameterConfig> config = new ThreadLocal<>();

    public static void setConfig(ParameterConfig config) {
        UiGlobalConfigUtil.config.set(config);
    }

    public static synchronized ParameterConfig getConfig() {
        return Optional.ofNullable(config.get()).orElse(new ParameterConfig());
    }

    public static void removeConfig() {
        config.remove();
    }
}
