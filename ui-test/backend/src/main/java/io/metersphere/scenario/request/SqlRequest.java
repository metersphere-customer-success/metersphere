package io.metersphere.scenario.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.dto.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SqlRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.SQL;
    @JsonProperty
    private String dataSource;
    @JsonProperty
    private String query;
    @JsonProperty
    private long queryTimeout;
    @JsonProperty
    private String resultVariable;
    @JsonProperty
    private String variableNames;
    @JsonProperty
    private List<KeyValue> variables;
}
