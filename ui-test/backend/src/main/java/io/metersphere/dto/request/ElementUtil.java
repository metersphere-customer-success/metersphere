package io.metersphere.dto.request;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.commons.constants.DelimiterConstants;
import io.metersphere.commons.constants.LoopConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.EnvironmentConfig;
import io.metersphere.dto.EnvironmentType;
import io.metersphere.dto.ScenarioVariable;
import io.metersphere.dto.request.controller.MsLoopController;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.hashtree.MsUiScenario;
import io.metersphere.i18n.Translator;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import io.metersphere.utils.ApiFileUtils;
import io.metersphere.utils.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ElementUtil {
    private static final String PRE = "PRE";
    private static final String POST = "POST";
    private static final String ASSERTIONS = "ASSERTIONS";
    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;
    public static final String HASH_TREE = "hashTree";


    public static void addCsvDataSet(HashTree tree, List<ScenarioVariable> variables, ParameterConfig config, String shareMode) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCSVValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list) && CollectionUtils.isNotEmpty(config.getTransferVariables())) {
                list = config.getTransferVariables().stream().filter(ScenarioVariable::isCSVValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CSVDataSet csvDataSet = new CSVDataSet();
                    csvDataSet.setEnabled(true);
                    csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
                    csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    csvDataSet.setName(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName());
                    csvDataSet.setProperty("fileEncoding", StringUtils.isEmpty(item.getEncoding()) ? "UTF-8" : item.getEncoding());
                    if (CollectionUtils.isEmpty(item.getFiles())) {
                        MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                    } else {
                        BodyFile file = item.getFiles().get(0);
                        String path = BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName();
                        if (StringUtils.equalsIgnoreCase(file.getStorage(), StorageConstants.FILE_REF.name())) {
                            path = ApiFileUtils.getFilePath(file);
                        }
                        if (!config.isOperating() && !new File(path).exists()) {
                            MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                        }
                        csvDataSet.setProperty("filename", path);
                    }
                    csvDataSet.setIgnoreFirstLine(false);
                    csvDataSet.setProperty("shareMode", shareMode);
                    csvDataSet.setProperty("recycle", true);
                    csvDataSet.setProperty("delimiter", item.getDelimiter());
                    csvDataSet.setProperty("quotedData", item.isQuotedData());
                    csvDataSet.setComment(StringUtils.isEmpty(item.getDescription()) ? "" : item.getDescription());
                    tree.add(csvDataSet);
                });
            }
        }
    }

    public static void addCounter(HashTree tree, List<ScenarioVariable> variables, boolean isInternal) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCounterValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CounterConfig counterConfig = new CounterConfig();
                    counterConfig.setEnabled(true);
                    counterConfig.setProperty(TestElement.TEST_CLASS, CounterConfig.class.getName());
                    counterConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("CounterConfigGui"));
                    counterConfig.setName(item.getName());
                    if (isInternal) {
                        counterConfig.setStart((item.getStartNumber() + 1));
                    } else {
                        counterConfig.setStart(item.getStartNumber());
                    }
                    counterConfig.setEnd(item.getEndNumber());
                    counterConfig.setVarName(item.getName());
                    counterConfig.setIncrement(item.getIncrement());
                    counterConfig.setFormat(String.valueOf(item.getValue()));
                    counterConfig.setComment(StringUtils.isEmpty(item.getDescription()) ? "" : item.getDescription());
                    tree.add(counterConfig);
                });
            }
        }
    }

    public static void addRandom(HashTree tree, List<ScenarioVariable> variables) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isRandom).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    RandomVariableConfig randomVariableConfig = new RandomVariableConfig();
                    randomVariableConfig.setEnabled(true);
                    randomVariableConfig.setProperty(TestElement.TEST_CLASS, RandomVariableConfig.class.getName());
                    randomVariableConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    randomVariableConfig.setName(item.getName());
                    randomVariableConfig.setProperty("variableName", item.getName());
                    randomVariableConfig.setProperty("outputFormat", String.valueOf(item.getValue()));
                    randomVariableConfig.setProperty("minimumValue", item.getMinNumber());
                    randomVariableConfig.setProperty("maximumValue", item.getMaxNumber());
                    randomVariableConfig.setComment(StringUtils.isEmpty(item.getDescription()) ? "" : item.getDescription());
                    tree.add(randomVariableConfig);
                });
            }
        }
    }

    public static String getFullPath(MsTestElement element, String path) {
        if (element.getParent() == null) {
            return path;
        }
        if (MsTestElementConstants.LoopController.name().equals(element.getType())) {
            MsLoopController loopController = (MsLoopController) element;
            if (StringUtils.equals(loopController.getLoopType(), LoopConstants.WHILE.name()) && loopController.getWhileController() != null) {
                path = "While 循环" + DelimiterConstants.STEP_DELIMITER.toString() + "While 循环-" + "${MS_LOOP_CONTROLLER_CONFIG}";
            }
            if (StringUtils.equals(loopController.getLoopType(), LoopConstants.FOREACH.name()) && loopController.getForEachController() != null) {
                path = "ForEach 循环" + DelimiterConstants.STEP_DELIMITER.toString() + " ForEach 循环-" + "${MS_LOOP_CONTROLLER_CONFIG}";
            }
            if (StringUtils.equals(loopController.getLoopType(), LoopConstants.LOOP_COUNT.name()) && loopController.getCountController() != null) {
                path = "次数循环" + DelimiterConstants.STEP_DELIMITER.toString() + "次数循环-" + "${MS_LOOP_CONTROLLER_CONFIG}";
            }
        } else {
            path = StringUtils.isEmpty(element.getName()) ? element.getType() : element.getName() + DelimiterConstants.STEP_DELIMITER.toString() + path;
        }
        return getFullPath(element.getParent(), path);
    }

    public static String getParentName(MsTestElement parent) {
        if (parent != null) {
            // 获取全路径以备后面使用
            String fullPath = getFullPath(parent, new String());
            return fullPath + DelimiterConstants.SEPARATOR.toString() + parent.getName();
        }
        return "";
    }

    public static String getFullIndexPath(MsTestElement element, String path) {
        if (element == null || element.getParent() == null) {
            return path;
        }
        path = element.getIndex() + "_" + path;
        return getFullIndexPath(element.getParent(), path);
    }

    public static boolean isURL(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return false;
            }
            new URL(str);
            return true;
        } catch (Exception e) {
            // 支持包含变量的url
            if (str.matches("^(http|https|ftp)://.*$") && str.matches(".*://\\$\\{.*$")) {
                return true;
            }
            return false;
        }
    }

    public static <T> List<T> findFromHashTreeByType(MsTestElement hashTree, Class<T> clazz, List<T> requests) {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        if (clazz.isInstance(hashTree)) {
            requests.add((T) hashTree);
        } else {
            if (hashTree != null) {
                LinkedList<MsTestElement> childHashTree = hashTree.getHashTree();
                if (CollectionUtils.isNotEmpty(childHashTree)) {
                    for (MsTestElement item : childHashTree) {
                        findFromHashTreeByType(item, clazz, requests);
                    }
                }
            }
        }
        return requests;
    }

    public final static Map<String, String> uiClazzMap = new HashMap<String, String>() {
        {
            put("scenario", "io.metersphere.hashtree.MsUiScenario");
            put("customCommand", "io.metersphere.hashtree.MsUiCustomCommand");
            put("UiScenario", "io.metersphere.hashtree.MsUiScenario");
            put("MsUiCommand", "io.metersphere.hashtree.MsUiCommand");
        }
    };

    public final static HashMap<String, String> clazzMap = new HashMap<String, String>() {
        {
            put("scenario", "io.metersphere.api.dto.definition.request.MsScenario");
            put("HTTPSamplerProxy", "io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy");
            put("DubboSampler", "io.metersphere.api.dto.definition.request.sampler.MsDubboSampler");
            put("JDBCSampler", "io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler");
            put("TCPSampler", "io.metersphere.api.dto.definition.request.sampler.MsTCPSampler");
            put("IfController", "io.metersphere.api.dto.definition.request.controller.MsIfController");
            put("TransactionController", "io.metersphere.api.dto.definition.request.controller.MsTransactionController");
            put("LoopController", "io.metersphere.api.dto.definition.request.controller.MsLoopController");
            put("ConstantTimer", "io.metersphere.api.dto.definition.request.timer.MsConstantTimer");
            put("JSR223Processor", "io.metersphere.api.dto.definition.request.processors.MsJSR223Processor");
            put("JSR223PreProcessor", "io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor");
            put("JSR223PostProcessor", "io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor");
            put("JDBCPreProcessor", "io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor");
            put("JDBCPostProcessor", "io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor");
            put("Assertions", "io.metersphere.api.dto.definition.request.assertions.MsAssertions");
            put("Extract", "io.metersphere.api.dto.definition.request.extract.MsExtract");
            put("JmeterElement", "io.metersphere.api.dto.definition.request.unknown.MsJmeterElement");
            put("TestPlan", "io.metersphere.api.dto.definition.request.MsTestPlan");
            put("ThreadGroup", "io.metersphere.api.dto.definition.request.MsThreadGroup");
            put("DNSCacheManager", "io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager");
            put("DebugSampler", "io.metersphere.api.dto.definition.request.sampler.MsDebugSampler");
            put("AuthManager", "io.metersphere.api.dto.definition.request.auth.MsAuthManager");
        }
    };

    public final static List<String> requests = new ArrayList<String>() {{
        this.add("HTTPSamplerProxy");
        this.add("MsUiCommand");
        this.add("DubboSampler");
        this.add("JDBCSampler");
        this.add("TCPSampler");
        this.add("JSR223Processor");
        this.add("JSR223PreProcessor");
        this.add("JSR223PostProcessor");
        this.add("JDBCPreProcessor");
        this.add("JDBCPostProcessor");
        this.add("JmeterElement");
        this.add("TestPlan");
        this.add("ThreadGroup");
        this.add("DNSCacheManager");
        this.add("DebugSampler");
        this.add("AuthManager");
        this.add("AbstractSampler");
    }};

    private static void formatSampler(JSONObject element) {
        if (element == null || StringUtils.isEmpty(element.optString("type"))) {
            return;
        }
        if (element.get("clazzName") == null && element.optString("type").equals("TCPSampler")) {
            if (element.optString("tcpPreProcessor") != null) {
                JSONObject tcpPreProcessor = new JSONObject(element.optString("tcpPreProcessor"));
                if (tcpPreProcessor != null && tcpPreProcessor.get("clazzName") == null) {
                    tcpPreProcessor.put("clazzName", clazzMap.get(tcpPreProcessor.optString("type")));
                    element.put("tcpPreProcessor", tcpPreProcessor);
                }
            }
        } else if (element.optString("type").equals("HTTPSamplerProxy")) {
            if (element.optString("authManager") != null) {
                JSONObject authManager = new JSONObject(element.optString("authManager"));
                if (authManager != null && authManager.get("clazzName") == null) {
                    authManager.put("clazzName", clazzMap.get(authManager.optString("type")));
                    element.put("authManager", authManager);
                }
            }
        }
    }

    /**
     * 只找出场景直接依赖
     *
     * @param hashTree
     * @param referenceRelationships
     */
    public static void relationships(JSONArray hashTree, List<String> referenceRelationships) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element != null && StringUtils.equals(element.get("type").toString(), "scenario") && StringUtils.equals(element.get("referenced").toString(), "REF")) {
                if (!referenceRelationships.contains(element.get("id").toString())) {
                    referenceRelationships.add(element.get("id").toString());
                }
            } else {
                if (element.has("hashTree")) {
                    JSONArray elementJSONArray = element.optJSONArray("hashTree");
                    relationships(elementJSONArray, referenceRelationships);
                }
            }
        }
    }

    public static void dataFormatting(JSONArray hashTree) {
        if (hashTree == null) return;
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element == null) {
                continue;
            }
            formatSampler(element);
            if (!verifyClazzName(element.optString("clazzName")) && uiClazzMap.containsKey(element.optString("type"))) {
                element.put("clazzName", uiClazzMap.get(element.optString("type")));
            }
            if (element.has("hashTree")) {
                JSONArray elementJSONArray = element.optJSONArray("hashTree");
                dataFormatting(elementJSONArray);
            }
        }
    }

    public static boolean verifyClazzName(String clazzName) {
        if (StringUtils.isBlank(clazzName)) {
            return false;
        }
        if (clazzName.contains("io.metersphere.xpack.ui.hashtree")) {
            return false;
        }
        if (clazzName.contains("io.metersphere.hashtree.MsScenario")) {
            return false;
        }
        return true;
    }

    public static void dataFormatting(JSONObject element) {
        if (element == null) return;
        if (element != null && !verifyClazzName(element.optString("clazzName")) && uiClazzMap.containsKey(element.optString("type"))) {
            element.put("clazzName", uiClazzMap.get(element.optString("type")));
        }
        formatSampler(element);
        if (element != null && element.has("hashTree")) {
            JSONArray elementJSONArray = element.optJSONArray("hashTree");
            dataFormatting(elementJSONArray);
        }
    }

    public static void mergeHashTree(MsTestElement element, LinkedList<MsTestElement> targetHashTree) {
        try {
            LinkedList<MsTestElement> sourceHashTree = element.getHashTree();
            if (CollectionUtils.isNotEmpty(sourceHashTree) && CollectionUtils.isNotEmpty(targetHashTree) && sourceHashTree.size() < targetHashTree.size()) {
                element.setHashTree(targetHashTree);
                return;
            }
            List<String> sourceIds = new ArrayList<>();
            List<String> delIds = new ArrayList<>();
            Map<String, MsTestElement> updateMap = new HashMap<>();
            if (CollectionUtils.isEmpty(sourceHashTree)) {
                if (CollectionUtils.isNotEmpty(targetHashTree)) {
                    element.setHashTree(targetHashTree);
                }
                return;
            }
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (MsTestElement item : targetHashTree) {
                    if (StringUtils.isNotEmpty(item.getId())) {
                        updateMap.put(item.getId(), item);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                for (int i = 0; i < sourceHashTree.size(); i++) {
                    MsTestElement source = sourceHashTree.get(i);
                    if (source != null) {
                        sourceIds.add(source.getId());
                        if (!StringUtils.equals(source.getLabel(), "SCENARIO-REF-STEP") && StringUtils.isNotEmpty(source.getId())) {
                            if (updateMap.containsKey(source.getId())) {
                                sourceHashTree.set(i, updateMap.get(source.getId()));
                            } else {
                                delIds.add(source.getId());
                            }
                        }
                        // 历史数据兼容
                        if (StringUtils.isEmpty(source.getId()) && !StringUtils.equals(source.getLabel(), "SCENARIO-REF-STEP") && i < targetHashTree.size()) {
                            sourceHashTree.set(i, targetHashTree.get(i));
                        }
                    }
                }
            }

            // 删除多余的步骤
            sourceHashTree.removeIf(item -> item != null && delIds.contains(item.getId()));
            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (MsTestElement item : targetHashTree) {
                    if (!sourceIds.contains(item.getId())) {
                        sourceHashTree.add(item);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                element.setHashTree(sourceHashTree);
            }
        } catch (Exception e) {
            element.setHashTree(targetHashTree);
        }
    }

    public static List<JSONObject> mergeHashTree(List<JSONObject> sourceHashTree, List<JSONObject> targetHashTree) {
        try {
            List<String> sourceIds = new ArrayList<>();
            List<String> delIds = new ArrayList<>();
            Map<String, JSONObject> updateMap = new HashMap<>();

            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (int i = 0; i < targetHashTree.size(); i++) {
                    JSONObject item = targetHashTree.get(i);
                    item.put("disabled", true);
                    if (StringUtils.isNotEmpty(item.optString("id"))) {
                        updateMap.put(item.optString("id"), item);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                for (int i = 0; i < sourceHashTree.size(); i++) {
                    JSONObject source = sourceHashTree.get(i);
                    if (source != null) {
                        sourceIds.add(source.optString("id"));
                        if (!StringUtils.equals(source.optString("label"), "SCENARIO-REF-STEP") && StringUtils.isNotEmpty(source.optString("id"))) {
                            if (updateMap.containsKey(source.optString("id"))) {
                                sourceHashTree.set(i, updateMap.get(source.optString("id")));
                            } else {
                                delIds.add(source.optString("id"));
                            }
                        }
                        // 历史数据兼容
                        if (StringUtils.isEmpty(source.optString("id")) && !StringUtils.equals(source.optString("label"), "SCENARIO-REF-STEP") && i < targetHashTree.size()) {
                            sourceHashTree.set(i, targetHashTree.get(i));
                        }
                    }
                }
            }

            // 删除多余的步骤
            for (int i = 0; i < sourceHashTree.size(); i++) {
                JSONObject source = sourceHashTree.get(i);
                if (delIds.contains(source.optString("id"))) {
                    sourceHashTree.remove(i);
                }
            }
            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (int i = 0; i < targetHashTree.size(); i++) {
                    JSONObject item = sourceHashTree.get(i);
                    if (!sourceIds.contains(item.optString("id"))) {
                        sourceHashTree.add(item);
                    }
                }
            }
        } catch (Exception e) {
            return targetHashTree;
        }
        return sourceHashTree;
    }

    public static String hashTreeToString(HashTree hashTree) {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, bas);
            return bas.toString();
        } catch (Exception e) {
            e.printStackTrace();
            io.metersphere.plugin.core.utils.LogUtil.warn("HashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    public static String getResourceId(String resourceId, ParameterConfig config, MsTestElement parent, String indexPath) {
        if (StringUtils.isNotEmpty(config.getScenarioId()) && StringUtils.equals(config.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            resourceId = config.getScenarioId() + "=" + resourceId;
        }
        // 跳过失败重试层
        if (parent != null && StringUtils.equalsAnyIgnoreCase("RetryLoopController", parent.getType())) {
            parent = parent.getParent();
        }
        return resourceId + "_" + ElementUtil.getFullIndexPath(parent, indexPath);
    }

    public static JSR223PreProcessor argumentsToProcessor(Arguments arguments) {
        JSR223PreProcessor processor = new JSR223PreProcessor();
        processor.setEnabled(true);
        processor.setName("User Defined Variables");
        processor.setProperty("scriptLanguage", "beanshell");
        processor.setProperty(TestElement.TEST_CLASS, JSR223PreProcessor.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        StringBuffer script = new StringBuffer();
        if (arguments != null) {
            for (int i = 0; i < arguments.getArguments().size(); ++i) {
                String argValue = arguments.getArgument(i).getValue();
                script.append("vars.put(\"" + arguments.getArgument(i).getName() + "\",\"" + argValue + "\");").append("\n");
            }
            processor.setProperty("script", script.toString());
        }
        return processor;
    }

    public static UserParameters argumentsToUserParameters(Arguments arguments) {
        UserParameters processor = new UserParameters();
        processor.setEnabled(true);
        processor.setName("User Defined Variables");
        processor.setPerIteration(true);
        processor.setProperty(TestElement.TEST_CLASS, UserParameters.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("UserParametersGui"));
        if (arguments != null && arguments.getArguments().size() > 0) {
            List<String> names = new LinkedList<>();
            List<Object> values = new LinkedList<>();
            List<Object> threadValues = new LinkedList<>();
            for (int i = 0; i < arguments.getArguments().size(); ++i) {
                String argValue = arguments.getArgument(i).getValue();
                String name = arguments.getArgument(i).getName();
                names.add(name);
                values.add(argValue);
            }
            processor.setNames(names);
            threadValues.add(values);
            processor.setThreadLists(threadValues);
        }
        return processor;
    }

    public static void setBaseParams(AbstractTestElement sampler, MsTestElement parent, ParameterConfig config, String id, String indexPath) {
        sampler.setProperty("MS-ID", id);
        sampler.setProperty("MS-RESOURCE-ID", ElementUtil.getResourceId(id, config, parent, indexPath));
    }

    public static void accuracyHashTree(HashTree hashTree) {
        Map<Object, HashTree> objects = new LinkedHashMap<>();
        Object groupHashTree = hashTree;
        if (hashTree != null && hashTree.size() > 0) {
            for (Object key : hashTree.keySet()) {
                if (key instanceof TestPlan) {
                    for (Object node : hashTree.get(key).keySet()) {
                        if (node instanceof ThreadGroup) {
                            groupHashTree = hashTree.get(key).get(node);
                        }
                    }
                } else {
                    objects.put(key, hashTree.get(key));
                }
            }
        }
        if (!objects.isEmpty() && groupHashTree instanceof HashTree) {
            for (Object key : objects.keySet()) {
                hashTree.remove(key);
                ((HashTree) groupHashTree).add(key, objects.get(key));
            }
        }
    }

    private static final List<String> preOperates = new ArrayList<String>() {{
        this.add("JSR223PreProcessor");
        this.add("JDBCPreProcessor");
        this.add("ConstantTimer");
    }};
    private static final List<String> postOperates = new ArrayList<String>() {{
        this.add("JSR223PostProcessor");
        this.add("JDBCPostProcessor");
        this.add("Extract");
    }};


    public static void copyBean(JSONObject target, JSONObject source) {
        if (source == null || target == null) {
            return;
        }
        for (String key : target.keySet()) {
            if (source.has(key) && !StringUtils.equalsIgnoreCase(key, "hashTree")) {
                target.put(key, source.get(key));
            }
        }
    }

    private static List<MsTestElement> orderList(List<MsTestElement> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (StringUtils.isEmpty(list.get(i).getIndex())) {
                    list.get(i).setIndex(String.valueOf(i));
                }
            }
        }
        return list;
    }

    public static Map<String, List<JSONObject>> group(JSONArray elements) {
        Map<String, List<JSONObject>> groupMap = new LinkedHashMap<>();
        if (elements != null) {
            for (int i = 0; i < elements.length(); i++) {
                JSONObject item = elements.optJSONObject(i);
                if ("Assertions".equals(item.optString("type"))) {
                    if (groupMap.containsKey(ASSERTIONS)) {
                        groupMap.get(ASSERTIONS).add(item);
                    } else {
                        groupMap.put(ASSERTIONS, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                } else if (preOperates.contains(item.optString("type"))) {
                    if (groupMap.containsKey(PRE)) {
                        groupMap.get(PRE).add(item);
                    } else {
                        groupMap.put(PRE, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                } else if (postOperates.contains(item.optString("type"))) {
                    if (groupMap.containsKey(POST)) {
                        groupMap.get(POST).add(item);
                    } else {
                        groupMap.put(POST, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                }
            }
        }
        return groupMap;
    }

    public static List<MsTestElement> order(List<MsTestElement> elements) {
        List<MsTestElement> elementList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(elements)) {
            Map<String, List<MsTestElement>> groupMap = new LinkedHashMap<>();
            elements.forEach(item -> {
                if ("Assertions".equals(item.getType())) {
                    if (groupMap.containsKey(ASSERTIONS)) {
                        groupMap.get(ASSERTIONS).add(item);
                    } else {
                        groupMap.put(ASSERTIONS, new LinkedList<MsTestElement>() {{
                            this.add(item);
                        }});
                    }
                } else if (preOperates.contains(item.getType())) {
                    if (groupMap.containsKey(PRE)) {
                        groupMap.get(PRE).add(item);
                    } else {
                        groupMap.put(PRE, new LinkedList<MsTestElement>() {{
                            this.add(item);
                        }});
                    }
                } else if (postOperates.contains(item.getType())) {
                    if (groupMap.containsKey(POST)) {
                        groupMap.get(POST).add(item);
                    } else {
                        groupMap.put(POST, new LinkedList<MsTestElement>() {{
                            this.add(item);
                        }});
                    }
                } else {
                    elementList.add(item);
                }
            });
            if (CollectionUtils.isNotEmpty(groupMap.get(PRE))) {
                elementList.addAll(orderList(groupMap.get(PRE)));
            }
            if (CollectionUtils.isNotEmpty(groupMap.get(POST))) {
                elementList.addAll(orderList(groupMap.get(POST)));
            }
            if (CollectionUtils.isNotEmpty(groupMap.get(ASSERTIONS))) {
                elementList.addAll(groupMap.get(ASSERTIONS));
            }
        }
        return elementList;
    }

    public static String getEvlValue(String evlValue) {
        try {
            if (StringUtils.startsWith(evlValue, "@")) {
                return ScriptEngineUtils.calculate(evlValue);
            } else {
                return ScriptEngineUtils.buildFunctionCallString(evlValue);
            }
        } catch (Exception e) {
            return evlValue;
        }
    }

    public static void dataSetDomain(JSONArray hashTree, MsParameter msParameter) {
        try {
            UiScenarioMapper uiScenarioMapper = CommonBeanFactory.getBean(UiScenarioMapper.class);
            BaseEnvGroupProjectService environmentGroupProjectService = CommonBeanFactory.getBean(BaseEnvGroupProjectService.class);
            BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);

            for (int i = 0; i < hashTree.length(); i++) {
                JSONObject element = hashTree.optJSONObject(i);
                boolean isScenarioEnv = false;
                ParameterConfig config = new ParameterConfig();
                MsUiScenario scenario = JSON.parseObject(element.toString(), MsUiScenario.class);
                if (scenario.getEnvironmentEnable()) {
                    isScenarioEnv = true;
                    Map<String, String> environmentMap = new HashMap<>();
                    UiScenarioWithBLOBs uiScenarioWithBLOBs = uiScenarioMapper.selectByPrimaryKey(scenario.getId());
                    if (uiScenarioWithBLOBs == null) {
                        continue;
                    }
                    if (StringUtils.equals(uiScenarioWithBLOBs.getEnvironmentType(), EnvironmentType.GROUP.name())) {
                        environmentMap = environmentGroupProjectService.getEnvMap(uiScenarioWithBLOBs.getEnvironmentGroupId());
                    } else if (StringUtils.equals(uiScenarioWithBLOBs.getEnvironmentType(), EnvironmentType.JSON.name())) {
                        environmentMap = JSON.parseObject(uiScenarioWithBLOBs.getEnvironmentJson(), Map.class);
                    }
                    Map<String, EnvironmentConfig> envConfig = new HashMap<>();
                    if (environmentMap != null && !environmentMap.isEmpty()) {
                        for (String projectId : environmentMap.keySet()) {
                            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentMap.get(projectId));
                            if (environment != null && environment.getConfig() != null) {
                                EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                                env.setEnvironmentId(environment.getId());
                                envConfig.put(projectId, env);
                            }
                        }
                        config.setConfig(envConfig);
                    }
                }
                if (element.has(HASH_TREE)) {
                    JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                    if (isScenarioEnv) {
                        dataSetDomain(elementJSONArray, config);
                    } else {
                        dataSetDomain(elementJSONArray, msParameter);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }


}