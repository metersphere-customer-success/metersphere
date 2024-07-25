package io.metersphere.dto;

import io.metersphere.dto.request.auth.MsAuthManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UiTestImportRequest {
    private String name;
    private String id;
    private String moduleId;
    private String modulePath;
    private String environmentId;
    private String projectId;
    private String platform;
    private Boolean useEnvironment;
    private String swaggerUrl;
    private String fileName;
    private String resourceId;
    //导入策略
    private String modeId;
    private String userId;
    private String versionId; // 新导入选择的版本
    private String updateVersionId; // 覆盖导入已存在的接口选择的版本
    private String defaultVersion;
    //调用类型
    private String type;
    // 是否开启自定义ID
    private Boolean openCustomNum = false;
    // 鉴权相关
    private List<KeyValue> headers;
    private List<KeyValue> arguments;
    private MsAuthManager authManager;
    private String scenarioType;

}
