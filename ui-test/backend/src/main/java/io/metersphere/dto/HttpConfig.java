package io.metersphere.dto;

import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.config.Cookie;
import io.metersphere.config.HttpConfigCondition;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class HttpConfig {
    private String apiEnvironmentid;
    private String socket;
    private String domain;
    private String protocol = "https";
    private Cookie[] cookie;
    private int port;
    private List<HttpConfigCondition> conditions;
    private boolean headlessEnabled;
    private String browser;


    public HttpConfig initHttpConfig(HttpConfigCondition configCondition) {
        HttpConfig config = new HttpConfig();
        BeanUtils.copyBean(config, configCondition);
        return config;
    }

    public HttpConfig getPathCondition(String path, HttpConfigCondition configCondition) {
        if (CollectionUtils.isNotEmpty(configCondition.getDetails())) {
            List<KeyValue> details = configCondition.getDetails().stream().filter(detail -> (detail.getValue().equals("contains") && StringUtils.contains(path, detail.getName())) || (detail.getValue().equals("equals") && StringUtils.equals(path, detail.getName()))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(details)) {
                return initHttpConfig(configCondition);
            }
        }
        return null;
    }
}
