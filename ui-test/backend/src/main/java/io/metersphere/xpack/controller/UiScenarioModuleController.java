package io.metersphere.xpack.controller;

import io.metersphere.base.domain.ModuleNode;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.constants.UiScenarioType;
import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.dto.UiScenarioRequest;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.scenario.request.testcase.DragNodeRequest;
import io.metersphere.service.UiScenarioModuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/ui/scenario/module")
@RestController
public class UiScenarioModuleController {

    @Resource
    UiScenarioModuleService uiScenarioModuleService;

    @GetMapping("/list/{projectId}")
    public List<ModuleNodeDTO> getNodeByProjectId(@PathVariable String projectId, @RequestParam(name = "type", required = false) String type) {
        return uiScenarioModuleService.getNodeTreeByProjectId(projectId, type);
    }

    @PostMapping("/list/{projectId}/{type}")
    public List<ModuleNodeDTO> getNodeByConditionType(@PathVariable String projectId, @PathVariable String type, @RequestBody UiScenarioRequest param) {
        return uiScenarioModuleService.getNodeTreeByProjectId(projectId, type, param);
    }

    @GetMapping("/trash/list/{projectId}")
    public List<ModuleNodeDTO> getTrashNodeByProjectId(@PathVariable String projectId, @RequestParam(name = "type", required = false) String type) {
        return uiScenarioModuleService.getTrashNodeTreeByProjectId(projectId, type);
    }

    @PostMapping("/trash/list/{projectId}/{type}")
    public List<ModuleNodeDTO> getTrashNodeByConditionType(@PathVariable String projectId, @PathVariable String type, @RequestBody UiScenarioRequest param) {
        return uiScenarioModuleService.getTrashNodeTreeByProjectId(projectId, type, param);
    }

    @GetMapping("/custom/list/{projectId}")
    public List<ModuleNodeDTO> getCustomNodeByProjectId(@PathVariable String projectId) {
        return uiScenarioModuleService.getNodeTreeByProjectId(projectId, UiScenarioType.CUSTOM_COMMAND.getType());
    }

    @GetMapping("/custom/trash/list/{projectId}")
    public List<ModuleNodeDTO> getCustomTrashNodeByProjectId(@PathVariable String projectId) {
        return uiScenarioModuleService.getTrashNodeTreeByProjectId(projectId, UiScenarioType.CUSTOM_COMMAND.getType());
    }

    @PostMapping("/add")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public String addNode(@RequestBody ModuleNode node) {
        if(StringUtils.isBlank(node.getScenarioType())){
            node.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        return uiScenarioModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public int editNode(@RequestBody DragNodeRequest node) {
        if(StringUtils.isBlank(node.getScenarioType())){
            node.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        return uiScenarioModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return uiScenarioModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public void dragNode(@RequestBody DragNodeRequest node) {
        if(StringUtils.isBlank(node.getScenarioType())){
            node.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        uiScenarioModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    public void treeSort(@RequestBody List<String> ids) {
        uiScenarioModuleService.sort(ids);
    }
}
