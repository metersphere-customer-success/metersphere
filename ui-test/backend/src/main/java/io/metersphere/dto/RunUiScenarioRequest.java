package io.metersphere.dto;

import io.metersphere.dto.automation.RunScenarioRequest;
import io.metersphere.dto.testcase.UiRunModeConfigDTO;
import lombok.Data;
import java.util.List;

@Data
public class RunUiScenarioRequest extends RunScenarioRequest {
    private UiRunModeConfigDTO uiConfig;

    @Override
    public void setIds(List<String> ids) {
        super.setIds(ids);
    }
}
