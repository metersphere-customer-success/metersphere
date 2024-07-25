package io.metersphere.service.task;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.UiScenarioExample;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.base.domain.UiTaskRefresh;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.service.UiAutomationService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 刷新元素库引用关系
 */
@Component
@Order(100)
public class RefreshElementReferenceTask extends AbstractTask {

    @Resource
    private UiAutomationService uiAutomationService;
    @Resource
    private UiScenarioMapper uiScenarioMapper;

    @Override
    public void doTask() {
        try {
            UiTaskRefresh taskRefresh = getTaskByKey();
            if (taskRefresh != null && COMPLETE.equals(taskRefresh.getTaskStatus())) {
                //持久化记录
                return;
            }

            int goPage = 1;
            int pageSize = 30;
            while (true) {
                UiScenarioExample example = new UiScenarioExample();
                UiScenarioExample.Criteria criteria = example.createCriteria();
                criteria.andStatusEqualTo("Underway");
                Page<Object> page = PageHelper.startPage(goPage, pageSize, false);
                List<UiScenarioWithBLOBs> list = PageUtils.setPageInfo(page, uiScenarioMapper.selectByExampleWithBLOBs(example)).getListObject();
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }

                for (UiScenarioWithBLOBs item : list) {
                    uiAutomationService.saveElementAndScenarioRelation(item);
                }

                goPage++;
            }
            //持久化记录
            saveRefreshStatus(taskRefresh, COMPLETE);

        } catch (Exception e) {
            LogUtil.error(String.format("刷新%s数据失败", getKey()));
        }
    }


    @Override
    public String getKey() {
        return "UI_REFRESH_ELEMENT_REF";
    }
}
