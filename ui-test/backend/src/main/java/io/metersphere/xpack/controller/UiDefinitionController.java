package io.metersphere.xpack.controller;

import io.metersphere.service.api.ApiService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/definition")
public class UiDefinitionController {

    @Resource
    ApiService apiService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return apiService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return apiService.get(request.getRequestURI());
    }
}
