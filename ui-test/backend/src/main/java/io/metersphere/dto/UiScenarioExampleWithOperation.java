package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioExample;
import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/7/5 5:43 下午
 */
@Getter
@Setter
public class UiScenarioExampleWithOperation extends UiScenarioExample {
    private String operator;
    private Long operationTime;
}
