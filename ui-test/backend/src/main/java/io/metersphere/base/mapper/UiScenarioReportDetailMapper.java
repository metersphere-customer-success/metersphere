package io.metersphere.base.mapper;

import java.util.List;

import io.metersphere.base.domain.UiScenarioReportDetail;
import io.metersphere.base.domain.UiScenarioReportDetailExample;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportDetailMapper {
    long countByExample(UiScenarioReportDetailExample example);

    int deleteByExample(UiScenarioReportDetailExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(UiScenarioReportDetail record);

    int insertSelective(UiScenarioReportDetail record);

    List<UiScenarioReportDetail> selectByExampleWithBLOBs(UiScenarioReportDetailExample example);

    List<UiScenarioReportDetail> selectByExample(UiScenarioReportDetailExample example);

    UiScenarioReportDetail selectByPrimaryKey(String reportId);

    int updateByExampleSelective(@Param("record") UiScenarioReportDetail record, @Param("example") UiScenarioReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportDetail record, @Param("example") UiScenarioReportDetailExample example);

    int updateByExample(@Param("record") UiScenarioReportDetail record, @Param("example") UiScenarioReportDetailExample example);

    int updateByPrimaryKeySelective(UiScenarioReportDetail record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportDetail record);

    int updateByPrimaryKey(UiScenarioReportDetail record);
}