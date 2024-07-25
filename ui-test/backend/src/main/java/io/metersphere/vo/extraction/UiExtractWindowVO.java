package io.metersphere.vo.extraction;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
public class UiExtractWindowVO extends UiCommandBaseVo {

    private String type = UiVoType.EXTRACTWINDOW;
    // 具体提取类型
    private String extractType;
    // 窗口 handle 变量名
    private String webHandleVarName;
    // 网页标题变量名
    private String webTitleVarName;

    @Override
    public String getCommand(MsUiCommand command) {
        if (StringUtils.isNotBlank(extractType)) {
            return extractType;
        }
        return null;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getTarget() {
        if (StringUtils.equalsIgnoreCase("storeWindowHandle", extractType)) {
            return webHandleVarName;
        }
        return null;
    }

    @Override
    public String getValue() {
        if (StringUtils.equalsIgnoreCase("storeTitle", extractType)) {
            return webTitleVarName;
        }
        return null;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiExtractWindowVO vo = new UiExtractWindowVO();
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        vo.setExtractType(replaceNote(sideCommand.getCommand()));
        if (StringUtils.equalsIgnoreCase("storeWindowHandle", vo.getExtractType())) {
            vo.setWebHandleVarName(originTarget);
        }
        if (StringUtils.equalsIgnoreCase("storeTitle", vo.getExtractType())) {
            vo.setWebTitleVarName(originValue);
        }
        msUiCommand.setVo(vo);
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiExtractWindowVO vo = null;
        if (command.getVo() instanceof UiExtractWindowVO) {
            vo = (UiExtractWindowVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(vo.getExtractType())) {
            return getFailResult(command, Translator.get("extract_type") + Translator.get("is_null"));
        }
        if ("storeWindowHandle".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isBlank(vo.getWebHandleVarName())) {
                return getFailResult(command, Translator.get("param_error"));
            }
        }
        if ("storeTitle".equalsIgnoreCase(vo.getExtractType())) {
            if (StringUtils.isBlank(vo.getWebTitleVarName())) {
                return getFailResult(command, Translator.get("webtitle_varname") + Translator.get("is_null"));
            }
        }
        return r;
    }
}
