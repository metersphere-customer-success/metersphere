package io.metersphere.project.service;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.enums.ProjectRobotType;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ProjectRobotService {

    @Resource
    private ProjectRobotMapper robotMapper;

    @Resource
    private MessageTaskMapper messageTaskMapper;

    @Resource
    private MessageTaskBlobMapper messageTaskBlobMapper;

    public void add(ProjectRobot projectRobot) {
        projectRobot.setId(IDGenerator.nextStr());
        projectRobot.setEnable(projectRobot.getEnable());
        checkDingTalk(projectRobot);
        robotMapper.insert(projectRobot);
    }

    private static void checkDingTalk(ProjectRobot projectRobot) {
        if (StringUtils.equals(projectRobot.getPlatform(), ProjectRobotPlatform.DING_TALK.toString())) {
            if (StringUtils.isBlank(projectRobot.getType())) {
                throw new MSException(Translator.get("ding_type_is_null"));
            }
            if (StringUtils.equals(projectRobot.getType(), ProjectRobotType.ENTERPRISE.toString())) {
                if (StringUtils.isBlank(projectRobot.getAppKey())) {
                    throw new MSException(Translator.get("ding_app_key_is_null"));
                }
                if (StringUtils.isBlank(projectRobot.getAppSecret())) {
                    throw new MSException(Translator.get("ding_app_secret_is_null"));
                }
            }
        }
    }

    public void update(ProjectRobot projectRobot) {
        checkRobotExist(projectRobot.getId());
        checkDingTalk(projectRobot);
        robotMapper.updateByPrimaryKeySelective(projectRobot);
    }

    private ProjectRobot checkRobotExist(String robotId) {
        ProjectRobot projectRobotInDB = robotMapper.selectByPrimaryKey(robotId);
        if (projectRobotInDB == null) {
            throw new MSException(Translator.get("robot_is_null"));
        }
        return projectRobotInDB;
    }

    public void delete(String id) {
        checkRobotExist(id);
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectRobotIdEqualTo(id);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        List<String> ids = messageTasks.stream().map(MessageTask::getId).toList();
        if (CollectionUtils.isNotEmpty(ids)) {
            MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
            messageTaskBlobExample.createCriteria().andIdIn(ids);
            messageTaskBlobMapper.deleteByExample(messageTaskBlobExample);
        }
        messageTaskMapper.deleteByExample(messageTaskExample);
        robotMapper.deleteByPrimaryKey(id);
    }

    public void enable(String id, String updateUser, Long updateTime) {
        ProjectRobot projectRobot = checkRobotExist(id);
        if (projectRobot.getEnable()) {
            projectRobot.setEnable(false);
        } else {
            projectRobot.setEnable(true);
        }
        projectRobot.setCreateUser(null);
        projectRobot.setCreateTime(null);
        projectRobot.setUpdateUser(updateUser);
        projectRobot.setUpdateTime(updateTime);
        robotMapper.updateByPrimaryKeySelective(projectRobot);
    }

    public List<ProjectRobot> getList(String projectId) {

        ProjectRobotExample projectExample = new ProjectRobotExample();
        ProjectRobotExample.Criteria criteria = projectExample.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        projectExample.setOrderByClause("create_time desc");
        List<ProjectRobot> projectRobots = robotMapper.selectByExample(projectExample);
        Integer inSiteIndex = 0;
        Integer mailIndex = 0;
        for (int i = 0; i < projectRobots.size(); i++) {
            ProjectRobot projectRobot = projectRobots.get(i);
            if (StringUtils.equalsIgnoreCase(projectRobot.getPlatform(), ProjectRobotPlatform.IN_SITE.toString())) {
                inSiteIndex = i;
            }
            if (StringUtils.equalsIgnoreCase(projectRobot.getPlatform(), ProjectRobotPlatform.MAIL.toString())) {
                mailIndex = i;
            }
            if ((StringUtils.equalsIgnoreCase(projectRobot.getPlatform(), ProjectRobotPlatform.IN_SITE.toString()) || StringUtils.equalsIgnoreCase(projectRobot.getPlatform(), ProjectRobotPlatform.MAIL.toString())) && StringUtils.isNotBlank(projectRobot.getDescription())) {
                projectRobot.setDescription(Translator.get(projectRobot.getDescription()));
                projectRobot.setName(Translator.get(projectRobot.getName()));
            }
        }
        if (projectRobots.size()>0 && inSiteIndex != 0) {
            Collections.swap(projectRobots, inSiteIndex, 0);
        }
        if (projectRobots.size()>1 && mailIndex != 1) {
            Collections.swap(projectRobots, mailIndex, 1);
        }
        return projectRobots;
    }

    public ProjectRobotDTO getDetail(String robotId) {
        ProjectRobot projectRobotInDB = checkRobotExist(robotId);
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        BeanUtils.copyBean(projectRobotDTO, projectRobotInDB);
        return projectRobotDTO;
    }
}