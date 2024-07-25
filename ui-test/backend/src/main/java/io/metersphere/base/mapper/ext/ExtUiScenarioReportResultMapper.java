package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.UiScenarioReportResultWithBLOBs;
import io.metersphere.dto.report.UiReportStepResultDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUiScenarioReportResultMapper {
    List<UiScenarioReportResultWithBLOBs> selectBaseInfoResultByReportId(String reportId);

    void deleteHisReportResult(@Param("scenarioIds") List<String> scenarioIds, @Param("reportId") String reportId);

    List<String> getReportIds(@Param("ids") List<String> ids);

    List<String> selectDistinctStatusByReportId(String reportId);

    List<UiReportStepResultDTO> getStepResult(@Param("reportId") String reportId, @Param("stepId") String stepId);
}
