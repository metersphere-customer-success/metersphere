package io.metersphere.dto.report;

import io.metersphere.dto.request.ReportRequest;
import lombok.Data;

import java.util.List;

@Data
public class DeleteReportRequest {
    private List<String> ids;
    private String projectId;
    private ReportRequest condition;
}
