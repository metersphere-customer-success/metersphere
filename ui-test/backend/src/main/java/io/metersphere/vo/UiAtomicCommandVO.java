package io.metersphere.vo;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.ArgTypeEnum;
import io.metersphere.constants.UiVoType;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommonCommand;
import io.metersphere.utils.ArgTypeParamProcessor;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 给原子指令使用的接收 vo
 */
@Data
public class UiAtomicCommandVO extends UiCommandBaseVo {

    private String type = UiVoType.ATOMIC;
    private String clazzName = UiAtomicCommandVO.class.getCanonicalName();
    /**
     * 给 String 文本类型的指令使用
     */
    private Object text;

    @Override
    public String getCommand(MsUiCommand command) {
        return null;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    /**
     * 原子类型的指令导入处理
     *
     * @param sideCommand
     * @return
     */
    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        msUiCommand.setTarget(sideCommand.getTarget());
        msUiCommand.setValue(sideCommand.getValue());
        Map<String, CommonCommand> uiCommandMap = (Map<String, CommonCommand>) CommonBeanFactory.getBean("uiCommandMap");
        CommonCommand cmd = uiCommandMap.get(replaceNote(sideCommand.getCommand()));
        if (cmd != null) {
            if (StringUtils.isNotBlank(cmd.getTarget())) {
                ArgTypeParamProcessor.process(ArgTypeEnum.valueOf(cmd.getTarget()), cmd.getName(), msUiCommand, "target");
            }
            if (StringUtils.isNotBlank(cmd.getValue())) {
                ArgTypeParamProcessor.process(ArgTypeEnum.valueOf(cmd.getValue()), cmd.getName(), msUiCommand, "value");
            }
        }
        msUiCommand.setCommandType(CommandType.COMMAND_TYPE_ATOM);
        msUiCommand.setViewType(cmd.getViewType());
        msUiCommand.setId(sideCommand.getId());
        msUiCommand.setResourceId(sideCommand.getId());
        msUiCommand.setName(Optional.ofNullable(sideCommand.getDescription()).orElse(replaceNote(sideCommand.getCommand())));
        msUiCommand.setDescription(sideCommand.getDescription());
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiAtomicCommandVO vo = null;
        if (command.getVo() instanceof UiAtomicCommandVO) {
            vo = (UiAtomicCommandVO) command.getVo();
        } else {
            return getFailResult(command, Translator.get("param_error"));
        }
        return r;
    }
}
