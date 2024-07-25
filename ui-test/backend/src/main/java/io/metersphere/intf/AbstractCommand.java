package io.metersphere.intf;

import io.metersphere.utils.ArgTypeValidator;
import org.json.JSONObject;
import org.springframework.util.Assert;

import java.util.Optional;

public abstract class AbstractCommand implements SeleniumIDECommand, CypressCommand {
    /**
     * 转为 Selenium 脚本
     * 带有校验参数的祖先方法
     *
     * @param param
     * @return
     */
    public String toWebdriverSamplerScript(JSONObject param) {
        // 暂时注释校验，某些指令触发校验会报错
        //Assert.isTrue(ArgTypeValidator.validate(getTargetType(), Optional.ofNullable(param.getString("target")).orElse("")), "targetType validate fail!");
        //Assert.isTrue(ArgTypeValidator.validate(getValueType(), Optional.ofNullable(param.getString("value")).orElse("")), "valueType validate fail!");
        return this._toWebdriverSamplerScript(param);
    }


    /**
     * 转为 Cypress 脚本
     * 带有校验参数的祖先方法
     *
     * @param param
     * @return
     */
    public String toCypressScript(JSONObject param) {
        Assert.isTrue(ArgTypeValidator.validate(getTargetType(), Optional.ofNullable(param.getString("target")).orElse("")), "targetType validate fail!");
        Assert.isTrue(ArgTypeValidator.validate(getValueType(), Optional.ofNullable(param.getString("value")).orElse("")), "valueType validate fail!");
        return this._toCypressScript(param);
    }
}
