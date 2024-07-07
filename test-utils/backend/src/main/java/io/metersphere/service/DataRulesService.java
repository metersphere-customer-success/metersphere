package io.metersphere.service;

import io.metersphere.base.domain.TestPlan;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.db.mapper.DataRulesMapper;
import io.metersphere.dto.BatchOperateRequest;
import io.metersphere.dto.DataRules;
import io.metersphere.dto.DataRulesExample;
import io.metersphere.i18n.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataRulesService {

    @Resource
    DataRulesMapper dataRulesMapper;


    public DataRules addDataRule(DataRules dataRules) {
        if (getDataRulesByName(dataRules.getName()).size() > 0) {
            MSException.throwException(Translator.get("plan_name_already_exists"));
        }
        dataRules.setCreateTime(System.currentTimeMillis());
        dataRules.setUpdateTime(System.currentTimeMillis());
        dataRules.setCreator(SessionUtils.getUser().getId());


        if (StringUtils.isBlank(dataRules.getProjectId())) {
            dataRules.setProjectId(SessionUtils.getCurrentProjectId());
        }
        dataRulesMapper.insert(dataRules);

        return dataRules;
    }

    public List<DataRules> getDataRulesByName(String name) {
        DataRulesExample example = new DataRulesExample();
        example.createCriteria()
                .andProjectIdEqualTo(SessionUtils.getCurrentProjectId())
                .andNameEqualTo(name);
        return dataRulesMapper.selectByExample(example);
    }

    public DataRules getDataRule(String dataRuleId) {
        return Optional.ofNullable(dataRulesMapper.selectByPrimaryKey(dataRuleId)).orElse(new DataRules());
    }

    public DataRules get(String dataRuleId) {
        return dataRulesMapper.selectByPrimaryKey(dataRuleId);
    }


    public DataRules editTestPlanWithRequest(DataRules request) {
        return editDataRule(request);
    }

    public DataRules editDataRule(DataRules dataRules) {
        checkTestPlanExist(dataRules);
        DataRules res = dataRulesMapper.selectByPrimaryKey(dataRules.getId()); //  先查一次库
        res.setUpdateTime(System.currentTimeMillis());
        dataRulesMapper.updateByPrimaryKeySelective(dataRules);
        return dataRulesMapper.selectByPrimaryKey(dataRules.getId());
    }

    private void checkTestPlanExist(DataRules testPlan) {
        if (testPlan.getName() != null) {
            DataRulesExample example = new DataRulesExample();
            example.createCriteria()
                    .andNameEqualTo(testPlan.getName())
                    .andProjectIdEqualTo(testPlan.getProjectId())
                    .andIdNotEqualTo(testPlan.getId());
            if (dataRulesMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("plan_name_already_exists"));
            }
        }
    }

    public int deleteDataRule(String ruleId) {


        return dataRulesMapper.deleteByPrimaryKey(ruleId);
    }

    public int deleteDataRulesBatch(BatchOperateRequest request) {
        List<String> ruleIds = request.getIds();
        if (CollectionUtils.isEmpty(ruleIds)) {
            return 0;
        }
        DataRulesExample testPlanExample = new DataRulesExample();
        testPlanExample.createCriteria().andIdIn(ruleIds);
        int deletedSize = dataRulesMapper.deleteByExample(testPlanExample);
        return deletedSize;
    }


    public List<DataRules> listDataRule(DataRules request) {
        DataRulesExample dataRulesExample = new DataRulesExample();
        List<DataRules> dataRules = dataRulesMapper.selectByExample(dataRulesExample);
        return dataRules;
    }



}
