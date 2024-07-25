package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanUiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.dto.automation.MsScenarioEnv;
import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.dto.request.ElementUtil;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.hashtree.MsUiScenario;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.JSONUtil;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UiScenarioEnvService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Resource
    private TestPlanUiScenarioMapper testPlanUiScenarioMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private BaseEnvGroupProjectService baseEnvironmentGroupProjectService;

    public static final String SCENARIO = "scenario";

    public LinkedHashMap<String, List<String>> selectProjectNameAndEnvName(Map<String, List<String>> projectEnvIdMap) {
        LinkedHashMap<String, List<String>> returnMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(projectEnvIdMap)) {
            for (Map.Entry<String, List<String>> entry : projectEnvIdMap.entrySet()) {
                String projectId = entry.getKey();
                List<String> envIdList = entry.getValue();
                String projectName = this.selectNameById(projectId);
                List<String> envNameList = apiTestEnvironmentService.selectNameByIds(envIdList);
                if (CollectionUtils.isNotEmpty(envNameList) && StringUtils.isNotEmpty(projectName)) {
                    returnMap.put(projectName, new ArrayList<>() {{
                        this.addAll(envNameList);
                    }});
                }
            }
        }
        return returnMap;
    }

    public LinkedHashMap<String, List<String>> getProjectEnvMapByEnvConfig(String envConfig) {
        LinkedHashMap<String, List<String>> returnMap = new LinkedHashMap<>();
        Map<String, List<String>> envMapByExecution = new HashMap<>();
        Map<String, String> envMapByRunConfig = new HashMap<>();
        try {
            JSONObject jsonObject = JSONUtil.parseObject(envConfig);
            if (jsonObject.has("executionEnvironmentMap")) {
                //集合报告
                RunModeConfigWithEnvironmentDTO configWithEnvironment = JSON.parseObject(envConfig, RunModeConfigWithEnvironmentDTO.class);

                if (MapUtils.isNotEmpty(configWithEnvironment.getExecutionEnvironmentMap())) {
                    envMapByExecution = configWithEnvironment.getExecutionEnvironmentMap();
                } else {
                    envMapByRunConfig = configWithEnvironment.getEnvMap();
                }
            } else {
                //独立报告
                RunModeConfigDTO config = JSON.parseObject(envConfig, RunModeConfigDTO.class);
                envMapByRunConfig = config.getEnvMap();
            }
        } catch (Exception e) {
            LogUtil.error("解析RunModeConfig失败!参数：" + envConfig, e);
        }
        returnMap.putAll(this.selectProjectNameAndEnvName(envMapByExecution));
        if (MapUtils.isNotEmpty(envMapByRunConfig)) {
            for (Map.Entry<String, String> entry : envMapByRunConfig.entrySet()) {
                String projectId = entry.getKey();
                String envId = entry.getValue();
                String projectName = this.selectNameById(projectId);
                String envName = apiTestEnvironmentService.selectNameById(envId);
                if (StringUtils.isNoneEmpty(projectName, envName)) {
                    returnMap.put(projectName, new ArrayList<>() {{
                        this.add(envName);
                    }});
                }
            }
        }
        return returnMap;
    }

    public String selectNameById(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return null;
        } else {
            return project.getName();
        }
    }

    public Map<String, List<String>> selectProjectEnvMapByTestPlanScenarioIds(List<String> resourceIds) {
        Map<String, List<String>> returnMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            List<String> reportEnvConfList = testPlanUiScenarioMapper.selectReportEnvConfByResourceIds(resourceIds);
            reportEnvConfList.forEach(envConf -> {
                LinkedHashMap<String, List<String>> projectEnvMap = this.getProjectEnvMapByEnvConfig(envConf);
                for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                    String projectName = entry.getKey();
                    List<String> envNameList = entry.getValue();
                    if (StringUtils.isEmpty(projectName) || CollectionUtils.isEmpty(envNameList)) {
                        continue;
                    }
                    if (returnMap.containsKey(projectName)) {
                        envNameList.forEach(envName -> {
                            if (!returnMap.get(projectName).contains(envName)) {
                                returnMap.get(projectName).add(envName);
                            }
                        });
                    } else {
                        returnMap.put(projectName, envNameList);
                    }
                }
            });
        }
        return returnMap;
    }

    public Map<String, List<String>> selectUiScenarioEnv(List<? extends UiScenarioWithBLOBs> list) {
        Map<String, List<String>> projectEnvMap = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            try {
                Map<String, String> map = new HashMap<>();
                String env = list.get(i).getEnvironmentJson();
                // 环境属性为空 跳过
                if (StringUtils.isBlank(env)) {
                    continue;
                }
                map = JSON.parseObject(env, Map.class);

                Set<String> set = map.keySet();
                HashMap<String, String> envMap = new HashMap<>(16);
                // 项目为空 跳过
                if (set.isEmpty()) {
                    continue;
                }
                for (String projectId : set) {
                    String envId = map.get(projectId);
                    envMap.put(projectId, envId);
                }
                for (Map.Entry<String, String> entry : envMap.entrySet()) {
                    String projectId = entry.getKey();
                    String envId = entry.getValue();
                    if (projectEnvMap.containsKey(projectId)) {
                        if (!projectEnvMap.get(projectId).contains(envId)) {
                            projectEnvMap.get(projectId).add(envId);
                        }
                    } else {
                        projectEnvMap.put(projectId, new ArrayList<>() {{
                            this.add(envId);
                        }});
                    }
                }
            } catch (Exception e) {
                LogUtil.error("api scenario environment map incorrect parsing. api scenario id:" + list.get(i).getId());
            }
        }
        return projectEnvMap;
    }

    public boolean verifyScenarioEnv( UiScenarioWithBLOBs uiScenarioWithBLOBs) {
        String definition = uiScenarioWithBLOBs.getScenarioDefinition();
        MsScenarioEnv scenario = JSON.parseObject(definition, MsScenarioEnv.class);
        Map<String, String> envMap = scenario.getEnvironmentMap();
        return this.check(definition, envMap, scenario.getEnvironmentId(), uiScenarioWithBLOBs.getProjectId());
    }

    private boolean check(String definition, Map<String, String> envMap, String envId, String projectId) {
        boolean isEnv = true;
        ScenarioEnv apiScenarioEnv = getApiScenarioEnv(definition);
        // 所有请求非全路径检查环境
        if (!apiScenarioEnv.getFullUrl()) {
            try {
                if (envMap == null || envMap.isEmpty()) {
                    isEnv = false;
                } else {
                    Set<String> projectIds = apiScenarioEnv.getProjectIds();
                    projectIds.remove(null);
                    if (CollectionUtils.isNotEmpty(envMap.keySet())) {
                        for (String id : projectIds) {
                            Project project = projectMapper.selectByPrimaryKey(id);
                            String s = envMap.get(id);
                            if (project == null) {
                                s = envMap.get(projectId);
                            }
                            if (StringUtils.isBlank(s)) {
                                isEnv = false;
                                break;
                            } else {
                                ApiTestEnvironmentWithBLOBs env = apiTestEnvironmentMapper.selectByPrimaryKey(s);
                                if (env == null) {
                                    isEnv = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        isEnv = false;
                    }
                }
            } catch (Exception e) {
                isEnv = false;
                LogUtil.error(e.getMessage(), e);
            }
        }

        // 1.8 之前环境是 environmentId
        if (!isEnv) {
            if (StringUtils.isNotBlank(envId)) {
                ApiTestEnvironmentWithBLOBs env = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
                if (env != null) {
                    isEnv = true;
                }
            }
        }
        return isEnv;
    }

    public ScenarioEnv getApiScenarioEnv(String definition) {
        ScenarioEnv env = new ScenarioEnv();
        if (StringUtils.isEmpty(definition)) {
            return env;
        }
        List<MsTestElement> hashTree = UiGenerateHashTreeUtil.getHashTreeByScenario(definition);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            // 过滤掉禁用的步骤
            hashTree = hashTree.stream().filter(item -> item.isEnable()).collect(Collectors.toList());
        }
        List<Boolean> hasFullUrlList = new ArrayList<>();
        for (MsTestElement testElement : hashTree) {
            this.formatElement(testElement, env, hasFullUrlList);
            if (CollectionUtils.isNotEmpty(testElement.getHashTree())) {
                getHashTree(testElement.getHashTree(), env, hasFullUrlList);
            }
        }
        env.setFullUrl(!hasFullUrlList.contains(false));
        return env;
    }

    private void formatElement(MsTestElement testElement, ScenarioEnv env, List<Boolean> hasFullUrlList) {
        if (StringUtils.equals(MsTestElementConstants.REF.name(), testElement.getReferenced())) {
            UiScenarioWithBLOBs uiScenario= uiScenarioMapper.selectByPrimaryKey(testElement.getId());
            if (uiScenario != null) {
                env.getProjectIds().add(uiScenario.getProjectId());
                String scenarioDefinition = uiScenario.getScenarioDefinition();
                JSONObject element = JSONUtil.parseObject(scenarioDefinition);
                ElementUtil.dataFormatting(element);
                testElement.setHashTree(UiGenerateHashTreeUtil.getHashTreeByScenario(scenarioDefinition));
            }
        }
        if (StringUtils.equals(testElement.getType(), SCENARIO)
                && !((MsUiScenario) testElement).getEnvironmentEnable()) {
            env.getProjectIds().add(testElement.getProjectId());
        }
    }


    private void getHashTree(List<MsTestElement> tree, ScenarioEnv env, List<Boolean> hasFullUrlList) {
        try {
            // 过滤掉禁用的步骤
            tree = tree.stream().filter(item -> item.isEnable()).collect(Collectors.toList());
            for (MsTestElement element : tree) {
                this.formatElement(element, env, hasFullUrlList);
                if (CollectionUtils.isNotEmpty(element.getHashTree())) {
                    getHashTree(element.getHashTree(), env, hasFullUrlList);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 设置场景的运行环境 环境组/环境JSON
     *
     * @param uiScenarioWithBLOBs
     */
    public void setScenarioEnv(UiScenarioWithBLOBs uiScenarioWithBLOBs , RunScenarioRequest request) {

        String environmentType = uiScenarioWithBLOBs.getEnvironmentType();
        String environmentJson = uiScenarioWithBLOBs.getEnvironmentJson();
        String environmentGroupId = uiScenarioWithBLOBs.getEnvironmentGroupId();
        if (StringUtils.isBlank(environmentType)) {
            environmentType = EnvironmentType.JSON.toString();
        }
        String definition = uiScenarioWithBLOBs.getScenarioDefinition();
        JSONObject element = JSONUtil.parseObject(definition);
        ElementUtil.dataFormatting(element);
        definition = element.toString();
        MsUiScenario scenario = JSON.parseObject(definition, MsUiScenario.class);
        UiGenerateHashTreeUtil.parse(definition, scenario);
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString()) && StringUtils.isNotEmpty(environmentJson)) {
            scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class));
        } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.toString())) {
            Map<String, String> map = baseEnvironmentGroupProjectService.getEnvMap(environmentGroupId);
            scenario.setEnvironmentMap(map);
        }
        if (request != null && request.getConfig() != null && request.getConfig().getEnvMap() != null && !request.getConfig().getEnvMap().isEmpty()) {
            scenario.setEnvironmentMap(request.getConfig().getEnvMap());
        }
        uiScenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(scenario));
    }

    public void setEnvConfig(Map<String, String> environmentMap, ParameterConfig config) {
        final Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (MapUtils.isNotEmpty(environmentMap)) {
            environmentMap.keySet().forEach(projectId -> {
                BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
                ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentMap.get(projectId));
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setEnvironmentId(environment.getId());
                    envConfig.put(projectId, env);
                }
            });
            config.setConfig(envConfig);
        }
    }


}
