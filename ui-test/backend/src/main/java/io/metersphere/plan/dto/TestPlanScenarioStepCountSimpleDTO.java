package io.metersphere.plan.dto;

import io.metersphere.dto.request.plan.TestPlanScenarioStepCountDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanScenarioStepCountSimpleDTO {
    private TestPlanScenarioStepCountDTO stepCount;
    private int underwayStepsCounts ;
}

