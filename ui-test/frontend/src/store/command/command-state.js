/**
 * 指令相关 state 存储
 */
export default {
    id: "commandStore",
    state: () => (
        {
            selectCommand: null,
            //存储变更前的场景，用于关闭窗口时判断是否做了修改来提示用户是否要保存
            scenarioMap: new Map(),
            selectStep: null,
            currentScenario: null,
            refreshUiScenario: null,
            refreshUiCustom: null,
            librarySelectElement: null,
            useEnvironment: null,
            uiElementLibraryElements: null,
            uiElementLibraryModuleIds: null,
            scenarioJmxs: null,
            test: null,
            testCaseDefaultValue: null,
        }
    ),
    persist: true,
    getters: {
        commandStore: (state) => state.$state,
    }
}
