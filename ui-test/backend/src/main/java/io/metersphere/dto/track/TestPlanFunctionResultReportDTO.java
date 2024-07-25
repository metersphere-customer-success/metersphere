package io.metersphere.dto.track;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanFunctionResultReportDTO {
    private List<TestCaseReportStatusResultDTO> caseData;
    private List<TestCaseReportStatusResultDTO> issueData;
}

