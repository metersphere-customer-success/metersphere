package io.metersphere.vo.program.controller;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.hashtree.MsUiCommand;
import lombok.Data;

import java.util.ArrayList;

@Data
public class UiElseIfVo extends UiIfVo {
    private String type = UiVoType.ELSE_IF;

    @Override
    public String getCommand(MsUiCommand command) {
        return "elseIf";
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiElseIfVo vo = new UiElseIfVo();
        String originTarget = sideCommand.getTarget();
        vo.setModel("expression");
        vo.setParamsFilterType("And");
        vo.setExpression(originTarget);
        vo.setConditions(new ArrayList<>());
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_COMBINATION);
        addAppendPauseCommand(msUiCommand);
        msUiCommand.setEndCommand("end");
        msUiCommand.setViewType("programController");
        return msUiCommand;
    }
}

