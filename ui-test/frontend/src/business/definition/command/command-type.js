/**
 * atomic 原子指令，前端 后端 一对一
 * combine 前端指令包含了多个指令实现 比如 IF 指令，if 指令必须有 start 和 end
 * proxy 前端指令使用了代理模式 比如前端选择等待元素指令，实际上还有一个子类别具体选择，waitForElement 还是 waitForEditable
 * combination_proxy 前端指令组合了多个代理指令
 */
export const COMMAND_TYPE_ATOM = "atom";
export const COMMAND_TYPE_COMBINATION = "combination";
export const COMMAND_TYPE_PROXY = "proxy";
export const COMMAND_TYPE_COMBINATION_PROXY = "combination_proxy";
