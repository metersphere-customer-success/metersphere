package io.metersphere.utils;

import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.UiScenarioModule;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.dto.NodeTree;
import io.metersphere.service.UiScenarioModuleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

public class UiScenarioImportUtil {

    public static UiScenarioModule getSelectModule(String moduleId) {
        return getSelectModule(moduleId, null);
    }

    public static UiScenarioModule getSelectModule(String moduleId, String userId) {
        UiScenarioModuleService uiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        if (StringUtils.isNotBlank(moduleId) && !StringUtils.equals("root", moduleId)) {
            UiScenarioModule module = new UiScenarioModule();
            ModuleNodeDTO moduleDTO = uiModuleService.getNode(moduleId);
            if (moduleDTO != null) {
                BeanUtils.copyBean(module, moduleDTO);
            } else {
                if (StringUtils.isNotBlank(userId)) {
                    module.setCreateUser(userId);
                }
            }
            return module;
        }
        return null;
    }

    public static String getSelectModulePath(String path, String pid) {
        UiScenarioModuleService apiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        if (StringUtils.isNotBlank(pid)) {
            ModuleNodeDTO moduleDTO = apiModuleService.getNode(pid);
            if (moduleDTO != null) {
                return getSelectModulePath(moduleDTO.getName() + "/" + path, moduleDTO.getParentId());
            }
        }
        return "/" + path;
    }

    public static ModuleNode buildModule(ModuleNode parentModule, String name, String projectId) {
        UiScenarioModuleService uiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        ModuleNode module;
        if (parentModule != null) {
            module = uiModuleService.getNewModule(name, projectId, parentModule.getLevel() + 1);
            module.setParentId(parentModule.getId());
        } else {
            module = uiModuleService.getNewModule(name, projectId, 1);
        }
        createModule(module);
        return module;
    }

    private static void createNodeTree(NodeTree nodeTree, String pid, String projectId,
                                       UiScenarioModuleService uiModuleService, String path, int baseLevel) {
        ModuleNode module = new ModuleNode();
        BeanUtils.copyBean(module, nodeTree);
        uiModuleService.buildNewModule(module);
        module.setProjectId(projectId);
        module.setParentId(pid);
        module.setLevel(module.getLevel() + baseLevel);
        createModule(module, SessionUtils.getUserId());
        nodeTree.setNewId(module.getId());
        path = path + nodeTree.getName();
        nodeTree.setPath(path);
        List<NodeTree> children = nodeTree.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            String finalPath = path;
            children.forEach(item -> {
                createNodeTree(item, module.getId(), projectId, uiModuleService, finalPath + "/", baseLevel);
            });
        }
    }

    /**
     * 根据导出的模块树，创建新的模块树
     *
     * @param nodeTree
     * @param projectId
     */
    public static void createNodeTree(List<NodeTree> nodeTree, String projectId, String moduleId) {
        UiScenarioModuleService apiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        Iterator<NodeTree> iterator = nodeTree.iterator();
        boolean hasModuleSelected = false;
        ModuleNodeDTO selectModule = null;
        if (StringUtils.isNotBlank(moduleId) && !"root".equals(moduleId)) {
            selectModule = apiModuleService.getNode(moduleId);
            hasModuleSelected = true;
        }
        while (iterator.hasNext()) {
            NodeTree node = iterator.next();
            createNodeTree(node, hasModuleSelected ? selectModule.getId() : null,
                    projectId, apiModuleService, "/", hasModuleSelected ? selectModule.getLevel() : 0);
        }
    }

    public static ModuleNode buildModule(ModuleNode parentModule, String name, String projectId, String userId) {
        UiScenarioModuleService uiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        ModuleNode module;
        if (parentModule != null) {
            module = uiModuleService.getNewModule(name, projectId, parentModule.getLevel() + 1);
            module.setParentId(parentModule.getId());
        } else {
            module = uiModuleService.getNewModule(name, projectId, 1);
        }
        createModule(module, userId);
        return module;
    }

    public static void createModule(ModuleNode module) {
        createModule(module, null);
    }

    public static void createModule(ModuleNode module, String userId) {
        UiScenarioModuleService apiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        if (module.getName().length() > 64) {
            module.setName(module.getName().substring(0, 64));
        }
        List<ModuleNode> apiModules = apiModuleService.selectSameModule(module);
        if (CollectionUtils.isEmpty(apiModules)) {
            if (StringUtils.isNotBlank(userId)) {
                module.setCreateUser(userId);
            }
            apiModuleService.addNode(module);
        } else {
            module.setId(apiModules.get(0).getId());
        }
    }
}
