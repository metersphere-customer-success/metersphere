package io.metersphere.dto.track;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.dto.automation.ApiScenarioDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanFailureScenarioDTO extends ApiScenarioDTO {
    private ScenarioReportResultWrapper response;
}
