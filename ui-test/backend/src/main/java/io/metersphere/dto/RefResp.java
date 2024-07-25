package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RefResp {
    private String id;
    private String scenarioName;
    private String workspace;
    private String project;
    private String dateStr;
    private String targetId;
    private String workspaceId;
    private String projectId;
}
