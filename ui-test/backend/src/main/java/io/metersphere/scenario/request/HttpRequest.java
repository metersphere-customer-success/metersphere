package io.metersphere.scenario.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.dto.Body;
import io.metersphere.dto.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HttpRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.HTTP;
    @JsonProperty
    private String url;
    @JsonProperty
    private String method;
    @JsonProperty
    private String path;
    @JsonProperty
    private List<KeyValue> parameters;
    @JsonProperty
    private List<KeyValue> headers;
    @JsonProperty
    private Body body;
    @JsonProperty
    private String connectTimeout;
    @JsonProperty
    private String responseTimeout;
    @JsonProperty
    private boolean followRedirects;
    @JsonProperty
    private boolean doMultipartPost;
    @JsonProperty
    private List<KeyValue> rest;
    @JsonProperty
    private AuthConfig authConfig;
    // 和接口定义模块用途区分
    @JsonProperty
    private boolean isDefinition;

}
