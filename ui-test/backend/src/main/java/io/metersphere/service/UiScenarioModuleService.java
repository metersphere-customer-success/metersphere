package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestPlanUiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.constants.ScenarioStatus;
import io.metersphere.constants.UiScenarioType;
import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.dto.UiScenarioRequest;
import io.metersphere.scenario.request.testcase.QueryNodeRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioModuleService extends BaseModuleService {
    @Resource
    private UiAutomationService uiAutomationService;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;

    public UiScenarioModuleService() {
        super("ui_scenario_module");
    }

    public int deleteNode(List<String> nodeIds) {
        return deleteNode(nodeIds, uiAutomationService::deleteByNodeIds);
    }

    public ModuleNode getNewModule(String name, String projectId, int level) {
        ModuleNode node = new ModuleNode();
        buildNewModule(node);
        node.setLevel(level);
        node.setName(name);
        node.setProjectId(projectId);
        return node;
    }

    public ModuleNode buildNewModule(ModuleNode node) {
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        return node;
    }

    public ModuleNode getDefaultNode(String projectId) {
        return super.getDefaultNode(projectId, "未规划场景");
    }

    public ModuleNode getDefaultCustomNode(String projectId) {
        return super.getDefaultNode(projectId, "未规划模块");
    }

    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    private ExtUiScenarioMapper extUiScenarioMapper;

    public List<ModuleNodeDTO> getNodeTreeByProjectId(String projectId, String type) {
        String defaultName = "未规划场景";
        if(StringUtils.isBlank(type)){
            type = UiScenarioType.SCENARIO.getType();
        }

        if(StringUtils.equalsIgnoreCase(type, UiScenarioType.CUSTOM_COMMAND.getType())){
            type = UiScenarioType.CUSTOM_COMMAND.getType();
            defaultName = "未规划模块";
        }
        QueryNodeRequest request = new QueryNodeRequest();
        request.setProjectId(projectId);
        List<String> list = new ArrayList<>();
        list.add("Prepare");
        list.add("Underway");
        list.add("Completed");
        Map<String, List<String>> filters = new LinkedHashMap<>();
        filters.put("status", list);
        request.setFilters(filters);
        request.setScenarioType(type);

        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNodeWithType(projectId, type, defaultName);
        List<ModuleNodeDTO> nodes = extModuleNodeMapper.getNodeTreeByProjectIdWithType("ui_scenario_module",type, projectId);
        buildNodeCount(projectId, nodes, extUiScenarioMapper::listModuleByCollection, request);
        return getNodeTrees(nodes);
    }

    public List<ModuleNodeDTO> getNodeTreeByProjectId(String projectId, String type,  UiScenarioRequest param) {
        String defaultName = "未规划场景";
        if(StringUtils.isBlank(type)){
            type = UiScenarioType.SCENARIO.getType();
        }

        if(StringUtils.equalsIgnoreCase(type, UiScenarioType.CUSTOM_COMMAND.getType())){
            type = UiScenarioType.CUSTOM_COMMAND.getType();
            defaultName = "未规划模块";
        }
        QueryNodeRequest request = new QueryNodeRequest();
        request.setProjectId(projectId);
//        List<String> list = new ArrayList<>();
//        list.add("Prepare");
//        list.add("Underway");
//        list.add("Completed");
//        Map<String, List<String>> filters = new LinkedHashMap<>();
//        filters.put("status", list);
//        request.setFilters(filters);
        request.setScenarioType(type);
        BeanUtils.copyBean(request, param);
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNodeWithType(projectId, type, defaultName);
        List<ModuleNodeDTO> nodes = extModuleNodeMapper.getNodeTreeByProjectIdWithType("ui_scenario_module",type, projectId);
        buildNodeCount(projectId, nodes, extUiScenarioMapper::listModuleByCollection, request);
        return getNodeTrees(nodes);
    }

    public List<ModuleNodeDTO> getTrashNodeTreeByProjectId(String projectId, String type) {
        String defaultName = "未规划场景";
        if(StringUtils.isBlank(type)){
            type = UiScenarioType.SCENARIO.getType();
        }

        if(StringUtils.equalsIgnoreCase(type, UiScenarioType.CUSTOM_COMMAND.getType())){
            type = UiScenarioType.CUSTOM_COMMAND.getType();
            defaultName = "未规划模块";
        }
        //回收站数据初始化：检查是否存在模块被删除的接口，则把接口挂再默认节点上
        initTrashDataModule(projectId, type, defaultName);
        //通过回收站里的接口模块进行反显
        Map<String, List<UiScenario>> trashScenarioMap = uiAutomationService.selectBaseInfoGroupByModuleId(projectId, ScenarioStatus.Trash.name(), type);
        //查找回收站里的模块
        List<ModuleNodeDTO> trashModuleList = this.selectModuleStructById(trashScenarioMap.keySet());
        this.initScenarioCount(trashModuleList, trashScenarioMap);
        return getNodeTrees(trashModuleList);
    }

    public List<ModuleNodeDTO> getTrashNodeTreeByProjectId(String projectId, String type, UiScenarioRequest param) {
        String defaultName = "未规划场景";
        if(StringUtils.isBlank(type)){
            type = UiScenarioType.SCENARIO.getType();
        }

        if(StringUtils.equalsIgnoreCase(type, UiScenarioType.CUSTOM_COMMAND.getType())){
            type = UiScenarioType.CUSTOM_COMMAND.getType();
            defaultName = "未规划模块";
        }
        //回收站数据初始化：检查是否存在模块被删除的接口，则把接口挂再默认节点上
        initTrashDataModule(projectId, type, defaultName);
        //通过回收站里的接口模块进行反显
        param.setModuleIds(null);
        Map<String, List<UiScenario>> trashScenarioMap = uiAutomationService.selectBaseInfoGroupByCondition(projectId, ScenarioStatus.Trash.name(), type, param);
        //查找回收站里的模块
        List<ModuleNodeDTO> trashModuleList = this.selectModuleStructById(trashScenarioMap.keySet());
        this.initScenarioCount(trashModuleList, trashScenarioMap);
        return getNodeTrees(trashModuleList);
    }

    private void initScenarioCount(List<ModuleNodeDTO> moduleList, Map<String, List<UiScenario>> scenarioMap) {
        if (CollectionUtils.isNotEmpty(moduleList) && MapUtils.isNotEmpty(scenarioMap)) {
            moduleList.forEach(node -> {
                List<String> moduleIds = new ArrayList<>();
                moduleIds = this.nodeList(moduleList, node.getId(), moduleIds);
                moduleIds.add(node.getId());
                int countNum = 0;
                for (String moduleId : moduleIds) {
                    if (scenarioMap.containsKey(moduleId)) {
                        countNum += scenarioMap.get(moduleId).size();
                    }
                }
                node.setCaseNum(countNum);
            });
        }
    }

    private List<ModuleNodeDTO> selectModuleStructById(Collection<String> moduleIdList) {
        if (CollectionUtils.isEmpty(moduleIdList)) {
            return new ArrayList<>(0);
        } else {
            List<String> parentIdList = new ArrayList<>();
            List<ModuleNodeDTO> moduleList = extModuleNodeMapper.selectByIds("ui_scenario_module", moduleIdList);
            moduleList.forEach(moduleDTO -> {
                if (StringUtils.isNotBlank(moduleDTO.getParentId()) && !parentIdList.contains(moduleDTO.getParentId())) {
                    parentIdList.add(moduleDTO.getParentId());
                }
            });
            moduleList.addAll(0, this.selectModuleStructById(parentIdList));
            List<ModuleNodeDTO> returnList = new ArrayList<>(moduleList.stream().collect(Collectors.toMap(ModuleNodeDTO::getId, Function.identity(), (t1, t2) -> t1)).values());
            return returnList;
        }
    }

    private void initTrashDataModule(String projectId, String type, String defaultName) {
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.getDefaultNodeWithType(projectId, type, defaultName);
    }


    private Map<String, Integer> parseModuleCountList(List<Map<String, Object>> moduleCountList) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (Map<String, Object> map : moduleCountList) {
            Object moduleIdObj = map.get("moduleId");
            Object countNumObj = map.get("countNum");
            if (moduleIdObj != null && countNumObj != null) {
                String moduleId = String.valueOf(moduleIdObj);
                try {
                    Integer countNumInteger = new Integer(String.valueOf(countNumObj));
                    returnMap.put(moduleId, countNumInteger);
                } catch (Exception e) {
                }
            }
        }
        return returnMap;
    }

    public List<TestPlanUiScenario> getUiScenarioByPlanId(String planId) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanUiScenarioMapper.selectByExample(example);
    }

}
