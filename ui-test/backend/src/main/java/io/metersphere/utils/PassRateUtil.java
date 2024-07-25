package io.metersphere.utils;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.UiScenarioReportStructureMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.ReportStatus;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.StepTreeDTO;
import io.metersphere.service.UiScenarioReportStructureService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API/UI/PERFORMANCE 通过率计算工具
 */
public class PassRateUtil {

    private final static String ZERO_PERCENT = "0%";

    private final static String TOTAL = "total";

    public static String calculatePassRate(List<UiScenarioReportResultWithBLOBs> requestResults, UiScenarioReport report) {
        if (CollectionUtils.isEmpty(requestResults)) {
            return ZERO_PERCENT;
        } else {
            long successSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ReportStatus.SUCCESS.name())).count();
            //resourceId -> 结果列表 循环可能一个 resourceId 对应多个接口
            Map<String, List<UiScenarioReportResultWithBLOBs>> resultResourceStatusMap = requestResults.stream().collect(Collectors.groupingBy(UiScenarioReportResult::getResourceId));

            UiScenarioReportStructureExample e = new UiScenarioReportStructureExample();
            e.createCriteria().andReportIdEqualTo(report.getId());

            List<UiScenarioReportStructureWithBLOBs> apiScenarioReportStructures = CommonBeanFactory.getBean(UiScenarioReportStructureMapper.class).selectByExampleWithBLOBs(e);
            if (CollectionUtils.isEmpty(apiScenarioReportStructures)) {
                return ZERO_PERCENT;
            }
            List<StepTreeDTO> stepList = JsonUtils.parseArray(new String(apiScenarioReportStructures.get(0).getResourceTree(), StandardCharsets.UTF_8), StepTreeDTO.class);
            ((UiScenarioReportStructureService) CommonBeanFactory.getBean("uiScenarioReportStructureService")).reportFormatting(stepList, resultResourceStatusMap, "UI");

            successSize = getUISuccessSize(stepList);
            if (CollectionUtils.isEmpty(stepList)) {
                return ZERO_PERCENT;
            }
            Integer totalSteps = getTotalSteps(stepList);
            return new DecimalFormat(ZERO_PERCENT).format((float) successSize /totalSteps );
        }
    }

    /**
     * 计算 UI 成功的步骤数
     *
     * @param stepList
     * @return
     */
    private static Integer getUISuccessSize(List<StepTreeDTO> stepList) {
       int i=0;
       return childrenSize(stepList,i,ReportStatus.SUCCESS.name());
    }

    private static int childrenSize(List<StepTreeDTO> stepList, int i, String type) {
        for (StepTreeDTO stepTreeDTO : stepList) {
            if (stepTreeDTO.getChildren().size()>0) {
                i = childrenSize(stepTreeDTO.getChildren(),i,type);
            } else {
                if (StringUtils.equalsIgnoreCase(ReportStatus.SUCCESS.name(), type)) {
                    if (StringUtils.equalsIgnoreCase(ReportStatus.SUCCESS.name(), stepTreeDTO.getTotalStatus())) {
                        i = i+1;
                    }
                }
                if (StringUtils.equalsIgnoreCase(TOTAL, type)) {
                    i = i+1;
                }

            }
        }
        return i;
    }

    /**
     * 计算总步骤数
     *
     * @param stepList
     * @return
     */
    private static Integer getTotalSteps(List<StepTreeDTO> stepList) {
        int i=0;
        return childrenSize(stepList,i,TOTAL);
    }


}
