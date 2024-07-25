import elementOperation from "./element-operation"
import mouseOperation from "./mouse-operation"
import programController from "./program-controller"
import inputOperation from "./input-operation"
import browserOperation from "./browser-operation"
import dialogOperation from "./dialog-operation";
import validation from "./validation";
import extraction from "./extraction";
import atomicCommandDefinition from "./atomic-command-definition";
import independentOperation from "./independent-operation";
import { reduce } from "lodash-es";
import i18n from "@/i18n/lang/i18n";

/**
 * :todo 视觉层面的指令分类
 * @type {{inputOperation: [{commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}], name: string, sort: number, type: string}], elementOperation: [{cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, {commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}], name: string, viewType: string, sort: number, type: string}, {commandType: string, component: string, atomicCommands, name: string, viewType: string, sort: number, type: string}, {commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}], name: string, viewType: string, sort: number, type: string}], programController: ({commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, sort: number, type: string})[]}}
 */
export let groupCommandDefinition = {
    elementOperation,
    mouseOperation,
    programController,
    inputOperation,
    browserOperation,
    dialogOperation,
    validation,
    extraction,
    independentOperation,
}

function adaptES5() {
    let complexCmdList = [
        ...elementOperation,
        ...mouseOperation,
        ...programController,
        ...inputOperation,
        ...browserOperation,
        ...dialogOperation,
        ...validation,
        ...extraction,
        ...independentOperation,
    ];
    return reduce(complexCmdList, function (result, value, key) {
        if (!result[value.name]) {
            result[value.name] = value;
        }
        return result;
    }, {});
}

/**
 * 复合指令定义
 * 名称 : 对象
 * @type {{[p: string]: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}|{commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}], name: string, viewType: string, sort: number, type: string}|{commandType: string, component: string, atomicCommands, name: string, viewType: string, sort: number, type: string}|{commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}], name: string, viewType: string, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, sort: number, type: string}|{commandType: string, component: string, endCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, name: string, startCommand: {cnType: string, cnDesc: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string}, sort: number, type: string}|{commandType: string, component: string, atomicCommands: [{cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}, {cnType: string, cnDesc: string, valueCNName: string, description: string, twName: string, twDesc: string, sort: number, type: string, targetTWName: string, target: string, webdriverScript: string, component: string, cnName: string, targetCNName: string, name: string, viewType: string, twType: string, cypressScript: string, value: string, valueTWName: string}], name: string, sort: number, type: string}}}
 */
export let commandDefinitionMap = adaptES5();

/**
 * 根据指令的名称 找到对应指令的分类
 * 分别从原子指令定义和复合指令定义找
 * @param commandName
 * @returns {string|*}
 * @constructor
 */
export function FindCommandUiGroupByName(commandName) {
    return commandDefinitionMap[commandName] ?
        commandDefinitionMap[commandName].viewType :
        atomicCommandDefinition[commandName] ?
            atomicCommandDefinition[commandName].viewType : ""
}


/**
 * 根据指令的名称 找到对应指令的组件
 * 分别从原子指令定义和复合指令定义找
 * @param commandName
 * @returns {string|*}
 * @constructor
 */
export function GetComponentByName(commandName) {
    if (!commandName) {
        return "";
    }
    return atomicCommandDefinition[commandName] ?
        atomicCommandDefinition[commandName].component : commandDefinitionMap[commandName].component;
}


export function getCommandParamCNName(commandName, paramName) {
    if (atomicCommandDefinition[commandName]) {
        if (!atomicCommandDefinition[commandName][paramName]) {
            return i18n.t("ui.operation");
        }
        let prefix = "";
        if (i18n.locale == "zh-CN" || i18n.locale == "zh_CN") {
            prefix = "CNName";
        } else if (i18n.locale == "zh-TW" || i18n.locale == "zh_TW") {
            prefix = "TWName";
        } else {
            console.log(atomicCommandDefinition[commandName][paramName + prefix])
            return atomicCommandDefinition[commandName][paramName + prefix].substr(0, 1).toUpperCase() + atomicCommandDefinition[commandName][paramName + prefix].substr(1);
        }
        return atomicCommandDefinition[commandName][paramName + prefix];
    }

    return this.$t("ui." + atomicCommandDefinition[commandName][paramName]) + "：";
}

/**
 * 所有复合 ui 指令的定义数组
 */
export default [
    ...elementOperation,
    ...mouseOperation,
    ...programController,
    ...inputOperation,
    ...browserOperation,
    ...dialogOperation,
    ...validation,
    ...extraction,
    ...independentOperation,
];
