import {
  COMMAND_TYPE_PROXY,
  COMMAND_TYPE_COMBINATION_PROXY
} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"
/**
 * 断言操作，实际上不作为指令展示在前端。只作为后置展示在后置操作，但是实际上后端处理为一个通常指令
 * 硬断言，如 assertTitle 断言失败会终止测试流程继续执行，
 * 软断言， 如 verifyTitle 断言失败不会终止测试流程继续执行
 * @type {({cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}|{commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}], name: string, viewType: string, vo: DropDownBoxVO, sort: number, type: string}|{commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}], name: string, viewType: string, vo: SetItemVO, sort: number, type: string}|{commandType: string, component: string, atomicCommands: ({cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string})[], name: string, viewType: string, vo: WaitElementVO, sort: number, type: string})[]}
 */
const validation = [
  {
    "name": "cmdValidation",//断言
    "command": "cmdValidation",//断言
    "type": "MsUiCommand",
    "viewType": "validation",
    "commandType": COMMAND_TYPE_COMBINATION_PROXY,
    "sort": 1,
  },
  {
    "name": "cmdValidateTitle",//标题
    "command": "cmdValidateTitle",//标题
    "type": "MsUiCommand",
    "viewType": "validation",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.assertTitle,
      AtomicCommandDefinition.verifyTitle,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdValidateTitleVO)
  },
  {
    "name": "cmdValidateElement",//元素断言
    "command": "cmdValidateElement",//元素断言
    "type": "MsUiCommand",
    "viewType": "validation",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 3,
    "atomicCommands": [
      AtomicCommandDefinition.assertChecked,
      AtomicCommandDefinition.verifyChecked,
      AtomicCommandDefinition.assertNotChecked,
      AtomicCommandDefinition.verifyNotChecked,
      AtomicCommandDefinition.assertEditable,
      AtomicCommandDefinition.verifyEditable,
      AtomicCommandDefinition.assertNotEditable,
      AtomicCommandDefinition.verifyNotEditable,
      AtomicCommandDefinition.assertElementPresent,
      AtomicCommandDefinition.verifyElementPresent,
      AtomicCommandDefinition.assertElementNotPresent,
      AtomicCommandDefinition.verifyElementNotPresent,
      AtomicCommandDefinition.assertText,
      AtomicCommandDefinition.verifyText,
      AtomicCommandDefinition.assertNotText,
      AtomicCommandDefinition.verifyNotText,
      AtomicCommandDefinition.assertValue,
      AtomicCommandDefinition.verifyValue,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdValidateElementVO)
  },
  {
    "name": "cmdValidateDropdown",//下拉框
    "command": "cmdValidateDropdown",//下拉框
    "type": "MsUiCommand",
    "viewType": "validation",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 2,
    "atomicCommands": [
      AtomicCommandDefinition.assertSelectedValue,
      AtomicCommandDefinition.verifySelectedValue,
      AtomicCommandDefinition.assertSelectedLabel,
      AtomicCommandDefinition.verifySelectedLabel,
      AtomicCommandDefinition.assertNotSelectedValue,
      AtomicCommandDefinition.verifyNotSelectedValue,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdValidateDropdownVO)
  },
  {
    "name": "cmdValidateValue",//断言值
    "command": "cmdValidateValue",//断言值
    "type": "MsUiCommand",
    "viewType": "validation",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 5,
    "atomicCommands": [
      AtomicCommandDefinition.assert,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdValidateValueVO)
  },
  {
    "name": "cmdValidateText",//弹窗文本
    "command": "cmdValidateText",//弹窗文本
    "type": "MsUiCommand",
    "viewType": "validation",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 4,
    "atomicCommands": [
      AtomicCommandDefinition.assertConfirmation,
      AtomicCommandDefinition.assertPrompt,
    ],
    "vo": cloneDeep(CommandVODefinition.cmdValidateTextVO)
  }
];
export default validation;
