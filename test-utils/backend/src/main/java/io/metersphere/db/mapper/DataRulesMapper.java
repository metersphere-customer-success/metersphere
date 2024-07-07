package io.metersphere.db.mapper;

import io.metersphere.dto.DataRules;
import io.metersphere.dto.DataRulesExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataRulesMapper {
    long countByExample(DataRulesExample example);

    int deleteByExample(DataRulesExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataRules record);

    int insertSelective(DataRules record);

    List<DataRules> selectByExample(DataRulesExample example);

    DataRules selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataRules record, @Param("example") DataRulesExample example);

    int updateByExample(@Param("record") DataRules record, @Param("example") DataRulesExample example);

    int updateByPrimaryKeySelective(DataRules record);

    int updateByPrimaryKey(DataRules record);
}