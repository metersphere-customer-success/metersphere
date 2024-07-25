package io.metersphere.dto.track;

import io.metersphere.dto.TestPlanApiCaseDTO;
import io.metersphere.dto.automation.ApiScenarioDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FailureTestCasesAdvanceDTO {
    private List<TestPlanCaseDTO> functionalTestCases;
    private List<TestPlanApiCaseDTO> apiTestCases;
    private List<ApiScenarioDTO> scenarioTestCases;
    private List<TestPlanLoadCaseDTO> loadTestCases;
}
