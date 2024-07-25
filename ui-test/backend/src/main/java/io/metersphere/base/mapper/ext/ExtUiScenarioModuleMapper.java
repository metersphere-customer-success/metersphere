package io.metersphere.base.mapper.ext;

import io.metersphere.dto.UiScenarioModuleDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUiScenarioModuleMapper {
    List<UiScenarioModuleDTO> getNodeTreeByProjectId(@Param("projectId") String projectId);

    void updatePos(String id, Double pos);
}
