import {COMMAND_TYPE_PROXY} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"

const inputOperation = [
  {
    "name": "cmdInput",
    "command": "cmdInput",
    "cnDesc": "对元素实现输入的操作",
    "twDesc": "對元素實現輸入的操作",
    "enDesc": "Implements an input operation on an element",
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_PROXY,
    "viewType": "inputOperation",
    "atomicCommands": [
      AtomicCommandDefinition.sendKeys,
      AtomicCommandDefinition.type,
      AtomicCommandDefinition.editContent
    ],
    "sort": 1,
    "component": "Input",
    "vo": cloneDeep(CommandVODefinition.InputVO),
  },
  {
    "name": "cmdFileUpload",
    "command": "cmdFileUpload",
    "cnDesc": "需先上传文件到文件管理中",
    "twDesc": "需先上傳文件到文件管理中",
    "enDesc": "Need upload file in file management center before you select",
    "popoverDesc" : true,
    "type": "MsUiCommand",
    "commandType": COMMAND_TYPE_PROXY,
    "viewType": "inputOperation",
    "atomicCommands": [
      AtomicCommandDefinition.fileUpload
    ],
    "sort": 2,
    "component": "Input",
    "vo": cloneDeep(CommandVODefinition.InputVO),
  },
];
export default inputOperation;
