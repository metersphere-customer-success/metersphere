package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.UiElement;
import io.metersphere.dto.element.UiElementDto;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.scenario.request.testcase.QueryNodeRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtUiElementMapper {

    List<UiElement> list(@Param("request") BaseQueryRequest request);

    List<String> listIdsByQuery(@Param("request") BaseQueryRequest request);

    Long getPreOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    Long getLastOrder(@Param("projectId") String projectId, @Param("baseOrder") Long baseOrder);

    UiElement getMaxNumByProjectId(String s);

    List<UiElement> listNames(@Param("request") BaseQueryRequest request);

    List<Map<String, Object>> groupByModuleId(@Param("request") QueryNodeRequest request);

    UiElement getNextNum(@Param("projectId") String projectId);

    List<Map<String, Object>> listModuleByCollection(@Param("request") QueryNodeRequest request);

}
