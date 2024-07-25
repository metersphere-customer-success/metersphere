package io.metersphere.vo.browser.operation;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import java.util.List;

@Data
public class UiSetWindowSizeVO extends UiCommandBaseVo {

    private String type = "SetWindowSize";
    private String clazzName = UiSetWindowSizeVO.class.getCanonicalName();
    // 像素长度 X
    private String coordX;
    // 像素宽度 Y
    private String coordY;

    @Override
    public String getCommand(MsUiCommand command) {
        return "setWindowSize";
    }

    @Override
    public String getTarget() {
        // 选择全屏或者指定窗口大小
        if (StringUtils.equalsIgnoreCase("fullScreen", optContent)) {
            return "fullScreen";
        } else if (StringUtils.equalsIgnoreCase("setSize", optContent)) {
            return coordX + "x" + coordY;
        } else {
            return null;
        }
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
        UiSetWindowSizeVO vo = new UiSetWindowSizeVO();
        String originTarget = sideCommand.getTarget();
        if (StringUtils.isNotBlank(originTarget)) {
            if (StringUtils.equals("fullScreen", originTarget)) {
                vo.setOptContent("fullScreen");
            } else {
                vo.setOptContent("setSize");
                try {
                    vo.setCoordX(originTarget.split("x")[0]);
                    vo.setCoordY(originTarget.split("x")[1]);
                } catch (Exception e) {
                    LogUtil.error(String.format("转换 setWindowSize 指令出错! %s", originTarget));
                }
            }
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiSetWindowSizeVO vo = null;
        if (command.getVo() instanceof UiSetWindowSizeVO) {
            vo = (UiSetWindowSizeVO) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        switch (vo.getOptContent()) {
            case "setSize":
                if (StringUtils.isBlank(vo.getCoordX())) {
                    r.setResult(false);
                    r.setMsg(wrapErrorMsg(command, Translator.get("coord") + ":x " + Translator.get("is_null")));
                }
                if (StringUtils.isBlank(vo.getCoordY())) {
                    r.setResult(false);
                    r.setMsg(wrapErrorMsg(command, Translator.get("coord") + ":y " + Translator.get("is_null")));
                }
                break;
            default:
                break;
        }
        return r;
    }
}