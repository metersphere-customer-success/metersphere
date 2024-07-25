package io.metersphere.constants;

public class TemplateConstants {
    //指令的封装脚本
    public static final String TEMPLATE_CMDWRAP = "cmdwrap";
    // 逻辑控制或者循环控制封装脚本
    public static final String TEMPLATE_EMPTYWRAP = "emptywrap";
    //全局注入 js 脚本
    public static final String TEMPLATE_INJECTSCRIPT = "injectscript";
    //画图相关函数库
    public static final String FUNCTION_DRAW_SCRIPT = "draw-functions";
    //初始化retry变量
    public static final String FUNCTION_INIT_RETRY_SCRIPT = "init-retry-functions";
    //检查retry变量
    public static final String FUNCTION_RETRY_SCRIPT = "retry-functions";
    //转换jackson防止序列化报错
    public static final String FUNCTION_CONVERT_JACKSON = "convert-jackson";
    //类型处理相关函数
    public static final String FUNCTION_TYPE = "type-function";
    //与环境有关逻辑处理函数
    public static final String FUNCTION_ENVIRONMENT = "environment-functions";
}
