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
public class UiSelectFrameVO extends UiCommandBaseVo {

    private String type = "SelectFrame";
    private String clazzName = UiSelectFrameVO.class.getCanonicalName();
    // frame 索引
    private Integer frameIndex;

    @Override
    public String getCommand(MsUiCommand command) {
        return "selectFrame";
    }

    @Override
    public String getTarget() {
        // 退出 frame(回到初始状态)
        if (StringUtils.equalsIgnoreCase("switchTheFrameToParent", optContent)) {
            return "relative=parent";
        }
        if (StringUtils.equalsIgnoreCase("switchTheFrameByIndex", optContent)) {
            return "index=" + frameIndex;
        }
        return formatLocateString();
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
    public MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiSelectFrameVO vo = new UiSelectFrameVO();
        if (StringUtils.isNotBlank(sideCommand.getTarget())) {
            if ("relative=parent".equalsIgnoreCase(sideCommand.getTarget())) {
                vo.setOptContent("switchTheFrameToParent");
            } else if (sideCommand.getTarget().startsWith("index=")) {
                vo.setOptContent("switchTheFrameByIndex");
                try {
                    vo.setFrameIndex(Integer.valueOf(sideCommand.getTarget().split("=")[1]));
                } catch (Exception e) {
                    LogUtil.error(String.format("导入 selectFrame 指令时转换 index 错误，原始 target 为 %s", sideCommand.getTarget()));
                }
            } else {
                vo.setOptContent("default");
                vo.setLocatorProp(sideCommand.getTarget());
            }
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiSelectFrameVO vo = null;
        if (command.getVo() instanceof UiSelectFrameVO) {
            vo = (UiSelectFrameVO) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        switch (vo.getOptContent()) {
            case "switchTheFrameByIndex":
                if (vo.getFrameIndex() == null) {
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