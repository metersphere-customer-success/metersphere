import {
  COMMAND_TYPE_PROXY,
  COMMAND_TYPE_COMBINATION_PROXY
} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"

/**
 * 数据提取
 */
const extraction = [
  {
    "name": "cmdExtraction",// 提取
    "command": "cmdExtraction",
    "type": "MsUiCommand",
    "viewType": "dataExtraction",
    "cnDesc": "提取页面的数据，并将其存放在变量中，在后续的请求中使用变量",
    "commandType": COMMAND_TYPE_COMBINATION_PROXY,
    "sort": 1,
  },
  {
    "name": "cmdExtractWindow",// 提取窗口信息
    "command": "cmdExtractWindow",
    "type": "MsUiCommand",
    "viewType": "dataExtraction",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.storeWindowHandle,
      AtomicCommandDefinition.storeTitle,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdExtractWindowVO)
  },
  {
    "name": "cmdExtractElement",// 提取元素信息
    "command": "cmdExtractElement",
    "type": "MsUiCommand",
    "viewType": "dataExtraction",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.store,
      AtomicCommandDefinition.storeAttribute,
      AtomicCommandDefinition.storeCssAttribute,
      AtomicCommandDefinition.storeValue,
      AtomicCommandDefinition.storeText,
      AtomicCommandDefinition.storeXpathCount,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdExtractElementVO)
  },
];
export default extraction;
