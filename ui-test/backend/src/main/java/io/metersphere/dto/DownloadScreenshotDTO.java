package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DownloadScreenshotDTO {

    private String reportId;
    private String dirName;
    private List<ScreenshotDTO> resources;

    @Setter
    @Getter
    public static class ScreenshotDTO{
        private String index;
        private String label;
        private String combinationImg;
        private String fileName;
    }
}
