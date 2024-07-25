package io.metersphere.vo.program.controller;


import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.vo.UiAtomicCommandVO;
import io.metersphere.vo.UiCommandBaseVo;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class UiProgramBaseVO extends UiCommandBaseVo {
    protected void addAppendPauseCommand(MsUiCommand msUiCommand) {
        MsUiCommand pauseCommand = createPauseCommand();
        UiAtomicCommandVO targetVO = new UiAtomicCommandVO();
        targetVO.setText(0);
        pauseCommand.setTargetVO(targetVO);
        msUiCommand.setAppendCommands(new ArrayList<>() {{
            add(pauseCommand);
        }});
    }
}
