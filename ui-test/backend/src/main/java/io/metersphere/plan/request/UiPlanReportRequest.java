package io.metersphere.plan.request;


import io.metersphere.dto.report.TestPlanExecuteReportDTO;
import io.metersphere.request.PlanSubReportRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UiPlanReportRequest extends PlanSubReportRequest {
    private TestPlanExecuteReportDTO testPlanExecuteReportDTO;
}
