package io.metersphere.dto.request;

import io.metersphere.base.domain.Schedule;
import io.metersphere.dto.KeyValue;
import io.metersphere.dto.request.auth.MsAuthManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/12 12:57 下午
 * @Description
 */
@Getter
@Setter
public class ScheduleRequest extends Schedule {

    //定时任务来源： 测试计划/测试场景
    private String scheduleFrom;

    private String projectId;

    private String moduleId;

    private String modulePath;

    private String modeId;

    private String swaggerUrl;

    private String taskId;

    // 鉴权相关
    private List<KeyValue> headers;
    private List<KeyValue> arguments;
    private MsAuthManager authManager;
    private Boolean coverModule = false;

}