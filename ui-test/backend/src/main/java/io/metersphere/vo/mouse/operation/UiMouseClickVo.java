package io.metersphere.vo.mouse.operation;

import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
public class UiMouseClickVo extends UiCommandBaseVo {
    private String type = "MouseClick";
    private String clazzName = UiMouseClickVo.class.getCanonicalName();
    private String clickType;
    private String clickLocationX;
    private String clickLocationY;
    private boolean enableClickLocation;
    private boolean enableForcedClick;

    @Override
    public String getCommand(MsUiCommand command) {
        if (enableClickLocation) {
            return clickType + "At";
        }
        return clickType;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        if (enableClickLocation) {
            return clickLocationX + "," + clickLocationY;
        }
        return String.valueOf(enableForcedClick);
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiMouseClickVo vo = new UiMouseClickVo();
        String atomicCommand = sideCommand.getCommand();
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        if (atomicCommand.contains("At")) {
            vo.setEnableClickLocation(true);
        } else {
            vo.setEnableClickLocation(false);
            if (checkBooleanStr(originValue)) {
                vo.setEnableForcedClick(Boolean.parseBoolean(originValue));
            }
        }
        vo.setClickType(replaceNote(sideCommand.getCommand()).replace("At", ""));
        vo.setLocatorProp(originTarget);
        if (vo.isEnableClickLocation()) {
            if (StringUtils.isNotBlank(originValue) && originValue.contains(",")) {
                vo.setClickLocationX(originValue.split(",")[0]);
                vo.setClickLocationY(originValue.split(",")[1]);
            }
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    private boolean checkBooleanStr(String originValue) {
        if (StringUtils.isBlank(originValue)) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(String.valueOf(Boolean.FALSE), originValue)
                || StringUtils.equalsIgnoreCase(String.valueOf(Boolean.TRUE), originValue);
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiMouseClickVo vo = null;
        if (command.getVo() instanceof UiMouseClickVo) {
            vo = (UiMouseClickVo) command.getVo();
        }

        String validateStr = vo.validateLocateString();
        if (StringUtils.isNotBlank(validateStr)) {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, validateStr));
            return r;
        }

        if (vo.isEnableClickLocation()) {
            if (org.apache.commons.lang3.StringUtils.isAnyBlank(vo.getClickLocationX(), vo.getClickLocationY())) {
                r.setResult(false);
                r.setMsg(wrapErrorMsg(command, Translator.get("coord") + Translator.get("is_null")));
                return r;
            }
            try {
                int x = Integer.valueOf(vo.getClickLocationX());
                int y = Integer.valueOf(vo.getClickLocationY());
                if (x < 0) {
                    r.setResult(false);
                    r.setMsg(wrapErrorMsg(command, Translator.get("coord") + Translator.get("is_null")));
                    return r;
                }
            } catch (Exception e) {
            }
        }
        return r;
    }
}
