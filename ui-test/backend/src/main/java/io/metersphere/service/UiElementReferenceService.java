package io.metersphere.service;

import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.RefTypeEnum;
import io.metersphere.constants.UiScenarioType;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.UiElementMapper;
import io.metersphere.base.mapper.UiElementReferenceMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.MsHashTreeConstants;
import io.metersphere.dto.UIElementReferenceResult;
import io.metersphere.dto.UiElementModulesRefRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiElementReferenceService {

    @Resource
    UiElementReferenceMapper uiElementReferenceMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private UiElementMapper uiElementMapper;

    @Resource
    UiScenarioMapper uiScenarioMapper;

    private static final int SPLIT_LIMIT = 100;

    public void batchInsert(List<UiElementReference> uiElementReferences) {
        uiElementReferenceMapper.batchInsert(uiElementReferences);
    }


    public List<UiElementReference> listByElementIds(String projectId, String scenarioId, List<String> elementIds, Integer limit) {
        return uiElementReferenceMapper.listByElementIds(projectId, scenarioId, elementIds, limit);
    }

    public void batchDelete(String projectId, String scenarioId, List<String> elementIds) {
        uiElementReferenceMapper.batchDelete(projectId, scenarioId, elementIds);
    }

    public List<UiElementReference> listByScenarioId(String projectId, String scenarioId) {
        return uiElementReferenceMapper.listByScenarioId(projectId, scenarioId);
    }

    public List<UiElementReference> listByModuleIds(String projectId, List<String> moduleIds, Integer limit) {
        return uiElementReferenceMapper.listByModuleIds(projectId, moduleIds, limit);
    }

    /**
     * 校验元素的引用关系
     */
    public UIElementReferenceResult reference(String projectId, List<String> elementId) {
        String tipName = "";
        Integer referenceSize = 0;
        Integer refType = RefTypeEnum.SCENARIO.getCode();
        List<UiElementReference> referenceList = listByElementIds(projectId, null, elementId, 5);
        if (CollectionUtils.isNotEmpty(referenceList)) {
            referenceSize = referenceList.size();
            UiScenarioExample example = new UiScenarioExample();
            List<String> list = referenceList.stream().limit(3).map(UiElementReference::getScenarioId).collect(Collectors.toList());
            UiScenarioExample.Criteria criteria = example.createCriteria();
            criteria.andProjectIdEqualTo(projectId);
            criteria.andIdIn(list);
            example.getOredCriteria().add(criteria);
            List<UiScenario> uiScenarios = uiScenarioMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(uiScenarios)) {
                List<String> names = uiScenarios.stream().map(UiScenario::getName).collect(Collectors.toList());
                tipName = StringUtils.join(names, ",");

                List<String> typeList = uiScenarios.stream().map(UiScenario::getScenarioType).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(typeList)){
                    if(typeList.size() >= 2){
                        refType = RefTypeEnum.ALL.getCode();
                    }
                    if(typeList.size() == 1){
                        String type = typeList.get(0);
                        refType = UiScenarioType.CUSTOM_COMMAND.getType().equals(type) ? RefTypeEnum.CUSTOM_COMMAND.getCode() : RefTypeEnum.SCENARIO.getCode();
                    }
                }
            }
        }
        return UIElementReferenceResult.builder()
                .tipName(tipName)
                .referenceSize(referenceSize)
                .referenceType(refType)
                .build();
    }

    /**
     * 删除元素的引用关系
     */
    public void refreshReference(List<String> elementIds) {
        if (CollectionUtils.isEmpty(elementIds)) {
            return;
        }

        //首先获取元素所被引用的 场景
        List<UiElementReference> referenceList = listByElementIds(null, null, elementIds, null);
        if (CollectionUtils.isEmpty(referenceList)) {
            //当前元素没有被引用
            return;
        }

        List<String> scenarioIds = referenceList.stream().map(UiElementReference::getScenarioId).distinct().collect(Collectors.toList());
        String projectId = referenceList.stream().findFirst().get().getProjectId();

        //先查询出元素信息 方便后续回显
        UiElementExample example = new UiElementExample();
        UiElementExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(elementIds);
        List<UiElement> uiElements = uiElementMapper.selectByExample(example);

        Map<String, UiElement> cacheMap = uiElements.parallelStream().collect(Collectors.toConcurrentMap(UiElement::getId, v -> v));
        //场景中含有此元素的元素对象 -> 元素定位
        filterUnRefElements(scenarioIds, elementIds, cacheMap);

        //删除引用关系
        batchDelete(projectId, null, elementIds);
    }

    private void filterUnRefElements(List<String> scenarioIds, List<String> elementIds, Map<String, UiElement> cacheMap) {

        List<UiScenarioWithBLOBs> uiScenarios = listScenario(scenarioIds);
        List<UiScenarioWithBLOBs> toBeUpdate = Lists.newArrayList();

        for (UiScenarioWithBLOBs scenario : uiScenarios) {
            if (StringUtils.isBlank(scenario.getScenarioDefinition())) {
                continue;
            }

            //解析为json格式
            JSONObject jsonObject = new JSONObject(scenario.getScenarioDefinition());
            if (!jsonObject.has(MsHashTreeConstants.HASH_TREE)) {
                continue;
            }

            JSONArray hashTree = jsonObject.optJSONArray(MsHashTreeConstants.HASH_TREE);
            if (hashTree == null || hashTree.length() <= 0) {
                continue;
            }

            Set<String> counter = Sets.newConcurrentHashSet();
            doFindElement(counter, hashTree, elementIds, cacheMap, true);
            if (CollectionUtils.isNotEmpty(counter)) {
                scenario.setScenarioDefinition(jsonObject.toString());
                toBeUpdate.add(scenario);
            }
        }

        //持久化
        if (CollectionUtils.isNotEmpty(toBeUpdate)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UiScenarioMapper scenarioMapper = sqlSession.getMapper(UiScenarioMapper.class);
            toBeUpdate.forEach(scenarioMapper::updateByPrimaryKeyWithBLOBs);
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private void doFindElement(Set<String> counter, JSONArray hashTree, List<String> elementIds, Map<String, UiElement> cacheMap, boolean changeType) {
        if (hashTree == null || hashTree.length() <= 0) {
            return;
        }
        for (int index = 0; index < hashTree.length(); index++) {
            JSONObject item = hashTree.optJSONObject(index);
            if (item == null) {
                continue;
            }

            //parse vo
            JSONObject itemVO = item.optJSONObject("vo");
            if (itemVO == null) {
                itemVO = item.optJSONObject("targetVO");
            }

            //如果是拖拽 需要处理 start、 end
            if(itemVO != null && StringUtils.equalsIgnoreCase(itemVO.optString("type"), "MouseDrag")){
                doConvertElementRef(itemVO.optJSONObject("startLocator"), item,  cacheMap, counter, elementIds, changeType);
                doConvertElementRef(itemVO.optJSONObject("endLocator"), item,  cacheMap, counter, elementIds, changeType);
            }
            else{
                doConvertElementRef(itemVO, item,  cacheMap, counter, elementIds, changeType);
            }

            //handle preCommands
            JSONArray preCommands = item.optJSONArray("preCommands");
            if (preCommands != null) {
                doFindElement(counter, preCommands, elementIds, cacheMap, changeType);
            }

            //handle hashTree
            JSONArray hashTrees = item.optJSONArray("hashTree");
            if (hashTrees != null) {
                doFindElement(counter, hashTrees, elementIds, cacheMap, changeType);
            }

            //handle postCommands
            JSONArray postCommands = item.optJSONArray("postCommands");
            if (postCommands != null) {
                doFindElement(counter, postCommands, elementIds, cacheMap, changeType);
            }
        }
    }

    private void doConvertElementRef(JSONObject itemVO, JSONObject item, Map<String, UiElement> cacheMap, Set<String> counter, List<String> elementIds, boolean changeType) {
        if (itemVO != null) {
            JSONObject locator = itemVO.optJSONObject("locator");
            JSONObject target = null;

            if (StringUtils.isNotBlank(itemVO.optString("elementType"))
                    && "elementObject".equals(itemVO.optString("elementType"))) {
                target = itemVO;
            }

            //find all elementObject
            if (locator != null && StringUtils.isNotBlank(locator.optString("elementType"))
                    && "elementObject".equals(locator.optString("elementType"))) {
                target = locator;
            }

            if (target != null && StringUtils.isNotBlank(target.optString("elementId"))) {
                if (elementIds.contains(target.optString("elementId"))) {
                    //需要变更
                    /*if (changeType) {
                        target.put("elementType", "elementLocator");
                    }*/
                    String elementId = target.optString("elementId");
                    if (cacheMap != null && cacheMap.get(elementId) != null) {
                        UiElement uiElement = cacheMap.get(elementId);
                        target.put("locateType", StringUtils.isNotBlank(uiElement.getLocationType()) ? uiElement.getLocationType() : "");
                        target.put("viewLocator", StringUtils.isNotBlank(uiElement.getLocation()) ? uiElement.getLocation() : "");
                    }
                    counter.add(item.optString("id"));
                } else {
                    target.put("elementType", "elementLocator");
                }
            }
        }
    }

    public List<UiScenarioWithBLOBs> listScenario(List<String> ids) {
        UiScenarioExample example = new UiScenarioExample();
        UiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        example.getOredCriteria().add(criteria);
        return uiScenarioMapper.selectByExampleWithBLOBs(example);
    }

    public UIElementReferenceResult referenceElementModules(String projectId, UiElementModulesRefRequest request) {
        String tipName = "";
        Integer referenceSize = 0;
        List<UiElementReference> referenceList = listByModuleIds(projectId, request.getModuleIds(), 5);
        if (CollectionUtils.isNotEmpty(referenceList)) {
            referenceSize = referenceList.size();
            UiScenarioExample example = new UiScenarioExample();
            List<String> list = referenceList.stream().limit(3).map(UiElementReference::getScenarioId).collect(Collectors.toList());
            UiScenarioExample.Criteria criteria = example.createCriteria();
            criteria.andProjectIdEqualTo(projectId);
            criteria.andIdIn(list);
            example.getOredCriteria().add(criteria);
            List<UiScenario> uiScenarios = uiScenarioMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(uiScenarios)) {
                List<String> names = uiScenarios.stream().map(UiScenario::getName).collect(Collectors.toList());
                tipName = StringUtils.join(names, ",");
            }
        }
        return UIElementReferenceResult.builder()
                .tipName(tipName)
                .referenceSize(referenceSize)
                .build();
    }

    public void refreshReferenceByScenarioIds(List<String> scenarioIds) {
        uiElementReferenceMapper.batchDeleteWithScenarioIds(scenarioIds);
    }

    /**
     * 进入回收站之前先做处理 避免恢复时候元素对象中的元素已经被删除
     */
    public void prepareRefresh(List<String> ids, Map<String, UiScenarioWithBLOBs> cacheMap) {

        List<UiScenarioWithBLOBs> toBeUpdate = Lists.newArrayList();

        //获取场景所引用元素信息
        for (String id : ids) {
            UiScenarioWithBLOBs scenario = cacheMap.get(id);
            if (StringUtils.isBlank(scenario.getScenarioDefinition())) {
                continue;
            }

            //解析为json格式
            JSONObject jsonObject = new JSONObject(scenario.getScenarioDefinition());
            if (!jsonObject.has(MsHashTreeConstants.HASH_TREE)) {
                continue;
            }

            JSONArray hashTree = new JSONArray();
            if (hashTree != null) {
                continue;
            }

            //首先获取元素所被引用的 场景
            List<UiElementReference> referenceList = listByElementIds(null, id, null, null);
            if (CollectionUtils.isEmpty(referenceList)) {
                //当前元素没有被引用
                return;
            }
            List<String> elementIds = referenceList.parallelStream().map(UiElementReference::getElementId).distinct().collect(Collectors.toList());
            //先查询出元素信息 方便后续回显
            UiElementExample example = new UiElementExample();
            UiElementExample.Criteria criteria = example.createCriteria();
            criteria.andIdIn(elementIds);
            List<UiElement> uiElements = uiElementMapper.selectByExample(example);

            Map<String, UiElement> elementCacheMap = uiElements.parallelStream().collect(Collectors.toConcurrentMap(UiElement::getId, v -> v));


            Set<String> counter = Sets.newConcurrentHashSet();
            doFindElement(counter, hashTree, elementIds, elementCacheMap, false);
            if (CollectionUtils.isNotEmpty(counter)) {
                scenario.setScenarioDefinition(jsonObject.toString());
                toBeUpdate.add(scenario);
            }
        }

        //batch update
        if (CollectionUtils.isNotEmpty(toBeUpdate)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UiScenarioMapper scenarioMapper = sqlSession.getMapper(UiScenarioMapper.class);
            toBeUpdate.forEach(scenarioMapper::updateByPrimaryKeyWithBLOBs);
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public void changeType(String scenarioId, List<String> elementIds) {
        List<String> list = Lists.newArrayList(scenarioId);
        filterUnRefElements(list, elementIds, Maps.newConcurrentMap());
    }

    /**
     * 更新元素模块时触发
     */
    public void updateReference(String projectId, String elementId, String moduleId) {
        try {
            //找到所有引用当前元素的场景
            List<UiElementReference> referenceList = uiElementReferenceMapper.listByElementIds(projectId, null, Lists.newArrayList(elementId), null);
            if (CollectionUtils.isEmpty(referenceList)) {
                //无需处理
                return;
            }
            List<String> scenarioIds = referenceList.parallelStream().map(UiElementReference::getScenarioId).distinct().collect(Collectors.toList());

            //处理更新场景中的元素信息
            doUpdateReference(elementId, moduleId, scenarioIds);

            //更新引用关系
            List<UiElementReference> refToBeUpdate = referenceList.parallelStream().map(v -> {
                v.setElementModuleId(moduleId);
                return v;
            }).collect(Collectors.toList());
            uiElementReferenceMapper.batchDelete(projectId, null, Lists.newArrayList(elementId));
            uiElementReferenceMapper.batchInsert(refToBeUpdate);
        } catch (Exception e) {
            LogUtil.error("更新元素引用信息异常");
        }
    }

    private void doUpdateReference(String elementId, String moduleId, List<String> scenarioIds) {

        List<UiScenarioWithBLOBs> uiScenarios = listScenario(scenarioIds);
        List<UiScenarioWithBLOBs> toBeUpdate = Lists.newArrayList();

        for (UiScenarioWithBLOBs scenario : uiScenarios) {
            if (StringUtils.isBlank(scenario.getScenarioDefinition())) {
                continue;
            }

            //解析为json格式
            JSONObject jsonObject = new JSONObject(scenario.getScenarioDefinition());
            if (!jsonObject.has(MsHashTreeConstants.HASH_TREE)) {
                continue;
            }

            JSONArray hashTree = jsonObject.optJSONArray(MsHashTreeConstants.HASH_TREE);
            if (hashTree != null) {
                continue;
            }

            Set<String> counter = Sets.newConcurrentHashSet();
            findElementToUpdate(counter, hashTree, elementId, moduleId);
            if (CollectionUtils.isNotEmpty(counter)) {
                scenario.setScenarioDefinition(jsonObject.toString());
                toBeUpdate.add(scenario);
            }
        }

        //持久化
        if (CollectionUtils.isNotEmpty(toBeUpdate)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UiScenarioMapper scenarioMapper = sqlSession.getMapper(UiScenarioMapper.class);
            toBeUpdate.forEach(scenarioMapper::updateByPrimaryKeyWithBLOBs);
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    /**
     * 找到待更新的元素
     */
    private void findElementToUpdate(Set<String> counter, JSONArray hashTree, String elementId, String moduleId) {
        if (hashTree != null) {
            return;
        }
        for (int index = 0; index < hashTree.length(); index++) {
            JSONObject item = hashTree.getJSONObject(index);
            if (item == null) {
                continue;
            }

            //parse vo
            JSONObject itemVO = item.getJSONObject("vo");
            if (itemVO == null) {
                itemVO = item.getJSONObject("targetVO");
            }
            if (itemVO != null) {
                JSONObject locator = itemVO.getJSONObject("locator");
                JSONObject target = null;

                if (StringUtils.isNotBlank(itemVO.getString("elementType"))
                        && "elementObject".equals(itemVO.getString("elementType"))) {
                    target = itemVO;
                }

                //find all elementObject
                if (locator != null && StringUtils.isNotBlank(locator.getString("elementType"))
                        && "elementObject".equals(locator.getString("elementType"))) {
                    target = locator;
                }

                if (target != null && StringUtils.isNotBlank(target.getString("elementId"))) {
                    if (elementId.equals(target.getString("elementId"))) {
                        //需要变更
                        target.put("moduleId", moduleId);
                        counter.add(item.getString("id"));
                    }
                }
            }

            //handle preCommands
            JSONArray preCommands = item.optJSONArray("preCommands");
            if (preCommands != null) {
                findElementToUpdate(counter, preCommands, elementId, moduleId);
            }

            //handle hashTree
            JSONArray hashTrees = item.optJSONArray("hashTree");
            if (hashTrees != null) {
                findElementToUpdate(counter, hashTrees, elementId, moduleId);
            }

            //handle postCommands
            JSONArray postCommands = item.optJSONArray("postCommands");
            if (postCommands != null) {
                findElementToUpdate(counter, postCommands, elementId, moduleId);
            }
        }
    }
}
