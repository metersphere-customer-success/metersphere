package io.metersphere.base.mapper;

import java.util.List;

import io.metersphere.base.domain.UiScenarioReport;
import io.metersphere.base.domain.UiScenarioReportExample;
import io.metersphere.base.domain.UiScenarioReportWithBLOBs;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportMapper {
    long countByExample(UiScenarioReportExample example);

    int deleteByExample(UiScenarioReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReportWithBLOBs record);

    int insertSelective(UiScenarioReportWithBLOBs record);

    List<UiScenarioReportWithBLOBs> selectByExampleWithBLOBs(UiScenarioReportExample example);

    List<UiScenarioReport> selectByExample(UiScenarioReportExample example);

    UiScenarioReportWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReportWithBLOBs record, @Param("example") UiScenarioReportExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportWithBLOBs record, @Param("example") UiScenarioReportExample example);

    int updateByExample(@Param("record") UiScenarioReport record, @Param("example") UiScenarioReportExample example);

    int updateByPrimaryKeySelective(UiScenarioReportWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportWithBLOBs record);

    int updateByPrimaryKey(UiScenarioReport record);
    void updateExecuteTime(@Param("id") String id,@Param("time") Long time);
}