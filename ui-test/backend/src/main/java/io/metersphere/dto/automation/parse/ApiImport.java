package io.metersphere.dto.automation.parse;

import io.metersphere.dto.Scenario;
import lombok.Data;

import java.util.List;

@Data
public class ApiImport {
    private List<Scenario> scenarios;
}
