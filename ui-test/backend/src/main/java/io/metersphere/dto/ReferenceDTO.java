package io.metersphere.dto;

import io.metersphere.base.domain.ApiScenario;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReferenceDTO {
    List<ApiScenario> scenarioList;

    List<TestPlanDTO> testPlanList;
}
