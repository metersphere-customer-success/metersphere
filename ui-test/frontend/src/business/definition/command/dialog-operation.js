import {COMMAND_TYPE_PROXY} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"

const dialogOperation = [
  {
    "name": "cmdDialog",
    "command": "cmdDialog",
    "type": "MsUiCommand",
    "viewType": "dialogOperation",
    "cnDesc": "对无法进行元素定位的弹窗实现的操作 (注：弹框事件不会产生截图)",
    "twDesc": "對無法進行元素定位的彈窗實現的操作 (注：彈框事件不會產生截圖)",
    "enDesc": "The operation implemented on the pop-up window that cannot be positioned on the element (Note: the pop-up event will not generate a screenshot)",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.answerOnNextPrompt,
      AtomicCommandDefinition.chooseOkOnNextConfirmation,
      AtomicCommandDefinition.webdriverChooseOkOnVisibleConfirmation,
      AtomicCommandDefinition.webdriverAnswerOnVisiblePrompt,
      AtomicCommandDefinition.webdriverChooseCancelOnVisibleConfirmation,
      AtomicCommandDefinition.chooseCancelOnNextConfirmation,
      AtomicCommandDefinition.chooseCancelOnNextPrompt,
      AtomicCommandDefinition.webdriverChooseCancelOnVisiblePrompt,
    ],
    "vo": cloneDeep(CommandVODefinition.DialogVO),
    "component": "Dialog"
  }
];
export default dialogOperation;
