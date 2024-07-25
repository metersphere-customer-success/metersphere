package io.metersphere.vo.mouse.operation;


import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.utils.UiGlobalConfigUtil;
import io.metersphere.vo.BaseLocator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.List;

@Data
public class UiMouseDragVo extends UiCommandBaseVo {
    private String type = "MouseDrag";
    private String clazzName = UiMouseDragVo.class.getCanonicalName();

    //鼠标起始定位
    private BaseLocator startLocator;
    //鼠标起始定位
    private BaseLocator endLocator;
    //拖拽方式 内部拖拽 true 允许内部拖拽
    private boolean innerDrag;
    //是否设置坐标 true 支持
    private boolean setCoord;

    @Override
    public String getCommand(MsUiCommand command) {
        //如果是导出
        if (UiGlobalConfigUtil.getConfig().isOperating()) {
            return "dragAndDropToObject";
        }
        if (innerDrag) {
            //元素内拖动 使用指令
            return "mouseDragAndDropAt";
        } else if (setCoord) {
            //元素坐标之间拖拽
            return "dragAndDropToObjectAt";
        }
        return "dragAndDropToObject";
    }

    @Override
    public String getTarget() {
        if (UiGlobalConfigUtil.getConfig().isOperating() || (!innerDrag && !setCoord)) {
            return startLocator.formatLocateString();
        } else {
            //除了ide原生拖拽命令 其他两个新增原子指令返回的都是 json 对象，取值方式不需要用双引号
            JSONObject targetJSON = new JSONObject();
            targetJSON.put("coords", startLocator.getCoords());
            if (innerDrag || setCoord) {
                targetJSON.put("locator", startLocator.formatLocateStringWithOutEscape());
                return JSON.toJSONString(targetJSON);
            } else {
                targetJSON.put("locator", startLocator.formatLocateString());
            }
            return JSON.toJSONString(targetJSON).replace("\"", "\\\"");
        }
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        if (UiGlobalConfigUtil.getConfig().isOperating()) {
            if (innerDrag) {
                return startLocator.formatLocateString();
            }
            return endLocator.formatLocateString();
        } else {
            if (setCoord) {
                JSONObject targetJSON = new JSONObject();
                targetJSON.put("locator", endLocator.formatLocateStringWithOutEscape());
                targetJSON.put("coords", endLocator.getCoords());
                return JSON.toJSONString(targetJSON);
            } else if (innerDrag) {
                //内部拖动,没有value
                return "";
            }
            return endLocator.formatLocateString();
        }
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiMouseDragVo vo = new UiMouseDragVo();
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        vo.setStartLocator(BaseLocator.parse(originTarget));
        vo.setEndLocator(BaseLocator.parse(originValue));
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();
        UiMouseDragVo vo = null;
        if (command.getVo() instanceof UiMouseDragVo) {
            vo = (UiMouseDragVo) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        String valS1 = vo.getStartLocator().validateLocateString();
        String valS2 = vo.getEndLocator().validateLocateString();
        if (!StringUtils.isAllBlank(valS1, valS2)) {
            return getFailResult(command, Translator.get("locator_is_null"));
        }
        return r;
    }
}
