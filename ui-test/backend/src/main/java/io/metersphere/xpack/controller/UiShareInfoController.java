package io.metersphere.xpack.controller;

import io.metersphere.dto.automation.ScenarioReportResultWrapper;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.UiScenarioReportService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * UI 报告分享转发服务
 */
@RestController
@RequestMapping(value = "/share")
public class UiShareInfoController {
    @Resource
    private ShareInfoService shareInfoService;
    @Resource
    private UiScenarioReportService uiScenarioReportService;

    @GetMapping(value = "/scenario/report")
    public String shareApiRedirect() {
        return "share-ui-report.html";
    }

    @GetMapping("/ui/scenario/report/get/{shareId}/{reportId}")
    public ScenarioReportResultWrapper get(@PathVariable String shareId, @PathVariable String reportId) {
        shareInfoService.validateExpired(shareId);
        return uiScenarioReportService.get(reportId, true);
    }
}

@Controller
@RequestMapping
class UiShareIndexController {
    @GetMapping(value = "/shareUiReport")
    public String shareUiRedirect() {
        return "share-ui-report.html";
    }
}