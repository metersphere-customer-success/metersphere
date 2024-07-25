package io.metersphere.intf;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ModuleNodeDTO;
import io.metersphere.service.UiScenarioModuleService;
import io.metersphere.utils.TemplateUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.InputStream;

public abstract class AbstractScenarioParser<T> implements ScenarioParser<T> {
    protected String projectId;

    protected String readContent(InputStream in) {
        return TemplateUtils.readContent(in);
    }

    public static String getSelectModulePath(String path, String pid) {
        UiScenarioModuleService uiModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        if (StringUtils.isNotBlank(pid)) {
            ModuleNodeDTO moduleDTO = uiModuleService.getNode(pid);
            if (moduleDTO != null) {
                return getSelectModulePath(moduleDTO.getName() + "/" + path, moduleDTO.getParentId());
            }
        }
        return "/" + path;
    }
}
