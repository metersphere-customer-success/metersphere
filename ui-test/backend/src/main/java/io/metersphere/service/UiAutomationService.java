package io.metersphere.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanUiScenarioCaseMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.*;
import io.metersphere.config.ThreadPoolConfig;
import io.metersphere.constants.MsHashTreeConstants;
import io.metersphere.constants.*;
import io.metersphere.controller.handler.ResultHolder;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.ApiScenarioDTO;
import io.metersphere.dto.automation.ApiScenarioRequest;
import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.dto.request.ElementUtil;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.dto.testcase.FileOperationRequest;
import io.metersphere.dto.testcase.testcase.UiCaseRelevanceRequest;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.hashtree.MsUiScenario;
import io.metersphere.hashtree.MsWebDriverSampler;
import io.metersphere.i18n.Translator;
import io.metersphere.impl.CommandConfig;
import io.metersphere.impl.CommonCommand;
import io.metersphere.intf.ScenarioParser;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.utils.*;
import io.metersphere.vo.BaseLocator;
import io.metersphere.vo.UiAtomicCommandVO;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Response;
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import io.metersphere.utils.processor.CompatibilityOldData;

import static io.metersphere.constants.MsHashTreeConstants.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiAutomationService {
    @Resource
    UiScenarioModuleMapper uiScenarioModuleMapper;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private ExtUiScenarioMapper extUiScenarioMapper;
    @Resource
    private UiScheduleService scheduleService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UiScenarioExecuteService uiScenarioExecuteService;
    @Resource
    private BaseProjectVersionMapper extProjectVersionMapper;
    @Resource
    private UiScenarioReferenceMapper uiScenarioReferenceMapper;
    @Resource
    private UiElementService uiElementService;
    @Resource
    private UiElementReferenceService uiElementReferenceService;
    @Resource
    private ExtTestPlanUiScenarioCaseMapper extTestPlanUiScenarioCaseMapper;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private UiScenarioEnvService uiScenarioEnvService;
    @Resource
    private BaseEnvGroupProjectService baseEnvironmentGroupProjectService;

    private static FileManagerService fileManagerService;


    private final ThreadLocal<Long> currentScenarioOrder = new ThreadLocal<>();

    public static final String RESOURCE_ID = "resourceId";
    public static final String SCENARIO_LABEL = "场景";
    public static final String CUSTOM_COMMAND_LABEL = "指令";
    public static final String VARIABLES = "variables";
    private static final int SPLIT_LIMIT = 100;
    public static final String HASH_TREE = "hashTree";
    public static final String BODY_FILE_DIR = "/opt/metersphere/data/body";


    public UiScenarioDTO getDto(String id) {
        UiScenarioRequest request = new UiScenarioRequest();
        request.setId(id);
        UiScenarioDTO uiScenarioDTO = null;
        UiScenarioWithBLOBs uiScenarioWithBLOBs = uiScenarioMapper.selectByPrimaryKey(id);
        List<UiScenarioDTO> list = extUiScenarioMapper.list(request);
        if (CollectionUtils.isNotEmpty(list)) {
            uiScenarioDTO = list.get(0);
            BeanUtils.copyBean(uiScenarioDTO, uiScenarioWithBLOBs);
        }
        return uiScenarioDTO;
    }

    public String getUser(String id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user != null) {
            return user.getName();
        }
        return null;
    }

    public List<UiScenarioDTO> list(UiScenarioRequest request) {
        this.initRequest(request, true, true);
        List<UiScenarioDTO> list = extUiScenarioMapper.list(request);
        if (BooleanUtils.isTrue(request.isSelectEnvironment())) {
            setUiScenarioEnv(list);
        }
        return list;
    }

    public void setUiScenarioEnv(List<UiScenarioDTO> list) {
        List<Project> projectList = projectMapper.selectByExample(new ProjectExample());
        List<ApiTestEnvironmentWithBLOBs> apiTestEnvironments = apiTestEnvironmentMapper.selectByExampleWithBLOBs(new ApiTestEnvironmentExample());
        for (UiScenarioDTO scenarioDTO : list) {
            Map<String, String> map = new HashMap<>();
            String env = scenarioDTO.getEnv();
            if (StringUtils.equals(scenarioDTO.getEnvironmentType(), EnvironmentTypeEnum.JSON.name())) {
                // 环境属性为空 跳过
                if (StringUtils.isBlank(env)) {
                    continue;
                }
                map = JSON.parseObject(env, Map.class);
            }

            Set<String> set = map.keySet();
            HashMap<String, String> envMap = new HashMap<>(16);
            // 项目为空 跳过
            if (set.isEmpty()) {
                continue;
            }
            for (String projectId : set) {
                String envId = map.get(projectId);
                if (StringUtils.isBlank(envId)) {
                    continue;
                }
                List<Project> projects = projectList.stream().filter(p -> StringUtils.equals(p.getId(), projectId)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(projects)) {
                    continue;
                }
                Project project = projects.get(0);
                List<ApiTestEnvironmentWithBLOBs> envs = apiTestEnvironments.stream().filter(e -> StringUtils.equals(e.getId(), envId)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(envs)) {
                    continue;
                }
                ApiTestEnvironmentWithBLOBs environment = envs.get(0);
                String projectName = project.getName();
                String envName = environment.getName();
                if (StringUtils.isBlank(projectName) || StringUtils.isBlank(envName)) {
                    continue;
                }
                envMap.put(projectName, envName);
            }
            scenarioDTO.setEnvironmentMap(envMap);
        }
    }

    public List<UiScenarioDTO> listAll(UiScenarioBatchRequest request) {
        if (StringUtils.isBlank(request.getCondition().getScenarioType())) {
            request.getCondition().setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        return extUiScenarioMapper.selectIds(request.getIds());
    }

    public int listAllTrash(UiScenarioBatchRequest request) {
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        return extUiScenarioMapper.selectTrashWithType(request.getProjectId(), request.getScenarioType());
    }

    public List<String> idAll(UiScenarioBatchRequest request) {
        if (StringUtils.isBlank(request.getCondition().getScenarioType())) {
            request.getCondition().setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        // 与列表查询保持顺序一致
        if (CollectionUtils.isEmpty(request.getCondition().getOrders())) {
            request.getCondition().setOrders(ServiceUtils.getDefaultSortOrder(null));
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        return request.getIds();
    }

    public List<UiScenarioDTO> listReview(UiScenarioRequest request) {
        this.initRequest(request, true, true);
        return extUiScenarioMapper.listReview(request);
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefultOrders
     * @param checkThisWeekData
     * @return
     */
    private UiScenarioRequest initRequest(UiScenarioRequest request, boolean setDefultOrders, boolean checkThisWeekData) {
        if (setDefultOrders) {
            request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        }
        if (StringUtils.isNotEmpty(request.getExecuteStatus())) {
            Map<String, List<String>> statusFilter = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add("Prepare");
            list.add("Underway");
            list.add("Completed");
            statusFilter.put("status", list);
            request.setFilters(statusFilter);
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
                if (weekFirstTime != null) {
                    request.setCreateTime(weekFirstTime.getTime());
                }
            }
        }

        //默认场景类型
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
        }

        request.setName(TemplateUtils.escapeSqlSpecialChars(request.getName()));
        return request;
    }

    public UiScenario create(SaveUiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        checkNameExist(request);
        //        checkScenarioNum(request);
        UiScenarioWithBLOBs scenario = buildSaveScenario(request);
        int nextNum = getNextNum(request.getProjectId());
        scenario.setNum(nextNum);
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setOrder(ServiceUtils.getNextOrder(scenario.getProjectId(), extUiScenarioMapper::getLastOrder));
        scenario.setRefId(scenario.getId());
        scenario.setLatest(true);
        scenario.setEnvironmentJson(request.getEnvironmentJson());
        scenario.setEnvironmentType(request.getEnvironmentType());
        scenario.setScenarioType(StringUtils.isBlank(request.getScenarioType()) ? UiScenarioType.SCENARIO.getType() : request.getScenarioType());
        uiScenarioMapper.insertSelective(scenario);

        //处理元素的引用关系
        saveElementAndScenarioRelation(scenario);

        //处理场景 指令的引用关系
        saveApiAndScenarioRelation(scenario);

        return scenario;
    }

    public void uploadFiles(SaveUiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        FileUtils.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);
        List<String> bodyFileRequestIds = request.getBodyFileRequestIds();
        // MinIO存储
        if (fileManagerService == null) {
            fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
        }
        if (CollectionUtils.isNotEmpty(bodyFileRequestIds)) {
            bodyFileRequestIds.forEach(requestId->{
                if (CollectionUtils.isNotEmpty(bodyFiles) && StringUtils.isNotBlank(requestId)) {
                    for (MultipartFile bodyFile : bodyFiles) {
                        fileManagerService.upload(bodyFile, getRequest("ui/"+request.getProjectId()+"/"+requestId, bodyFile));
                    }
                }
            });
        }

    }

    private static FileRequest getRequest(String requestId, MultipartFile bodyFile) {
        FileRequest request = new FileRequest();
        String path = StringUtils.join(BODY_FILE_DIR, File.separator, requestId);
        request.setProjectId(path);
        request.setFileName(bodyFile.getOriginalFilename());
        request.setStorage(StorageConstants.MINIO.name());
        LoggerUtil.info("开始从MinIO处理文件：", path);
        return request;
    }

    private void checkCustomNumExist(SaveUiScenarioRequest request) {
        UiScenarioExample example = new UiScenarioExample();
        String id = request.getId();
        UiScenarioWithBLOBs apiScenarioWithBLOBs = uiScenarioMapper.selectByPrimaryKey(id);
        UiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andCustomNumEqualTo(request.getCustomNum())
                .andProjectIdEqualTo(request.getProjectId())
                .andIdNotEqualTo(id);
        if (apiScenarioWithBLOBs != null && StringUtils.isNotBlank(apiScenarioWithBLOBs.getRefId())) {
            criteria.andRefIdNotEqualTo(apiScenarioWithBLOBs.getRefId());
        }
        List<UiScenario> list = uiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            MSException.throwException("自定义ID " + request.getCustomNum() + " 已存在！");
        }
    }

    private int getNextNum(String projectId) {
        UiScenario apiScenario = extUiScenarioMapper.getNextNumOpt(projectId);
        if (apiScenario == null || apiScenario.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiScenario.getNum() + 1).orElse(100001);
        }
    }

    public UiScenario update(SaveUiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        //        checkQuota();
        checkNameExist(request);
        //        checkScenarioNum(request);
        UiScenarioWithBLOBs scenario = new UiScenarioWithBLOBs();
        BeanUtils.copyBean(scenario, request);
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setScenarioType(StringUtils.isBlank(request.getScenarioType()) ? UiScenarioType.SCENARIO.getType() : request.getScenarioType());
        uiScenarioMapper.updateByPrimaryKeySelective(scenario);
        //保存场景引用和复制的关系
        saveApiAndScenarioRelation(scenario);
        //处理元素的引用关系
        saveElementAndScenarioRelation(scenario);
        // 场景/指令被依赖需要更新相关执行场景/指令
        dealWithRef(request.getId());

        return scenario;
    }

    /**
     * handle element and ui scenario reference
     */
    public void saveElementAndScenarioRelation(UiScenarioWithBLOBs scenario) {
        if (scenario == null || scenario.getId() == null
                || StringUtils.isBlank(scenario.getScenarioDefinition())) {
            return;
        }

        try {
            //解析为json格式
            JSONObject jsonObject = new JSONObject(scenario.getScenarioDefinition());
            if (!jsonObject.has(MsHashTreeConstants.HASH_TREE)) {
                return;
            }

            //找到含有元素对象的 建立引用关系
            createElementReference(scenario.getId(), scenario.getProjectId(), jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createElementReference(String scenarioId, String projectId, JSONObject jsonObject) {

        JSONArray hashTree = jsonObject.optJSONArray(MsHashTreeConstants.HASH_TREE);

        //hashTree中找到所有引用元素的
        List<UiElementReference> list = doCreateElementReference(scenarioId, projectId, hashTree);

        List<String> elementIds = list.stream().map(UiElementReference::getElementId).distinct().collect(Collectors.toList());
        //存在的元素
        List<UiElement> uiElements = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(elementIds)) {
            //检测元素是否存在
            UiElementExample uiElementExample = new UiElementExample();
            UiElementExample.Criteria criteria = uiElementExample.createCriteria();
            criteria.andIdIn(elementIds);
            criteria.andProjectIdEqualTo(projectId);
            uiElements = uiElementService.listByExample(uiElementExample);
        }

        //存在的元素id列表
       List<String> exist = uiElements.stream().map(UiElement::getId).toList();

        //从list中过滤掉存在的 找到不存在的元素
        List<UiElementReference> notExist = list.stream()
                .filter(v -> !exist.contains(v.getElementId())).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(notExist)) {
            //修改 definition 元素已经不存在的 元素对象 改为 元素定位
            List<String> notExistIds = notExist.stream().map(UiElementReference::getElementId).distinct().collect(Collectors.toList());
            uiElementReferenceService.changeType(scenarioId, notExistIds);
            //删掉元素引用
            uiElementReferenceService.batchDelete(projectId, scenarioId,
                    notExist.stream().map(UiElementReference::getElementId).collect(Collectors.toList()));
            list.removeAll(notExist);
        }

        //检测引用是否存在
        List<UiElementReference> referenceList = uiElementReferenceService.listByScenarioId(projectId, scenarioId);
        List<String> referenceIds = referenceList.stream().map(UiElementReference::getElementId).toList();
        List<String> currentIds = list.stream().map(UiElementReference::getElementId).toList();

        //从list中过滤出不再引用的
        List<String> toBeDelete = referenceIds.stream().filter(v -> !currentIds.contains(v)).collect(Collectors.toList());
        List<UiElementReference> toBeUpdate = list.stream()
                .filter(v -> !referenceIds.contains(v.getElementId())).collect(Collectors.toList());

        //增量
        if (CollectionUtils.isNotEmpty(toBeUpdate)) {
            uiElementReferenceService.batchInsert(toBeUpdate);
        }

        //删除不再引用的
        if (CollectionUtils.isNotEmpty(toBeDelete)) {
            uiElementReferenceService.batchDelete(projectId, scenarioId, toBeDelete);
        }
    }

    private List<UiElementReference> doCreateElementReference(String scenarioId, String projectId, JSONArray hashTree) {
        List<UiElementReference> list = new ArrayList<>();
        if (hashTree == null || hashTree.length() == 0) {
            return list;
        }
        for (int index = 0; index < hashTree.length(); index++) {
            JSONObject item = hashTree.optJSONObject(index);
            if (item == null) {
                continue;
            }

            //parse vo
            checkAndCreateElementReference(item.optJSONObject("vo"), list, projectId, scenarioId);
            checkAndCreateElementReference(item.optJSONObject("targetVO"), list, projectId, scenarioId);

            //handle preCommands
            JSONArray preCommands = item.optJSONArray("preCommands");
            if (preCommands != null) {
                list.addAll(doCreateElementReference(scenarioId, projectId, preCommands));
            }

            //handle hashTree
            JSONArray hashTrees = item.optJSONArray("hashTree");
            if (hashTrees != null) {
                list.addAll(doCreateElementReference(scenarioId, projectId, hashTrees));
            }

            //handle postCommands
            JSONArray postCommands = item.optJSONArray("postCommands");
            if (postCommands != null) {
                list.addAll(doCreateElementReference(scenarioId, projectId, postCommands));
            }
        }
        return list;
    }

    /**
     * 处理vo中的定位器的引用
     *
     * @param itemVO
     * @param list
     */
    private void checkAndCreateElementReference(JSONObject itemVO, List<UiElementReference> list, String projectId, String scenarioId) {

        //情况1:vo本身就是定位器的
        if (itemVO == null) {
            return;
        }
        String elementType = itemVO.optString("elementType");
        if (StringUtils.isNotBlank(elementType) && StringUtils.equalsIgnoreCase(elementType,"elementObject") && StringUtils.isNotBlank(itemVO.optString("elementId"))) {
            //组装UiElementReference
            UiElementReference reference = new UiElementReference();
            reference.setId(UUID.randomUUID().toString());
            reference.setElementModuleId(itemVO.optString("moduleId"));
            reference.setElementId(itemVO.optString("elementId"));
            reference.setProjectId(projectId);
            reference.setScenarioId(scenarioId);
            reference.setCreateTime(System.currentTimeMillis());
            list.add(reference);
            return;
        }

        //情况2：指令中又多个定位器 比如叫  locator , startLocator, endLocator
        String[] locatorNames = new String[]{"locator", "startLocator", "endLocator"};
        for (String locatorName : locatorNames) {
            JSONObject locator = itemVO.optJSONObject(locatorName);
            JSONObject target = null;

            //find all elementObject
            if (locator != null && StringUtils.isNotBlank(locator.optString("elementType"))
                    && "elementObject".equals(locator.optString("elementType"))) {
                target = locator;
            }

            if (target != null && StringUtils.isNotBlank(target.optString("elementId"))) {
                //组装UiElementReference
                UiElementReference reference = new UiElementReference();
                reference.setId(UUID.randomUUID().toString());
                reference.setElementModuleId(target.optString("moduleId"));
                reference.setElementId(target.optString("elementId"));
                reference.setProjectId(projectId);
                reference.setScenarioId(scenarioId);
                reference.setCreateTime(System.currentTimeMillis());
                list.add(reference);
            }
        }
    }

    public void saveApiAndScenarioRelation(UiScenarioWithBLOBs scenario) {
        if (scenario.getId() == null) {
            return;
        }
        this.deleteByScenarioId(scenario.getId());
        Map<String, UiScenarioReference> referenceIdMap = new HashMap<>();
        if (StringUtils.isNotEmpty(scenario.getScenarioDefinition())) {
            JSONObject jsonObject = new JSONObject(scenario.getScenarioDefinition());
            if (!jsonObject.has(MsHashTreeConstants.HASH_TREE)) {
                return;
            }
            JSONArray hashTree = jsonObject.optJSONArray(MsHashTreeConstants.HASH_TREE);
            for (int index = 0; index < hashTree.length(); index++) {
                JSONObject item = hashTree.optJSONObject(index);
                if (item == null) {
                    continue;
                }
                boolean isResource = true;
                if (item.has(MsHashTreeConstants.ID) && item.has(REFERENCED)) {
                    UiScenarioReference saveItem = new UiScenarioReference();
                    saveItem.setId(UUID.randomUUID().toString());
                    saveItem.setUiScenarioId(scenario.getId());
                    saveItem.setReferenceId(isResource ? item.optString(RESOURCE_ID) : item.optString(MsHashTreeConstants.ID));
                    saveItem.setReferenceType(item.optString(REFERENCED));
                    saveItem.setDataType(item.optString(TYPE));
                    referenceIdMap.put(item.optString(MsHashTreeConstants.ID), saveItem);
                }
                if (item.has(MsHashTreeConstants.HASH_TREE)) {
                    referenceIdMap.putAll(this.deepElementRelation(scenario.getId(), item.optJSONArray(MsHashTreeConstants.HASH_TREE)));
                }
            }
        }
        if (MapUtils.isNotEmpty(referenceIdMap)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            UiScenarioReferenceMapper referenceIdMapper = sqlSession.getMapper(UiScenarioReferenceMapper.class);
            for (UiScenarioReference UiScenarioReference : referenceIdMap.values()) {
                UiScenarioReference.setCreateTime(System.currentTimeMillis());
                UiScenarioReference.setCreateUserId(SessionUtils.getUserId());
                referenceIdMapper.insert(UiScenarioReference);
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        } else {
            UiScenarioReference saveItem = new UiScenarioReference();
            saveItem.setId(UUID.randomUUID().toString());
            saveItem.setUiScenarioId(scenario.getId());
            saveItem.setCreateTime(System.currentTimeMillis());
            saveItem.setCreateUserId(SessionUtils.getUserId());
            uiScenarioReferenceMapper.insert(saveItem);
        }
    }

    public Map<String, UiScenarioReference> deepElementRelation(String scenarioId, JSONArray hashTree) {
        Map<String, UiScenarioReference> deepRelations = new HashMap<>();
        if (hashTree != null) {
            for (int index = 0; index < hashTree.length(); index++) {
                JSONObject item = hashTree.optJSONObject(index);
                if (item.has(MsHashTreeConstants.ID) && item.has(REFERENCED)) {
                    UiScenarioReference saveItem = new UiScenarioReference();
                    saveItem.setId(UUID.randomUUID().toString());
                    saveItem.setUiScenarioId(scenarioId);
                    saveItem.setReferenceId(item.optString(MsHashTreeConstants.ID));
                    saveItem.setReferenceType(item.optString(REFERENCED));
                    saveItem.setDataType(item.optString(TYPE));
                    deepRelations.put(item.optString(MsHashTreeConstants.ID), saveItem);
                }
                if (item.has(MsHashTreeConstants.HASH_TREE)) {
                    deepRelations.putAll(this.deepElementRelation(scenarioId, item.optJSONArray(MsHashTreeConstants.HASH_TREE)));
                }
            }
        }
        return deepRelations;
    }

    private void checkAndSetLatestVersion(String refId) {
        extUiScenarioMapper.clearLatestVersion(refId);
        extUiScenarioMapper.addLatestVersion(refId);
    }

    public UiScenarioWithBLOBs buildSaveScenario(SaveUiScenarioRequest request) {
        UiScenarioWithBLOBs scenario = new UiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setCustomNum(request.getCustomNum());
        if (StringUtils.equals(request.getTags(), "[]")) {
            scenario.setTags("");
        } else {
            scenario.setTags(request.getTags());
        }
        scenario.setModuleId(request.getModuleId());
        scenario.setModulePath(request.getModulePath());
        scenario.setLevel(request.getLevel());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setDescription(request.getDescription());
        scenario.setCreateUser(SessionUtils.getUserId());
        scenario.setCommandViewStruct(request.getCommandViewStruct());

        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));

        if (StringUtils.isNotEmpty(request.getStatus())) {
            scenario.setStatus(request.getStatus());
        } else {
            scenario.setStatus(ScenarioStatus.Underway.name());
        }
        if (StringUtils.isNotEmpty(request.getUserId())) {
            scenario.setUserId(request.getUserId());
        } else {
            scenario.setUserId(SessionUtils.getUserId());
        }

        if (StringUtils.isEmpty(request.getModuleId()) || "default-module".equals(request.getModuleId())) {
            replenishScenarioModuleIdPath(request.getProjectId(), uiScenarioModuleMapper, scenario);
        }
        if (StringUtils.isEmpty(request.getVersionId())) {
            scenario.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        } else {
            scenario.setVersionId(request.getVersionId());
        }
        return scenario;
    }

    public void delete(String id) {
        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(id);
        if (scenario == null) {
            return;
        }
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andRefIdEqualTo(scenario.getRefId());
        List<UiScenario> apiScenarios = uiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(s -> {
            //删除定时任务
            scheduleService.deleteByResourceId(s.getId(), ScheduleGroup.UI_SCENARIO_TEST.name());
            uiScenarioMapper.deleteByPrimaryKey(s.getId());
            //删除测试计划关联数据
            TestPlanUiScenarioExample testPlanUiScenarioExample = new TestPlanUiScenarioExample();
            testPlanUiScenarioExample.createCriteria().andUiScenarioIdEqualTo(s.getId());
            testPlanUiScenarioMapper.deleteByExample(testPlanUiScenarioExample);

        });
    }

    public void deleteBatch(List<String> ids) {
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<UiScenario> apiScenarios = uiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiScenario -> this.delete(apiScenario.getId()));
    }

    public void removeToGc(List<String> apiIds) {
        UiScenarioExample scenarioExample = new UiScenarioExample();
        UiScenarioExample.Criteria criteria = scenarioExample.createCriteria();
        criteria.andIdIn(apiIds);
        List<UiScenarioWithBLOBs> list = uiScenarioMapper.selectByExampleWithBLOBs(scenarioExample);

        //先将场景中引用元素的元素对象信息存储起来， 方便后续回显
        Map<String, UiScenarioWithBLOBs> cacheMap = list.stream().collect(Collectors.toConcurrentMap(UiScenarioWithBLOBs::getId, v -> v));
        uiElementReferenceService.prepareRefresh(apiIds, cacheMap);

        //进入回收站
        for (String id : apiIds) {
            UiScenarioWithBLOBs scenario = cacheMap.get(id);
            if (scenario == null) {
                return;
            }
            UiScenarioRequest request = new UiScenarioRequest();
            request.setRefId(scenario.getRefId());
            List<String> scenarioIds = extUiScenarioMapper.selectIdsByQuery(request);
            //将这些场景的定时任务删除掉
            scenarioIds.forEach(scenarioId -> scheduleService.closeByResourceId(scenarioId, ScheduleGroup.UI_SCENARIO_TEST.name()));
            scheduleService.closeByResourceId(id, ScheduleGroup.UI_SCENARIO_TEST.name());
            UiScenarioExampleWithOperation example = new UiScenarioExampleWithOperation();
            example.createCriteria().andRefIdEqualTo(scenario.getRefId());
            example.setOperator(SessionUtils.getUserId());
            example.setOperationTime(System.currentTimeMillis());
            extUiScenarioMapper.removeToGcByExample(example);
        }

        //清除回收站场景的引用关系
        uiElementReferenceService.refreshReferenceByScenarioIds(apiIds);
    }

    public void removeToGcOpt(List<String> apiIds) {
        //进入回收站
        if (apiIds.size() > SPLIT_LIMIT) {
            List<List<String>> parts = Lists.partition(apiIds, SPLIT_LIMIT);
            LogUtil.info("total parts num:" + parts.size());
            for (int i = 0; i < parts.size(); i++) {
                LogUtil.info("current excute num:" + i);
                removeToGc(parts.get(i));
            }
        } else {
            removeToGc(apiIds);
        }
    }

    public void reduction(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ids.forEach(id -> {
            UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(id);
            if (scenario == null) {
                return;
            }
            UiScenarioRequest request = new UiScenarioRequest();
            request.setRefId(scenario.getRefId());
            HashMap<String, List<String>> filters = new HashMap<>();
            filters.put("status", Collections.singletonList(ScenarioStatus.Trash.name()));
            request.setFilters(filters);
            List<String> scenarioIds = extUiScenarioMapper.selectIdsByQuery(request);
            if (CollectionUtils.isNotEmpty(scenarioIds)) {
                extUiScenarioMapper.checkOriginalStatusByIds(scenarioIds);
            } else {
                return;
            }
            //检查原来模块是否还在
            UiScenarioExample example = new UiScenarioExample();
            example.createCriteria().andIdIn(scenarioIds);
            List<UiScenario> scenarioList = uiScenarioMapper.selectByExample(example);
            Map<String, List<UiScenario>> nodeMap = new HashMap<>();
            for (UiScenario ui : scenarioList) {
                String moduleId = ui.getModuleId();
                if (StringUtils.isEmpty(moduleId)) {
                    moduleId = "";
                }
                if (nodeMap.containsKey(moduleId)) {
                    nodeMap.get(moduleId).add(ui);
                } else {
                    List<UiScenario> list = new ArrayList<>();
                    list.add(ui);
                    nodeMap.put(moduleId, list);
                }
            }
            UiScenarioModuleService uiScenarioModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
            for (Map.Entry<String, List<UiScenario>> entry : nodeMap.entrySet()) {
                String nodeId = entry.getKey();
                List<UiScenario> scenariosListItem = entry.getValue();
                Map<String, List<UiScenario>> projectMap = scenariosListItem.stream().collect(Collectors.groupingBy(UiScenario::getProjectId));
                for (Map.Entry<String, List<UiScenario>> projectEntry : projectMap.entrySet()) {
                    String projectId = projectEntry.getKey();
                    List<UiScenario> checkList = projectEntry.getValue();
                    if (StringUtils.isNotEmpty(projectId)) {
                        long nodeCount = uiScenarioModuleService.countById(nodeId);
                        if (nodeCount <= 0) {
                            ModuleNode node = uiScenarioModuleService.getDefaultNode(projectId);
                            for (UiScenario testCase : checkList) {
                                UiScenarioWithBLOBs updateCase = new UiScenarioWithBLOBs();
                                updateCase.setId(testCase.getId());
                                updateCase.setModuleId(node.getId());
                                updateCase.setModulePath("/" + node.getName());
                                uiScenarioMapper.updateByPrimaryKeySelective(updateCase);
                            }
                        }
                    }
                }
            }
            extUiScenarioMapper.reduction(scenarioIds);

            //重置当前恢复场景的引用关系
            saveElementAndScenarioRelation(scenario);
        });
    }

    private void checkNameExist(SaveUiScenarioRequest request) {
        UiScenarioExample example = new UiScenarioExample();
        UiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(request.getName())
                .andProjectIdEqualTo(request.getProjectId())
                .andStatusNotEqualTo("Trash")
                .andIdNotEqualTo(request.getId())
                .andModuleIdEqualTo(request.getModuleId());
        if (uiScenarioMapper.countByExample(example) > 0) {
            String msg = getCurrentMsg(request.getScenarioType(), request.getModulePath(), request.getName());
            MSException.throwException(msg);
        }
    }

    /**
     * 根据类型返回不同的描述信息
     */
    private String getCurrentMsg(String scenarioType, String modulePath, String name) {
        if (StringUtils.isNotBlank(scenarioType) && StringUtils.equalsIgnoreCase(scenarioType, UiScenarioType.CUSTOM_COMMAND.getType())) {
            return Translator.get("automation_command_name_already_exists") + " :" + Translator.get("api_definition_module") + modulePath + " ," + Translator.get("command_name") + " :" + name;
        }
        return Translator.get("automation_name_already_exists") + " :" + Translator.get("api_definition_module") + modulePath + " ," + Translator.get("automation_name") + " :" + name;
    }


    public UiScenarioDTO getNewUiScenario(String id) {
        UiScenarioDTO scenarioWithBLOBs = extUiScenarioMapper.selectById(id);
        if (scenarioWithBLOBs == null) {
            return null;
        }
        if (StringUtils.equals(scenarioWithBLOBs.getScenarioType(), "customCommand") && StringUtils.isNotBlank(scenarioWithBLOBs.getCommandViewStruct())) {
            JSONArray element = new JSONArray(scenarioWithBLOBs.getCommandViewStruct());
            for (int i = 0; i < element.length(); i++) {
                dataFormatting(element.optJSONObject(i));
            }
            scenarioWithBLOBs.setCommandViewStruct(element.toString());
        }

        if (StringUtils.isNotEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            JSONObject element = new JSONObject(scenarioWithBLOBs.getScenarioDefinition());
            convertOldVariables(element);
            dataFormatting(element);
            scenarioWithBLOBs.setScenarioDefinition(element.toString());
        }
        return scenarioWithBLOBs;
    }


    public List<UiScenarioDTO> getUiScenarios(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extUiScenarioMapper.selectIds(ids);
        }
        return new ArrayList<>();
    }

    public List<UiScenarioDTO> getNewUiScenarios(List<String> ids) {
        List<UiScenarioDTO> list = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(item -> {
                UiScenarioDTO dto = this.getNewUiScenario(item);
                list.add(dto);
            });
        }
        return list;
    }

    public byte[] loadFileAsBytes(FileOperationRequest fileOperationRequest) {
        if (fileOperationRequest.getId().contains("/") || fileOperationRequest.getName().contains("/"))
            MSException.throwException(Translator.get("invalid_parameter"));
        File file = new File(FileUtils.BODY_FILE_DIR + "/" + fileOperationRequest.getId() + "_" + fileOperationRequest.getName());
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);) {
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            return bos.toByteArray();
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return null;
    }

    public long countScenarioByProjectID(String projectId) {
        return extUiScenarioMapper.countByProjectID(projectId);
    }

    public List<UiDataCountResult> countRunResultByProjectID(String projectId) {
        return extUiScenarioMapper.countRunResultByProjectID(projectId);
    }

    public List<UiScenario> selectByIds(List<String> ids) {
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return uiScenarioMapper.selectByExample(example);
    }

    public List<UiScenarioWithBLOBs> selectByIdsWithBLOBs(List<String> ids) {
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return uiScenarioMapper.selectByExampleWithBLOBs(example);
    }

    private UiScenarioWithBLOBs importCreate(UiScenarioWithBLOBs request, UiScenarioMapper batchMapper, ExtUiScenarioMapper extUiScenarioMapper,
                                             UiTestImportRequest uiScenarioImportRequest) {
        final UiScenarioWithBLOBs uiScenarioWithBLOBs = new UiScenarioWithBLOBs();
        SaveUiScenarioRequest saveReq = new SaveUiScenarioRequest();
        BeanUtils.copyBean(saveReq, request);
        BeanUtils.copyBean(uiScenarioWithBLOBs, request);
        uiScenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
        uiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        uiScenarioWithBLOBs.setScenarioType(request.getScenarioType());
        if (StringUtils.isEmpty(uiScenarioWithBLOBs.getStatus())) {
            uiScenarioWithBLOBs.setStatus(APITestStatus.Underway.name());
        }
        if (uiScenarioWithBLOBs.getUserId() == null) {
            uiScenarioWithBLOBs.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            uiScenarioWithBLOBs.setUserId(uiScenarioWithBLOBs.getUserId());
        }
        UiScenarioWithBLOBs tempScenario = new UiScenarioWithBLOBs();
        replenishScenarioModuleIdPath(request.getProjectId(), uiScenarioModuleMapper, tempScenario);
        String defaultModuleId = tempScenario.getModuleId();
        List<UiScenario> sameRequest;
        sameRequest = getSameRequest(saveReq, Optional.ofNullable(uiScenarioWithBLOBs.getModuleId()).orElse(defaultModuleId));

        if (CollectionUtils.isEmpty(sameRequest)) {
            sameRequest = getSameRequestById(saveReq.getId(), uiScenarioImportRequest.getProjectId(), Optional.ofNullable(uiScenarioWithBLOBs.getModuleId()).orElse(defaultModuleId));
        }
        if (StringUtils.equals("fullCoverage", uiScenarioImportRequest.getModeId())) {
            _importCreate(sameRequest, batchMapper, uiScenarioWithBLOBs, extUiScenarioMapper, uiScenarioImportRequest);
        } else if (StringUtils.equals("incrementalMerge", uiScenarioImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(sameRequest)) {
                //postman 可能含有前置脚本，接口定义去掉脚本
                uiScenarioWithBLOBs.setOrder(getImportNextOrder(uiScenarioImportRequest.getProjectId()));
                String originId = uiScenarioWithBLOBs.getId();
                uiScenarioWithBLOBs.setId(UUID.randomUUID().toString());
                uiScenarioWithBLOBs.setRefId(uiScenarioWithBLOBs.getId());
                if (StringUtils.isNotEmpty(uiScenarioImportRequest.getVersionId())) {
                    uiScenarioWithBLOBs.setVersionId(uiScenarioImportRequest.getVersionId());
                } else {
                    uiScenarioWithBLOBs.setVersionId(uiScenarioImportRequest.getDefaultVersion());
                }
                uiScenarioWithBLOBs.setLatest(true); // 新增的接口 latest = true
                batchMapper.insert(uiScenarioWithBLOBs);
            }
        } else {
            _importCreate(sameRequest, batchMapper, uiScenarioWithBLOBs, extUiScenarioMapper, uiScenarioImportRequest);
        }

        return uiScenarioWithBLOBs;
    }

    private void _importCreate(List<UiScenario> sameRequest, UiScenarioMapper batchMapper, UiScenarioWithBLOBs uiScenarioWithBLOBs, ExtUiScenarioMapper extUiScenarioMapper, UiTestImportRequest uiScenarioImportRequest) {

        if (CollectionUtils.isEmpty(sameRequest)) { // 没有这个接口 新增
            uiScenarioWithBLOBs.setId(UUID.randomUUID().toString());
            uiScenarioWithBLOBs.setRefId(uiScenarioWithBLOBs.getId());
            if (StringUtils.isNotEmpty(uiScenarioImportRequest.getVersionId())) {
                uiScenarioWithBLOBs.setVersionId(uiScenarioImportRequest.getVersionId());
            } else {
                uiScenarioWithBLOBs.setVersionId(uiScenarioImportRequest.getDefaultVersion());
            }
            uiScenarioWithBLOBs.setLatest(true); // 新增接口 latest = true

            uiScenarioWithBLOBs.setOrder(getImportNextOrder(uiScenarioImportRequest.getProjectId()));
            batchMapper.insert(uiScenarioWithBLOBs);
        } else { //如果存在则修改
            if (StringUtils.isEmpty(uiScenarioImportRequest.getUpdateVersionId())) {
                uiScenarioImportRequest.setUpdateVersionId(uiScenarioImportRequest.getDefaultVersion());
            }
            //UI只有覆盖，没有版本区分
            sameRequest.forEach(existScenario->{
                uiScenarioWithBLOBs.setId(existScenario.getId());
                uiScenarioWithBLOBs.setStatus(existScenario.getStatus());
                uiScenarioWithBLOBs.setOriginalState(existScenario.getOriginalState());
                uiScenarioWithBLOBs.setNum(existScenario.getNum()); //id 不变
                uiScenarioWithBLOBs.setRefId(existScenario.getRefId());
                uiScenarioWithBLOBs.setVersionId(uiScenarioImportRequest.getUpdateVersionId());
                uiScenarioWithBLOBs.setOrder(existScenario.getOrder());
                uiScenarioWithBLOBs.setLatest(true);
                if (!StringUtils.equalsIgnoreCase(uiScenarioImportRequest.getPlatform(), ApiImportPlatform.Metersphere.name())) {
                    uiScenarioWithBLOBs.setTags(existScenario.getTags()); // 其他格式 tag 不变，MS 格式替换
                }
                uiScenarioMapper.updateByPrimaryKeyWithBLOBs(uiScenarioWithBLOBs);
            });
        }
    }

    private List<UiScenario> getSameRequest(SaveUiScenarioRequest request, String defaultModuleId) {
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName()).andStatusNotEqualTo("Trash")
                .andModuleIdEqualTo(Optional.ofNullable(request.getModuleId()).orElse(defaultModuleId))
                .andProjectIdEqualTo(request.getProjectId());
        return uiScenarioMapper.selectByExample(example);
    }

    private List<UiScenario> getSameRequestById(String id, String projectId, String defaultModuleId) {
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andStatusNotEqualTo("Trash")
                .andModuleIdEqualTo(defaultModuleId)
                .andProjectIdEqualTo(projectId)
                .andIdEqualTo(id);
        return uiScenarioMapper.selectByExample(example);
    }


    private void editScenario(UiTestImportRequest request, ScenarioImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UiScenarioMapper batchMapper = sqlSession.getMapper(UiScenarioMapper.class);
        ExtUiScenarioMapper extUiScenarioMapper = sqlSession.getMapper(ExtUiScenarioMapper.class);
        List<UiScenarioWithBLOBs> data = apiImport.getData();
        currentScenarioOrder.remove();
        int num = 0;

        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }
        String defaultVersion = extProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        for (int i = 0; i < data.size(); i++) {
            UiScenarioWithBLOBs item = data.get(i);
            item.setScenarioType(request.getScenarioType());
            if (StringUtils.isBlank(item.getModuleId()) || "default-module".equals(item.getModuleId())) {
                replenishScenarioModuleIdPath(request.getProjectId(), uiScenarioModuleMapper, item);
            }

            if (StringUtils.isBlank(item.getCreateUser())) {
                item.setCreateUser(SessionUtils.getUserId());
                item.setPrincipal(SessionUtils.getUserId());
            }
            if (StringUtils.isNotEmpty(item.getName()) && item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            item.setNum(num);

            if (BooleanUtils.isFalse(request.getOpenCustomNum())) {
                // 如果未开启，即使有自定值也直接覆盖
                item.setCustomNum(String.valueOf(num));
            } else {
                if (StringUtils.isBlank(item.getCustomNum())) {
                    item.setCustomNum(String.valueOf(num));
                }
            }
            num++;
            if (StringUtils.isBlank(item.getId())) {
                item.setId(UUID.randomUUID().toString());
            }
            // 导入之后刷新latest
            importCreate(item, batchMapper, extUiScenarioMapper, request);
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void replenishScenarioModuleIdPath(String request, UiScenarioModuleMapper uiScenarioModuleMapper, UiScenarioWithBLOBs item) {
        UiScenarioModuleExample example = new UiScenarioModuleExample();
        example.createCriteria().andProjectIdEqualTo(request).andNameEqualTo("未规划场景");
        List<UiScenarioModule> modules = uiScenarioModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(modules)) {
            item.setModuleId(modules.get(0).getId());
            item.setModulePath(modules.get(0).getName());
        }
    }

    private Long getImportNextOrder(String projectId) {
        Long order = currentScenarioOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extUiScenarioMapper::getLastOrder);
        }
        order = (order == null ? 0 : order) + ServiceUtils.ORDER_STEP;
        currentScenarioOrder.set(order);
        return order;
    }

    public ScenarioImport scenarioImport(MultipartFile file, UiTestImportRequest request) {
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        ScenarioParser runService = ScenarioImportParserFactory.getImportParser(request.getPlatform());
        ScenarioImport scenarioImport = null;
        Optional.ofNullable(file)
                .ifPresent(item -> request.setFileName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."))));
        try {
            scenarioImport = (ScenarioImport) Objects.requireNonNull(runService).parse(file == null ? null : file.getInputStream(), request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }
        if (scenarioImport != null) {
            editScenario(request, scenarioImport);
            if (CollectionUtils.isNotEmpty(scenarioImport.getData())) {
                List<String> names = scenarioImport.getData().stream().map(UiScenarioWithBLOBs::getName).collect(Collectors.toList());
                List<String> ids = scenarioImport.getData().stream().map(UiScenarioWithBLOBs::getId).collect(Collectors.toList());
                request.setName(String.join(",", names));
                request.setId(JSON.toJSONString(ids));
            }
        }
        return scenarioImport;
    }


    private List<UiScenarioWithBLOBs> getExportResult(UiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        List<UiScenarioWithBLOBs> apiScenarioWithBLOBs = new ArrayList<>();
        if (request.getIds().size() > SPLIT_LIMIT) {
            List<List<String>> parts = Lists.partition(request.getIds(), 10);
            UiScenarioExample example;
            List<UiScenarioWithBLOBs> list;
            for (int i = 0; i < parts.size(); i++) {
                example = new UiScenarioExample();
                example.createCriteria().andIdIn(parts.get(i));
                list = extUiScenarioMapper.selectByExampleWithBLOBs(example);
                apiScenarioWithBLOBs.addAll(list);
            }
        } else {
            UiScenarioExample example = new UiScenarioExample();
            example.createCriteria().andIdIn(request.getIds());
            apiScenarioWithBLOBs = uiScenarioMapper.selectByExampleWithBLOBs(example);
        }
        return apiScenarioWithBLOBs;
    }

    public byte[] export(UiScenarioBatchRequest request) {
        List<UiScenarioWithBLOBs> exportResult = getExportResult(request);
        List<SideDTO.TestsDTO> tests = new ArrayList<>();

        exportResult.forEach(result -> buildExportTestsDTO(tests, result));

        SideDTO sideDTO = new SideDTO();
        sideDTO.setId(request.getProjectId());
        sideDTO.setName(projectMapper.selectByPrimaryKey(request.getProjectId()).getName());
        sideDTO.setVersion("2.0");
        sideDTO.setTests(tests);
        sideDTO.setPlugins(Collections.emptyList());
        List<SideDTO.SuitesDTO> suites = new ArrayList<>();
        SideDTO.SuitesDTO suitesDTO = new SideDTO.SuitesDTO();
        suitesDTO.setId(UUID.randomUUID().toString());
        suitesDTO.setName("Default Suite");
        suitesDTO.setParallel(false);
        suitesDTO.setPersistSession(false);
        suitesDTO.setTimeout(300);
        if (CollectionUtils.isNotEmpty(exportResult)) {
            suitesDTO.setTests(Collections.singletonList(exportResult.get(0).getId()));
        }
        suites.add(suitesDTO);
        sideDTO.setSuites(suites);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            //兼容ide 不能含null值的属性 否则导入IDE失败
            return objectMapper.writeValueAsString(sideDTO).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //        return JSON.toJSONString(sideDTO).getBytes(StandardCharsets.UTF_8);
    }

    private void buildExportTestsDTO(List<SideDTO.TestsDTO> tests, UiScenarioWithBLOBs result) {
        String scenarioDefinition = result.getScenarioDefinition();
        LinkedList<MsTestElement> hashTree = UiGenerateHashTreeUtil.getHashTreeByScenario(scenarioDefinition);

        // 将hash递归展开成 MsUiCommand数组
        LinkedList<MsUiCommand> msCommands = new LinkedList<>();
        expandMsUiCommands(hashTree, msCommands);

        SideDTO.TestsDTO testsDTO = new SideDTO.TestsDTO();
        testsDTO.setId(UUID.randomUUID().toString());
        ParameterConfig parameterConfig = new ParameterConfig();
        //告诉转换器是导出操作 控制次数循环，while，dowhile等不添加多余的pause指令进行导出
        parameterConfig.setOperating(true);
        //设置全局配置
        UiGlobalConfigUtil.setConfig(parameterConfig);
        testsDTO.setCommands(MsWebDriverSampler.parseMsCommands(msCommands, parameterConfig));
        //及时清除配置
        UiGlobalConfigUtil.removeConfig();
        testsDTO.setName(result.getName());
        testsDTO.setId(result.getId());
        tests.add(testsDTO);

        SideDTO.TestsDTO.CommandsDTO tmp = null;
        for (int i = testsDTO.getCommands().size() - 1; i > -1; i--) {
            SideDTO.TestsDTO.CommandsDTO item = testsDTO.getCommands().get(i);

            // IDE里else和repeat之前没有end，导出时去掉
            if (tmp != null && StringUtils.containsAnyIgnoreCase(tmp.getCommand(), "else", "repeat")
                    && StringUtils.containsAnyIgnoreCase(item.getCommand(), "end")) {
                testsDTO.getCommands().remove(i);
                tmp = null;
                continue;
            }

            if (StringUtils.equalsAnyIgnoreCase(item.getCommand(), "foreach", "assert", "verify")) {
                parseTargetMsVariable(item);
            }

            if (!item.isEnable()) {
                item.setCommand("//" + item.getCommand());
            }
            tmp = item;
        }
        Map<String, CommonCommand> uiCommandDefinitionMap = (Map<String, CommonCommand>) CommonBeanFactory.getBean("uiCommandMap");
        Iterator<SideDTO.TestsDTO.CommandsDTO> iterator = testsDTO.getCommands().iterator();
        while (iterator.hasNext()) {
            SideDTO.TestsDTO.CommandsDTO item = iterator.next();
            if (uiCommandDefinitionMap.get(item.getCommand()) != null && !uiCommandDefinitionMap.get(item.getCommand()).isExport()) {
                iterator.remove();
                continue;
            }
            if (!item.isEnable() || (uiCommandDefinitionMap.get(item.getCommand()) != null && uiCommandDefinitionMap.get(item.getCommand()).isExportWithNote())) {
                item.setCommand("//" + item.getCommand());
            }

        }
    }

    private void parseTargetMsVariable(SideDTO.TestsDTO.CommandsDTO item) {
        String target = item.getTarget();
        if (StringUtils.isNotBlank(target) && target.startsWith("${")) {
            // 导出去掉 ${}
            item.setTarget(target.substring(2, target.length() - 1));
        }
    }

    /**
     * 将hash递归展开成 MsUiCommand数组
     *
     * @param hashTree
     * @param msCommands
     */
    private void expandMsUiCommands(LinkedList<MsTestElement> hashTree, LinkedList<MsUiCommand> msCommands) {
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(item -> {
                if (item instanceof MsUiScenario) {
                    List<ScenarioVariable> variables = ((MsUiScenario) item).getVariables();
                    if (CollectionUtils.isNotEmpty(variables)) {
                        LinkedList converted = variables.stream().filter(t->StringUtils.isNotBlank(t.getName())).map(UiGenerateHashTreeUtil::variableToMsUiCommand).collect(Collectors.toCollection(LinkedList::new));
                        if (CollectionUtils.isNotEmpty(converted)) {
                            int addIndex = getAddIndex(msCommands);
                            MsUiCommand command = new MsUiCommand();
                            //导出环境变量的时候，对应的指令为 cmdExtractElement
                            command.setCommand("cmdExtraction");
                            command.setCommandType(CommandType.COMMAND_TYPE_COMBINATION_PROXY);
                            command.setViewType("dataExtraction");
                            command.setHashTree(converted);
                            msCommands.add(addIndex,command);
                        }
                    }
                    int startIndex = msCommands.size();
                    expandMsUiCommands(item.getHashTree(), msCommands);
                    if (!item.isEnable()) {
                        // 如果导入的场景是disable的，则其下所有的指令变成disable
                        for (int i = startIndex; i < msCommands.size(); i++) {
                            msCommands.get(i).setEnable(false);
                        }
                    }
                } else if (item instanceof MsUiCommand) {
                    msCommands.add((MsUiCommand) item);
                }
            });
        }
    }

    private static int getAddIndex(LinkedList<MsUiCommand> msCommands) {
        int addIndex = 0;
        for (int i = 0; i < msCommands.size(); i++) {
            if (!StringUtils.equals(msCommands.get(i).getCommand(),"cmdExtraction")) {
                addIndex = i;
                break;
            }
        }
        return addIndex;
    }

    public void removeToGcByBatch(UiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));

        this.removeToGcOpt(request.getIds());
    }

    public void deleteBatchByCondition(UiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ids.forEach(id -> {
            UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(id);
            if (scenario == null) {
                return;
            }
            scheduleService.deleteByResourceId(id, ScheduleGroup.UI_SCENARIO_TEST.name());
            UiScenarioExample example = new UiScenarioExample();
            example.createCriteria().andRefIdEqualTo(scenario.getRefId());
            List<UiScenario> apiScenarios = uiScenarioMapper.selectByExample(example);
            List<String> apiIds = apiScenarios.stream().map(UiScenario::getId).collect(Collectors.toList());
            this.deleteBatch(apiIds);
        });
    }

    public List<UiScenarioWithBLOBs> listWithIds(UiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        return extUiScenarioMapper.listWithIds(request.getIds());
    }

    public String getLogDetails(String id) {
        UiScenarioWithBLOBs bloBs = uiScenarioMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, AutomationReference.automationColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            UiScenarioExample example = new UiScenarioExample();
            example.createCriteria().andIdIn(ids);
            List<UiScenario> definitions = uiScenarioMapper.selectByExample(example);
            List<String> names = definitions.stream().map(UiScenario::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), definitions.get(0).getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void batchCopy(UiScenarioBatchRequest request) {
        //默认场景类型
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        List<UiScenarioDTO> apiScenarioList = extUiScenarioMapper.simpleSelectIds(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UiScenarioMapper mapper = sqlSession.getMapper(UiScenarioMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extUiScenarioMapper::getLastOrder);
        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < apiScenarioList.size(); i++) {
                UiScenarioWithBLOBs api = apiScenarioList.get(i);
                api.setId(UUID.randomUUID().toString());
                api.setName(ServiceUtils.getCopyName(api.getName()));
                api.setModuleId(request.getModuleId());
                api.setModulePath(request.getModulePath());
                api.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                api.setScenarioType(request.getScenarioType());
                api.setCreateTime(System.currentTimeMillis());
                api.setUpdateTime(System.currentTimeMillis());
                api.setRefId(api.getId());
                api.setNum(nextNum++);
                api.setCustomNum(String.valueOf(api.getNum()));
                //重置UserId
                api.setUserId(SessionUtils.getUserId());
                api.setCreateUser(SessionUtils.getUserId());
                mapper.insert(api);
                //处理场景 指令的引用关系
                saveApiAndScenarioRelation(api);
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void batchCopyOpt(UiScenarioBatchRequest request) {
        //默认场景类型
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
            request.getCondition().setScenarioType(UiScenarioType.SCENARIO.getType());
        } else {
            request.getCondition().setScenarioType(request.getScenarioType());
        }

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        if (ids.size() > SPLIT_LIMIT) {
            List<List<String>> parts = Lists.partition(ids, SPLIT_LIMIT);
            LogUtil.info("total parts num:" + parts.size());
            for (int i = 0; i < parts.size(); i++) {
                LogUtil.info("current excute num:" + i);
                excuteBatchCopy(request, parts.get(i));
            }
        } else {
            excuteBatchCopy(request, ids);
        }
    }

    private void excuteBatchCopy(UiScenarioBatchRequest request, List<String> ids) {
        List<UiScenarioDTO> apiScenarioList = extUiScenarioMapper.simpleSelectIds(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UiScenarioMapper mapper = sqlSession.getMapper(UiScenarioMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extUiScenarioMapper::getLastOrder);
        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < apiScenarioList.size(); i++) {
                UiScenarioWithBLOBs api = apiScenarioList.get(i);
                api.setId(UUID.randomUUID().toString());
                api.setName(ServiceUtils.getCopyName(api.getName()));
                api.setModuleId(request.getModuleId());
                api.setModulePath(request.getModulePath());
                api.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                api.setScenarioType(request.getScenarioType());
                api.setCreateTime(System.currentTimeMillis());
                api.setUpdateTime(System.currentTimeMillis());
                api.setRefId(api.getId());
                api.setNum(nextNum++);
                api.setCustomNum(String.valueOf(api.getNum()));
                //重置UserId
                api.setUserId(SessionUtils.getUserId());
                api.setCreateUser(SessionUtils.getUserId());
                mapper.insertSelective(api);
                //处理场景 指令的引用关系
                saveApiAndScenarioRelation(api);
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<UiScenario> getScenarioCaseByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            UiScenarioExample example = new UiScenarioExample();
            example.createCriteria().andIdIn(ids);
            return uiScenarioMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public long countExecuteTimesByProjectID(String projectId) {
        Long result = extUiScenarioMapper.countExecuteTimesByProjectID(projectId);
        if (result == null) {
            return 0;
        } else {
            return result.longValue();
        }
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(UiScenarioWithBLOBs.class, UiScenarioMapper.class,
                extUiScenarioMapper::selectProjectIds,
                extUiScenarioMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(UiResetOrderRequestExt request) {
        updateOrderFieldWithType(request, UiScenarioWithBLOBs.class,
                uiScenarioMapper::selectByPrimaryKey,
                uiScenarioMapper::updateByPrimaryKeySelective);
    }

    /**
     * @param request
     * @param clazz
     * @param selectByPrimaryKeyFunc
     * @param updateByPrimaryKeySelectiveFuc
     * @param <T>
     */
    public <T> void updateOrderFieldWithType(UiResetOrderRequestExt request, Class<T> clazz,
                                             Function<String, T> selectByPrimaryKeyFunc,
                                             Consumer<T> updateByPrimaryKeySelectiveFuc) {
        Long order;
        Long lastOrPreOrder;
        String scenarioType = StringUtils.isNotEmpty(request.getScenarioType())
                ? request.getScenarioType() : UiScenarioType.SCENARIO.getType();
        try {
            Method getOrder = clazz.getMethod("getOrder");
            Method setId = clazz.getMethod("setId", String.class);
            Method setOrder = clazz.getMethod("setOrder", Long.class);

            // 获取移动的参考对象
            T target = selectByPrimaryKeyFunc.apply(request.getTargetId());
            Long targetOrder = (Long) getOrder.invoke(target);

            if (request.getMoveMode().equals(ResetOrderRequest.MoveMode.AFTER.name())) {
                // 追加到参考对象的之后
                order = targetOrder - ServiceUtils.ORDER_STEP;
                // ，因为是降序排，则查找比目标 order 小的一个order
                lastOrPreOrder = extUiScenarioMapper.getPreOrderWithType(request.getGroupId(), targetOrder, scenarioType);
            } else {
                // 追加到前面
                order = targetOrder + ServiceUtils.ORDER_STEP;
                // 因为是降序排，则查找比目标 order 更大的一个order
                lastOrPreOrder = extUiScenarioMapper.getLastOrderWithType(request.getGroupId(), targetOrder, scenarioType);
            }
            if (lastOrPreOrder != null) {
                // 如果不是第一个或最后一个则取中间值
                order = (targetOrder + lastOrPreOrder) / 2;
            }

            // 更新order值
            T updateObj = clazz.newInstance();
            setId.invoke(updateObj, request.getMoveId());
            setOrder.invoke(updateObj, order);
            updateByPrimaryKeySelectiveFuc.accept(updateObj);
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("更新 order 字段失败");
        }
    }


    public void setScenarioEnvGroupIdNull(String envGroupId) {
        extUiScenarioMapper.setScenarioEnvGroupIdNull(envGroupId);
    }

    public List<UiScenarioDTO> getUiScenarioVersions(String scenarioId) {
        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(scenarioId);
        if (scenario == null) {
            return new ArrayList<>();
        }
        UiScenarioRequest request = new UiScenarioRequest();
        request.setRefId(scenario.getRefId());
        return this.list(request);
    }

    public UiScenarioDTO getUiScenarioByVersion(String refId, String versionId) {
        UiScenarioRequest request = new UiScenarioRequest();
        request.setRefId(refId);
        request.setVersionId(versionId);
        List<UiScenarioDTO> list = this.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public void deleteUiScenarioByVersion(String refId, String version) {
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(version);
        uiScenarioMapper.deleteByExample(example);
        checkAndSetLatestVersion(refId);
    }

    public List<String> getProjects(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extUiScenarioMapper.selectIdsByQuery(query));

        List<String> ids = request.getIds();
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<UiScenarioWithBLOBs> apiScenarios = uiScenarioMapper.selectByExampleWithBLOBs(example);

        List<String> strings = new LinkedList<>();
        apiScenarios.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                ScenarioEnv env = getUiScenarioEnv(item.getScenarioDefinition());
                if (!strings.contains(item.getProjectId())) {
                    strings.add(item.getProjectId());
                }
                if (env != null && CollectionUtils.isNotEmpty(env.getProjectIds())) {
                    env.getProjectIds().forEach(projectId -> {
                        if (!strings.contains(projectId)) {
                            strings.add(projectId);
                        }
                    });
                }
            }
        });
        return strings;
    }

    public String debug(RunDefinitionRequest request) {
        return uiScenarioExecuteService.debug(request);
    }

    public void deleteByNodeIds(List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return;
        }
        //删除模块后 模块中的场景都进入到 回收站
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andModuleIdIn(nodeIds);

        List<UiScenario> preRecycle = uiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(preRecycle)) {
            return;
        }
        String projectId = preRecycle.stream().findFirst().get().getProjectId();
        UiScenarioModuleExample searchByProjectId = new UiScenarioModuleExample();
        searchByProjectId.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo("未规划场景");

        List<UiScenarioModule> modules = uiScenarioModuleMapper.selectByExample(searchByProjectId);
        if (CollectionUtils.isEmpty(modules)) {
            return;
        }
        preRecycle.forEach(scenario -> {
            scenario.setModuleId(modules.get(0).getId());
            scenario.setModulePath(modules.get(0).getName());
            scenario.setDeleteUserId(SessionUtils.getUserId());
            scenario.setDeleteTime(System.currentTimeMillis());
            //进入回收站
            scenario.setStatus("Trash");
            uiScenarioMapper.updateByPrimaryKey(scenario);
        });
        removeRef(preRecycle.stream().map(UiScenario::getRefId).collect(Collectors.toList()));

        //清除模块下场景的元素引用关系
        uiElementReferenceService.refreshReferenceByScenarioIds(preRecycle.stream().map(UiScenario::getId).collect(Collectors.toList()));
    }

    public void removeRef(List<String> refIds) {
        for (String refId : refIds) {
            UiScenarioRequest request = new UiScenarioRequest();
            request.setRefId(refId);
            List<String> scenarioIds = extUiScenarioMapper.selectIdsByQuery(request);
            if (CollectionUtils.isNotEmpty(scenarioIds)) {
                //将这些场景的定时任务删除掉
                scenarioIds.forEach(scenarioId -> scheduleService.deleteByResourceId(scenarioId, ScheduleGroup.UI_SCENARIO_TEST.name()));
            }
        }
    }

    private JSONObject setRefScenario(JSONObject element) {
        boolean enable = !element.has(ENABLE) || element.optBoolean(ENABLE);
        if (!element.has(VARIABLE_ENABLE) && (!element.has(SMART_VARIABLE_ENABLE) || ( element.has(SMART_VARIABLE_ENABLE) && element.get(SMART_VARIABLE_ENABLE) == "false"))) {
            element.put(VARIABLE_ENABLE, true);
        } else if(!element.has(VARIABLE_ENABLE) && ( element.has(SMART_VARIABLE_ENABLE) && element.get(SMART_VARIABLE_ENABLE) == "true")){
            element.put(VARIABLE_ENABLE, false);
        }

        if (!element.has(SMART_VARIABLE_ENABLE)) {
            element.put(SMART_VARIABLE_ENABLE, false);
        }

        if (!element.has(ENV_ENABLE) || element.get(ENV_ENABLE)=="null") {
            element.put(ENV_ENABLE, false);
        }

        String resourceId = element.optString(RESOURCE_ID);
        UiScenarioDTO scenarioWithBLOBs = extUiScenarioMapper.selectById(resourceId);
        //优化查询
        if (scenarioWithBLOBs == null) {
            resourceId = element.optString(ID);
            scenarioWithBLOBs = extUiScenarioMapper.selectById(resourceId);
        }
        String scenarioType = element.optString(TYPE);
        if (scenarioWithBLOBs != null && StringUtils.isNotEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            boolean environmentEnable = element.optBoolean(ENV_ENABLE);
            boolean variableEnable = element.optBoolean(VARIABLE_ENABLE);
            boolean smartVariableEnable = element.optBoolean(SMART_VARIABLE_ENABLE);
            //处理 环境引用信息
            String environmentJson = scenarioWithBLOBs.getEnvironmentJson();

            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                //diff 引用 入参出参 注释等不更新
                element = directedUpdate(element, scenarioWithBLOBs);

                if (StringUtils.isNotBlank(environmentJson)) {
                    element.put("environmentJson", environmentJson);
                }
                //element = new JSONObject(scenarioWithBLOBs.getScenarioDefinition());
                if (StringUtils.isNotBlank(scenarioType)) {
                    element.put(TYPE, scenarioType);
                    element.put("clazzName", getClazzNameByType(scenarioType));
                }
                if (StringUtils.isNotBlank(resourceId)) {
                    element.put(RESOURCE_ID, resourceId);
                }
                element.put(REFERENCED, REF);
                if (!element.optBoolean("nameEdited")) {
                    element.put(NAME, scenarioWithBLOBs.getName());
                }
                if (!element.optBoolean("descEdited")) {
                    element.put(DESCRIPTION, scenarioWithBLOBs.getDescription());
                }

                doFillRefHasTree(element.optJSONArray(HASH_TREE), element.optBoolean(ENABLE) && enable);
            }
            //element.put(ID, scenarioWithBLOBs.getId());
            //重置ID
            if (element.has(ID) && element.has(RESOURCE_ID) && element.optString(ID).equals(element.optString(RESOURCE_ID))) {
                element.put(ID, UUID.randomUUID().toString());
            }
            element.put(ENV_ENABLE, environmentEnable);
            element.put(VARIABLE_ENABLE, variableEnable);
            element.put(SMART_VARIABLE_ENABLE, smartVariableEnable);
            this.setElement(element, scenarioWithBLOBs.getNum(), enable, scenarioWithBLOBs.getVersionName(), scenarioWithBLOBs.getVersionEnable());
        } else {
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                element.put(ENABLE, enable);
            }
            element.put(NUM, "");
        }
        return element;
    }

    /**
     * 定向更新
     *
     * @param origin
     * @param scenarioWithBLOBs
     */
    private JSONObject directedUpdate(JSONObject origin, UiScenarioDTO scenarioWithBLOBs) {
        JSONObject latest = new JSONObject(scenarioWithBLOBs.getScenarioDefinition());
        deepMerge(origin, latest);
        return latest;
    }

    /**
     * 深度拷贝
     * Merge "source" into "target". If fields have equal name, merge them recursively.
     *
     * @return the merged object (target).
     */
    List<String> handleKeyList = Lists.newArrayList("id", "@json_id", "resourceId", "variables", "outputVariables", "description", "referenced", "hashTree", "preCommands", "postCommands", "originValue", "nameEdited", "descEdited", "environmentEnable", "variableEnable", "smartVariableEnable");

    /**
     * 引用DIFF 操作
     *
     * @param source
     * @param target
     * @return
     */
    public void deepMerge(JSONObject source, JSONObject target) {
        for (String key : JSONObject.getNames(source)) {
            if (!handleKeyList.contains(key)) {
                continue;
            }
            //填充对应内容到 新模板
            Object value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.put(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value instanceof JSONObject valueJson) {
                    deepMerge(valueJson, target.getJSONObject(key));
                } else if (value instanceof JSONArray oArray && (HASH_TREE.equals(key) || "preCommands".equals(key) || "postCommands".equals(key))) {
                    JSONArray tArray = target.optJSONArray(key);
                    if (oArray.length() > 0 && tArray != null && tArray.length() > 0) {
                        for (int i = 0; i < tArray.length(); i++) {
                            JSONObject jsonObject = tArray.optJSONObject(i);
                            String id = jsonObject.optString(ID);
                            String resourceId = jsonObject.optString(RESOURCE_ID);
                            List<JSONObject> list = Lists.newArrayList();
                            for (int j = 0; j < oArray.length(); j++) {
                                if (id.equals(oArray.optJSONObject(j).optString(ID)) || resourceId.equals(oArray.optJSONObject(j).optString(RESOURCE_ID))) {
                                    list.add(oArray.optJSONObject(j));
                                }
                            }
                            if (CollectionUtils.isNotEmpty(list)) {
                                deepMerge(list.get(0), tArray.optJSONObject(i));
                            }
                        }
                    }
                } else if (VARIABLES.equals(key) && UiScenarioType.SCENARIO.getType().equals(source.get(TYPE))) {
                    value = target.get(key);
                    target.put(key, value);
                } else if (VARIABLES.equals(key) && UiScenarioType.CUSTOM_COMMAND.getType().equals(source.get(TYPE))) {
                    // 使用target 中的变量 替换 引用的变量（名称、类型、是否必填、原始值）  不替换描述 值等
                    JSONArray targetArr = target.optJSONArray(key);
                    //用新的去更新原始的
                    JSONArray arr = this.diffInnerVariables(value, targetArr);
                    target.put(key, arr);
                } else if ("outputVariables".equals(key) && UiScenarioType.CUSTOM_COMMAND.getType().equals(source.get(TYPE))) {
                    JSONArray targetArr = target.optJSONArray(key);
                    JSONArray valueArr = source.optJSONArray(key);
                    try {
                        if (targetArr == null) {
                            String str = target.optString(key);
                            if (StringUtils.isNotBlank(str)) {
                                targetArr = new JSONArray(str);
                            }
                        }
                        if (valueArr == null) {
                            String str = source.optString(key);
                            if (StringUtils.isNotBlank(str)) {
                                valueArr = new JSONArray(str);
                            }
                        }
                    } catch (Exception e) {
                    }
                    //用新的去更新原始的
                    JSONArray arr = this.diffOutputVariables(valueArr, targetArr);
                    target.put(key, arr);
                } else {
                    value = target.get(key);
                    target.put(key, value);
                }
            }
        }
    }

    /**
     * diff 出参
     */
    private JSONArray diffOutputVariables(JSONArray value, JSONArray targetArr) {
        if (value == null) {
            return targetArr;
        }
        if (value.length() <= 0) {
            return targetArr;
        }
        if (targetArr == null || targetArr.length() <= 0) {
            return targetArr;
        }
        //根据 名称 生成 检索map
        Map<String, JSONObject> cache = getOriginVarsMap(value);
        JSONArray results = new JSONArray();
        //都不为空 diff by name
        for (int i = 0; i < targetArr.length(); i++) {
            JSONObject item = targetArr.optJSONObject(i);
            JSONObject pre = cache.get(item.optString("name"));
            if (pre != null) {
                //pre.put("description", item.optString("description"));
                pre.put("commandType", item.optString("commandType"));
                pre.put("position", item.optString("position"));
                pre.put("type", item.optString("type"));
                results.put(pre);
            } else {
                results.put(item);
            }
        }
        return results;
    }

    /**
     * 变量diff 操作
     */
    private JSONArray diffInnerVariables(Object value, JSONArray targetArr) {
        if (!(value instanceof JSONArray)) {
            return targetArr;
        }

        JSONArray origin = (JSONArray) value;
        if (origin.length() <= 0) {
            return targetArr;
        }
        if (targetArr == null || targetArr.length() <= 0) {
            return targetArr;
        }
        //根据 名称 生成 检索map
        Map<String, JSONObject> cache = getOriginVarsMap(origin);
        JSONArray results = new JSONArray();
        //都不为空 diff by name
        for (int i = 0; i < targetArr.length(); i++) {
            JSONObject item = targetArr.optJSONObject(i);
            JSONObject pre = cache.get(item.optString("name"));
            if (pre != null) {
                //pre.put("description", item.optString("description"));
                pre.put("type", item.optString("type"));
                pre.put("originValue", item.optString("originValue"));
                pre.put("enable", pre.optBoolean("enable"));
                pre.put("required", item.optBoolean("required"));
                results.put(pre);
            } else {
                results.put(item);
            }
        }
        return results;
    }

    private Map<String, JSONObject> getOriginVarsMap(JSONArray origin) {
        Map<String, JSONObject> map = Maps.newConcurrentMap();
        for (int j = 0; j < origin.length(); j++) {
            JSONObject pre = origin.optJSONObject(j);
            if (StringUtils.isNotBlank(pre.optString("name"))) {
                map.put(pre.optString("name"), pre);
            }
        }
        return map;
    }


    /**
     * @param oArray 最新的
     * @param tArray 目标显示的
     */
    private void diffHashTree(JSONArray oArray, JSONArray tArray) {
        //找出 删除的
        List<Integer> toRemove = Lists.newArrayList();
        List<Integer> toAdd = Lists.newArrayList();
        for (int i = 0; i < tArray.length(); i++) {
            JSONObject jsonObject = tArray.optJSONObject(i);
            String id = jsonObject.optString("id");
            String resourceId = jsonObject.optString("resourceId");
            List<JSONObject> list = oArray.toList().stream().map(v -> (JSONObject) v).filter(v -> id.equals(v.optString("id")) || resourceId.equals(v.optString("resourceId"))).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                toRemove.add(i);
            }
        }
        toRemove.forEach(tArray::remove);

        //找出 新增的
        for (int i = 0; i < oArray.length(); i++) {
            JSONObject jsonObject = oArray.optJSONObject(i);
            String id = jsonObject.optString("id");
            String resourceId = jsonObject.optString("resourceId");
            List<JSONObject> list = oArray.toList().stream().map(v -> (JSONObject) v).filter(v -> id.equals(v.optString("id")) || resourceId.equals(v.optString("resourceId"))).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                toAdd.add(i);
            }
        }
        toAdd.forEach(tArray::put);
    }

    private String getClazzNameByType(String scenarioType) {
        if (StringUtils.equalsIgnoreCase(scenarioType, UiScenarioType.CUSTOM_COMMAND.getType())) {
            return "io.metersphere.hashtree.MsUiCustomCommand";
        }

        return "io.metersphere.hashtree.MsUiScenario";
    }

    private void doFillRefHasTree(JSONArray hashTree, boolean enable) {
        if (hashTree == null || hashTree.length() <= 0) {
            return;
        }
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            element.put(ENABLE, element.optBoolean(ENABLE) && enable);
            doFillRefHasTree(element.optJSONArray(HASH_TREE), element.optBoolean(ENABLE) && enable);
        }
    }

    public void dataFormatting(JSONArray hashTree, Boolean enable) {
        for (int i = 0; hashTree != null && i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            boolean tempEnable = element.has(ENABLE) ? element.optBoolean(ENABLE) : true;
            if (enable != null) {
                tempEnable = enable;
            }
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                boolean stop = element.has(ENABLE) ? element.optBoolean(ENABLE) : true;
                element.put(ENABLE, stop && tempEnable);
            }
            if (element.has("variables")) {
                convertOldVariables(element);
            }
            if (element.has(HASH_TREE)) {
                JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                dataFormatting(elementJSONArray, tempEnable);
            }
            if (element != null && (StringUtils.equalsIgnoreCase(element.optString(TYPE), SCENARIO)
                    || StringUtils.equalsIgnoreCase(element.optString(TYPE), "UiScenario")
                    || StringUtils.equalsIgnoreCase(element.optString(TYPE), UiScenarioType.CUSTOM_COMMAND.getType()))) {
                element = this.setRefScenario(element);
                hashTree.put(i, element);
            }
        }
    }

    private static void convertOldVariables(JSONObject element) {
        JSONArray variables = element.optJSONArray("variables");
        List<ScenarioVariable> variableList = variables != null ? JSON.parseObject(variables.toString(), new TypeReference<List<ScenarioVariable>>() {
        }) : new ArrayList<>();
        for (ScenarioVariable scenarioVariable : variableList) {
            if (StringUtils.equals(scenarioVariable.getType(),"CONSTANT")){
                String type = CompatibilityOldData.convertConstantType(scenarioVariable.getValue());
                scenarioVariable.setType(type);
            }
        }
        element.put("variables",variableList);
    }

    public void dataFormatting(JSONObject element) {
        if (element != null && (StringUtils.equalsIgnoreCase(element.optString(TYPE), SCENARIO)
                || StringUtils.equalsIgnoreCase(element.optString(TYPE), "UiScenario")
                || StringUtils.equalsIgnoreCase(element.optString(TYPE), UiScenarioType.CUSTOM_COMMAND.getType()))) {
            element = this.setRefScenario(element);
        }
        if (element != null && element.has(HASH_TREE)) {
            JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
            dataFormatting(elementJSONArray, null);
        }
    }

    private void setElement(JSONObject element, Integer num, Boolean enable, String versionName, Boolean versionEnable) {
        element.put(NUM, num);
        element.put(ENABLE, enable == null ? false : enable);
        element.put(VERSION_NAME, versionName);
        element.put(VERSION_ENABLE, versionEnable == null ? false : versionEnable);
    }

    public void bathEdit(UiScenarioBatchRequest request) {
        //默认场景类型
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));

        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            bathEditEnv(request);
            return;
        }

        UiScenarioExample uiScenarioExample = new UiScenarioExample();
        uiScenarioExample.createCriteria().andIdIn(request.getIds());

        //校验模块下是否已经存在了 名称
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            //批量获取场景信息
            List<UiScenarioWithBLOBs> uiScenarioWithBLOBs = uiScenarioMapper.selectByExampleWithBLOBs(uiScenarioExample);
            Map<String, UiScenarioWithBLOBs> cacheMap = uiScenarioWithBLOBs.stream().collect(Collectors.toConcurrentMap(UiScenarioWithBLOBs::getId, v -> v));
            request.getIds().forEach(id -> {
                UiScenarioWithBLOBs scenario = cacheMap.get(id);
                if (scenario == null) {
                    return;
                }
                //检查是否同名
                SaveUiScenarioRequest scenarioRequest = new SaveUiScenarioRequest();
                scenarioRequest.setScenarioType(request.getScenarioType());
                scenarioRequest.setProjectId(scenario.getProjectId());
                scenarioRequest.setName(scenario.getName());
                scenarioRequest.setId(scenario.getId());
                scenarioRequest.setModuleId(StringUtils.isBlank(request.getModuleId()) ? scenario.getModuleId() : request.getModuleId());
                scenarioRequest.setModulePath(StringUtils.isBlank(request.getModulePath()) ? scenario.getModulePath() : request.getModulePath());
                scenarioRequest.setVersionId(scenario.getVersionId());
                checkNameExist(scenarioRequest);
            });
        }

        UiScenarioWithBLOBs uiScenarioWithBLOBs = new UiScenarioWithBLOBs();
        BeanUtils.copyBean(uiScenarioWithBLOBs, request);
        uiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        uiScenarioWithBLOBs.setModuleId(request.getModuleId());
        if (StringUtils.isBlank(request.getModuleId())) {
            uiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        }

        uiScenarioMapper.updateByExampleSelective(
                uiScenarioWithBLOBs,
                uiScenarioExample);
    }

    public void bathEditOpt(UiScenarioBatchRequest request) {
        //默认场景类型
        if (StringUtils.isBlank(request.getScenarioType())) {
            request.setScenarioType(UiScenarioType.SCENARIO.getType());
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extUiScenarioMapper.selectIdsByQuery(query));

        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            bathEditEnv(request);
            return;
        }
        if (request.getIds().size() > SPLIT_LIMIT) {
            List<List<String>> parts = Lists.partition(request.getIds(), SPLIT_LIMIT);
            LogUtil.info("total parts num:" + parts.size());
            for (int i = 0; i < parts.size(); i++) {
                LogUtil.info("current excute num:" + i);
                excuteBathEdit(parts.get(i), request);
            }
        } else {
            excuteBathEdit(request.getIds(), request);
        }
    }

    public void excuteBathEdit(List<String> ids, UiScenarioBatchRequest request) {
        UiScenarioExample uiScenarioExample = new UiScenarioExample();
        uiScenarioExample.createCriteria().andIdIn(ids);

        //校验模块下是否已经存在了 名称
        if (CollectionUtils.isNotEmpty(ids)) {
            //批量获取场景信息
            List<UiScenarioWithBLOBs> uiScenarioWithBLOBs = uiScenarioMapper.selectByExampleWithBLOBs(uiScenarioExample);
            Map<String, UiScenarioWithBLOBs> cacheMap = uiScenarioWithBLOBs.stream().collect(Collectors.toConcurrentMap(UiScenarioWithBLOBs::getId, v -> v));
            ids.forEach(id -> {
                UiScenarioWithBLOBs scenario = cacheMap.get(id);
                if (scenario == null) {
                    return;
                }
                //检查是否同名
                SaveUiScenarioRequest scenarioRequest = new SaveUiScenarioRequest();
                scenarioRequest.setScenarioType(request.getScenarioType());
                scenarioRequest.setProjectId(scenario.getProjectId());
                scenarioRequest.setName(scenario.getName());
                scenarioRequest.setId(scenario.getId());
                scenarioRequest.setModuleId(StringUtils.isBlank(request.getModuleId()) ? scenario.getModuleId() : request.getModuleId());
                scenarioRequest.setModulePath(StringUtils.isBlank(request.getModulePath()) ? scenario.getModulePath() : request.getModulePath());
                scenarioRequest.setVersionId(scenario.getVersionId());
                checkNameExist(scenarioRequest);
            });
        }
        UiScenarioWithBLOBs uiScenarioWithBLOBs = new UiScenarioWithBLOBs();
        BeanUtils.copyBean(uiScenarioWithBLOBs, request);
        uiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        uiScenarioWithBLOBs.setModuleId(request.getModuleId());
        if (StringUtils.isBlank(request.getModuleId())) {
            uiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        }

        uiScenarioMapper.updateByExampleSelective(
                uiScenarioWithBLOBs,
                uiScenarioExample);
    }

    public void bathEditEnv(UiScenarioBatchRequest request) {
        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            List<UiScenarioWithBLOBs> apiScenarios = selectByIdsWithBLOBs(request.getIds());
            apiScenarios.forEach(item -> {
                JSONObject object = new JSONObject(item.getScenarioDefinition());
                object.put("environmentId", request.getEnvironmentId());
                if (object != null) {
                    item.setScenarioDefinition(JSON.toJSONString(object));
                }
                uiScenarioMapper.updateByPrimaryKeySelective(item);
                saveByApiScenario(item);
            });
        }
    }

    public void deleteByScenarioId(String scenarioId) {
        UiScenarioReferenceExample example = new UiScenarioReferenceExample();
        example.createCriteria().andUiScenarioIdEqualTo(scenarioId);
        uiScenarioReferenceMapper.deleteByExample(example);
    }

    public void saveByApiScenario(UiScenarioWithBLOBs scenario) {
        if (scenario.getId() == null) {
            return;
        }
        deleteByScenarioId(scenario.getId());

        long createTime = System.currentTimeMillis();
        String createUser = SessionUtils.getUserId();

        Map<String, UiScenarioReference> refreceIdDic = new HashMap<>();
        try {
            if (scenario.getScenarioDefinition() != null) {
                JSONObject jsonObject = new JSONObject(scenario.getScenarioDefinition());
                if (jsonObject.has("hashTree")) {
                    JSONArray testElementList = jsonObject.optJSONArray("hashTree");
                    for (int index = 0; index < testElementList.length(); index++) {
                        JSONObject item = testElementList.getJSONObject(index);
                        String refId = "";
                        String refrenced = "";
                        String dataType = "";
                        if (item.has("id")) {
                            refId = item.optString("id");
                        }
                        if (item.has("referenced")) {
                            refrenced = item.optString("referenced");
                        }
                        if (item.has("refType")) {
                            dataType = item.optString("refType");
                        }

                        if (StringUtils.isNotEmpty(refId) && StringUtils.isNotEmpty(refrenced)) {
                            UiScenarioReference saveItem = new UiScenarioReference();
                            saveItem.setId(UUID.randomUUID().toString());
                            saveItem.setUiScenarioId(scenario.getId());
                            saveItem.setCreateTime(createTime);
                            saveItem.setCreateUserId(createUser);
                            saveItem.setReferenceId(refId);
                            saveItem.setReferenceType(refrenced);
                            saveItem.setDataType(dataType);
                            refreceIdDic.put(refId, saveItem);
                        }

                        if (item.has("hashTree")) {
                            refreceIdDic.putAll(this.deepParseTestElement(createTime, createUser, scenario.getId(), item.optJSONArray("hashTree")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (MapUtils.isNotEmpty(refreceIdDic)) {
            for (UiScenarioReference model : refreceIdDic.values()) {
                uiScenarioReferenceMapper.insert(model);
            }
        } else {
            UiScenarioReference saveItem = new UiScenarioReference();
            saveItem.setId(UUID.randomUUID().toString());
            saveItem.setUiScenarioId(scenario.getId());
            saveItem.setCreateTime(createTime);
            saveItem.setCreateUserId(createUser);
            try {
                uiScenarioReferenceMapper.insert(saveItem);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
    }

    public Map<String, UiScenarioReference> deepParseTestElement(long createTime, String createUser, String scenarioId, JSONArray testElementList) {
        Map<String, UiScenarioReference> returnMap = new HashMap<>();

        if (testElementList != null) {
            for (int index = 0; index < testElementList.length(); index++) {
                JSONObject item = testElementList.getJSONObject(index);
                String refId = "";
                String refrenced = "";
                String dataType = "";
                if (item.has("id")) {
                    refId = item.optString("id");
                }
                if (item.has("referenced")) {
                    refrenced = item.optString("referenced");
                }
                if (item.has("refType")) {
                    dataType = item.optString("refType");
                }

                if (StringUtils.isNotEmpty(refId) && StringUtils.isNotEmpty(refrenced)) {
                    UiScenarioReference saveItem = new UiScenarioReference();
                    saveItem.setId(UUID.randomUUID().toString());
                    saveItem.setUiScenarioId(scenarioId);
                    saveItem.setCreateTime(createTime);
                    saveItem.setCreateUserId(createUser);
                    saveItem.setReferenceId(refId);
                    saveItem.setReferenceType(refrenced);
                    saveItem.setDataType(dataType);
                    returnMap.put(refId, saveItem);
                }
                if (item.has("hashTree")) {
                    returnMap.putAll(this.deepParseTestElement(createTime, createUser, scenarioId, item.optJSONArray("hashTree")));
                }
            }
        }

        return returnMap;
    }

    public List<MsExecResponseDTO> run(RunUiScenarioRequest request) {
        request.setConfig(request.getUiConfig());
        return uiScenarioExecuteService.run(request);
    }

    public ResultHolder validateScenario(String uiScenarioStr) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<UiValidateSingleResult> resultList = new LinkedList<>();
        try {
            LinkedList<MsTestElement> sourceHashTree = mapper.readValue(uiScenarioStr, new TypeReference<>() {
            });
            MsUiScenario msUiScenario = new MsUiScenario();
            ParameterConfig config = new ParameterConfig();
            config.setCommandConfig(new CommandConfig());
            List<MsUiCommand> commandList = msUiScenario.getMsCommands(sourceHashTree, config, false);
            commandList.stream().forEach(c -> validateSingleCommand(null, c, resultList));
        } catch (JsonProcessingException e) {
            LogUtil.error("校验 UI 场景步骤的参数出错！", e);
        }

        return ResultHolder.success(resultList);
    }


    /**
     * 校验单个指令通用逻辑
     *
     * @param parentCommandId 父亲指令 id
     * @param command
     * @param resultList
     */
    private void validateSingleCommand(String parentCommandId, MsUiCommand command, List<UiValidateSingleResult> resultList) {
        Map<String, CommonCommand> uiCommandMap = (Map<String, CommonCommand>) CommonBeanFactory.getBean("uiCommandMap");
        if (uiCommandMap.containsKey(command.getCommand())) {
            command.setCommandType(CommandType.COMMAND_TYPE_ATOM);
        }
        switch (command.getCommandType()) {
            // 复合指令，先将其vo转成原子指令，再解析HashTree，最后加上end结束
            case CommandType.COMMAND_TYPE_COMBINATION:
                // 添加start 指令
                resultList.add(command.getVo().validate(command));
                if (CollectionUtils.isNotEmpty(command.getHashTree())) {
                    command.getHashTree().forEach(item -> {
                        if (!command.isEnable()) {
                            // 如果复合指令为disable，内部指令也置为disable
                            item.setEnable(false);
                        }
                        if (item instanceof MsUiCommand) {
                            // 添加嵌套指令
                            validateSingleCommand(command.getId(), (MsUiCommand) item, resultList);
                        }
                    });
                }
                // 添加Append指令
                if (CollectionUtils.isNotEmpty(command.getAppendCommands())) {
                    command.getAppendCommands().forEach(c -> validateSingleCommand(command.getId(), c, resultList));
                }

                // 添加end指令 end指令自动添加 不用校验
                break;
            // 代理指令将vo转成原子指令
            case CommandType.COMMAND_TYPE_PROXY:
                resultList.add(command.getVo().validate(command));
                break;
            // 复合的代理指令，如数据提取，断言
            case CommandType.COMMAND_TYPE_COMBINATION_PROXY:
                if (CollectionUtils.isNotEmpty(command.getHashTree())) {
                    command.getHashTree().forEach(item -> {
                        if (item instanceof MsUiCommand) {
                            // 添加嵌套指令
                            validateSingleCommand(command.getId(), (MsUiCommand) item, resultList);
                        }
                    });
                }
                break;
            //原子指令校验
            case CommandType.COMMAND_TYPE_ATOM:
                validateAtomicCommand(command, resultList);
                break;
            default:
                break;
        }
        // 添加后置指令
        if (CollectionUtils.isNotEmpty(command.getPostCommands())) {
            command.getPostCommands().forEach(c -> validateSingleCommand(command.getId(), c, resultList));
        }
    }

    private void validateAtomicCommand(MsUiCommand command, List<UiValidateSingleResult> resultList) {
        Map<String, CommonCommand> uiCommandMap = (Map<String, CommonCommand>) CommonBeanFactory.getBean("uiCommandMap");
        CommonCommand currentCommand = uiCommandMap.get(command.getCommand());
        if (currentCommand != null) {
            ArgTypeEnum targetType = currentCommand.getTargetType();
            ArgTypeEnum valueType = currentCommand.getValueType();
            List<UiValidateSingleResult> r = new LinkedList<>();
            if (targetType != null) {
                validateAtomicTargetType(command, r, targetType, command.getTargetVO());
            }
            if (valueType != null) {
                validateAtomicTargetType(command, r, valueType, command.getValueVO());
            }
            if (CollectionUtils.isNotEmpty(r)) {
                resultList.add(new UiAtomicCommandVO().getFailResult(command, Translator.get("param_error")));
            }
        }
    }

    private void validateAtomicTargetType(MsUiCommand command, List<UiValidateSingleResult> resultList, ArgTypeEnum targetType, UiAtomicCommandVO param) {
        switch (targetType.getMsType()) {
            case ViewTargetTypeConstants.STRING:
            case ViewTargetTypeConstants.COORD:
            case ViewTargetTypeConstants.REGION:
            case ViewTargetTypeConstants.RESOLUTION:
            case ViewTargetTypeConstants.HANDLE:
            case ViewTargetTypeConstants.NUMBER:
            case ViewTargetTypeConstants.BROWSER:
                if (param == null || !targetType.getValidator()._validate(Optional.ofNullable(command.getTargetVO().getText()).orElse("").toString())) {
                    resultList.add(new UiAtomicCommandVO().getFailResult(command, Translator.get("param_error")));
                }
                break;
            case ViewTargetTypeConstants.LOCATOR:
                if (param == null) {
                    resultList.add(new UiAtomicCommandVO().getFailResult(command, Translator.get("param_error")));
                }
                try {
                    BaseLocator locator = JSON.parseObject(JSON.toJSONString(param), BaseLocator.class);
                    if (StringUtils.isNotBlank(locator.validateLocateString())) {
                        resultList.add(new UiAtomicCommandVO().getFailResult(command, locator.validateLocateString()));
                    }
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
    }

    public void relevance(UiCaseRelevanceRequest request) {
        List<String> relevanceIds = request.getSelectIds();
        if (CollectionUtils.isEmpty(relevanceIds)) {
            return;
        }
        Collections.reverse(relevanceIds);
        String planId = request.getPlanId();
        Long nextOrder = ServiceUtils.getNextOrder(planId, extTestPlanUiScenarioCaseMapper::getLastOrder);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanUiScenarioMapper batchMapper = sqlSession.getMapper(TestPlanUiScenarioMapper.class);
        String userId = SessionUtils.getUserId();
        Map<String, List<String>> mapping = request.getMapping();
        for (String id : relevanceIds) {
            TestPlanUiScenario testPlanUiScenario = new TestPlanUiScenario();
            testPlanUiScenario.setId(UUID.randomUUID().toString());
            testPlanUiScenario.setCreateUser(userId);
            testPlanUiScenario.setUiScenarioId(id);
            testPlanUiScenario.setTestPlanId(planId);
            testPlanUiScenario.setCreateTime(System.currentTimeMillis());
            testPlanUiScenario.setUpdateTime(System.currentTimeMillis());
            //设置环境
            Map<String, String> environmentMap = new HashMap<>();
            mapping.getOrDefault(id, Collections.emptyList()).forEach(p -> {
                environmentMap.put(p, request.getEnvMap().get(p));
            });
            testPlanUiScenario.setEnvironment(JSON.toJSONString(environmentMap));
            testPlanUiScenario.setEnvironmentType(EnvironmentTypeEnum.JSON.name());
            testPlanUiScenario.setLastResult(TestPlanUiResultStatus.UnExecute.name());
            testPlanUiScenario.setOrder(nextOrder);
            nextOrder += ServiceUtils.ORDER_STEP;
            batchMapper.insert(testPlanUiScenario);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void downloadScreenshot(HttpServletResponse response, DownloadScreenshotDTO request) {
        String reportId = request.getReportId();

        if (CollectionUtils.isEmpty(request.getResources())) {
            return;
        }

        int resourceSize = CollectionUtils.isEmpty(request.getResources()) ? 0 : request.getResources().size();

        //设置响应头
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getDirName() + ".zip");

        //开启时间监听
        StopWatch watch = new StopWatch();
        watch.start();

        //定义zip 压缩到 zip 包
        try (ServletOutputStream outputStream = response.getOutputStream();
             CheckedOutputStream checkedOutputStream = new CheckedOutputStream(outputStream, new Adler32());
             ZipOutputStream zos = new ZipOutputStream(checkedOutputStream);
        ) {
            request.getResources().forEach(screenshot -> {
                //获取当前图片的流
                String fullName = String.format("%s/%s/%s", FileUtils.UI_IMAGE_DIR, reportId, screenshot.getCombinationImg());
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fullName))) {
                    byte[] data = new byte[bis.available()];
                    bis.read(data);
                    if (data.length > 0) {
                        ZipEntry entry = new ZipEntry(screenshot.getFileName() + "_" + RandomStringUtils.randomAlphabetic(5) + ".png");
                        entry.setSize(data.length);
                        zos.putNextEntry(entry);
                        zos.write(data);
                        zos.flush();
                        zos.closeEntry();
                    }
                } catch (Exception e) {
                    LogUtil.error(String.format("read %s file err ~", fullName), e);
                }
            });
        } catch (Exception e) {
            LogUtil.error("Zip close err, please check ~", e);
        }

        //结束耗时监听
        watch.stop();
        LogUtil.info(String.format("[Screenshot download %s] 分批处理截图文件完成, 处理图片数: %s, 耗时：%s ms,  zip 打包 结束。",
                reportId, resourceSize, watch.getTotalTimeMillis()));
    }

    /**
     * 下载截图文件
     */
    @Deprecated
    public void downloadScreenshotWithMultiThreading(HttpServletResponse response, DownloadScreenshotDTO request) {
        String reportId = request.getReportId();

        if (CollectionUtils.isEmpty(request.getResources())) {
            return;
        }

        // 计算出分配的线程数
        int resourceSize = CollectionUtils.isEmpty(request.getResources()) ? 0 : request.getResources().size();
        int countDown = resourceSize > 50 ? ThreadPoolConfig.COUNT_DOWN_NUM : 1;
        //平均分配
        List<List<DownloadScreenshotDTO.ScreenshotDTO>> avgList = MsListUtil.averageAssign(request.getResources(), countDown);

        //分批处理
        CountDownLatch latch = new CountDownLatch(countDown);

        //Map<String, byte[]> files = Collections.synchronizedMap(new ConcurrentHashMap<>());

        //设置响应头
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getDirName() + ".zip");

        //开启时间监听
        StopWatch watch = new StopWatch();
        watch.start();

        try (ServletOutputStream outputStream = response.getOutputStream();) {

            //定义zip 压缩到 zip 包
            ZipOutputStream zos = new ZipOutputStream(outputStream);

            //分批执行
            avgList.forEach(resources -> {
                try {
                    threadPoolTaskExecutor.execute(() -> {
                        resources.forEach(screenshot -> {
                            //获取当前图片的流
                            String fullName = String.format("%s/%s/%s", FileUtils.UI_IMAGE_DIR, reportId, screenshot.getCombinationImg());
                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fullName))) {
                                byte[] data = new byte[bis.available()];
                                bis.read(data);
                                if (data.length > 0) {
                                    ZipEntry entry = new ZipEntry(screenshot.getFileName() + "_" + RandomStringUtils.randomAlphabetic(5) + ".png");
                                    entry.setSize(data.length);
                                    zos.putNextEntry(entry);
                                    zos.write(data);
                                    zos.flush();
                                    zos.closeEntry();
                                }
                            } catch (Exception e) {
                                LogUtil.error(String.format("read %s file err ~", fullName), e);
                            }
                        });
                        latch.countDown();
                    });
                } catch (Exception e) {
                    //线程池异常也要 countDown
                    LogUtil.error("Thread pool execute err ~", e);
                    latch.countDown();
                }
            });

            //主线程阻塞 等待所有任务线程执行完毕
            latch.await();
            //先阻塞 后关流
            zos.close();

        } catch (InterruptedException e) {
            LogUtil.error("CountDownLatch await err, please check ~", e);
        } catch (Exception e) {
            LogUtil.error("Zip close err, please check ~", e);
        }

        //结束耗时监听
        watch.stop();
        LogUtil.info(String.format("[Screenshot download %s] 分批处理截图文件完成, 处理图片数: %s, 耗时：%s ms,  zip 打包 结束。",
                reportId, resourceSize, watch.getTotalTimeMillis()));
    }

    public BufferedImage loadImageLocal(String imgName) {
        try {
            return ImageIO.read(new File(imgName));
        } catch (Exception e) {
            LogUtil.info("读取图片失败");
        }
        return null;
    }

    public byte[] bufferedImageToByteArray(BufferedImage image) {
        if (image == null) {
            return null;
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, List<UiScenario>> selectBaseInfoGroupByModuleId(String projectId, String status, String type) {
        List<UiScenario> apiList = extUiScenarioMapper.selectBaseInfoGroupByModuleId(projectId, status, type);
        return apiList.stream().collect(Collectors.groupingBy(UiScenario::getModuleId));
    }

    public void updateNoModuleScenarioToDefaultModule(String projectId, String status, String id, String type) {
        if (StringUtils.isNoneEmpty(projectId, id)) {
            extUiScenarioMapper.updateNoModuleScenarioToDefaultModule(projectId, status, id, type);
        }
    }


    public List<RefResp> refList(RefReq request) {
        return uiScenarioReferenceMapper.refList(request);
    }

    public List<TestPlanRefResp> getTestPlanRef(RefReq request) {
        return uiScenarioReferenceMapper.getTestPlanRef(request);
    }

    public String verifyUserSeleniumServer(String type) {
        String seleniumServerUrl = null;
        if ("user".equalsIgnoreCase(type)) {
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdEqualTo(SessionUtils.getUser().getId());
            List<User> users = userMapper.selectByExample(userExample);
            if (CollectionUtils.isNotEmpty(users)) {
                seleniumServerUrl = users.get(0).getSeleniumServer();
                if (StringUtils.isBlank(seleniumServerUrl)) {
                    return "noconfig";
                }
            } else {
                return "noconfig";
            }
        } else {
            SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(ParamConstants.BASE.SELENIUM_DOCKER_URL.getValue());
            if (systemParameter != null) {
                seleniumServerUrl = systemParameter.getParamValue();
            }
            if (StringUtils.isBlank(seleniumServerUrl)) {
                return "noconfig";
            }
        }
        if (StringUtils.isNotEmpty(seleniumServerUrl)) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,
                    "{\"operationName\":\"\",\"variables\":{},\"query\":\"query Summary {\\n  grid {\\n    uri\\n    totalSlots\\n    nodeCount\\n    maxSession\\n    sessionCount\\n    sessionQueueSize\\n    version\\n    __typename\\n  }\\n}\"}");
            Request req = new Request.Builder()
                    .url(seleniumServerUrl + "/graphql")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = null;
            try {
                response = client.newCall(req).execute();
                if (!response.isSuccessful()) {
                    if ("user".equalsIgnoreCase(type)) {
                        return "userConnectionErr";
                    }
                    return "connectionErr";
                }
            } catch (Exception e) {
                if ("user".equalsIgnoreCase(type)) {
                    return "userConnectionErr";
                }
                return "connectionErr";
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (Exception e) {
                    return "connectionErr";
                }
            }
        } else {
            return "configErr";
        }
        return "ok";
    }

    public UiCheckRefResp checkRef(UiCheckRefReq request) {
        if (CollectionUtils.isEmpty(request.getScenarioIds())) {
            return UiCheckRefResp.builder().build();
        }
        List<UiCheckRefDTO> list = uiScenarioReferenceMapper.checkRef(request);
        //生成result，
        List<String> resultList = list.stream().map(v -> {
            String type = StringUtils.equalsIgnoreCase(v.getScenarioType(),
                    UiScenarioType.CUSTOM_COMMAND.getType()) ? CUSTOM_COMMAND_LABEL : SCENARIO_LABEL;
            return String.format("[%s]-%s", type, v.getScenarioName());
        }).collect(Collectors.toList());
        String result = StringUtils.join(resultList, ",");
        return UiCheckRefResp.builder()
                .result(result)
                .data(list)
                .build();
    }

    private void dealWithRef(String id) {
        UiScenarioReferenceExample example = new UiScenarioReferenceExample();
        example.createCriteria().andReferenceIdEqualTo(id);
        List<UiScenarioReference> uiScenarioReferences = uiScenarioReferenceMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(uiScenarioReferences)) {
            uiScenarioReferences.forEach(uiScenarioReference -> {
                UiScenarioDTO newUiScenario = getNewUiScenario(uiScenarioReference.getUiScenarioId());
                if (newUiScenario != null && !DataStatus.TRASH.getValue().equals(newUiScenario.getStatus())) {
                    UiScenarioWithBLOBs record = new UiScenarioWithBLOBs();
                    record.setId(newUiScenario.getId());
                    record.setScenarioDefinition(newUiScenario.getScenarioDefinition());
                    record.setUpdateTime(System.currentTimeMillis());
                    uiScenarioMapper.updateByPrimaryKeySelective(record);
                }
            });
        }
    }

    public List<ApiScenarioDTO> getRelevanceScenarioList(ApiScenarioRequest request) {
        return extUiScenarioMapper.relevanceScenarioList(request);
    }

    public List<UiScenario> getUiCaseByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            UiScenarioExample e = new UiScenarioExample();
            e.createCriteria().andIdIn(ids);
            return uiScenarioMapper.selectByExample(e);
        }
        return new ArrayList<>();
    }

    public ScenarioEnv getUiScenarioEnv(String scenarioDefinition) {
        ScenarioEnv scenarioEnv = new ScenarioEnv();
        JSONObject element = new JSONObject(scenarioDefinition);
        this.countScenarioEnvInfo(element, scenarioEnv);
        return scenarioEnv;
    }

    public List<String> getUiScenarioIdsByDefinition(String scenarioDefinition) {
        JSONObject element = new JSONObject(scenarioDefinition);
        List<String> scenarioIds = new ArrayList<>();
        this.getSourceIdsByElement(element, scenarioIds);
        return scenarioIds;
    }

    private void countScenarioEnvInfo(JSONObject element, ScenarioEnv scenarioEnv) {
        if (element.has(UiGenerateHashTreeUtil.TYPE) && element.has(UiGenerateHashTreeUtil.RESOURCE_ID) && StringUtils.equalsIgnoreCase(element.optString(UiGenerateHashTreeUtil.TYPE), UiGenerateHashTreeUtil.SCENARIO)) {
            UiScenario uiScenario = uiScenarioMapper.selectByPrimaryKey(element.optString(UiGenerateHashTreeUtil.RESOURCE_ID));
            if (uiScenario != null) {
                scenarioEnv.getProjectIds().add(uiScenario.getProjectId());
            }
        } else {
            if (element.has(UiGenerateHashTreeUtil.PROJECT_ID)) {
                scenarioEnv.getProjectIds().add(element.optString(UiGenerateHashTreeUtil.PROJECT_ID));
            }
        }
        if (element.has(UiGenerateHashTreeUtil.HASH_TREE_ELEMENT)) {
            JSONArray hashTree = element.optJSONArray(UiGenerateHashTreeUtil.HASH_TREE_ELEMENT);
            if (hashTree != null) {
                hashTree.forEach(h -> countScenarioEnvInfo((JSONObject) h, scenarioEnv));
            }
        }
    }

    private List<String> getSourceIdsByElement(JSONObject element, List<String>sourceIds) {
        if (element.has(UiGenerateHashTreeUtil.TYPE) && element.has(UiGenerateHashTreeUtil.RESOURCE_ID) && StringUtils.equalsIgnoreCase(element.optString(UiGenerateHashTreeUtil.TYPE), UiGenerateHashTreeUtil.SCENARIO)) {
            sourceIds.add(element.optString(UiGenerateHashTreeUtil.RESOURCE_ID));
        }
        if (element.has(UiGenerateHashTreeUtil.HASH_TREE_ELEMENT)) {
            JSONArray hashTree = element.optJSONArray(UiGenerateHashTreeUtil.HASH_TREE_ELEMENT);
            if (hashTree != null) {
                hashTree.forEach(h -> getSourceIdsByElement((JSONObject) h, sourceIds));
            }
        }
        return sourceIds;
    }

    public Map<String, List<UiScenario>> selectBaseInfoGroupByCondition(String projectId, String status, String type, UiScenarioRequest param) {
        List<UiScenario> apiList = extUiScenarioMapper.selectBaseInfoGroupByCondition(projectId, status, type, param);
        return apiList.stream().collect(Collectors.groupingBy(UiScenario::getModuleId));
    }

    public ScenarioEnv getApiScenarioProjectId(String id) {
        UiScenarioWithBLOBs scenario = uiScenarioMapper.selectByPrimaryKey(id);
        ScenarioEnv scenarioEnv = getScenarioEnv(scenario);
        return scenarioEnv;
    }

    @NotNull
    private ScenarioEnv getScenarioEnv(UiScenarioWithBLOBs scenario) {
        ScenarioEnv scenarioEnv = new ScenarioEnv();
        if (scenario == null) {
            return scenarioEnv;
        }

        if (StringUtils.isBlank(scenario.getScenarioDefinition())) {
            return scenarioEnv;
        }

        scenarioEnv = getUiScenarioEnv(scenario.getScenarioDefinition());
        scenarioEnv.getProjectIds().remove(null);
        scenarioEnv.getProjectIds().add(scenario.getProjectId());
        return scenarioEnv;
    }

    public Map<String,List<String>> getScenarioProjectMap(List<String>ids){
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        UiScenarioExample uiScenarioExample = new UiScenarioExample();
        uiScenarioExample.createCriteria().andIdIn(ids);
        List<UiScenarioWithBLOBs> uiScenarioWithBLOBs = uiScenarioMapper.selectByExampleWithBLOBs(uiScenarioExample);
        if (CollectionUtils.isEmpty(uiScenarioWithBLOBs)) {
            return new HashMap<>();
        }

        Map<String, Set<String>> scenarioChildIdsMap = uiScenarioWithBLOBs.stream()
                .filter(t -> StringUtils.isNotBlank(t.getScenarioDefinition()))
                .collect(Collectors.toMap(UiScenarioWithBLOBs::getId, t -> new HashSet<>(getUiScenarioIdsByDefinition(t.getScenarioDefinition()))));

        Set<String> uiScenarioChildIds = scenarioChildIdsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        uiScenarioExample = new UiScenarioExample();
        uiScenarioExample.createCriteria().andIdIn(new ArrayList<>(uiScenarioChildIds));
        List<UiScenario> uiScenarioChildren = uiScenarioMapper.selectByExample(uiScenarioExample);

        Map<String, UiScenarioWithBLOBs> uiScenarioMap = uiScenarioWithBLOBs.stream()
                .collect(Collectors.toMap(UiScenarioWithBLOBs::getId, Function.identity()));

        Map<String, String> uiScenarioChildMap = uiScenarioChildren.stream().collect(Collectors.toMap(UiScenario::getId, UiScenario::getProjectId));

        Map<String, List<String>> scenarioProjectMap = uiScenarioMap.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), k -> {
                    List<String> list = new ArrayList<>();
                    list.add(uiScenarioMap.get(k).getProjectId());
                    scenarioChildIdsMap.getOrDefault(k, Collections.emptySet()).stream()
                            .map(uiScenarioChildMap::get)
                            .filter(StringUtils::isNotBlank)
                            .forEach(list::add);
                    return list;
                }));

        return scenarioProjectMap;
    }

    public Map<String, List<String>> getProjectEnvMap(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extUiScenarioMapper.selectIdsByQuery(query));

        List<String> ids = request.getIds();
        UiScenarioExample example = new UiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<UiScenarioWithBLOBs> apiScenarios = uiScenarioMapper.selectByExampleWithBLOBs(example);
        Map<String, List<String>> projectEnvMap = new HashMap<>();
        apiScenarios.forEach(item -> {
            if (StringUtils.isBlank(item.getEnvironmentJson())) {
                return;
            }
            JSONObject jsonObject = new JSONObject(item.getEnvironmentJson());
            Map<String, Object> projectIdEnvMap = jsonObject.toMap();
            if (MapUtils.isEmpty(projectIdEnvMap)) {
                return;
            }
            projectIdEnvMap.forEach((projectId, env) -> {
                if (env == null) {
                    return;
                }
                List<String> envIds = projectEnvMap.get(projectId);
                if (CollectionUtils.isNotEmpty(envIds)) {
                    if (!envIds.contains(env.toString())) {
                        envIds.add(env.toString());
                    }
                } else {
                    List<String> envIdList = new ArrayList<>();
                    envIdList.add(env.toString());
                    projectEnvMap.put(projectId, envIdList);
                }
            });
        });
        return projectEnvMap;
    }

    public boolean verifyScenarioEnv(String scenarioId) {
        UiScenarioWithBLOBs uiScenarioWithBLOBs = uiScenarioMapper.selectByPrimaryKey(scenarioId);
        if (uiScenarioWithBLOBs == null) {
            return true;
        }
        uiScenarioEnvService.setScenarioEnv(uiScenarioWithBLOBs, null);
        return uiScenarioEnvService.verifyScenarioEnv(uiScenarioWithBLOBs);
    }


    public String setDomain(UiScenarioEnvRequest request) {
        Boolean enable = request.getEnvironmentEnable();
        String scenarioDefinition = request.getDefinition();
        JSONObject element = JSONUtil.parseObject(scenarioDefinition);
        ElementUtil.dataFormatting(element);
        try {
            Map<String, String> environmentMap = new HashMap<>();
            if (BooleanUtils.isFalse(enable)) {
                String envType = request.getEnvironmentType();
                String envGroupId = request.getEnvironmentGroupId();
                if (StringUtils.equals(envType, EnvironmentType.GROUP.name())) {
                    environmentMap = baseEnvironmentGroupProjectService.getEnvMap(envGroupId);
                } else if (StringUtils.equals(envType, EnvironmentType.JSON.name())) {
                    environmentMap = request.getEnvironmentMap();
                }
            } else {
                String scenarioId = request.getId();
                UiScenarioDTO scenario = getNewUiScenario(scenarioId);
                if (scenario != null) {
                    String referenced = element.optString("referenced");
                    if (StringUtils.equalsIgnoreCase("REF", referenced)) {
                        JSONObject source = JSONUtil.parseObject(scenario.getScenarioDefinition());
                        element = jsonMerge(source, element);
                    }
                    element.put("referenced", referenced);
                    String environmentType = scenario.getEnvironmentType();
                    String environmentGroupId = scenario.getEnvironmentGroupId();
                    String environmentJson = scenario.getEnvironmentJson();
                    if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                        environmentMap = baseEnvironmentGroupProjectService.getEnvMap(environmentGroupId);
                    } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                        environmentMap = JSON.parseObject(environmentJson, Map.class);
                    }
                }
            }

            ParameterConfig config = new ParameterConfig();
            uiScenarioEnvService.setEnvConfig(environmentMap, config);
            if (config.getConfig() != null && !config.getConfig().isEmpty()) {
                ElementUtil.dataSetDomain(element.optJSONArray(HASH_TREE), config);
            }
            return element.toString();
        } catch (Exception e) {
            return scenarioDefinition;
        }
    }

    private static JSONObject jsonMerge(JSONObject source, JSONObject target) {
        // 覆盖目标JSON为空，直接返回覆盖源
        if (target == null) {
            return source;
        }
        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (!target.has(key)) {
                target.put(key, value);
            } else {
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    JSONObject targetValue = jsonMerge(valueJson, target.optJSONObject(key));
                    target.put(key, targetValue);
                } else if (value instanceof JSONArray) {
                    JSONArray valueArray = (JSONArray) value;
                    for (int i = 0; i < valueArray.length(); i++) {
                        try {
                            JSONObject obj = (JSONObject) valueArray.get(i);
                            JSONObject targetValue = jsonMerge(obj, (JSONObject) target.optJSONArray(key).get(i));
                            target.optJSONArray(key).put(i, targetValue);
                        } catch (Exception e) {
                            LogUtil.error(e);
                        }
                    }
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

    public Map<String, ScheduleDTO> selectScheduleInfo(List<String> scenarioIds) {
        if (CollectionUtils.isNotEmpty(scenarioIds)) {
            List<Schedule> scheduleList = scheduleService.selectByResourceIds(scenarioIds);
            List<ScheduleDTO> scheduleInfoList = new ArrayList<>();
            for (Schedule schedule : scheduleList) {
                ScheduleDTO scheduleInfo = new ScheduleDTO();
                BeanUtils.copyBean(scheduleInfo, schedule);
                scheduleInfo.setScheduleExecuteTime(this.getNextTriggerTime(scheduleInfo.getValue()));
                scheduleInfoList.add(scheduleInfo);
            }
            return scheduleInfoList.stream().collect(Collectors.toMap(Schedule::getResourceId, item -> item));
        } else {
            return new HashMap<>();
        }
    }

    //获取下次执行时间（getFireTimeAfter，也可以下下次...）
    private long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return 0;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
    }

    public List<UiScenario> getUiScenarioByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            UiScenarioExample example = new UiScenarioExample();
            example.createCriteria().andIdIn(ids).andStatusNotEqualTo(CommonConstants.TrashStatus);
            return uiScenarioMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

}
