package io.metersphere.base.mapper;

import java.util.List;

import io.metersphere.base.domain.UiScenarioReportResult;
import io.metersphere.base.domain.UiScenarioReportResultExample;
import io.metersphere.base.domain.UiScenarioReportResultWithBLOBs;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportResultMapper {
    long countByExample(UiScenarioReportResultExample example);

    int deleteByExample(UiScenarioReportResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReportResultWithBLOBs record);

    int insertSelective(UiScenarioReportResultWithBLOBs record);

    List<UiScenarioReportResultWithBLOBs> selectByExampleWithBLOBs(UiScenarioReportResultExample example);

    List<UiScenarioReportResult> selectByExample(UiScenarioReportResultExample example);

    UiScenarioReportResultWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReportResultWithBLOBs record, @Param("example") UiScenarioReportResultExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportResultWithBLOBs record, @Param("example") UiScenarioReportResultExample example);

    int updateByExample(@Param("record") UiScenarioReportResult record, @Param("example") UiScenarioReportResultExample example);

    int updateByPrimaryKeySelective(UiScenarioReportResultWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportResultWithBLOBs record);

    int updateByPrimaryKey(UiScenarioReportResult record);
}