package io.metersphere.service;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.UserGroup;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CheckPermissionService {
//    @Resource
//    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private UserService userService;
    @Resource
    private ProjectMapper projectMapper;


    public void checkProjectOwner(String projectId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        if (!projectIds.contains(projectId)) {
            MSException.throwException(Translator.get("check_owner_project"));
        }
    }

    public Set<String> getUserRelatedProjectIds() {
        UserDTO userDTO = userService.getUserDTO(SessionUtils.getUserId());
        List<String> groupIds = userDTO.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        return userDTO.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toSet());
    }

    public void checkTestPlanOwner(String planId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        // todo microservice
        int result = 1;
//        int result = extTestPlanMapper.checkIsHave(planId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_plan"));
        }
    }

    public Set<String> getOwnerByUserId(String userId) {
        UserDTO userDTO = userService.getUserDTO(userId);
        List<String> groupIds = userDTO.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        return userDTO.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toSet());
    }

    public void checkProjectBelongToWorkspace(String projectId, String workspaceId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null || !StringUtils.equals(project.getWorkspaceId(), workspaceId)) {
            MSException.throwException(Translator.get("check_owner_project"));
        }
    }
}
