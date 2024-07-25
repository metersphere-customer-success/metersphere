package io.metersphere.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.*;
import io.metersphere.dto.api.UiExecutionQueueParam;
import io.metersphere.dto.request.ElementUtil;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.hashtree.*;
import io.metersphere.jmeter.ResourcePoolCalculation;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.UiEnvironmentService;
import io.metersphere.vo.BooleanPool;
import io.metersphere.vo.extraction.UiExtractElementVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class UiGenerateHashTreeUtil {
    public final static String HASH_TREE_ELEMENT = "hashTree";
    public final static String TYPE = "type";
    public final static String RESOURCE_ID = "resourceId";
    public final static String RETRY = "MsRetry_";
    public final static String LOOP = "LoopController";
    public final static String SET_REPORT = "setReport";
    public final static String SCENARIO_DEFINITION = "scenarioDefinition";
    public final static String PROJECT_ID = "projectId";
    public final static String SCENARIO = "scenario";
    public final static String ID = "id";

    public static void parse(String scenarioDefinition, MsUiScenario scenario) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            JSONObject element = new JSONObject(scenarioDefinition);
            ElementUtil.dataFormatting(element);
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (element != null && element.optJSONArray(HASH_TREE_ELEMENT) != null) {
                LinkedList<MsTestElement> elements = mapper.readValue(Optional.ofNullable(element.optJSONArray(HASH_TREE_ELEMENT)).orElse(new JSONArray()).toString(),
                        new TypeReference<>() {
                        });
                scenario.setHashTree(elements);
            }
            if (element != null && element.optJSONArray("variables") != null) {
                LinkedList<ScenarioVariable> variables = mapper.readValue(Optional.ofNullable(element.optJSONArray("variables")).orElse(new JSONArray()).toString(),
                        new TypeReference<>() {
                        });
                scenario.setVariables(variables);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }


    public static HashTree generateHashTree(UiScenarioWithBLOBs item, JmeterRunRequestDTO runRequest) {
        return generateHashTree(item, runRequest, null);
    }

    public static HashTree generateHashTree(UiScenarioWithBLOBs item, JmeterRunRequestDTO runRequest, UiExecutionQueueParam param) {
        HashTree jmeterHashTree = new HashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        String reportId = runRequest.getReportId();
        if (StringUtils.equals(runRequest.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            reportId = runRequest.getTestPlanReportId();
        }
        try {
            MsThreadGroup group = new MsThreadGroup();
            group.setLabel(item.getName());
            group.setName(runRequest.getReportId());
            String definition = convertJacksonType(item.getScenarioDefinition());
            MsUiScenario scenario = JSON.parseObject(definition, MsUiScenario.class);
            scenario.setId(item.getId());
            if (param != null) {
                if (param.getHeadlessEnabled() != null) {
                    scenario.setHeadlessEnabled(param.getHeadlessEnabled());
                }
                if (StringUtils.isNotBlank(param.getBrowser())) {
                    scenario.setBrowser(scenario.getBrowserByName(param.getBrowser()));
                }
            }

            group.setOnSampleError(scenario.getOnSampleError());
            UiGenerateHashTreeUtil.parse(definition, scenario);
            scenario.setEnvironmentJson(item.getEnvironmentJson());
            MsUiRetryLoopController loopController = wrapWithRetryController(scenario, runRequest.isRetryEnable() ? runRequest.getRetryNum() : 0);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.registerSubtypes(MsUiScenario.class, MsUiCommand.class);

            LinkedList<MsTestElement> scenarios = new LinkedList<>();
            scenarios.add(loopController);

            group.setHashTree(scenarios);
            testPlan.getHashTree().add(group);

            LoggerUtil.info("报告ID: " + runRequest.getReportId() + " 场景资源：" + item.getName() + ", 生成执行脚本JMX成功");
        } catch (Exception ex) {
            // todo ui 异常重试
//            remakeException(runRequest);
            LoggerUtil.error("报告ID: " + runRequest.getReportId() + " 场景资源：" + item.getName() + ", 生成执行脚本失败", ex);
        }
        ParameterConfig config = new ParameterConfig();
        config.setScenarioId(item.getId());
        config.setReportType(runRequest.getReportType());
        config.setReportId(reportId);
        config.setRetryEnable(runRequest.isRetryEnable());
        config.setRetryNumber(runRequest.getRetryNum());
        if(StringUtils.isNotBlank(runRequest.getRunMode()) && runRequest.getRunMode().contains("PLAN")){
            config.setRequestOriginator(SystemConstants.UIRequestOriginatorEnum.TEST_PLAN.getName());
        }
        if (param != null && MapUtils.isNotEmpty(param.getEnvMap())) {
            Map<String, EnvironmentConfig> environmentConfigMap = new HashMap<>();
            param.getEnvMap().keySet().forEach(k -> {
                ApiTestEnvironmentWithBLOBs env = CommonBeanFactory.getBean(BaseEnvironmentService.class).get(param.getEnvMap().get(k));
                if (env != null) {
                    String cnfStr = env.getConfig();
                    environmentConfigMap.put(k, CommonBeanFactory.getBean(UiEnvironmentService.class).replaceDomainVars(JSON.parseObject(cnfStr, EnvironmentConfig.class)));
                }
            });
            config.setConfig(environmentConfigMap);
        }
        config.setProjectId(item.getProjectId());
        List<KeyValue> headers = new ArrayList<>();
        headers.add(new KeyValue("scenarioName", item.getName()));
        config.setHeaders(headers);
        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), config);
        return jmeterHashTree;
    }

    private static MsUiRetryLoopController wrapWithRetryController(MsUiScenario scenario, long retryNum) {
        MsUiRetryLoopController loopController = new MsUiRetryLoopController();
        loopController.setClazzName(MsUiRetryLoopController.class.getCanonicalName());
        loopController.setName(RETRY + scenario.getId());
        loopController.setRetryNum(retryNum);
        loopController.setEnable(true);
        loopController.setResourceId(UUID.randomUUID().toString());

        LinkedList<MsTestElement> hashTree = new LinkedList<>();
        hashTree.add(scenario);
        loopController.setHashTree(hashTree);
        return loopController;
    }

    public static LinkedList<MsTestElement> getHashTreeByScenario(String scenarioDefinition) {
        ObjectMapper mapper = CommonBeanFactory.getBean(ObjectMapper.class);
        JSONObject element = new JSONObject(UiGenerateHashTreeUtil.convertJacksonType(scenarioDefinition));
        ElementUtil.dataFormatting(element);
        LinkedList<MsTestElement> r = new LinkedList<>();
        try {
            //解析场景变量为 store 指令进行导出
            List<MsTestElement> variableHashTree = getUIVariableHashTree(scenarioDefinition);
            LinkedList<MsTestElement> hashTree = mapper.readValue(Optional.ofNullable(element.optJSONArray(HASH_TREE_ELEMENT)).orElse(new JSONArray()).toString(), new TypeReference<>() {
            });
            r.addAll(variableHashTree);
            r.addAll(hashTree);
            return r;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    /**
     * UI 场景变量提取
     *
     * @param scenarioDefinition
     * @return
     */
    public static List<MsTestElement> getUIVariableHashTree(String scenarioDefinition) {
        ObjectMapper mapper = CommonBeanFactory.getBean(ObjectMapper.class);
        JSONObject element = new JSONObject(scenarioDefinition);
        LinkedList<MsTestElement> r = new LinkedList<>();
        try {
            if (element.has("variables")) {
                LinkedList<ScenarioVariable> variables = mapper.readValue(Optional.ofNullable(element.optJSONArray("variables")).orElse(new JSONArray()).toString(), new TypeReference<>() {
                });
                if (CollectionUtils.isEmpty(variables)) {
                    return r;
                }
                LinkedList converted = variables.stream().map(UiGenerateHashTreeUtil::variableToMsUiCommand).collect(Collectors.toCollection(LinkedList::new));
                MsUiCommand command = new MsUiCommand();
                //导出环境变量的时候，对应的指令为 cmdExtractElement
                command.setCommand("cmdExtraction");
                command.setCommandType(CommandType.COMMAND_TYPE_COMBINATION_PROXY);
                command.setViewType("dataExtraction");
                command.setHashTree(converted);
                r.add(command);
            }
            return r;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    public static MsTestElement variableToMsUiCommand(ScenarioVariable variable) {
        UiExtractElementVO vo = new UiExtractElementVO();
        vo.setExtractType(variable.isEnable() ? "store" : "//store");

        MsUiCommand command = new MsUiCommand();
        //导出环境变量的时候，对应的指令为 cmdExtractElement
        command.setCommand("cmdExtractElement");
        vo.setVarValue(String.valueOf(variable.getValue()));
        vo.setVarName(variable.getName());

        switch (variable.getType()) {
            case "CONSTANT":
                break;
            case "STRING":
                vo.setIfString(true);
                break;
            case "ARRAY":
                break;
            case "JSONObject":
                break;
            case "NUMBER":
                break;
            default:
                break;
        }
        if (StringUtils.isNotBlank(String.valueOf(variable.getValue()))) {
            if (!isObject(String.valueOf(variable.getValue()))) {
                vo.setIfString(true);
            }
        }
        command.setVo(vo);
        command.setCommandType(CommandType.COMMAND_TYPE_PROXY);
        return command;
    }

    private static boolean isObject(String param) {
        try {
            new JSONObject(param);
        } catch (Exception e) {
            try {
                new JSONArray(param);
            } catch (Exception e1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSetReport(RunModeConfigDTO config) {
        return config != null && isSetReport(config.getReportType()) && StringUtils.isNotEmpty(config.getReportName());
    }

    public static boolean isSetReport(String reportType) {
        return StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString());
    }

    public static BooleanPool isResourcePool(String id) {
        BooleanPool pool = new BooleanPool();
        pool.setPool(StringUtils.isNotEmpty(id));
        if (pool.isPool()) {
            TestResourcePool resourcePool = CommonBeanFactory.getBean(TestResourcePoolMapper.class).selectByPrimaryKey(id);
            pool.setK8s(resourcePool != null && resourcePool.getApi() && resourcePool.getType().equals(ResourcePoolTypeEnum.K8S.name()));
        }
        return pool;
    }

    public static List<JvmInfoDTO> setPoolResource(String id) {
        if (isResourcePool(id).isPool() && !isResourcePool(id).isK8s()) {
            ResourcePoolCalculation resourcePoolCalculation = CommonBeanFactory.getBean(ResourcePoolCalculation.class);
            return resourcePoolCalculation.getPools(id);
        }
        return new LinkedList<>();
    }

    /**
     * 迁移后替换旧的 jackson type 字段
     *
     * @return
     */
    public static String convertJacksonType(String originStr) {
        return io.metersphere.utils.JSON.convertJacksonType(originStr);
    }

    /**
     * 迁移后替换旧的状态字段
     *
     * @return
     */
    public static String convertStatus(String originStr) {
        originStr = originStr.replace("success", ReportStatus.SUCCESS.name())
                .replace("fail", ReportStatus.ERROR.name())
                .replace("Timeout", ReportStatus.ERROR.name())
                .replace("Error", ReportStatus.ERROR.name())
                .replace("Success", ReportStatus.SUCCESS.name())
                .replace("unexecute", ReportStatus.PENDING.name())
                .replace("STOP", ReportStatus.STOPPED.name());
        return originStr;
    }

}
