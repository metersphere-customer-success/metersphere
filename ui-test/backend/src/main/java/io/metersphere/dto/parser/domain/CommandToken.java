package io.metersphere.dto.parser.domain;


import io.metersphere.dto.SideDTO;
import io.metersphere.dto.parser.constants.CommandTokenType;

/**
 * 前端指令类型token
 */
public class CommandToken {
    private CommandTokenType tokenType;
    private SideDTO.TestsDTO.CommandsDTO value;

    public CommandToken(CommandTokenType tokenType, SideDTO.TestsDTO.CommandsDTO sideCommand) {
        this.tokenType = tokenType;
        this.value = sideCommand;
    }

    public CommandTokenType getTokenType() {
        return tokenType;
    }

    public SideDTO.TestsDTO.CommandsDTO getValue() {
        return value;
    }

    public void setTokenType(CommandTokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setValue(SideDTO.TestsDTO.CommandsDTO value) {
        this.value = value;
    }
}
