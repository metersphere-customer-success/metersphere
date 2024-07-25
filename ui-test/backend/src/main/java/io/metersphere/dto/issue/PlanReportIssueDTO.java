package io.metersphere.dto.issue;

import io.metersphere.dto.PlanReportCaseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanReportIssueDTO extends PlanReportCaseDTO {
    private String platformStatus;
    private String platform;
}
