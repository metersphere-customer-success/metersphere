package io.metersphere.service;

import io.metersphere.commons.utils.JSON;
import org.json.JSONArray;
import io.metersphere.base.domain.UiScenarioExecutionInfo;
import io.metersphere.base.domain.UiScenarioExecutionInfoExample;
import io.metersphere.base.mapper.UiScenarioExecutionInfoMapper;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UiScenarioExecutionInfoService {
    @Resource
    private UiScenarioExecutionInfoMapper uiScenarioExecutionInfoMapper;

    @Lazy
    public void insertExecutionInfo(String scenarioId, String result, String triggerMode) {
        if (StringUtils.isNotEmpty(scenarioId) && StringUtils.isNotEmpty(result)) {
            UiScenarioExecutionInfo executionInfo = new UiScenarioExecutionInfo();
            executionInfo.setResult(result);
            executionInfo.setSourceId(scenarioId);
            executionInfo.setId(UUID.randomUUID().toString());
            executionInfo.setCreateTime(System.currentTimeMillis());
            executionInfo.setTriggerMode(triggerMode);
            uiScenarioExecutionInfoMapper.insert(executionInfo);
        }
    }

    public void insertExecutionInfoByScenarioIds(String scenarioIdJsonString, String status, String triggerMode) {
        try {
            List<String> scenarioIdList = JSON.parseArray(scenarioIdJsonString, String.class);
            for (String scenarioId : scenarioIdList) {
                this.insertExecutionInfo(scenarioId, status, triggerMode);
            }
        } catch (Exception e) {
            LogUtil.error("解析场景ID的JSON" + scenarioIdJsonString + "失败！", e);
        }
    }

    public void deleteByScenarioIdList(List<String> resourceIdList) {
        if (CollectionUtils.isNotEmpty(resourceIdList)) {
            UiScenarioExecutionInfoExample example = new UiScenarioExecutionInfoExample();
            example.createCriteria().andSourceIdIn(resourceIdList);
            uiScenarioExecutionInfoMapper.deleteByExample(example);
        }
    }
}
