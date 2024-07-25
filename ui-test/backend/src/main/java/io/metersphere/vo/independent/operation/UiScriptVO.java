package io.metersphere.vo.independent.operation;

import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
public class UiScriptVO extends UiCommandBaseVo {

    private String type = "Script";
    private String clazzName = UiScriptVO.class.getCanonicalName();
    //"sync"表示同步脚本 runScript 或者 executeScript
    //"async"表示异步脚本 executeAsyncScript
    private String scriptType;//同步脚本
    /**
     * true 有返回值 false 无返回值
     */
    private boolean returnType;//无返回值
    private String returnValue;
    private String script;

    @Override
    public String getCommand(MsUiCommand command) {
        if ("async".equalsIgnoreCase(scriptType)) {
            return "executeAsyncScript";
        }
        if (returnType) {
            return "executeScript";
        }
        return "runScript";
    }

    @Override
    public String getTarget() {
        if (script == null) {
            return "";
        }
        return script;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return returnValue;
    }

    @Override
    public SideDTO.TestsDTO.CommandsDTO toSideCommand(MsUiCommand command) {
        command.getCommandConfig().setIgnoreFail(true);
        return super.toSideCommand(command);
    }

    @Override
    public MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiScriptVO vo = new UiScriptVO();
        String atomicCommand = replaceNote(sideCommand.getCommand());
        if (!"runScript".equalsIgnoreCase(atomicCommand)) {
            vo.setReturnType(true);
            vo.setReturnValue(sideCommand.getValue());
        } else {
            vo.setReturnType(false);
        }
        if ("executeAsyncScript".equalsIgnoreCase(atomicCommand)) {
            if (StringUtils.isBlank(sideCommand.getValue())) {
                vo.setReturnType(false);
            }
            vo.setScriptType("async");
        } else {
            vo.setScriptType("sync");
        }
        vo.setScript(sideCommand.getTarget());
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiScriptVO vo = null;
        if (command.getVo() instanceof UiScriptVO) {
            vo = (UiScriptVO) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        return r;
    }

}
