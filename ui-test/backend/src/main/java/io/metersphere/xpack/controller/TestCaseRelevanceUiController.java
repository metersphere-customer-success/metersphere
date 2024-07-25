package io.metersphere.xpack.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.UiScenario;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.automation.ApiScenarioDTO;
import io.metersphere.dto.automation.ApiScenarioRequest;
import io.metersphere.service.UiAutomationService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/test/case")
public class TestCaseRelevanceUiController {

    @Resource
    UiAutomationService uiAutomationService;

    @PostMapping("/relevance/uiScenario/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> getTestCaseScenarioCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, uiAutomationService.getRelevanceScenarioList(request));
    }

    @PostMapping("/getUiCaseByIds")
    public List<UiScenario> getUiCaseByIds(@RequestBody List<String> ids) {
        return uiAutomationService.getUiCaseByIds(ids);
    }
}
