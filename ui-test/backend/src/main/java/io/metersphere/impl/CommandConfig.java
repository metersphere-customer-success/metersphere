package io.metersphere.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.config.HttpConfigCondition;
import io.metersphere.constants.UiProcessType;
import io.metersphere.dto.ScenarioVariable;
import io.metersphere.utils.ScopeVariableHandler;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 指令的执行逻辑配置类
 */
@Setter
@Getter
public class CommandConfig {

    //指令执行失败是否继续
    private boolean ignoreFail;

    /**
     * 截图配置 (0: 当前步骤截图 1: 出现异常截图  2: 不截图)
     */
    private Integer screenshotConfig;

    //指令断言失败是否继续
    private boolean ignoreAssertFail;

    //等待页面打开的秒钟数
    private Integer secondsWaitWindowOnLoad = 150;

    //等待元素出现时间
    private Integer secondsWaitElement = 15000;

    //如果 true 指令执行成功截图
    private boolean screenShotWhenSuccess = true;

    //如果 true 指令执行失败截图
    private boolean screenShotWhenFail = true;

    // 截图地址
    private String screenShotAddress = FileUtils.UI_IMAGE_DIR + "/";

    //如果是步骤，则 jmeter 会返回结果，否则不会返回结构
    private Boolean isNotStep = false;

    // 指令的流程类型
    private String processType = UiProcessType.MAIN.name();

    // 场景变量
    private List<ScenarioVariable> scenarioVariables;

    // 环境变量 对应环境Id的环境变量
    private List<ScenarioVariable> envVariables;

    // 环境Id
    private String envId;

    //作用域相关 场景变量 key scenarioId, vars
    private ScopeVariableHandler scopeScenarioVariables;

    // vo转成CommandsDTO之后自定义的一些参数
    private Object customParam;

    // 报告 ID,包括调试报告和常规报告
    private String reportId;

    private Integer resolutionRatioX;

    private Integer resolutionRatioY;

    /**
     * UI 是否启用无头模式
     */
    private boolean headlessEnabled = true;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 用于区分断言或数据提取
     */
    private String combinationProxyId;

    /**
     * 是否是数据提取
     */
    private boolean isExtract;

    /**
     * json 指令在 MsUiCommand 的参数 json 字符串形式
     *
     * @param args
     */
    @JsonIgnore
    private UiCommandBaseVo vo;

    /**
     * 环境配置中用于代换 url 的匹配规则,比如 type=PATH 表示如果 open 打开的url 包含 value 中的字符串，则用 socket 的值替换 open 指令中的域名
     */
    List<HttpConfigCondition> conditions;

}
