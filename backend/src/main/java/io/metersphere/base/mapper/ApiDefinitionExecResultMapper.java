package io.metersphere.base.mapper;

import io.metersphere.api.dto.dataCount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiDefinitionExecResultExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionExecResultMapper {
    long countByExample(ApiDefinitionExecResultExample example);

    int deleteByExample(ApiDefinitionExecResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionExecResult record);

    int insertSelective(ApiDefinitionExecResult record);

    List<ApiDefinitionExecResult> selectByExampleWithBLOBs(ApiDefinitionExecResultExample example);

    List<ApiDefinitionExecResult> selectByExample(ApiDefinitionExecResultExample example);

    ApiDefinitionExecResult selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByExample(@Param("record") ApiDefinitionExecResult record, @Param("example") ApiDefinitionExecResultExample example);

    int updateByPrimaryKeySelective(ApiDefinitionExecResult record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionExecResult record);

    int updateByPrimaryKey(ApiDefinitionExecResult record);

    @Select({
            "SELECT count(id) AS countNumber FROM api_definition_exec_result ",
            "WHERE resource_id IN ( ",
            "SELECT testCase.id FROM api_test_case testCase ",
            "WHERE testCase.project_id = #{projectId}) ",
            "and start_time BETWEEN #{firstDayTimestamp} AND #{lastDayTimestamp} "
    })
    long countByProjectIDAndCreateInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    @Select({
            "SELECT count(id) AS countNumber FROM api_definition_exec_result ",
            "WHERE resource_id IN ( ",
            "SELECT testCase.id FROM api_test_case testCase ",
            "WHERE testCase.project_id = #{projectId}) ",
    })
    long countByTestCaseIDInProject(String projectId);

//    AS testPlan FROM (  SELECT apiCase.id AS testCaseID,apiCase.`name` AS testCaseNa' at line 1

    @Select({
            "SELECT testCase.testCaseName AS caseName,testCase.testPlanName AS testPlan ,caseErrorCountData.dataCountNumber AS failureTimes FROM ( ",
                "SELECT apiCase.id AS testCaseID,apiCase.`name` AS testCaseName,group_concat(testPlan.`name`)  AS testPlanName FROM api_test_case apiCase ",
                "LEFT JOIN test_plan testPlan ON testPlan.api_ids like concat('%\"',apiCase.id,'\"%') ",
                "GROUP BY apiCase.id ",
                "ORDER BY apiCase.create_time DESC ",
            ")testCase ",
            "INNER JOIN ( ",
                "SELECT resource_id AS testCaseID,COUNT(id) AS dataCountNumber,start_time AS executeTime FROM api_definition_exec_result ",
                "WHERE resource_id IN ( ",
                    "SELECT id FROM api_test_case WHERE project_id = #{projectId} ",
            ") and `status` = 'error' GROUP BY resource_id ",
            ") caseErrorCountData ON caseErrorCountData.testCaseID =testCase.testCaseID ",
                "WHERE caseErrorCountData.executeTime >= #{startTimestamp} ",
                "ORDER BY caseErrorCountData.dataCountNumber DESC ",
            "limit #{limitNumber} "
    })
    List<ExecutedCaseInfoResult> findFaliureCaseInfoByProjectIDAndExecuteTimeAndLimitNumber(@Param("projectId") String projectId, @Param("startTimestamp") long startTimestamp, @Param("limitNumber") int limitNumber);
}