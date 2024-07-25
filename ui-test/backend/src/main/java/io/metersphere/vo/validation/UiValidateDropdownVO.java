package io.metersphere.vo.validation;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import io.metersphere.vo.BaseLocator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Data
public class UiValidateDropdownVO extends UiValidateBaseVo {

    private String type = UiVoType.VALIDATEDROPDOWN;

    private BaseLocator locator;
    private String assertType;
    private String assertValue;

    @Override
    public String getCommand(MsUiCommand command) {
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
        if (assertType != null && assertType.equals("assertSelectedLabel")) {
            // 去除前后空格处理
            assertValue = assertValue.trim();
        }
        return Optional.ofNullable(assertValue).orElse(null);
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String atomicCommand = replaceNote(sideCommand.getCommand());
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        UiValidateDropdownVO vo = new UiValidateDropdownVO();
        vo.setOriginCommand(replaceNote(sideCommand.getCommand()));
        vo.setLocator(BaseLocator.parse(originTarget));
        vo.setAssertType(atomicCommand.replace("verify", "assert"));
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
        } else {
            vo.setFailOver(true);
        }
        msUiCommand.setCommandConfig(commandConfig);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiValidateDropdownVO vo = null;
        if (command.getVo() instanceof UiValidateDropdownVO) {
            vo = (UiValidateDropdownVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (StringUtils.isAnyBlank(vo.getAssertValue())) {
            return getFailResult(command, Translator.get("param_error"));
        }
        return r;
    }
}
