package io.metersphere.plan.controller;

import io.metersphere.dto.track.TestPlanUiScenarioDTO;
import io.metersphere.plan.service.TestPlanUiScenarioCaseService;
import io.metersphere.service.ShareInfoService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/share/test/plan/uiScenario/case")
public class ShareTestPlanUiScenarioCaseController {

    @Resource
    ShareInfoService shareInfoService;
    @Resource
    TestPlanUiScenarioCaseService testPlanUiScenarioCaseService;

    @PostMapping("/list/all/{shareId}/{planId}")
    public List<TestPlanUiScenarioDTO> getUiScenarioAllList(@PathVariable String shareId, @PathVariable String planId,
                                                            @RequestBody(required = false) List<String> statusList) {
        shareInfoService.validate(shareId, planId);
        return testPlanUiScenarioCaseService.getAllCasesByStatusList(planId, statusList);
    }
}
