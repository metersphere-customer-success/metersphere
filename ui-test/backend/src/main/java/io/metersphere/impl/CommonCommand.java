package io.metersphere.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.metersphere.constants.ArgTypeEnum;
import io.metersphere.constants.AtomicCommandTypeEnum;
import io.metersphere.intf.AbstractCommand;
import io.metersphere.utils.TemplateUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用原子指令的定义类，实现了每一种指令在不同测试框架下的脚本
 */
@NoArgsConstructor
@Data
public class CommonCommand extends AbstractCommand {
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("target")
    private String target;
    @JsonProperty("value")
    private String value;
    @JsonProperty("type")
    private String type;
    @JsonProperty("viewType")
    private String viewType;
    @JsonProperty("cnName")
    private String cnName;
    @JsonProperty("cnDesc")
    private String cnDesc;
    @JsonProperty("twName")
    private String twName;
    @JsonProperty("twDesc")
    private String twDesc;
    @JsonProperty("cnType")
    private String cnType;
    @JsonProperty("targetCNName")
    private String targetCNName;
    @JsonProperty("targetTWName")
    private String targetTWName;
    @JsonProperty("valueCNName")
    private String valueCNName;
    @JsonProperty("valueTWName")
    private String valueTWName;
    @JsonProperty("twType")
    private String twType;
    @JsonProperty("sort")
    private Integer sort;
    @JsonProperty("webdriverScript")
    private String webdriverScript;
    @JsonProperty("cypressScript")
    private String cypressScript;
    //是否支持导出
    @JsonProperty("export")
    private boolean export = true;
    //导出的时候是否注释
    @JsonProperty("exportWithNote")
    private boolean exportWithNote = false;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCnName() {
        try {
            return StringUtils.isNotBlank(cnName) ? cnName : AtomicCommandTypeEnum.getByName(name).getTwName();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getTwName() {
        try {
            return StringUtils.isNotBlank(twName) ? twName : AtomicCommandTypeEnum.getByName(name).getTwName();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public AtomicCommandTypeEnum getCommandType() {
        return AtomicCommandTypeEnum.valueOf(type);
    }

    @Override
    public ArgTypeEnum getTargetType() {
        try {
            return ArgTypeEnum.valueOf(target);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ArgTypeEnum getValueType() {
        try {
            return ArgTypeEnum.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> ignoreReplaceOnceList = Lists.newArrayList("runScript", "executeScript", "executeAsyncScript", "forEach");

    @Override
    public String _toWebdriverSamplerScript(JSONObject param) {
        if (param == null) {
            return null;
        }
        final Map<String, Object> paramMap = new HashMap<>();
        param.keySet().forEach(k -> paramMap.put(k, param.get(k)));
        String originCommandType = param.optString("originCommandType");
        //此处 执行脚本等指令需要多次处理
        if (StringUtils.isNotBlank(originCommandType) && ignoreReplaceOnceList.contains(originCommandType)) {
            return CommandWrapper.wrap(TemplateUtils.replaceVars(param.getString("script"), paramMap), name, param);
        }
        return CommandWrapper.wrap(TemplateUtils.replaceVarsOnce(param.getString("script"), paramMap), name, param);
    }

    @Override
    public String _toCypressScript(JSONObject param) {
        //::todo
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getWebdriverScript() {
        return webdriverScript;
    }

    public String getCypressScript() {
        return cypressScript;
    }
}
