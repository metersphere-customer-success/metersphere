package io.metersphere.impl;

import io.metersphere.constants.TemplateConstants;
import io.metersphere.utils.TemplateUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指令的包装类，在 jmeter 执行前进行通用的参数替换以及指令注入
 */
@Component
public class CommandWrapper {
    @Qualifier("uiTemplateMap")
    @Resource
    private Map<String, String> uiTemplateMap;
    @Qualifier("controlCommandList")
    @Resource
    private List<String> controlCommandList;
    private static Map<String, String> staticUiTemplateMap;
    private static List<String> controlCmdList;

    @PostConstruct
    private void init() {
        staticUiTemplateMap = this.uiTemplateMap;
        controlCmdList = this.controlCommandList;
    }

    // 每一个指令的 wrap 包装，包含了 try catch
    public static String wrap(String script, String cmdName, JSONObject param) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("cmdId", param.optString("id"));
        paramMap.put("cmdName", cmdName);
        paramMap.put("webdriverScript", script);
        paramMap.put("singleCommandConfig", param.optString("singleCommandConfig"));
        paramMap.put("targetType", param.optString("targetType"));
        if(param.has("objTarget") && StringUtils.isNotBlank(param.optString("objTarget"))){
            paramMap.put("objTarget", param.optString("objTarget"));
        }
        paramMap.put("customName", param.optString("customName"));
        // 针对逻辑控制或者循环控制指令，不需要 try catch 包裹
        if(controlCmdList.contains(cmdName)){
            return TemplateUtils.replaceVars(staticUiTemplateMap.get(TemplateConstants.TEMPLATE_EMPTYWRAP), paramMap);
        }
        String str = TemplateUtils.replaceVars(staticUiTemplateMap.get(TemplateConstants.TEMPLATE_CMDWRAP), paramMap);
        return str;
    }
}
