package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RunModeDataDTO {
    // 执行HashTree
    private UiScenarioWithBLOBs uiScenario;

    // 测试场景/测试用例
    private String testId;

    private String reportId;

    // 初始化报告
    private ScenarioReportResultWrapper report;

    //
    private String apiCaseId;

    private Map<String, String> planEnvMap;

    public RunModeDataDTO() {

    }

    public RunModeDataDTO(ScenarioReportResultWrapper report, String testId) {
        this.report = report;
        this.testId = testId;
    }
}
