package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.UiScenario;
import io.metersphere.base.domain.UiScenarioExample;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.ApiScenarioDTO;
import io.metersphere.dto.automation.ApiScenarioRequest;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.scenario.request.testcase.QueryNodeRequest;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExtUiScenarioMapper {
    List<UiScenarioDTO> list(@Param("request") UiScenarioRequest request);

    int listModule(@Param("request") UiScenarioRequest request);

    List<UiScenarioDTO> listReview(@Param("request") UiScenarioRequest request);

    List<UiScenarioWithBLOBs> selectByTagId(@Param("id") String id);

    List<UiScenarioDTO> selectIds(@Param("ids") List<String> ids);

    List<UiScenarioDTO> simpleSelectIds(@Param("ids") List<String> ids);

    int selectTrash(@Param("projectId") String projectId);

    int selectTrashWithType(@Param("projectId") String projectId, @Param("scenarioType") String scenarioType);

    List<UiScenarioWithBLOBs> selectByIds(@Param("ids") String ids, @Param("oderId") String oderId);

    List<UiScenario> selectReference(@Param("request") UiScenarioRequest request);

//    int removeToGc(@Param("ids") List<String> ids);
//    int removeToGc(UiScenarioRequest request);


    int removeToGcByExample(UiScenarioExampleWithOperation example);

    int reduction(@Param("ids") List<String> ids);

    long countByProjectID(String projectId);

    List<UiScenarioWithBLOBs> selectIdAndScenarioByProjectId(String projectId);

    List<UiScenarioWithBLOBs> selectIdAndUseUrlByProjectId(String projectId);

    long countByProjectIDAndCreatInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<UiDataCountResult> countRunResultByProjectID(String projectId);

    List<String> selectIdsNotExistsInPlan(String projectId, String planId);

    UiScenario getNextNum(@Param("projectId") String projectId);

    UiScenario getNextNumOpt(@Param("projectId") String projectId);

    List<String> selectIdsByQuery(@Param("request") BaseQueryRequest request);

    void updateCustomNumByProjectId(@Param("projectId") String projectId);

    List<UiScenarioWithBLOBs> listWithIds(@Param("ids") List<String> ids);

    List<Map<String, Object>> listModuleByCollection(@Param("request") QueryNodeRequest request);

    List<String> selectIdsByUseUrlIsNull();

    String selectNameById(String id);

    List<String> selectNameByIdIn(@Param("ids") List<String> id);

    List<UiScenarioWithBLOBs> selectByNoReferenceId();

    void checkOriginalStatusByIds(@Param("ids") List<String> ids);

    List<String> selectIdsByExecuteTimeIsNull();

    Long countExecuteTimesByProjectID(String projectId);

    List<String> selectProjectIds();

    List<String> getIdsOrderByUpdateTime(@Param("projectId") String projectId);

    Long getPreOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getPreOrderWithType(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder, @Param("scenarioType") String scenarioType);

    Long getLastOrderWithType(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder, @Param("scenarioType") String scenarioType);

    List<RelationshipGraphData.Node> getTestCaseForGraph(@Param("ids") Set<String> ids);

    void setScenarioEnvGroupIdNull(@Param("environmentGroupId") String environmentGroupId);

    UiScenarioDTO selectById(@Param("id") String id);

    void clearLatestVersion(String refId);

    void addLatestVersion(String refId);

    List<String> selectRefIdsForVersionChange(@Param("versionId") String versionId, @Param("projectId") String projectId);

    List<UiScenario> selectBaseInfoGroupByModuleId(@Param("projectId") String projectId, @Param("status") String status, @Param("scenarioType") String scenarioType);

    List<UiScenario> selectBaseInfoGroupByCondition(@Param("projectId") String projectId, @Param("status") String status, @Param("scenarioType") String scenarioType, @Param("request") UiScenarioRequest request);

    void updateNoModuleScenarioToDefaultModule(@Param("projectId") String projectId, @Param("status") String status, @Param("moduleId") String moduleId, @Param("scenarioType") String scenarioType);

    List<UiScenarioWithBLOBs> selectByExampleWithBLOBs(UiScenarioExample example);

    List<ApiScenarioDTO> relevanceScenarioList(@Param("request") ApiScenarioRequest request);
}
