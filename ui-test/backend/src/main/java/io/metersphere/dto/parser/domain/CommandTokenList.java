package io.metersphere.dto.parser.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
public class CommandTokenList {

    private List<CommandToken> tokenList = new LinkedList<>();

    public CommandTokenList(List<CommandToken> tokenList) {
        this.tokenList = tokenList;
    }

    public List<CommandToken> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<CommandToken> tokenList) {
        this.tokenList = tokenList;
    }

    public void add(CommandToken commandToken) {
        tokenList.add(commandToken);
    }
}
