package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.impl.CommandConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * SIDE 文件对象
 */
@NoArgsConstructor
@Data
public class SideDTO {

    @JsonProperty("id")
    private String id;
    @JsonProperty("version")
    private String version;
    @JsonProperty("name")
    private String name;
    @JsonProperty("url")
    private String url;
    @JsonProperty("tests")
    private List<TestsDTO> tests;
    @JsonProperty("suites")
    private List<SuitesDTO> suites;
    @JsonProperty("urls")
    private List<String> urls;
    @JsonProperty("plugins")
    private List<?> plugins;

    @NoArgsConstructor
    @Data
    public static class TestsDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("commands")
        private List<CommandsDTO> commands;

        @NoArgsConstructor
        @Data
        public static class CommandsDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("comment")
            private String comment = "";
            @JsonProperty("command")
            private String command = "";
            @JsonProperty("target")
            private String target = "";
            @JsonProperty("targets")
            private List<?> targets;
            @JsonProperty("value")
            private String value = "";
            @JsonProperty("Description")
            private String Description = "";
            @JsonProperty("enable")
            private boolean enable = true;
            @JsonProperty("targetType")
            private String targetType = "";

            /**
             * 打开新窗口有关属性 selenium 打开新窗口要求 windowhandle 必须手动跟随切换，不然无法定位到该窗口的元素
             */
            //是否打开新窗口
            @JsonProperty("opensWindow")
            private boolean opensWindow = false;
            //新打开窗口的句柄名称
            @JsonProperty("windowHandleName")
            private String windowHandleName = "";
            //打开窗口的超时时间 ms
            @JsonProperty("windowTimeout")
            private long windowTimeout = 150000l;

            //每一个指令的配置
            @JsonProperty("commandConfig")
            private CommandConfig commandConfig;
            //自定义名称
            @JsonProperty("customName")
            private String customName;
        }
    }

    @NoArgsConstructor
    @Data
    public static class SuitesDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("persistSession")
        private Boolean persistSession;
        @JsonProperty("parallel")
        private Boolean parallel;
        @JsonProperty("timeout")
        private Integer timeout;
        @JsonProperty("tests")
        private List<String> tests;
    }
}
