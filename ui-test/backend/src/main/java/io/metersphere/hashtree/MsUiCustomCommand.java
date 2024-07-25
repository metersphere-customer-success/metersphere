package io.metersphere.hashtree;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.dto.ScenarioVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsUiCustomCommand extends MsUiScenario {
    private String type = "CustomCommand";
    private String clazzName = MsUiCustomCommand.class.getCanonicalName();

    @JsonProperty
    private String outputVariables;

    @JsonProperty
    private String description;

    @JsonProperty
    private Boolean nameEdited;

    @JsonProperty
    private Boolean descEdited;
}
