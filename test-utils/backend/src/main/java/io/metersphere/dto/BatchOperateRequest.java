package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchOperateRequest {
    private List<String> ids;
    boolean selectAll;
    private List<String> unSelectIds;
    private DataRules dataRule;
    private String projectId;
}
