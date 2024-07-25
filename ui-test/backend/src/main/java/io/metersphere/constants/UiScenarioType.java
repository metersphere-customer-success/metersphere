package io.metersphere.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UiScenarioType {
    /**
     * 场景
     */
    SCENARIO("scenario"),
    /**
     * 自定义指令
     */
    CUSTOM_COMMAND("customCommand")
    ;

    private String type;
}
