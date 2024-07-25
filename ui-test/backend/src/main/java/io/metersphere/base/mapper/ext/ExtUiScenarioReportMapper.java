package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.UiScenarioReport;
import io.metersphere.dto.ApiReportCountDTO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.dto.QueryUiReportRequest;
import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.dto.datacount.ApiDataCountResult;
import io.metersphere.task.dto.TaskCenterRequest;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtUiScenarioReportMapper {
    List<ScenarioReportResultWrapper> list(@Param("request") QueryUiReportRequest request);

    ScenarioReportResultWrapper get(@Param("reportId") String reportId);

    long countByProjectID(String projectId);

    long countByProjectIdAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    long countByProjectIdAndCreateAndByScheduleInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countByProjectIdGroupByExecuteResult(String projectId);

    List<UiScenarioReport> selectLastReportByIds(@Param("scenarioIdList") List<String> ids);

    List<UiScenarioReport> findByProjectIds(@Param("request") TaskCenterRequest request);

    UiScenarioReport selectPreviousReportByScenarioId(@Param("scenarioId") String scenarioId, @Param("nowId") String nowId);

    List<String> idList(@Param("request") QueryUiReportRequest request);

    List<ApiReportCountDTO> countByApiScenarioId();

    List<UiScenarioReport> selectStatusByIds(@Param("ids") Collection<String> values);

    List<UiScenarioReport> selectReportByProjectId(String projectId);

    List<PlanReportCaseDTO> selectForPlanReport(@Param("ids") List<String> reportIds);

    void update(@Param("ids") List<String> ids);

    @InsertProvider(type = ExtUiScenarioReportProvider.class, method = "insertListSql")
    void sqlInsert(List<ScenarioReportResultWrapper> list);

    List<String> selectByProjectIdAndLessThanTime(@Param("projectId") String projectId, @Param("time") long time);

    int stopScenario(@Param("request") TaskCenterRequest request);

}
