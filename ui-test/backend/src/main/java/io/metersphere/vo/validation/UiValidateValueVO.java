package io.metersphere.vo.validation;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.constants.ValidateTypeConstants;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import io.metersphere.utils.UiGlobalConfigUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class UiValidateValueVO extends UiValidateBaseVo {

    private String type = UiVoType.VALIDATEVALUE;

    private String varName;
    private String varValue;

    @Override
    public String getCommand(MsUiCommand command) {
        //如果是导出
        if (UiGlobalConfigUtil.getConfig().isOperating()) {
            if (!StringUtils.equalsIgnoreCase(optType, ValidateTypeConstants.ValidateValueEnum.EQUAL.getType())) {
                if (failOver) {
                    return "//verify";
                }
                return "//assert";
            }
        }
        if (failOver) {
            return "assert";
        }
        return "verify";
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getTarget() {
        if (StringUtils.isNotBlank(varName)) {
            if (varName.startsWith("${") && varName.endsWith("}")) {
                varName = varName.substring(2, varName.length() - 1);
            }
        }
        return varName;
    }

    @Override
    public String getValue() {
        return varValue;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        UiValidateValueVO vo = new UiValidateValueVO();
        vo.setOriginCommand(replaceNote(sideCommand.getCommand()));
        if (StringUtils.isNotBlank(originTarget)) {
            vo.setVarName("${" + originTarget + "}");
        }
        vo.setVarValue(originValue);
        CommandConfig commandConfig = sideCommand.getCommandConfig();
        if (commandConfig == null) {
            commandConfig = new CommandConfig();
            commandConfig.setIsNotStep(true);
            commandConfig.setIgnoreFail(false);
        }
        if (commandConfig.getIsNotStep() == null) {
            commandConfig.setIsNotStep(true);
        }
        if ("verify".equalsIgnoreCase(replaceNote(sideCommand.getCommand()))) {
            vo.setFailOver(!commandConfig.isIgnoreFail());
        } else {
            vo.setFailOver(true);
        }
        vo.setOptType(ValidateTypeConstants.ValidateValueEnum.EQUAL.getType());
        msUiCommand.setCommandConfig(commandConfig);
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiValidateValueVO vo = null;
        if (command.getVo() instanceof UiValidateValueVO) {
            vo = (UiValidateValueVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (StringUtils.isAnyBlank(vo.getVarName(), vo.getVarValue())) {
            return getFailResult(command, Translator.get("param_error"));
        }
        return r;
    }
}
