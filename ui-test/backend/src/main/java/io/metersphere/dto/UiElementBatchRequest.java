package io.metersphere.dto;

import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiElementBatchRequest extends BaseQueryRequest {

    private String moduleId;
}
