package io.metersphere.vo.program.controller;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UiForEachVo extends UiProgramBaseVO {

    private String type = UiVoType.FOR_EACH;

    private String arrayVariable;
    private String iteratorVariable;

    @Override
    public String getCommand(MsUiCommand command) {
        return "forEach";
    }

    @Override
    public String getTarget() {
        if (StringUtils.isNotBlank(arrayVariable) && !arrayVariable.startsWith("${")) {
            // 转成标准的json格式
            return arrayVariable.replaceAll("\"", "\\\'");
        }
        return arrayVariable;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return iteratorVariable;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiForEachVo vo = new UiForEachVo();
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        vo.setArrayVariable(originTarget);
        vo.setIteratorVariable(originValue);
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_COMBINATION);
        addAppendPauseCommand(msUiCommand);
        msUiCommand.setEndCommand("end");
        msUiCommand.setViewType("programController");
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiForEachVo vo = null;
        if (command.getVo() instanceof UiForEachVo) {
            vo = (UiForEachVo) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }

        if (StringUtils.isAnyBlank(arrayVariable, iteratorVariable)) {
            return getFailResult(command, Translator.get("param_error"));
        }

        return r;
    }
}
