package io.metersphere.vo.validation;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.CommandConstants;
import io.metersphere.constants.UiVoType;
import io.metersphere.constants.ValidateTypeConstants;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import io.metersphere.utils.UiGlobalConfigUtil;
import io.metersphere.vo.BaseLocator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Data
public class UiValidateElementVO extends UiValidateBaseVo {

    private String type = UiVoType.VALIDATEELEMENT;

    private BaseLocator locator;
    private String assertType;
    private String assertValue;

    @Override
    public String getCommand(MsUiCommand command) {
        if (UiGlobalConfigUtil.getConfig().isOperating()) {
            //导出时特殊处理 assertInText assertNotInText 都处理成包含或者不包含文本带上注释
            if (StringUtils.equalsAnyIgnoreCase(assertType, ValidateTypeConstants.ValidateElementEnum.ASSERT_IN_TEXT.getType(), ValidateTypeConstants.ValidateElementEnum.ASSERT_NOT_IN_TEXT.getType())) {
                if (failOver) {
                    if (StringUtils.equalsIgnoreCase(assertType, ValidateTypeConstants.ValidateElementEnum.ASSERT_IN_TEXT.getType())) {
                        return CommandConstants.ANNOTATE + CommandConstants.ASSERTTEXT;
                    } else {
                        return CommandConstants.ANNOTATE + CommandConstants.ASSERTNOTTEXT;
                    }
                }
                if (StringUtils.equalsIgnoreCase(assertType, ValidateTypeConstants.ValidateElementEnum.ASSERT_IN_TEXT.getType())) {
                    return CommandConstants.ANNOTATE + CommandConstants.VERIFYTEXT;
                } else {
                    return CommandConstants.ANNOTATE + CommandConstants.VERIFYNOTTEXT;
                }
            }
        }
        if (failOver) {
            return assertType;
        }
        return assertType.replace("assert", "verify");
    }

    @Override
    public String getTarget() {
        return locator.formatLocateString();
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return Optional.ofNullable(assertValue).orElse(null);
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String atomicCommand = replaceNote(sideCommand.getCommand());
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        UiValidateElementVO vo = new UiValidateElementVO();
        vo.setOriginCommand(replaceNote(sideCommand.getCommand()));
        //只有标题没有元素定位
        if (!atomicCommand.contains("Title")) {
            BaseLocator locator = BaseLocator.parse(originTarget);
            vo.setLocator(locator);
        }
        vo.setAssertValue(originValue);
        CommandConfig commandConfig = sideCommand.getCommandConfig();
        if (commandConfig == null) {
            commandConfig = new CommandConfig();
            commandConfig.setIsNotStep(true);
            commandConfig.setIgnoreFail(false);
        }
        if (commandConfig.getIsNotStep() == null) {
            commandConfig.setIsNotStep(true);
        }
        if (atomicCommand.startsWith("verify")) {
            vo.setFailOver(!commandConfig.isIgnoreFail());
            msUiCommand.setCommand(atomicCommand.replace("verify", "assert"));
        } else {
            vo.setFailOver(true);
        }
        vo.setAssertType(atomicCommand.replace("verify", "assert"));
        msUiCommand.setCommandConfig(commandConfig);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiValidateElementVO vo = null;
        if (command.getVo() instanceof UiValidateElementVO) {
            vo = (UiValidateElementVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        String valS = locator.validateLocateString();
        if (StringUtils.isNotBlank(valS)) {
            return getFailResult(command, valS);
        }
        if (StringUtils.isAnyBlank(vo.getAssertValue())) {
            return getFailResult(command, Translator.get("param_error"));
        }
        return r;
    }
}
