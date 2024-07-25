package io.metersphere.dto;

import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseAssertionResult;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UiCommandResult extends RequestResult {
    private Boolean isNotStep;
    private String processType;
    private String id;
    private String uiImg;
    private String cmdName;
    private long time;
    private long endTime;
    private long startTime;
    private boolean success;
    private int totalAssertions;
    private int passAssertions;
    private String body;
    private String vars;
    private final List<ResponseAssertionResult> assertions = new ArrayList();
    private String reportId;
    private String combinationImg;

    private List<OutputMetaData> outputList = new ArrayList();
    private List<ScreenshotDTO> uiScreenshots;

    @Getter
    @Setter
    public static class OutputMetaData{

        private String id;
        private String name;
        private String key;
        private String value;
        private String type;
        private String processType;
    }

    @Getter
    @Setter
    public static class ScreenshotDTO{
        /**
         * 截图名称
         */
        private String name;

        /**
         * 步骤id
         */
        private String stepId;

        /**
         * 截图时间
         */
        private Long time;

        /**
         * 类型
         */
        private String type;

        /**
         * 截图路径
         */
        private String path;
    }

    public void calTime() {
        if (this.endTime != 0l && this.startTime != 0l) {
            this.time = this.endTime - this.startTime;
        }
    }
}
