package io.metersphere.service.task;


import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.base.mapper.ext.BaseTaskMapper;
import io.metersphere.base.mapper.ext.ExtUiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.ReportStatus;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.automation.TaskRequest;
import io.metersphere.jmeter.JMeterService;
import io.metersphere.jmeter.JMeterThreadUtils;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.UiExecutionQueueService;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiTaskService {
    @Resource
    private BaseTaskMapper extTaskMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourceMapper testResourceMapper;
    @Resource
    private ExtUiDefinitionExecResultMapper extUiDefinitionExecResultMapper;
    @Resource
    private ExecThreadPoolExecutor execThreadPoolExecutor;
    @Resource
    private CheckPermissionService checkPermissionService;
    @Resource
    private UiExecutionQueueService uiExecutionQueueService;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Resource
    private ExtUiScenarioReportMapper extUiScenarioReportMapper;

    public List<String> getOwnerProjectIds(String userId) {
        Set<String> userRelatedProjectIds = null;
        if (StringUtils.isEmpty(userId)) {
            userRelatedProjectIds = checkPermissionService.getUserRelatedProjectIds();
        } else {
            userRelatedProjectIds = checkPermissionService.getOwnerByUserId(userId);
        }
        if (CollectionUtils.isEmpty(userRelatedProjectIds)) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(userRelatedProjectIds);
    }

    public List<TaskCenterDTO> getCases(String id) {
        return extTaskMapper.getCases(id);
    }

    public List<TaskCenterDTO> getScenario(String id) {
        return extTaskMapper.getScenario(id);
    }


    public void send(Map<String, List<String>> poolMap) {
        try {
            LoggerUtil.info("结束所有NODE中执行的资源");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("STOP-NODE");
                    for (String poolId : poolMap.keySet()) {
                        TestResourcePoolExample example = new TestResourcePoolExample();
                        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andIdEqualTo(poolId);
                        List<TestResourcePool> pools = testResourcePoolMapper.selectByExample(example);
                        if (CollectionUtils.isNotEmpty(pools)) {
                            List<String> poolIds = pools.stream().map(pool -> pool.getId()).collect(Collectors.toList());
                            TestResourceExample resourceExample = new TestResourceExample();
                            resourceExample.createCriteria().andTestResourcePoolIdIn(poolIds);
                            List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(resourceExample);
                            for (TestResource testResource : testResources) {
                                String configuration = testResource.getConfiguration();
                                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                                String nodeIp = node.getIp();
                                Integer port = node.getPort();
                                String uri = String.format(JMeterService.BASE_URL + "/jmeter/stop", nodeIp, port);
                                restTemplate.postForEntity(uri, poolMap.get(poolId), void.class);
                            }
                        }
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }

    public String stop(List<TaskRequest> taskRequests) {
        if (CollectionUtils.isNotEmpty(taskRequests)) {
            List<TaskRequest> stopTasks = taskRequests.stream().filter(s -> StringUtils.isNotEmpty(s.getReportId())).collect(Collectors.toList());
            // 聚类，同一批资源池的一批发送
            Map<String, List<String>> poolMap = new HashMap<>();
            // 单条停止
            if (CollectionUtils.isNotEmpty(stopTasks) && stopTasks.size() == 1) {
                // 从队列移除
                TaskRequest request = stopTasks.get(0);
                execThreadPoolExecutor.removeQueue(request.getReportId());
                uiExecutionQueueService.stop(Arrays.asList(request.getReportId()));
                PoolExecBlockingQueueUtil.offer(request.getReportId());
                if (StringUtils.equals(request.getType(), "UI_SCENARIO")) {
                    UiScenarioReportWithBLOBs report = uiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                    if (report != null) {
                        report.setStatus(ReportStatus.STOPPED.name());
                        uiScenarioReportMapper.updateByPrimaryKeySelective(report);
                        extracted(poolMap, request.getReportId(), report.getActuator());
                    }
                }

            } else {
                try {
                    LoggerUtil.info("进入批量停止方法");
                    // 全部停止
                    Map<String, TaskRequest> taskRequestMap = taskRequests.stream().collect(Collectors.toMap(TaskRequest::getType, taskRequest -> taskRequest));
                    // 获取工作空间项目
                    LoggerUtil.info("获取工作空间对应的项目");
                    TaskCenterRequest taskCenterRequest = new TaskCenterRequest();
                    taskCenterRequest.setProjects(this.getOwnerProjectIds(taskRequestMap.get("SCENARIO").getUserId()));

                    // 结束掉未分发完成的任务
                    LoggerUtil.info("结束正在进行中的计划任务队列");
                    JMeterThreadUtils.stop("PLAN-CASE");
                    JMeterThreadUtils.stop("API-CASE-RUN");
                    JMeterThreadUtils.stop("SCENARIO-PARALLEL-THREAD");

                    if (taskRequestMap.containsKey("SCENARIO")) {
                        List<UiScenarioReport> reports = extUiScenarioReportMapper.findByProjectIds(taskCenterRequest);
                        LoggerUtil.info("查询到执行中的场景报告：" + reports.size());
                        if (CollectionUtils.isNotEmpty(reports)) {
                            for (UiScenarioReport report : reports) {

                                extracted(poolMap, report.getId(), report.getActuator());
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(report.getId());
                                PoolExecBlockingQueueUtil.offer(report.getId());
                            }

                            // 清理队列并停止测试计划报告
                            LoggerUtil.info("结束所有进行中的场景报告 ");
                            List<String> ids = reports.stream().map(UiScenarioReport::getId).collect(Collectors.toList());
                            extUiScenarioReportMapper.stopScenario(taskCenterRequest);
                            // 清理队列并停止测试计划报告
                            LoggerUtil.info("清理队列并停止测试计划报告 ");
                            uiExecutionQueueService.stop(ids);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
            if (!poolMap.isEmpty()) {
                this.send(poolMap);
            }
        }
        return ReportStatus.SUCCESS.name();
    }

    private void extracted(Map<String, List<String>> poolMap, String reportId, String actuator) {
        if (StringUtils.isEmpty(reportId)) {
            return;
        }
        if (StringUtils.isNotEmpty(actuator) && !StringUtils.equals(actuator, "LOCAL")) {
            if (poolMap.containsKey(actuator)) {
                poolMap.get(actuator).add(reportId);
            } else {
                poolMap.put(actuator, new ArrayList<String>() {{
                    this.add(reportId);
                }});
            }
        } else {
            JMeterThreadUtils.stop(reportId);
        }
    }
}
