package io.metersphere.vo.program.controller;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class UiWhileVo extends UiProgramBaseVO {

    private String type = UiVoType.WHILE;

    private String expression;

    private int timeout = 20 * 1000;

    private Boolean isDoWhile = false;

    @Override
    public String getCommand(MsUiCommand command) {
        return isDoWhile ? "do" : "while";
    }

    @Override
    public String getTarget() {
        return isDoWhile ? null : expression;
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
    public SideDTO.TestsDTO.CommandsDTO toSideCommand(MsUiCommand command) {
        SideDTO.TestsDTO.CommandsDTO commandsDTO = super.toSideCommand(command);
        commandsDTO.getCommandConfig().setCustomParam(timeout);
        return commandsDTO;
    }

    @Override
    public SideDTO.TestsDTO.CommandsDTO getEndCommand(MsUiCommand command) {
        SideDTO.TestsDTO.CommandsDTO endCommand = super.getEndCommand(command);
        if (isDoWhile) {
            endCommand.setCommand("repeatIf");
            endCommand.setTarget(expression);
        }
        return endCommand;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiWhileVo vo = new UiWhileVo();
        String atomicCommand = sideCommand.getCommand();
        String originTarget = sideCommand.getTarget();
        if ("do".equalsIgnoreCase(atomicCommand)) {
            vo.setIsDoWhile(true);
        } else {
            vo.setIsDoWhile(false);
        }
        vo.setExpression(originTarget);
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_COMBINATION);
        addAppendPauseCommand(msUiCommand);
        msUiCommand.setEndCommand("repeatIf");
        msUiCommand.setViewType("programController");
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiWhileVo vo = null;
        if (command.getVo() instanceof UiWhileVo) {
            vo = (UiWhileVo) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (StringUtils.isBlank(vo.getExpression())) {
            return getFailResult(command, Translator.get("expression") + Translator.get("is_null"));
        }
        return r;
    }
}
