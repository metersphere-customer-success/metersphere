package io.metersphere.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.domain.UiScenarioReportResult;
import io.metersphere.base.mapper.UiScenarioMapper;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.base.mapper.UiScenarioReportResultMapper;
import io.metersphere.base.mapper.UiScenarioReportStructureMapper;
import io.metersphere.base.mapper.ext.ExtUiScenarioReportResultMapper;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.*;
import io.metersphere.dto.api.UiScenarioReportBaseInfoDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.UiGenerateHashTreeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioReportStructureService {
    private static final List<String> REQUESTS = Arrays.asList("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "JSR223Processor", "AbstractSampler");
    private static final List<String> CONTROLS = Arrays.asList("Assertions", "IfController", "ConstantTimer");
    private static final String RESOURCE_ID = "resourceId";
    private static final String REFERENCED = "referenced";
    private static final String ERROR_CODE = "errorCode";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SCENARIO = "scenario";
    private static final String TYPE = "type";
    private static final String HASH_TREE = "hashTree";
    private static final String ENABLE = "enable";
    private static final String UI_COMMAND = "MsUiCommand";
    private static final String ERROR_REPORT = "errorReportResult";
    private static final String ERROR = "Error";
    public static final String PRE_PROCESS_SCRIPT = "PRE_PROCESSOR_ENV_";
    public static final String POST_PROCESS_SCRIPT = "POST_PROCESSOR_ENV_";
    @Resource
    private UiScenarioReportStructureMapper mapper;
    @Resource
    private UiScenarioReportResultMapper reportResultMapper;
    @Resource
    private UiScenarioReportMapper scenarioReportMapper;
    @Resource
    private ExtUiScenarioReportResultMapper extUiScenarioReportResultMapper;
    @Resource
    private UiScenarioReportMapper uiScenarioReportMapper;
    @Autowired
    private UiScenarioEnvService uiScenarioEnvService;

    public void saveUiScenarios(List<UiScenarioWithBLOBs> uiScenarios, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (UiScenarioWithBLOBs bos : uiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("Ui Scenario run-执行脚本装载-生成场景报告结构：" + JSON.toJSONString(dtoList));
        }
        this.save(reportId, dtoList);
    }

    public void save(List<UiScenarioWithBLOBs> apiScenarios, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (UiScenarioWithBLOBs bos : apiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        this.save(reportId, dtoList);
    }

    public List<StepTreeDTO> get(List<UiScenarioWithBLOBs> apiScenarios, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (UiScenarioWithBLOBs bos : apiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        return dtoList;
    }

    public void saveUi(List<UiScenarioWithBLOBs> uiScenarios, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (UiScenarioWithBLOBs bos : uiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("UI Scenario run-执行脚本装载-生成场景报告结构：" + JSON.toJSONString(dtoList));
        }
        this.save(reportId, dtoList);
    }

    public void save(UiScenarioWithBLOBs uiScenario, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        StepTreeDTO dto = dataFormatting(uiScenario, reportType);
        dtoList.add(dto);
        this.save(reportId, dtoList);
    }

    public void save(String reportId, List<StepTreeDTO> dtoList) {
        UiScenarioReportStructureWithBLOBs structure = new UiScenarioReportStructureWithBLOBs();
        structure.setId(UUID.randomUUID().toString());
        structure.setCreateTime(System.currentTimeMillis());
        structure.setReportId(reportId);
        structure.setResourceTree(JSON.toJSONString(dtoList).getBytes(StandardCharsets.UTF_8));
        mapper.insert(structure);
    }

    public void update(String reportId, List<StepTreeDTO> dtoList) {
        UiScenarioReportStructureExample example = new UiScenarioReportStructureExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<UiScenarioReportStructureWithBLOBs> structures = mapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(structures)) {
            UiScenarioReportStructureWithBLOBs structure = structures.get(0);
            structure.setResourceTree(JSON.toJSONString(dtoList).getBytes(StandardCharsets.UTF_8));
            mapper.updateByPrimaryKeySelective(structure);
        }
    }

    public void update(String reportId, String console) {
        UiScenarioReportStructureExample example = new UiScenarioReportStructureExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<UiScenarioReportStructureWithBLOBs> structures = mapper.selectByExampleWithBLOBs(example);
        for (UiScenarioReportStructureWithBLOBs structure : structures) {
            structure.setConsole(console);
            mapper.updateByPrimaryKeyWithBLOBs(structure);
        }
    }

    public static StepTreeDTO dataFormatting(UiScenarioWithBLOBs uiScenario, String reportType) {
        return dataFormatting(null, uiScenario.getName(), uiScenario.getScenarioDefinition(), reportType);
    }

    private static String combinationResourceId(JSONObject element, String reportType, String id) {
        element = getRefElement(element);
        String resourceId = StringUtils.isNotEmpty(element.optString(ID))
                ? element.optString(ID) : element.optString(RESOURCE_ID);
        if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())) {
            if (StringUtils.equals(element.optString(TYPE), SCENARIO)) {
                resourceId = id;
            } else if (StringUtils.isNotEmpty(resourceId) && StringUtils.isNotEmpty(id) && !resourceId.contains(id)) {
                resourceId = id + "=" + resourceId;
            }
        }
        return resourceId;
    }

    public static StepTreeDTO dataFormatting(String id, String name, String scenarioDefinition, String reportType) {
        JSONObject element = new JSONObject(scenarioDefinition);
        if (element != null && element.optBoolean(ENABLE)) {
            String resourceId = combinationResourceId(element, reportType, id);
            StepTreeDTO dto = new StepTreeDTO(name, resourceId, element.optString(TYPE), resourceId, 1);
            dto.setAllIndex(null);
            if (element.has(HASH_TREE) && !REQUESTS.contains(dto.getType())) {
                JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                dataFormatting(elementJSONArray, dto, id, reportType);
            }
            return dto;
        }
        return null;
    }

    private static JSONObject getRefElement(JSONObject element) {
        String referenced = element.optString(REFERENCED);
        if (StringUtils.equals(referenced, MsTestElementConstants.REF.name())) {
            if (StringUtils.equals(element.optString(TYPE), SCENARIO)) {
                UiScenarioWithBLOBs scenarioWithBLOBs = CommonBeanFactory.getBean(UiScenarioMapper.class).selectByPrimaryKey(element.optString(ID));
                if (scenarioWithBLOBs != null) {
                    return new JSONObject(scenarioWithBLOBs.getScenarioDefinition());
                }
            }
        }
        return element;
    }

    public static void dataFormatting(JSONArray hashTree, StepTreeDTO dto, String id, String reportType) {
        if (hashTree == null) return;
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && element.optBoolean(ENABLE)) {
                String resourceId = combinationResourceId(element, reportType, id);
                StepTreeDTO children = new StepTreeDTO(element.optString(NAME), resourceId, element.optString(TYPE), resourceId, element.optInt("index"));
                if (StringUtils.isNotBlank(children.getType()) && children.getType().equals(UI_COMMAND)) {
                    children.setResourceId(resourceId);
                    children.setLabel(element.optString(NAME));
                    children.setCmdType(element.optString("commandType"));
                } else if (StringUtils.isNotEmpty(dto.getAllIndex())) {
                    children.setAllIndex(dto.getAllIndex() + "_" + (children.getIndex() == 0 ? (i + 1) : children.getIndex()));
                    children.setResourceId(resourceId + "_" + children.getAllIndex());
                } else {
                    children.setAllIndex("" + (children.getIndex() == 0 ? (i + 1) : children.getIndex()));
                    children.setResourceId(resourceId + "_" + children.getAllIndex());
                }
                dto.getChildren().add(children);
                if (element.has(HASH_TREE) && !REQUESTS.contains(children.getType())) {
                    JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                    dataFormatting(elementJSONArray, children, id, reportType);
                }
            }
        }
    }

    private void calculateScenarios(List<StepTreeDTO> dtoList,
                                    AtomicLong totalScenario, AtomicLong scenarioError, AtomicLong errorReport, AtomicLong unExecute) {
        for (StepTreeDTO step : dtoList) {
            totalScenario.set(totalScenario.longValue() + 1);
            if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), ReportStatus.ERROR.name())) {
                scenarioError.set(scenarioError.longValue() + 1);
            } else if (!StringUtils.equalsIgnoreCase(step.getTotalStatus(), ReportStatus.SUCCESS.name())) {
                unExecute.set(unExecute.longValue() + 1);
            }
        }
    }

    private void calculate(List<StepTreeDTO> dtoList, AtomicLong totalTime) {
        for (StepTreeDTO step : dtoList) {
            if (!StringUtils.equalsAny(step.getType(), SCENARIO, "UiScenario") && step.getValue() != null) {
                if (step.getValue().getStartTime() == 0 || step.getValue().getEndTime() == 0) {
                    totalTime.set(totalTime.longValue() + 0);
                } else if (step.getValue().getStartTime() > step.getValue().getEndTime() && step.getValue().getResponseResult() != null) {
                    // 异常时间处理
                    totalTime.set(totalTime.longValue() + step.getValue().getResponseResult().getResponseTime());
                } else {
                    totalTime.set((totalTime.longValue() + (step.getValue().getEndTime() - step.getValue().getStartTime())));
                }
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                calculate(step.getChildren(), totalTime);
            }
        }
    }

    private void calculateStep(List<StepTreeDTO> dtoList, AtomicLong stepTotal, AtomicLong stepError, AtomicLong stepErrorCode, AtomicLong stepUnExecute) {
        for (StepTreeDTO root : dtoList) {
            int unExecSize = 0;
            if (CollectionUtils.isNotEmpty(root.getChildren())) {
                stepTotal.set((stepTotal.longValue() + root.getChildren().size()));
                for (StepTreeDTO step : root.getChildren()) {
                    if (StringUtils.equalsAnyIgnoreCase(step.getTotalStatus(), ReportStatus.ERROR.name())) {
                        stepError.set(stepError.longValue() + 1);
                    } else if (!StringUtils.equalsIgnoreCase(step.getTotalStatus(), ReportStatus.SUCCESS.name())) {
                        stepUnExecute.set(stepUnExecute.longValue() + 1);
                        unExecSize++;
                    }
                }
            }
            root.setUnExecuteTotal(unExecSize);
        }
    }

    public void reportFormatting(List<StepTreeDTO> dtoList, Map<String, List<UiScenarioReportResultWithBLOBs>> maps, String reportType) {
        // 按照创建时间排序
        for (int index = 0; index < dtoList.size(); index++) {
            StepTreeDTO dto = dtoList.get(index);
            dto.setIndex((index + 1));
            List<UiScenarioReportResultWithBLOBs> reportResults = maps.get(dto.getResourceId());
            if (CollectionUtils.isNotEmpty(reportResults)) {
                for (int i = 0; i < reportResults.size(); i++) {
                    UiScenarioReportResultWithBLOBs reportResult = reportResults.get(i);
                    StepTreeDTO step = i == 0 ? dto :
                            new StepTreeDTO(dto.getLabel(), UUID.randomUUID().toString(), dto.getType(), reportResults.get(i).getId(), (i + 1));
                    step.setStepId(reportResults.get(i).getId());
                    RequestResult result = new RequestResultExpandDTO(reportResult);
                    if (reportResult.getContent() != null) {
                        result = getUIRequestResult(reportResults, i, result);
                    }
                    step.setValue(result);
                    step.setTotalStatus(reportResult.getStatus());
                    step.setErrorCode(reportResults.get(i).getErrorCode());
                    if (i > 0) {
                        //循环的结果
                        dtoList.add(getLoopIndex(dtoList, step), step);
                    }
                }
            }
            // 未执行请求
            if (StringUtils.isNotEmpty(dto.getType()) && REQUESTS.contains(dto.getType()) && dto.getValue() == null || isUiUnExecuteCommand(dto)) {
                dto.setTotalStatus(ReportStatus.PENDING.toString());
                dto.setValue(new RequestResultExpandDTO(dto.getLabel(), ReportStatus.PENDING.toString()));
            } else if (StringUtils.isNotEmpty(dto.getType()) && CONTROLS.contains(dto.getType()) && dto.getValue() == null) {
                // 条件控制步骤
                dto.setTotalStatus(ReportStatus.SUCCESS.name());
                dto.setValue(new RequestResultExpandDTO(dto.getLabel(), ReportStatus.SUCCESS.name()));
            } else if (dto.getValue() instanceof RequestResultExpandDTO && StringUtils.isNotEmpty(((RequestResultExpandDTO) dto.getValue()).getStatus())) {
                dto.setTotalStatus(((RequestResultExpandDTO) dto.getValue()).getStatus());
            } else if (dto.getValue() != null && StringUtils.isEmpty(dto.getTotalStatus())) {
                if (dto.getValue().getError() > 0 || BooleanUtils.isNotTrue(dto.getValue().isSuccess())) {
                    dto.setTotalStatus(ReportStatus.ERROR.name());
                } else {
                    dto.setTotalStatus(ReportStatus.SUCCESS.name());
                }
            }
            if (StringUtils.isNotEmpty(dto.getErrorCode()) && StringUtils.isEmpty(dto.getTotalStatus())) {
                dto.setTotalStatus(ERROR_CODE);
            }

            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                reportFormatting(dto.getChildren(), maps, reportType);

                if (StringUtils.isEmpty(dto.getErrorCode())) {
                    //统计child的errorCode，合并到parent中
                    List<String> childErrorCodeList = new ArrayList<>();
                    for (StepTreeDTO child : dto.getChildren()) {
                        if (StringUtils.isNotEmpty(child.getErrorCode()) && !childErrorCodeList.contains(child.getErrorCode())) {
                            childErrorCodeList.add(child.getErrorCode());
                        }
                    }
                    if (CollectionUtils.isNotEmpty(childErrorCodeList)) {
                        dto.setErrorCode(StringUtils.join(childErrorCodeList, ","));
                    }
                }

                int failCount = 0;
                int errorReportCount = 0;
                int successCount = 0;
                for (StepTreeDTO child : dto.getChildren()) {
                    if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), ReportStatus.ERROR.name())) {
                        failCount++;
                    } else if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), ReportStatus.SUCCESS.name())) {
                        successCount++;
                    } else if (StringUtils.equalsAnyIgnoreCase(child.getTotalStatus(), ERROR_CODE, ERROR_REPORT)) {
                        errorReportCount++;
                    }
                }

                //当有多个子步骤结果时，如果当前步骤不是场景，则：失败>误报>未执行>成功>未执行； 如果是场景：误报>失败>成功>未执行
                if (failCount == 0 && errorReportCount == 0 && successCount == 0) {
                    dto.setTotalStatus(ReportStatus.PENDING.toString());
                } else if (successCount == dto.getChildren().size() || (successCount > 0 && errorReportCount == 0 && failCount == 0)) {
                    dto.setTotalStatus(ReportStatus.SUCCESS.name());
                } else {
                    if (StringUtils.equalsIgnoreCase(dto.getType(), SCENARIO)) {
                        if (failCount > 0) {
                            dto.setTotalStatus(ReportStatus.ERROR.name());
                        } else if (errorReportCount > 0) {
                            dto.setTotalStatus(ERROR_CODE);
                        } else {
                            dto.setTotalStatus(ReportStatus.SUCCESS.name());
                        }
                    } else {
                        if (failCount > 0) {
                            dto.setTotalStatus(ReportStatus.ERROR.name());
                        } else if (errorReportCount > 0) {
                            dto.setTotalStatus(ERROR_CODE);
                        } else {
                            dto.setTotalStatus(ReportStatus.SUCCESS.name());
                        }
                    }
                }
            }
            if (StringUtils.isEmpty(dto.getTotalStatus())) {
                dto.setTotalStatus(ReportStatus.PENDING.toString());
            } else if (StringUtils.equalsAnyIgnoreCase(dto.getTotalStatus(), ERROR)) {
                dto.setTotalStatus(ReportStatus.ERROR.name());
            }
        }
        if (!reportType.startsWith("UI")) {
            this.orderLoops(dtoList);
        }
    }

    private int getLoopIndex(List<StepTreeDTO> dtoList, StepTreeDTO step) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return 0;
        }
        for (int i = dtoList.size() - 1; i >= 0; i--) {
            if (dtoList.get(i).getValue() != null) {
                if (StringUtils.equalsIgnoreCase(dtoList.get(i).getValue().getId(), step.getValue().getId())) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * ui 的报告内容不进行懒加载，统一从 content 中取，由于 v2.1 中合并图片存在 baseInfo
     * 这里做一个兼容处理
     *
     * @param reportResults
     * @param i
     * @param result
     * @return
     */
    private RequestResult getUIRequestResult(List<UiScenarioReportResultWithBLOBs> reportResults, int i, RequestResult result) {
        String combinationImg = ((RequestResultExpandDTO) result).getCombinationImg();
        result = JSON.parseObject(new String(reportResults.get(i).getContent(), StandardCharsets.UTF_8), UiCommandResult.class);
        ((UiCommandResult) result).calTime();
        if (StringUtils.isNotBlank(combinationImg)) {
            ((UiCommandResult) result).setCombinationImg(combinationImg);
        }
        return result;
    }

    /**
     * 循环步骤请求从新排序
     */
    private void orderLoops(List<StepTreeDTO> dtoList) {
        try {
            List<StepTreeDTO> steps = dtoList.stream().filter(e -> e.getValue() == null || e.getValue().getStartTime() == 0)
                    .collect(Collectors.toList());
            // 都是没有结果的步骤，不需要再次排序
            if (dtoList.size() == steps.size()) {
                return;
            }
            // 非正常执行结束的请求结果
            List<StepTreeDTO> unList = dtoList.stream().filter(e -> e.getValue() != null
                    && ((StringUtils.equalsIgnoreCase(e.getType(), "DubboSampler") && e.getValue().getStartTime() == 0)
                    || StringUtils.equalsIgnoreCase(e.getTotalStatus(), ReportStatus.PENDING.toString())))
                    .collect(Collectors.toList());

            // 有效数据按照时间排序
            List<StepTreeDTO> list = dtoList.stream().filter(e -> e.getValue() != null && e.getValue().getStartTime() != 0).collect(Collectors.toList());
            list = list.stream().sorted(Comparator.comparing(x -> x.getValue().getStartTime())).collect(Collectors.toList());
            unList = unList.stream().sorted(Comparator.comparing(x -> x.getIndex())).collect(Collectors.toList());
            unList.addAll(steps);
            List<StepTreeDTO> mergeList = unList.stream().distinct().collect(Collectors.toList());
            // 处理请求结果开始时间为0的数据
            for (StepTreeDTO unListDTO : mergeList) {
                int index = unListDTO.getIndex();
                if (index > 0) {
                    list.add(index - 1, unListDTO);
                }
            }
            for (int index = 0; index < list.size(); index++) {
                list.get(index).setIndex((index + 1));
            }
            dtoList.clear();
            dtoList.addAll(list);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private boolean isUiUnExecuteCommand(StepTreeDTO dto) {
        if (dto.getType().equals(UI_COMMAND) && dto.getValue() == null
                && (StringUtils.isBlank(dto.getCmdType()) || !dto.getCmdType().equalsIgnoreCase(CommandType.COMMAND_TYPE_COMBINATION))) {
            return true;
        }
        return false;
    }

    public UiScenarioReportDTO assembleReport(String reportId, boolean selectReportContent) {
        UiScenarioReportWithBLOBs report = scenarioReportMapper.selectByPrimaryKey(reportId);
        UiScenarioReportDTO dto = this.getReport(reportId, selectReportContent);
        dto.setActuator(report.getActuator());
        dto.setName(report.getName());
        dto.setEnvConfig(report.getEnvConfig());
        this.initProjectEnvironmentByEnvConfig(dto, report.getEnvConfig());
        return dto;
    }

    public void initProjectEnvironmentByEnvConfig(UiScenarioReportDTO dto, String envConfig) {
        if (StringUtils.isNotEmpty(envConfig)) {
            LinkedHashMap<String, List<String>> projectEnvMap = uiScenarioEnvService.getProjectEnvMapByEnvConfig(envConfig);
            if (MapUtils.isNotEmpty(projectEnvMap)) {
                dto.setProjectEnvMap(projectEnvMap);
            }
        }
    }

    private UiScenarioReportDTO getReport(String reportId, boolean selectContent) {
        UiScenarioReport mainReport = uiScenarioReportMapper.selectByPrimaryKey(reportId);
        List<UiScenarioReportResultWithBLOBs> reportResults = null;
        if (selectContent || mainReport.getReportType().startsWith(SystemConstants.TestTypeEnum.UI.name())) {
            // UI 报告不执行懒加载
            UiScenarioReportResultExample example = new UiScenarioReportResultExample();
            example.createCriteria().andReportIdEqualTo(reportId);
            reportResults = reportResultMapper.selectByExampleWithBLOBs(example);
        } else {
            reportResults = this.selectBaseInfoResultByReportId(reportId);
            //判断base_info是否为空，为空则是旧数据
            boolean isBaseInfoNull = false;
            for (UiScenarioReportResultWithBLOBs result : reportResults) {
                if (result.getBaseInfo() == null) {
                    isBaseInfoNull = true;
                    break;
                }
            }
            if (isBaseInfoNull) {
                UiScenarioReportResultExample example = new UiScenarioReportResultExample();
                example.createCriteria().andReportIdEqualTo(reportId);
                reportResults = reportResultMapper.selectByExampleWithBLOBs(example);
            }
        }

        removeUiResultIfNotStep(reportResults, reportId);

        UiScenarioReportStructureExample structureExample = new UiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(reportId);
        List<UiScenarioReportStructureWithBLOBs> reportStructureWithBLOBs = mapper.selectByExampleWithBLOBs(structureExample);

        UiScenarioReportDTO reportDTO = new UiScenarioReportDTO();
        // 写入控制台信息
        if (CollectionUtils.isNotEmpty(reportStructureWithBLOBs)) {
            reportDTO.setConsole(reportStructureWithBLOBs.get(0).getConsole());
        }
        // 组装报告
        if (CollectionUtils.isNotEmpty(reportStructureWithBLOBs) && CollectionUtils.isNotEmpty(reportResults)) {
            UiScenarioReportStructureWithBLOBs scenarioReportStructure = reportStructureWithBLOBs.get(0);
            String stepTreeStr = UiGenerateHashTreeUtil.convertStatus(new String(scenarioReportStructure.getResourceTree(), StandardCharsets.UTF_8));
            List<StepTreeDTO> stepList = JSON.parseArray(stepTreeStr, StepTreeDTO.class);
            //判断是否含有全局前后置脚本，如果有的话需要将脚本内容添加到stepDTO中
            reportResults = this.filterProcessResult(reportResults);

            reportDTO.setTotal(reportResults.size());
            reportDTO.setError(reportResults.stream().filter(e -> StringUtils.equals(e.getStatus(), ReportStatus.ERROR.name())).collect(Collectors.toList()).size());
            reportDTO.setPassAssertions(reportResults.stream().mapToLong(UiScenarioReportResult::getPassAssertions).sum());
            reportDTO.setTotalAssertions(reportResults.stream().mapToLong(UiScenarioReportResult::getTotalAssertions).sum());


            // 匹配结果
            Map<String, List<UiScenarioReportResultWithBLOBs>> maps = reportResults.stream().collect(Collectors.groupingBy(UiScenarioReportResult::getResourceId));
            this.reportFormatting(stepList, maps, mainReport.getReportType());

            reportDTO = this.countReportNum(stepList, reportDTO);
            // 统计场景数据
            AtomicLong stepError = new AtomicLong();
            AtomicLong stepTotal = new AtomicLong();

            reportDTO.setScenarioSuccess((reportDTO.getScenarioTotal() - reportDTO.getScenarioError() - reportDTO.getScenarioUnExecute() - reportDTO.getScenarioErrorReport()));

            //统计步骤数据
            AtomicLong stepErrorCode = new AtomicLong();
            AtomicLong stepUnExecute = new AtomicLong();
            calculateStep(stepList, stepTotal, stepError, stepErrorCode, stepUnExecute);

            reportDTO.setScenarioStepSuccess((stepTotal.longValue() - stepError.longValue() - stepErrorCode.longValue() - stepUnExecute.longValue()));
            reportDTO.setScenarioStepTotal(stepTotal.longValue());
            reportDTO.setScenarioStepError(stepError.longValue());
            reportDTO.setScenarioStepErrorReport(stepErrorCode.longValue());
            reportDTO.setScenarioStepUnExecuteReport(stepUnExecute.longValue());
            reportDTO.setConsole(scenarioReportStructure.getConsole());
            reportDTO.setSteps(stepList);

            //统计未执行的请求数量
            AtomicLong allUnExecute = new AtomicLong();
            this.countAllUnexpected(stepList, allUnExecute);
            reportDTO.setUnExecute(allUnExecute.longValue());
            //之前的total中请求数是按照获得报告的响应数来算的。这里要加上未执行的数量
            reportDTO.setTotal(reportDTO.getTotal() + allUnExecute.longValue());
        }
        return reportDTO;
    }

    private List<UiScenarioReportResultWithBLOBs> filterProcessResult(List<UiScenarioReportResultWithBLOBs> reportResults) {
        List<UiScenarioReportResultWithBLOBs> withOutProcessList = new ArrayList<>();
        for (UiScenarioReportResultWithBLOBs item : reportResults) {
            if (item.getBaseInfo() != null) {
                UiScenarioReportBaseInfoDTO dto = JSON.parseObject(item.getBaseInfo(), UiScenarioReportBaseInfoDTO.class);
                if (!StringUtils.startsWithAny(dto.getReqName(), PRE_PROCESS_SCRIPT, POST_PROCESS_SCRIPT)) {
                    withOutProcessList.add(item);
                }
            } else {
                withOutProcessList.add(item);
            }
        }
        return withOutProcessList;
    }

    private List<UiScenarioReportResultWithBLOBs> selectBaseInfoResultByReportId(String reportId) {
        return extUiScenarioReportResultMapper.selectBaseInfoResultByReportId(reportId);
    }

    /**
     * UI 测试结果统计去掉前后置或其他不算步骤的执行结果
     *
     * @param reportResults
     */
    private void removeUiResultIfNotStep(List<UiScenarioReportResultWithBLOBs> reportResults, String reportId) {
        UiScenarioReport report = scenarioReportMapper.selectByPrimaryKey(reportId);
        if (report.getReportType() != null && report.getReportType().startsWith("UI")) {
            if (CollectionUtils.isNotEmpty(reportResults)) {
                Iterator<UiScenarioReportResultWithBLOBs> iterator = reportResults.iterator();
                while (iterator.hasNext()) {
                    UiScenarioReportResultWithBLOBs item = iterator.next();
                    String baseInfo = item.getBaseInfo();
                    if (StringUtils.isNotBlank(baseInfo)) {
                        Boolean isNoStep = new JSONObject(baseInfo).optBoolean("isNotStep");
                        if (BooleanUtils.isTrue(isNoStep)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void countAllUnexpected(List<StepTreeDTO> stepList, AtomicLong allUnExecute) {
        for (StepTreeDTO step : stepList) {
            if (step.getValue() != null) {
                if (step.getValue() instanceof RequestResultExpandDTO
                        && StringUtils.equalsIgnoreCase(((RequestResultExpandDTO) step.getValue()).getStatus(), ReportStatus.PENDING.toString())) {
                    allUnExecute.set(allUnExecute.longValue() + 1);
                }
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                this.countAllUnexpected(step.getChildren(), allUnExecute);
            }
        }
    }

    private UiScenarioReportDTO countReportNum(List<StepTreeDTO> stepList, UiScenarioReportDTO reportDTO) {
        if (reportDTO != null && CollectionUtils.isNotEmpty(stepList)) {
            // 统计场景数据
            AtomicLong totalScenario = new AtomicLong();
            AtomicLong scenarioError = new AtomicLong();
            AtomicLong totalTime = new AtomicLong();
            AtomicLong errorReport = new AtomicLong();
            AtomicLong unExecute = new AtomicLong();
            calculateScenarios(stepList, totalScenario, scenarioError, errorReport, unExecute);
            calculate(stepList, totalTime);
            reportDTO.setTotalTime(totalTime.longValue());
            reportDTO.setScenarioTotal(totalScenario.longValue());
            reportDTO.setScenarioError(scenarioError.longValue());
            reportDTO.setScenarioErrorReport(errorReport.longValue());
            reportDTO.setScenarioUnExecute(unExecute.longValue());
        }
        return reportDTO;
    }

    public RequestResult selectReportContent(String stepId) {
        RequestResult result = new RequestResult();
        try {
            result = selectReportContent(stepId, RequestResult.class);
        } catch (Exception ignore) {
        }
        return result;
    }

    public <T> T selectReportContent(String stepId, Class<T> clazz) {
        UiScenarioReportResultWithBLOBs apiScenarioReportResult = reportResultMapper.selectByPrimaryKey(stepId);
        if (apiScenarioReportResult != null) {
            T requestResult = JSON.parseObject(new String(apiScenarioReportResult.getContent(), StandardCharsets.UTF_8), clazz);
            return requestResult;
        } else {
            return (T) clazz.getInterfaces();
        }
    }
}
