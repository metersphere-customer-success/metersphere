package io.metersphere.xpack.controller;


import io.metersphere.base.domain.ModuleNode;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.scenario.request.testcase.DragNodeRequest;
import io.metersphere.service.UiElementModuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/ui/element/module")
@RestController
public class UiElementModuleController {

    @Resource
    UiElementModuleService uiElementModuleService;

    @PostMapping("/list/{projectId}")
    public List<ModuleNodeDTO> getNodeByProjectId(@PathVariable String projectId, @RequestBody BaseQueryRequest request) {
        return uiElementModuleService.getNodeTreeByProjectIdKeyWord(projectId, "未规划元素", request);
    }

    @PostMapping("/add")
    @MsRequestLog(module = OperLogModule.UI_ELEMENT)
    public String addNode(@RequestBody ModuleNode node) {
        return uiElementModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @MsRequestLog(module = OperLogModule.UI_ELEMENT)
    public int editNode(@RequestBody DragNodeRequest node) {
        return uiElementModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @MsRequestLog(module = OperLogModule.UI_ELEMENT)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return uiElementModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsRequestLog(module = OperLogModule.UI_ELEMENT)
    public void dragNode(@RequestBody DragNodeRequest node) {
        uiElementModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    @MsRequestLog(module = OperLogModule.UI_ELEMENT)
    public void treeSort(@RequestBody List<String> ids) {
        uiElementModuleService.sort(ids);
    }
}
