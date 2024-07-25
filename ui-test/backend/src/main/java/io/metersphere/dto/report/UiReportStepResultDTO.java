package io.metersphere.dto.report;

import io.metersphere.base.domain.UiScenarioReportResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiReportStepResultDTO  extends UiScenarioReportResult {
    private String content;
    private String baseInfo;
}
