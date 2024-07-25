package io.metersphere.dto;

import io.metersphere.dto.api.UiExecutionQueueParam;
import lombok.Data;
import org.apache.jorphan.collections.HashTree;

@Data
public class UiJmeterRunRequestDTO extends JmeterRunRequestDTO {
    private UiExecutionQueueParam uiExecutionQueueParam;

    public UiJmeterRunRequestDTO(String testId, String reportId, String runMode, HashTree o) {
        super(testId, reportId, runMode, o);
    }
}
