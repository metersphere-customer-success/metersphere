package io.metersphere.scenario.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.dto.extract.Extract;
import lombok.Data;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.timers.ConstantTimer;
import org.asynchttpclient.util.Assertions;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpRequest.class, name = RequestType.HTTP),
        @JsonSubTypes.Type(value = DubboRequest.class, name = RequestType.DUBBO),
        @JsonSubTypes.Type(value = SqlRequest.class, name = RequestType.SQL),
        @JsonSubTypes.Type(value = TCPRequest.class, name = RequestType.TCP)
})
@Data
public abstract class Request {
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private boolean enable = true;
    @JsonProperty
    private boolean useEnvironment;
    @JsonProperty
    private Assertions assertions;
    @JsonProperty
    private Extract extract;
    @JsonProperty
    private JSR223PreProcessor jsr223PreProcessor;
    @JsonProperty
    private JSR223PostProcessor jsr223PostProcessor;
    @JsonProperty
    private IfController controller;
    @JsonProperty
    private ConstantTimer timer;
}
