package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpResponse.class, name = RequestType.HTTP),
})
@Data
public abstract class Response {
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private Boolean enable;
}
