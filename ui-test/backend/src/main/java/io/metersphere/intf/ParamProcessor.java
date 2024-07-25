package io.metersphere.intf;

import io.metersphere.hashtree.MsUiCommand;

@FunctionalInterface
public interface ParamProcessor {
    //处理不同类型的参数 targetVO 到 target valueVO 到 value

    /**
     * @param command 指令的名称 如 select
     * @param uiCommand  ui指令
     * @param type    target / value 表示处理 target 还是 value
     * @return
     */
    void process(String command, MsUiCommand uiCommand, String type);
}
