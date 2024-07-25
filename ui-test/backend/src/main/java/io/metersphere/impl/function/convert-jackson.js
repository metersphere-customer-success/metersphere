function adaptToJackson(msTestElement) {
    if (msTestElement) {
        msTestElement = JSON.parse(msTestElement);
        if (msTestElement.scenarioDefinition) {
            if (msTestElement.scenarioDefinition.hashTree) {
                var hashTree = msTestElement.scenarioDefinition.hashTree;
                for (var i = 0; i < hashTree.length; i++) {
                    recursion(hashTree[i]);
                }
            }
        }
        if (msTestElement.hashTree) {
            if (msTestElement.hashTree) {
                var hashTree = msTestElement.hashTree;
                for (var i = 0; i < hashTree.length; i++) {
                    recursion(hashTree[i]);
                }
            }
        }
        var tmpStr = JSON.stringify(msTestElement);
        tmpStr = tmpStr.replace(/io.metersphere.xpack.ui.hashtree/g, "io.metersphere.hashtree");
        tmpStr = tmpStr.replace(/io.metersphere.api.dto.definition.request/g, "io.metersphere.hashtree");
        tmpStr = tmpStr.replace(/io.metersphere.xpack.ui.vo/g, "io.metersphere.vo");
        return tmpStr;
    }
    return msTestElement;
}

function getUUID() {
    function S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }

    return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
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

    if (command.vo) {
        if (!command.vo.clazzName) {
            command.vo.clazzName = "io.metersphere.vo.UiCommandBaseVo";
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

    //生成 jackson 解决循环依赖反序列化的问题
    command['@json_id'] = getUUID();

    if (command.postCommands) {
        for (var i = 0; i < command.postCommands.length; i++) {
            recursion(command.postCommands[i]);
        }
    }
    if (command.preCommands) {
        for (var i = 0; i < command.preCommands.length; i++) {
            recursion(command.preCommands[i]);
        }
    }
    if (command.hashTree) {
        for (var i = 0; i < command.hashTree.length; i++) {
            recursion(command.hashTree[i]);
        }
    }
    if (command.appendCommands) {
        for (var i = 0; i < command.appendCommands.length; i++) {
            recursion(command.appendCommands[i]);
        }
    }
}