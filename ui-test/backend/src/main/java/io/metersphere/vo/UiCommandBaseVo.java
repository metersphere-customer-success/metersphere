package io.metersphere.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.LocateTypeEnum;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.dto.parser.AtomicRelationShip;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import io.metersphere.impl.CommonCommand;
import io.metersphere.vo.browser.operation.UiOpenVO;
import io.metersphere.vo.browser.operation.UiSelectFrameVO;
import io.metersphere.vo.browser.operation.UiSelectWindowVO;
import io.metersphere.vo.browser.operation.UiSetWindowSizeVO;
import io.metersphere.vo.dialog.operation.UiDialogVo;
import io.metersphere.vo.element.operation.UiDropdownBoxVo;
import io.metersphere.vo.element.operation.UiSetItemVo;
import io.metersphere.vo.element.operation.UiWaitElementVo;
import io.metersphere.vo.extraction.UiExtractElementVO;
import io.metersphere.vo.extraction.UiExtractWindowVO;
import io.metersphere.vo.independent.operation.UiScriptVO;
import io.metersphere.vo.input.operation.UiInputVO;
import io.metersphere.vo.mouse.operation.UiMouseClickVo;
import io.metersphere.vo.mouse.operation.UiMouseDragVo;
import io.metersphere.vo.mouse.operation.UiMouseMoveVo;
import io.metersphere.vo.program.controller.*;
import io.metersphere.vo.validation.*;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UiTimesVo.class, name = UiVoType.TIMES),
        @JsonSubTypes.Type(value = UiWhileVo.class, name = UiVoType.WHILE),
        @JsonSubTypes.Type(value = UiForEachVo.class, name = UiVoType.FOR_EACH),
        @JsonSubTypes.Type(value = UiIfVo.class, name = UiVoType.IF),
        @JsonSubTypes.Type(value = UiElseIfVo.class, name = UiVoType.ELSE_IF),
        @JsonSubTypes.Type(value = UiElseVo.class, name = UiVoType.ELSE),
        @JsonSubTypes.Type(value = UiDropdownBoxVo.class, name = UiVoType.DROPDOWNBOX),
        @JsonSubTypes.Type(value = UiSetItemVo.class, name = UiVoType.SETITEM),
        @JsonSubTypes.Type(value = UiWaitElementVo.class, name = UiVoType.WAITELEMENT),
        @JsonSubTypes.Type(value = UiInputVO.class, name = UiVoType.INPUT),
        @JsonSubTypes.Type(value = UiDialogVo.class, name = UiVoType.DIALOG),
        @JsonSubTypes.Type(value = UiMouseClickVo.class, name = UiVoType.MOUSE_CLICK),
        @JsonSubTypes.Type(value = UiMouseMoveVo.class, name = UiVoType.MOUSE_MOVE),
        @JsonSubTypes.Type(value = UiMouseDragVo.class, name = UiVoType.MOUSE_DRAG),
        @JsonSubTypes.Type(value = UiValidateValueVO.class, name = UiVoType.VALIDATEVALUE),
        @JsonSubTypes.Type(value = UiValidateTitleVO.class, name = UiVoType.VALIDATETITLE),
        @JsonSubTypes.Type(value = UiValidateElementVO.class, name = UiVoType.VALIDATEELEMENT),
        @JsonSubTypes.Type(value = UiValidateTextVO.class, name = UiVoType.VALIDATETEXT),
        @JsonSubTypes.Type(value = UiValidateDropdownVO.class, name = UiVoType.VALIDATEDROPDOWN),
        @JsonSubTypes.Type(value = UiOpenVO.class, name = UiVoType.OPEN),
        @JsonSubTypes.Type(value = UiSelectWindowVO.class, name = UiVoType.SELECTWINDOW),
        @JsonSubTypes.Type(value = UiSetWindowSizeVO.class, name = UiVoType.SETWINDOWSIZE),
        @JsonSubTypes.Type(value = UiSelectFrameVO.class, name = UiVoType.SELECTFRAME),
        @JsonSubTypes.Type(value = UiExtractWindowVO.class, name = UiVoType.EXTRACTWINDOW),
        @JsonSubTypes.Type(value = UiExtractElementVO.class, name = UiVoType.EXTRACTELEMENT),
        @JsonSubTypes.Type(value = UiScriptVO.class, name = UiVoType.SCRIPT),
        @JsonSubTypes.Type(value = UiAtomicCommandVO.class, name = UiVoType.ATOMIC),
})
public abstract class UiCommandBaseVo extends BaseLocator {
    //操作内容
    public String optContent;
    //失败终止
    private boolean failOver;
    // target 类型
    public String targetType = "";

    /**
     * 模板方法 将展示层 vo 转成 运行时 vo 供导出或者运行时使用
     *
     * @param command
     * @return
     */
    public SideDTO.TestsDTO.CommandsDTO toSideCommand(MsUiCommand command) {
        SideDTO.TestsDTO.CommandsDTO commandsDTO = new SideDTO.TestsDTO.CommandsDTO();
        commandsDTO.setId(command.getId());
        // 这里的 command 对应原子指令 在后端翻译的时候找到 对应的 {command}WebdriverScript.js 进行翻译
        commandsDTO.setCommand(getCommand(command));
        commandsDTO.setTarget(getTarget());
        commandsDTO.setValue(getValue());
        commandsDTO.setTargets(getTargets());
        commandsDTO.setEnable(command.isEnable());
        commandsDTO.setTargetType(targetType);
        commandsDTO.setCommandConfig(command.getCommandConfig());
        commandsDTO.setComment(command.getName());
        afterCommonPropSet(command, commandsDTO);
        return commandsDTO;
    }

    protected void afterCommonPropSet(MsUiCommand command, SideDTO.TestsDTO.CommandsDTO commandsDTO) {

    }

    public abstract String getCommand(MsUiCommand command);

    public String getTarget() {
        return formatLocateString();
    }

    public abstract List<?> getTargets();

    public abstract String getValue();

    public SideDTO.TestsDTO.CommandsDTO getEndCommand(MsUiCommand command) {
        SideDTO.TestsDTO.CommandsDTO commandsDTO = new SideDTO.TestsDTO.CommandsDTO();
        commandsDTO.setId(UUID.randomUUID().toString());
        commandsDTO.setCommand("end");
        commandsDTO.setEnable(command.isEnable());
        commandsDTO.setComment(command.getName());
        return commandsDTO;
    }

    /**
     * 模板方法 将运行时 vo 转成 展示层 vo 供导入时使用
     *
     * @param sideCommand
     * @return
     */
    public MsUiCommand toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand firstCommand = new MsUiCommand();
        MsUiCommand secondCommand = this._toMsUiCommand(sideCommand);
        BeanUtils.copyBean(firstCommand, secondCommand);
        String id = Optional.ofNullable(sideCommand.getId()).orElse(UUID.randomUUID().toString());
        String atomicCommand = sideCommand.getCommand();
        firstCommand.setEnable(sideCommand.isEnable());
        atomicCommand = atomicCommand.replace("//", "");
        String complexCommand = AtomicRelationShip.getComplexCommand(atomicCommand);
        if (StringUtils.equalsIgnoreCase(CommandType.COMMAND_TYPE_ATOM, complexCommand)) {
            //原子类型名称还是用自己的渲染A
            complexCommand = atomicCommand;
        }
        firstCommand.setName(StringUtils.defaultIfEmpty(sideCommand.getComment(), complexCommand));
        firstCommand.setCommand(complexCommand);
        firstCommand.setId(id);
        firstCommand.setResourceId(id);
        //默认 proxy，如果指令比较特殊 在自己实现里面去修改状态
        firstCommand.setCommandType(Optional.ofNullable(secondCommand.getCommandType()).orElse(CommandType.COMMAND_TYPE_PROXY));

        Map<String, CommonCommand> uiCommandMap = (Map<String, CommonCommand>) CommonBeanFactory.getBean("uiCommandMap");
        firstCommand.setViewType(uiCommandMap.get(atomicCommand).getViewType());

        return firstCommand;
    }

    /**
     * 每个派生类需要自己实现逆向的转换 供导入使用
     *
     * @param sideCommand
     * @return
     */
    protected abstract MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand);

    protected void setLocatorProp(String originSideTarget) {
        try {
            this.elementType = "elementLocator";
            //如果不是标准的格式 先统一认为是 xpath
            this.locateType = LocateTypeEnum.XPATH.getTypeName();
            this.viewLocator = originSideTarget;
            if (originSideTarget.contains("=")) {
                int index = originSideTarget.indexOf("=");
                String type = originSideTarget.split("=")[0];
                if (LocateTypeEnum.contains(type)) {
                    this.locateType = originSideTarget.split("=")[0];
                    this.viewLocator = originSideTarget.substring(index + 1);
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("转换 originSideTarget 出错！原始格式:%s", originSideTarget));
        }
    }

    protected MsUiCommand createPauseCommand() {
        MsUiCommand msUiCommand = new MsUiCommand();
        msUiCommand.setId(UUID.randomUUID().toString());
        msUiCommand.setResourceId(UUID.randomUUID().toString());
        msUiCommand.setCommand("pause");
        msUiCommand.setName("pause");
        msUiCommand.setViewType("programController");
        msUiCommand.setTarget("waitTime");
        return msUiCommand;
    }

    /**
     * 去掉导入时注释
     *
     * @param command
     * @return
     */
    protected String replaceNote(String command) {
        return command.replace("//", "");
    }

    /**
     * 判断导入的参数是不是对象
     *
     * @param param
     * @return
     */
    protected boolean isObject(String param) {
        try {
            JSON.parseObject(param);
        } catch (Exception e) {
            try {
                JSON.parseArray(param);
            } catch (Exception e1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 参数的详细校验
     *
     * @param command
     * @return
     */
    public UiValidateSingleResult validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();
        r.setResult(true);
        BeanUtils.copyBean(r, _validate(command));
        if (StringUtils.isBlank(r.getMsg())) {
            r.setResult(true);
        }
        r.setId(command.getId());
        r.setName(command.getName());
        r.setIndex(command.getIndex());

        return r;
    }

    public abstract UiValidateSingleResult _validate(MsUiCommand command);

    protected String wrapErrorMsg(MsUiCommand command, String errorMsg) {
        String name = StringUtils.equalsIgnoreCase(command.getCommand(), command.getName()) ? Translator.get(command.getCommand()) : command.getName();
        return Translator.get("command") + ": '" + name + "'" + errorMsg;
    }

    public UiValidateSingleResult getFailResult(MsUiCommand command, String errorMsg) {
        UiValidateSingleResult r = new UiValidateSingleResult();
        r.setId(command.getId());
        r.setIndex(command.getIndex());
        r.setResult(false);
        r.setMsg(wrapErrorMsg(command, errorMsg));
        return r;
    }
}
