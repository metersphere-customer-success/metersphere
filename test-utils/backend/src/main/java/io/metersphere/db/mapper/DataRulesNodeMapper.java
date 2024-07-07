package io.metersphere.db.mapper;

import io.metersphere.dto.DataRulesNode;
import io.metersphere.dto.DataRulesNodeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataRulesNodeMapper {
    long countByExample(DataRulesNodeExample example);

    int deleteByExample(DataRulesNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataRulesNode record);

    int insertSelective(DataRulesNode record);

    List<DataRulesNode> selectByExample(DataRulesNodeExample example);

    DataRulesNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataRulesNode record, @Param("example") DataRulesNodeExample example);

    int updateByExample(@Param("record") DataRulesNode record, @Param("example") DataRulesNodeExample example);

    int updateByPrimaryKeySelective(DataRulesNode record);

    int updateByPrimaryKey(DataRulesNode record);
}