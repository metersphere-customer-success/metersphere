package io.metersphere.scenario.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TCPRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.TCP;
    @JsonProperty
    private String classname = "";
    @JsonProperty
    private String server = "";
    @JsonProperty
    private String port = "";
    @JsonProperty
    private String ctimeout = "";
    @JsonProperty
    private String timeout = "";
    @JsonProperty
    private boolean reUseConnection;
    @JsonProperty
    private boolean nodelay;
    @JsonProperty
    private boolean closeConnection;
    @JsonProperty
    private String soLinger = "";
    @JsonProperty
    private String eolByte = "";
    @JsonProperty
    private String request = "";
    @JsonProperty
    private String username = "";
    @JsonProperty
    private String password = "";
}
