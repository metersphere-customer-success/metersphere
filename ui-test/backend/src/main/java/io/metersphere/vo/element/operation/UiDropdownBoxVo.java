package io.metersphere.vo.element.operation;

import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class UiDropdownBoxVo extends UiCommandBaseVo {

    private String type = "DropdownBox";
    private String clazzName = UiDropdownBoxVo.class.getCanonicalName();
    //子选项
    private String subItemType;
    //内容
    private String subItem;

    private List<String> supportedSubType = new ArrayList<>() {{
        add("label");
        add("index");
        add("value");
    }};

    /**
     * 对应前端选择操作是或者否
     *
     * @param command
     * @return
     */
    @Override
    public String getCommand(MsUiCommand command) {
        return optContent;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        if (Objects.isNull(subItemType)) {
            return null;
        }
        switch (subItemType) {
            case "option":
                return "label=" + subItem;
            case "index":
                return "index=" + subItem;
            case "value":
                return "value=" + subItem;
            default:
                return "label=" + subItem;
        }
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        UiDropdownBoxVo vo = new UiDropdownBoxVo();
        String atomicCommand = sideCommand.getCommand();
        String originTarget = sideCommand.getTarget();
        String originValue = sideCommand.getValue();
        msUiCommand.setCommand(atomicCommand);
        if ("addSelection".equalsIgnoreCase(atomicCommand)) {
            //前后端都用 select 表示
            atomicCommand = "select";
        }
        vo.setOptContent(replaceNote(atomicCommand));
        vo.setLocatorProp(originTarget);
        if (StringUtils.isNotBlank(originValue) && originValue.contains("=")) {
            vo.setSubItemType(originValue.split("=")[0]);
            if (supportedSubType.contains(vo.getSubItemType())) {
                if (vo.getSubItemType().equalsIgnoreCase("label")) {
                    vo.setSubItemType("option");
                }
            }
            vo.setSubItem(originValue.split("=")[1]);
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiDropdownBoxVo vo = null;
        if (command.getVo() instanceof UiDropdownBoxVo) {
            vo = (UiDropdownBoxVo) command.getVo();
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
        if (StringUtils.isBlank(vo.getSubItemType())) {
            //第一优先级
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("subitem_type") + Translator.get("is_null")));
            return r;
        }
        if (StringUtils.isBlank(vo.getSubItem())) {
            //第一优先级
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("subitem") + Translator.get("is_null")));
        }
        return r;
    }
}
