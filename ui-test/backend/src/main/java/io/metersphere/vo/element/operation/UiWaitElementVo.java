package io.metersphere.vo.element.operation;


import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Data
public class UiWaitElementVo extends UiCommandBaseVo {

    private String type = "WaitElement";
    private String clazzName = UiWaitElementVo.class.getCanonicalName();
    //等待文本
    private String waitText;
    private Long waitTime;

    @Override
    public String getCommand(MsUiCommand command) {
        //前端回传的名称就是对应原子指令的名称
        return optContent;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        if (!"waitForText".equalsIgnoreCase(optContent)) {
            if (waitTime == null)
                return "3000";
            return waitTime.toString();
        }
        return Optional.ofNullable(waitText).orElse("");
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiWaitElementVo vo = new UiWaitElementVo();
        vo.setOptContent(replaceNote(sideCommand.getCommand()));
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        vo.setLocatorProp(originTarget);
        if ("waitForText".equalsIgnoreCase(vo.getOptContent())) {
            vo.setWaitText(originValue);
        } else {
            try {
                vo.setWaitTime(Long.parseLong(Optional.ofNullable(originValue).orElse("3000")));
            } catch (Exception e) {
                LogUtil.error(String.format("转换 originValue 失败!指令 ：%s", JSON.toJSONString(sideCommand)));
            }
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiWaitElementVo vo = null;
        if (command.getVo() instanceof UiWaitElementVo) {
            vo = (UiWaitElementVo) command.getVo();
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
