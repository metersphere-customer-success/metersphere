package io.metersphere.dto.report;

import io.metersphere.dto.track.TestPlanFailureApiDTO;
import io.metersphere.dto.track.TestPlanFailureScenarioDTO;
import io.metersphere.dto.track.TestPlanUiScenarioDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanExecuteReportDTO {
    private Map<String,String> testPlanApiCaseIdAndReportIdMap;
    private Map<String,String> testPlanScenarioIdAndReportIdMap;
    private Map<String,String> testPlanUiScenarioIdAndReportIdMap;
    private Map<String,String> testPlanLoadCaseIdAndReportIdMap;
    private Map<String, TestPlanFailureApiDTO> apiCaseInfoDTOMap;
    private Map<String, TestPlanFailureScenarioDTO> scenarioInfoDTOMap;
    private Map<String, TestPlanUiScenarioDTO> uiScenarioInfoDTOMap;
}
