<template>
  <ms-container>
    <ui-custom-command-edit-aside
      @openScenario="openScenario"
      v-loading="loadingDefinition"
      v-model="scenarioData"
      :module-options="moduleOptions"
      @changeCurrentCommand="changeCurrentCommand"
      @setProjectEnvMap="setProjectEnvMap"
      ref="aside"
    />

    <ms-main-container
      style="margin: 6px; height: calc(100vh - 145px); overflow: hidden"
    >
      <ui-custom-command-edit-bar
        :current-scenario="scenarioData"
        @runDebug="jumperToStep"
        @open-variable="openVariable"
        class="ui-edit-bar"
        @save="save"
        @setProjectEnvMap="setProjectEnvMap"
        :init-project-env-map="projectEnvMap"
        :scenarioType="currentType"
        ref="uiEditBar"
      />

      <div class="card-content" @click.stop="handleFabBtnStatus(false)">
        <div v-show="currentCommand.command">
          <ms-form-divider :title="$t('ui.command_steps_label')" />
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
          <!-- Sence v2.4 -->
          <UiCustomCommandPane
            :currentCommand="currentCommand"
            @changeInnerParam="changeInnerParam"
            @changeOutputParam="changeOutputParam"
            scenarioType="customCommand"
            :initName = "scenarioData.name"
            :initDesc = "scenarioData.description"
            @changeName = "changeName"
            @changeDesc = "changeDesc"
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
          :key="currentCommand.id"
        />

        <ui-scenario-relevance
          ref="scenarioImportDialog"
          :scenarioType="currentType"
          @save="addScenario"
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
import UiCustomCommandEditAside from "@/business/automation/custom-commands/edit/UiCustomCommandEditAside";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import { API_STATUS } from "@/api/JsonData";
import { STEP, TYPE_TO_C } from "@/api/Setting";
import UiCustomCommandEditBar from "@/business/automation/custom-commands/edit/UiCustomCommandEditBar";
import UiScenarioEditTree from "@/business/automation/scenario/edit/UiCustomCommandEditTree";
import UiScenarioEditFabBtn from "@/business/automation/scenario/edit/UiScenarioEditFabBtn";
import UiScenarioCommand from "@/business/automation/scenario/edit/command/UiScenarioCommand";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import UiScenarioAdvance from "@/business/automation/scenario/edit/advance/UiScenarioAdvance";
import { saveScenario } from "@/api/api-automation";
import { cloneDeep } from "lodash-es";
import UiScenarioRelevance from "@/business/automation/scenario/edit/UiScenarioEditRelevance";
import {
  getIndexScenarioMap,
  getIndexScenarioMapFromStruct,
} from "@/business/util/commonUtils";
import MsDebugRun from "@/business/automation/scenario/DebugRun";
import { useCommandStore } from "@/store";
import {adaptToJackson, jsonToMap} from "@/common/js/convert";
import UiCustomCommandPane from "@/business/automation/custom-commands/edit/UiCustomCommandPane";
import {ENV_TYPE} from "@/api/constants";
import {getUUID, strMapToObj} from "metersphere-frontend/src/utils";
import {hasPermissionForProjectId} from "metersphere-frontend/src/utils/permission";

const commandStore = new useCommandStore();
export default {
  name: "UiCustomCommandEdit",
  components: {
    MsDebugRun,
    UiScenarioAdvance,
    UiScenarioEditFabBtn,
    UiScenarioEditTree,
    UiCustomCommandEditBar,
    MsContainer,
    MsMainContainer,
    UiCustomCommandEditAside,
    UiScenarioCommand,
    MsFormDivider,
    UiScenarioRelevance,
    UiCustomCommandPane,
  },
  props: {
    moduleOptions: Array,
    type: String,
    currentType: String,
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
      tempRequest: {},
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
    //默认选中第一个
    if (
      this.scenarioData &&
      this.scenarioData.scenarioDefinition &&
      this.scenarioData.scenarioDefinition.hashTree &&
      this.scenarioData.scenarioDefinition.hashTree.length > 0
    ) {
      this.autoChangeCurrentCommand(
        this.scenarioData.scenarioDefinition.hashTree[0]
      );
    }
    window.addEventListener("keydown", this.handleEvent);
  },
  created() {
    this.scenarioData = this.currentScenario;
    // this.getModules();
    this.$EventBus.$on(
      "autoChangeCurrentCommand",
      this.autoChangeCurrentCommand
    );
    if (this.scenarioData.env) {
      this.projectEnvMap = jsonToMap(this.scenarioData.env);
    }
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
    changeName(name){
      this.$set(this.scenarioData, "name", name);
    },
    changeDesc(desc){
      this.scenarioData.description = desc;
    },
    //接收入参变更
    changeInnerParam(variables) {
      this.currentCommand.variables = variables;
      this.scenarioData.variables = variables;
      this.scenarioData.scenarioDefinition.variables = variables;
    },
    changeOutputParam(str) {
      this.currentCommand.outputVariables = str;
    },
    openScenario(data) {
      this.$emit("openScenario", data);
    },
    handleEvent(event) {
      if (this.currentTabId !== this.scenarioData.id) {
        return;
      }
      // 键盘监听 ctrl + r
      if (event.ctrlKey && event.keyCode === 82) {
        event.preventDefault();
        this.runMode({mode: "default"});
      }
      // 键盘监听 ctrl + s
      if (event.ctrlKey && event.keyCode === 83) {
        event.preventDefault();
        this.save();
      }
    },
    getModules() {
      //获取模块信息
      getScenarioModules(
        this.scenarioData.projectId,
        false,
        (data) => {
          if (data) {
            this.moduleOptions = data;
            if (this.scenarioData.moduleId === "default-module") {
              this.scenarioData.moduleId = this.moduleOptions[0].id;
            }
          }
        },
        this.currentType
      );
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
    async save(saveType, scenarioType) {
      if (saveType === "variable") {
        let preCheck = this.$refs.uiEditBar.prepareValidate();
        let preResult = preCheck.validate();
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
          return;
        }
        let scenario = {
          id: this.scenarioData.id,
          name: this.scenarioData.name,
          type: "customCommand",
          clazzName: TYPE_TO_C.get("customCommand"),
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
          scenarioConfig: this.scenarioData.scenarioDefinition.scenarioConfig,
          baseURL: this.scenarioData.scenarioDefinition
            ? this.scenarioData.scenarioDefinition.baseURL
            : "",
        };

        let request = {
          id: this.scenarioData.id,
          name: this.scenarioData.name,
          ...this.scenarioData,
          scenarioDefinition: scenario,
          type: "customCommand",
          clazzName: TYPE_TO_C.get("customCommand"),
          variables: this.scenarioData.variables,
          referenced: "Created",
          stepTotal: this.scenarioData.scenarioDefinition.hashTree.length,
          environmentType: ENV_TYPE.JSON,
          environmentJson: JSON.stringify(strMapToObj(this.projectEnvMap)),
          projectId: this.scenarioData.projectId
            ? this.scenarioData.projectId
            : this.projectId,
          scenarioType: scenarioType ? scenarioType : this.currentType,
        };
        request = adaptToJackson(request);
        request.tags = JSON.stringify(request.tags);
        request.scenarioType = this.currentType;
        if (request.commandViewStruct) {
          //去掉MAIN 中调试代码
          for (let i = 0; i < request.commandViewStruct.length; i++) {
            if (
              request.commandViewStruct[i].debuggerDetail &&
              request.commandViewStruct[i].debuggerDetail.type === "MAIN"
            ) {
              if(request.commandViewStruct[i].variables){
                request.scenarioDefinition.variables =  request.commandViewStruct[i].variables;
                request.scenarioDefinition.outputVariables = request.commandViewStruct[i].outputVariables;
              }
              request.commandViewStruct[i].hashTree = [];
              break;
            }
          }
          request.commandViewStruct = JSON.stringify(request.commandViewStruct);
        }
        //重置json_id
        this.tempRequest = request;
        this.resetJsonId();
        request = this.tempRequest;
        if (this.scenarioData.copy) {
          request.id = getUUID();
        }
        saveScenario(this.path, request, [], this, (response) => {
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
          commandStore.scenarioMap.set(this.scenarioData.id, JSON.parse(JSON.stringify(this.scenarioData)));
        });
      } else {
        this.$error(this.$t("ui.valiate_fail"));
        if (!asideValidateRes) {
          this.$refs.aside.activeName = "baseInfo";
        }
      }
    },
    resetJsonId() {
      if (!this.tempRequest.scenarioDefinition) {
        return;
      }
      this.doRestJsonId(this.tempRequest.scenarioDefinition.hashTree);
    },
    doRestJsonId(hashTree) {
      if (!hashTree || hashTree.length <= 0) {
        return;
      }

      hashTree.forEach((item) => {
        if (item["@json_id"]) {
          delete item["@json_id"];
        }

        if (item.hashTree && item.hashTree.length > 0) {
          this.doRestJsonId(item.hashTree);
        }
        if (item.preCommands && item.preCommands.length > 0) {
          this.doRestJsonId(item.preCommands);
        }
        if (item.postCommands && item.postCommands.length > 0) {
          this.doRestJsonId(item.postCommands);
        }
      });
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
    addScenario(arr) {
      let that = this;
      if (arr && arr.length > 0) {
        arr.forEach((item) => {
          if (item.resourceId === this.scenarioData.id) {
            this.$error(this.$t("api_test.scenario.scenario_error"));
            this.$refs.scenarioImportDialog.changeButtonLoadingType();
            return;
          }
          if (!item.hashTree) {
            item.hashTree = [];
          }
          // this.resetResourceId(item.hashTree);
          item.enable === undefined ? (item.enable = true) : item.enable;
          let currentId = commandStore.selectCommand
            ? commandStore.selectCommand.resourceId
            : null;
          let structure;
          if (this.scenarioData.commandViewStruct) {
            structure = getIndexScenarioMapFromStruct(
              this.scenarioData.commandViewStruct,
              new Map(),
              new Map()
            );
          } else {
            structure = getIndexScenarioMap(
              this.scenarioData.scenarioDefinition,
              new Map(),
              new Map()
            );
          }
          let scenarioMap = structure[0];
          let indexMap = structure[1];
          if (
            scenarioMap.get(currentId) &&
            !scenarioMap.get(currentId).readonly
          ) {
            scenarioMap
              .get(currentId)
              .hashTree.splice(indexMap.get(currentId) + 1, 0, item);
            this.changeCurrentCommand(scenarioMap.get(currentId).hashTree[scenarioMap.get(currentId).hashTree.length - 1])
          } else {
            //获取当前选中的是否为前后置
            let selectCommand = commandStore.selectCommand;
            if(selectCommand && selectCommand.useDebugger){
              selectCommand.hashTree.push(item);
              this.changeCurrentCommand(selectCommand.hashTree[selectCommand.hashTree.length - 1])
            }
            else{
              this.scenarioData.scenarioDefinition.hashTree.push(item);
            }
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
      if (!hasPermissionForProjectId("PROJECT_UI_SCENARIO:READ",selectCommand.projectId)) {
        return;
      }
      this.currentCommand = selectCommand;
      this.refreshElementLocator(this.currentCommand);
      this.sort();
      this.$nextTick(() => {
        if (this.$refs.cmd) {
          this.$refs.cmd.initCmd();
        }
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
        })
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
/*
  Sence v2.4
*/
.base-info-wrap .sub-row {
  display: flex;
  padding: 5px 0 5px 0px;
}
.sub-row .label {
  width: 120px;
  text-align: right;
  padding-right: 12px;
}
.sub-row .content {
  width: 180px;
}

.desc-row .content {
  width: 600px;
}

.param-wrap {
  padding: 0px 10px 10px 10px;
  border: 1px solid #d7d2d2;
  border-radius: 4px;
}

.param-data-table {
  width: 100%;
  border: 1px solid #d7d2d2;
  border-radius: 4px;
}
</style>
