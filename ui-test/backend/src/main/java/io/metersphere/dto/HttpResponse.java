package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HttpResponse extends Response {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.HTTP;
    @JsonProperty
    private List<KeyValue> headers;
    @JsonProperty
    private List<KeyValue> statusCode;
    @JsonProperty
    private Body body;

}
