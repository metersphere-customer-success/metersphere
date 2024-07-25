package io.metersphere.hashtree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.googlecode.jmeter.plugins.webdriver.sampler.WebDriverSampler;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.AtomicCommandTypeEnum;
import io.metersphere.constants.CommandConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.KeyValue;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.impl.CommandConfig;
import io.metersphere.impl.CommonCommand;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.UiCommandService;
import io.metersphere.utils.WebDriverSamplerHelper;
import io.metersphere.vo.browser.operation.UiOpenVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsWebDriverSampler extends MsTestElement {
    private static final Logger logger = LoggerFactory.getLogger("web-driver-sampler");
    @JsonProperty
    private String type = "WebDriverSampler";
    private String clazzName = MsWebDriverSampler.class.getCanonicalName();

    @JsonProperty
    private String script = "";
    @JsonProperty
    private String projectId;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
    }

    private static WebDriverSampler getWebDriverSampler(ParameterConfig config, String script) {
        WebDriverSampler webDriverSampler = new WebDriverSampler();
        webDriverSampler.setEnabled(true);
        if (config != null && BooleanUtils.isTrue(config.getRetryEnable())) {
            //加上重试方便backendlistener统一处理重复的结果
            webDriverSampler.setName("MsRetry_WebDriverSampler");
        } else {
            webDriverSampler.setName("WebDriverSampler");
        }
        webDriverSampler.setName("WebDriverSampler");
        webDriverSampler.setProperty(TestElement.TEST_CLASS, WebDriverSampler.class.getName());
        webDriverSampler.setProperty(TestElement.GUI_CLASS, "com.googlecode.jmeter.plugins.webdriver.sampler.gui.WebDriverSamplerGui");
        webDriverSampler.setScript(script);
//        webDriverSampler.setScriptLanguage("js"); nashorn 使用、由于升级了jdk，如果要引用必须使用 add-module 方式比较繁琐，因此干脆使用 graalvmjs，引入方式相对简单
        webDriverSampler.setScriptLanguage(SystemConstants.JSENGINE_GRAAL);
        return webDriverSampler;
    }

    /**
     * 将多条 command 生成一个 WebDriverSamplerHelper
     *
     * @param uiCommands
     * @param config
     */
    public static WebDriverSampler parseCommandsToSampler(List<MsUiCommand> uiCommands, ParameterConfig config) {
        SideDTO sideDTO = generateSideDTO(uiCommands, config);
        String script = WebDriverSamplerHelper.getFullWebDriverScript(sideDTO,
                Optional.ofNullable((CommandConfig) config.getCommandConfig()).orElse(new CommandConfig()));
        logger.info("reportId: " + config.getReportId() + ", script: \n[{}]\n", script);
        return getWebDriverSampler(config, script);
    }

    private static SideDTO generateSideDTO(List<MsUiCommand> uiCommands, ParameterConfig config) {
        SideDTO sideDTO = new SideDTO();
        sideDTO.setUrl(Optional.ofNullable(config.getHeader("baseURL")).orElse(new KeyValue()).getValue());
        SideDTO.TestsDTO testsDTO = generateTestsDTO(uiCommands, config);
        sideDTO.setTests(new ArrayList<>() {{
            add(testsDTO);
        }});
        return sideDTO;
    }

    private static SideDTO.TestsDTO generateTestsDTO(List<MsUiCommand> uiCommands, ParameterConfig config) {
        SideDTO.TestsDTO testsDTO = new SideDTO.TestsDTO();
        testsDTO.setId(UUID.randomUUID().toString());
        List<SideDTO.TestsDTO.CommandsDTO> commands = parseMsCommands(uiCommands, config);

        afterHandle(commands);

        List<SideDTO.TestsDTO.CommandsDTO> commandsDTOS = commands.stream()
                .filter(SideDTO.TestsDTO.CommandsDTO::isEnable)
                .collect(Collectors.toList());
        //生成 cookie
        if (config.isGenerateCookie()) {
            generateCookieCommand(commandsDTOS);
        }
        //免登录
        if (config.isEnableCookieShare()) {
            setCookieBeforeScenario(commandsDTOS, config);
        }
        testsDTO.setCommands(commandsDTOS);
        return testsDTO;
    }

    private static void generateCookieCommand(List<SideDTO.TestsDTO.CommandsDTO> commandsDTOS) {
        SideDTO.TestsDTO.CommandsDTO getCookieCmd = new SideDTO.TestsDTO.CommandsDTO();
        getCookieCmd.setId(UUID.randomUUID().toString());
        getCookieCmd.setCommand(CommandConstants.GETCOOKIE);
        getCookieCmd.setEnable(true);
        getCookieCmd.setCustomName("cookie");
        getCookieCmd.setId("cookie");
        commandsDTOS.add(getCookieCmd);
    }

    private static void setCookieBeforeScenario(List<SideDTO.TestsDTO.CommandsDTO> commandsDTOS, ParameterConfig config) {
        if (CollectionUtils.isNotEmpty(commandsDTOS)) {
            int firstIndex = 0;
            for (int i = 0; i < commandsDTOS.size(); i++) {
                if (StringUtils.equalsIgnoreCase(commandsDTOS.get(i).getCommand(), CommandConstants.OPEN)) {
                    firstIndex = i;
                    break;
                }
            }
            SideDTO.TestsDTO.CommandsDTO firstOpenCmd = commandsDTOS.get(firstIndex);
            SideDTO.TestsDTO.CommandsDTO repeatOpenCmd = new SideDTO.TestsDTO.CommandsDTO();
            BeanUtils.copyBean(repeatOpenCmd, firstOpenCmd);

            repeatOpenCmd.setId(UUID.randomUUID().toString());
            CommandConfig commandConfig = new CommandConfig();
            commandConfig.setIsNotStep(true);
            repeatOpenCmd.setCommandConfig(commandConfig);

            SideDTO.TestsDTO.CommandsDTO setCookieCmd = new SideDTO.TestsDTO.CommandsDTO();
            setCookieCmd.setCommand(CommandConstants.SETCOOKIE);
            setCookieCmd.setTarget(config.getCookie());
            setCookieCmd.setEnable(true);
            setCookieCmd.setCustomName("setCookie");
            setCookieCmd.setId("setcookie");
            commandsDTOS.add(setCookieCmd);

            commandsDTOS.add(firstIndex, setCookieCmd);
            commandsDTOS.add(firstIndex, firstOpenCmd);
        }
    }

    public static List<SideDTO.TestsDTO.CommandsDTO> parseMsCommands(List<MsUiCommand> uiCommands, ParameterConfig config) {
        List<SideDTO.TestsDTO.CommandsDTO> commands = new ArrayList<>();
        uiCommands.stream().forEach(command -> parseCommand(command, commands, config));
        //在导出的过程中 检查是否有打开新窗口，这里插入一条切换窗口句柄的指令
        checkOpenNewWindow(commands);
        return commands;
    }

    private static void checkOpenNewWindow(List<SideDTO.TestsDTO.CommandsDTO> commands) {
        List<Integer> splitIndexList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(commands)) {
            for (int i = 0; i < commands.size(); i++) {
                if (commands.get(i).isOpensWindow()) {
                    splitIndexList.add(i);
                }
            }
        }

        for (int i = 0; i < splitIndexList.size(); i++) {
            commands.add(splitIndexList.get(i) + (i + 1), createSelectWindowCmd());
        }
    }

    private static SideDTO.TestsDTO.CommandsDTO createSelectWindowCmd() {
        SideDTO.TestsDTO.CommandsDTO selectWindowHandle = new SideDTO.TestsDTO.CommandsDTO();
        selectWindowHandle.setId(UUID.randomUUID().toString());
        selectWindowHandle.setCommand(CommandConstants.SELECTWINDOW);
        selectWindowHandle.setTarget(String.format("handle=${%s}", UiOpenVO.innerHandleName));
        return selectWindowHandle;
    }


    /**
     * 将 MsUiCommand 转成 SideDTO.TestsDTO.CommandsDTO
     * 并添加到指令集中
     *
     * @param command
     * @param commands
     * @param config
     */
    private static void parseCommand(MsUiCommand command, List<SideDTO.TestsDTO.CommandsDTO> commands, ParameterConfig config) {
        Map<String, CommonCommand> uiCommandMap = (Map<String, CommonCommand>) CommonBeanFactory.getBean("uiCommandMap");
        if (uiCommandMap.containsKey(command.getCommand())) {
            command.setCommandType(CommandType.COMMAND_TYPE_ATOM);
        }
        if (StringUtils.equals(command.getViewType(), AtomicCommandTypeEnum.programController.getName())) {
            // 流程控制没有前后置，导入的时候可能带了
            command.setPreCommands(new ArrayList<>());
            command.setPostCommands(new ArrayList<>());
            if (command.getAppendCommands() == null) {
                command.setAppendCommands(new ArrayList<>());
            }
        }
        UiCommandService uiCommandService = CommonBeanFactory.getBean(UiCommandService.class);
        //处理所有的原子指令 给 command 以及前后置等，设置 target 和 value
        uiCommandService.processCommandParam(command);
        if (config != null && config.getCommandConfig() != null) {
            command.getCommandConfig().setHeadlessEnabled(((CommandConfig) config.getCommandConfig()).isHeadlessEnabled());
        }
        //方便指令的编写，把 uicomamnd vo也传递给 ideCommand
        command.getCommandConfig().setVo(command.getVo());

        // 添加前置指令
        addCommands(command, command.getPreCommands(), commands);

        if (StringUtils.isBlank(command.getCommandType())) {
            SideDTO.TestsDTO.CommandsDTO commandsDTO = command.toSideCommand();
            commandsDTO.setCustomName(command.getName());
            commands.add(commandsDTO);
        } else {
            switch (command.getCommandType()) {
                // 复合指令，先将其vo转成原子指令，再解析HashTree，最后加上end结束
                case CommandType.COMMAND_TYPE_COMBINATION:
                    // 添加start 指令
                    SideDTO.TestsDTO.CommandsDTO combCommandsDTO = command.getVo().toSideCommand(command);
                    combCommandsDTO.setCustomName(command.getName());
                    commands.add(combCommandsDTO);
                    if (CollectionUtils.isNotEmpty(command.getHashTree())) {
                        command.getHashTree().forEach(item -> {
                            if (!command.isEnable()) {
                                // 如果复合指令为disable，内部指令也置为disable
                                item.setEnable(false);
                            }
                            if (item instanceof MsUiCommand) {
                                // 添加嵌套指令
                                parseCommand((MsUiCommand) item, commands, config);
                            } else if (item instanceof MsUiScenario) {
                                delUiScenario(commands, config, item);
                            }
                        });
                    }
                    // 只有MS运行时添加Append指令，导出时不增加
                    if (!config.isOperating()) {
                        addCommands(command, command.getAppendCommands(), commands);
                    }

                    // 添加end指令
                    commands.add(command.getVo().getEndCommand(command));

                    break;
                // 代理指令将vo转成原子指令
                case CommandType.COMMAND_TYPE_PROXY:
                    SideDTO.TestsDTO.CommandsDTO proxyCommandsDTO = command.getVo().toSideCommand(command);
                    proxyCommandsDTO.setCustomName(command.getName());
                    commands.add(proxyCommandsDTO);
                    break;
                // 复合的代理指令，如数据提取，断言
                case CommandType.COMMAND_TYPE_COMBINATION_PROXY:
                    if (CollectionUtils.isNotEmpty(command.getHashTree())) {
                        command.getHashTree().forEach(item -> {
                            if (item instanceof MsUiCommand) {
                                // 添加嵌套指令
                                parseCommand((MsUiCommand) item, commands, config);
                            } else if (item instanceof MsUiScenario) {
                                //处理while if 等语句嵌套场景的问题
                                if (CollectionUtils.isNotEmpty(item.getHashTree())) {
                                    for (MsTestElement msTestElement : item.getHashTree()) {
                                        parseCommand((MsUiCommand) msTestElement, commands, config);
                                    }
                                }
                            }
                        });
                    }
                    break;
                default:
                    SideDTO.TestsDTO.CommandsDTO defaultCommandsDTO = command.toSideCommand();
                    defaultCommandsDTO.setCustomName(command.getName());
                    commands.add(defaultCommandsDTO);
            }
        }

        // 添加后置指令
        addCommands(command, command.getPostCommands(), commands);

    }

    private static void delUiScenario(List<SideDTO.TestsDTO.CommandsDTO> commands, ParameterConfig config, MsTestElement item) {
        //处理while if 等语句嵌套场景的问题
        if (CollectionUtils.isNotEmpty(item.getHashTree())) {
            for (MsTestElement msTestElement : item.getHashTree()) {
                //嵌套自定义指令的情况
                if (msTestElement instanceof MsUiCustomCommand) {
                    if (CollectionUtils.isNotEmpty(msTestElement.getHashTree())) {
                        msTestElement.getHashTree().forEach(t -> {
                            parseCommand((MsUiCommand) t, commands, config);
                        });
                    }
                } else if (msTestElement instanceof MsUiScenario) {
                    delUiScenario(commands, config,msTestElement);
                }
                else {
                    //普通的场景里面嵌套
                    parseCommand((MsUiCommand) msTestElement, commands, config);
                }
            }
        }
    }


    private static void addCommands(MsUiCommand parentCommand, List<MsUiCommand> msUiCommands, List<SideDTO.TestsDTO.CommandsDTO> commands) {
        if (CollectionUtils.isNotEmpty(msUiCommands)) {
            msUiCommands.forEach(msUiCommand -> {
                if (!parentCommand.isEnable()) {
                    msUiCommand.setEnable(false);
                }
                // 数据提取和断言的前后置需要从 hashTree 处理指令
                if (UiCommandService.VirtualCommands.contains(msUiCommand.getCommand())) {
                    if (CollectionUtils.isNotEmpty(msUiCommand.getHashTree())) {
                        for (MsTestElement subCommand : msUiCommand.getHashTree()) {
                            MsUiCommand subUiCommand = (MsUiCommand) subCommand;
                            if (!parentCommand.isEnable() || !msUiCommand.isEnable()) {
                                // 如果父disable，内部指令也置为disable
                                subUiCommand.setEnable(false);
                            }
                            SideDTO.TestsDTO.CommandsDTO sideCommand = subUiCommand.getVo().toSideCommand((subUiCommand));
                            sideCommand.getCommandConfig().setVo(((MsUiCommand) subCommand).getVo());
                            commands.add(sideCommand);

                            //如果父级是 proxy
                            if (CommandType.COMMAND_TYPE_COMBINATION_PROXY.equals(msUiCommand.getCommandType())) {
                                //只生成1张截图
                                subUiCommand.getCommandConfig().setCombinationProxyId(msUiCommand.getId() + "_" + msUiCommand.getCommand());
                            }
                        }
                    }
                } else if ("cmdScript".equalsIgnoreCase(msUiCommand.getCommand())) {
                    commands.add(msUiCommand.getVo().toSideCommand(msUiCommand));
                    //前后置脚本
                } else {
                    //等待时间 等原子指令
                    commands.add(msUiCommand.toSideCommand());
                }
            });
        }
    }

    private static void afterHandle(List<SideDTO.TestsDTO.CommandsDTO> commands) {
        Set<String> extractCommands = (Set<String>) CommonBeanFactory.getBean("extractCommands");
        commands.forEach(item -> {
            // 提取忽略错误
            if (extractCommands.contains(item.getCommand())) {
                item.getCommandConfig().setIgnoreFail(true);
            }
        });
    }
}
