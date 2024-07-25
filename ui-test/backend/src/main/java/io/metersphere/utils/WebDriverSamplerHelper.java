package io.metersphere.utils;

import com.google.common.collect.Lists;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.CommandConstants;
import io.metersphere.constants.TemplateConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.ScenarioVariable;
import io.metersphere.dto.SideDTO;
import io.metersphere.impl.CommandConfig;
import io.metersphere.impl.CommonCommand;
import io.metersphere.intf.AbstractCommand;
import io.metersphere.jmeter.JMeterService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class WebDriverSamplerHelper {
    @Resource(name = "uiCommandMap")
    private Map<String, CommonCommand> uiCommandMap;
    @Resource(name = "uiTemplateMap")
    private Map<String, String> uiTemplateMap;
    @Resource(name = "webdriverScriptMap")
    private Map<String, String> webdriverScriptMap;
    @Resource(name = "uiCommonFunctionMap")
    private Map<String, String> uiCommonFunctionMap;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private List<String> controlCommandList;
    @Resource
    private Set<String> assertionCommands;
    @Resource
    private Set<String> extractCommands;
    @Resource
    private List<String> loopCommandList;
    @Value("${ui.resolution-ratio-x:1920}")
    private Integer resolutionRatioX;
    @Value("${ui.resolution-ratio-y:1080}")
    private Integer resolutionRatioY;

    private static Integer staticResolutionRatioX;
    private static Integer staticResolutionRatioY;
    private static Set<String> staticAssertionCommands;
    private static Set<String> staticExtractCommands;
    private static Map<String, CommonCommand> staticCommandMap;
    private static Map<String, String> staticTemplateMap;
    private static Map<String, String> staticWebdriverScriptMap;
    private static Map<String, String> staticUiCommonFunctionMap;
    private static List<String> controlCmdList;
    public static final List<String> needEscapeCommandList = new ArrayList<>() {{
        add("runScript");
        add("executeScript");
        add("executeAsyncScript");
    }};
    private static JMeterService staticJmeterService;
    private static final ThreadLocal<Integer> loopCount = new ThreadLocal<>();
    public static List<String> staticLoopCommandList;

    /**
     * @PostConstruct 修饰的 initStaticField 会在服务器加载 servlet 的时候运行，staticTemplateMap 获得初始化数据
     */
    @PostConstruct
    private void initStaticField() {
        staticCommandMap = this.uiCommandMap;
        staticTemplateMap = this.uiTemplateMap;
        staticWebdriverScriptMap = this.webdriverScriptMap;
        controlCmdList = this.controlCommandList;
        staticJmeterService = this.jMeterService;
        staticUiCommonFunctionMap = this.uiCommonFunctionMap;
        staticAssertionCommands = this.assertionCommands;
        staticExtractCommands = this.extractCommands;
        staticLoopCommandList = this.loopCommandList;
        staticResolutionRatioX = this.resolutionRatioX;
        staticResolutionRatioY = this.resolutionRatioY;
    }

    public static final String ASSERTION_NAME = "断言";
    public static final String EXTRACT_NAME = "数据提取";
    public static final String SCREENSHOT = "截图";

    private static String tellJMeterEnough = " WDS.sampleResult.setSuccessful(false)\n" +
            "\t   WDS.sampleResult.setResponseMessage('no test defined!')";

    /**
     * 将导入的 side 文件中定义的指令，转换为 webdriver sampler 中的 js 脚本
     *
     * @param request
     * @return
     */
    public static String getFullWebDriverScript(SideDTO request, CommandConfig globalConfig) {
        try {
            loopCount.set(1);
            if (request == null) {
                MSException.throwException("request is null!");
            }
            if (CollectionUtils.isEmpty(request.getTests())) {
                return tellJMeterEnough;
            }
            List<SideDTO.TestsDTO> testsDTOS = request.getTests();
            StringBuffer finalScript = new StringBuffer();
            // 遍历每一个定义流程，每个流程里的 commands 数组的每一个元素就是一个步骤
            testsDTOS.forEach(t -> {
                finalScript.append(getOneTestFullWebDriverScript(testsDTOS, t, request.getUrl(), globalConfig));
            });
            return finalScript.toString();
        } finally {
            // 清理线程变量
            loopCount.remove();
        }
    }

    /**
     * @param request
     * @param baseUrl 打开的第一个网页地址
     * @return
     */
    public static String getOneTestFullWebDriverScript(List<SideDTO.TestsDTO> testsDTOS, SideDTO.TestsDTO request, String baseUrl, CommandConfig globalConfig) {
        if (CollectionUtils.isEmpty(request.getCommands())) {
            return tellJMeterEnough;
        }
        StringBuffer finalScript = new StringBuffer();
        //注入全局初始化脚本 简化编程实现
        // 如果是多个 test 内容，则头部不重复写
        if ((testsDTOS.get(0).getId()).equals(request.getId())) {
            injectGlobalScript(finalScript);
        }
        AtomicReference<List<ScenarioVariable>> arr = new AtomicReference<>(new LinkedList<ScenarioVariable>());
        AtomicInteger count = new AtomicInteger(0);
        List<String> controlStopCmd = Lists.newArrayList("end", "repeatIf");
        List<String> controlIgnoreParseCmd = Lists.newArrayList("elseIf", "else", "end", "repeatIf");
        List<String> judgementList = Lists.newArrayList("if", "elseIf", "else");

        Stack<String> controlStack = new Stack<>();
        request.getCommands().stream().filter(v -> StringUtils.isNotBlank(v.getCommand())).forEach(c -> {
            // 如果指令名含有注释，则跳过
            if ((c.getCommand().trim()).startsWith("//")) {
                return;
            }
            AbstractCommand currentCmd = validateParam(c);
            JSONObject param = new JSONObject();
            //预先设置 WDS.browser.get(baseUrl);
            if (StringUtils.isBlank(c.getTarget()) && StringUtils.isNotBlank(baseUrl) && currentCmd.getName().equalsIgnoreCase(CommandConstants.OPEN)) {
                c.setTarget(baseUrl);
            }
            //处理特殊字符
            c.setValue(handleSpecialSymbol(c.getCommand(), c.getValue()));

            //某些指令比如 runScript
            escapeParam(c);
            // 设置指令 ID
            param.put("id", c.getId());
            if (StringUtils.equals(c.getCommand(), "storeJson")) {
                c.setTarget(TemplateUtils.escapeQuotes(c.getTarget()));
            }
            param.put("target", c.getTarget());
            if (StringUtils.equals(c.getCommand(), "screenshot") && StringUtils.isBlank(c.getTarget())) {
                param.put("target", SCREENSHOT);
            }
            // value 如果有双引号也需要转义
            param.put("value", c.getValue());
            param.put("targetType", c.getTargetType());
            param.put("vo", JSON.toJSONString(Optional.ofNullable(c.getCommandConfig()).orElse(new CommandConfig()).getVo()));
            // 设置单个指令的配置
            if (c.getCommandConfig() == null) {
                CommandConfig config = new CommandConfig();
                config.setIgnoreFail(true);
                c.setCommandConfig(config);
            }
            if (count.get() > 0 && Objects.nonNull(arr.get())) {
                c.getCommandConfig().setScenarioVariables(arr.get());
            }

            CommandConfig singleCommandConfigObj = c.getCommandConfig();
            //扩展参数
            singleCommandConfigObj.setExtract(checkIsExtract(c.getCommand()));
            param.put("singleCommandConfig", JSON.toJSONString(singleCommandConfigObj));

            param.put("customName", getCustomCommandName(c.getCommand(), StringUtils.isBlank(c.getCustomName()) ? currentCmd.getCnName() : c.getCustomName()));

            if (staticWebdriverScriptMap.get(currentCmd.getWebdriverScript()) == null) {
                return;
            }

            //将需要end结束符的入队
            if (!controlStopCmd.contains(currentCmd.getName()) && controlCmdList.contains(currentCmd.getName())) {
                controlStack.push(currentCmd.getName());
            }

            if (controlCmdList.contains(currentCmd.getName())) {
                String cmdScript = characterCut(currentCmd);
                cmdScript = parseCustomParam(c, currentCmd, cmdScript);
                if (!controlStopCmd.contains(currentCmd.getName())) {
                    if (CollectionUtils.isNotEmpty(c.getCommandConfig().getScenarioVariables())) {
                        arr.get().addAll(c.getCommandConfig().getScenarioVariables());
                    }
                    count.set(count.get() + 1);
                    if (!controlIgnoreParseCmd.contains(currentCmd.getName())) {
                        cmdScript = "beforeHandle('" + c.getId() + "'," + JSON.toJSONString(c.getCommandConfig()) + "); \n incrProgramRegistry('" + c.getId() + "');\n" + cmdScript;
                    }
                    if ("elseIf".equals(c.getCommand()) || "else".equals(c.getCommand())) {
                        cmdScript = cmdScript + "beforeHandle('" + c.getId() + "'," + JSON.toJSONString(c.getCommandConfig()) + ");\n ";
                    }
                }
                if (controlStopCmd.contains(currentCmd.getName())) {
                    count.set(count.get() - 1);
                    String pre = controlStack.pop();
                    if (judgementList.contains(pre)) {
                        cmdScript = " \n decrProgramRegistry(); " + cmdScript;
                    } else {
                        cmdScript = cmdScript + " \ndecrProgramRegistry(); ";
                    }

                    if (count.get() <= 0) {
                        arr.set(new LinkedList<>());
                    }
                }
                param.put("script", cmdScript);
            } else {
                param.put("script", staticWebdriverScriptMap.get(currentCmd.getWebdriverScript()));
            }
            param.put("originCommandType", c.getCommand());
            finalScript.append(currentCmd.toWebdriverSamplerScript(param)).append("\n");
        });
        if (finalScript.length() > 0) {
            // 添加结束指令
            finalScript.append("WDS.sampleResult.sampleEnd();\n");
            finalScript.append("WDS.sampleResult.setResponseHeaders(JSON.stringify(responseHeaders));");
            //fill globalConfig
            fillGlobalConfigByProps(globalConfig);
            String cmdConfig = JSON.toJSONString(globalConfig);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("CommandConfig", cmdConfig);

            finalScript.substring(0, finalScript.length() - 2);
            String rst = TemplateUtils.replaceVars(finalScript.toString(), paramMap);
            return rst;
        }
        return finalScript.toString();
    }

    private static void fillGlobalConfigByProps(CommandConfig globalConfig) {
        globalConfig.setResolutionRatioX(staticResolutionRatioX);
        globalConfig.setResolutionRatioY(staticResolutionRatioY);
    }

    private static boolean checkIsExtract(String command) {
        return staticExtractCommands.contains(command);
    }

    public static String getCustomCommandName(String command, String defaultName) {
        if (staticAssertionCommands.contains(command)) {
            return ASSERTION_NAME;
        }
        if (staticExtractCommands.contains(command)) {
            return EXTRACT_NAME;
        }
        //自定义名称需要处理转义字符问题
        if (StringUtils.isNotBlank(defaultName)) {
            //处理 \n \r \t 等转义字符
            defaultName = TemplateUtils.escapeQuotes(defaultName)
                    .replaceAll("\\\\n", "\\\\\\\\\\\\n")
                    .replaceAll("\\\\r", "\\\\\\\\\\\\r")
                    .replaceAll("\\\\t", "\\\\\\\\\\\\t")
                    .replaceAll("\\\\s", "\\\\\\\\\\\\s")
            ;
        }
        return defaultName;
    }

    private static void injectGlobalScript(StringBuffer finalScript) {
        Map<String, String> preParam = new HashMap<>();
        //这个js引擎需要特殊处理下windows和linux下的路径
        preParam.put("base64Url", replaceFirstSlash(staticJmeterService.getJmeterHome() + "/bin/base64.js"));
        preParam.put("mockJsUrl", replaceFirstSlash(staticJmeterService.getJmeterHome() + "/bin/mock-min.js"));
        preParam.put("injectFunction", staticUiCommonFunctionMap.get(TemplateConstants.FUNCTION_DRAW_SCRIPT));
        preParam.put("injectTypeFunction", staticUiCommonFunctionMap.get(TemplateConstants.FUNCTION_TYPE));
        preParam.put("environmentFunction", staticUiCommonFunctionMap.get(TemplateConstants.FUNCTION_ENVIRONMENT));
        finalScript.append(TemplateUtils.replaceVars(staticTemplateMap.get(TemplateConstants.TEMPLATE_INJECTSCRIPT), preParam));
    }

    private static String replaceFirstSlash(String originStr) {
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            return originStr.replaceFirst("\\/", "");
        }
        return originStr;
    }

    private static String handleSpecialSymbol(String command, String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        // 处理输入操作
        if (CommandConstants.TYPE.equals(command) || CommandConstants.SENDKEYS.equals(command)) {
            return value.replaceAll("\\n", "\\\\\\\\n");
        }

        if (CommandConstants.DRAGANDDROPTOOBJECTAT.equals(command) || CommandConstants.MOUSEDRAGANDDROPAT.equals(command)) {
            return value;
        }

        return TemplateUtils.escapeQuotes(value);
    }

    /**
     * 使用 Base64 转义某些特殊指令
     *
     * @param c
     */
    private static void escapeParam(SideDTO.TestsDTO.CommandsDTO c) {
        if (needEscapeCommandList.contains(c.getCommand())) {
            if (StringUtils.isNotBlank(c.getTarget())) {
                c.setTarget(CodingUtil.base64Encoding(c.getTarget()));
            }
        }
    }

    /**
     * 循环添加循环超时
     *
     * @param c
     * @param currentCmd
     * @param cmdScript
     * @return
     */
    public static String parseCustomParam(SideDTO.TestsDTO.CommandsDTO c, AbstractCommand currentCmd, String cmdScript) {
        String cmdName = currentCmd.getName();
        if (StringUtils.equalsAnyIgnoreCase(cmdName, "while", "do")) {
            return cmdScript.replace("\"#{timeout}\"", c.getCommandConfig().getCustomParam().toString());
        }
        return cmdScript;
    }

    public static AbstractCommand validateParam(SideDTO.TestsDTO.CommandsDTO c) {
        CommonCommand currentCmd = staticCommandMap.get(c.getCommand());
        if (Objects.isNull(currentCmd)) {
            MSException.throwException("指令配置错误！");
        }
//        doValidateParam(()->StringUtils.isNotBlank(currentCmd.getTarget()) && StringUtils.isBlank(c.getTarget()),
//                String.format("%s，参数校验失败： 未填写%s， 请检查！",currentCmd.getCnType(), currentCmd.getTargetCNName()));
//        doValidateParam(()->StringUtils.isNotBlank(currentCmd.getValue()) && StringUtils.isBlank(c.getValue()),
//                String.format("%s，参数校验失败： 未填写%s， 请检查！",currentCmd.getCnType(), currentCmd.getValueCNName()));

        return currentCmd;
    }

    /**
     * 执行检查
     *
     * @param conditionCallback
     * @param errorMsg
     */
    public static void doValidateParam(ObjectFactory<?> conditionCallback, String errorMsg) {
        Object condition = conditionCallback.getObject();
        if (condition instanceof Boolean && (Boolean) condition) {
            MSException.throwException(errorMsg);
        }
    }

    // 部分逻辑和循环控制指令脚本处理
    public static String characterCut(AbstractCommand curentCmd) {
        String cmdScript = staticWebdriverScriptMap.get(curentCmd.getWebdriverScript());
        String cmdName = curentCmd.getName();
        switch (cmdName) {
            case "times":
                cmdScript = characterCutEnd(cmdScript);
                cmdScript = handleLoopIndex(cmdScript);
                break;
            case "if":
                cmdScript = characterCutEnd(cmdScript);
                break;
            case "elseIf":
                // 前面去除 if(1){  去除结尾 }
                cmdScript = characterCutStart(cmdScript);
                cmdScript = characterCutEnd(cmdScript);
                break;
            case "else":
                // 前面去除 if(1){  去除结尾 }
                cmdScript = characterCutStart(cmdScript);
                cmdScript = characterCutEnd(cmdScript);
                break;
            case "end":
                // 前面去除 {
                cmdScript = characterCutStart(cmdScript);
                cmdScript = cmdScript + "}";
                break;
            case "do":
                // 去除结尾 }while(1)
                cmdScript = characterCutEnd(cmdScript);
                cmdScript = handleLoopIndex(cmdScript);
                break;
            case "repeatIf":
                // 前面去除 if(1){  去除结尾 }
                cmdScript = characterCutStart(cmdScript);
                cmdScript = "}" + cmdScript;
                break;
            case "while":
                cmdScript = characterCutEnd(cmdScript);
                cmdScript = handleLoopIndex(cmdScript);
                break;
            case "forEach":
                cmdScript = characterCutEnd(cmdScript);
                cmdScript = handleLoopIndex(cmdScript);
                break;
            default:
                return cmdScript;
        }
        return cmdScript;
    }

    /**
     * 循环的变量设置成不同的，避免死循环
     */
    private static String handleLoopIndex(String cmdScript) {
        Integer index = loopCount.get();
        loopCount.set(index + 1);
        return cmdScript.replaceAll("#\\{index\\}", index.toString());
    }

    /**
     * 去除结尾 "}..."
     */
    private static String characterCutEnd(String s) {
        for (int i = s.length() - 1; i > -1; i--) {
            if (s.charAt(i) == '}') {
                return s.substring(0, i);
            }
        }
        return s;
    }

    /**
     * 去除结尾 "...}"
     */
    private static String characterCutStart(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '}') {
                return s.substring(i + 1);
            }
        }
        return s;
    }

    /**
     * 获取并行集合线程名称
     *
     * @param dto
     * @return
     */
    public static String getParallelThreadName(ResultDTO dto) {
        return dto.getReportId() + "@" + dto.getTestId();
    }

    /**
     * 获取并行集合线程名称
     *
     * @param dto
     * @return
     */
    public static String getParallelThreadName(JmeterRunRequestDTO dto) {
        return dto.getReportId() + "@" + dto.getTestId();
    }

}
