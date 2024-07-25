import {COMMAND_TYPE_PROXY} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"

const browserOperation = [
  // 打开网页
  {
    "name": "cmdOpen",
    "command": "cmdOpen",
    "cnDesc": "使用指定的浏览器打开网页",
    "twDesc": "使用指定的瀏覽器打開網頁",
    "enDesc": "Open the webpage with the specified browser",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_PROXY,
    "viewType": "browserOperation",
    "atomicCommands": [
      AtomicCommandDefinition.open,
    ],
    "sort": 1,
    "component": "Open",
    "vo": cloneDeep(CommandVODefinition.OpenVO),
  },
  // 关闭网页
  AtomicCommandDefinition.close,
  // 切换窗口
  {
    "name": "cmdSelectWindow",
    "command": "cmdSelectWindow",
    "cnDesc": "切换到指定窗口",
    "twDesc": "切換到指定窗口",
    "enDesc": "switch to the specified window",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_PROXY,
    "viewType": "browserOperation",
    "atomicCommands": [
      AtomicCommandDefinition.selectWindow,
    ],
    "sort": 3,
    "component": "SelectWindow",
    "vo": cloneDeep(CommandVODefinition.SelectWindowVO),
  },
  // 设置窗口大小
  {
    "name": "cmdSetWindowSize",
    "command": "cmdSetWindowSize",
    "cnDesc": "设置窗口大小",
    "twDesc": "設置窗口大小",
    "enDesc": "set window size",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_PROXY,
    "viewType": "browserOperation",
    "atomicCommands": [
      AtomicCommandDefinition.setWindowSize,
    ],
    "sort": 4,
    "component": "SetWindowSize",
    "vo": cloneDeep(CommandVODefinition.SetWindowSizeVO),
  },
  // 内嵌网页
  {
    "name": "cmdSelectFrame",
    "command": "cmdSelectFrame",
    "cnDesc": "从当前 window 选择 frame(对 frame 标签起作用)",
    "twDesc": "從當前 window 選擇 frame(對 frame 標籤起作用)",
    "enDesc": "Select frame from current window (works with frame tag)",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_PROXY,
    "viewType": "browserOperation",
    "atomicCommands": [
      AtomicCommandDefinition.selectFrame,
    ],
    "sort": 5,
    "component": "SelectFrame",
    "vo": cloneDeep(CommandVODefinition.SelectFrameVO),
  },
];
export default browserOperation;
