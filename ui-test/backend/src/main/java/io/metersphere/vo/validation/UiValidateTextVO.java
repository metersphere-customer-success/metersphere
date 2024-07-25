package io.metersphere.vo.validation;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
public class UiValidateTextVO extends UiValidateBaseVo {

    private String type = UiVoType.VALIDATETEXT;

    private String text;
    private boolean confirmWindow;

    @Override
    public String getCommand(MsUiCommand command) {
        if (StringUtils.isNotBlank(originCommand)) {
            return originCommand;
        }
        if (failOver) {
            return "assertPrompt";
        }
        return "verifyPrompt";
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getTarget() {
        return text;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String originTarget = sideCommand.getTarget();
        UiValidateTextVO vo = new UiValidateTextVO();
        vo.setText(originTarget);
        //这个参数暂时不起效
        vo.setConfirmWindow(true);
        vo.setFailOver(true);
        vo.setOriginCommand(replaceNote(sideCommand.getCommand()));
        msUiCommand.setVo(vo);
        CommandConfig commandConfig = new CommandConfig();
        commandConfig.setIsNotStep(true);
        commandConfig.setIgnoreFail(false);
        msUiCommand.setCommandConfig(commandConfig);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiValidateTextVO vo = null;
        if (command.getVo() instanceof UiValidateTextVO) {
            vo = (UiValidateTextVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(vo.getText())) {
            return getFailResult(command, Translator.get("param_error"));
        }
        return r;
    }
}
