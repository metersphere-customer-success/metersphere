package io.metersphere.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  RefTypeEnum {

    /**
     * 场景
     */
    SCENARIO("scenario", 1),
    /**
     * 自定义指令
     */
    CUSTOM_COMMAND("customCommand", 2),

    ALL("all", 3),

    ;
    private String name;
    private Integer code;
}
