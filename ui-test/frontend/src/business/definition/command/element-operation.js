import {COMMAND_TYPE_PROXY} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"

const elementOperation = [
  AtomicCommandDefinition.submit,
  {
    "name": "cmdDropdownBox",
    "command": "cmdDropdownBox",
    "type": "MsUiCommand",
    "viewType": "elementOperation",
    "cnDesc": "对下拉选项进行操作，可实现单选，多选，以及取消选择的操作",
    "twDesc": "對下拉選項進行操作，可實現單選，多選，以及取消選擇的操作",
    "enDesc": "Operates on drop-down options, enabling single-selection, multiple-selection, and de-selection operations",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.select,
      AtomicCommandDefinition.addSelection,
      AtomicCommandDefinition.removeSelection,
    ],
    "component": "DropdownBox",
    "vo": cloneDeep(CommandVODefinition.DropdownBoxVO)
  },
  {
    "name": "cmdSetItem",
    "command": "cmdSetItem",
    "type": "MsUiCommand",
    "viewType": "elementOperation",
    "cnDesc": "用于设置 checkbox/radio 的状态",
    "twDesc": "用於設置 checkbox/radio 的狀態",
    "enDesc": "Used to set the state of checkbox/radio",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.check,
      AtomicCommandDefinition.uncheck,
    ],
    "vo": cloneDeep(CommandVODefinition.SetItemVO),
    "component": "SetItem"
  },
  {
    "name": "cmdWaitElement",
    "command": "cmdWaitElement",
    "type": "MsUiCommand",
    "viewType": "elementOperation",
    "cnDesc": "对页面对象执行指定的等待操作，默认等待超时15000ms",
    "twDesc": "對頁面對象執行指定的等待操作，默認等待超時15000ms",
    "enDesc": "Perform the specified wait operation on the page object, the default wait timeout is 15000ms",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.waitForElementNotVisible,
      AtomicCommandDefinition.waitForText,
      AtomicCommandDefinition.waitForElementEditable,
      AtomicCommandDefinition.waitForElementPresent,
      AtomicCommandDefinition.waitForElementVisible,
      AtomicCommandDefinition.waitForElementNotPresent,
      AtomicCommandDefinition.waitForElementNotEditable,
    ],
    "vo": cloneDeep(CommandVODefinition.WaitElementVO),
    "component": "WaitElement"
  }
];
export default elementOperation;
