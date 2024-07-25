import {UiScenario} from "./ui-automation-model";
import {createComponent} from "@/business/definition/jmeter/components";
import commandVo from "@/business/definition/command/vo/command-vo";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";
import {commandDefinitionMap} from "@/business/definition/command/all-command-definition"
import { cloneDeep } from "lodash-es";
import {$error} from "metersphere-frontend/src/plugins/message";
import CommandConfig from "@/business/definition/command/vo/command-config"
import {fileDownloadPost, get, post} from "metersphere-frontend/src/plugins/request"
import {adaptToJackson} from "@/common/js/convert";
import {uuid} from "metersphere-frontend/src/model/ApiTestModel";

export function getUiFabButtons(this_) {
    let buttons = [
        {
            title: this_.$t('ui.browser_operation'),
            // show: this_.showButton("MsBrowserCommand"),
            titleColor: "#015478",
            titleBgColor: "#E6EEF2",
            icon: "colorize",
            click: () => {
                this_.addCommand('browserOperation')
            }
        },
        {
            title: this_.$t('ui.dialog_operation'),
            // show: this_.showButton("MsBrowserCommand"),
            titleColor: "#015478",
            titleBgColor: "#E6EEF2",
            icon: "colorize",
            click: () => {
                this_.addCommand('dialogOperation')
            }
        },
        {
            title: this_.$t('ui.element_operation'),
            // show: this_.showButton("MsElementCommand"),
            titleColor: "#783887",
            titleBgColor: "#F2ECF3",
            icon: "skip_next",
            click: () => {
                this_.addCommand('elementOperation')
            }
        },
        {
            title: this_.$t('ui.mouse_operation'),
            // show: this_.showButton("MsMouseCommand"),
            titleColor: "#B8741A",
            titleBgColor: "#F9F1EA",
            icon: "skip_previous",
            click: () => {
                this_.addCommand('mouseOperation')
            }
        },
        {
            title: this_.$t('ui.input_operation'),
            // show: this_.showButton("MsKeyboardCommand"),
            titleColor: "#1483F6",
            titleBgColor: "#F2ECF3",
            icon: "skip_next",
            click: () => {
                this_.addCommand('inputOperation')
            }
        },
        {
            title: this_.$t('ui.program_controller'),
            // show: this_.showButton("MsProcessCommand"),
            titleColor: "#7B4D12",
            titleBgColor: "#F1EEE9",
            icon: "code",
            click: () => {
                this_.addCommand('programController')
            }
        },
        // {
        //   title: this_.$t('数据提取'),
        //   // show: this_.showButton("MsExtractionCommand"),
        //   titleColor: "#E6A23C",
        //   titleBgColor: "#FCF6EE",
        //   icon: "code",
        //   click: () => {
        //     this_.addCommand('dataExtraction')
        //   }
        // },

        {
            title: this_.$t('ui.import_by_list_label'),
            // show: this_.showButton("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler"),
            titleColor: "#F56C6C",
            titleBgColor: "#FCF1F1",
            icon: "api",
            click: () => {
                this_.addCommand('UiScenario')
            }
        }
    ];
    return buttons;
}

/**
 *
 * @param currentScenario
 * @param runMode 运行模式为本地调试时永远都启用 gui
 * @returns {any}
 */
export function getUiRunDebugData(currentScenario, runMode) {
    let scenarioStep = currentScenario.scenarioDefinition;
    if (scenarioStep) {
        let debugData = {
            id: currentScenario.id,
            name: currentScenario.name,
            type: "UiScenario",
            browser: scenarioStep.browser,
            variables: scenarioStep.variables,
            headlessEnabled: scenarioStep.headlessEnabled,
            referenced: 'Created',
            onSampleError: scenarioStep.onSampleError,
            hashTree: scenarioStep.hashTree,
            scenarioConfig: scenarioStep.scenarioConfig,
            environmentJson: currentScenario.environmentJson,
        };
        debugData = adaptToJackson(debugData);
        return debugData;
    }
}

export function createCommand(cmdName, definition) {
    let newCmd = createComponent('MsUiCommand');

    if (cmdName && atomicCommandDefinition[cmdName]) {
        // 如果是原子指令复制下基本信息
        newCmd = Object.assign(newCmd, cloneDeep(atomicCommandDefinition[cmdName]));
    } else if (definition) {
        // 传入定义信息
        newCmd = Object.assign(newCmd, cloneDeep(definition));
    } else {
        //找复合指令
        newCmd = Object.assign(newCmd, cloneDeep((commandDefinitionMap[cmdName])));
    }

    newCmd.id = uuid();
    newCmd.resourceId = uuid();

    let voDefinition = commandVo[newCmd.name || cmdName];
    if (voDefinition) {
        // 非原子指令初始化vo
        newCmd.vo = {};
        newCmd.vo = cloneDeep(voDefinition);

        if (voDefinition.appendCommands) {
            // 复合指令如 while，需要追加间隔时间
            newCmd.appendCommands = [];
            voDefinition.appendCommands.forEach(name => {
                newCmd.appendCommands.push(createCommand(name))
            });
        }
    }

    newCmd.target = null;
    newCmd.value = null;
    newCmd.commandConfig = cloneDeep(CommandConfig);
    return newCmd;
}

export function createCommandByDefinition(definition) {
    return createCommand(null, definition);
}

export function validateFirstCommand(hashTree) {
    if (!hashTree || hashTree.length < 1) {
        throwError("步骤不能为空");
    }
    let first = null;
    let pre = undefined;
    for (let i = 0; i < hashTree.length; i++) {
        // 记录指令调试的前置步骤信息
        if (hashTree[i].enable && hashTree[i].debuggerDetail && hashTree[i].debuggerDetail.type === "PRE") {
            pre = hashTree[i];
        }
        if (hashTree[i].enable && (!hashTree[i].debuggerDetail || hashTree[i].debuggerDetail.type === "MAIN")) {
            first = hashTree[i];
            if (first && first.hashTree && first.hashTree.length > 0) {
                break;
            }
            if (first && first.type == "MsUiCommand") {
                break;
            }
        }
        if (hashTree[i].enable && hashTree[i].debuggerDetail && hashTree[i].debuggerDetail.type === "POST") {
            first = hashTree[i];
        }
    }

    if (!first) {
        throwError("没有开启的步骤");
    }
    if (first.command === 'cmdTimes' && first.viewType === "programController" && first.hashTree.length > 0) {
        validateFirstCommand(first.hashTree)
    }
    if (first.type === 'UiScenario' || first.type === 'scenario') {
        /*if (pre && pre.hashTree && pre.hashTree.length > 0) {
            if (validateFirstCommand(pre.hashTree)) {
                return true;
            }
        }*/
        return validateFirstCommand(first.hashTree);
    } else if (first.command !== 'cmdOpen' && first.viewType !== "programController" && first.type !== 'customCommand' ) {
        throwError("第一个步骤必须是打开网页");
    }
    return true
}

export function validateProgramController(hashTree) {
    let pre = null;
    hashTree.forEach(item => {
        if (!item.enable) {
            //流程控制被禁用了 不在校验
            return;
        }
        if (item.command === "cmdElseIf" && (!pre || (pre.command !== "cmdIf" && pre.command !== "cmdElseIf"))) {
            throwError("ElseIf 必须在 If 之后");
        } else if (item.command === "cmdElse" && (!pre || (pre.command !== "cmdIf" && pre.command !== "cmdElseIf"))) {
            throwError("Else 必须在 If 或 ElseIf 之后");
        }
        pre = item;
        if (item.hashTree) {
            validateProgramController(item.hashTree);
        }
    });
}

function throwError(tip) {
    $error(tip);
    throw new Error(tip);
}

export function commandFromValidate(vueObj, formName) {
    let r = true;
    let ref = formName ? vueObj.$refs[formName] : vueObj.$refs['cmdForm'];
    if (ref) {
        ref.validate((valid) => {
            if (!valid) {
                r = false;
            }
        });
    }
    return r;
}

export function commandFromAndLocatorFromValidate(vueObj, formName, locatorFrom) {
    let locatorRef = locatorFrom ? vueObj.$refs[locatorFrom] : vueObj.$refs['locatorForm'];
    if (locatorRef) {
        let locatorRes = locatorRef.validate();
        let cmdRes = commandFromValidate(vueObj, formName);
        return locatorRes && cmdRes;
    } else {
        return commandFromValidate(vueObj, formName);
    }
}

export function getUiAutomationList(currentPage, pageSize, param) {
    return post("/ui/automation/list/" + currentPage + "/" + pageSize, param);
}

export function downloadAutomationExport(param, fileName) {
    fileDownloadPost(
        '/ui/automation/export',
        param
        , fileName)
}

export function downloadElementExport(param, fileName) {
    return fileDownloadPost(
        '/ui/element/export',
        param
        , fileName)
}

export function downloadScreenshotExport(param, fileName) {
    fileDownloadPost(
        '/ui/automation/download/screenshot',
        param
        , fileName)
}

export function getAautomationListAllTrash(param) {
    return post("/ui/automation/list/all/trash", param);
}

export function getUiScenarioModuleList(projectId) {
    return get("/ui/scenario/module/list/" + projectId);
}

export function getUiAutomationListWithIdsAll(param) {
    return post("/ui/automation/listWithIds/all", param);
}

export function getUiScenarioModuleListPlan(planId) {
    return get("/ui/scenario/module/list/plan/" + planId);
}

export function getUiAutomation(resourceId) {
    return get("/ui/automation/get/" + resourceId);
}

export function getProject(id) {
    return get("/project/get/" + id)
}

export function checkBeforeDelete(param) {
    return post("/ui/automation/checkBeforeDelete", param)
}

export function removeToGcByBatch(param) {
    if (param.ids.length > 1) {
        return post("/ui/automation/removeToGcByBatch", param)
    } else {
        return post("/ui/automation/removeToGc", param.ids)
    }
}


export function editAutomationBatchEdit(param) {
    return post("/ui/automation/batch/edit", param)
}

export function editAutomationBatch(url, param) {
    return post(url, param)
}

export function createSchedule(url, params) {
    return post(url, params);
}

export function getUiAutomationVersions(id) {
    return get("/ui/automation/versions/" + id);
}

export function runBatch(param) {
    return post("/ui/automation/run/batch", param);
}

export function getProjectVersions(projectId) {
    return get('/project/version/get-project-versions/' + projectId);
}

export function editOrder(param) {
    return post('/ui/automation/edit/order', param);
}

export function getScheduleByIdAndType(scheduleResourceID, taskType) {
    let url = '/ui/automation/schedule/get/' + scheduleResourceID + '/' + taskType;
    return get(url);
}

export function getNoticeTasks(testId) {
    return get(`/notice/search/message/${testId}`);
}

export function getOwnerProjects() {
    return get('/project/getOwnerProjects');
}

export function getUiScenarioEnv(param) {
    return post('/ui/automation/scenario-env', param);
}

export function getScheduleDetail(scenarioIds) {
    let url = '/ui/automation/scenario/schedule';
    return post(url, scenarioIds);
}

export function scheduleUpdate(param) {
    let url = '/ui/automation/schedule/status/update';
    return post(url, param);
}

export default class UiAutomation {
}
