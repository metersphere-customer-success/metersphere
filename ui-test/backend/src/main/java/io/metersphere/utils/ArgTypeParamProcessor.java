package io.metersphere.utils;

import io.metersphere.constants.ArgTypeEnum;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.intf.ParamProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * 参数处理器
 */
public class ArgTypeParamProcessor {
    private static Map<ArgTypeEnum, ParamProcessor> processorMap = new HashMap<>();

    static {
        for (ArgTypeEnum value : ArgTypeEnum.values()) {
            processorMap.put(value, value.getParamProcessor());
        }
    }

    public static void process(ArgTypeEnum argTypeEnum, String command, MsUiCommand uiCommand, String type) {
        if (processorMap.get(argTypeEnum) != null)
            processorMap.get(argTypeEnum).process(command, uiCommand, type);
    }

}
