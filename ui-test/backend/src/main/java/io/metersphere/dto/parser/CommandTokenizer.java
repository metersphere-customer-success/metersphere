package io.metersphere.dto.parser;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.parser.constants.CommandTokenType;
import io.metersphere.dto.parser.domain.CommandToken;
import io.metersphere.dto.parser.domain.CommandTokenList;


/**
 * 将  side 文件读取成 tokenList 以便反解析成 MsUiCommand
 */
public class CommandTokenizer {

    public CommandTokenList tokenize(SideDTO.TestsDTO testsDTO) {
        CommandTokenList commandTokenList = new CommandTokenList();
        if (testsDTO == null) {
            return commandTokenList;
        }
        for (SideDTO.TestsDTO.CommandsDTO command : testsDTO.getCommands()) {
            String atomicName = command.getCommand().replace("//", "");
            CommandTokenType tokenType = CommandTokenType.getTokenTypeByCommandName(atomicName);
            if (tokenType == null) {
                LogUtil.error(String.format("指令: %s 无法转成当前复合指令", atomicName));
                continue;
            }
            CommandToken token = new CommandToken(tokenType, command);
            commandTokenList.add(token);
        }
        return commandTokenList;
    }
}
