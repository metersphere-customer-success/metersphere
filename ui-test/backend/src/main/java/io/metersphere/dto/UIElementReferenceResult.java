package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class UIElementReferenceResult {

    private Integer referenceSize;

    /**
     * 1-场景 2-指令 3-场景/指令
     */
    private Integer referenceType;

    private String tipName;

}
