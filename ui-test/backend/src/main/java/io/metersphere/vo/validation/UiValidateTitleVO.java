package io.metersphere.vo.validation;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class UiValidateTitleVO extends UiValidateBaseVo {

    private String type = UiVoType.VALIDATETITLE;
    private String title;

    @Override
    public String getCommand(MsUiCommand command) {
        if (failOver) {
            return "assertTitle";
        }
        return "verifyTitle";
    }

    @Override
    public String getTarget() {
        return title;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String originTarget = sideCommand.getTarget();
        UiValidateTitleVO vo = new UiValidateTitleVO();
        vo.setOriginCommand(replaceNote(sideCommand.getCommand()));
        vo.setTitle(originTarget);
        CommandConfig commandConfig = sideCommand.getCommandConfig();
        if (commandConfig == null) {
            commandConfig = new CommandConfig();
            commandConfig.setIsNotStep(true);
            commandConfig.setIgnoreFail(false);
        }
        if (commandConfig.getIsNotStep() == null) {
            commandConfig.setIsNotStep(true);
        }
        if ("verifyTitle".equalsIgnoreCase(replaceNote(sideCommand.getCommand()))) {
            vo.setFailOver(!commandConfig.isIgnoreFail());
        } else {
            vo.setFailOver(true);
        }
        msUiCommand.setCommandConfig(commandConfig);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiValidateTitleVO vo = null;
        if (command.getVo() instanceof UiValidateTitleVO) {
            vo = (UiValidateTitleVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (StringUtils.isAnyBlank(vo.getTitle())) {
            return getFailResult(command, Translator.get("param_error"));
        }
        return r;
    }
}
