package io.metersphere.dto;

import io.metersphere.commons.constants.VariableTypeConstants;
import io.metersphere.request.BodyFile;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class ScenarioVariable {

    /**
     * CONSTANT LIST CSV COUNTER RANDOM
     */
    private String type = VariableTypeConstants.CONSTANT.name();
    private String id;
    private String name;

    /**
     * 常量值，列表值[] ,计数器输出格式，随机数输出格式
     */
    private Object value;
    private String description;
    /**
     * csv
     */
    private List<BodyFile> files;
    private String delimiter;
    private boolean quotedData;
    private String encoding;
    /**
     * counter
     */
    private int startNumber;
    private int endNumber;
    private int increment;
    /**
     * random
     */
    private String minNumber;
    private String maxNumber;

    private String version;

    private boolean required = false;

    private String originValue;

    private boolean enable = true;

    // ui or api
    private String scope;

    public ScenarioVariable() {

    }

    public ScenarioVariable(String key, String value, String description, String type) {
        this.name = key;
        this.value = value;
        this.description = description;
        this.type = type;
    }

    public boolean isConstantValid() {
        if (StringUtils.isEmpty(this.type) || (StringUtils.equals("text", this.type) && StringUtils.isNotEmpty(name)) || (StringUtils.equals(this.type, VariableTypeConstants.CONSTANT.name()) && StringUtils.isNotEmpty(name))) {
            return true;
        }
        return false;
    }

    public boolean isCSVValid() {
        if (StringUtils.equals(this.type, VariableTypeConstants.CSV.name()) && StringUtils.isNotEmpty(name)) {
            return true;
        }
        return false;
    }

    public boolean isListValid() {
        if(value == null){
            return false;
        }
        if (StringUtils.equals(this.type, VariableTypeConstants.LIST.name()) && StringUtils.isNotEmpty(name) && String.valueOf(value).indexOf(",") != -1) {
            return true;
        }
        return false;
    }

    public boolean isCounterValid() {
        if (StringUtils.equals(this.type, VariableTypeConstants.COUNTER.name()) && StringUtils.isNotEmpty(name)) {
            return true;
        }
        return false;
    }

    public boolean isRandom() {
        if (StringUtils.equals(this.type, VariableTypeConstants.RANDOM.name()) && StringUtils.isNotEmpty(name)) {
            return true;
        }
        return false;
    }
}
