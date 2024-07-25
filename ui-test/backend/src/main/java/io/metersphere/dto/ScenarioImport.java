package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioImport {
    private String projectId;
    private List<UiScenarioWithBLOBs> data;
    private List<NodeTree> nodeTree;
}
