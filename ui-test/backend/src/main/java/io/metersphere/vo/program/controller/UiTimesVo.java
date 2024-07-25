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
public class UiTimesVo extends UiProgramBaseVO {

    private String type = UiVoType.TIMES;

    private String times;

    @Override
    public String getCommand(MsUiCommand command) {
        return "times";
    }

    @Override
    public String getTarget() {
        return times;
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
        UiTimesVo vo = new UiTimesVo();
        String originTarget = sideCommand.getTarget();
        vo.setTimes(originTarget);
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

        UiTimesVo vo = null;
        if (command.getVo() instanceof UiTimesVo) {
            vo = (UiTimesVo) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (StringUtils.isBlank(vo.getTimes())) {
            return getFailResult(command, Translator.get("times") + Translator.get("is_null"));
        }
        return r;
    }
}
