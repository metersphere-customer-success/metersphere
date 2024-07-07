package io.metersphere.db.mapper.ext;

import io.metersphere.dto.DataRulesNodeDTO;
import io.metersphere.dto.QueryDataRulesRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtDataRulesNodeMapper {

    List<DataRulesNodeDTO> getCountNodes(@Param("request") QueryDataRulesRequest request);

    List<DataRulesNodeDTO> getNodeTreeByProjectId(@Param("projectId") String projectId);

    DataRulesNodeDTO getNode(String id);

    void updatePos(String id, Double pos);
}
