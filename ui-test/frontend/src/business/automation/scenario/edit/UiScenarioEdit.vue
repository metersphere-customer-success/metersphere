<template>
  <ms-container v-loading="loading">
    <ui-scenario-edit-aside
      v-loading="loadingDefinition"
      v-model="scenarioData"
      :module-options="moduleOptions"
      @changeCurrentCommand="changeCurrentCommand"
      @openScenario="openScenario"
      @setProjectEnvMap="setProjectEnvMap"
      ref="aside"
    />

    <ms-main-container
      style="margin: 6px; height: calc(100vh - 145px); overflow: hidden"
    >
      <scenario-edit-bar
        :current-scenario="scenarioData"
        @runDebug="jumperToStep"
        @open-variable="openVariable"
        class="ui-edit-bar"
        @setProjectEnvMap="setProjectEnvMap"
        :init-project-env-map="projectEnvMap"
        @save="save"
        ref="uiEditBar"
      />

      <div class="card-content" @click.stop="handleFabBtnStatus(false)">
        <div v-show="currentCommand.command">
          <ms-form-divider :title="$t('ui.scenario_steps_label')" />
          <ui-scenario-command
            class="scenario-command"
            :key="currentCommand.id"
            ref="cmd"
            v-model="currentCommand"
            @changeCurrentCommand="changeCurrentCommand"
          />
        </div>
        <div v-if="currentCommand.command"></div>
        <div
          v-else-if="
            !currentCommand.command &&
            currentCommand.type === 'customCommand' &&
            (!currentCommand.debuggerDetail ||
              (currentCommand.debuggerDetail &&
                !currentCommand.debuggerDetail.disableShowDetailPane))
          "
        >
          <!-- Since v2.4 -->
          <UiCustomCommandPane
            :currentCommand="currentCommand"
            @changeInnerParam="changeInnerParam"
            @changeOutputParam="changeOutputParam"
          ></UiCustomCommandPane>
        </div>

        <div class="pre-opt-tip" style="padding-top: 10px" v-else>
          <span>
            {{ $t("api_test.scenario.step_info") }}
          </span>
        </div>

        <ms-form-divider
          :title="$t('schema.adv_setting')"
          class="mt"
          v-show="showAdvance"
        />

        <ui-scenario-advance
          v-model="scenarioData"
          v-show="showAdvance"
          :scenario-index="currentCommand.index"
          :key="currentCommand.index"
        />

        <ui-scenario-relevance
          ref="scenarioImportDialog"
          @save="addScenario"
          :scenarioType="currentType"
        ></ui-scenario-relevance>
      </div>

      <ui-scenario-edit-fab-btn
        v-model="scenarioData"
        @sort="sort"
        @scenarioImport="scenarioImport"
        @changeCurrentCommand="changeCurrentCommand"
        @listenFabBtnStatusChange="handleFabBtnStatus"
        :immediately="immediately"
      />
    </ms-main-container>
  </ms-container>
</template>

<script>
import UiScenarioEditAside from "./UiScenarioEditAside";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import { API_STATUS } from "@/api/JsonData";
import { STEP, TYPE_TO_C } from "@/api/Setting";
import ScenarioEditBar from "@/business/automation/scenario/edit/UiScenarioEditBar";
import UiScenarioEditFabBtn from "@/business/automation/scenario/edit/UiScenarioEditFabBtn";
import UiScenarioCommand from "@/business/automation/scenario/edit/command/UiScenarioCommand";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import UiScenarioAdvance from "@/business/automation/scenario/edit/advance/UiScenarioAdvance";
import { saveScenario } from "@/api/api-automation";
import { cloneDeep } from "lodash-es";
import UiScenarioRelevance from "@/business/automation/scenario/edit/UiScenarioEditRelevance";
import { getIndexScenarioMap } from "@/business/util/commonUtils";
import MsDebugRun from "@/business/automation/scenario/DebugRun";
import { useCommandStore } from "@/store";
import { adaptToJackson, jsonToMap } from "@/common/js/convert";
import UiCustomCommandPane from "@/business/automation/custom-commands/edit/UiCustomCommandPane";
import { ENV_TYPE } from "@/api/constants";
import { getUUID, strMapToObj } from "metersphere-frontend/src/utils";
import {hasPermissionForProjectId} from "metersphere-frontend/src/utils/permission";

const commandStore = new useCommandStore();
export default {
  name: "UiScenarioEdit",
  components: {
    MsDebugRun,
    UiScenarioAdvance,
    UiScenarioEditFabBtn,
    ScenarioEditBar,
    MsContainer,
    MsMainContainer,
    UiScenarioEditAside,
    UiScenarioCommand,
    MsFormDivider,
    UiScenarioRelevance,
    UiCustomCommandPane,
  },
  props: {
    currentType: String,
    moduleOptions: Array,
    type: String,
    currentScenario: {
      type: Object,
      default() {
        return {};
      },
    },
    customNum: {
      type: Boolean,
      default: false,
    },
    currentTabId:String
  },
  data() {
    return {
      immediately: true,
      loadingDefinition: false,
      onSampleError: true,
      newOnSampleError: true,
      value: API_STATUS[0].id,
      options: API_STATUS,
      scenario: {},
      loading: false,
      customizeVisible: false,
      isBtnHide: false,
      debugVisible: false,
      operatingElements: [],
      selectedTreeNode: undefined,
      selectedNode: undefined,
      expandedNode: [],
      path: "/ui/automation/update",
      enableCookieShare: false,
      newEnableCookieShare: false,
      globalOptions: {
        spacing: 30,
      },
      response: {},
      projectIds: new Set(),
      projectEnvMap: new Map(),
      newProjectEnvMap: new Map(),
      projectList: [],
      drawer: false,
      isFullUrl: true,
      expandedStatus: false,
      stepEnable: true,
      envResult: {
        loading: false,
      },
      debug: false,
      debugLoading: false,
      reloadDebug: "",
      stopDebug: "",
      isTop: false,
      stepSize: 0,
      stepFilter: new STEP(),
      plugins: [],
      dialogVisible: false,
      currentItem: {},
      currentCommand: {},
      scenarioData:{}
    };
  },
  mounted() {
    if (!(commandStore.scenarioMap instanceof Map)) {
      commandStore.scenarioMap = new Map();
    }
    if (this.scenarioData.copy || this.scenarioData.type === "add") {
      this.path = "/ui/automation/create";
      if (this.scenarioData.env) {
        this.projectEnvMap = jsonToMap(this.scenarioData.env);
      }
    }
    window.addEventListener("keydown", this.handleEvent);
  },
  created() {
    this.scenarioData = this.currentScenario;
    if (this.scenarioData.moduleId === "default-module") {
      this.scenarioData.moduleId = this.moduleOptions[0].id;
    }
    if (this.scenarioData.env) {
      this.projectEnvMap = jsonToMap(this.scenarioData.env);
    }
    this.$EventBus.$on(
      "autoChangeCurrentCommand",
      this.autoChangeCurrentCommand
    );
    this.$EventBus.$on("changeFabBtnStatus", this.handleFabBtnStatus);
  },
  beforeDestroy() {
    this.$EventBus.$off(
      "autoChangeCurrentCommand",
      this.autoChangeCurrentCommand
    );
    this.$EventBus.$off("changeFabBtnStatus", this.handleFabBtnStatus);
    window.removeEventListener("keydown", this.handleEvent);
  },
  provide() {
    return {
      checkBeforceSave: this.checkBeforceSave,
    };
  },
  computed: {
    showAdvance() {
      return (
        this.currentCommand.command &&
        this.currentCommand.viewType != "programController"
      );
    },
  },
  methods: {
    //接收入参变更
    changeInnerParam(variables) {
      this.currentCommand.variables = variables;
    },
    changeOutputParam(str) {
      this.currentCommand.outputVariables = str;
    },
    openScenario(data) {
      this.$emit("openScenario", data);
    },
    /**
     *  接受FabBtn 组件的变更
     */
    handleFabBtnStatus(status) {
      this.$nextTick(() => {
        this.immediately = status;
      });
    },
    autoChangeCurrentCommand(data) {
      this.changeCurrentCommand(data);
      this.$nextTick(() => {
        this.checkBeforceSave();
      });
    },
    checkBeforceSave() {
      return this.$refs.cmd.validate() && this.$refs.aside.validate();
    },
    async save(saveType) {
      try {
        this.loading = true;
        if (saveType === "variable") {
          let preCheck = this.$refs.uiEditBar.doValidate();
          let preResult = await preCheck.validate();
          let verify =
            preResult && preResult.validateResult
              ? preResult.validateResult.verify
              : true;
          if (
            !this.$refs.aside.validate() ||
            !this.$refs.cmd.validate() ||
            !verify
          ) {
            this.$success(this.$t("commons.save_success"));
            this.loading = false;
            return;
          }
          // 场景还没有保存过
          if (this.scenarioData.type === "add") {
            this.$success(this.$t("commons.save_success"));
            this.loading = false;
            return;
          }
        }

        let asideValidateRes = this.$refs.aside.validate();
        if (this.$refs.cmd.validate() && asideValidateRes) {
          let checkResult = await this.$refs.uiEditBar.validateScenario(
            this.scenarioData.scenarioDefinition
              ? this.scenarioData.scenarioDefinition.hashTree
              : []
          );
          if (!checkResult) {
            this.loading = false;
            return;
          }
          let scenario = {
            id: this.scenarioData.id,
            name: this.scenarioData.name,
            type: "scenario",
            clazzName: TYPE_TO_C.get("UiScenario"),
            browser: this.scenarioData.scenarioDefinition
              ? this.scenarioData.scenarioDefinition.browser
              : "CHROME",
            variables: this.scenarioData.variables,
            referenced: "Created",
            hashTree: cloneDeep(
              this.scenarioData.scenarioDefinition
                ? this.scenarioData.scenarioDefinition.hashTree
                : []
            ),
            onSampleError: this.onSampleError,
            projectId: this.scenarioData.projectId
              ? this.scenarioData.projectId
              : this.projectId,
            headlessEnabled:
              this.scenarioData.scenarioDefinition.headlessEnabled,
            scenarioConfig:
              this.scenarioData.scenarioDefinition.scenarioConfig,
            baseURL: this.scenarioData.scenarioDefinition
              ? this.scenarioData.scenarioDefinition.baseURL
              : "",
          };
          let request = {
            id: this.scenarioData.id,
            name: this.scenarioData.name,
            ...this.scenarioData,
            scenarioDefinition: scenario,
            type: "scenario",
            clazzName: TYPE_TO_C.get("UiScenario"),
            variables: this.scenarioData.variables,
            referenced: "Created",
            stepTotal: this.scenarioData.scenarioDefinition.hashTree.length,
            environmentType: ENV_TYPE.JSON,
            environmentJson: JSON.stringify(strMapToObj(this.projectEnvMap)),
            projectId: this.scenarioData.projectId
              ? this.scenarioData.projectId
              : this.projectId,
          };
          request.tags = JSON.stringify(request.tags);
          if (this.scenarioData.copy) {
            request.id = getUUID();
          }
          request = adaptToJackson(request);
          saveScenario(this.path, request, scenario.hashTree, this, (response) => {
            if (response.data) {
              this.path = "/ui/automation/update";
              delete this.scenarioData.type;
            }
            this.$success(this.$t("commons.save_success"));
            request.tags = JSON.parse(request.tags);
            this.$emit("refresh", response.data);
            if (this.scenarioData.copy) {
              this.scenarioData.id = request.id;
              this.scenarioData.copy = false;
            }
            commandStore.scenarioMap.set(
                this.scenarioData.id,
                JSON.parse(JSON.stringify(this.scenarioData))
            );
            this.loading = false;
          }, (error)=>{
            this.loading = false;

          });
        }
        else {
          this.$error(this.$t("ui.valiate_fail"));
          if (!asideValidateRes) {
            this.$refs.aside.activeName = "baseInfo";
          }
          this.loading = false;
        }
      } catch (error) {
        console.error(error);
      }
    },
    jumperToStep() {
      this.$refs.aside.activeName = "step";
    },
    openVariable() {},
    showAll() {
      // 控制当有弹出页面操作时禁止刷新按钮列表
      if (!this.customizeVisible && !this.isBtnHide) {
        this.operatingElements = this.stepFilter.get("ALL");
        this.selectedTreeNode = undefined;
        commandStore.selectStep = undefined;
      }
    },
    getVariableSize() {
      let size = 0;
      if (this.scenarioData.variables) {
        size += this.scenarioData.variables.length;
      }
      return size;
    },

    addListener() {
      //当前此处已通过UiScenarioEditBar 监听
      // document.addEventListener("keydown", this.createCtrlSHandle);
      // document.addEventListener("keydown", this.createCtrlRHandle);
    },
    removeListener() {},
    sort() {
      this.$refs.aside.$refs.tree.sort();
    },
    scenarioImport() {
      this.$refs.scenarioImportDialog.open();
    },
    checkRefSelf(scenario, currentScenarioId){
      if (scenario.resourceId === currentScenarioId && ( scenario.referenced === "REF" || scenario.referenced === "Copy") ) {
        this.$error(this.$t("api_test.scenario.scenario_error"));
        this.$refs.scenarioImportDialog.changeButtonLoadingType();
        return true;
      } else {
        if (scenario.hashTree && scenario.hashTree.length > 0) {
          for (let i = 0; i < scenario.hashTree.length; i++) {
            if (this.checkRefSelf(scenario.hashTree[i], currentScenarioId)) {
              return true;
            }
          }
        }
        return false;
      }
    },
  async  addScenario(arr) {
      if (arr && arr.length > 0) {
        arr.forEach((item) => {
          let hasRef = this.checkRefSelf(item, this.scenarioData.id)
          if (hasRef) {
            return;
          }
          if (!item.hashTree) {
            item.hashTree = [];
          }
          // this.resetResourceId(item.hashTree);
          item.enable === undefined ? (item.enable = true) : item.enable;
          let currentId = commandStore.commandStore.selectCommand
            ? commandStore.commandStore.selectCommand.resourceId
            : null;
          let structure = getIndexScenarioMap(
            this.scenarioData.scenarioDefinition,
            new Map(),
            new Map()
          );
          let scenarioMap = structure[0];
          if (scenarioMap.get(currentId) && scenarioMap.get(currentId).referenced === "REF") {
            this.$warning(this.$t("ui.scenario_ref_add_warning"));
            return;
          } else {
            this.handleAddStep(this.scenarioData.scenarioDefinition.hashTree, commandStore.commandStore.selectCommand, item)
          }
          this.addProjectEnvMap(item);
        });
      }
      this.isBtnHide = false;
      this.sort();
      this.reload();
    },
    resetResourceId(hashTree) {
      hashTree.forEach((item) => {
        item.resourceId = getUUID();
        if (item.hashTree && item.hashTree.length > 0) {
          this.resetResourceId(item.hashTree);
        }
      });
    },
    reload() {
      this.loadingDefinition = true;
      this.$nextTick(() => {
        this.loadingDefinition = false;
      });
    },
    fillingNullParams(request) {
      if (request.type == "MsUiCommand") {
        if (!request.commandConfig) {
          request.commandConfig = {
            ignoreFail: false,
            secondsWaitElement: 15000,
          };
        }
      } else if (request.type == "scenario") {
        if (request.hashTree) {
          for (let i = 0; i < request.hashTree.length; i++) {
            this.fillingNullParams(request[i]);
          }
        }
      } else if (request.type == "customCommand") {
        if (request.hashTree) {
          for (let i = 0; i < request.hashTree.length; i++) {
            this.fillingNullParams(request[i]);
          }
        }
      }
    },
    changeCurrentCommand(selectCommand) {
      if (selectCommand.projectId && !hasPermissionForProjectId("PROJECT_UI_SCENARIO:READ",selectCommand.projectId)) {
        return;
      }
      this.currentCommand = selectCommand;
      this.refreshElementLocator(this.currentCommand);
      this.sort();
      this.$nextTick(() => {
        this.$refs.cmd.initCmd();
        commandStore.selectCommand = selectCommand;
      });
    },
    //元素对象的模块可能会更改，这里统一做一次刷新操作
    refreshElementLocator(currentCommand) {
      let types = ["targetVO", "valueVO", "vo"];
      if (currentCommand) {
        for (let i = 0; i < types.length; i++) {
          if (types[i].split(".").length == 1) {
            if (
              currentCommand[types[i]] &&
              currentCommand[types[i]].elementType == "elementObject"
            ) {
              if (currentCommand[types[i]].elementId) {
                this.getElementById(
                  currentCommand[types[i]].elementId,
                  (el) => {
                    if (el && el.moduleId) {
                      currentCommand[types[i]].moduleId = el.moduleId;
                    }
                  }
                );
              }
            }
          }
        }

        if (
          currentCommand.vo &&
          currentCommand.vo.locator &&
          currentCommand.vo.locator.elementType == "elementObject"
        ) {
          if (currentCommand.vo.locator.elementId) {
            this.getElementById(currentCommand.vo.locator.elementId, (el) => {
              if (el && el.moduleId) {
                currentCommand.vo.locator.moduleId = el.moduleId;
              }
            });
          }
        }

        if (currentCommand.postCommands) {
          for (let k = 0; k < currentCommand.postCommands.length; k++) {
            if (
              currentCommand.postCommands[k].hashTree &&
              currentCommand.postCommands[k].hashTree.length
            ) {
              for (
                let n = 0;
                n < currentCommand.postCommands[k].hashTree.length;
                n++
              ) {
                if (
                  currentCommand.postCommands[k].hashTree[n].type ==
                  "MsUiCommand"
                )
                  this.refreshElementLocator(
                    currentCommand.postCommands[k].hashTree[n]
                  );
              }
            }
          }
        }

        if (currentCommand.preCommands) {
          for (let k = 0; k < currentCommand.preCommands.length; k++) {
            if (
              currentCommand.preCommands[k].hashTree &&
              currentCommand.preCommands[k].hashTree.length
            ) {
              for (
                let m = 0;
                m < currentCommand.preCommands[k].hashTree.length;
                m++
              ) {
                if (
                  currentCommand.preCommands[k].hashTree[m].type ==
                  "MsUiCommand"
                )
                  this.refreshElementLocator(
                    currentCommand.preCommands[k].hashTree[m]
                  );
              }
            }
          }
        }
      }
    },
    getElementById(elementId, callback) {
      this.$get("ui/element/" + elementId).then((data) => {
        callback(data.data);
      });
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
    },
    addProjectEnvMap(item) {
      if (item && item.environmentJson) {
        this.projectEnvMap = this.projectEnvMap || [];
        let projectEnvMapTobeMerge = JSON.parse(item.environmentJson) || {};
        Object.keys(projectEnvMapTobeMerge).forEach((key) => {
          if (!this.projectEnvMap[key]) {
            this.projectEnvMap[key] = projectEnvMapTobeMerge[key];
          }
        });
      }
    },
    handleEvent(event) {
      if (this.currentTabId !== this.scenarioData.id) {
        return;
      }
      // 键盘监听 ctrl + r
      if (event.ctrlKey && event.keyCode === 82) {
        event.preventDefault();
        this.runMode({ mode: "default" });
      }
      // 键盘监听 ctrl + s
      if (event.ctrlKey && event.keyCode === 83) {
        event.preventDefault();
        this.save();
      }
    },
    handleAddStep(hashTree, selectCommand, item) {
      if (hashTree.length && hashTree.length > 0) {
        this.handleAddChildStep(hashTree, selectCommand, item);
      } else {
        if (hashTree) {
          hashTree.push(item);
        } else {
          this.scenarioData.scenarioDefinition.hashTree = [];
          this.scenarioData.scenarioDefinition.hashTree.push(item);
        }
      }
    },
    handleAddChildStep(hashTree, selectCommand, item){
      if (!hashTree || hashTree.length <=0 ) {
        return;
      }
      for (let i = 0; i < hashTree.length; i++) {
        if (selectCommand && selectCommand.resourceId ) {
          if ((hashTree[i].resourceId === selectCommand.resourceId) && selectCommand.viewType === "programController") {
            hashTree[i].hashTree.push(item)
            return;
          } else if ((hashTree[i].resourceId === selectCommand.resourceId) && selectCommand.referenced ==="Copy") {
            hashTree[i].hashTree.push(item)
            return;
          } else if ((hashTree[i].resourceId === selectCommand.resourceId) ) {
            hashTree.splice(i + 1, 0, item);
            return;
          } else {
            this.handleAddChildStep(hashTree[i].hashTree,selectCommand, item);
          }
        } else {
          hashTree.push(item);
          return;
        }
      }
    }

  },
};
</script>

<style scoped>
.scenario-command {
}

.mt {
  margin-top: 30px;
}

.el-form-item__content.el-form-item__error {
  left: 118px !important;
}

.ms-container {
  height: calc(100vh - 130px);
}

.pre-opt-tip {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.ui-edit-bar {
  position: relative;
}

.card-content {
  height: calc(100vh - 210px);
  overflow-y: auto;
}
</style>
