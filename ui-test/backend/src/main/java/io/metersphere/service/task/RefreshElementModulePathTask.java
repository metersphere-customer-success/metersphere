package io.metersphere.service.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.UiTaskRefresh;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 刷 元素库 模块路径
 */
@Component
@Order(101)
public class RefreshElementModulePathTask extends AbstractTask {

    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;

    @Override
    public void doTask() {
        try {
            UiTaskRefresh taskRefresh = getTaskByKey();
            if (taskRefresh != null && REFRESH.equals(taskRefresh.getTaskStatus())) {
                //持久化记录
                return;
            }
            //获取元素库模块列表
            List<ModuleNode> moduleNodes = extModuleNodeMapper.getAllNodeTree("ui_element_module");
            if (CollectionUtils.isEmpty(moduleNodes)) {
                saveRefreshStatus(taskRefresh, REFRESH);
                return;
            }

            Map<String, String> paramMap = Maps.newConcurrentMap();
            //重新处理 module_path
            Map<Integer, List<ModuleNode>> map = moduleNodes.parallelStream().collect(Collectors.groupingBy(ModuleNode::getLevel));
            List<Integer> keys = map.keySet().stream().sorted(Comparator.comparing(v -> v)).collect(Collectors.toList());
            List<ModuleNode> result = Lists.newArrayList();
            for (Integer key : keys) {
                List<ModuleNode> list = map.get(key);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                for (ModuleNode node : list) {
                    if (key <= 1) {
                        node.setModulePath("/" + node.getName());
                    } else {
                        String parentId = node.getParentId();
                        if (StringUtils.isNotBlank(parentId) && StringUtils.isNotBlank(paramMap.get(parentId))) {
                            node.setModulePath(paramMap.get(parentId) + "/" + node.getName());
                        }
                    }
                    paramMap.put(node.getId(), node.getModulePath());
                    result.add(node);
                }
            }

            for (ModuleNode node : result) {
                extModuleNodeMapper.updateByPrimaryKeySelective("ui_element_module", node);
            }
            //持久化记录
            saveRefreshStatus(taskRefresh, REFRESH);
        } catch (Exception e) {
            LogUtil.error(String.format("刷新%s数据失败", getKey()));
        }
    }

    @Override
    public String getKey() {
        return "UI_REFRESH_ELEMENT_MODULE_PATH";
    }
}
