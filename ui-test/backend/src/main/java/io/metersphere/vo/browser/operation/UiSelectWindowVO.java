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
public class UiSelectWindowVO extends UiCommandBaseVo {

    private String type = "SelectWindow";
    private String clazzName = UiSelectWindowVO.class.getCanonicalName();
    // 网页句柄 ID
    private String handleId;
    // 网页名称
    private String handleName;
    // 网页索引
    private Integer handleIndex;

    @Override
    public String getCommand(MsUiCommand command) {
        return "selectWindow";
    }

    @Override
    public String getTarget() {
        // 根据句柄 ID 切换
        if (StringUtils.equalsIgnoreCase("switchTheWindowById", optContent)) {
            return "handle=" + handleId;
        } else if (StringUtils.equalsIgnoreCase("switchTheWindowByName", optContent)) {
            return "name=" + handleName;
        } else if (StringUtils.equalsIgnoreCase("switchTheWindowByIndex", optContent)) {
            return "win_ser_" + handleIndex;
        } else if (StringUtils.equalsIgnoreCase("switchTheWindowToStart", optContent)) {
            return "win_ser_local";
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
        String originTarget = sideCommand.getTarget();
        UiSelectWindowVO vo = new UiSelectWindowVO();
        if (StringUtils.isNotBlank(originTarget)) {
            if (originTarget.startsWith("handle=")) {
                vo.setOptContent("switchTheWindowById");
                try {
                    vo.setHandleId(originTarget.split("handle=")[1]);

                    //先从MS导出再导入，如果遇到内置的窗口切换不进行处理，因为MS内部执行时已经处理过
                    if (StringUtils.contains(vo.getHandleId(), UiOpenVO.innerHandleName)) {
                        msUiCommand.setEnable(false);
                    }
                } catch (Exception e) {
                    LogUtil.error("转换 switchTheWindowById 出错");
                }
            }
            if (originTarget.startsWith("name=")) {
                vo.setOptContent("switchTheWindowByName");
                try {
                    vo.setHandleName(originTarget.split("name=")[1]);
                } catch (Exception e) {
                    LogUtil.error("转换 switchTheWindowByName 出错");
                }
            }
            if (originTarget.startsWith("win_ser_")) {
                vo.setOptContent("switchTheWindowByIndex");
                try {
                    vo.setHandleIndex(Integer.valueOf(originTarget.split("win_ser_")[1]));
                } catch (Exception e) {
                    LogUtil.error("转换 switchTheWindowByIndex 出错，默认赋值为 0");
                    vo.setHandleIndex(0);
                }
            }
            if (originTarget.startsWith("win_ser_local")) {
                vo.setOptContent("switchTheWindowToStart");
            }
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiSelectWindowVO vo = null;
        if (command.getVo() instanceof UiSelectWindowVO) {
            vo = (UiSelectWindowVO) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        switch (vo.getOptContent()) {
            case "switchTheWindowByIndex":
                if (vo.getHandleIndex() == null) {
                    r.setResult(false);
                    r.setMsg(wrapErrorMsg(command, Translator.get("frame_index_is_null")));
                }
                break;
            case "default":
                if (StringUtils.isNotBlank(vo.validateLocateString())) {
                    r.setResult(false);
                    r.setMsg(wrapErrorMsg(command, vo.validateLocateString()));
                }
                break;
            default:
                break;
        }
        return r;
    }

}