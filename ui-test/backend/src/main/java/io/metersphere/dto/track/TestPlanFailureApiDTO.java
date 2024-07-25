package io.metersphere.dto.track;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanFailureApiDTO {
    private String response;
    private String reportId;
}
