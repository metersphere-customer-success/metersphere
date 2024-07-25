package io.metersphere.hashtree;

import lombok.Data;

/**
 * 场景的额外配置类
 */
@Data
public class ScenarioConfig {
    /**
     * 是否免登录
     */
    private Boolean useCookie;
}
