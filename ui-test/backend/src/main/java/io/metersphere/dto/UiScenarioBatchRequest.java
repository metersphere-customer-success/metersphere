package io.metersphere.dto;


import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.dto.UiScenarioRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UiScenarioBatchRequest extends UiScenarioWithBLOBs {
    private List<String> ids;
    private String id;
    private String name;
    private String projectId;
    private String environmentId;
    private boolean selectAll;

    private UiScenarioRequest condition;

    /**
     * 环境和项目对应关系
     */
    private Map<String, String> envMap;

    /**
     * 场景用例跨项目的关系
     */
    private Map<String, List<String>> mapping;
}
