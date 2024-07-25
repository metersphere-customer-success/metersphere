package io.metersphere.utils;


import io.metersphere.base.domain.UiScenarioReportResultWithBLOBs;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.ReportStatus;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.api.UiScenarioReportBaseInfoDTO;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ResultConversionUtil {

    public static UiScenarioReportResultWithBLOBs getApiScenarioReportResult(String reportId, RequestResult requestResult) {
        //解析误报内容
        String resourceId = requestResult.getResourceId();
        UiScenarioReportResultWithBLOBs report = newScenarioReportResult(reportId, resourceId);
        report.setTotalAssertions(Long.parseLong(requestResult.getTotalAssertions() + ""));
        report.setPassAssertions(Long.parseLong(requestResult.getPassAssertions() + ""));
        String status = requestResult.getError() == 0 ? ReportStatus.SUCCESS.name() : ReportStatus.ERROR.toString();
        requestResult.setStatus(status);
        report.setStatus(status);
        report.setRequestTime(requestResult.getEndTime() - requestResult.getStartTime());
        LoggerUtil.info("报告ID [ " + reportId + " ] 执行请求：【 " + requestResult.getName() + "】 入库存储");
        return report;
    }

    public static UiScenarioReportResultWithBLOBs getApiScenarioReportResultBLOBs(String reportId, RequestResult result) {
        UiScenarioReportResultWithBLOBs report = getApiScenarioReportResult(reportId, result);
        report.setBaseInfo(JSON.toJSONString(getBaseInfo(result)));
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }

    //记录基础信息
    private static UiScenarioReportBaseInfoDTO getBaseInfo(RequestResult result) {
        UiScenarioReportBaseInfoDTO baseInfoDTO = new UiScenarioReportBaseInfoDTO();
        baseInfoDTO.setReqName(result.getName());
        baseInfoDTO.setReqSuccess(result.isSuccess());
        baseInfoDTO.setReqError(result.getError());
        baseInfoDTO.setReqStartTime(result.getStartTime());
        if (result.getResponseResult() != null) {
            baseInfoDTO.setRspCode(result.getResponseResult().getResponseCode());
            baseInfoDTO.setRspTime(result.getResponseResult().getResponseTime());
        }
        return baseInfoDTO;
    }

    public static UiScenarioReportResultWithBLOBs newScenarioReportResult(String reportId, String resourceId) {
        UiScenarioReportResultWithBLOBs report = new UiScenarioReportResultWithBLOBs();
        report.setId(UUID.randomUUID().toString());
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(0L);
        report.setPassAssertions(0L);
        report.setCreateTime(System.currentTimeMillis());
        return report;
    }
}
