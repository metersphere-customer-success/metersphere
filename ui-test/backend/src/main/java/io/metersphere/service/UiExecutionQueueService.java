package io.metersphere.service;


import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtUiExecutionQueueMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.TestPlanExecuteCaseType;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.dto.RunModeDataDTO;
import io.metersphere.dto.api.UiExecutionQueueParam;
import io.metersphere.jmeter.JMeterService;
import io.metersphere.jmeter.JMeterThreadUtils;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UiExecutionQueueService {
    @Resource
    private ApiExecutionQueueMapper baseExecutionQueueMapper;
    @Resource
    private ExtUiExecutionQueueMapper extUiExecutionQueueMapper;
    @Resource
    private ApiExecutionQueueDetailMapper baseExecutionQueueDetailMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UiScenarioReportService uiScenarioReportService;
    @Resource
    private UiScenarioSerialService uiScenarioSerialService;
    @Resource
    private ExtUiScenarioReportMapper extUiScenarioReportMapper;
    @Resource
    private RedisTemplateService redisTemplateService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DBTestQueue add(Object runObj, String poolId, String type, String reportId, String reportType, String runMode, RunModeConfigDTO config) {
        LoggerUtil.info("报告【" + reportId + "】开始生成执行链");
        if (config.getEnvMap() == null) {
            config.setEnvMap(new LinkedHashMap<>());
        }
        ApiExecutionQueue executionQueue = getUiExecutionQueue(poolId, reportId, reportType, runMode, config);
        baseExecutionQueueMapper.insert(executionQueue);
        DBTestQueue resQueue = new DBTestQueue();
        BeanUtils.copyBean(resQueue, executionQueue);

        Map<String, String> detailMap = new HashMap<>();
        List<ApiExecutionQueueDetail> queueDetails = new LinkedList<>();
        // 初始化API/用例队列
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name())) {
            Map<String, ApiDefinitionExecResult> runMap = (Map<String, ApiDefinitionExecResult>) runObj;
            initApi(runMap, resQueue, config, detailMap, queueDetails);
        }
        // 初始化性能测试执行链
        else if (StringUtils.equalsIgnoreCase(type, ApiRunMode.TEST_PLAN_PERFORMANCE_TEST.name())) {
            Map<String, String> requests = (Map<String, String>) runObj;
            initPerf(requests, resQueue, config, detailMap, queueDetails);
        }
        // 初始化场景/UI执行链
        else {
            Map<String, RunModeDataDTO> runMap = (Map<String, RunModeDataDTO>) runObj;
            initScenario(runMap, resQueue, config, type, detailMap, queueDetails);
        }
        if (CollectionUtils.isNotEmpty(queueDetails)) {
            extUiExecutionQueueMapper.sqlInsert(queueDetails);
        }
        //redis移除key
        redisTemplateService.unlock(reportId, TestPlanExecuteCaseType.UI_SCENARIO.name(), reportId);
        resQueue.setDetailMap(detailMap);
        LoggerUtil.info("报告【" + reportId + "】生成执行链结束");
        return resQueue;
    }

    private void initScenario(Map<String, RunModeDataDTO> runMap, DBTestQueue resQueue,
                              RunModeConfigDTO config, String type, Map<String, String> detailMap, List<ApiExecutionQueueDetail> queueDetails) {
        final int[] sort = {0};
        runMap.forEach((k, v) -> {
            String envMap = null;
            if (StringUtils.startsWith(type, "UI_")) {
                UiExecutionQueueParam param = new UiExecutionQueueParam();
                //注意：每次运行都要拷贝一份最外层传递进来的config
                RunModeConfigDTO newConfig = new RunModeConfigDTO();
                BeanUtils.copyBean(newConfig, config);
                if (MapUtils.isEmpty(config.getEnvMap())) {
                    newConfig.setEnvMap(v.getPlanEnvMap());
                } else {
                    Map<String, String> thisProjectEnvMap = new HashMap<>();
                    if (StringUtils.isNotBlank(config.getEnvMap().get(v.getUiScenario().getProjectId()))) {
                        thisProjectEnvMap.put(v.getUiScenario().getProjectId(), config.getEnvMap().get(v.getUiScenario().getProjectId()));
                    }
                }
                BeanUtils.copyBean(param, newConfig);
                envMap = JSON.toJSONString(param);
            }
            ApiExecutionQueueDetail queue = detail(k, v.getTestId(), config.getMode(), sort[0], resQueue.getId(), envMap);
            queue.setSort(sort[0]);
            if (sort[0] == 0) {
                resQueue.setDetail(queue);
            }
            sort[0]++;
            queue.setRetryEnable(config.isRetryEnable());
            queue.setRetryNumber(config.getRetryNum());
            List<String> projectIds = new ArrayList<>();
            // 获取当前所属项目ID用于后续区分资源隔离
            if (MapUtils.isNotEmpty(v.getPlanEnvMap())) {
                List<String> kyList = v.getPlanEnvMap().keySet()
                        .stream()
                        .collect(Collectors.toList());
                projectIds.addAll(kyList);
            } else {
                projectIds.add(v.getReport().getProjectId());
            }
            queue.setProjectIds(JSON.toJSONString(projectIds));
            queueDetails.add(queue);
            detailMap.put(k, queue.getId());
        });
    }

    protected ApiExecutionQueue getUiExecutionQueue(String poolId, String reportId, String reportType, String runMode, RunModeConfigDTO config) {
        ApiExecutionQueue executionQueue = new ApiExecutionQueue();
        executionQueue.setId(UUID.randomUUID().toString());
        executionQueue.setCreateTime(System.currentTimeMillis());
        executionQueue.setPoolId(poolId);
        executionQueue.setFailure(config.isOnSampleError());
        executionQueue.setReportId(reportId);
        executionQueue.setReportType(StringUtils.isNotEmpty(reportType) ? reportType : RunModeConstants.INDEPENDENCE.toString());
        executionQueue.setRunMode(runMode);
        executionQueue.setUiDriver(config.getDriverConfig());
        return executionQueue;
    }

    private void initApi(Map<String, ApiDefinitionExecResult> runMap,
                         DBTestQueue resQueue, RunModeConfigDTO config, Map<String, String> detailMap, List<ApiExecutionQueueDetail> queueDetails) {
        int sort = 0;
        String envStr = JSON.toJSONString(config.getEnvMap());
        for (String k : runMap.keySet()) {
            ApiExecutionQueueDetail queue = detail(runMap.get(k).getId(), k, config.getMode(), sort++, resQueue.getId(), envStr);
            if (sort == 1) {
                resQueue.setDetail(queue);
            }
            queue.setRetryEnable(config.isRetryEnable());
            queue.setRetryNumber(config.getRetryNum());
            queue.setProjectIds(JSON.toJSONString(new ArrayList<>() {{
                this.add(runMap.get(k).getProjectId());
            }}));
            queueDetails.add(queue);
            detailMap.put(k, queue.getId());
        }
        resQueue.setDetailMap(detailMap);
    }

    protected ApiExecutionQueueDetail detail(String reportId, String testId, String type, int sort, String queueId, String envMap) {
        ApiExecutionQueueDetail queue = new ApiExecutionQueueDetail();
        queue.setCreateTime(System.currentTimeMillis());
        queue.setId(UUID.randomUUID().toString());
        queue.setEvnMap(envMap);
        queue.setReportId(reportId);
        queue.setTestId(testId);
        queue.setType(type);
        queue.setSort(sort);
        queue.setQueueId(queueId);
        return queue;
    }


    private void initPerf(Map<String, String> requests,
                          DBTestQueue resQueue, RunModeConfigDTO config, Map<String, String> detailMap, List<ApiExecutionQueueDetail> queueDetails) {
        String envStr = JSON.toJSONString(config.getEnvMap());
        int i = 0;
        for (String testId : requests.keySet()) {
            ApiExecutionQueueDetail queue = detail(requests.get(testId), testId, config.getMode(), i++, resQueue.getId(), envStr);
            if (i == 1) {
                resQueue.setDetail(queue);
            }
            queue.setRetryEnable(config.isRetryEnable());
            queue.setRetryNumber(config.getRetryNum());
            queueDetails.add(queue);
            detailMap.put(testId, queue.getId());
        }
    }

    public void defendQueue() {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        // 计算一小时前的超时报告
        final long timeout = System.currentTimeMillis() - (60 * MINUTE_MILLIS);
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andCreateTimeLessThan(timeout).andTypeNotEqualTo("loadTest");
        List<ApiExecutionQueueDetail> queueDetails = baseExecutionQueueDetailMapper.selectByExample(example);

        for (ApiExecutionQueueDetail item : queueDetails) {
            ApiExecutionQueue queue = baseExecutionQueueMapper.selectByPrimaryKey(item.getQueueId());
            if (queue == null) {
                baseExecutionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                continue;
            }
            // 在资源池中执行
            if (StringUtils.isNotEmpty(queue.getPoolId())
                    && jMeterService.getRunningQueue(queue.getPoolId(), item.getReportId())) {
                continue;
            }
            // 检查执行报告是否还在等待队列中或执行线程中
            if (JMeterThreadUtils.isRunning(item.getReportId(), item.getTestId())) {
                continue;
            }
            // 检查是否已经超时
            ResultDTO dto = new ResultDTO();
            dto.setQueueId(item.getQueueId());
            dto.setTestId(item.getTestId());
            if (StringUtils.isNotBlank(queue.getRunMode()) && !queue.getRunMode().startsWith(SystemConstants.TestTypeEnum.UI.name())) {
                continue;
            }
            UiScenarioReportWithBLOBs report = uiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
            // 报告已经被删除则队列也删除
            if (report == null) {
                baseExecutionQueueDetailMapper.deleteByPrimaryKey(item.getId());
            }
            // 这里只处理已经开始执行的队列如果 报告状态是 Waiting 表示还没开始暂不处理
            if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(), TestPlanReportStatus.RUNNING.name())
                    && report.getUpdateTime() < timeout) {
                report.setStatus(ReportStatus.TIMEOUT.name());
                uiScenarioReportMapper.updateByPrimaryKeySelective(report);

                LoggerUtil.info("超时处理报告：" + report.getId());
                if (queue != null && StringUtils.equalsIgnoreCase(item.getType(), RunModeConstants.SERIAL.toString())) {
                    // 删除串行资源锁
                    redisTemplate.delete(RunModeConstants.SERIAL.name() + "_" + dto.getReportId());

                    LoggerUtil.info("超时处理报告：【" + report.getId() + "】进入下一个执行");
                    dto.setTestPlanReportId(queue.getReportId());
                    dto.setReportId(queue.getReportId());
                    dto.setRunMode(queue.getRunMode());
                    dto.setRunType(item.getType());
                    dto.setReportType(queue.getReportType());
                    queueNext(dto);
                } else {
                    baseExecutionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                }
            }
        }
        // 集成报告超时处理
        ApiExecutionQueueExample queueDetailExample = new ApiExecutionQueueExample();
        queueDetailExample.createCriteria().andReportTypeEqualTo(RunModeConstants.SET_REPORT.toString()).andCreateTimeLessThan(timeout);
        List<ApiExecutionQueue> executionQueues = baseExecutionQueueMapper.selectByExample(queueDetailExample);
        if (CollectionUtils.isNotEmpty(executionQueues)) {
            executionQueues.forEach(item -> {
                UiScenarioReportWithBLOBs report = uiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(), TestPlanReportStatus.RUNNING.name(), ReportStatus.PENDING.name())
                        && (report.getUpdateTime() < timeout)) {
                    report.setStatus(ReportStatus.TIMEOUT.name());
                    uiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
            });
        }
        // 处理测试计划报告
        List<ApiExecutionQueue> queues = extUiExecutionQueueMapper.findTestPlanReportQueue();
        if (CollectionUtils.isNotEmpty(queues)) {
            queues.forEach(item -> {
                // 更新测试计划报告
                if (StringUtils.isNotEmpty(item.getReportId())) {
                    LoggerUtil.info("Handling test plan reports that are not in the execution queue：【" + item.getReportId() + "】");
                    checkTestPlanUiCaseTestEnd(null,null,item.getReportId());
                }
            });
        }

        List<String> testPlanReports = extUiExecutionQueueMapper.findTestPlanRunningReport();
        if (CollectionUtils.isNotEmpty(testPlanReports)) {
            testPlanReports.forEach(reportId -> {
                LoggerUtil.info("Compensation Test Plan Report：【" + reportId + "】");
                checkTestPlanUiCaseTestEnd(null,null,reportId);
            });
        }
        // 清除异常队列/一般是服务突然停止产生
        extUiExecutionQueueMapper.delete();
    }

    public void queueNext(ResultDTO dto) {
        LoggerUtil.info("开始处理队列：" + dto.getReportId() + ", QID：" + dto.getQueueId());
        if (StringUtils.equals(dto.getRunType(), RunModeConstants.PARALLEL.toString())) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId()).andTestIdEqualTo(dto.getTestId());
            baseExecutionQueueDetailMapper.deleteByExample(example);
            // 检查队列是否已空
            ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
            queueDetailExample.createCriteria().andQueueIdEqualTo(dto.getQueueId());
            long count = baseExecutionQueueDetailMapper.countByExample(queueDetailExample);
            if (count == 0) {
                baseExecutionQueueMapper.deleteByPrimaryKey(dto.getQueueId());
                if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    String reportId = dto.getTestPlanReportId();
                    uiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
                }
            }
            return;
        }
        // 获取串行下一个执行节点
        DBTestQueue executionQueue = this.handleQueue(dto.getQueueId(), dto.getTestId());
        if (executionQueue != null) {
            // 串行失败停止
            if (BooleanUtils.isTrue(executionQueue.getFailure()) && StringUtils.isNotEmpty(executionQueue.getCompletedReportId())) {
                boolean isNext = failure(executionQueue, dto);
                if (!isNext) {
                    return;
                }
            }
            LoggerUtil.info("开始处理执行队列：" + executionQueue.getId() + " 当前资源是：" + dto.getTestId() + "报告ID：" + dto.getReportId());
            if (executionQueue.getDetail() != null && StringUtils.isNotEmpty(executionQueue.getDetail().getTestId())) {
                if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
                    LoggerUtil.info("当前执行队列是：" + JSON.toJSONString(executionQueue.getDetail()));
                    // 防止重复执行
                    boolean isNext = redisTemplate.opsForValue().setIfAbsent(RunModeConstants.SERIAL.name() + "_" + executionQueue.getDetail().getReportId(), executionQueue.getDetail().getQueueId());
                    if (!isNext) {
                        return;
                    }
                    redisTemplate.expire(RunModeConstants.SERIAL.name() + "_" + executionQueue.getDetail().getReportId(), 60, TimeUnit.MINUTES);
                    if (StringUtils.startsWith(executionQueue.getRunMode(), "UI")) {
                        uiScenarioSerialService.serial(executionQueue, executionQueue.getDetail());
                    }
                }
            } else {
                if (StringUtils.equalsIgnoreCase(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    String reportId = dto.getTestPlanReportId();
                    uiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
                }
                baseExecutionQueueMapper.deleteByPrimaryKey(dto.getQueueId());
                LoggerUtil.info("Queue execution ends：" + dto.getQueueId());
            }

            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId()).andTestIdEqualTo(dto.getTestId());
            baseExecutionQueueDetailMapper.deleteByExample(example);
        }
        LoggerUtil.info("处理队列结束：" + dto.getReportId() + "QID：" + dto.getQueueId());
    }

    public DBTestQueue handleQueue(String id, String testId) {
        ApiExecutionQueue executionQueue = baseExecutionQueueMapper.selectByPrimaryKey(id);
        DBTestQueue queue = new DBTestQueue();
        if (executionQueue != null) {
            BeanUtils.copyBean(queue, executionQueue);
            LoggerUtil.info("Get the next execution point：【" + id + "】");

            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.setOrderByClause("sort asc");
            example.createCriteria().andQueueIdEqualTo(id);
            List<ApiExecutionQueueDetail> queues = baseExecutionQueueDetailMapper.selectByExampleWithBLOBs(example);

            if (CollectionUtils.isNotEmpty(queues)) {
                // 处理掉当前已经执行完成的资源
                List<ApiExecutionQueueDetail> completedQueues = queues.stream().filter(item -> StringUtils.equals(item.getTestId(), testId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(completedQueues)) {
                    ApiExecutionQueueDetail completed = completedQueues.get(0);
                    queue.setCompletedReportId(completed.getReportId());
                    baseExecutionQueueDetailMapper.deleteByPrimaryKey(completed.getId());
                    queues.remove(completed);
                }
                // 取出下一个要执行的节点
                if (CollectionUtils.isNotEmpty(queues)) {
                    queue.setDetail(queues.get(0));
                } else {
                    LoggerUtil.info("execution complete,clear queue：【" + id + "】");
                    baseExecutionQueueMapper.deleteByPrimaryKey(id);
                }
            } else {
                LoggerUtil.info("execution complete,clear queue：【" + id + "】");
                baseExecutionQueueMapper.deleteByPrimaryKey(id);
            }
        } else {
            LoggerUtil.info("The queue was accidentally deleted：【" + id + "】");
        }
        return queue;
    }

    private boolean failure(DBTestQueue executionQueue, ResultDTO dto) {
        LoggerUtil.info("进入失败停止处理：" + executionQueue.getId());
        boolean isError = UiScenarioReportService.getUiErrorSize(dto) > 0;

        if (isError) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId());
            this.checkTestPlanUiCaseTestEnd(dto.getTestId(),dto.getRunMode(),dto.getTestPlanReportId());
            // 更新未执行的报告状态
            List<ApiExecutionQueueDetail> details = baseExecutionQueueDetailMapper.selectByExample(example);
            List<String> reportIds = details.stream().map(ApiExecutionQueueDetail::getReportId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(reportIds)) {
                extUiScenarioReportMapper.update(reportIds);
            }
            // 清除队列
            baseExecutionQueueDetailMapper.deleteByExample(example);
            baseExecutionQueueMapper.deleteByPrimaryKey(executionQueue.getId());

            if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                String reportId = dto.getReportId();
                if (StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())) {
                    reportId = dto.getTestPlanReportId();
                }
                uiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
            }
            return false;
        }
        return true;
    }

    public void checkTestPlanUiCaseTestEnd(String testId, String runMode, String testPlanReportId) {
        if (StringUtils.isEmpty(testPlanReportId)) {
            //不是整体测试计划执行的用例，发送testID给测试跟踪模块，用于做单接口执行后续操作处理
            this.testPlanUiCaseTestEnd(testId, runMode);
        } else {
            // 由测试计划检查测试计划中其他队列是否结束
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testPlanReportId);
        }
    }

    private void testPlanUiCaseTestEnd(String testId, String runMode) {
        //不是整体测试计划执行的用例，发送testID给测试跟踪模块，用于做单接口执行后续操作处理
        if (StringUtils.isNotEmpty(testId) && StringUtils.equalsAnyIgnoreCase(runMode,
                ApiRunMode.UI_SCENARIO_PLAN.name(), ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name(),
                ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name())) {
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testId, UUID.randomUUID().toString());
        }
    }

    public void stop(List<String> reportIds) {
        if (CollectionUtils.isEmpty(reportIds)) {
            return;
        }
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andReportIdIn(reportIds);
        List<ApiExecutionQueueDetail> details = baseExecutionQueueDetailMapper.selectByExample(example);

        List<String> queueIds = new ArrayList<>();
        details.forEach(item -> {
            if (!queueIds.contains(item.getQueueId())) {
                queueIds.add(item.getQueueId());
            }
        });
        baseExecutionQueueDetailMapper.deleteByExample(example);

        for (String queueId : queueIds) {
            ApiExecutionQueue queue = baseExecutionQueueMapper.selectByPrimaryKey(queueId);
            // 更新测试计划报告
            if (queue != null && StringUtils.isNotEmpty(queue.getReportId())) {
                checkTestPlanUiCaseTestEnd(null,null,queue.getReportId());
                baseExecutionQueueMapper.deleteByPrimaryKey(queueId);
            }
        }
    }

}
