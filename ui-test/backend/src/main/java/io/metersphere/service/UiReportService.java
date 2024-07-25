package io.metersphere.service;


import com.google.common.collect.Lists;
import io.metersphere.base.domain.UiScenarioReport;
import io.metersphere.base.domain.UiScenarioReportResultWithBLOBs;
import io.metersphere.base.mapper.UiScenarioReportMapper;
import io.metersphere.base.mapper.UiScenarioReportResultMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.UiProcessType;
import io.metersphere.dto.*;
import io.metersphere.dto.api.UiScenarioReportBaseInfoDTO;
import io.metersphere.utils.ResultConversionUtil;
import io.metersphere.xmind.utils.FileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiReportService {
    @Resource
    UiScenarioReportStructureService uiScenarioReportStructureService;
    @Resource
    UiScenarioReportResultMapper uiScenarioReportResultMapper;
    @Lazy
    @Resource
    UiScenarioReportService uiScenarioReportService;
    @Resource
    UiScenarioReportMapper uiScenarioReportMapper;
    @Resource(name = "assertionCommands")
    Set<String> assertionCommands;
    @Resource(name = "extractCommands")
    Set<String> extractCommands;

    /**
     * ui执行完后，存储UI报告结果
     * 反射调用，勿删
     *
     * @param reportId
     */
    public void saveUiResult(String reportId, List<RequestResult> queue) {
        try {
            if (CollectionUtils.isNotEmpty(queue)) {
                List<UiCommandResult> preResults = new ArrayList<>();
                UiCommandResult currentMainResult = null;
                String header = queue.get(queue.size() - 1).getResponseResult().getHeaders();
                String retryHeader = retryHandle(queue);
                if (StringUtils.isNotEmpty(header) && StringUtils.isNotEmpty(retryHeader)) {
                    header = retryHeader;
                }
                LogUtil.info(String.format("UI 场景执行结束，报告 reportId: %s, 返回 headers: %s", reportId, header));
                try {
                    if (StringUtils.isNoneBlank(header)) {
                        List<UiCommandResult> response = JSON.parseArray(header, UiCommandResult.class);
                        for (UiCommandResult result : response) {
                            if (StringUtils.equals(result.getProcessType(), UiProcessType.MAIN.name())) {
                                currentMainResult = result;
                                preResults.forEach(pre -> addExtraction(result, pre));
                                preResults.clear();
                            } else if (StringUtils.equals(result.getProcessType(), UiProcessType.PRE.name())) {
                                preResults.add(result);
                            } else if (StringUtils.equals(result.getProcessType(), UiProcessType.POST.name())) {
                                addAssertion(currentMainResult, result);
                                addExtraction(currentMainResult, result);
                            }
                        }
                        // 合并时间 前置/后置处理时间需要算在报告里面
                        gergeDealTime(response);
                        response.forEach((result) -> {
                            // todo 批量优化
                            // 前后置的操作不入库
                            if (!StringUtils.equals(result.getProcessType(), UiProcessType.PRE.name()) &&
                                    !StringUtils.equals(result.getProcessType(), UiProcessType.POST.name())) {
                                //将收集的截图按照事件顺序 排序
                                if (CollectionUtils.isNotEmpty(result.getUiScreenshots())) {
                                    List<UiCommandResult.ScreenshotDTO> screenshotList = result.getUiScreenshots().parallelStream().sorted(Comparator.comparing(UiCommandResult.ScreenshotDTO::getTime)).collect(Collectors.toList());
                                    result.setUiScreenshots(screenshotList);
                                }
                                uiScenarioReportResultMapper.insert(newUiScenarioReportResult(reportId, result.getId(), result));
                            }

                        });
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private String retryHandle(List<RequestResult> queue) {
        if (queue.size() > 1) {
            List<RequestResult> webDriverSamplerList = queue.stream().filter(node -> node.getName().equals("WebDriverSampler")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(webDriverSamplerList)) {
                return null;
            }
            Boolean failFlag = Boolean.FALSE;
            for (int i = 0; i < webDriverSamplerList.size(); i++) {
                if (webDriverSamplerList.get(i).isSuccess()) {
                    failFlag = Boolean.TRUE;
                }
            }
            // 重试都失败的取第一条报告
            if (!failFlag && webDriverSamplerList.size() > 1) {
                return webDriverSamplerList.get(0).getResponseResult().getHeaders();
            }
        }
        return null;
    }

    public UiScenarioReportResultWithBLOBs newUiScenarioReportResult(String reportId, String resourceId, UiCommandResult result) {
        UiScenarioReportResultWithBLOBs report = ResultConversionUtil.newScenarioReportResult(reportId, resourceId);
        String status = result.isSuccess() ? ReportStatus.SUCCESS.name() : ReportStatus.ERROR.toString();
        report.setStatus(status);
        result.setReportId(reportId);
        UiScenarioReportBaseInfoDTO baseInfo = getUiBaseInfo(result);
        //异步生成长图
        baseInfo.setCombinationImg(generateCombinationImg(reportId, resourceId, result.getUiScreenshots()));
        result.setCombinationImg(baseInfo.getCombinationImg());
        report.setBaseInfo(JSON.toJSONString(baseInfo));
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        report.setPassAssertions((long) result.getPassAssertions());
        report.setTotalAssertions((long) result.getTotalAssertions());
        return report;
    }

    public String generateCombinationImg(String reportId, String resourceId, List<UiCommandResult.ScreenshotDTO> uiScreenshots) {
        if (CollectionUtils.isEmpty(uiScreenshots)) {
            return "";
        }
        //生成 长图存储路径
        String prefix = String.format("%s/%s/", FileUtils.UI_IMAGE_DIR, reportId);
        String path = String.format("combination——%s——%s——screenshot.png", resourceId, System.currentTimeMillis());
        String fullPath = prefix + path;
        //异步组装图片
        mergeImages(fullPath, reportId, uiScreenshots);
        return path;
    }

    /**
     * 合并 图片为长图
     *
     * @param path
     * @param uiScreenshots
     */
    private void mergeImages(String path, String reportId, List<UiCommandResult.ScreenshotDTO> uiScreenshots) {
        new ImageOptComponent().mergeImage(uiScreenshots.parallelStream().map(v -> {
            ImageOptComponent.ImageDTO image = new ImageOptComponent.ImageDTO();
            image.setPath(String.format("%s/%s/%s", FileUtils.UI_IMAGE_DIR, reportId, v.getPath()));
            image.setTitle(v.getName());
            return image;
        }).collect(Collectors.toList()), path);
    }

    //记录基础信息
    public UiScenarioReportBaseInfoDTO getUiBaseInfo(UiCommandResult result) {
        UiScenarioReportBaseInfoDTO baseInfoDTO = new UiScenarioReportBaseInfoDTO();
        baseInfoDTO.setReqName(result.getCmdName());
        baseInfoDTO.setReqSuccess(result.isSuccess());
        baseInfoDTO.setReqStartTime(result.getStartTime());
        baseInfoDTO.setRspTime(result.getEndTime() - result.getStartTime());
        baseInfoDTO.setIsNotStep(result.getIsNotStep());
        baseInfoDTO.setUiImg(result.getUiImg());
        baseInfoDTO.setReportId(result.getReportId());
        baseInfoDTO.setUiScreenshots(JSON.toJSONString(result.getUiScreenshots()));
        baseInfoDTO.setOutputList(JSON.toJSONString(result.getOutputList()));
        return baseInfoDTO;
    }


    private void addExtraction(UiCommandResult currentMainResult, UiCommandResult result) {
        //收集截图信息
        currentMainResult.getUiScreenshots().addAll(result.getUiScreenshots());
        if (CollectionUtils.isNotEmpty(result.getOutputList())) {
            if (CollectionUtils.isEmpty(currentMainResult.getOutputList())) {
                currentMainResult.setOutputList(Lists.newArrayList());
            }
            currentMainResult.getOutputList().addAll(result.getOutputList());
        }
        if (extractCommands.contains(result.getCmdName())) {
            if (currentMainResult.getVars() == null) {
                currentMainResult.setVars(result.getBody());
            } else {
                currentMainResult.setVars(currentMainResult.getVars() + "\n" + result.getBody());
            }
        }
    }

    private void addAssertion(UiCommandResult currentMainResult, UiCommandResult result) {
        if (assertionCommands.contains(result.getCmdName())) {
            ResponseAssertionResult assertionResult = new ResponseAssertionResult();
            assertionResult.setMessage(result.getBody());
            assertionResult.setName(result.getCmdName());
            assertionResult.setPass(result.isSuccess());
            currentMainResult.getAssertions().add(assertionResult);
            currentMainResult.setTotalAssertions(currentMainResult.getTotalAssertions() + 1);
            currentMainResult.setPassAssertions(result.isSuccess() ? currentMainResult.getPassAssertions() + 1 : currentMainResult.getPassAssertions());
            if (!assertionResult.isPass()) {
                currentMainResult.setSuccess(false);
            }
        }
    }

    public UiCommandResult selectReportContent(String stepId) {
        return uiScenarioReportStructureService.selectReportContent(stepId, UiCommandResult.class);
    }

    public void delete(DeleteAPIReportRequest request) {

        UiScenarioReport report = uiScenarioReportMapper.selectByPrimaryKey(request.getId());

        uiScenarioReportService.deleteScenarioReportResource(request.getId());

        // 为 UI 类型报告，需要删除报告产生的截图
        this.cleanUpUiReportImg(Arrays.asList(request.getId()));

        // 补充逻辑，如果是集成报告则把零时报告全部删除
        if (report != null && StringUtils.isNotEmpty(report.getScenarioId())) {
            List<String> list = uiScenarioReportService.getReportIds(report.getScenarioId());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
                UiReportBatchRequest reportRequest = new UiReportBatchRequest();
                reportRequest.setIsUi(request.getIsUi());
                reportRequest.setIds(list);
                deleteAPIReportBatch(reportRequest);
            }
        }
    }

    public void deleteAPIReportBatch(UiReportBatchRequest reportRequest) {
        List<String> ids = uiScenarioReportService.getIdsByDeleteBatchRequest(reportRequest);
        ids = uiScenarioReportService.batchDeleteReportResource(reportRequest, ids, false);

        // 删除报告产生的截图
        this.cleanUpUiReportImg(ids);

        //处理最后剩余的数据
        if (!ids.isEmpty()) {
            uiScenarioReportService.deleteScenarioReportByIds(ids);
        }
    }

    public void cleanUpUiReportImg(List<String> ids) {
        try {
            if (ids != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(ids)) {
                for (String id : ids) {
                    if (FileUtil.deleteDir(new File(FileUtils.UI_IMAGE_DIR + "/" + id))) {
                        LogUtil.info("删除 UI 报告截图成功，报告 ID 为 ：" + id);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    private void gergeDealTime(List<UiCommandResult> response) {
        for (int i=0; i< response.size(); i++) {
            UiCommandResult result = response.get(i);
            if (StringUtils.equals(result.getProcessType(), UiProcessType.PRE.name()) && i < response.size()-1) {
                UiCommandResult mainResult = response.get(i+1);
                mainResult.setStartTime(result.getStartTime());
            }
            if (StringUtils.equals(result.getProcessType(), UiProcessType.POST.name()) && i > 0) {
                UiCommandResult mainResult = response.get(i-1);
                mainResult.setEndTime(result.getEndTime());
            }
        }
    }

    public UiScenarioReport getReport(DeleteAPIReportRequest request) {
        return uiScenarioReportMapper.selectByPrimaryKey(request.getId());
    }
}
