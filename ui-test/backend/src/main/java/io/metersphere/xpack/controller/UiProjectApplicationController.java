package io.metersphere.xpack.controller;

import io.metersphere.service.project.ProjectService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/project_application")
public class UiProjectApplicationController {

    @Resource
    ProjectService projectService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return projectService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return projectService.get(request.getRequestURI());
    }
}
