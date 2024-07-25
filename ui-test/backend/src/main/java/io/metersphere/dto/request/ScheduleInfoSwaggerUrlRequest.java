package io.metersphere.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleInfoSwaggerUrlRequest {
    private String taskId;
    private Boolean taskStatus;
    private String id;
}
