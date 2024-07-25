package io.metersphere.vo.mouse.operation;

import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class UiMouseMoveVo extends UiCommandBaseVo {
    private String type = "MouseMove";
    private String clazzName = UiMouseMoveVo.class.getCanonicalName();

    private String moveType;
    private String moveLocationX;
    private String moveLocationY;

    @Override
    public String getCommand(MsUiCommand command) {
        return moveType;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        if (StringUtils.equals("mouseMoveAt", moveType)) {
            return moveLocationX + "," + moveLocationY;
        }
        return null;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiMouseMoveVo vo = new UiMouseMoveVo();
        String atomicCommand = replaceNote(sideCommand.getCommand());
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        vo.setMoveType(atomicCommand);
        vo.setLocatorProp(originTarget);
        if (atomicCommand.equalsIgnoreCase("mouseMoveAt")) {
            try {
                vo.setMoveLocationX(originValue.split(",")[0]);
                vo.setMoveLocationY(originValue.split(",")[1]);
            } catch (Exception e) {
                vo.setMoveLocationX("0");
                vo.setMoveLocationY("0");
            }
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiMouseMoveVo vo = null;
        if (command.getVo() instanceof UiMouseMoveVo) {
            vo = (UiMouseMoveVo) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        if (!StringUtils.equalsIgnoreCase("mouseMoveAt", vo.getMoveType())) {
            String valS1 = vo.validateLocateString();
            if (StringUtils.isNotBlank(valS1)) {
                return getFailResult(command, valS1);
            }

            try {
                int x = Integer.valueOf(vo.getMoveLocationX());
                int y = Integer.valueOf(vo.getMoveLocationY());
                if (x < 0) {
                    return getFailResult(command, Translator.get("coord") + "x:" + Translator.get("cant_be_negative"));

                }
                if (y < 0) {
                    return getFailResult(command, Translator.get("coord") + "y:" + Translator.get("cant_be_negative"));
                }
            } catch (Exception e) {
            }
        }

        return r;
    }
}
