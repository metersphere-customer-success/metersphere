package io.metersphere.listener;

import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RunInterface;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.JarConfigService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.PluginService;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private JarConfigService jarConfigService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private IssuesService issuesService;
    @Resource
    private ProjectService projectService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private PluginService pluginService;
    @Resource
    private TestCaseService testCaseService;

    @Value("${jmeter.home}")
    private String jmeterHome;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        System.out.println("================= 应用启动 =================");

        System.setProperty("jmeter.home", jmeterHome);

        loadJars();

        initPythonEnv();

        try {
            //检查状态为开启的TCP-Mock服务端口
            projectService.initMockTcpService();
        }catch (Exception e){
            e.printStackTrace();
        }


        initOperate(apiAutomationService::checkApiScenarioUseUrl, "init.scenario.url");
        initOperate(apiAutomationService::checkApiScenarioReferenceId, "init.scenario.referenceId");
        initOperate(apiAutomationService::initExecuteTimes, "init.scenario.executeTimes");
        initOperate(issuesService::syncThirdPartyIssues, "init.issue");
        initOperate(issuesService::issuesCount, "init.issueCount");
        initOperate(performanceTestService::initScenarioLoadTest, "init.scenario.load.test");
        initOperate(testCaseService::initOrderField, "init.sort.test.case");
        pluginService.loadPlugins();
        try {
            Thread.sleep(1 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduleService.startEnableSchedules();

    }


    /**
     * 处理初始化数据、兼容数据
     * 只在第一次升级的时候执行一次
     * @param initFuc
     * @param key
     */
    private void initOperate(RunInterface initFuc, final String key) {
        try {
            String value = systemParameterService.getValue(key);
            if (StringUtils.isBlank(value)) {
                initFuc.run();
                systemParameterService.saveInitParam(key);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 解决接口测试-无法导入内置python包
     */
    private void initPythonEnv() {
        //解决无法加载 PyScriptEngineFactory
        Options.importSite = false;
        try {
            PythonInterpreter interp = new PythonInterpreter();
            String path = jMeterService.getJmeterHome();
            System.out.println("sys.path: " + path);
            path += "/lib/ext/jython-standalone.jar/Lib";
            interp.exec("import sys");
            interp.exec("sys.path.append(\"" + path + "\")");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 加载jar包
     */
    private void loadJars() {
        List<JarConfig> jars = jarConfigService.list();

        jars.forEach(jarConfig -> {
            try {
                NewDriverManager.loadJar(jarConfig.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
        });

    }
}
