package io.metersphere.utils.processor;

import io.metersphere.commons.utils.JSON;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CompatibilityOldData implements Serializable {

    public static String convertConstantType(Object value) {
        String valueStr = String.valueOf(value);
        String type;
        try {
            int i = Integer.parseInt(valueStr);
            type = "NUMBER";
            return type;
        } catch (Exception e) {
        }
        try {
            if (StringUtils.isNotBlank(valueStr) && valueStr.startsWith("[") && valueStr.endsWith("]")) {
                List list = JSON.parseArray(valueStr);
                type = "ARRAY";
            } else {
                Object o = JSON.parseObject(valueStr);
                type = "JSON";
            }
            return type;
        } catch (Exception e) {
        }

        type = "STRING";

        return type;
    }
}
