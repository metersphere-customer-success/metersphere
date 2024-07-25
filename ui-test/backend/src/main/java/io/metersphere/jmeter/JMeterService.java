package io.metersphere.jmeter;


import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.base.domain.TestPlanUiScenario;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.base.mapper.TestPlanUiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.JSON;
import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaConfig;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JvmInfoDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.UiJmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

@Service
public class JMeterService {
    public static final String BASE_URL = "http://%s:%d";
    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    private ExecThreadPoolExecutor execThreadPoolExecutor;

    @PostConstruct
    private void init() {
        String JMETER_HOME = getJmeterHome();

        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public String getJmeterHome() {
        String home = getClass().getResource("/").getPath() + "jmeter";
        try {
            File file = new File(home);
            if (file.exists()) {
                return home;
            } else {
                return jmeterProperties.getHome();
            }
        } catch (Exception e) {
            return jmeterProperties.getHome();
        }
    }

    /**
     * 添加调试监听
     *
     * @param testId
     * @param testPlan
     */
    private void addDebugListener(String testId, HashTree testPlan) {
        MsDebugListener resultCollector = new MsDebugListener();
        resultCollector.setName(testId);
        resultCollector.setProperty(TestElement.TEST_CLASS, MsDebugListener.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ViewResultsFullVisualizer"));
        resultCollector.setEnabled(true);

        // 添加DEBUG标示
        HashTree test = ArrayUtils.isNotEmpty(testPlan.getArray()) ? testPlan.getTree(testPlan.getArray()[0]) : null;
        if (test != null && ArrayUtils.isNotEmpty(test.getArray()) && test.getArray()[0] instanceof ThreadGroup) {
            ThreadGroup group = (ThreadGroup) test.getArray()[0];
            group.setProperty(BackendListenerConstants.MS_DEBUG.name(), true);
        }
        testPlan.add(testPlan.getArray()[0], resultCollector);
    }

    private void runLocal(UiJmeterRunRequestDTO request) {
        request.setKafkaConfig(KafkaConfig.getKafka());

        if (StringUtils.isNotEmpty(request.getTestPlanReportId())
                && !FixedCapacityUtils.containsKey(request.getTestPlanReportId())
                && StringUtils.equals(request.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            FixedCapacityUtils.put(request.getTestPlanReportId(), new StringBuffer());
        } else {
            // 报告日志记录
            FixedCapacityUtils.put(request.getReportId(), new StringBuffer());
        }
        LoggerUtil.debug("监听MessageCache.tasks当前容量：" + FixedCapacityUtils.size());
        if (request.isDebug() && !StringUtils.equalsAny(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加同步接收结果 Listener");
            JMeterBase.addBackendListener(request, request.getHashTree(), MsApiBackendListener.class.getCanonicalName());
        }

        if (MapUtils.isNotEmpty(request.getExtendedParameters())
                && request.getExtendedParameters().containsKey("SYN_RES")
                && (Boolean) request.getExtendedParameters().get("SYN_RES")) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加Debug Listener");
            addDebugListener(request.getReportId(), request.getHashTree());
        }

        if (request.isDebug()) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加Debug Listener");
            addDebugListener(request.getReportId(), request.getHashTree());
        } else {
            UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(request.getTestId());
            if (scenario == null) {
                TestPlanUiScenario testPlanUiScenario = testPlanUiScenarioMapper.selectByPrimaryKey(request.getTestId());
                scenario = uiScenarioMapper.selectByPrimaryKey(testPlanUiScenario.getUiScenarioId());
            }

            // 本地执行生成hashTree
            request.setHashTree(UiGenerateHashTreeUtil.generateHashTree(scenario, request, request.getUiExecutionQueueParam()));
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加同步接收结果 Listener");
            JMeterBase.addBackendListener(request, request.getHashTree(), MsApiBackendListener.class.getCanonicalName());
        }

        if (StringUtils.equals(request.getRunType(), RunModeConstants.PARALLEL.toString()) &&
                StringUtils.equals(request.getReportType(), RunModeConstants.INDEPENDENCE.toString())) {
            String poolId = request.getPoolId();
            Long time = System.currentTimeMillis();
            uiScenarioReportMapper.updateExecuteTime(poolId, time);
        }

        execThreadPoolExecutor.outApiThreadPoolExecutorLogger("资源：[" + request.getTestId() + "] 加入JMETER中开始执行: " + request.getReportId());
        UILocalRunner runner = new UILocalRunner(request.getHashTree());
        runner.run(request.getReportId());
    }

    public void run(UiJmeterRunRequestDTO request) {
        execThreadPoolExecutor.addTask(request);
    }

    public void addQueue(UiJmeterRunRequestDTO request) {
        this.runLocal(request);
    }

    public boolean getRunningQueue(String poolId, String reportId) {
        try {
            List<JvmInfoDTO> resources = UiGenerateHashTreeUtil.setPoolResource(poolId);
            if (CollectionUtils.isEmpty(resources)) {
                return false;
            }
            boolean isRunning = false;
            for (JvmInfoDTO testResource : resources) {
                String configuration = testResource.getTestResource().getConfiguration();
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                String nodeIp = node.getIp();
                Integer port = node.getPort();
                String uri = String.format(BASE_URL + "/jmeter/get/running/queue/" + reportId, nodeIp, port);
                ResponseEntity<Boolean> result = restTemplate.getForEntity(uri, Boolean.class);
                if (result != null && result.getBody()) {
                    isRunning = true;
                    break;
                }
            }
            return isRunning;
        } catch (Exception e) {
            return false;
        }
    }
}
