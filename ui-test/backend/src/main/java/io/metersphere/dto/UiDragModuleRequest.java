package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioModule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UiDragModuleRequest extends UiScenarioModule {

    List<String> nodeIds;
    UiScenarioModuleDTO nodeTree;
}
