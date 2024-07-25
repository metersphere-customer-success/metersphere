package io.metersphere.intf;

import io.metersphere.constants.ArgTypeEnum;
import io.metersphere.constants.AtomicCommandTypeEnum;

public interface Command {
    /**
     * 指令名称
     *
     * @return
     */
    String getName();

    /**
     * 指令名称
     *
     * @return
     */
    String getCnName();

    /**
     * 指令名称
     *
     * @return
     */
    String getTwName();

    /**
     * 指令类型
     *
     * @return
     */
    AtomicCommandTypeEnum getCommandType();

    /**
     * 操作对象参数类型
     *
     * @return
     */
    ArgTypeEnum getTargetType();

    /**
     * 值的类型
     *
     * @return
     */
    ArgTypeEnum getValueType();
}
