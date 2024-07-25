package io.metersphere.dto.request;

import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.EnvironmentConfig;
import io.metersphere.dto.KeyValue;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.dto.ScenarioVariable;
import io.metersphere.scenario.ssl.MsKeyStore;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;

import java.util.*;

@Data
public class ParameterConfig extends MsParameter {
    /**
     * 环境配置 在debug和正常执行时与 environmentJson 互斥只有一个生效
     */
    private Map<String, EnvironmentConfig> config;

    /**
     * 环境信息
     */
    private String environmentJson;

    /**
     * UI 指令全局配置
     */
    private Object commandConfig;
    /**
     * 缓存同一批请求的认证信息
     */
    private Map<String, MsKeyStore> keyStoreMap = new HashMap<>();
    /**
     * 公共场景参数
     */
    private List<ScenarioVariable> variables;
    /**
     * 当前场景变量，逐层传递
     */
    private List<ScenarioVariable> transferVariables;

    /**
     * 公共场景参数
     */
    private List<KeyValue> headers;

    /**
     * 公共Cookie 免登录
     */
    private boolean enableCookieShare;
    /**
     * 是否停止继续
     */
    private Boolean onSampleError;

    /**
     * 是否是导入/导出操作
     */
    private boolean isOperating;
    /**
     * 导入/导出操作时取样器的testname值
     */
    private String operatingSampleTestName;
    /**
     * 项目ID，支持单接口执行
     */
    private String projectId;

    private String scenarioId;

    /**
     * 报告 ID
     */
    private String reportId;

    private String reportType;

    private boolean runLocal;

    private String browserLanguage;

    /**
     * 排除生成临界控制器的场景
     */
    private List<String> excludeScenarioIds = new ArrayList<>();

    private List<String> csvFilePaths = new ArrayList<>();

    /**
     * 是否开始重试
     */
    private Boolean retryEnable;

    /**
     * 重试次数
     */
    private Long retryNumber;

    /**
     * 生成 cookie
     */
    private boolean generateCookie;

    /**
     * 当前线程使用的 cookie
     */
    private String cookie;

    /**
     * 场景执行的来源
     * TEST_PLAN 由于有历史的没有环境的数据，所以当测试计划中环境配置为空，但是原始关联场景环境不为空时要以无环境的配置来运行
     */
    private String requestOriginator = SystemConstants.UIRequestOriginatorEnum.UI_MODULE.getName();


    public boolean isEffective(String projectId) {
        if (this.config != null && this.config.get(projectId) != null) {
            return true;
        }
        return false;
    }

    static public Arguments valueSupposeMock(Arguments arguments) {
        for (int i = 0; i < arguments.getArguments().size(); ++i) {
            String argValue = arguments.getArgument(i).getValue();
            arguments.getArgument(i).setValue(ScriptEngineUtils.buildFunctionCallString(argValue));
        }
        return arguments;
    }

    public void setHeader(String name, String value) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        if (CollectionUtils.isEmpty(headers)) {
            headers = new LinkedList<>();
        }
        for (KeyValue kv : headers) {
            if (StringUtils.equalsIgnoreCase(kv.getName(), name)) {
                kv.setValue(value);
                return;
            }
        }
        KeyValue kv = new KeyValue(name, value);
        headers.add(kv);
    }

    public KeyValue getHeader(String name) {
        if (StringUtils.isNotEmpty(name) && CollectionUtils.isNotEmpty(headers)) {
            for (KeyValue kv : headers) {
                if (StringUtils.equalsIgnoreCase(kv.getName(), name)) {
                    return kv;
                }
            }
        }
        return null;
    }
}
