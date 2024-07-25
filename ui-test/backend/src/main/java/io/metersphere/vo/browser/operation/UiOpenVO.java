package io.metersphere.vo.browser.operation;

import io.metersphere.constants.CommandConstants;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.UiGlobalConfigUtil;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class UiOpenVO extends UiCommandBaseVo {

    private String type = "Open";
    private String clazzName = UiOpenVO.class.getCanonicalName();
    // 网页 URL 地址
    private String webUrl;
    //是否新窗口打开
    private boolean blank = false;
    @JsonIgnore
    public static String innerHandleName = "innerMSHandle";

    @Override
    public String getCommand(MsUiCommand command) {
        if (UiGlobalConfigUtil.getConfig().isOperating() && blank) {
            return CommandConstants.RUNSCRIPT;
        }
        return CommandConstants.OPEN;
    }

    @Override
    public String getTarget() {
        if (UiGlobalConfigUtil.getConfig().isOperating() && blank) {
            return "window.open('" + webUrl + "')";
        }
        return webUrl;
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
        msUiCommand.setCommand(CommandConstants.OPEN);
        UiOpenVO vo = new UiOpenVO();
        vo.setWebUrl(sideCommand.getTarget());
        //支持导入新打开窗口的指令
        if (sideCommand.isOpensWindow()) {
            vo.setBlank(true);
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    protected void afterCommonPropSet(MsUiCommand command, SideDTO.TestsDTO.CommandsDTO commandsDTO) {
        //如果有新窗口则打开新窗口 导出处理成 OpensWindow + selectWindow
        if (UiGlobalConfigUtil.getConfig().isOperating() && ((UiOpenVO) command.getVo()).blank) {
            commandsDTO.setOpensWindow(true);
            commandsDTO.setWindowHandleName(innerHandleName);
        }
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiOpenVO vo = null;
        if (command.getVo() instanceof UiOpenVO) {
            vo = (UiOpenVO) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        if (StringUtils.isBlank(vo.getWebUrl())) {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("url_is_null")));
        }
        return r;
    }

}