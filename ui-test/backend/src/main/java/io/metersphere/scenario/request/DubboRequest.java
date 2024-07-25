package io.metersphere.scenario.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.dto.KeyValue;
import io.metersphere.scenario.request.dubbo.ConfigCenter;
import io.metersphere.scenario.request.dubbo.ConsumerAndService;
import io.metersphere.scenario.request.dubbo.RegistryCenter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DubboRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.DUBBO;
    @JsonProperty
    private String protocol;
    @JsonProperty
    private String _interface;
    @JsonProperty
    private String method;

    @JsonProperty
    private ConfigCenter configCenter;
    @JsonProperty
    private RegistryCenter registryCenter;
    @JsonProperty
    private ConsumerAndService consumerAndService;

    @JsonProperty
    private List<KeyValue> args;
    @JsonProperty
    private List<KeyValue> attachmentArgs;
}
