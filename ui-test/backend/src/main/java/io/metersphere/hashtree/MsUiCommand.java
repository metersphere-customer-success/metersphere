package io.metersphere.hashtree;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.metersphere.dto.SideDTO;
import io.metersphere.impl.CommandConfig;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.TemplateUtils;
import io.metersphere.vo.UiAtomicCommandVO;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.jorphan.collections.HashTree;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@json_id"
)
public class MsUiCommand extends MsTestElement {
    @JsonProperty
    private String type = "MsUiCommand";
    private String clazzName = MsUiCommand.class.getCanonicalName();

    //side 文件定义字段
    @JsonProperty
    private String id;
    @JsonProperty
    private String comment;
    @JsonProperty
    private String command;
    @JsonProperty
    private String target;
    @JsonProperty
    private List<?> targets;
    @JsonProperty
    private String value;
    @JsonProperty
    private String description;

    //MS 前端解析的结构 atom 类型解析结构
    @JsonProperty
    private UiAtomicCommandVO targetVO;

    @JsonProperty
    private UiAtomicCommandVO valueVO;

    /**
     * MS 前端解析的结构 proxy 与 combine
     */
    @JsonProperty
    private UiCommandBaseVo vo;

    //引用类型
    @JsonProperty
    private String referenced;

    //唯一确定一个步骤
    @JsonProperty
    private String resourceId;

    //每一个指令的配置
    @JsonProperty
    private CommandConfig commandConfig = new CommandConfig();

    // 前后置的指令 导出时替换成 pause 指令
    private List<MsUiCommand> preCommands;
    private List<MsUiCommand> postCommands;

    // 类如 while 的间隔时间，追加到 while 内部的指令集合中
    private List<MsUiCommand> appendCommands;

    private String startCommand;
    private String endCommand;
    private String commandType;
    private String viewType;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
    }

    public SideDTO.TestsDTO.CommandsDTO toSideCommand() {
        SideDTO.TestsDTO.CommandsDTO commandsDTO = new SideDTO.TestsDTO.CommandsDTO();
        commandsDTO.setId(id);
        commandsDTO.setCommand(command);
        commandsDTO.setComment(comment);
        commandsDTO.setDescription(description);
        commandsDTO.setTarget(TemplateUtils.escapeQuotes(target));
        commandsDTO.setValue(TemplateUtils.escapeQuotes(value));
        commandsDTO.setTargets(targets);
        commandsDTO.setCommandConfig(commandConfig);
        commandsDTO.setEnable(isEnable());
        return commandsDTO;
    }
}
