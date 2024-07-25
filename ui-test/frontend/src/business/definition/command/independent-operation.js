import {COMMAND_TYPE_PROXY} from "./command-type";
import AtomicCommandDefinition from "./atomic-command-definition"
import CommandVODefinition from "@/business/definition/command/vo/command-vo"
import { cloneDeep } from "lodash-es"

/**
 * 独立指令 不属于任何右边的分类
 * @type {({cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}|{cnDesc: string, commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}], name: string, viewType: string, vo: DropDownBoxVO, sort: number, type: string, command: string}|{cnDesc: string, commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}], name: string, viewType: string, vo: SetItemVO, sort: number, type: string, command: string}|{cnDesc: string, commandType: string, component: string, atomicCommands: ({cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}|{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, command: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string})[], name: string, viewType: string, vo: WaitElementVO, sort: number, type: string, command: string})[]}
 */
const independentOperation = [
  {
    "name": "cmdScript",
    "command": "cmdScript",
    "type": "MsUiCommand",
    "viewType": "independentOperation",
    "cnDesc": "前后置指令的运行脚本",
    "twDesc": "前後置指令的運行腳本",
    "enDesc": "Running scripts for pre- and post-commands",
    "commandType": COMMAND_TYPE_PROXY,
    "sort": 1,
    "atomicCommands": [
      AtomicCommandDefinition.runScript,
      AtomicCommandDefinition.executeScript,
      AtomicCommandDefinition.executeAsyncScript,
    ],
    "component": "UiScriptComponent",
    "vo": cloneDeep(CommandVODefinition.cmdScript)
  },
];
export default independentOperation;
