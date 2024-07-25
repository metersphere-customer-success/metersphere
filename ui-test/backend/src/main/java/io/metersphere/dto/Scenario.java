package io.metersphere.dto;

import io.metersphere.api.exec.scenario.DatabaseConfig;
import io.metersphere.config.DubboConfig;
import io.metersphere.config.TCPConfig;
import io.metersphere.scenario.request.Request;
import lombok.Data;
import org.asynchttpclient.util.Assertions;

import java.util.List;

@Data
public class Scenario {
    private String id;
    private String name;
    private String url;
    private String environmentId;
    private boolean enableCookieShare;
    private List<KeyValue> variables;
    private List<KeyValue> headers;
    private List<Request> requests;
    private Assertions assertions;
    private DubboConfig dubboConfig;
    private TCPConfig tcpConfig;
    private List<DatabaseConfig> databaseConfigs;
    private boolean enable = true;
    private Boolean referenceEnable;
}
