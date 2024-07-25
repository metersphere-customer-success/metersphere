package io.metersphere.dto;

import lombok.Data;

@Data
public class EnvironmentConfig {
    private CommonConfig commonConfig;
    private HttpConfig httpConfig;
    private String environmentId;
}
