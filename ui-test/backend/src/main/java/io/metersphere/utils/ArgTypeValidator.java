package io.metersphere.utils;


import io.metersphere.constants.ArgTypeEnum;
import io.metersphere.intf.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * 参数校验器
 */
public class ArgTypeValidator {
    private static Map<ArgTypeEnum, Validator> validatorMap = new HashMap<>();

    static {
        for (ArgTypeEnum value : ArgTypeEnum.values()) {
            validatorMap.put(value, value.getValidator());
        }
    }

    public static boolean validate(ArgTypeEnum argTypeEnum, String param) {
        if (validatorMap.get(argTypeEnum) == null) return true;
        return validatorMap.get(argTypeEnum)._validate(param);
    }

}
