package io.metersphere.service;

import com.google.common.collect.Lists;
import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.UiElement;
import io.metersphere.base.domain.UiElementExample;
import io.metersphere.base.domain.UiElementReference;
import io.metersphere.base.mapper.UiElementMapper;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtUiElementMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.dto.excel.UiElementExcelData;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.scenario.request.testcase.QueryNodeRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiElementModuleService extends BaseModuleService {

    @Resource
    private UiElementService uiElementService;
    @Resource
    private ExtUiElementMapper extUiElementMapper;
    @Resource
    private UiElementReferenceService uiElementReferenceService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private BaseProjectVersionMapper extProjectVersionMapper;

    private ThreadLocal<Integer> importCreateNum = new ThreadLocal<>();
    private ThreadLocal<Integer> beforeImportCreateNum = new ThreadLocal<>();

    public UiElementModuleService() {
        super("ui_element_module");
    }

    public int deleteNode(List<String> nodeIds) {
        //删除模块下的引用关系
        List<UiElementReference> referenceList = uiElementReferenceService.listByModuleIds(null, nodeIds, null);
        if (CollectionUtils.isNotEmpty(referenceList)) {
            List<String> elementIds = referenceList.stream().map(UiElementReference::getElementId).distinct().collect(Collectors.toList());
            uiElementReferenceService.refreshReference(elementIds);
        }
        return deleteNode(nodeIds, uiElementService::deleteByNodeIds);
    }

    @Override
    public List<ModuleNodeDTO> getNodeTreeByProjectId(String projectId, String defaultName) {
        return getNodeTreeByProjectIdWithCount(projectId, extUiElementMapper::groupByModuleId, defaultName);
    }

    public List<ModuleNodeDTO> getNodeTreeByProjectIdKeyWord(String projectId, String defaultName, BaseQueryRequest param) {
        QueryNodeRequest request = new QueryNodeRequest();
        request.setProjectId(projectId);
        BeanUtils.copyBean(request, param);
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNode(projectId, defaultName);
        List<ModuleNodeDTO> nodes = extModuleNodeMapper.getNodeTreeByProjectId("ui_element_module", projectId);
        buildNodeCount(projectId, nodes, extUiElementMapper::listModuleByCollection, request);
        return getNodeTrees(nodes);
    }

    /**
     * 检测模块下 元素名是否存在
     */
    public boolean checkElementExist(UiElementExcelData data, String projectId) {

        //处理nodePath
        String nodePath = data.getNodePath();
        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }

        //校验模块是否存在
        ModuleNode node = new ModuleNode();
        node.setProjectId(projectId);
        node.setModulePath(nodePath);
        List<ModuleNode> moduleNodes = selectByModulePath(node);
        if (CollectionUtils.isEmpty(moduleNodes)) {
            return false;
        }
        //若不存在 则直接返回 false
        Optional<ModuleNode> first = moduleNodes.stream().findFirst();
        if (first.isEmpty()) {
            return false;
        }

        ModuleNode moduleNode = first.get();
        //若存在 检测 模块下是否有 同名的元素 并且id不等于自己
        UiElementExample example = new UiElementExample();
        UiElementExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        criteria.andModuleIdEqualTo(moduleNode.getId());
        criteria.andNameEqualTo(data.getName());
        List<UiElement> uiElements = uiElementService.listByExample(example);
        if (CollectionUtils.isEmpty(uiElements)) {
            return false;
        }

        //检测是否是否同一个id
        if (StringUtils.isNotBlank(data.getCustomNum())) {
            List<UiElement> list = uiElements.stream().filter(v -> data.getCustomNum().equals(v.getNum() + "")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                //同一个id 更新操作
                return false;
            }
        }

        return true;
    }

    public void handleImportSave(String projectId, List<UiElementExcelData> toBeInsert, String version) {
        if (CollectionUtils.isEmpty(toBeInsert)) {
            return;
        }
        if (StringUtils.isBlank(version)) {
            version = extProjectVersionMapper.getDefaultVersion(projectId);
        }
        List<Integer> occupyIds = toBeInsert.stream().filter(v -> StringUtils.isNotBlank(v.getCustomNum())).map(v -> Integer.parseInt(v.getCustomNum())).distinct().collect(Collectors.toList());
        int nextNum = getNextNum(projectId);
        importCreateNum.set(nextNum);
        beforeImportCreateNum.set(nextNum);
        Map<String, String> modulePathMap = createModulePathMap(projectId, toBeInsert);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UiElementMapper mapper = sqlSession.getMapper(UiElementMapper.class);
        try {
            Integer num = importCreateNum.get();
            Integer beforeInsertId = beforeImportCreateNum.get();
            // 反向遍历，保持和文件顺序一致
            for (int i = toBeInsert.size() - 1; i > -1; i--) {
                UiElement uiElement = new UiElement();
                UiElementExcelData excelData = toBeInsert.get(i);

                uiElement.setLocation(excelData.getElementLocator());
                uiElement.setLocationType(excelData.getLocatorType());
                uiElement.setProjectId(projectId);
                uiElement.setId(UUID.randomUUID().toString());
                uiElement.setCreateUser(SessionUtils.getUserId());
                uiElement.setUpdateUser(SessionUtils.getUserId());
                uiElement.setName(excelData.getName());
                uiElement.setDescription(excelData.getDescription());
                uiElement.setCreateTime(System.currentTimeMillis());
                uiElement.setUpdateTime(System.currentTimeMillis());

                if (StringUtils.isNotBlank(excelData.getNodePath())) {
                    String[] modules = excelData.getNodePath().split("/");
                    StringBuilder path = new StringBuilder();
                    for (String module : modules) {
                        if (StringUtils.isNotBlank(module)) {
                            path.append("/");
                            path.append(module.trim());
                        }
                    }
                    excelData.setNodePath(path.toString());
                }
                uiElement.setModuleId(modulePathMap.get(excelData.getNodePath()));
                if (StringUtils.isBlank(excelData.getCustomNum())) {
                    while (occupyIds.contains(num)) {
                        num++;
                    }
                    uiElement.setNum(num);
                    num++;
                } else {
                    uiElement.setNum(Integer.parseInt(excelData.getCustomNum()));
                }
                uiElement.setRefId(uiElement.getId());
                uiElement.setVersionId(version);
                uiElement.setOrder((long) (toBeInsert.size() - (num - beforeInsertId)) * ServiceUtils.ORDER_STEP);
                uiElement.setLatest(true);
                mapper.insert(uiElement);
            }

            importCreateNum.set(num);
        } finally {
            sqlSession.commit();
            sqlSession.clearCache();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

    }

    private int getNextNum(String projectId) {
        UiElement element = extUiElementMapper.getMaxNumByProjectId(projectId);
        if (element == null || element.getNum() == null) {
            return 100001;
        } else {
            return Optional.ofNullable(element.getNum() + 1).orElse(100001);
        }
    }

    /**
     * 获取模块信息map 没有的则创建
     */
    private Map<String, String> createModulePathMap(String projectId, List<UiElementExcelData> toBeInsert) {
        List<String> nodePaths = toBeInsert.stream()
                .map(UiElementExcelData::getNodePath)
                .collect(Collectors.toList());

        return this.createNodes(nodePaths, projectId, "未规划元素");
    }

    public void handleImportUpdate(String projectId, List<UiElementExcelData> toBeUpdate, String version, Map<Integer, List<UiElement>> map) {
        if (CollectionUtils.isEmpty(toBeUpdate)) {
            return;
        }
        if (StringUtils.isBlank(version)) {
            version = extProjectVersionMapper.getDefaultVersion(projectId);
        }

        Map<String, String> modulePathMap = createModulePathMap(projectId, toBeUpdate);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UiElementMapper mapper = sqlSession.getMapper(UiElementMapper.class);

        try {
            for (UiElementExcelData excelData : toBeUpdate) {
                List<UiElement> uiElements = map.get(Integer.parseInt(excelData.getCustomNum()));
                if (CollectionUtils.isEmpty(uiElements)) {
                    continue;
                }
                UiElement uiElement = uiElements.get(0);

                uiElement.setLocation(excelData.getElementLocator());
                uiElement.setLocationType(excelData.getLocatorType());
                uiElement.setProjectId(projectId);
                uiElement.setUpdateUser(SessionUtils.getUserId());
                uiElement.setName(excelData.getName());
                uiElement.setUpdateTime(System.currentTimeMillis());
                uiElement.setVersionId(version);

                if (StringUtils.isNotBlank(excelData.getNodePath())) {
                    String[] modules = excelData.getNodePath().split("/");
                    StringBuilder path = new StringBuilder();
                    for (String module : modules) {
                        if (StringUtils.isNotBlank(module)) {
                            path.append("/");
                            path.append(module.trim());
                        }
                    }
                    excelData.setNodePath(path.toString());
                }
                uiElement.setModuleId(modulePathMap.get(excelData.getNodePath()));

                mapper.updateByPrimaryKeySelective(uiElement);
            }
            sqlSession.flushStatements();
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

    }

    public List<UiElement> findExistElementNum(String projectId, List<Integer> numList) {
        if (CollectionUtils.isEmpty(numList)) {
            return Lists.newArrayList();
        }
        UiElementExample example = new UiElementExample();
        UiElementExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        criteria.andNumIn(numList);
        List<UiElement> result = uiElementService.listByExample(example);
        if (CollectionUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        return result;
    }
}
