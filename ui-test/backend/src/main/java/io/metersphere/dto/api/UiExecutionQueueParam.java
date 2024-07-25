package io.metersphere.dto.api;

import lombok.Data;

import java.util.Map;

@Data
public class UiExecutionQueueParam {
    private String browser;
    private Boolean headlessEnabled = false;
    private Boolean useCookie = false;
    //ui 执行环境配置　key:projectId value:environmentId
    private Map<String, String> envMap;
}
