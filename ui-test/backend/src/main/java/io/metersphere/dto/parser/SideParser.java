package io.metersphere.dto.parser;


import com.googlecode.jmeter.plugins.webdriver.config.RemoteBrowser;
import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.dto.parser.constants.CommandTokenType;
import io.metersphere.dto.parser.domain.CommandToken;
import io.metersphere.dto.parser.domain.CommandTokenList;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.hashtree.MsUiScenario;
import io.metersphere.intf.AbstractScenarioParser;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.UiScenarioModuleService;
import io.metersphere.utils.StringUtil;
import io.metersphere.utils.TemplateUtils;
import io.metersphere.vo.browser.operation.UiOpenVO;
import io.metersphere.vo.program.controller.UiForEachVo;
import io.metersphere.vo.program.controller.UiWhileVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SideParser extends AbstractScenarioParser<ScenarioImport> {

    private ModuleNode selectModule;

    private String selectModulePath;

    private String urlPatternWithSlashStr =  "^(http|https|ftp):\\/\\/";

    private Pattern urlPattern = Pattern.compile(urlPatternWithSlashStr);

    private static final String UI_SCENARIO = "scenario";

    @Override
    public ScenarioImport parse(InputStream source, UiTestImportRequest request) {
        String sideStr = TemplateUtils.readContent(source);
        this.projectId = request.getProjectId();
        SideDTO sideDTO = JSON.parseObject(sideStr, SideDTO.class);
        ScenarioImport apiImport = new ScenarioImport();
        List<UiScenarioWithBLOBs> results = new ArrayList<>();
        this.selectModule = CommonBeanFactory.getBean(UiScenarioModuleService.class).get(request.getModuleId());

        if (this.selectModule != null) {
            this.selectModulePath = getSelectModulePath(this.selectModule.getName(), this.selectModule.getParentId());
        } else {
            if (UI_SCENARIO.equals(request.getScenarioType())) {
                this.selectModule = CommonBeanFactory.getBean(UiScenarioModuleService.class).getDefaultNode(this.projectId);
            } else {
                this.selectModule = CommonBeanFactory.getBean(UiScenarioModuleService.class).getDefaultCustomNode(this.projectId);
            }
            this.selectModulePath = "";
        }
        parseItem(sideDTO, results, this.selectModule, request.getProjectId());
        refactorName(results);
        Collections.reverse(results); // 调整顺序
        apiImport.setData(results);
        return apiImport;
    }

    /**
     * 名字唯一
     *
     * @param results
     */
    private void refactorName(List<UiScenarioWithBLOBs> results) {
        if (CollectionUtils.isEmpty(results)) return;
        Map<String, List<UiScenarioWithBLOBs>> countMap = results.stream().collect(Collectors.groupingBy(UiScenarioWithBLOBs::getName));
        AtomicInteger i = new AtomicInteger();
        countMap.entrySet().forEach(e -> {
            if (e.getValue().size() > 1) {
                i.set(1);
                e.getValue().forEach(s -> {
                    s.setName(StringUtils.join(s.getName(), "_", i.getAndIncrement()));
                });
            }
        });
    }

    protected void parseItem(SideDTO sideDTO, List<UiScenarioWithBLOBs> results, ModuleNode parentModule, String projectId) {
        AtomicInteger count = new AtomicInteger();
        if (sideDTO == null || sideDTO.getTests() == null) {
            LogUtil.error("导入 ui 场景为空!" + JSON.toJSONString(sideDTO));
            return;
        }
        for (SideDTO.TestsDTO item : sideDTO.getTests()) {
            UiScenarioWithBLOBs request = new UiScenarioWithBLOBs();
            request.setName(item.getName());
            request.setProjectId(projectId);
            MsUiScenario msUiScenario = new MsUiScenario();
            LinkedList<MsTestElement> hashTree = parseMsTestElement(item, count);
            LinkedList<ScenarioVariable> variables = parseVariable(item, count);
            request.setStepTotal(hashTree.size());
            request.setLevel("P0");
            setBaseUrl(hashTree, sideDTO.getUrl());
            msUiScenario.setHashTree(hashTree);
            msUiScenario.setBaseURL(sideDTO.getUrl());
            //默认谷歌浏览器
            msUiScenario.setBrowser(RemoteBrowser.CHROME);
            msUiScenario.setResourceId(UUID.randomUUID().toString());
            msUiScenario.setVariables(variables);
            request.setScenarioDefinition(JSON.toJSONString(msUiScenario));
            if (parentModule != null) {
                if (StringUtils.isNotBlank(this.selectModulePath)) {
                    request.setModulePath(this.selectModulePath + "/" + parentModule.getName());
                } else {
                    request.setModulePath("/" + parentModule.getName());
                }
                request.setModuleId(parentModule.getId());
            }
            results.add(request);
        }
    }

    private LinkedList<ScenarioVariable> parseVariable(SideDTO.TestsDTO testsDTO, AtomicInteger count) {
        CommandTokenList tokenList = new CommandTokenizer().tokenize(testsDTO);
        CommandTokenList variableTokenList = new CommandTokenList();
        boolean end = false;
        for (CommandToken commandToken : tokenList.getTokenList()) {
            if (!end && commandToken.getTokenType() == CommandTokenType.CMDEXTRACTELEMENT) {
                variableTokenList.add(commandToken);
            } else {
                //除了提取元素和提取窗口，遇到其他指令说明环境变量结束了不应该继续读取下去
                if (commandToken.getTokenType() != CommandTokenType.CMDEXTRACTWINDOW) {
                    end = true;
                }
            }
        }
        return variableTokenList.getTokenList().stream().map(this::toScenarioVariable).collect(Collectors.toCollection(LinkedList::new));
    }

    private ScenarioVariable toScenarioVariable(CommandToken commandToken) {
        SideDTO.TestsDTO.CommandsDTO commandsDTO = commandToken.getValue();
        ScenarioVariable variable = new ScenarioVariable();
        variable.setId(UUID.randomUUID().toString());
        variable.setName(commandsDTO.getValue());
        variable.setValue(TemplateUtils.resetQuotes(Optional.ofNullable(commandsDTO.getTarget()).orElse("")));
        variable.setType(io.metersphere.utils.JSON.getJSONType(Optional.ofNullable(commandsDTO.getTarget()).orElse("")));
        return variable;
    }


    private void setBaseUrl(LinkedList<MsTestElement> hashTree, String url) {
        if (StringUtils.isNotBlank(url)) {
            url = url.trim();
            String baseUrl = url;
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            Matcher m = urlPattern.matcher(url);
            if (m.find()) {
                url = m.group();
            }

            for (MsTestElement msTestElement : hashTree) {
                MsUiCommand currentCommand = (MsUiCommand) msTestElement;
                //存在 baseURL 并且 open 指令 target 不是 url 协议开头则拼接一个 baseURL 进去
                if ("cmdOpen".equalsIgnoreCase(currentCommand.getCommand()) && !((UiOpenVO) currentCommand.getVo()).getWebUrl().matches("^(http|https|ftp):\\/\\/.*")) {
                    if (StringUtils.isNotBlank(((UiOpenVO) currentCommand.getVo()).getWebUrl())) {
                        if (((UiOpenVO) currentCommand.getVo()).getWebUrl().startsWith("/")) {
                            ((UiOpenVO) currentCommand.getVo()).setWebUrl(baseUrl);
                        } else {
                            ((UiOpenVO) currentCommand.getVo()).setWebUrl(url + "/" + ((UiOpenVO) currentCommand.getVo()).getWebUrl());
                        }
                    }
                }
            }
        }
    }

    protected LinkedList<MsTestElement> parseMsTestElement(SideDTO.TestsDTO testsDTO, AtomicInteger count) {
        CommandTokenList tokenList = new CommandTokenizer().tokenize(testsDTO);
        return parse(tokenList, count);
    }

    private LinkedList<MsTestElement> parse(CommandTokenList tokenList, AtomicInteger count) {
        LinkedList<MsTestElement> msTestElements = new LinkedList<>();
        for (CommandToken commandToken : tokenList.getTokenList()) {
            msTestElements.add(commandToken.getTokenType().getVo().toMsUiCommand(commandToken.getValue()));
        }
        //处理前后置等等
        msTestElements = processElement(msTestElements, count);
        //前端side复制时，id没有被打乱会产生重复id，这里防止前端渲染和报告产生对应不上的问题
        distinctId(msTestElements);
        for (int i = 0; i < msTestElements.size(); i++) {
            if (StringUtils.isBlank(msTestElements.get(i).getIndex())) {
                msTestElements.get(i).setIndex(String.valueOf(i+1));
            }
        }
        return msTestElements;
    }

    private void distinctId(LinkedList<MsTestElement> msTestElements) {
        Set<String> allIds = new HashSet<>();
        msTestElements.forEach(m -> {
            if (allIds.contains(m.getId())) {
                String newId = UUID.randomUUID().toString();
                m.setId(newId);
                allIds.add(newId);
            } else {
                allIds.add(m.getId());
            }
        });
    }

    //数据提取断言
    List<String> combinationProxyType = new ArrayList<>() {{
        add("validation");
        add("dataExtraction");
    }};

    //前后置、等待脚本、截图
    List<String> specificCommands = new ArrayList<>() {{
        add("pause");
        add("screenshot");
        add("cmdScript");
    }};

    //每种指令及其结束标记
    Map<String, List<String>> expectedProgramCommandPairs = new HashMap<String, List<String>>() {{
        put("cmdTimes", new ArrayList<>() {{
            add("end");
        }});
        put("cmdForEach", new ArrayList<>() {{
            add("end");
        }});
        put("cmdWhile", new ArrayList<>() {{
            add("end");
            add("repeatIf");
        }});
        put("cmdIf", new ArrayList<>() {{
            add("end");
            add("cmdElseIf");
            add("cmdElse");
        }});
        put("cmdElseIf", new ArrayList<>() {{
            add("cmdElseIf");
            add("cmdElse");
            add("end");
        }});
        put("cmdElse", new ArrayList<>() {{
            add("end");
        }});
    }};

    List<String> startCommands = new ArrayList<>() {{
        add("cmdTimes");
        add("cmdForEach");
        add("cmdWhile");
        add("cmdIf");
        add("cmdElseIf");
        add("cmdElse");
    }};

    List<String> notAllowPrePostCommands = new ArrayList<>() {{
        addAll(combinationProxyType);
        addAll(specificCommands);
        addAll(expectedProgramCommandPairs.keySet());
        add("cmdValidateText");
        add("cmdValidateTitle");
        add("cmdValidateElement");
        add("cmdValidateDropdown");
        add("cmdValidateValue");
        add("cmdExtractWindow");
        add("cmdExtractElement");
    }};

    private LinkedList<MsTestElement> processElement(LinkedList<MsTestElement> msTestElements, AtomicInteger count) {
        LinkedList<MsTestElement> finalMsTestElement = new LinkedList<>();
        //目前处理的元素的 index
        MsUiCommand currentCommand = null;
        Stack<MsUiCommand> startCommandStack = new Stack<>();
        //记录前置指令用于存放后置脚本等
        MsUiCommand preCommand = null;
        List<MsUiCommand> firstPreCommandList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(msTestElements)) {
            for (int i = 0; i < msTestElements.size(); i++) {

                count.addAndGet(1);
                currentCommand = (MsUiCommand) msTestElements.get(i);
                String commandName = currentCommand.getCommand();
                String viewType = currentCommand.getViewType();
                if ("end".equalsIgnoreCase(commandName)) {
                    MsUiCommand startCommand = startCommandStack.pop();
                    if (startCommands.contains(startCommand.getCommand())) {
                        continue;
                    } else {
                        MSException.throwException("校验失败！");
                    }
                }

                if ("repeatIf".equalsIgnoreCase(commandName)) {
                    MsUiCommand doCommand = startCommandStack.pop();
                    if (!"cmdWhile".equalsIgnoreCase(doCommand.getCommand())) {
                        MSException.throwException("校验失败！");
                    }
                    ((UiWhileVo) doCommand.getVo()).setExpression(currentCommand.getTarget());
                    continue;
                }

                if (currentCommand.getVo() != null && currentCommand.getVo() instanceof UiForEachVo) {
                    String arrayVariable = ((UiForEachVo) currentCommand.getVo()).getArrayVariable();
                    if(StringUtil.isChinese(arrayVariable)){
                        ((UiForEachVo) currentCommand.getVo()).setArrayVariable(((UiForEachVo) currentCommand.getVo()).getArrayVariable());
                    } else {
                        ((UiForEachVo) currentCommand.getVo()).setArrayVariable("${" + ((UiForEachVo) currentCommand.getVo()).getArrayVariable() + "}");
                    }
                }

                List<String> expectedPairCommand = expectedProgramCommandPairs.get(commandName);
                if (expectedPairCommand != null) {
                    if (!startCommandStack.isEmpty()) {
                        //上一个指令没有结束 放上一个指令的 hashTree
                        if (!expectedProgramCommandPairs.get(startCommandStack.peek().getCommand()).contains(commandName)) {
                            if (startCommandStack.peek().getHashTree() == null) {
                                startCommandStack.peek().setHashTree(new LinkedList<>());
                            }
                            startCommandStack.peek().getHashTree().add(currentCommand);
                            if (CollectionUtils.isNotEmpty(expectedPairCommand)) {
                                startCommandStack.push(currentCommand);
                            }
                            continue;
                        } else {
                            //上一个指令已经结束
                            startCommandStack.pop();
                            if (CollectionUtils.isNotEmpty(expectedPairCommand)) {
                                if (!startCommandStack.isEmpty()) {
                                    if (startCommandStack.peek().getHashTree() == null) {
                                        startCommandStack.peek().setHashTree(new LinkedList<>());
                                    }
                                    startCommandStack.peek().getHashTree().add(currentCommand);
                                    startCommandStack.push(currentCommand);
                                    continue;
                                } else {
                                    finalMsTestElement.add(currentCommand);
                                    startCommandStack.push(currentCommand);
                                    continue;
                                }
                            } else {
                                finalMsTestElement.add(currentCommand);
                            }
                        }
                    }
                    startCommandStack.push(currentCommand);
                    finalMsTestElement.add(currentCommand);
                    continue;
                }

                //
                if (combinationProxyType.contains(viewType)) {
                    //如果第一个指令是数据提取或者断言则不做任何处理
                    if (preCommand == null) {
                        //store storeJson 已经作为环境变量处理了，这里处理前置所有数据提取
                        if ("dataExtraction".equalsIgnoreCase(viewType) && !StringUtils.equalsAnyIgnoreCase(currentCommand.getVo().getCommand(null), "storeJson", "store")) {
                            if (CollectionUtils.isEmpty(firstPreCommandList)) {
                                MsUiCommand cmdExtraction = createVirtualMsUiCommand("cmdExtraction");
                                cmdExtraction.getHashTree().add(currentCommand);
                                firstPreCommandList.add(cmdExtraction);
                            } else {
                                //合并同类项
                                MsUiCommand lastFirstPreCommand = firstPreCommandList.get(firstPreCommandList.size() - 1);
                                if (StringUtils.equalsAnyIgnoreCase(lastFirstPreCommand.getViewType(), "cmdExtraction")) {
                                    lastFirstPreCommand.getHashTree().add(currentCommand);
                                }
                            }
                        }
                        continue;
                    } else {
                        //数据提取和断言放到前一个指令的后置
                        MsUiCommand virtualCommand = new MsUiCommand();
                        List<MsUiCommand> postCommands = preCommand.getPostCommands();
                        boolean exist = false;
                        if (CollectionUtils.isEmpty(postCommands)) {
                            postCommands = new ArrayList<>();
                        } else {
                            virtualCommand = postCommands.get(postCommands.size() - 1);
                            exist = true;
                        }

                        //如果是断言数据提取，则进行合并处理
                        if (StringUtils.equalsIgnoreCase(virtualCommand.getViewType(), currentCommand.getViewType())) {
                            virtualCommand.getHashTree().add(currentCommand);
                        } else {
                            virtualCommand = createVirtualMsUiCommand(currentCommand.getViewType());
                            virtualCommand.setEnable(currentCommand.isEnable());
                            virtualCommand.getHashTree().add(currentCommand);
                            exist = false;
                        }

                        if (!exist) {
                            postCommands.add(virtualCommand);
                            preCommand.setPostCommands(postCommands);
                        }
                    }
                } else if (specificCommands.contains(commandName)) {
                    //特殊处理前后置脚本和等待时间 将其作为第一个指令的前置
                    if (preCommand == null) {
                        firstPreCommandList.add(currentCommand);
                        continue;
                    }
                    if (CollectionUtils.isEmpty(preCommand.getPostCommands())) {
                        preCommand.setPostCommands(new LinkedList<>());
                    }
                    preCommand.getPostCommands().add(currentCommand);
                } else {
                    if (!startCommandStack.isEmpty()) {
                        if (startCommandStack.peek().getHashTree() == null) {
                            startCommandStack.peek().setHashTree(new LinkedList<>());
                        }
                        startCommandStack.peek().getHashTree().add(currentCommand);
                    } else {
                        //设置第一个指令的前置脚本
                        if (CollectionUtils.isNotEmpty(firstPreCommandList)) {
                            List<MsUiCommand> preCommandList = new LinkedList<>();
                            preCommandList.addAll(firstPreCommandList);
                            currentCommand.setPreCommands(preCommandList);
                            firstPreCommandList = null;
                        }
                        finalMsTestElement.add(currentCommand);
                    }
                }
                if (!notAllowPrePostCommands.contains(currentCommand.getCommand())) {
                    preCommand = currentCommand;
                }
            }

        }

        return finalMsTestElement;
    }

    /**
     * 根据 viewType 创建断言或者数据提取指令
     *
     * @param viewType
     * @return
     */
    private MsUiCommand createVirtualMsUiCommand(String viewType) {
        MsUiCommand msUiCommand = new MsUiCommand();
        msUiCommand.setId(UUID.randomUUID().toString());
        msUiCommand.setViewType(viewType);
        msUiCommand.setResourceId(UUID.randomUUID().toString());
        if ("validation".equalsIgnoreCase(viewType)) {
            msUiCommand.setName("cmdValidation");
            msUiCommand.setCommand("cmdValidation");
            msUiCommand.setCommandType(CommandType.COMMAND_TYPE_COMBINATION_PROXY);
        } else {
            msUiCommand.setName("cmdExtraction");
            msUiCommand.setCommand("cmdExtraction");
            msUiCommand.setCommandType(CommandType.COMMAND_TYPE_COMBINATION_PROXY);
        }
        msUiCommand.setHashTree(new LinkedList<>());
        return msUiCommand;
    }

    public static String getSelectModulePath(String path, String pid) {
        UiScenarioModuleService uiScenarioModuleService = CommonBeanFactory.getBean(UiScenarioModuleService.class);
        if (StringUtils.isNotBlank(pid)) {
            ModuleNodeDTO moduleDTO = uiScenarioModuleService.getNode(pid);
            if (moduleDTO != null) {
                return getSelectModulePath(moduleDTO.getName() + "/" + path, moduleDTO.getParentId());
            }
        }
        return "/" + path;
    }
}
