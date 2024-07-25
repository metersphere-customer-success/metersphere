package io.metersphere.vo.element.operation;

import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

@Data
public class UiSetItemVo extends UiCommandBaseVo {

    private String type = "SetItem";
    private String clazzName = UiSetItemVo.class.getCanonicalName();

    @Override
    public String getCommand(MsUiCommand command) {
        return optContent;
    }

    @Override
    public String getTarget() {
        return super.getTarget();
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiSetItemVo vo = new UiSetItemVo();
        vo.setOptContent(replaceNote(sideCommand.getCommand()));
        String originTarget = sideCommand.getTarget();
        vo.setLocatorProp(originTarget);
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiSetItemVo vo = null;
        if (command.getVo() instanceof UiSetItemVo) {
            vo = (UiSetItemVo) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        String validateStr = vo.validateLocateString();
        if (StringUtils.isNotBlank(validateStr)) {
            //第一优先级
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, validateStr));
            return r;
        }
        return r;
    }
}
