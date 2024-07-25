package io.metersphere.dto;

import io.metersphere.hashtree.MsUiScenario;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SaveUiScenarioRequest {
    private String id;

    private String projectId;

    private String tags;

    private String userId;

    private String moduleId;

    private String environmentId;

    private String modulePath;

    private String name;

    private String level;

    private String status;

    private String principal;

    private Integer stepTotal;

    private List<String> follows;

    private String schedule;

    private String description;

    private Integer version;

    private String commandViewStruct;

    private MsUiScenario scenarioDefinition;

    List<String> bodyFileRequestIds;

    List<String> scenarioFileIds;

    private List<String> scenarioIds;

    private boolean isSelectAllDate;

    private Map<String, List<String>> filters;

    private List<String> moduleIds;

    private List<String> unSelectIds;

    private String customNum;

    private String environmentType;
    private String environmentJson;
    private String environmentGroupId;

    private String versionId;

    private String scenarioType;
}
