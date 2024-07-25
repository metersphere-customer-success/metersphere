package io.metersphere.xpack.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import io.metersphere.base.domain.UiElement;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.*;
import io.metersphere.dto.element.UiElementDto;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.security.CheckOwner;
import io.metersphere.service.UiElementReferenceService;
import io.metersphere.service.UiElementService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/ui/element")
@RestController
public class UiElementController {

    @Resource
    UiElementService uiElementService;
    @Resource
    UiElementReferenceService uiElementReferenceService;

    @GetMapping("/{id}")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ")
    public UiElement getElementById(@PathVariable String id) {
        return uiElementService.get(id);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<UiElementDto>> getNodeByProjectId(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseQueryRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, uiElementService.list(request));
    }

    @PostMapping("/list/name")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ")
    public List<UiElement> getNodeByProjectId(@RequestBody BaseQueryRequest request) {
        return uiElementService.listNames(request);
    }

    @PostMapping("/add")
    @RequiresPermissions(value = {"PROJECT_UI_ELEMENT:READ+CREATE", "PROJECT_UI_ELEMENT:READ+COPY"}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.UI_ELEMENT, type = OperLogConstants.CREATE, title = "#uiElement.name", content = "#msClass.getLogDetails(#uiElement.id)", project = "#uiElement.projectId", msClass = UiElementService.class)
    public String add(@RequestBody UiElement uiElement) {
        return uiElementService.add(uiElement);
    }

    @PostMapping("/edit")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ+EDIT")
    @MsAuditLog(module = OperLogModule.UI_ELEMENT, type = OperLogConstants.UPDATE, title = "#uiElement.name", beforeEvent = "#msClass.getLogDetails(#uiElement.id)", content = "#msClass.getLogDetails(#uiElement.id)", project = "#uiElement.projectId", msClass = UiElementService.class)
    @CheckOwner(resourceId = "#uiElement.id", resourceType = "ui_element")
    public int edit(@RequestBody UiElement uiElement) {
        return uiElementService.edit(uiElement);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ+EDIT")
    @MsAuditLog(module = OperLogModule.UI_ELEMENT, type = OperLogConstants.BATCH_UPDATE,  beforeEvent = "#msClass.getLogDetails(#batchVO.ids)", content = "#msClass.getLogDetails(#batchVO.ids)", project = "#batchVO.projectId" , msClass = UiElementService.class)
    public int batchEdit(@RequestBody UiElementBatchRequest batchVO) {
        return uiElementService.batchEdit(batchVO);
    }

    @PostMapping("/batch/copy")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ+EDIT")
    @MsAuditLog(module = OperLogModule.UI_ELEMENT, type = OperLogConstants.COPY, title = "#batchVO.name", project = "#batchVO.projectId", msClass = UiElementService.class)
    public void batchCopy(@RequestBody UiElementBatchRequest batchVO) {
        uiElementService.batchCopy(batchVO);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ+DELETE")
    @CheckOwner(resourceId = "#id", resourceType = "ui_element")
    public int deleteNode(@PathVariable String id) {
        return uiElementService.delete(id);
    }

    @PostMapping("/delete")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ+DELETE")
    @MsAuditLog(module = OperLogModule.UI_ELEMENT, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#batchVO.ids)", project = "#batchVO.projectId", msClass = UiElementService.class)
    public int deleteNodeBatch(@RequestBody BaseQueryRequest batchVO) {
        return uiElementService.delete(batchVO);
    }

    @GetMapping("/reference/{projectId}/{id}")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ+DELETE")
    public UIElementReferenceResult reference(@PathVariable("projectId") String projectId, @PathVariable String id) {
        return uiElementReferenceService.reference(projectId, Lists.newArrayList(id));
    }

    @PostMapping("/reference/{projectId}")
    public UIElementReferenceResult referenceBatch(@PathVariable("projectId") String projectId, @RequestBody BaseQueryRequest request) {
        return uiElementService.referenceBatch(projectId, request);
    }

    @PostMapping("/reference/modules/{projectId}")
    public UIElementReferenceResult referenceElementModules(@PathVariable("projectId") String projectId, @RequestBody UiElementModulesRefRequest request) {
        return uiElementReferenceService.referenceElementModules(projectId, request);
    }

    @GetMapping("/export/template/{projectId}/{importType}")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ")
    public void elementTemplateExport(@PathVariable Integer importType, HttpServletResponse response) {
        uiElementService.templateExport(importType, response);
    }

    @PostMapping("/import")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ")
    @MsRequestLog(module = OperLogModule.UI_ELEMENT)
    public ExcelResponse<?> elementImport(@RequestPart("request") UiElementImportRequest request, @RequestPart("file") MultipartFile file) {
        return uiElementService.importFile(file, request);
    }

    @PostMapping("/export")
    @RequiresPermissions("PROJECT_UI_ELEMENT:READ")
    public void elementExport(@RequestBody BaseQueryRequest request, HttpServletResponse response) {
        uiElementService.elementExport(response, request);
    }
}
