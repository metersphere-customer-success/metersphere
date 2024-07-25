package io.metersphere.listener;

import com.github.pagehelper.PageHelper;
import com.mchange.lang.IntegerUtils;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.UiScenarioType;
import io.metersphere.service.BaseScheduleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UiAppStartListener implements ApplicationRunner {
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    private BaseScheduleService baseScheduleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtil.info("================= UI 应用启动 =================");
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(ParamConstants.BASE.GRID_CONCURRENCY.getValue());
        if (systemParameter != null && StringUtils.isNotEmpty(systemParameter.getParamValue())) {
            LogUtil.info("设置并发队列核心数: {}", systemParameter.getParamValue());
            CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).setCorePoolSize(IntegerUtils.parseInt(systemParameter.getParamValue(), 4));
        }
        this.makeReportIntermediateStatusToFinal();

        this.initDefaultNodeWithType();

        baseScheduleService.startEnableSchedules(ScheduleGroup.UI_SCENARIO_TEST);
    }

    private void initDefaultNodeWithType() {
        long count = projectMapper.countByExample(new ProjectExample());
        long pages = Double.valueOf(Math.ceil(count / 100.0)).longValue();

        for (int i = 1; i <= pages; i++) {
            PageHelper.startPage(i, 100, true);
            List<Project> projectList = projectMapper.selectByExample(new ProjectExample());
            projectList.forEach(project -> {
                initProjectDefaultNode(project.getId());
            });
        }
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
        if (moduleNodes.size() > 0) {
            return;
        }
        LogUtil.info("初始化项目配置, projectId: {}, projectName: {}", project.getId(), project.getName());
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
            record.setId(UUID.randomUUID().toString());
            record.setName("未规划模块");
            record.setScenarioType(UiScenarioType.CUSTOM_COMMAND.getType());
            extModuleNodeMapper.insertWithModulePathAndType(ProjectModuleDefaultNodeEnum.UI_SCENARIO_DEFAULT_NODE.getTableName(), record);
        }
        record.setId(UUID.randomUUID().toString());
        record.setName(ProjectModuleDefaultNodeEnum.UI_ELEMENT_DEFAULT_NODE.getNodeName());
        record.setModulePath("/" + record.getName());
        extModuleNodeMapper.insertWithModulePath(ProjectModuleDefaultNodeEnum.UI_ELEMENT_DEFAULT_NODE.getTableName(), record);
    }

    // 启动时把报告中间状态[RUNNING] -> [ERROR]
    private void makeReportIntermediateStatusToFinal() {
        UiScenarioReportWithBLOBs report = new UiScenarioReportWithBLOBs();
        report.setStatus(ReportStatus.ERROR.toString());
        UiScenarioReportExample example = new UiScenarioReportExample();
        example.createCriteria().andStatusEqualTo(ReportStatus.RUNNING.name());
        uiScenarioReportMapper.updateByExampleSelective(report, example);
    }

}
