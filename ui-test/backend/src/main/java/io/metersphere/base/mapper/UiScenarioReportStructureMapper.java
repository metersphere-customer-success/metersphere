package io.metersphere.base.mapper;

import java.util.List;

import io.metersphere.base.domain.UiScenarioReportStructure;
import io.metersphere.base.domain.UiScenarioReportStructureExample;
import io.metersphere.base.domain.UiScenarioReportStructureWithBLOBs;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportStructureMapper {
    long countByExample(UiScenarioReportStructureExample example);

    int deleteByExample(UiScenarioReportStructureExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReportStructureWithBLOBs record);

    int insertSelective(UiScenarioReportStructureWithBLOBs record);

    List<UiScenarioReportStructureWithBLOBs> selectByExampleWithBLOBs(UiScenarioReportStructureExample example);

    List<UiScenarioReportStructure> selectByExample(UiScenarioReportStructureExample example);

    UiScenarioReportStructureWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReportStructureWithBLOBs record, @Param("example") UiScenarioReportStructureExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportStructureWithBLOBs record, @Param("example") UiScenarioReportStructureExample example);

    int updateByExample(@Param("record") UiScenarioReportStructure record, @Param("example") UiScenarioReportStructureExample example);

    int updateByPrimaryKeySelective(UiScenarioReportStructureWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportStructureWithBLOBs record);

    int updateByPrimaryKey(UiScenarioReportStructure record);
}