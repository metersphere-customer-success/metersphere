/**
 * 替换迁移后的 class
 * @param msTestElement
 */
import {getUUID} from "metersphere-frontend/src/utils";

export function adaptToJackson(msTestElement) {
    if (msTestElement) {
        if (msTestElement.scenarioDefinition) {
            if (msTestElement.scenarioDefinition.hashTree) {
                let hashTree = msTestElement.scenarioDefinition.hashTree;
                for (let i = 0; i < hashTree.length; i++) {
                    recursion(hashTree[i]);
                }
            }
        }
        if (msTestElement.hashTree) {
            if (msTestElement.hashTree) {
                let hashTree = msTestElement.hashTree;
                for (let i = 0; i < hashTree.length; i++) {
                    recursion(hashTree[i]);
                }
            }
        }
        let tmpStr = JSON.stringify(msTestElement);
        tmpStr = tmpStr.replace(/io.metersphere.xpack.ui.hashtree/g, "io.metersphere.hashtree");
        tmpStr = tmpStr.replace(/io.metersphere.api.dto.definition.request/g, "io.metersphere.hashtree");
        return JSON.parse(tmpStr);
    }
    return msTestElement;
}

function recursion(command) {
    if (command.targetVO) {
        if (!command.targetVO.type) {
            command.targetVO.type = "Atomic";
        }
        if (!command.targetVO.clazzName) {
            command.targetVO.clazzName = "io.metersphere.vo.UiAtomicCommandVO";
        }
    }
    if (command.valueVO) {
        if (!command.valueVO.type) {
            command.valueVO.type = "Atomic";
        }
        if (!command.valueVO.clazzName) {
            command.valueVO.clazzName = "io.metersphere.vo.UiAtomicCommandVO";
        }
    }
    //生成 jackson 解决循环依赖反序列化的问题
    command['@json_id'] = getUUID();

    if (command.postCommands) {
        for (let i = 0; i < command.postCommands.length; i++) {
            recursion(command.postCommands[i]);
        }
    }
    if (command.preCommands) {
        for (let i = 0; i < command.preCommands.length; i++) {
            recursion(command.preCommands[i]);
        }
    }
    if (command.hashTree) {
        for (let i = 0; i < command.hashTree.length; i++) {
            recursion(command.hashTree[i]);
        }
    }
    if (command.appendCommands) {
        for (let i = 0; i < command.appendCommands.length; i++) {
            recursion(command.appendCommands[i]);
        }
    }
    if (command.outputVariables) {
        if (typeof (command.outputVariables) !== "string") {
            try {
                command.outputVariables = JSON.stringify(command.outputVariables);
            } catch (e) {
                command.outputVariables = "";
            }
        }
    }
}

export function jsonToMap(jsonStr) {
    let obj = JSON.parse(jsonStr);
    let strMap = new Map();
    for (let k of Object.keys(obj)) {
        strMap.set(k, obj[k]);
    }
    return strMap;
}
