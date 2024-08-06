package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioExecutionInfo implements Serializable {
    private String id;

    private String sourceId;

    private String result;

    private String triggerMode;

    private Long createTime;

    private static final long serialVersionUID = 1L;
}