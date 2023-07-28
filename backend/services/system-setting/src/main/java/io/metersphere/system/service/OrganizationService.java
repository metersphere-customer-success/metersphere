package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.OrgUserExtend;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.OrganizationProjectOptionsDto;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.*;
import io.metersphere.system.request.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 * 组织功能
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtUserRoleRelationMapper extUserRoleRelationMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;

    public List<OrganizationDTO> list(OrganizationRequest organizationRequest) {
        List<OrganizationDTO> organizationDTOS = extOrganizationMapper.list(organizationRequest);
        return buildOrgAdminInfo(organizationDTOS);
    }

    public List<OrganizationDTO> listAll() {
        return extOrganizationMapper.listAll();
    }

    public OrganizationDTO getDefault() {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andNumEqualTo(100001L);
        List<Organization> organizations = organizationMapper.selectByExample(example);
        Organization organization = organizations.get(0);
        BeanUtils.copyBean(organizationDTO, organization);
        return organizationDTO;
    }

    public List<UserExtend> getMemberListBySystem(OrganizationRequest request) {
        return extOrganizationMapper.listMember(request);
    }

    public void addMemberBySystem(OrganizationMemberRequest organizationMemberRequest, String createUserId) {
        OrganizationMemberBatchRequest batchRequest = new OrganizationMemberBatchRequest();
        batchRequest.setOrganizationIds(List.of(organizationMemberRequest.getOrganizationId()));
        batchRequest.setMemberIds(organizationMemberRequest.getMemberIds());
        addMemberBySystem(batchRequest, createUserId);
    }

    public void addMemberBySystem(OrganizationMemberBatchRequest batchRequest, String createUserId) {
        checkOrgExist(batchRequest.getOrganizationIds());
        Map<String, User> userMap = checkUserExist(batchRequest.getMemberIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        batchRequest.getOrganizationIds().forEach(organizationId -> {
            for (String userId : batchRequest.getMemberIds()) {
                if (userMap.get(userId) == null) {
                    throw new MSException("id:" + userId + Translator.get("user.not.exist"));
                }
                //组织用户成员关系已存在, 不再重复添加
                UserRoleRelationExample example = new UserRoleRelationExample();
                example.createCriteria().andSourceIdEqualTo(organizationId).andUserIdEqualTo(userId).andRoleIdEqualTo(InternalUserRole.ORG_MEMBER.getValue());
                if (userRoleRelationMapper.countByExample(example) > 0) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(UUID.randomUUID().toString());
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(organizationId);
                userRoleRelation.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelations.add(userRoleRelation);
            }
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            extUserRoleRelationMapper.batchInsert(userRoleRelations);
        }
    }

    public List<OrgUserExtend> getMemberListByOrg(OrganizationRequest organizationRequest) {
        //根据组织ID获取所有组织用户关系表
        String organizationId = organizationRequest.getOrganizationId();
        List<OrgUserExtend> orgUserExtends = extOrganizationMapper.listMemberByOrg(organizationRequest);
        if (CollectionUtils.isEmpty(orgUserExtends)) {
            return new ArrayList<>();
        }
        Map<String, OrgUserExtend> userMap = orgUserExtends.stream().collect(Collectors.toMap(OrgUserExtend::getId, user -> user));
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        List<Project> projectsList = projectMapper.selectByExample(projectExample);
        List<String> projectIdList = projectsList.stream().map(Project::getId).toList();
        Map<String, String> projectIdNameMap = projectsList.stream().collect(Collectors.toMap(Project::getId, Project::getName));
        //根据用户id获取所有与该用户有关的当前组织以及组织下的项目关系表
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        List<String> sourceIds = new ArrayList<>(projectIdList);
        sourceIds.add(organizationId);
        userRoleRelationExample.createCriteria().andUserIdIn(new ArrayList<>(userMap.keySet())).andSourceIdIn(sourceIds);
        List<UserRoleRelation> userRoleRelationsByUsers = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //根据关系表查询出用户的关联组织和用户组
        Map<String, Map<String, String>> userIdprojectIdMap = new HashMap<>();
        Map<String, Set<String>> userIdRoleIdMap = new HashMap<>();
        for (UserRoleRelation userRoleRelationsByUser : userRoleRelationsByUsers) {
            String projectId = userRoleRelationsByUser.getSourceId();
            String roleId = userRoleRelationsByUser.getRoleId();
            String userId = userRoleRelationsByUser.getUserId();
            Map<String, String> pIdNameMap = userIdprojectIdMap.get(userId);
            if (MapUtils.isEmpty(pIdNameMap)) {
                pIdNameMap = new HashMap<>();
            }
            String projectName = projectIdNameMap.get(projectId);
            if (StringUtils.isNotBlank(projectName)) {
                pIdNameMap.put(projectId, projectName);
            }
            userIdprojectIdMap.put(userId, pIdNameMap);

            Set<String> roleIds = userIdRoleIdMap.get(userId);
            if (CollectionUtils.isEmpty(roleIds)) {
                roleIds = new HashSet<>();
            }
            roleIds.add(roleId);
            userIdRoleIdMap.put(userId, roleIds);
        }
        for (OrgUserExtend orgUserExtend : orgUserExtends) {
            Map<String, String> pIdNameMap = userIdprojectIdMap.get(orgUserExtend.getId());
            if (MapUtils.isNotEmpty(pIdNameMap)) {
                //移除当前组织id
                orgUserExtend.setProjectIdNameMap(projectIdNameMap);
            }
            Set<String> userRoleIds = userIdRoleIdMap.get(orgUserExtend.getId());
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria().andIdIn(new ArrayList<>(userRoleIds));
            List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
            Map<String, String> userRoleIdNameMap = userRoles.stream().collect(Collectors.toMap(UserRole::getId, UserRole::getName));
            orgUserExtend.setUserRoleIdNameMap(userRoleIdNameMap);
        }
        return orgUserExtends;
    }

    public void removeMember(String organizationId, String userId) {
        checkOrgExist(List.of(organizationId));
        //删除组织下项目与成员的关系
        List<String> projectIds = getProjectIds(organizationId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            UserRoleRelationExample example = new UserRoleRelationExample();
            example.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(projectIds);
            userRoleRelationMapper.deleteByExample(example);
        }
        //删除组织与成员的关系
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        userRoleRelationMapper.deleteByExample(example);
    }

    private List<OrganizationDTO> buildOrgAdminInfo(List<OrganizationDTO> organizationDTOS) {
        if (CollectionUtils.isEmpty(organizationDTOS)) {
            return organizationDTOS;
        }
        organizationDTOS.forEach(organizationDTO -> {
            List<User> orgAdminList = extOrganizationMapper.getOrgAdminList(organizationDTO.getId());
            organizationDTO.setOrgAdmins(orgAdminList);
        });
        return organizationDTOS;
    }

    public List<OrganizationProjectOptionsDto> getOrganizationOptions() {
        return extOrganizationMapper.selectOrganizationOptions();
    }

    public void addMemberByOrg(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId) {
        checkOrgExist(List.of(organizationMemberExtendRequest.getOrganizationId()));
        Map<String, User> userMap = checkUserExist(organizationMemberExtendRequest.getMemberIds());
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds());
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, createUserId, userMap, userRoleMap, true);
    }

    private void setRelationByMemberAndGroupIds(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId, Map<String, User> userMap, Map<String, UserRole> userRoleMap, boolean add) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO> logDTOList = new ArrayList<>();
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        organizationMemberExtendRequest.getMemberIds().forEach(memberId -> {
            if (userMap.get(memberId) == null) {
                throw new MSException("id:" + memberId + Translator.get("user.not.exist"));
            }
            organizationMemberExtendRequest.getUserRoleIds().forEach(userGroupId -> {
                if (userRoleMap.get(userGroupId) != null) {
                    //过滤已存在的关系
                    UserRoleRelationExample example = new UserRoleRelationExample();
                    example.createCriteria().andSourceIdEqualTo(organizationId).andUserIdEqualTo(memberId).andRoleIdEqualTo(userGroupId);
                    List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
                    if (CollectionUtils.isEmpty(userRoleRelations)) {
                        UserRoleRelation userRoleRelation = new UserRoleRelation();
                        userRoleRelation.setId(UUID.randomUUID().toString());
                        userRoleRelation.setUserId(memberId);
                        userRoleRelation.setSourceId(organizationId);
                        userRoleRelation.setRoleId(userGroupId);
                        userRoleRelation.setCreateTime(System.currentTimeMillis());
                        userRoleRelation.setCreateUser(createUserId);
                        userRoleRelationMapper.insert(userRoleRelation);
                        //add Log
                        String path = add ? "/organization/add-member" : "/organization/role/update-member";
                        setLog(organizationId, "system", path, OperationLogModule.ORGANIZATION_MEMBER, "成员", logDTOList, memberId, userRoleRelation, add);
                    }
                }
            });
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    private static void setLog(String organizationId, String projectId, String path, String module, String content, List<LogDTO> logDTOList, String memberId, Object originalValue, boolean add) {
        String type = add ? OperationLogType.ADD.name() : OperationLogType.UPDATE.name();
        LogDTO dto = new LogDTO(
                projectId,
                organizationId,
                memberId,
                null,
                type,
                module,
                content);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        logDTOList.add(dto);
    }

    public void updateMemberRole(OrganizationMemberExtendRequest organizationMemberExtendRequest, String userId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExist(List.of(organizationId));
        Map<String, User> userMap = checkUserExist(organizationMemberExtendRequest.getMemberIds());
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds());
        //在新增组织成员与用户组和组织的关系
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, userId, userMap, userRoleMap, false);
    }

    public void addMemberToProject(OrgMemberExtendProjectRequest orgMemberExtendProjectRequest, String userId) {
        String requestOrganizationId = orgMemberExtendProjectRequest.getOrganizationId();
        checkOrgExist(List.of(requestOrganizationId));
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> projectIds = orgMemberExtendProjectRequest.getProjectIds();
        //用户不在当前组织内过掉
        Map<String, User> userMap = checkUserExist(orgMemberExtendProjectRequest.getMemberIds());
        orgMemberExtendProjectRequest.getMemberIds().forEach(memberId -> {
            if (userMap.get(memberId) == null) {
                return;
            }
            projectIds.forEach(projectId -> {
                //过滤已存在的关系
                UserRoleRelationExample example = new UserRoleRelationExample();
                example.createCriteria().andSourceIdEqualTo(projectId).andUserIdEqualTo(memberId).andRoleIdEqualTo(InternalUserRole.PROJECT_MEMBER.getValue());
                List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(userRoleRelations)) {
                    UserRoleRelation userRoleRelation = new UserRoleRelation();
                    userRoleRelation.setId(UUID.randomUUID().toString());
                    userRoleRelation.setUserId(memberId);
                    userRoleRelation.setSourceId(projectId);
                    userRoleRelation.setRoleId(InternalUserRole.PROJECT_MEMBER.getValue());
                    userRoleRelation.setCreateTime(System.currentTimeMillis());
                    userRoleRelation.setCreateUser(userId);
                    userRoleRelationMapper.insert(userRoleRelation);
                    //add Log
                    setLog(requestOrganizationId, projectId, "/organization/project/add-member", OperationLogModule.PROJECT_PROJECT_MEMBER, "", logDTOList, memberId, userRoleRelation, true);
                }
            });
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);

    }

    /**
     * 删除组织用户日志
     * @return 日志内容
     */
    public List<LogDTO> batchDelLog(String organizationId, String userId) {
        List<String> projectIds = getProjectIds(organizationId);
        UserRoleRelationExample example = new UserRoleRelationExample();
        if (CollectionUtils.isEmpty(projectIds)) {
            return null;
        }
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(projectIds);
        List<UserRoleRelation> userRoleWidthProjectRelations = userRoleRelationMapper.selectByExample(example);
        example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleWidthOrgRelations = userRoleRelationMapper.selectByExample(example);
        List<LogDTO> dtoList = new ArrayList<>();
        for (UserRoleRelation userRoleWidthProjectRelation : userRoleWidthProjectRelations) {
            LogDTO dto = new LogDTO(
                    userRoleWidthProjectRelation.getSourceId(),
                    organizationId,
                    userRoleWidthProjectRelation.getId(),
                    userRoleWidthProjectRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.ORGANIZATION_MEMBER,
                    "成员");

            dto.setPath("/organization/remove-member/{organizationId}/{userId}");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(userRoleWidthProjectRelation));
            dtoList.add(dto);
        }
        for (UserRoleRelation userRoleWidthOrgRelation : userRoleWidthOrgRelations) {
            LogDTO dto = new LogDTO(
                    "system",
                    organizationId,
                    userRoleWidthOrgRelation.getId(),
                    userRoleWidthOrgRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.ORGANIZATION_MEMBER,
                    "成员");

            dto.setPath("/organization/remove-member/{organizationId}/{userId}");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(userRoleWidthOrgRelation));
            dtoList.add(dto);
        }

        return dtoList;
    }

    private List<String> getProjectIds(String organizationId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        if (CollectionUtils.isNotEmpty(projects)) {
            return projects.stream().map(Project::getId).toList();
        } else {
            return new ArrayList<>();
        }
    }

    public void updateMember(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        List<String> memberIds = organizationMemberExtendRequest.getMemberIds();

        //校验组织是否存在
        checkOrgExist(List.of(organizationId));
        //校验成员是否是当前组织的成员
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdIn(memberIds).andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().collect(Collectors.toList());
        Map<String, User> userMap = checkUserExist(userIds);
        //删除旧的关系
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdIn(userIds).andSourceIdEqualTo(organizationId);
        userRoleRelationMapper.deleteByExample(userRoleRelationExample);
        //检查用户组是否是组织级别用户组
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO> logDTOList = new ArrayList<>();
        userIds.forEach(userId -> {
            organizationMemberExtendRequest.getUserRoleIds().forEach(userRoleId -> {
                if (userRoleMap.get(userRoleId) == null) {
                    return;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(UUID.randomUUID().toString());
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(organizationId);
                userRoleRelation.setRoleId(userRoleId);
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelationMapper.insert(userRoleRelation);
                //add Log
                String path = "/organization/update-member";
                setLog(organizationId, "system", path, OperationLogModule.ORGANIZATION_MEMBER, "成员", logDTOList, userId, userRoleRelation, false);
            });
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    private void checkOrgExist(List<String> organizationIds) {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andIdIn(organizationIds);
        if (organizationMapper.countByExample(example) < organizationIds.size()) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    private Map<String, UserRole> checkUseRoleExist(List<String> userRoleIds) {
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andIdIn(userRoleIds).andTypeEqualTo("ORGANIZATION");
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
        return userRoles.stream().collect(Collectors.toMap(UserRole::getId, user -> user));

    }

    private Map<String, User> checkUserExist(List<String> memberIds) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(memberIds);
        List<User> users = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(users)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        return users.stream().collect(Collectors.toMap(User::getId, user -> user));
    }
}
