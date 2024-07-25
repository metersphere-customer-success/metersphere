package io.metersphere.plan.dto;


import io.metersphere.dto.track.TestPlanUiScenarioDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UiPlanReportDTO {
    private List<TestPlanUiScenarioDTO> uiAllCases;
    private List<TestPlanUiScenarioDTO> uiFailureCases;
}
