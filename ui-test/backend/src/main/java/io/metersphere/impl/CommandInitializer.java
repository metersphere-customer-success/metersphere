package io.metersphere.impl;

import io.metersphere.UiApplication;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.ArgTypeEnum;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 读取 指令 json 文件定义
 */
@Component
public class CommandInitializer {

    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(UiApplication.class.getClassLoader());


    @Bean
    public List<String> controlCommandList() {
        List<String> controlList = new ArrayList<>();
        controlList.add("do");
        controlList.add("if");
        controlList.add("elseIf");
        controlList.add("else");
        controlList.add("end");
        controlList.add("times");
        controlList.add("repeatIf");
        controlList.add("while");
        controlList.add("forEach");
        return controlList;
    }

    @Bean
    public List<String> loopCommandList() {
        List<String> controlList = new ArrayList<>();
        controlList.add("cmdTimes");
        controlList.add("cmdWhile");
        controlList.add("cmdForEach");
        return controlList;
    }

    /**
     * 指令 map
     *
     * @return
     */
    @Bean
    public Map<String, CommonCommand> uiCommandMap() {
        Map<String, String> contentMap = readFolderToMap("io/metersphere/impl/cmd/definition/*.json", "加载 ui 自动化指令定义脚本失败！", ".json");
        Map<String, CommonCommand> commandMap = new HashMap<>();
        for (Map.Entry<String, String> entry : contentMap.entrySet()) {
            commandMap.putIfAbsent(entry.getKey(), JSON.parseObject(entry.getValue(), CommonCommand.class));
        }
        return commandMap;
    }

    @Bean
    @DependsOn("uiCommandMap")
    public Set<String> assertionCommands(Map<String, CommonCommand> uiCommandMap) {
        return getCommandsByViewType(uiCommandMap, "validation");
    }

    @Bean
    @DependsOn("uiCommandMap")
    public Set<String> extractCommands(Map<String, CommonCommand> uiCommandMap) {
        return getCommandsByViewType(uiCommandMap, "dataExtraction");
    }

    public Set<String> getCommandsByViewType(Map<String, CommonCommand> uiCommandMap, String viewType) {
        Set<String> assertionCommands = new HashSet<>();
        Set<Map.Entry<String, CommonCommand>> entries = uiCommandMap.entrySet();
        entries.forEach(item -> {
            if (item.getValue().getViewType().equals(viewType)) {
                assertionCommands.add(item.getKey());
            }
        });
        return assertionCommands;
    }

    /**
     * template
     * Map<String,String>
     * 名称，内容
     *
     * @return
     */
    @Bean
    @DependsOn("uiCommandMap")
    public Map<String, String> uiTemplateMap() {
        return readFolderToMap("io/metersphere/impl/template/*.tpl", "加载 ui 自动化模板脚本失败！", ".tpl");
    }

    @Bean
    public Map<String, String> uiParamMap() {
        Map<String, String> result = new HashMap<>();
        for (ArgTypeEnum value : ArgTypeEnum.values()) {
            result.put(value.getType(), value.getMsType());
        }
        return result;
    }

    /**
     * template
     * Map<String,String>
     * 名称，内容
     *
     * @return
     */
    @Bean
    public Map<String, String> webdriverScriptMap() {
        return readFolderToMap("io/metersphere/impl/cmd/script/selenium/*.js", "加载 webdriverScriptMap 失败！", "");
    }

    /**
     * 公共函数
     * <p>
     * Map<String,String>
     * 名称，内容
     *
     * @return
     */
    @Bean
    public Map<String, String> uiCommonFunctionMap() {
        return readFolderToMap("io/metersphere/impl/function/*", "加载 ui 自动化模板公共函数失败！", ".js", ".tpl");
    }

    private Map<String, String> readFolderToMap(String path, String message, String... replaceStr) {
        Map<String, String> templateMap = new HashMap<>();
        try {
            LogUtil.info("读取资源文件： " + path);
            Resource[] resources = resolver.getResources(path);
            for (Resource resource : resources) {
                String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                String name = resource.getFilename();
                for (String suffix : replaceStr) {
                    name = name.replace(suffix, "");
                }
                templateMap.put(name, content);
            }
        } catch (IOException e) {
            LogUtil.error(message, e);
        }
        LogUtil.info("读取资源文件:" + path + " 容量：" + templateMap.size());
        for (String key : templateMap.keySet()) {
            LogUtil.debug("key: " + key);
        }

        return templateMap;
    }
}
