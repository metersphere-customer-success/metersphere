package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiScenarioExecutionInfo;
import io.metersphere.base.domain.UiScenarioExecutionInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioExecutionInfoMapper {
    long countByExample(UiScenarioExecutionInfoExample example);

    int deleteByExample(UiScenarioExecutionInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioExecutionInfo record);

    int insertSelective(UiScenarioExecutionInfo record);

    List<UiScenarioExecutionInfo> selectByExample(UiScenarioExecutionInfoExample example);

    UiScenarioExecutionInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioExecutionInfo record, @Param("example") UiScenarioExecutionInfoExample example);

    int updateByExample(@Param("record") UiScenarioExecutionInfo record, @Param("example") UiScenarioExecutionInfoExample example);

    int updateByPrimaryKeySelective(UiScenarioExecutionInfo record);

    int updateByPrimaryKey(UiScenarioExecutionInfo record);
}