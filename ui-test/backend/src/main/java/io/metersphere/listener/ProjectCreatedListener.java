package io.metersphere.listener;

import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCaseNodeExample;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.constants.UiScenarioType;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProjectCreatedListener {

    public static final String CONSUME_ID = "ui-project-created";

    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    private ProjectMapper projectMapper;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_CREATED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        this.initProjectDefaultNode(projectId);
    }

    private void initProjectDefaultNode(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }

        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId).andNameEqualTo(ProjectModuleDefaultNodeEnum.UI_SCENARIO_DEFAULT_NODE.getNodeName());
        List<ModuleNode> moduleNodes = extModuleNodeMapper.selectByExample(ProjectModuleDefaultNodeEnum.UI_SCENARIO_DEFAULT_NODE.getTableName(), example);
        ModuleNode record = new ModuleNode();
        record.setId(UUID.randomUUID().toString());
        record.setCreateUser(project.getCreateUser());
        record.setPos(1.0);
        record.setLevel(1);
        record.setCreateTime(System.currentTimeMillis());
        record.setUpdateTime(System.currentTimeMillis());
        record.setProjectId(projectId);
        record.setName(ProjectModuleDefaultNodeEnum.UI_SCENARIO_DEFAULT_NODE.getNodeName());
        if (moduleNodes.size() == 0) {
            extModuleNodeMapper.insert(ProjectModuleDefaultNodeEnum.UI_SCENARIO_DEFAULT_NODE.getTableName(), record);
            // 初始化自定义指令
            ModuleNode customCommandModule = new ModuleNode();
            BeanUtils.copyBean(customCommandModule, record);
            customCommandModule.setId(UUID.randomUUID().toString());
            customCommandModule.setName("未规划模块");
            customCommandModule.setScenarioType(UiScenarioType.CUSTOM_COMMAND.getType());
            extModuleNodeMapper.insertWithModulePathAndType(ProjectModuleDefaultNodeEnum.UI_SCENARIO_DEFAULT_NODE.getTableName(), customCommandModule);
        }
        ModuleNode elementModule = new ModuleNode();
        BeanUtils.copyBean(elementModule, record);
        elementModule.setId(UUID.randomUUID().toString());
        elementModule.setName(ProjectModuleDefaultNodeEnum.UI_ELEMENT_DEFAULT_NODE.getNodeName());
        elementModule.setModulePath("/" + elementModule.getName());
        extModuleNodeMapper.insertWithModulePath(ProjectModuleDefaultNodeEnum.UI_ELEMENT_DEFAULT_NODE.getTableName(), elementModule);
    }
}
