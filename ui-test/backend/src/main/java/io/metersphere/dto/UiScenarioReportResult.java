package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioReport;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UiScenarioReportResult extends UiScenarioReport {

    private String testName;

    private String projectName;

    private String testId;

    private String userName;

    private List<String> scenarioIds;

    private String content;
}
