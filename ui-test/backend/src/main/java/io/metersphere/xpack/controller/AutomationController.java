package io.metersphere.xpack.controller;

import io.metersphere.base.domain.UiScenario;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.SaveUiScenarioRequest;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.UiAutomationService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/automation")
@RestController
public class AutomationController {

    @Resource
    private UiAutomationService uiAutomationService;

    @PostMapping(value = "/create")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_CREATE, PermissionConstants.PROJECT_UI_SCENARIO_READ_COPY}, logical = Logical.OR)
    public UiScenario create(@RequestPart("request") SaveUiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                             @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return uiAutomationService.create(request, bodyFiles, scenarioFiles);
    }

    @PostMapping(value = "/update")
    @MsRequestLog(module = OperLogModule.UI_AUTOMATION)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_UI_SCENARIO_READ_EDIT, PermissionConstants.PROJECT_UI_SCENARIO_READ_COPY}, logical = Logical.OR)
    public UiScenario update(@RequestPart("request") SaveUiScenarioRequest request, @RequestPart(value = "bodyFiles", required = false) List<MultipartFile> bodyFiles,
                             @RequestPart(value = "scenarioFiles", required = false) List<MultipartFile> scenarioFiles) {
        return uiAutomationService.update(request, bodyFiles, scenarioFiles);
    }

}
