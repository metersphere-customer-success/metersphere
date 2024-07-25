package io.metersphere.utils;

import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.dto.ErrorReportLibraryParseDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.RequestResultExpandDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求返回解析工具
 */
public class ResponseUtil {
    public static final String ERROR_REPORT_RESULT = "errorReportResult";

    public static RequestResultExpandDTO parseByRequestResult(RequestResult baseResult) {
        //解析是否含有误报库信息
        ErrorReportLibraryParseDTO errorCodeDTO = ErrorReportLibraryUtil.parseAssertions(baseResult);
        RequestResult requestResult = errorCodeDTO.getResult();
        RequestResultExpandDTO expandDTO = new RequestResultExpandDTO();
        BeanUtils.copyBean(expandDTO, requestResult);

        if (CollectionUtils.isNotEmpty(errorCodeDTO.getErrorCodeList())) {
            Map<String, String> expandMap = new HashMap<>();
            expandMap.put(ERROR_REPORT_RESULT, errorCodeDTO.getErrorCodeStr());
            if (StringUtils.equalsIgnoreCase(errorCodeDTO.getRequestStatus(), ERROR_REPORT_RESULT)) {
                expandMap.put("status", ERROR_REPORT_RESULT);
            }
            expandDTO.setAttachInfoMap(expandMap);
        }
        if (StringUtils.equalsIgnoreCase(errorCodeDTO.getRequestStatus(), ERROR_REPORT_RESULT)) {
            expandDTO.setStatus(errorCodeDTO.getRequestStatus());
        }
        return expandDTO;
    }
}
