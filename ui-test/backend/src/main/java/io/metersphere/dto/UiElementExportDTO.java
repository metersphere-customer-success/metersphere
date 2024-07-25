package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UiElementExportDTO {

    private String projectId;
    private Boolean selectAll;
    private List<String> ids;
    private String name;
    private List<String> nodeIds;
}
