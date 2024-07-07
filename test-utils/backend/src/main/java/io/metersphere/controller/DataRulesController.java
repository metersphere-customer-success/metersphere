package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.BatchOperateRequest;
import io.metersphere.dto.DataRules;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.DataRulesService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RequestMapping("/rules")
@RestController
public class DataRulesController {

    @Resource
    DataRulesService dataRulesService;
    @Resource
    private BaseUserService baseUserService;


    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ)
    public Pager<List<DataRules>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody DataRules request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataRulesService.listDataRule(request));
    }


    @GetMapping("/get/{dataRuleId}")
    public DataRules getDataRules(@PathVariable String dataRuleId) {
        return dataRulesService.getDataRule(dataRuleId);
    }

    @PostMapping("/add")
    public DataRules addDataRule(@RequestBody DataRules dataRules) {
        dataRules.setId(UUID.randomUUID().toString());
        DataRules result = dataRulesService.addDataRule(dataRules);
        return result;
    }

    @PostMapping("/edit")
    public DataRules editDataRule(@RequestBody DataRules dataRules) {
        dataRules = dataRulesService.editDataRule(dataRules);
        return dataRules;
    }

    @PostMapping("/delete/{dataRuleId}")
    public int deleteDataRules(@PathVariable String dataRuleId) {
        return dataRulesService.deleteDataRule(dataRuleId);
    }

    @PostMapping("/delete/batch")
    public void deleteDataRulesBatch(@RequestBody BatchOperateRequest request) {
        dataRulesService.deleteDataRulesBatch(request);
    }

//
//    @PostMapping("/copy/{id}")
//    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, subject = "测试计划通知")
//    @CheckOwner(resourceId = "#id", resourceType = "test_plan")
//    public DataRules copy(@PathVariable String id) {
//        DataRules result = dataRulesService.copy(id);
//        result.setStage(StatusReference.statusMap.containsKey(result.getStage()) ? StatusReference.statusMap.get(result.getStage()) : result.getStage());
//        result.setStatus(StatusReference.statusMap.containsKey(result.getStatus()) ? StatusReference.statusMap.get(result.getStatus()) : result.getStatus());
//        return result;
//    }

}
