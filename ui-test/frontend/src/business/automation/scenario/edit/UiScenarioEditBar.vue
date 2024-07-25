<template>
  <el-card @click.native="clickCard">
    <el-row :gutter="5">
      <!--   左半边，左对齐   -->
      <el-col :span="9">
        <el-row>
          <el-col :span="10">
            {{ $t("api_test.automation.step_total") }}：{{ stepsNumber }}
          </el-col>
          <el-col :span="2"> &nbsp;</el-col>
          <el-col :span="10" style="display: flex; align-items: center">
            <el-link class="head scenario_total" @click="openVariable">
              {{ $t("api_test.automation.scenario_total") }}
            </el-link>
            ：{{ variableSize }}
          </el-col>
        </el-row>
      </el-col>

      <!--   右半边，右对齐   -->
      <el-col :span="15">
        <!-- 容器 -->
        <div class="scenario-container">
          <!-- 性能模式 -->
          <div class="performance-opt">
            <el-checkbox
              v-if="currentScenario.scenarioDefinition"
              v-model="currentScenario.scenarioDefinition.headlessEnabled"
            >
              <span> {{ $t("ui.performance_mode") }}</span>
            </el-checkbox>
            <ms-instructions-icon size="10" :content="$t('ui.per_tip')" />
          </div>

          <!-- 浏览器驱动 -->
          <div class="browser-opt">
            <el-select
              v-if="currentScenario.scenarioDefinition"
              size="mini"
              v-model="currentScenario.scenarioDefinition.browser"
              style="width: 100px"
              @click.native="clickCard"
            >
              <el-option
                v-for="b in browsers"
                :key="b.value"
                :value="b.value"
                :label="b.label"
              ></el-option>
            </el-select>
          </div>

          <div class="env-bar">
            <env-popover
              :disabled="readOnlyEnvPopover"
              :env-map="projectEnvMap"
              :project-ids="projectIds"
              :result="envResult"
              :environment-type.sync="environmentType"
              :isReadOnly="readOnlyEnvPopover"
              :group-id="envGroupId"
              :project-list="projectList"
              :show-config-button-with-out-permission="false"
              @setProjectEnvMap="setProjectEnvMap"
              @setEnvGroup="setEnvGroup"
              @showPopover="showPopover"
              :show-env-group="false"
              ref="envPopover"
              class="ms-message-right"
            />
          </div>

          <!--          调试运行-->
          <div class="debug-opt">
            <el-tooltip v-if="!debugLoading" content="Ctrl + R" placement="top">
              <el-dropdown
                v-permission="['PROJECT_UI_SCENARIO:READ+DEBUG']"
                split-button
                type="primary"
                class="ms-message-right"
                size="mini"
                @click="runMode({ mode: 'default' })"
                @command="handleCommand"
              >
                {{ currentDebugMode }}
                <el-dropdown-menu slot="dropdown" ref="runDropdownRef">
                  <el-dropdown-item :command="{ mode: 'local', report: false }"
                    >{{ $t("ui.ui_local_debug") }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ mode: 'server', report: false }"
                    >{{ $t("ui.ui_server_debug") }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ mode: 'report', report: true }"
                    >{{ $t("api_test.automation.generate_report") }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-tooltip>

            <!-- 停止按钮 -->
            <el-button
              v-else
              size="mini"
              type="primary"
              @click="stop"
              class="stop-opt"
              :disabled="runDebugEnabled"
            >
              {{ $t("report.stop_btn") }}
            </el-button>
          </div>

          <!-- 保存 -->
          <div class="save-opt">
            <el-tooltip
              class="item"
              effect="dark"
              :content="$t('commons.save')"
              placement="top-start"
            >
              <el-button
                size="mini"
                type="primary"
                @click="preSave"
                class="ht-btn-confirm"
                >{{ $t("commons.save") }}
              </el-button>
            </el-tooltip>
          </div>

          <!-- 刷新 -->
          <div class="refresh-opt">
            <el-tooltip
              class="item"
              effect="dark"
              :content="$t('commons.refresh')"
              placement="top-start"
            >
              <el-button
                :disabled="stepsNumber < 1"
                size="mini"
                icon="el-icon-refresh"
                v-prevent-re-click
                @click="fresh"
                class="refresh-btn"
              ></el-button>
            </el-tooltip>
          </div>
        </div>
      </el-col>
    </el-row>

    <ms-debug-run
      :debug="true"
      :reportId="reportId"
      :saved="saved"
      :execute-type="executeType"
      :run-data="debugData"
      :runLocal="runLocal"
      :environment="projectEnvMap"
      :browserLanguage="browserLanguage"
      :uiRunMode="uiRunMode"
      @runRefresh="runRefresh"
      @errorRefresh="errorRefresh"
      ref="runTest"
    />

    <!-- 调试结果 -->
    <el-drawer
      :visible.sync="debugVisible"
      :destroy-on-close="true"
      :before-close="resetDebugBtn"
      direction="ltr"
      :withHeader="true"
      :modal="false"
      size="90%"
    >
      <sysn-api-report-detail
        v-loading="debugLoading"
        :element-loading-text="$t('ui.executing')"
        @finish="debugLoading = false"
        :scenario="currentScenario"
        :report-id="reportId"
        :debug="true"
        :is-ui="true"
        ref="uiReport"
        @refresh="debugData = {}"
      />
    </el-drawer>
    <!--场景公共参数-->
    <ui-variable-list
      @setVariables="setVariables"
      ref="scenarioParameters"
      class="ms-sc-variable-header"
    />
  </el-card>
</template>

<script>
import UiScenarioEditAside from "./UiScenarioEditAside";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import {
  getReportMessageSocket,
  savePreciseEnvProjectIds,
  saveScenario,
} from "@/api/api-automation";

import {
  getOwnerProjects,
  getUiRunDebugData,
  getUiScenarioEnv,
  validateFirstCommand,
  validateProgramController,
} from "@/business/automation/ui-automation";
import MsDebugRun from "@/business/automation/scenario/DebugRun";
import SysnApiReportDetail from "@/business/automation/report/SysnApiReportDetail";
import UiVariableList from "@/business/automation/scenario/component/UiVariableList";
import { TYPE_TO_C } from "@/api/Setting";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import { filter, findIndex, cloneDeep, map, forEach } from "lodash-es";
import DefinitionValidator from "@/business/definition/validator/definition-validator";
import { useCommandStore } from "@/store";
import { getProjectApplicationGetConfig } from "@/business/network/ui-scenario";
import { hasPermission } from "metersphere-frontend/src/utils/permission";
import { ENV_TYPE, MOCKJS_FUNC } from "@/api/constants";
import ParamProcessor from "@/business/definition/parameter-processor";
import { setScenarioConfig } from "@/business/automation/ui-automation-model";
import EnvPopover from "@/business/automation/env/EnvPopover";
import { getUUID, strMapToObj } from "metersphere-frontend/src/utils";
import {
  getCurrentProjectID,
  getCurrentWorkspaceId,
} from "metersphere-frontend/src/utils/token";

const commandStore = new useCommandStore();
export default {
  name: "UiScenarioEditBar",
  components: {
    MsInstructionsIcon,
    SysnApiReportDetail,
    MsDebugRun,
    MsContainer,
    MsMainContainer,
    UiScenarioEditAside,
    UiVariableList,
    EnvPopover,
  },
  props: {
    currentScenario: Object,
    initProjectEnvMap: {
      type: Map,
      default() {
        return new Map();
      },
    },
  },
  watch: {
    initProjectEnvMap: {
      handler(val) {
        this.projectEnvMap = val;
      },
      immediate: true,
    },
  },
  data() {
    return {
      tempCurrentScenario: {},
      currentDebugMode: "",
      variableSize: 0,
      debugLoading: false,
      reportId: "",
      saved: false,
      runLocal: false,
      browserLanguage: "",
      uiRunMode: "",
      debugData: {},
      message: "",
      clearMessage: "",
      websocket: {},
      debugVisible: false,
      messageWebSocket: {},
      reportOverView: {
        reqTotal: 0,
        reqSuccess: 0,
        reqError: 0,
        reqTotalTime: 0,
      },
      browsers: [
        {
          label: this.$t("chrome"),
          value: "CHROME",
        },
        {
          label: this.$t("firefox"),
          value: "FIREFOX",
        },
      ],
      executeType: null,
      mockFuncs: MOCKJS_FUNC.map((f) => {
        return f.name;
      }),
      projectList: [],
      projectIds: new Set(),
      envResult: {
        loading: false,
      },
      envGroupId: "",
      projectEnvMap: new Map(),
      environmentType: ENV_TYPE.JSON,
      runDebugEnabled: true,
    };
  },
  beforeUpdate() {
    this.getVariableSize();
  },
  computed: {
    scenarioName() {
      return this.currentScenario.name === undefined || ""
        ? this.$t("api_test.scenario.name")
        : this.currentScenario.name;
    },
    stepsNumber() {
      if (
        !this.currentScenario.scenarioDefinition ||
        !this.currentScenario.scenarioDefinition.hashTree
      ) {
        return 0;
      }
      return this.currentScenario.scenarioDefinition.hashTree.length;
    },
    projectId() {
      return getCurrentProjectID();
    },
    workspaceId() {
      return getCurrentWorkspaceId();
    },
    readOnlyEnvPopover() {
      if (
        this.currentScenario.scenarioDefinition &&
        this.currentScenario.scenarioDefinition.hashTree &&
        this.currentScenario.scenarioDefinition.hashTree.length > 0
      ) {
        return false;
      }
      return true;
    },
  },
  mounted() {
    this.getCustomUiDebugModeName();
    this.getWsProjects();
    setScenarioConfig(this.currentScenario.scenarioDefinition, this);
    if (this.currentScenario.environmentJson) {
      this.projectEnvMap = JSON.parse(this.currentScenario.environmentJson);
    }
  },
  methods: {
    hasPermission,
    clickCard() {
      this.$EventBus.$emit("changeFabBtnStatus", false);
    },
    validateInnerParam() {
      let validateDTO = ParamProcessor.validate(
        this.currentScenario.scenarioDefinition
      );
      if (validateDTO && validateDTO.status <= 0) {
        if (validateDTO.status == -2) {
          this.$warning("参数名称不能为空");
          return false;
        } else {
          this.$warning(
            "" +
              "【" +
              validateDTO.key +
              "】" +
              this.$t("load_test.param_is_duplicate")
          );
          return false;
        }
      }
      return true;
    },
    preSave() {
      if (this.currentScenario.type !== "add" && this.currentScenario.id) {
        if (!hasPermission("PROJECT_UI_SCENARIO:READ+EDIT")) {
          this.$message.error(this.$t("ui.no_scenario_edit_permission"));
          return;
        }
      }
      if (this.currentScenario.copy) {
        if (!hasPermission("PROJECT_UI_SCENARIO:READ+COPY")) {
          this.$message.error(this.$t("ui.no_scenario_copy_permission"));
          return;
        }
      }

      if (this.currentScenario.type === "add") {
        if (!hasPermission("PROJECT_UI_SCENARIO:READ+CREATE")) {
          this.$message.error(this.$t("ui.no_scenario_create_permission"));
          return;
        }
      }

      //先处理流程控制校验
      let hashTree = this.currentScenario.scenarioDefinition.hashTree;
      if (hashTree.length > 0) {
        validateProgramController(hashTree);
      }

      let r = this.validateInnerParam();
      if (!r) {
        return;
      }

      if (this.currentScenario.scenarioDefinition) {
        //首先处理入参 出参数据
        this.handleParamData(this.currentScenario.scenarioDefinition);
      }
      this.$emit("save");
    },

    async getCustomUiDebugModeName() {
      let userRunMode = localStorage.getItem(
        "CUSTOM_RUN_MODE_" + this.workspaceId
      );
      if (!userRunMode) {
        // let node = this.$refs["runDropdownRef"];
        // node.showPopper = true;
        let mode = await this.getCustomUiDebugMode();
        this.currentDebugMode =
          mode && mode.mode === "local"
            ? this.$t("ui.ui_local_debug")
            : this.$t("ui.ui_server_debug");
      } else {
        let userRunModeObj = JSON.parse(userRunMode);
        this.currentDebugMode =
          userRunModeObj.mode === "local"
            ? this.$t("ui.ui_local_debug")
            : userRunModeObj.mode === "report"
            ? (this.currentDebugMode = this.$t(
                "api_test.automation.generate_report"
              ))
            : this.$t("ui.ui_server_debug");
      }
    },
    async runMode(data) {
      if (!this.checkEnvBeforeSave()) {
        return;
      }
      //查看缓存中是否有用户的调试mode
      let userRunMode = localStorage.getItem(
        "CUSTOM_RUN_MODE_" + this.workspaceId
      );
      if (!userRunMode) {
        // let node = this.$refs["runDropdownRef"];
        // node.showPopper = true;
        let mode = await this.getCustomUiDebugMode();
        let projectData = mode ? mode.data : undefined;
        if (!projectData || !projectData.uiQuickMenu) {
          mode = { mode: "server", report: false };
        } else {
          mode = { mode: projectData.uiQuickMenu, report: false };
        }
        await this.runDebug(mode);
      } else {
        await this.runDebug(JSON.parse(userRunMode));
      }
    },
    async getCustomUiDebugMode() {
      return getProjectApplicationGetConfig(this.projectId);
    },
    getVariableSize() {
      let size = 0;
      if (this.currentScenario.variables) {
        size += this.currentScenario.variables.length;
      }
      this.variableSize = size;
      return size;
    },

    setVariables(saveVariables, headers) {
      this.currentScenario.variables = saveVariables;
      this.currentScenario.headers = headers;
      // scenarioDefinition 也需要更新
      this.currentScenario.scenarioDefinition.variables = saveVariables;
      this.currentScenario.scenarioDefinition.headers = headers;
      // 直接更新场景防止编辑内容丢失
      // this.setParameter();
      this.getVariableSize();
      this.$emit("save", "variable");
    },

    initParameter() {
      if (!this.currentScenario.projectId) {
        this.currentScenario.projectId = this.projectId;
      }
      // 构建一个场景对象 方便引用处理
      let scenario = {
        id: this.currentScenario.id,
        enableCookieShare: this.enableCookieShare,
        name: this.currentScenario.name,
        type: "scenario",
        clazzName: TYPE_TO_C.get("UiScenario"),
        variables: this.currentScenario.variables,
        headers: this.currentScenario.headers,
        referenced: "Created",
        environmentMap: strMapToObj(this.projectEnvMap),
        hashTree: this.scenarioDefinition,
        onSampleError: this.onSampleError,
        headlessEnabled:
          this.currentScenario.scenarioDefinition.headlessEnabled,
        scenarioConfig: this.currentScenario.scenarioDefinition.scenarioConfig,
        projectId: this.currentScenario.projectId
          ? this.currentScenario.projectId
          : this.projectId,
      };
      let request = {
        id: this.currentScenario.id,
        name: this.currentScenario.name,
        ...this.currentScenario,
        scenarioDefinition: scenario,
        type: "scenario",
        clazzName: TYPE_TO_C.get("UiScenario"),
        variables: this.currentScenario.variables,
        referenced: "Created",
        projectId: this.currentScenario.projectId
          ? this.currentScenario.projectId
          : this.projectId,
      };
      this.currentScenario.environmentType = this.environmentType;
      this.currentScenario.environmentJson = JSON.stringify(
        strMapToObj(this.projectEnvMap)
      );
      request.tags = JSON.stringify(request.tags);
      saveScenario("/ui/automation/update", request, [], this, (response) => {
        this.$success(this.$t("commons.save_success"));
        request.tags = JSON.parse(request.tags);
      });
    },
    setParameter() {
      this.initParameter();
      let definition = JSON.parse(JSON.stringify(this.currentScenario));
      definition.hashTree = this.scenarioDefinition;
      // 保存时同步所需要的项目环境
      savePreciseEnvProjectIds(this.projectIds, this.projectEnvMap);
    },
    openVariable() {
      this.$refs.scenarioParameters.open(
        this.currentScenario.variables,
        this.currentScenario.headers
      );
    },
    showAll() {
      // 控制当有弹出页面操作时禁止刷新按钮列表
      if (!this.customizeVisible && !this.isBtnHide) {
        this.operatingElements = this.stepFilter.get("ALL");
        this.selectedTreeNode = undefined;
        commandStore.selectStep = undefined;
      }
    },

    clearDebug() {
      this.reportOverView = {
        reqTotal: 0,
        reqSuccess: 0,
        reqError: 0,
        reqTotalTime: 0,
      };
    },
    clearResult(arr) {
      if (arr) {
        arr.forEach((item) => {
          this.$set(item, "debugResult", null);
          if (item.hashTree && item.hashTree.length > 0) {
            this.clearResult(item.hashTree);
          }
        });
      }
    },
    runRefresh() {
      this.runLocal = false;
      this.runDebugEnabled = false;
      if (this.saved && this.executeType == "Saved") {
        this.debugVisible = true;
        this.loading = false;
      } else {
        this.messageWebSocket = getReportMessageSocket(this.reportId);
        this.messageWebSocket.onmessage = this.onDebugMessage;
        this.messageWebSocket.onclose = this.onDebugMessageClose;
      }
    },
    onDebugMessageClose() {
      if (this.debugLoading) {
        console.log("ws message end, stop task~");
        this.stopTask();
      }
    },
    onDebugMessageErr() {
      if (this.debugLoading) {
        console.log("ws message err, stop task~");
        this.stopTask();
      }
    },
    stopTask() {
      this.runScenario = undefined;
      this.debugLoading = false;
      this.message = "stop";
      this.stopDebug = "stop";
    },
    onDebugMessage(e) {
      if (e.data && e.data.startsWith("result_")) {
        let data = JSON.parse(e.data.substring(7));
        let results = data.responseResult.headers
          ? JSON.parse(data.responseResult.headers)
          : [];
        this.reportOverView.reqTotal += results.length;
        let resultMap = {};
        results.forEach((result) => {
          if (result.success) {
            this.reportOverView.reqSuccess += 1;
          } else {
            this.reportOverView.reqError += 1;
          }
          let time = result.endTime - result.startTime;
          this.reportOverView.reqTotalTime += time > 0 ? time : 0;

          // 如果id出现多次则是循环，转成数据
          let v = resultMap[result.id];
          if (v) {
            if (v instanceof Array) {
              v.push(result);
            } else {
              v = [v, result];
            }
          } else {
            v = result;
          }
          resultMap[result.id] = v;
        });
        this.runningEvaluation(
          this.currentScenario.scenarioDefinition.hashTree,
          resultMap
        );

        //运行出参结果回显
        new ParamProcessor()
          .getParamProcessor("OUTPUT")
          .fillRunTimeData(this.currentScenario.scenarioDefinition, results);
        //发送出参处理事件
      }
      this.message = getUUID();
      if (e.data && e.data.indexOf("MS_TEST_END") !== -1) {
        this.runScenario = undefined;
        this.debugLoading = false;
        this.message = "stop";
        this.stopDebug = "stop";
      }
    },
    runningEvaluation(nodes, debugResults) {
      nodes.forEach((node) => {
        if (debugResults[node.id]) {
          //后置没有断言的时候 该步骤成功就是成功 否则 该步骤成功与否要看断言的结果
          this.$set(
            node,
            "debugResult",
            this.getValidateDebugResult(node, debugResults)
          );
        }
        if (node.hashTree) {
          this.runningEvaluation(node.hashTree, debugResults);
        }
      });
    },
    getValidateDebugResult(node, debugResults) {
      if (
        !Array.isArray(debugResults[node.id]) &&
        !debugResults[node.id].success
      ) {
        return debugResults[node.id];
      }
      if (!node.postCommands || !node.postCommands.length) {
        return debugResults[node.id];
      }
      let validateCmds = filter(
        node.postCommands,
        (c) => c.command == "cmdValidation"
      );
      if (!validateCmds || !validateCmds.length) {
        return debugResults[node.id];
      }
      let validateResults = [];
      for (let i = 0; i < validateCmds.length; i++) {
        if (validateCmds[i].hashTree) {
          for (let j = 0; j < validateCmds[i].hashTree.length; j++) {
            let validateCmd = validateCmds[i].hashTree[j];
            //只要有一个断言失败 整个步骤就失败
            if (
              debugResults[validateCmd.id] &&
              !debugResults[validateCmd.id].success
            ) {
              return debugResults[validateCmd.id];
            }
          }
        }
      }
      return debugResults[node.id];
    },
    errorRefresh(error) {
      this.isTop = false;
      this.debugLoading = false;
      this.debugVisible = false;
      this.loading = false;
      this.runScenario = undefined;
      this.message = "stop";
      this.clearMessage = getUUID().substring(0, 8);
      this.debugData = {};
    },
    async runDebug(data) {
      this.debugLoading = true;
      this.runDebugEnabled = true;
      let checkResult = await this.validateScenario(
        this.currentScenario.scenarioDefinition
          ? this.currentScenario.scenarioDefinition.hashTree
          : []
      );
      if (!checkResult) {
        this.debugLoading = false;
        return;
      }
      let result = true;
      let localResult = await this.validatePersonalSetting(data);
      let serverResult = await this.validateSeleniumSetting(data);
      result = localResult && serverResult;
      this.currentDebugMode =
        data.mode === "local"
          ? this.$t("ui.ui_local_debug")
          : data.mode === "report"
          ? (this.currentDebugMode = this.$t(
              "api_test.automation.generate_report"
            ))
          : this.$t("ui.ui_server_debug");
      localStorage.setItem(
        "CUSTOM_RUN_MODE_" + this.workspaceId,
        JSON.stringify(data)
      );
      if (!result) {
        this.debugLoading = false;
        return;
      }

      let hashTree = this.currentScenario.scenarioDefinition.hashTree;
      // if (hashTree.length < 1) {
      //   this.debugLoading = false;
      //   return;
      // }
      try {
        this.validateHashTree(hashTree);
      } catch (e) {
        this.debugLoading = false;
        return;
      }

      let r = this.validateInnerParam();
      if (!r) {
        this.debugLoading = false;
        return;
      }

      //处理场景变量
      try {
        if (this.$refs.scenarioParameters) {
          this.$refs.scenarioParameters.paramConvertProcessor(
            this.currentScenario.variables
          );
        }
      } catch (e) {
        console.log("reset vars err");
      }

      //首先处理入参 出参数据
      this.handleParamData(this.currentScenario.scenarioDefinition);

      //填充场景变量的mock数据
      this.tempCurrentScenario = cloneDeep(this.currentScenario);
      this.fillMockData(this.tempCurrentScenario.scenarioDefinition);
      this.$emit("runDebug");
      this.stopDebug = "";
      this.clearDebug();
      this.clearResult(hashTree);
      //调试和报告都存
      this.saved = true;
      this.executeType = data.report ? "Saved" : "Debug";
      if (data.mode === "local") {
        this.runLocal = true;
        this.uiRunMode = "local";
      }
      if (data.mode === "server") {
        this.uiRunMode = "server";
      }
      if (data.mode === "report") {
        this.uiRunMode = "report";
      }

      //获取浏览器语言
      this.browserLanguage = navigator.language || navigator.userLanguage;
      this.debugData = getUiRunDebugData(this.tempCurrentScenario, data.mode);
      await this.getEnv(
        JSON.stringify(this.tempCurrentScenario.scenarioDefinition)
      );
      this.reportId = getUUID().substring(0, 8);
    },
    handleParamData(scenarioDefinition) {
      let processor = new ParamProcessor();
      processor.convert(scenarioDefinition);
    },
    handleScenarioDefinition(scenarioDefinition) {
      if (
        !scenarioDefinition ||
        scenarioDefinition.handleRepeat ||
        !scenarioDefinition.hashTree
      ) {
        return;
      }
      scenarioDefinition.resourceId = scenarioDefinition.id || getUUID();
      scenarioDefinition.id = getUUID();
      for (let item of scenarioDefinition.hashTree) {
        let temp = item.id || getUUID();
        item.id = item.resourceId;
        item.resourceId = getUUID();
        if (item.hashTree) {
          this.handleScenarioDefinition(item);
        }
      }
      scenarioDefinition.handleRepeat = true;
    },
    fillMockData(scenarioDefinition) {
      //没有场景变量 无需处理
      //doFill
      if (
        scenarioDefinition &&
        scenarioDefinition.variables &&
        scenarioDefinition.variables.length > 0
      ) {
        let result = this.doFill(scenarioDefinition.variables);
        scenarioDefinition.variables = result;
      }
      //递归
      if (
        scenarioDefinition.hashTree &&
        scenarioDefinition.hashTree.length > 0
      ) {
        scenarioDefinition.hashTree.forEach((v) => {
          this.fillMockData(v);
        });
      }

      //前后置
      if (
        scenarioDefinition.postCommands &&
        scenarioDefinition.postCommands.length > 0
      ) {
        scenarioDefinition.postCommands.forEach((v) => {
          this.fillMockData(v);
        });
      }

      if (
        scenarioDefinition.preCommands &&
        scenarioDefinition.preCommands.length > 0
      ) {
        scenarioDefinition.preCommands.forEach((v) => {
          this.fillMockData(v);
        });
      }
    },
    doFill(variables) {
      return variables.map((obj) => {
        if (!obj.value || obj.value.trim === "") {
          return obj;
        }
        // 有值范围的不加空格
        if (obj.value.indexOf("(") > -1 && obj.value.indexOf(")") > -1) {
          obj.value = this.refreshTempStrMockData(obj.value);
        } else {
          obj.value = this.refreshTempMockData(obj.value);
        }

        return obj;
      });
    },
    refreshTempMockData(temp) {
      if (!temp) {
        return temp;
      }
      try {
        for (let i = 0; i < this.mockFuncs.length; i++) {
          let target = this.mockFuncs[i];
          temp = temp.replace(new RegExp(target, "g"), target);
        }
      } catch (e) {
        //ignore
      }

      return temp;
    },
    refreshTempStrMockData(temp) {
      if (!temp) {
        return temp;
      }
      try {
        for (let i = 0; i < this.mockFuncs.length; i++) {
          let target = this.mockFuncs[i];
          temp = temp.replace(new RegExp(target, "g"), target);
        }
      } catch (e) {
        //ignore
      }

      return temp;
    },
    async validatePersonalSetting(data) {
      if (data.mode === "local") {
        //选择本地调试默认取消性能模式
        const h = this.$createElement;
        //检测是否配置了 个人地址, 检测配置的ip端口 是否正常
        let result = await this.$get(
          "/ui/automation/verify/seleniumServer/user",
          null
        );
        if (result && result.data) {
          let res = result.data;
          if (res === "ok") {
            return true;
          } else if (res === "userConnectionErr") {
            this.showMessageBox(
              h("p", null, [
                h("span", null, this.$t("ui.check_grid")),
                h(
                  "p",
                  {
                    style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                    on: {
                      click: (value) => {
                        this.redirectOptDoc(value);
                      },
                    },
                  },
                  this.$t("ui.view_config")
                ),
              ])
            );
            return false;
          } else {
            this.showMessageBox(
              h("p", null, [
                h("span", null, this.$t("ui.config_ip")),
                h(
                  "a",
                  {
                    style: "color: #783887;cursor:pointer;",
                    on: {
                      click: (value) => {
                        this.openPersonInfo(value);
                      },
                    },
                  },
                  this.$t("ui.personal_info")
                ),
                h("span", null, this.$t("ui.in_config")),
                h(
                  "p",
                  {
                    style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                    on: {
                      click: (value) => {
                        this.redirectOptDoc(value);
                      },
                    },
                  },
                  this.$t("ui.view_config")
                ),
              ])
            );
            return false;
          }
        }
      }
      return true;
    },
    async validateSeleniumSetting(data) {
      if (data.mode === "report" || data.mode === "server") {
        //选择本地调试默认取消性能模式
        const h = this.$createElement;
        //检测是否配置了 个人地址, 检测配置的ip端口 是否正常
        let result = await this.$get(
          "/ui/automation/verify/seleniumServer/sys",
          null
        );
        if (result.data) {
          let res = result.data;
          if (res === "ok") {
            return true;
          } else if (res === "connectionErr") {
            this.showMessageBox(
              h("p", null, [
                h("span", null, this.$t("ui.check_grid")),
                h(
                  "p",
                  {
                    style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                    on: {
                      click: (value) => {
                        this.redirectSetting();
                      },
                    },
                  },
                  this.$t("ui.view_config")
                ),
              ])
            );
            return false;
          } else {
            this.showMessageBox(
              h("p", null, [
                h("span", null, this.$t("ui.check_grid_ip")),
                h(
                  "p",
                  {
                    style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                    on: {
                      click: (value) => {
                        this.redirectSetting();
                      },
                    },
                  },
                  this.$t("ui.view_config")
                ),
              ])
            );
            return false;
          }
        }
      }
      return true;
    },
    validateHashTree(hashTree) {
      validateFirstCommand(hashTree);
      validateProgramController(hashTree);
    },
    redirectOptDoc() {
      window.open("https://kb.fit2cloud.com/?p=261");
    },
    redirectSetting() {
      window.open("/#/setting/systemparametersetting");
    },
    openPersonInfo() {
      this.$msgbox.close();
      this.$EventBus.$emit("showPersonInfo", "personal");
      if (hasPermission("PERSONAL_INFORMATION:READ+UI_SETTING")) {
        setTimeout(() => {
          this.$EventBus.$emit("siwtchActive", "commons.ui_setting");
        }, 10);
      }
    },
    showMessageBox(msg) {
      this.$msgbox({
        title: "",
        message: msg,
        confirmButtonText: this.$t("commons.confirm"),
        cancelButtonText: this.$t("commons.cancel"),
      })
        .then((action) => {})
        .catch(() => {});
    },
    handleCommand(data) {
      this.runDebug(data);
    },
    stop() {
      if (this.reportId) {
        this.debugLoading = false;
        try {
          if (this.messageWebSocket) {
            this.messageWebSocket.close();
          }
          if (this.websocket) {
            this.websocket.close();
          }
          this.clearDebug();
          this.$success(this.$t("report.test_stop_success"));
        } catch (e) {
          this.debugLoading = false;
        }
        this.runScenario = undefined;
        // 停止jmeter执行
        this.$get("/ui/automation/stop/" + this.reportId);
      }
    },
    fresh() {
      // commandStore.setRefreshUiScenario = true;
      commandStore.refreshUiScenario = true;
    },
    resetDebugBtn() {
      this.debugLoading = false;
      this.debugVisible = false;
      this.$refs.uiReport.cleanHeartBeat();
    },
    validateScenarioPromise(hashTree) {
      return this.$post("ui/automation/validateScenario", hashTree);
    },
    /**
     * todo 2.0 开启参数校验并且配套前端一系列动作
     * @param hashTree
     * @returns {Promise<boolean>}
     */
    async validateScenario(hashTree) {
      let validator = this.doValidate();
      let data = await validator.validate();
      if (data) {
        this.$error(this.$t("ui.valiate_fail"));

        this.$EventBus.$emit("autoOpenExpansion", validator.errResourceIds);
        this.$EventBus.$emit("autoChangeCurrentCommand", data);
        this.$nextTick(() => {
          if (data.validateResult.preCommandsErr) {
            this.$EventBus.$emit("handleAdvanceValidate", "pre");
          } else if (data.validateResult.postCommandsErr) {
            this.$EventBus.$emit("handleAdvanceValidate", "post");
          }
        });
        return false;
      }
      return true;
    },
    //执行校验逻辑
    doValidate() {
      let validator = new DefinitionValidator(
        this.currentScenario.scenarioDefinition,
        this
      );
      return validator;
    },
    formatValidateMsg(validateResult) {
      let msg = "";
      if (validateResult && validateResult.length) {
        for (let i = 0; i < validateResult.length; i++) {
          msg += validateResult[i].msg + "<br /><br />";
        }
      }
      return msg;
    },
    resetCurrentCommand(validateResult) {
      if (!validateResult || !validateResult.length) {
        return;
      }
      let id = validateResult[0].id;
      let hashTree = cloneDeep(
        this.currentScenario.scenarioDefinition.hashTree || []
      );
      if (!hashTree.length) {
        return;
      }
      let index = findIndex(hashTree, { id: id });
      if (index != -1) {
        this.changeCurrentCommand(hashTree[index]);
      } else {
        index = findIndex(hashTree, { id: validateResult[0].parentId });
        if (index != -1) {
          this.changeCurrentCommand(hashTree[index]);
        } else {
          hashTree = map(hashTree, (h) => {
            if (h.preCommands && h.preCommands.length) {
              index = findIndex(h.preCommands, { id: id });
              if (index != -1) {
                return true;
              } else {
                let find = false;
                forEach(h.preCommands, (c) => {
                  if (c.hashTree) {
                    index = findIndex(c.hashTree, { id: id });
                    if (index != -1) {
                      find = true;
                    }
                  }
                });
                return find;
              }
            } else if (h.postCommands && h.postCommands.length) {
              index = findIndex(h.postCommands, { id: id });
              if (index != -1) {
                return true;
              } else {
                let find = false;
                forEach(h.postCommands, (c) => {
                  if (c.hashTree) {
                    index = findIndex(c.hashTree, { id: id });
                    if (index != -1) {
                      find = true;
                    }
                  }
                });
                return find;
              }
            } else {
              return false;
            }
          });
        }
      }
    },
    getWsProjects() {
      getOwnerProjects().then(async (res) => {
        this.projectList = res.data;
      });
    },
    showPopover() {
      this.envResult.loading = true;
      this.getEnv(JSON.stringify(this.currentScenario.scenarioDefinition)).then(
        () => {
          this.$refs.envPopover.openEnvSelect();
          this.envResult.loading = false;
        }
      );
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
      this.$emit("setProjectEnvMap", projectEnvMap);
    },
    setEnvGroup() {
      //todo
    },
    getEnv(definition) {
      return new Promise((resolve) => {
        getUiScenarioEnv({ definition: definition }).then((res) => {
          if (res.data) {
            this.projectIds = new Set(res.data.projectIds);
            this.projectIds.add(this.projectId);
            if (this.currentScenario && this.currentScenario.env) {
              let temSet = new Set(
                Object.keys(JSON.parse(this.currentScenario.env))
              );
              temSet.forEach((t) => this.projectIds.add(t));
            }
          }
          resolve();
        });
      });
    },
    checkEnvBeforeSave() {
      // if (_.size(this.projectEnvMap) == 0) {
      //   this.$warning(this.$t("ui.pls_select_env"));
      //   return false;
      // }
      return true;
    },
  }
};
</script>

<style scoped>
.el-col {
  line-height: 30px;
}

.scenario_total {
  border-bottom: 1px solid #303133;
}

:deep(.el-button-group) {
  display: flex !important;
  justify-content: flex-start;
  margin-top: 1px;
  margin-right: 5px;
}

:deep(.el-button--mini),
.el-button--mini.is-round {
  padding: 7px 7px;
}

.scenario-container {
  display: flex;
}

.performance-opt,
.browser-opt,
.save-opt,
.refresh-opt {
  margin-right: 10px;
}

.performance-opt {
  min-width: 97px;
}

.debug-opt {
  margin: 1px 5px;
}

.env-bar {
  margin: 0px 5px;
}

.stop-opt {
  width: 56px;
  margin-right: 5px;
}

.ht-btn-confirm {
  width: 56px;
}

.refresh-btn {
  width: 44px;
}
</style>
