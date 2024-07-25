import {COMMAND_TYPE_COMBINATION} from "./command-type";

const programController = [
  {
    "name": "cmdTimes",
    "command": "cmdTimes",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_COMBINATION,
    "startCommand": 'times',
    "endCommand": 'end',
    "sort": 1,
    "component": "Times",
    "cnDesc": "将步骤拖动到里面，可以设置步骤执行次数",
    "twDesc": "將步驟拖動到裡面，可以設置步驟執行次數",
    "enDesc": "Drag the step into it, you can set the number of steps to be executed",
  },
  {
    "name": "cmdForEach",
    "command": "cmdForEach",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_COMBINATION,
    "startCommand": 'forEach',
    "endCommand": 'end',
    "sort": 1,
    "component": "ForEach",
    "cnDesc": "将步骤拖动到里面，可以遍历给定的集合",
    "twDesc": "將步驟拖動到裡面，可以遍歷給定的集合",
    "enDesc": "Drag steps into it to traverse the given collection",

  },
  {
    "name": "cmdWhile",
    "command": "cmdWhile",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_COMBINATION,
    "startCommand": 'while',
    "endCommand": 'end',
    "sort": 1,
    "component": "While",
    "cnDesc": "设置循环表达式，满足表达式的条件则循环执行里面的步骤",
    "twDesc": "設置循環表達式，滿足表達式的條件則循環執行里面的步驟",
    "enDesc": "Set the loop expression, if the conditions of the expression are satisfied, the steps inside will be executed in a loop",
  },
  {
    "name": "cmdIf",
    "command": "cmdIf",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_COMBINATION,
    "startCommand": 'if',
    "endCommand": 'end',
    "sort": 1,
    "component": "If",
    "cnDesc": "设置表达式，满足表达式的条件则循环执行里面的步骤",
    "twDesc": "設置表達式，滿足表達式的條件則循環執行里面的步驟",
    "enDesc": "Set the expression, and if the conditions of the expression are satisfied, the steps inside will be executed in a loop",
  },
  {
    "name": "cmdElseIf",
    "command": "cmdElseIf",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_COMBINATION,
    "startCommand": 'elseIf',
    "endCommand": 'end',
    "sort": 1,
    "component": "If",
    "cnDesc": "配置If指令进行流程控制，不满足 If 条件且满足 ElseIf 条件则执行",
    "twDesc": "配置If指令進行流程控制，不滿足 If 條件且滿足 ElseIf 條件則執行",
    "enDesc": "Configure the If instruction to control the flow, and execute if the If condition is not met and the ElseIf condition is met",
  },
  {
    "name": "cmdElse",
    "command": "cmdElse",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_COMBINATION,
    "startCommand": 'else',
    "endCommand": 'end',
    "sort": 1,
    "component": "Else",
    "cnDesc": "配置 If 指令进行流程控制，不满足 If 条件则执行",
    "twDesc": "配置 If 指令進行流程控制，不滿足 If 條件則執行",
    "enDesc": "Configure the If instruction to control the flow and execute if the If condition is not met",
  }
];

programController.forEach(item => {
  item.viewType = "programController";
});

export default programController;
