package io.metersphere.dto.track;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.metersphere.dto.UiScenarioDTO;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanUiScenarioDTO extends UiScenarioDTO {
    private ScenarioReportResultWrapper response;
}
