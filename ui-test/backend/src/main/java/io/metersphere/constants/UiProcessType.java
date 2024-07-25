package io.metersphere.constants;


/**
 * @author jianxing
 * 表示执行指令的类型
 * MAIN 主要指令，对应报告一个步骤
 * PRE 表示前置操作的指令
 * POST 表示后置操作的指令
 * APPEND 表示主要指令的附加指令，比如cmdWhile中的间隔时间
 */
public enum UiProcessType {
    MAIN,PRE,POST,APPEND
}

