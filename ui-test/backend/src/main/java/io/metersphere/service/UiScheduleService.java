package io.metersphere.service;


import io.metersphere.base.domain.Schedule;
import io.metersphere.base.domain.ScheduleExample;
import io.metersphere.base.domain.User;
import io.metersphere.base.domain.UserExample;
import io.metersphere.base.mapper.ScheduleMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.BaseScheduleMapper;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.dto.request.ScheduleRequest;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.schedule.ScheduleReference;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.QueryScheduleRequest;
import io.metersphere.sechedule.ScheduleManager;
import io.metersphere.sechedule.UiTestJob;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private BaseScheduleMapper extScheduleMapper;
    @Resource
    private UserMapper userMapper;

    public void addSchedule(Schedule schedule) {
        schedule.setId(UUID.randomUUID().toString());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        scheduleMapper.insert(schedule);
    }

    public Schedule getSchedule(String ScheduleId) {
        return scheduleMapper.selectByPrimaryKey(ScheduleId);
    }

    public int editSchedule(Schedule schedule) {
        schedule.setUpdateTime(System.currentTimeMillis());
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    public Schedule getScheduleByResource(String resourceId, String group) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andGroupEqualTo(group);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (schedules.size() > 0) {
            return schedules.get(0);
        }
        return null;
    }

    public List<Schedule> getScheduleByResourceIds(List<String> resourceIds, String group) {
        ScheduleExample example = new ScheduleExample();
        if (resourceIds.size() == 0) {
            return new ArrayList<>();
        }
        example.createCriteria().andResourceIdIn(resourceIds).andGroupEqualTo(group);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (schedules.size() > 0) {
            return schedules;
        }
        return new ArrayList<>();
    }


    public void closeByResourceId(String resourceId, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(resourceId);
        List<Schedule> scheduleList = scheduleMapper.selectByExample(scheduleExample);
        removeJob(resourceId, group);
        for (Schedule schedule : scheduleList) {
            schedule.setEnable(false);
            scheduleMapper.updateByPrimaryKeySelective(schedule);
        }
    }

    public int deleteByResourceId(String resourceId, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(resourceId);
        removeJob(resourceId, group);
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    private void removeJob(String resourceId, String group) {
        if (StringUtils.equals(ScheduleGroup.UI_SCENARIO_TEST.name(), group)) {
            scheduleManager.removeJob(UiTestJob.getJobKey(resourceId), UiTestJob.getTriggerKey(resourceId));
        }
    }

    public List<Schedule> listSchedule() {
        ScheduleExample example = new ScheduleExample();
        return scheduleMapper.selectByExample(example);
    }

    public List<Schedule> getEnableSchedule() {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andEnableEqualTo(true);
        return scheduleMapper.selectByExample(example);
    }

    public void startEnableSchedules() {
        List<Schedule> Schedules = getEnableSchedule();

        Schedules.forEach(schedule -> {
            try {
                if (schedule.getEnable()) {
                    LogUtil.info("初始化任务：" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(new JobKey(schedule.getKey(), schedule.getGroup()),
                            new TriggerKey(schedule.getKey(), schedule.getGroup()), Class.forName(schedule.getJob()), schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getUserId()));
                }
            } catch (Exception e) {
                LogUtil.error("初始化任务失败", e);
            }
        });
    }

    public Schedule buildApiTestSchedule(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setResourceId(request.getResourceId());
        schedule.setEnable(request.getEnable());
        schedule.setValue(request.getValue().trim());
        schedule.setKey(request.getResourceId());
        schedule.setUserId(SessionUtils.getUser().getId());
        schedule.setProjectId(request.getProjectId());
        schedule.setWorkspaceId(request.getWorkspaceId());
        schedule.setConfig(request.getConfig());
        return schedule;
    }

    public void resetJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        try {
            scheduleManager.removeJob(jobKey, triggerKey);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException("重置定时任务-删除旧定时任务时出现异常");
        }
        if (!request.getEnable()) {
            return;
        }
        try {
            scheduleManager.addCronJob(jobKey, triggerKey, clazz, request.getValue(),
                    scheduleManager.getDefaultJobDataMap(request, request.getValue(), SessionUtils.getUser().getId()));
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException("重置定时任务-启动新定时任务出现异常");
        }
    }


    public void updateScheduleStatus(Schedule request) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(request.getId());
        schedule.setEnable(request.getEnable());
        this.updateSchedule(schedule);
    }

    public void updateSchedule(Schedule request) {
        JobKey jobKey = UiTestJob.getJobKey(request.getResourceId());
        TriggerKey triggerKey = UiTestJob.getTriggerKey(request.getResourceId());
        Class clazz = UiTestJob.class;
        request.setJob(UiTestJob.class.getName());
        this.editSchedule(request);
        this.resetJob(request, jobKey, triggerKey, clazz);
    }

    public void addOrUpdateCronJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (enable != null && enable && StringUtils.isNotBlank(cronExpression)) {
            try {
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression,
                        scheduleManager.getDefaultJobDataMap(request, cronExpression, SessionUtils.getUser().getId()));
            } catch (SchedulerException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException("定时任务开启异常");
            }
        } else {
            try {
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                MSException.throwException("定时任务关闭异常");
            }
        }
    }

    public List<ScheduleDao> list(QueryScheduleRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        request.setOrders(orderList);
        return extScheduleMapper.list(request);
    }

    public void build(Map<String, String> resourceNameMap, List<ScheduleDao> schedules) {
        List<String> userIds = schedules.stream()
                .map(Schedule::getUserId)
                .collect(Collectors.toList());
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        Map<String, String> userMap = userMapper.selectByExample(example).stream().collect(Collectors.toMap(User::getId, User::getName));
        schedules.forEach(schedule -> {
            schedule.setResourceName(resourceNameMap.get(schedule.getResourceId()));
            schedule.setUserName(userMap.get(schedule.getUserId()));
        });
    }

    public List<TaskInfoResult> findRunningTaskInfoByProjectID(String projectID, BaseQueryRequest request) {
        List<TaskInfoResult> runningTaskInfoList = extScheduleMapper.findRunningTaskInfoByProjectID(projectID, request);
        return runningTaskInfoList;
    }

    public Object getCurrentlyExecutingJobs() {
        return scheduleManager.getCurrentlyExecutingJobs();
    }

    public String getScheduleInfo(String id) {
        ScheduleExample schedule = new ScheduleExample();
        schedule.createCriteria().andResourceIdEqualTo(id);
        List<Schedule> list = scheduleMapper.selectByExample(schedule);
        if (list.size() > 0) {
            return list.get(0).getKey();
        } else {
            return "";
        }

    }

    public String getLogDetails(String id) {
        Schedule bloB = scheduleMapper.selectByPrimaryKey(id);
        if (bloB != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloB, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloB.getId()), bloB.getProjectId(), bloB.getName(), bloB.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ScheduleRequest request) {
        Schedule bloBs = this.getScheduleByResource(request.getResourceId(), request.getGroup());
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getName(), bloBs.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(Schedule request) {
        Schedule bloBs = this.getScheduleByResource(request.getResourceId(), request.getGroup());
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getName(), bloBs.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<Schedule> selectByResourceIds(List<String> scenarioIds) {
        if (CollectionUtils.isNotEmpty(scenarioIds)) {
            ScheduleExample example = new ScheduleExample();
            example.createCriteria().andResourceIdIn(scenarioIds);
            return scheduleMapper.selectByExample(example);
        } else {
            return new ArrayList<>();
        }
    }
}
