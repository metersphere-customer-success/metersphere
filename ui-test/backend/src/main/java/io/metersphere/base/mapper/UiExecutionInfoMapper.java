package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiExecutionInfo;
import io.metersphere.base.domain.UiExecutionInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiExecutionInfoMapper {
    long countByExample(UiExecutionInfoExample example);

    int deleteByExample(UiExecutionInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiExecutionInfo record);

    int insertSelective(UiExecutionInfo record);

    List<UiExecutionInfo> selectByExample(UiExecutionInfoExample example);

    UiExecutionInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiExecutionInfo record, @Param("example") UiExecutionInfoExample example);

    int updateByExample(@Param("record") UiExecutionInfo record, @Param("example") UiExecutionInfoExample example);

    int updateByPrimaryKeySelective(UiExecutionInfo record);

    int updateByPrimaryKey(UiExecutionInfo record);
}