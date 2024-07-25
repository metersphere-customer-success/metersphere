package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TestPlanRefResp extends RefResp{
    private String workspaceId;
    private String projectId;
}
