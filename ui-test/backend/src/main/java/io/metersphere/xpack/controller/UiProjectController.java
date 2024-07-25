package io.metersphere.xpack.controller;

import io.metersphere.dto.ProjectDTO;
import io.metersphere.service.BaseCheckPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RequestMapping("project")
@RestController
public class UiProjectController {

    @Autowired
    BaseCheckPermissionService baseCheckPermissionService;

    @GetMapping("/getOwnerProjects")
    public List<ProjectDTO> getOwnerProjects() {
        return baseCheckPermissionService.getOwnerProjects();
    }

    @GetMapping("/get/owner/ids")
    public Collection<String> getOwnerProjectIds() {
        return baseCheckPermissionService.getUserRelatedProjectIds();
    }

}
