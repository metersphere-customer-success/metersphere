package io.metersphere.service;

import io.metersphere.constants.ArgTypeEnum;
import io.metersphere.constants.UiProcessType;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.impl.CommandConfig;
import io.metersphere.impl.CommonCommand;
import io.metersphere.utils.ArgTypeParamProcessor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * command 指令核心处理类，指令参数转换，校验
 */
@Service
public class UiCommandService {
    @Resource(name = "uiCommandMap")
    Map<String, CommonCommand> uiCommandMap;

    public static final List VirtualCommands = new ArrayList() {{
        add("cmdValidation");
        add("cmdExtraction");
    }};

    /**
     * 对所有的指令 target command
     * 1.处理 targetVO valueVO 到 target value
     * 2.校验不同指令的参数是否合法
     *
     */
    public void processCommandParam(MsUiCommand uiCommand) {
        String command = uiCommand.getCommand();
        CommonCommand cmd = uiCommandMap.get(command);

        if (uiCommand.getCommandConfig() == null) {
            uiCommand.setCommandConfig(new CommandConfig());
        }

        if (cmd != null) {
            if (StringUtils.isNotBlank(cmd.getTarget())) {
                ArgTypeParamProcessor.process(ArgTypeEnum.valueOf(cmd.getTarget()), cmd.getName(), uiCommand, "target");
            }
            if (StringUtils.isNotBlank(cmd.getValue())) {
                ArgTypeParamProcessor.process(ArgTypeEnum.valueOf(cmd.getValue()), cmd.getName(), uiCommand, "value");
            }
        }
        Integer screenshotConfig = uiCommand.getCommandConfig().getScreenshotConfig();
        processChildCommandsParam(uiCommand.getPreCommands(), UiProcessType.PRE.name(), true, screenshotConfig);
        processChildCommandsParam(uiCommand.getPostCommands(), UiProcessType.POST.name(), true, screenshotConfig);
        processChildCommandsParam(uiCommand.getAppendCommands(), UiProcessType.APPEND.name(), true, screenshotConfig);
    }

    private void processChildCommandsParam(List<MsUiCommand> commands, String processType, boolean isNotStep, Integer screenshotConfig) {
        if (CollectionUtils.isNotEmpty(commands)) {
            commands.forEach((command) -> {
                // 数据提取和断言的前后置需要从 hashTree 处理指令
                if (VirtualCommands.contains(command.getCommand())) {
                    if (CollectionUtils.isNotEmpty(command.getHashTree())) {
                        command.getHashTree().forEach(subCommands -> {
                            CommandConfig commandConfig = Optional.ofNullable(((MsUiCommand)subCommands).getCommandConfig()).orElse(new CommandConfig());
                            commandConfig.setScreenShotWhenSuccess(false);
                            setCommandConfig(processType, isNotStep, commandConfig, screenshotConfig);
                            ((MsUiCommand) subCommands).setCommandConfig(commandConfig);
                            processCommandParam(((MsUiCommand) subCommands));
                        });
                    }
                } else {
                    processCommandParam(command);
                    CommandConfig commandConfig = command.getCommandConfig();
                    commandConfig.setScreenShotWhenSuccess(false);
                    setCommandConfig(processType, isNotStep, commandConfig, screenshotConfig);
                }
            });
        }
    }

    private void setCommandConfig(String processType, boolean isNotStep, CommandConfig config, Integer screenshotConfig) {
        config.setProcessType(processType);
        config.setIsNotStep(isNotStep);
        config.setScreenshotConfig(screenshotConfig);
    }
}
