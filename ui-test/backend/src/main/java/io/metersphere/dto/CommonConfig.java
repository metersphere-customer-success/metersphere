package io.metersphere.dto;

import com.shapesecurity.salvation2.Values.Host;
import lombok.Data;

import java.util.List;

@Data
public class CommonConfig {
    private List<ScenarioVariable> variables;
    private boolean enableHost;
    private List<Host> hosts;
    private int requestTimeout;
    private int responseTimeout;
}