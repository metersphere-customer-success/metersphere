package io.metersphere.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.util.List;

/**
 * 前端复合指令定义类
 */
public class ComplexCommand extends CommonCommand {

    @JsonProperty("commandType")
    private String commandType;

    @JsonProperty("atomicCommands")
    private List<String> atomicCommands;

    @JsonProperty("startCommand")
    private String startCommand;

    @JsonProperty("endCommand")
    private String endCommand;

    @JsonProperty("vo")
    private JSONObject vo;
}
