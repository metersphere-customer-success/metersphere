package io.metersphere.request;

import io.metersphere.base.domain.TestPlanNode;
import io.metersphere.dto.DataRulesNode;
import io.metersphere.dto.DataRulesNodeDTO;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * @author ycr
 */
@Data
public class DragDataRulesRequest extends DataRulesNode {

    @Serial
    private static final long serialVersionUID = -2663513817971996721L;

    private List<String> nodeIds;
    private DataRulesNodeDTO nodeTree;
}
