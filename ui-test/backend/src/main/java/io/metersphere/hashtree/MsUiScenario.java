package io.metersphere.hashtree;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.jmeter.plugins.webdriver.config.RemoteBrowser;
import com.googlecode.jmeter.plugins.webdriver.config.RemoteDriverConfig;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.EnvironmentConfig;
import io.metersphere.dto.ScenarioVariable;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.impl.CommandConfig;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UiEnvironmentService;
import io.metersphere.utils.ScopeVariableHandler;
import io.metersphere.utils.processor.CompatibilityOldData;
import io.metersphere.vo.element.operation.UiWaitElementVo;
import io.metersphere.vo.program.controller.UiIfVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@json_id"
)
public class MsUiScenario extends MsTestElement {

    private String type = "UiScenario";
    private String clazzName = MsUiScenario.class.getCanonicalName();

    @JsonProperty
    private String environmentId;

    @JsonProperty
    private List<ScenarioVariable> variables;

    @JsonProperty
    private Map<String, String> environmentMap;

    @JsonProperty
    private Boolean onSampleError;

    @JsonProperty
    private String baseURL;

    /**
     * chrome firefox all
     */
    @JsonProperty
    private RemoteBrowser browser;

    @JsonProperty
    private boolean headlessEnabled = true;

    @JsonProperty
    private Boolean variableEnable = null;

    /**
     * 优先使用当前场景变量，没有则使用原场景变量， variableEnable为false时候  smartVariableEnable为true 生效
     */
    @JsonProperty
    private Boolean smartVariableEnable = false;

    /**
     * 是否使用原场景环境执行
     */
    @JsonProperty
    private Boolean environmentEnable = false;

    @JsonProperty
    private ScenarioConfig scenarioConfig = new ScenarioConfig();

    @JsonProperty
    private String environmentJson;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        //设置一个 selenium-ide 使用的 url
        config.setHeader("baseURL", baseURL);

        if (config.getCommandConfig() == null) {
            // 设置全局 commandConfig 参数, 如果有，则沿用主场景的配置
            CommandConfig globalCommandConfig = new CommandConfig();
            globalCommandConfig.setHeadlessEnabled(headlessEnabled);
            globalCommandConfig.setReportId(config.getReportId());
            config.setCommandConfig(globalCommandConfig);
        }

        tree.add(getRemoteDriverConfig(config));

        setEnvironmentConfig(config);

        List<MsUiCommand> commands = getMsCommands(hashTree, config, null);

        if (CollectionUtils.isNotEmpty(commands)) {
            tree.add(MsWebDriverSampler.parseCommandsToSampler(commands, config));
        }
    }

    /**
     * ParameterConfig 里面的 config 最优先（表示从外部批量执行或定时任务或执行队列的配置进来)其次时本身场景的 environmentJson 或者 envMap
     */
    private void setEnvironmentConfig(ParameterConfig config) {

        if (StringUtils.isNotBlank(config.getEnvironmentJson()) && MapUtils.isNotEmpty(config.getConfig())) {
            Map<String, EnvironmentConfig> jsonMap = fetchEnvCnfWithCache(config.getEnvironmentJson(), config.getProjectId());
            Map<String, EnvironmentConfig> merge = new HashMap<>();
            jsonMap.keySet().forEach(k -> {
                merge.put(k, null);
            });
            config.getConfig().keySet().forEach(k -> {
                merge.put(k, null);
            });
            merge.keySet().forEach(k -> {
                if (jsonMap.containsKey(k) && !config.getConfig().containsKey(k)) {
                    merge.put(k, jsonMap.get(k));
                } else {
                    merge.put(k, config.getConfig().get(k));
                }
            });
            config.setConfig(merge);
        }

        if (MapUtils.isEmpty(config.getConfig()) && StringUtils.isNotBlank(config.getEnvironmentJson())) {
            config.setConfig(fetchEnvCnfWithCache(config.getEnvironmentJson(), config.getProjectId()));
        }

        //最后本身场景的环境配置
        if (StringUtils.isNotBlank(environmentJson) && config.getConfig() == null) {
            if (StringUtils.equalsIgnoreCase(config.getRequestOriginator(), SystemConstants.UIRequestOriginatorEnum.TEST_PLAN.getName())) {
                //测试计划中执行过来的，没有配置运行环境的不设置环境信息
                return;
            }
            Map<String, EnvironmentConfig> jsonMap = fetchEnvCnfWithCache(environmentJson, getProjectId());
            Map<String, EnvironmentConfig> merge = new HashMap<>();
            Map<String, EnvironmentConfig> configMap = Optional.ofNullable(config.getConfig()).orElse(new HashMap<>());
            jsonMap.keySet().forEach(k -> {
                merge.put(k, null);
            });
            configMap.keySet().forEach(k -> {
                merge.put(k, null);
            });
            merge.keySet().forEach(k -> {
                if (jsonMap.containsKey(k)) {
                    if (!configMap.containsKey(k)) {
                        merge.put(k, jsonMap.get(k));
                    } else {
                        merge.put(k, configMap.get(k));
                    }
                } else {
                    if (configMap.get(k) != null) {
                        merge.put(k, configMap.get(k));
                    }
                }
            });
            config.setConfig(merge);
        }

        if (config.getConfig() != null && MapUtils.isNotEmpty(config.getConfig())) {
            config.getConfig().keySet().forEach(k -> {
                if (config.getConfig().get(k) == null) {
                    return;
                }
                config.getConfig().put(k, CommonBeanFactory.getBean(UiEnvironmentService.class).replaceDomainVars(config.getConfig().get(k)));
            });
        }
    }

    /**
     * 将场景中的指令展开，如果嵌套了场景就将嵌套场景的指令递归展开
     * 同时设置所有场景的场景变量
     *
     * @param hashTree
     * @param config
     * @return
     */
    public List<MsUiCommand> getMsCommands(List<MsTestElement> hashTree, ParameterConfig config, Boolean variableEnable) {
        // 设置场景变量
        variableEnable = variableEnable == null ? true : variableEnable;
        // 获取环境信息
        if (StringUtils.isNotBlank(environmentJson) && (config.getConfig() == null || MapUtils.isEmpty(config.getConfig())) && environmentEnable) {
            config.setConfig(fetchEnvCnfWithCache(environmentJson, getProjectId()));
        }

        if (config.getConfig() != null && MapUtils.isNotEmpty(config.getConfig())) {
            config.getConfig().keySet().forEach(k -> {
                if (config.getConfig().get(k) == null) {
                    return;
                }
                config.getConfig().put(k, CommonBeanFactory.getBean(UiEnvironmentService.class).replaceDomainVars(config.getConfig().get(k)));
            });
        }

        assembleScopeVariables(this, config, variableEnable);
        CommandConfig commandConfig = (CommandConfig) config.getCommandConfig();
        JSONArray varArr = null;
        if (commandConfig.getScopeScenarioVariables() != null) {
            varArr = commandConfig.getScopeScenarioVariables().findInheritJsonArrayById(this.getId());
        }
        List<MsUiCommand> commands = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(hashTree)) {
            JSONArray finalVarArr = varArr;
            hashTree.forEach(el -> {
                if (el instanceof MsUiCommand) {
                    MsUiCommand sub = (MsUiCommand) el;

                    if (sub.getCommandConfig() == null) {
                        CommandConfig subConfig = getCommandConfigOrDefault(sub.getCommandConfig());
                        sub.setCommandConfig(subConfig);
                    }
                    if (sub.getVo() instanceof UiWaitElementVo) {
                        UiWaitElementVo vo = (UiWaitElementVo) sub.getVo();
                        sub.getCommandConfig().setSecondsWaitElement(Math.toIntExact(vo.getWaitTime()));
                        sub.getCommandConfig().setSecondsWaitWindowOnLoad(Math.toIntExact(vo.getWaitTime())/1000);
                    }

                    //设置每一个指令的局部变量
                    setScopeVariable(finalVarArr, commandConfig, sub);

                    commands.add(sub);
                } else if (el instanceof MsUiScenario) {
                    MsUiScenario child = (MsUiScenario) el;
                    child.setParent(this);
                    commands.addAll(child.getMsCommands(child.getHashTree(), config, child.getVariableEnable()));
                }
            });
        }
        return commands;
    }

    private Map<String, EnvironmentConfig> fetchEnvCnfWithCache(String envJson, String project) {
        if (StringUtils.isBlank(project)) {
            return Maps.newConcurrentMap();
        }
        String tempEnvironmentJson = StringUtils.isNotEmpty(envJson) ? envJson : environmentJson;
        Map<String, String> envMap = JSON.parseMap(tempEnvironmentJson);

        String key = Optional.ofNullable(envMap.get(project)).orElse("");
        if (StringUtils.isBlank(key)) {
            return Maps.newConcurrentMap();
        }
        Map<String, EnvironmentConfig> envConfig = Maps.newConcurrentMap();
        //最外层的环境配置可能是多个项目多个环境
        if (envMap.keySet().size() > 1) {
            envMap.keySet().forEach(k -> {
                envConfig.put(k, CommonBeanFactory.getBean(UiEnvironmentService.class).convertCnfToClazz(envMap.get(k)));
            });
        } else if (envMap.keySet().size() == 1) {
            EnvironmentConfig cnf = CommonBeanFactory.getBean(UiEnvironmentService.class).convertCnfToClazz(key);
            envConfig.put(project, cnf);
        }
        return envConfig;
    }

    /**
     * 根据指令的类型递归设置指令的场景变量
     *
     * @param finalVarArr
     * @param sub
     */
    private void setScopeVariable(JSONArray finalVarArr, CommandConfig commandConfig, MsUiCommand sub) {
        CommandConfig subConfig = getCommandConfigOrDefault(sub.getCommandConfig());
        subConfig.setEnvId(commandConfig.getEnvId());
        subConfig.setEnvVariables(commandConfig.getEnvVariables());
        subConfig.setConditions(commandConfig.getConditions());

        if (finalVarArr != null || commandConfig.getEnvVariables() != null) {
            List<ScenarioVariable> variables = finalVarArr != null ? JSON.parseObject(finalVarArr.toString(), new TypeReference<List<ScenarioVariable>>() {
            }) : new ArrayList<>();
            // 给 condition 补充value 的 type
            setConditionValueType(sub, variables);
            subConfig.setScenarioVariables(variables);
            subConfig.setScreenshotConfig(sub.getCommandConfig().getScreenshotConfig());

            sub.setCommandConfig(subConfig);
            if (StringUtils.isBlank(sub.getCommandType())) {
                sub.setCommandType(CommandType.COMMAND_TYPE_ATOM);
            }
            switch (sub.getCommandType()) {
                // 复合指令，先将其vo转成原子指令，再解析HashTree，最后加上end结束
                case CommandType.COMMAND_TYPE_COMBINATION_PROXY:
                case CommandType.COMMAND_TYPE_COMBINATION:
                    // 添加start 指令
                    if (CollectionUtils.isNotEmpty(sub.getHashTree())) {
                        sub.getHashTree().forEach(item -> {
                            if (!sub.isEnable()) {
                                return;
                            }
                            if (item instanceof MsUiCommand) {
                                // 添加嵌套指令
                                setChildConfig(commandConfig, sub, variables, (MsUiCommand) item);
                            } else if (item instanceof MsUiScenario) {
                                delUiScenario(commandConfig, sub, variables, item);
                            }
                        });
                    }
                    break;
                // 代理指令将vo转成原子指令
                case CommandType.COMMAND_TYPE_ATOM:
                case CommandType.COMMAND_TYPE_PROXY:
                default:
                    sub.getCommandConfig().setScenarioVariables(variables);
                    break;
            }

            //前后置操作中也要设置场景变量
            if (CollectionUtils.isNotEmpty(sub.getPostCommands())) {
                sub.getPostCommands().forEach(v -> {
                    setScopeVariable(finalVarArr, commandConfig, v);
                });
            }

            if (CollectionUtils.isNotEmpty(sub.getPreCommands())) {
                sub.getPreCommands().forEach(v -> {
                    setScopeVariable(finalVarArr, commandConfig, v);
                });
            }
        }
    }

    private void delUiScenario(CommandConfig commandConfig, MsUiCommand sub, List<ScenarioVariable> variables, MsTestElement item) {
        //处理while if 等语句嵌套场景的问题
        if (CollectionUtils.isNotEmpty(item.getHashTree())) {
            for (MsTestElement msTestElement : item.getHashTree()) {
                //嵌套自定义指令的情况
                if (msTestElement instanceof MsUiCustomCommand) {
                    if (CollectionUtils.isNotEmpty(msTestElement.getHashTree())) {
                        msTestElement.getHashTree().forEach(t -> {
                            setChildConfig(commandConfig, sub, variables, (MsUiCommand) t);
                        });
                    }
                } else if (msTestElement instanceof MsUiScenario) {
                    delUiScenario(commandConfig, sub, variables,  msTestElement);
                }
                else {
                    //普通的场景里面嵌套
                    setChildConfig(commandConfig, sub, variables, (MsUiCommand) msTestElement);
                }
            }
        }
    }

    private static void setConditionValueType(MsUiCommand sub, List<ScenarioVariable> variables) {
        if (sub.getVo() instanceof UiIfVo) {
            UiIfVo vo = (UiIfVo) sub.getVo();
            if (CollectionUtils.isNotEmpty(vo.getConditions())) {
                for (UiIfVo.Condition condition : vo.getConditions()) {
                    if (StringUtils.isNotBlank(condition.getValue())) {
                        String name;
                        if (condition.getName().startsWith("${") && condition.getName().endsWith("}")) {
                            String subStr = condition.getName().replace("${","");
                            name = subStr.replace("}","");
                        } else {
                            name = condition.getValue();
                        }
                        for (ScenarioVariable variable : variables) {
                            if ( variable.getName().equals(name)) {
                                condition.setValueType(variable.getType());
                                break;
                            }
                        }

                    }
                }
            }
        }
    }

    private void setChildConfig(CommandConfig commandConfig, MsUiCommand sub, List<ScenarioVariable> variables, MsUiCommand item) {
        CommandConfig thisCf = item.getCommandConfig();
        thisCf.setScenarioVariables(variables);
        if (thisCf.getScreenshotConfig() == null) {
            thisCf.setScreenshotConfig(sub.getCommandConfig().getScreenshotConfig());
        }
        thisCf.setEnvId(commandConfig.getEnvId());
        thisCf.setEnvVariables(commandConfig.getEnvVariables());
        thisCf.setConditions(commandConfig.getConditions());
    }

    public CommandConfig getCommandConfigOrDefault(CommandConfig config) {
        if (config != null) {
            return config;
        }
        CommandConfig subConfig = new CommandConfig();
        subConfig.setIgnoreFail(true);
        return subConfig;
    }

    private void assembleScopeVariables(MsUiScenario msUiScenario, ParameterConfig config, Boolean variableEnable) {
        // 环境变量处理
        List<ScopeVariableHandler.InheritScopeVariable.ScopedVar> envVarArr = Lists.newArrayList();
        List<ScenarioVariable> envVariables = Lists.newArrayList();
        String key = null;
        if (MapUtils.isNotEmpty(config.getConfig())) {
            // 转换环境中的变量
            Map<String, EnvironmentConfig> cnf = config.getConfig();
            //根据projectId 找到对应环境变量
            key = Optional.ofNullable(msUiScenario.getProjectId()).orElse(cnf.keySet().stream().findFirst().get());
            EnvironmentConfig environmentConfig = cnf.get(key);
            List<ScenarioVariable> variables = environmentConfig != null ? environmentConfig.getCommonConfig().getVariables(): new ArrayList<>();
            if (CollectionUtils.isNotEmpty(variables)) {
                envVarArr = variables.stream()
                        .filter(item -> item.isEnable() && StringUtils.isNotBlank(item.getScope()) && StringUtils.isNotBlank(item.getName()) && StringUtils.equalsIgnoreCase("ui", item.getScope()))
                        .map(var -> {
                            ScopeVariableHandler.InheritScopeVariable.ScopedVar scopedVar = new ScopeVariableHandler.InheritScopeVariable.ScopedVar();
                            if (StringUtils.equals(var.getType(),"CONSTANT")) {
                                String type = CompatibilityOldData.convertConstantType(var.getValue());
                                var.setType(type);
                            }
                            scopedVar.setName(var.getName());
                            scopedVar.setType(var.getType());
                            scopedVar.setValue(typeConvert(var.getType(), var.getValue()));
                            scopedVar.setEnable(var.isEnable());
                            return scopedVar;
                        }).collect(Collectors.toList());
                envVariables = variables.stream()
                        .filter(item -> item.isEnable() && StringUtils.isNotBlank(item.getScope()) && StringUtils.isNotBlank(item.getName()) && StringUtils.equalsIgnoreCase("ui", item.getScope()))
                        .collect(Collectors.toList());
                //类型转换
                envVariables.forEach(e -> {
                    e.setValue(typeConvert(e.getType(), e.getValue()));
                });
            }
        }

        CommandConfig commandConfig = ((CommandConfig) config.getCommandConfig());
        //环境url匹配规则
        if (MapUtils.isNotEmpty(config.getConfig())) {
            Map<String, EnvironmentConfig> cnf = config.getConfig();
            key = Optional.ofNullable(msUiScenario.getProjectId()).orElse(cnf.keySet().stream().findFirst().get());
            EnvironmentConfig environmentConfig = cnf.get(key);
            if (environmentConfig != null && environmentConfig.getHttpConfig() != null && CollectionUtils.isNotEmpty(environmentConfig.getHttpConfig().getConditions()))
                commandConfig.setConditions(environmentConfig.getHttpConfig().getConditions());
        }else {
            commandConfig.setConditions(null);
        }

        //作用域相关变量信息处理
        List<ScenarioVariable> currentScopeVars = msUiScenario.getVariables();
        if (currentScopeVars == null) {
            currentScopeVars = new ArrayList<>();
        }
        List<ScopeVariableHandler.InheritScopeVariable.ScopedVar> varArray = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(currentScopeVars)) {
            // 设置场景变量
            for (ScenarioVariable scenarioVariable : variables) {
                if (StringUtils.equals(scenarioVariable.getType(),"CONSTANT")) {
                    String type = CompatibilityOldData.convertConstantType(scenarioVariable.getValue());
                    scenarioVariable.setType(type);
                }
                ScopeVariableHandler.InheritScopeVariable.ScopedVar varObj = new ScopeVariableHandler.InheritScopeVariable.ScopedVar();
                varObj.setType(scenarioVariable.getType());
                varObj.setName(scenarioVariable.getName());
                getParentValue(msUiScenario, commandConfig, scenarioVariable);
                varObj.setValue(typeConvert(scenarioVariable.getType(), scenarioVariable.getValue()));
                varObj.setEnable(scenarioVariable.isEnable());
                varArray.add(varObj);
            }
        }
        if (commandConfig.getScopeScenarioVariables() == null) {
            ScopeVariableHandler scopeVariableHandler = new ScopeVariableHandler();
            commandConfig.setScopeScenarioVariables(scopeVariableHandler);
        }

        // 设置环境变量
        commandConfig.setEnvVariables(envVariables);
        commandConfig.setEnvId(key);

        commandConfig.getScopeScenarioVariables().push(msUiScenario.getId(),
                msUiScenario.getParent() != null ? msUiScenario.getParent().getId() : null,
                varArray, variableEnable, smartVariableEnable, environmentEnable, envVarArr);

    }

    private static void getParentValue(MsUiScenario msUiScenario, CommandConfig commandConfig, ScenarioVariable scenarioVariable) {
        if (scenarioVariable ==null || scenarioVariable.getValue() == null) {
            return;
        }
        if (scenarioVariable.getValue().toString().startsWith("${") && scenarioVariable.getValue().toString().endsWith("}")) {
            String subStr = scenarioVariable.getValue().toString().replace("${","");
            String name = subStr.replace("}","");
            if (commandConfig.getScopeScenarioVariables() != null) {
                ScopeVariableHandler.InheritScopeVariable inheritScopeVariable = commandConfig.getScopeScenarioVariables().getIndexMap().get(msUiScenario.getParent().getId());
                if (inheritScopeVariable == null) {
                   return;
                }
                List<ScopeVariableHandler.InheritScopeVariable.ScopedVar> varArr = inheritScopeVariable.getVarArr();
                if (CollectionUtils.isNotEmpty(varArr)) {
                    for (ScopeVariableHandler.InheritScopeVariable.ScopedVar scopedVar : varArr) {
                        if (StringUtils.equals(name,scopedVar.getName())) {
                            scenarioVariable.setValue(scopedVar.getValue());
                            break;
                        }
                    }
                }
            }
        }
    }

    private Object typeConvert(String type, Object value) {
        String valueStr = String.valueOf(value);
        Object val =  valueStr;
        try {
            switch (type) {
                case "JSON":
                case "JSONObject":
                    if (StringUtils.isBlank(valueStr)) {
                        value = "{}";
                    }
                    if (value instanceof List) {
                        val = value;
                        break;
                    }
                    if (value instanceof Map) {
                        jsonMockParse((Map) value);
                        val = value;
                        break;
                    }
                    val = JSON.parseObject(valueStr);
                    break;
                case "ARRAY":
                case "JSONArray":
                    if (StringUtils.isBlank(valueStr)) {
                        value = "[]";
                    }
                    if (value instanceof List) {
                        val = value;
                        break;
                    }
                    val = JSON.parseArray(valueStr);
                    break;
                case "NUMBER":
                    if (StringUtils.isBlank(valueStr)) {
                        val = 0;
                    }
                    break;
                case "CONSTANT":
                case "STRING":
                    if (val != null && StringUtils.isNotBlank(String.valueOf(val))) {
                        String temp = String.valueOf(val);
                        if (isFilePath(temp)) {
                            val = temp.replaceAll("\\\\", "/");
                        }
                    }
                    break;
                default:
                    val = value;
            }
        } catch (Exception e) {
            LogUtil.error(valueStr+"转换UI变量时报错！", e);
            MSException.throwException("转换UI变量时报错，请检查变量的格式！" + e.getMessage());
        }
        return val;
    }

    private void jsonMockParse(Map map) {
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                return;
            } else if (value instanceof Map) {
                jsonMockParse((Map) value);
            } else if (value instanceof String) {
                if (StringUtils.isNotBlank((String) value)) {
                    value = ScriptEngineUtils.buildFunctionCallString((String) value);
                }
                map.put(key, value);
            }
        }
    }

    private boolean isFilePath(String path) {
        try {
            if (StringUtils.isBlank(path)) {
                return false;
            }
            String regx = "^[a-zA-Z]:\\\\(((?![<>:\"/\\\\|?*]).)+((?<![ .])\\\\)?)*$";
            Pattern pattern = Pattern.compile(regx);
            if (pattern.matcher(path).matches()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private RemoteDriverConfig getRemoteDriverConfig(ParameterConfig config) {
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        String seleniumAddress = systemParameterService.getValue("base.selenium.docker.url");
        if (config.isRunLocal()) {
            seleniumAddress = SessionUtils.getUser().getSeleniumServer();
            if (StringUtils.isBlank(seleniumAddress)) {
                MSException.throwException("请在个人信息中配置 selenium-server 地址");
            }
        } else {
            if (StringUtils.isBlank(seleniumAddress)) {
                MSException.throwException("请在系统参数配置 Grid 地址");
            }
        }

        String browser = Optional.ofNullable(this.browser).orElse(RemoteBrowser.CHROME).name();
        boolean headlessEnabled = this.headlessEnabled;

        if (config.getCommandConfig() != null && StringUtils.isNotBlank(((CommandConfig) config.getCommandConfig()).getBrowser())) {
            browser = Optional.ofNullable(((CommandConfig) config.getCommandConfig()).getBrowser()).orElse("CHROME");
            headlessEnabled = ((CommandConfig) config.getCommandConfig()).isHeadlessEnabled();
        }

        RemoteDriverConfig remoteDriverConfig = MsRemoteDriverConfig.getRemoteDriverConfig(seleniumAddress, RemoteBrowser.valueOf(browser), config.getBrowserLanguage());
        remoteDriverConfig.setHeadless(headlessEnabled);


        return remoteDriverConfig;
    }

    public RemoteBrowser getBrowserByName(String name) {
        if (StringUtils.isBlank(name)) {
            return RemoteBrowser.CHROME;
        } else if (RemoteBrowser.FIREFOX.name().equals(name)) {
            return RemoteBrowser.FIREFOX;
        } else if (RemoteBrowser.INTERNET_EXPLORER.name().equals(name)) {
            return RemoteBrowser.INTERNET_EXPLORER;
        }
        return RemoteBrowser.CHROME;
    }

    public static void main(String[] args) throws ScriptException {
        ScriptEngine graalEngine = new ScriptEngineManager().getEngineByName("graal.js");
        graalEngine.eval("print('Hello World!');");
    }
}
