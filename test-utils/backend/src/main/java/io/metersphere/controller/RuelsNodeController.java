package io.metersphere.controller;

import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.DataRulesNode;
import io.metersphere.dto.DataRulesNodeDTO;
import io.metersphere.dto.QueryDataRulesRequest;
import io.metersphere.request.DragDataRulesRequest;
import io.metersphere.service.DataRulesNodeService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rules/node")
public class RuelsNodeController {
    @Resource
    private DataRulesNodeService dataRulesNodeService;

    @PostMapping(value = "/list/{projectId}")
    public List<DataRulesNodeDTO> ruleNodeList(@PathVariable String projectId, @RequestBody(required = false) QueryDataRulesRequest request) {

        // 高级搜索所属模块搜索时, 切换项目时需替换projectId为参数中切换项目
        if (request != null && request.getProjectId() != null) {
            projectId = request.getProjectId();
        }
        return dataRulesNodeService.getNodeTreeByProjectId(projectId,
                Optional.ofNullable(request).orElse(new QueryDataRulesRequest()));
    }

    @PostMapping("/add")
    public String addNode(@RequestBody DataRulesNode node) {
        return dataRulesNodeService.addNode(node);
    }

    @PostMapping("/edit")
    public int editNode(@RequestBody DataRulesNode node) {
        return dataRulesNodeService.editNode(node);
    }

    @PostMapping("/delete")
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return dataRulesNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    public void dragNode(@RequestBody DragDataRulesRequest node) {
        dataRulesNodeService.dragNode(node);
    }

    @PostMapping("/pos")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT})
    public void treeSort(@RequestBody List<String> ids) {
        dataRulesNodeService.sort(ids);
    }

}
