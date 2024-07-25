package io.metersphere.vo.validation;

import io.metersphere.constants.ValidateTypeConstants;
import io.metersphere.dto.SideDTO;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;

@Data
public abstract class UiValidateBaseVo extends UiCommandBaseVo {
    protected boolean failOver;

    protected String originCommand;

    protected String optType = ValidateTypeConstants.ValidateValueEnum.EQUAL.getType();
    /**
     * 模板方法 将展示层 vo 转成 运行时 vo
     *
     * @param command
     * @return
     */
    public SideDTO.TestsDTO.CommandsDTO toSideCommand(MsUiCommand command) {
        SideDTO.TestsDTO.CommandsDTO commandsDTO = super.toSideCommand(command);
        if (failOver) {
            //失败继续
            commandsDTO.getCommandConfig().setIgnoreFail(false);
        } else {
            commandsDTO.getCommandConfig().setIgnoreFail(true);
        }
        return commandsDTO;
    }
}
