<template>
  <!-- 场景步骤内容 -->
  <div v-loading="loading">
    <el-row style="margin: 5px">
      <el-col
        :span="1"
        class="ms-col-one ms-font"
        v-show="scenarioDefinition.length > 0"
      >
        <el-tooltip
          :content="$t('test_track.case.batch_operate')"
          placement="top"
          effect="light"
          v-show="!isBatchProcess"
        >
          <font-awesome-icon
            class="ms-batch-btn"
            :icon="['fa', 'bars']"
            v-prevent-re-click
            @click="batchProcessing"
          />
        </el-tooltip>
        <div class="ms-batch-opt">
          <el-checkbox
            v-show="isBatchProcess"
            v-model="isCheckedAll"
            @change="checkedAll"
          />
          <el-tooltip
            :content="$t('commons.cancel')"
            placement="top"
            effect="light"
            v-show="isBatchProcess"
          >
            <font-awesome-icon
              class="ms-batch-btn"
              :icon="['fa', 'times']"
              v-prevent-re-click
              @click="cancelBatchProcessing"
            />
          </el-tooltip>
        </div>
      </el-col>
    </el-row>

    <vue-easy-tree
      v-if="showHideTree && scenarioDefinition"
      ref="stepTree"
      node-key="id"
      height="calc(100% - 220px)"
      :minItemSize="38"
      :props="props"
      :data="scenarioDefinition"
      :default-expanded-keys="expandedNode"
      :expand-on-click-node="false"
      :allow-drop="checkAllowDrop"
      :show-checkbox="isBatchProcess"
      :current-node-key="currentKey"
      :allow-drag="checkAllowDrag"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      @node-drag-end="allowDrag"
      @node-click="nodeClick"
      @check-change="chooseHeadsUp"
      isDynamic
      highlight-current
      draggable
    >
      <span class="custom-tree-node father" slot-scope="{ node, data }">
        <!-- 批量操作 -->
        <div
          :class="
            data.checkBox ? 'custom-tree-node-hide' : 'custom-tree-node-col'
          "
          style="padding-left: 0px; padding-right: 0px"
          v-if="
            showAllNodeBtn ||
            (data.hashTree && data.hashTree.length === 0) ||
            data.isLeaf
          "
        >
          <show-more-btn
            class="show-more-btn-info"
            :is-show="node.checked"
            :buttons="batchOperators"
            :size="selectDataCounts"
            :has-showed="isShowTip(node) !== node.id"
          />
        </div>
        <div class="debug-result-icon" v-if="data.debugResult">
          <i
            v-if="isSuccessResult(data.debugResult)"
            class="el-icon-success result-icon"
          ></i>
          <i v-else class="el-icon-error result-icon"></i>
        </div>
        <div v-else-if="hasResult && data.type !== 'scenario'"></div>
        <!-- 步骤组件-->
        <component-config
          :refNodeIdCahceMap="refNodeIdCahceMap"
          :isMax="true"
          :type="data.type"
          :scenario="handleData(data)"
          :response="response"
          :currentScenario="currentScenario"
          :currentEnvironmentId="currentEnvironmentId"
          :node="node"
          :project-list="projectList"
          :env-map="projectEnvMap"
          :message="message"
          :show-btn="false"
          @remove="remove"
          @copyRow="copyRow"
          @runScenario="handleRunScenario"
          @stopScenario="stopScenario"
          @suggestClick="suggestClick"
          @refReload="reload"
          @openScenario="openScenario"

        />
      </span>
    </vue-easy-tree>

    <!-- 批量编辑步骤操作 -->
    <div class="batch-edit-container">
      <el-dialog
        :title="dialogTitle"
        :visible.sync="stepDialogVisible"
        width="30%"
        class="batch-edit-dialog"
        :destroy-on-close="true"
        @close="handleClose"
      >
        <!-- 更多高级设置选项 -->
        <el-form
          :model="stepBatchEditForm"
          label-position="right"
          label-width="180px"
          size="medium"
          :rules="stepBatchEditFormRules"
          ref="stepBatchEditForm"
        >
          <el-form-item :label="$t('ui.more_config_options')" prop="type">
            <el-select
              v-model="stepBatchEditForm.type"
              size="mini"
              style="width: 80%"
            >
              <el-option
                v-for="(option, index) in options"
                :key="index"
                :value="option.id"
                :label="option.name"
              >
                {{ option.name }}
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 详情配置 -->
          <el-form-item
            :label="$t('ui.wait_element_timeout')"
            prop="secondsWaitElement"
            v-if="stepBatchEditForm.type === 1"
          >
            <!-- 超时时间配置 -->

            <div class="timeout-row">
              <el-input-number
                v-model="stepBatchEditForm.secondsWaitElement"
                class="time-input"
                size="mini"
                :min="0"
                :step="1000"
                style="width: 80%"
              />
              ms

              <el-tooltip class="item" effect="dark" placement="right">
                <div slot="content">
                  如果步骤涉及的元素定位在一定时间内无法找到元素，将会提示超时错误。<br />
                  (注：若超时错误的日志显示会四舍五入成
                  秒，但不影响等待具体时长)
                </div>
                <i
                  :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
                  class="el-icon-info"
                ></i>
              </el-tooltip>
            </div>
          </el-form-item>

          <el-form-item
            :label="$t('ui.updated_config_info')"
            prop="ignoreAssertFail"
            v-if="stepBatchEditForm.type === 4"
          >
            <!-- 断言配置 -->
            <div class="assert-config-row">
              <el-checkbox v-model="stepBatchEditForm.ignoreAssertFail"
                >{{ $t("ui.fail_over") }}
              </el-checkbox>
            </div>
          </el-form-item>

          <el-form-item
            :label="$t('ui.updated_config_info')"
            prop="ignoreFail"
            v-if="stepBatchEditForm.type === 3"
          >
            <!-- 错误处理 -->
            <div class="error-handle-row">
              <el-select
                v-model="stepBatchEditForm.ignoreFail"
                size="mini"
                style="width: 80%"
              >
                <el-option
                  v-for="item in ignoreFailTypes"
                  :key="item.label"
                  :label="$t('ui.' + item.label)"
                  :value="item.value"
                />
              </el-select>
            </div>
          </el-form-item>

          <el-form-item
            :label="$t('ui.updated_config_info')"
            prop="screenshotConfig"
            v-if="stepBatchEditForm.type === 2"
          >
            <!-- 截图配置 -->
            <div class="screenshot-row">
              <el-select
                v-model="stepBatchEditForm.screenshotConfig"
                size="mini"
                style="width: 80%"
              >
                <el-option
                  v-for="item in screenshotConfigOptions"
                  :key="item.label"
                  :label="$t(item.label)"
                  :value="item.value"
                />
              </el-select>
              <el-tooltip class="item" effect="dark" placement="right">
                <div slot="content">
                  当前步骤截图: 场景步骤,执行后截图;<br />
                  出现异常截图:
                  当前步骤出现异常截图,包括场景步骤异常、数据提取和断言异常;<br />
                  不截图: 当前场景步骤不截图。
                </div>
                <i
                  :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
                  class="el-icon-info"
                ></i>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-form>

        <template v-slot:footer>
          <ms-dialog-footer
            @cancel="stepDialogVisible = false"
            @confirm="saveBatchEditStepResult"
          />
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import ScenarioEditBar from "./UiScenarioEditBar";
import UiScenarioEditTreeItem from "./UiScenarioEditTreeItem";
import { scenarioSort } from "@/api/api-automation";
import ComponentConfig from "@/business/automation/scenario/component/ComponentConfig";
import { getUiScenario } from "@/business/network/ui-scenario";
import { COMMAND_TYPE_COMBINATION } from "@/business/definition/command/command-type";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
// import PerformanceNodeTree from "./virtualNodeTree/tree";
import { useCommandStore } from "@/store";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import ParamProcessor from "@/business/definition/parameter-processor";
import { getUUID, objToStrMap } from "metersphere-frontend/src/utils";
import VueEasyTree from "@ba1q1/vue-easy-tree";

const commandStore = new useCommandStore();
export default {
  name: "UiScenarioEditTree",
  components: {
    ComponentConfig,
    ScenarioEditBar,
    MsContainer,
    MsMainContainer,
    UiScenarioEditTreeItem,
    ShowMoreBtn,
    // PerformanceNodeTree,
    MsDialogFooter,
    VueEasyTree,
  },
  props: {
    moduleOptions: Array,
    currentScenario: {},
    type: String,
    customNum: {
      type: Boolean,
      default: false,
    },
    paramDefinition: [],
    commandDefinition: Object,
    value: Object,
    dialogTitle: {
      type: String,
      default() {
        return this.$t("test_track.case.batch_operate");
      },
    },
  },
  data() {
    return {
      ignoreFailTypes: [
        {
          label: "not_ignore_fail",
          value: false,
        },
        {
          label: "ignore_fail",
          value: true,
        },
      ],
      screenshotConfigOptions: [
        {
          label: this.$t("ui.current_step_screenshot"),
          value: 0,
        },
        {
          label: this.$t("ui.error_step_screenshot"),
          value: 1,
        },
        {
          label: this.$t("ui.not_screentshot"),
          value: 2,
        },
      ],
      options: [
        {
          id: 1,
          name: this.$t("ui.wait_time_config"),
        },
        {
          id: 2,
          name: this.$t("ui.screenshot_config"),
        },
        {
          id: 3,
          name: this.$t("ui.error_handling"),
        },
        {
          id: 4,
          name: this.$t("ui.assertion_configuration"),
        },
      ],
      stepBatchEditForm: {
        type: 3,
        screenshotConfig: 1,
        ignoreFail: false,
        secondsWaitElement: 15000,
        ignoreAssertFail: false,
      },
      stepBatchEditFormRules: {
        type: {
          required: true,
          message: this.$t("test_track.case.please_select_attr"),
          trigger: ["blur", "change"],
        },
        secondsWaitElement: {
          required: true,
          message: this.$t("test_track.case.please_select_attr"),
          trigger: ["blur", "change"],
        },
        ignoreFail: {
          required: true,
          message: this.$t("test_track.case.please_select_attr"),
          trigger: ["blur", "change"],
        },
        screenshotConfig: {
          required: true,
          message: this.$t("test_track.case.please_select_attr"),
          trigger: ["blur", "change"],
        },
      },
      stepDialogVisible: false,
      currentKey: undefined,
      tipTemp: null,
      showAllNodeBtn: true,
      isCheckedAll: false,
      selectDataCounts: 0,
      isBatchProcess: false,
      showConfigButtonWithOutPermission: false,
      props: {
        label: "label",
        children: "hashTree",
      },
      moduleObj: {
        id: "id",
        label: "name",
      },
      currentEnvironmentId: "",
      scenario: {},
      loading: false,
      showHideTree: true,
      customizeVisible: false,
      isBtnHide: false,
      debugVisible: false,
      operatingElements: [],
      selectedTreeNode: undefined,
      selectedNode: undefined,
      expandedNode: [],
      checkedNode: [],
      scenarioDefinition: [],
      path: "/ui/automation/create",
      debugData: {},
      reportId: "",
      enableCookieShare: false,
      newEnableCookieShare: false,
      globalOptions: {
        spacing: 30,
      },
      response: {},
      projectIds: new Set(),
      projectEnvMap: new Map(),
      projectList: [],
      debugResult: new Map(),
      hasResult: false,
      isFullUrl: true,
      stepEnable: true,
      debug: false,
      saved: false,
      debugLoading: false,
      reqTotal: 0,
      reqSuccess: 0,
      reqError: 0,
      reqTotalTime: 0,
      reloadDebug: "",
      stopDebug: "",
      isTop: false,
      stepSize: 0,
      message: "",
      buttonData: [],
      stepFilter: new Map([
        ["MsUiCommand", []],
        ["UiScenario", ["MsUiCommand"]],
        ["scenario", ["MsUiCommand"]],
      ]),
      clearMessage: "",
      runScenario: undefined,
      showFollow: false,
      versionData: [],
      newData: [],
      oldData: [],
      dialogVisible: false,
      newScenarioDefinition: [],
      oldScenarioDefinition: [],
      currentItem: {},
      pluginDelStep: false,
      batchOperators: [
        {
          name: this.$t("api_test.automation.bulk_activation_steps"),
          handleClick: this.enableAll,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
        {
          name: this.$t("api_test.automation.batch_disable_steps"),
          handleClick: this.disableAll,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
        {
          name: this.$t("api_test.automation.open_expansion"),
          handleClick: this.openExpansion,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
        {
          name: this.$t("api_test.automation.close_expansion"),
          handleClick: this.closeExpansion,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
        {
          name: this.$t("api_test.definition.request.batch_delete") + "步骤",
          handleClick: this.handleDeleteBatch,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
        {
          name: this.$t("ui.batch_editing_steps"),
          handleClick: this.handleEditBatch,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
        {
          name: this.$t("ui.create_custom_command_label"),
          handleClick: this.createCustomCommand,
          permissions: ["PROJECT_API_SCENARIO:READ+DELETE"],
        },
      ],
      commandStore: commandStore,
      refNodeIdCahceMap: new Map(),
      selfRefNodeIdChacheMap: new Map(),
      nodeCache: new Map(),
      tempStepTreeCache: [],
      stepTreeCache: [],
      GENARATE_REF_TIMER: -1,
    };
  },
  created() {
    this.$EventBus.$on("autoOpenExpansion", this.autoOpenExpansion);
    this.$EventBus.$on("handleScenarioREFEvent", this.generateREFNodeIds);
  },
  beforeDestroy() {
    this.$EventBus.$off("autoOpenExpansion", this.autoOpenExpansion);
    this.$EventBus.$off("handleScenarioREFEvent", this.generateREFNodeIds);
  },
  mounted() {
    if (!(commandStore.scenarioMap instanceof Map)) {
      commandStore.scenarioMap = new Map();
    }
    this.getUiScenario();
  },
  watch: {
    "commandStore.refreshUiScenario"(value) {
      if (value) {
        this.getUiScenario();
        commandStore.refreshUiScenario = false;
      }
    },
    "commandStore.selectCommand": function (o) {
      if (o) {
        this.$nextTick(() => {
          this.currentKey = o.id;
          this.$refs.stepTree.setCurrentKey(this.currentKey);
          this.generateREFNodeIds();
        });
      }
    },
  },
  methods: {
    saveBatchEditStepResult() {
      //处理 批量更改步骤
      this.$refs.stepBatchEditForm.validate((valid) => {
        if (valid) {
          this.handleBatchEditInfo();
          this.$success(this.$t("ui.config_success"));
          this.stepDialogVisible = false;
          return true;
        } else {
          this.$warning("请检查必填项！");
          return false;
        }
      });
    },
    handleBatchEditInfo() {
      let checkedIds = this.getAllResourceIds();
      //查看当前类型
      switch (this.stepBatchEditForm.type) {
        //超时时间
        case 1:
          this.handleTimeoutData(checkedIds);
          break;
        //截图
        case 2:
          this.handleScreentshotData(checkedIds);
          break;
        //错误处理
        case 3:
          this.handleErrorHandleData(checkedIds);
          break;
        // 断言配置
        case 4:
          this.handleAssertConfigData(checkedIds);
          break;
      }
    },
    handleAssertConfigData(checkedIds) {
      this.fillPreOrPostConfigData(
        this.scenarioDefinition,
        checkedIds,
        "failOver",
        this.stepBatchEditForm.ignoreAssertFail
      );
    },
    doFillPreOrPostConfigData(
      scenarioDefinition,
      checkedIds,
      configKey,
      configValue
    ) {
      if (!scenarioDefinition || scenarioDefinition.length <= 0) {
        return;
      }
      //处理 前后置断言信息
      for (let i = 0; i < scenarioDefinition.length; i++) {
        let item = scenarioDefinition[i];
        if (item.viewType !== "validation") {
          continue;
        }
        if (item.hashTree && item.hashTree.length > 0) {
          //此处注意忽略 弹出文本 并且只过滤断言
          for (let j = 0; j < item.hashTree.length; j++) {
            let val = item.hashTree[j];
            if (!val.vo || val.vo.type === "ValidateText") {
              continue;
            }
            if (Object.prototype.hasOwnProperty.call(val.vo, configKey)) {
              val.vo[configKey] = configValue;
            }
          }
        }
      }
    },
    fillPreOrPostConfigData(
      scenarioDefinition,
      checkedIds,
      configKey,
      configValue
    ) {
      if (!scenarioDefinition || scenarioDefinition.length <= 0) {
        return;
      }
      scenarioDefinition.forEach((item) => {
        //首先检测是否存在 checkIds
        if (item.referenced === "REF" || !checkedIds.includes(item.id)) {
          return;
        }

        //只有后置才有断言
        if (item.postCommands) {
          this.doFillPreOrPostConfigData(
            item.postCommands,
            checkedIds,
            configKey,
            configValue
          );
        }

        if (item.hashTree) {
          this.fillPreOrPostConfigData(
            item.hashTree,
            checkedIds,
            configKey,
            configValue
          );
        }
      });
    },
    handleTimeoutData(checkedIds) {
      this.fillHandleConfig(
        this.scenarioDefinition,
        checkedIds,
        "secondsWaitElement",
        this.stepBatchEditForm.secondsWaitElement
      );
    },
    handleScreentshotData(checkedIds) {
      this.fillHandleConfig(
        this.scenarioDefinition,
        checkedIds,
        "screenshotConfig",
        this.stepBatchEditForm.screenshotConfig
      );
    },
    handleErrorHandleData(checkedIds) {
      this.fillHandleConfig(
        this.scenarioDefinition,
        checkedIds,
        "ignoreFail",
        this.stepBatchEditForm.ignoreFail
      );
    },
    fillHandleConfig(scenarioDefinition, checkedIds, configKey, configValue) {
      if (!scenarioDefinition || scenarioDefinition.length <= 0) {
        return;
      }
      scenarioDefinition.forEach((item) => {
        //首先检测是否存在 checkIds
        if (item.referenced === "REF" || !checkedIds.includes(item.id)) {
          return;
        }

        if (
          item.commandConfig &&
          Object.prototype.hasOwnProperty.call(item.commandConfig, configKey)
        ) {
          item.commandConfig[configKey] = configValue;
        }

        if (item.hashTree) {
          this.fillHandleConfig(
            item.hashTree,
            checkedIds,
            configKey,
            configValue
          );
        }
      });
    },
    handleEditBatch() {
      // scenarioDefinition
      let nodes = this.$refs.stepTree.getCheckedNodes() || [];
      this.stepDialogVisible = true;
    },
    handleClose() {
      //this.stepDialogVisible = false;
    },
    handleNodeRel() {
      if (!this.tempStepTreeCache) {
        return;
      }
      this.tempStepTreeCache.forEach((e) => {
        if (!this.nodeCache.get(e.id)) {
          this.stepTreeCache.push(e);
          //将e的所有id加入缓存，包含hashTree
          this.setNodeIdToCache(e);
        }
      });
    },
    setNodeIdToCache(e) {
      if (!e) {
        return;
      }
      this.nodeCache.set(e.id, e);
      if (this.refNodeIdCahceMap && this.refNodeIdCahceMap.get(e.id)) {
        e.readonly = true;
      }
      if (e.hashTree) {
        e.hashTree.forEach((v) => {
          this.setNodeIdToCache(v);
        });
      }
    },
    createCustomCommand() {
      //重置缓存
      this.tempStepTreeCache = this.$refs.stepTree.getCheckedNodes() || [];
      this.nodeCache = new Map();
      this.stepTreeCache = [];
      //处理父子结构
      this.handleNodeRel();
      //创建自定义指令 tab
      //将当前场景的场景变量带过去
      let vars = this.currentScenario.variables || [];
      this.$EventBus.$emit("createTab", {
        name: "add_custom_command",
        stepTree: this.stepTreeCache,
        variables: vars,
      });
    },
    generateREFNodeIds() {
      //节流
      clearTimeout(this.GENARATE_REF_TIMER);
      this.GENARATE_REF_TIMER = setTimeout(() => {
        //清除缓存
        this.refNodeIdCahceMap = new Map();
        this.selfRefNodeIdChacheMap = new Map();
        this.doGenerateREFNode(this.scenarioDefinition);
      }, 100);
    },
    doGenerateREFNode(hashTree) {
      if (!hashTree) {
        return;
      }

      hashTree.forEach((v) => {
        if (v.referenced && v.referenced === "REF") {
          //引用场景的所有子节点不能拖拽
          this.selfRefNodeIdChacheMap.set(v.id, v.resourceId);
          if (v.type === "customCommand") {
            v.pREF = true;
            // this.$set(v, "pREF", true);
          }
          this.collectChildToREFCache(v.hashTree);
        } else {
          this.doGenerateREFNode(v.hashTree);
        }
      });
    },
    collectChildToREFCache(hashTree) {
      if (!hashTree) {
        return;
      }

      hashTree.forEach((v) => {
        let val = v.resourceId ? v.resourceId : v.id;
        this.refNodeIdCahceMap.set(v.id, val);
        this.collectChildToREFCache(v.hashTree);
      });
    },
    checkAllowDrag(node) {
      return !this.refNodeIdCahceMap.get(node.data.id);
    },
    checkAllowDrop(draggingNode, dropNode, type) {
      //检测是否要放入引用的场景中
      if (
        this.selfRefNodeIdChacheMap.get(dropNode.data.id) ||
        this.refNodeIdCahceMap.get(dropNode.data.id)
      ) {
        if (draggingNode.parent && draggingNode.parent.level === 0) {
          return !this.refNodeIdCahceMap.get(dropNode.data.id);
        }
        return false;
      }

      if (type != "inner") {
        return true;
      } else if (dropNode.data.commandType === COMMAND_TYPE_COMBINATION) {
        return true;
      }

      return false;
    },
    handleData(data) {
      if (data.variableEnable === null || data.variableEnable === undefined) {
        data.variableEnable = true;
        // this.$set(data, "variableEnable", true);
      }
      return data;
    },
    isShowTip(node) {
      if (this.tipTemp === null) {
        this.tipTemp = node.id;
      }
      return this.tipTemp;
    },
    //-----------------------------------BATCH CHECK BOX ---------------------------------------
    batchProcessing() {
      this.isBatchProcess = true;
    },
    cancelBatchProcessing() {
      this.isBatchProcess = false;
      this.isCheckedAll = false;
      if (
        this.$refs.stepTree &&
        this.$refs.stepTree.root &&
        this.$refs.stepTree.root.childNodes
      ) {
        this.recursionChecked(false, this.$refs.stepTree.root.childNodes);
      }
      this.selectDataCounts = 0;
      this.commandTreeNode();
      this.reloadTreeStatus();
    },
    reloadTreeStatus() {
      this.$nextTick(() => {
        let row = { resourceId: "ms-reload-test" };
        if (this.$refs.stepTree && this.$refs.stepTree.root.data) {
          this.$refs.stepTree.root.data.push(row);
          this.$refs.stepTree.root.data.splice(
            this.$refs.stepTree.root.data.length - 1,
            1
          );
        }
      });
    },
    commandTreeNode(node, array) {
      if (!array) {
        array = this.scenarioDefinition;
      }
      let isLeaf = true;
      array.forEach((item) => {
        item.checkBox = false;
        if (
          isLeaf &&
          this.stepFilter.get("ALlSamplerStep") &&
          this.stepFilter.get("ALlSamplerStep").indexOf(item.type) === -1
        ) {
          isLeaf = false;
        }
        if (item.hashTree && item.hashTree.length > 0) {
          this.commandTreeNode(item, item.hashTree);
        } else {
          item.isLeaf = true;
        }
      });
      if (node) {
        node.isBatchProcess = this.isBatchProcess;
        node.checkBox = false;
        node.isLeaf = isLeaf;
      }
    },
    checkedAll(v) {
      if (
        this.$refs.stepTree &&
        this.$refs.stepTree.root &&
        this.$refs.stepTree.root.childNodes
      ) {
        this.recursionChecked(v, this.$refs.stepTree.root.childNodes);
      }
    },
    recursionChecked(v, array) {
      if (array) {
        array.forEach((item) => {
          if (item.childNodes && item.childNodes.length > 0) {
            this.recursionChecked(v, item.childNodes);
          }
          item.checked = v;
          //if (
          // item.data &&
          // item.data.type === "scenario" &&
          //  item.data.referenced === "REF"
          // ) {
          //  item.expanded = false;
          // }
        });
      }
    },
    chooseHeadsUp() {
      if (this.$refs.stepTree) {
        this.selectDataCounts = this.$refs.stepTree.getCheckedNodes().length;
      }
    },
    // -------------------------------OPT-------------------------------------
    enableAll() {
      this.stepEnable = true;
      let resourceIds = this.getAllResourceIds();
      this.stepStatus(resourceIds, this.scenarioDefinition);
    },
    disableAll() {
      this.stepEnable = false;
      let resourceIds = this.getAllResourceIds();
      this.stepStatus(resourceIds, this.scenarioDefinition);
    },
    handleDeleteBatch() {
      this.$alert(this.$t("test_track.module.delete_batch_confirm"), "", {
        confirmButtonText: this.$t("commons.confirm"),
        callback: (action) => {
          if (action === "confirm") {
            this.getAllResourceIds().forEach((item) => {
              this.recursionDelete(item, this.scenarioDefinition);
            });
            this.$emit("changeCurrentCommand", {});
            commandStore.selectCommand = {};
            commandStore.selectStep = {};
            this.sort();
            this.forceRerender();
            this.cancelBatchProcessing();
          }
        },
      });
    },
    autoOpenExpansion(resourceIds) {
      this.expandedStatus = true;
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
      this.recursionExpansion(resourceIds, this.$refs.stepTree.root.childNodes);
    },
    openExpansion() {
      this.expandedStatus = true;
      let rootResourceId = this.checkALevelChecked();
      let resourceIds = [];
      if (rootResourceId.length > 0) {
        resourceIds = rootResourceId;
      } else {
        resourceIds = this.getAllResourceIds();
      }
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
      this.recursionExpansion(resourceIds, this.$refs.stepTree.root.childNodes);
    },
    closeExpansion() {
      this.expandedStatus = false;
      let resourceIds = this.getAllResourceIds();
      this.changeNodeStatus(resourceIds, this.scenarioDefinition);
      this.recursionExpansion(resourceIds, this.$refs.stepTree.root.childNodes);
    },
    recursionDelete(resourceId, nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (resourceId === nodes[i].id) {
            nodes.splice(i, 1);
          } else {
            if (
              nodes[i].hashTree != undefined &&
              nodes[i].hashTree.length > 0
            ) {
              this.recursionDelete(resourceId, nodes[i].hashTree);
            }
          }
        }
      }
    },
    changeNodeStatus(resourceIds, nodes) {
      for (let i in nodes) {
        // && !(nodes[i].type === "scenario" && nodes[i].referenced === "REF"
        if (nodes[i]) {
          if (resourceIds.indexOf(nodes[i].id) !== -1) {
            nodes[i].active = this.expandedStatus;
          }
          if (
            nodes[i].hashTree &&
            nodes[i].hashTree.length > 0 &&
            !this.expandedStatus
          ) {
            this.changeNodeStatus(resourceIds, nodes[i].hashTree);
          }
        }
      }
    },
    checkALevelChecked() {
      let empty = [];
      if (this.$refs.stepTree) {
        let resourceIds = [];
        this.$refs.stepTree.root.childNodes.forEach((item) => {
          if (item.checked) {
            resourceIds.push(item.data.id);
          }
        });
        if (resourceIds.length > 20) {
          resourceIds = resourceIds.slice(0, 20);
          //this.$warning(this.$t("api_test.automation.open_check_message"));
        }
        return resourceIds;
      }
      return empty;
    },
    recursionExpansion(resourceIds, array) {
      if (array) {
        array.forEach((item) => {
          if (
            item.data &&
            item.data.type === "scenario" &&
            item.data.referenced === "REF"
          ) {
            item.expanded = this.expandedStatus;
          } else {
            if (resourceIds.indexOf(item.data.id) !== -1) {
              item.expanded = this.expandedStatus;
            }
          }
          if (item.childNodes && item.childNodes.length > 0) {
            this.recursionExpansion(resourceIds, item.childNodes);
          }
        });
      }
    },
    getAllResourceIds() {
      if (this.$refs.stepTree) {
        return this.$refs.stepTree.getCheckedKeys();
      }
      return [];
    },
    forceRerender() {
      this.$nextTick(() => {
        commandStore.forceRerenderIndex = getUUID();
      });
    },
    stepStatus(resourceIds, nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (resourceIds.indexOf(nodes[i].id) !== -1) {
            nodes[i].enable = this.stepEnable;
          }
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.stepStatus(resourceIds, nodes[i].hashTree);
          }
        }
      }
    },
    // -------------------------------------------------------------------------
    show(data) {
      window.alert(data);
    },
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
      if (this.currentScenario.variables) {
        size += this.currentScenario.variables.length;
      }
      if (
        this.currentScenario.headers &&
        this.currentScenario.headers.length > 1
      ) {
        size += this.currentScenario.headers.length - 1;
      }
      return size;
    },
    nodeClick(data, node) {
      this.selectedTreeNode = data;
      this.selectedNode = node;
      data.expanded = node.expanded;
      //readonly
      if (this.refNodeIdCahceMap && this.refNodeIdCahceMap.get(data.id)) {
        data.readonly = true;
        this.$nextTick(() => {
          data.disabled = false;
        });
        data.disabled = false;
      }
      if (
        this.selfRefNodeIdChacheMap &&
        this.selfRefNodeIdChacheMap.get(data.id) &&
        data.type === "customCommand"
      ) {
        data.pREF = true;
        // this.$set(data, "pREF", true);
      } else {
        data.pREF = false;
        // this.$set(data, "pREF", false);
      }
      commandStore.selectCommand = data;
      this.$emit("changeCurrentCommand", data);
    },
    suggestClick(node) {
      this.response = {};
      if (node.parent && node.parent.data.requestResult) {
        this.response = node.parent.data.requestResult[0];
      }
    },
    runDebug(runScenario) {
      if (this.scenarioDefinition.length < 1) {
        return;
      }
      this.stopDebug = "";
      this.clearDebug();
      this.validatePluginData(this.scenarioDefinition);
      if (this.pluginDelStep) {
        this.$error("场景包含插件步骤，对应场景已经删除不能调试！");
        return;
      }
      this.clearResult(this.scenarioDefinition);
      this.clearNodeStatus(this.$refs.stepTree.root.childNodes);
      this.saved = runScenario && runScenario.stepScenario ? false : true;
      /*触发执行操作*/
      this.$refs.currentScenario.validate(async (valid) => {
        if (valid) {
          let definition = JSON.parse(JSON.stringify(this.currentScenario));
          definition.hashTree = this.scenarioDefinition;
          await this.getEnv(JSON.stringify(definition));
          await this.$refs.envPopover.initEnv();
          const sign = await this.$refs.envPopover.checkEnv(this.isFullUrl);
          if (!sign) {
            this.buttonIsLoading = false;
            this.clearMessage = getUUID().substring(0, 8);
            return;
          }
          let scenario = undefined;
          if (runScenario && runScenario.type === "scenario") {
            scenario = runScenario;
            this.runScenario = runScenario;
          }
          //调试时不再保存
          this.debugData = {
            id: scenario ? scenario.id : this.currentScenario.id,
            name: scenario ? scenario.name : this.currentScenario.name,
            type: "scenario",
            variables: scenario
              ? scenario.variables
              : this.currentScenario.variables,
            referenced: "Created",
            enableCookieShare: this.enableCookieShare,
            headers: scenario ? scenario.headers : this.currentScenario.headers,
            environmentMap:
              scenario && scenario.environmentEnable
                ? scenario.environmentMap
                : this.projectEnvMap,
            hashTree: scenario ? scenario.hashTree : this.scenarioDefinition,
          };
          if (scenario && scenario.environmentEnable) {
            this.debugData.environmentEnable = scenario.environmentEnable;
            this.debugLoading = false;
          } else {
            this.debugLoading = true;
          }
          this.reportId = getUUID().substring(0, 8);
          this.debug = true;
          this.pluginDelStep = false;
        } else {
          this.clearMessage = getUUID().substring(0, 8);
        }
      });
    },
    handleParamStruct() {
      //处理入参 出参 信息
      let processor = new ParamProcessor();
      processor.convert(this.currentScenario);
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    detailRefresh(result) {
      // 把执行结果分发给各个请求
      this.debugResult = result;
      this.sort();
    },
    // 打开引用的场景
    openScenario(data) {
      this.$emit("openScenario", data);
    },
    stopScenario() {
      this.$emit("stopScenario");
    },
    editScenarioAdvance(data) {
      // 打开编辑参数设置对话框，并且传递修改的蓝笔引用值参数
      this.currentItem = data;
      this.$refs.scenarioVariableAdvance.open();
    },
    copyRow(row, node) {
      let data = this.copyScenarioRow(row, node);
      this.setCopyDataId(data);
      this.sort();
    },
    resetResourceId(hashTree) {
      hashTree.forEach((item) => {
        item.resourceId = getUUID();
        item["@json_id"] = getUUID();
        if (item.hashTree && item.hashTree.length > 0) {
          this.resetResourceId(item.hashTree);
        }
      });
    },
    copyScenarioRow(row, node) {
      if (!row || !node) {
        return;
      }
      const parent = node.parent;
      const hashTree = parent.data.hashTree || parent.data;
      // 深度复制
      let obj = JSON.parse(JSON.stringify(row));
      if (obj.hashTree && obj.hashTree.length > 0) {
        this.resetResourceId(obj.hashTree);
      }
      obj["@json_id"] = getUUID();
      obj.resourceId = getUUID();
      if (obj.name) {
        obj.name = obj.name + "_copy";
      }
      const index = hashTree.findIndex((d) => d.resourceId === row.resourceId);
      if (index != -1) {
        hashTree.splice(index + 1, 0, obj);
      } else {
        hashTree.push(obj);
      }
      return obj;
    },
    setCopyDataId(data) {
      this.setCopyDataChildId(data);
      data.id = getUUID();
    },
    setCopyDataChildId(rootNode) {
      if (rootNode.hashTree) {
        rootNode.hashTree.forEach((item) => {
          this.setCopyDataChildId(item);
          item.id = getUUID();
        });
      }
      this._setCopyDataChildId(rootNode, "postCommands");
      this._setCopyDataChildId(rootNode, "preCommands");
      this._setCopyDataChildId(rootNode, "appendCommands");
      this._setCopyDataChildId(rootNode, "atomicCommands");
    },
    _setCopyDataChildId(rootNode, prop) {
      if (rootNode[prop]) {
        rootNode[prop].forEach((item) => {
          this._setCopyDataChildId(item, "hashTree");
          item.id = getUUID();
        });
      }
    },
    remove(row, node) {
      let name = row === undefined || row.name === undefined ? "" : row.name;
      this.$alert(
        this.$t("api_test.definition.request.delete_confirm_step") +
          " " +
          name +
          " ？",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          callback: (action) => {
            if (action === "confirm") {
              let parent = node.parent;
              let hashTree = parent.data.hashTree || parent.data;
              let index = hashTree.findIndex(
                (d) =>
                  d.id != undefined && row.id != undefined && d.id === row.id
              );

              //删除空步骤
              while (
                parent &&
                parent.data &&
                parent.data.hashTree &&
                parent.data.hashTree.length <= 0 &&
                (parent.data.type === "scenario" ||
                  (parent.data.type === "customCommand" &&
                    !parent.data.useDebugger))
              ) {
                parent = parent.parent;
                if (!parent) {
                  break;
                }
                hashTree = parent.data.hashTree || parent.data;
                index = hashTree.findIndex(
                  (d) =>
                    d.id != undefined && row.id != undefined && d.id === row.id
                );
                hashTree.splice(index, 1);
              }

              if (index > 0) {
                this.nodeClick(hashTree[index - 1], node);
              }
              else {
                if (hashTree.length > 0) {
                  this.nodeClick(hashTree[0], node);
                } else {
                  this.$emit("changeCurrentCommand", {});
                }
              }
              hashTree.splice(index, 1);
              if (hashTree.length <= 0) {
                this.$emit("changeCurrentCommand", {});
              }
              this.sort();
              this.reload();
            }
          },
        }
      );
    },
    handleRunScenario() {},
    sort() {
      scenarioSort(this);
    },
    allowDrop(draggingNode, dropNode, dropType) {
      if (dropType != "inner") {
        return true;
      } else if (
        dropNode.data.referenced === "REF" &&
        dropNode.data.referenced === "Deleted" &&
        this.stepFilter
          .get(dropNode.data.type)
          .indexOf(draggingNode.data.type) != -1
      ) {
        return true;
      } else if (dropNode.data.commandType === COMMAND_TYPE_COMBINATION) {
        return true;
      }
      return false;
    },
    allowDrag(draggingNode, dropNode, dropType) {
      if (dropNode && draggingNode && dropType) {
        this.sort();
      }
    },
    nodeExpand(data, node) {
      if (
        data &&
        data.resourceId &&
        this.expandedNode.indexOf(data.resourceId) === -1
      ) {
        this.expandedNode.push(data.resourceId);
        data.expanded = node.expanded;
        let selectCommand = commandStore.selectCommand;
        if (selectCommand!=null) {
          selectCommand.expanded = node.expanded;
        }
        if (selectCommand && selectCommand.resourceId === data.resourceId) {
          this.changeCurrentNodeStore(selectCommand);
        } else {
          this.$emit("changeCurrentCommand", data);
          this.changeCurrentNodeStore(data);
        }
      }
    },
    nodeCollapse(data, node) {
      if (data && data.resourceId) {
        this.expandedNode.splice(this.expandedNode.indexOf(data.resourceId), 1);
        let selectCommand = commandStore.selectCommand;
        selectCommand.expanded = false;
        data.expanded = false;
        if (selectCommand.resourceId === data.resourceId) {
          this.changeCurrentNodeStore(selectCommand);
        } else {
          this.$emit("changeCurrentCommand", data);
          this.changeCurrentNodeStore(data);
        }
      }
    },
    changeCurrentNodeStore(data) {
      commandStore.selectCommand = data;
      commandStore.selectStep = data;
    },
    getUiScenario() {
      this.loading = true;
      this.stepEnable = true;
      if (
        this.currentScenario.tags !== undefined &&
        this.currentScenario.tags
      ) {
        if (!(this.currentScenario.tags instanceof Array)) {
          this.currentScenario.tags = JSON.parse(this.currentScenario.tags);
        }
      } else {
        this.currentScenario.tags = [];
        // this.$set(this.currentScenario, "tags", []);
      }
      if (!this.currentScenario.variables) {
        this.currentScenario.variables = [];
      }
      if (!this.currentScenario.headers) {
        this.currentScenario.headers = [];
      }
      //存储最开始的场景
      if (this.currentScenario.id) {
        getUiScenario(this.currentScenario.id).then((data) => {
          let isNew = false;
          if (data) {
            commandStore.currentScenario = data.data;
            let obj = data.scenarioDefinition;
            if (obj) {
              this.currentEnvironmentId = obj.environmentId;
              this.currentScenario.variables = obj.variables;
              this.value.scenarioDefinition = obj;
              obj.hashTree = obj.hashTree ? obj.hashTree : [];
              //复制需要打乱id
              if (this.currentScenario.copy) {
                this.resetId(obj.hashTree);
              }
              this.scenarioDefinition = obj.hashTree;
              this.handleParamStruct();
              if (this.currentScenario.variables) {
                this.currentScenario.variables.map((v, i) => {
                  return (v["num"] = i + 1);
                });
              }
              if (data.environmentJson) {
                this.projectEnvMap = objToStrMap(
                  JSON.parse(data.environmentJson)
                );
                this.$emit("setProjectEnvMap", this.projectEnvMap);
              }

              commandStore.scenarioDefinition = obj.hashTree;
              //默认选中第一个步骤
              this.$nextTick(() => {
                if (obj.hashTree.length) {
                  this.checkedNode = [].concat(obj.hashTree[0].id);
                  commandStore.selectCommand = obj.hashTree[0];
                  commandStore.selectStep = obj.hashTree[0];
                  this.$emit("changeCurrentCommand", obj.hashTree[0]);
                }
              });
            }
            this.path = "/ui/automation/update";
            if (this.currentScenario.copy) {
              this.path = "/ui/automation/create";
            }
          } else {
            commandStore.selectCommand = null;
            this.scenarioDefinition = this.value.scenarioDefinition
              ? this.value.scenarioDefinition.hashTree
              : [];
            isNew = true;
            this.handleParamStruct();
          }
          this.loading = false;
          this.sort();

          let tempScenario = JSON.parse(JSON.stringify(this.currentScenario));
          if (isNew) {
            //标记新增，关闭tab的时候给是否要保存的提示
            tempScenario.add = true;
          }
          commandStore.scenarioMap.set(this.currentScenario.id, tempScenario);
        });
      }
    },
    /**
     * 复制场景打乱步骤 id 防止报告内容不唯一
     * @param array
     * @param map
     */
    resetId(array) {
      array.forEach((item) => {
        if (item) {
          item.id = getUUID();
        } else {
          return;
        }
        if (item.postCommands && item.postCommands.length > 0) {
          this.resetId(item.postCommands);
        }
        if (item.preCommands && item.preCommands.length > 0) {
          this.resetId(item.preCommands);
        }
        if (item && item.hashTree && item.hashTree.length > 0) {
          this.resetId(item.hashTree);
        }
      });
    },
    dataProcessing(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].hashTree) {
            stepArray[i].hashTree = [];
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {
              type: "JSON",
              data: {
                xmlFollowAPI: false,
                jsonFollowAPI: false,
                json: [],
                xml: [],
              },
            };
          }
          if (stepArray[i].hashTree.length > 0) {
            this.dataProcessing(stepArray[i].hashTree);
          }
        }
      }
    },
    isSuccessResult(data) {
      this.hasResult = true;
      if (data instanceof Array) {
        for (let item of data) {
          if (!item.success) {
            return false;
          }
        }
        return true;
      } else {
        return data.success;
      }
    },
  },
};
</script>

<style scoped>
.ms-batch-opt {
  display: flex;
}

.ms-batch-btn {
  margin-left: 5px;
}

.debug-result-icon {
  padding-left: 5px;
}

.ms-batch-btn:hover {
  cursor: pointer;
  color: #6d317c;
}

.custom-tree-node-hide {
  display: flex;
  align-items: center;
  width: 0px;
}

.custom-tree-node-col {
  display: flex;
  align-items: center;
  width: 10px;
}

:deep(.ms-icon-more::before) {
  position: relative;
  top: 9px;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content){
  border: 1px solid red;
  border-radius: 4px;
  background-color: #f0f7ff;
}

.request-form {
  flex: 1;
}

.custom-tree-node {
  width: 1000px;
  display: flex;
  justify-content: flex-start;
}

.el-tree-node__content {
  align-items: center;
}

.father .child {
  display: none;
}

.father:hover .child {
  display: block;
}

:deep(.el-tree-node__content) {
  height: 100%;
  margin-top: 3px;
  vertical-align: center;
}

.result-icon {
  float: left;
  margin-right: 2px;
  line-height: 35px;
  margin-left: -13px;
}

.el-icon-success {
  color: #67c23a;
}

.el-icon-error {
  color: #f56c6c;
}

:deep(.el-tree-node__expand-icon.el-icon-caret-right) {
  padding-right: 0;
  padding-left: 0px;
}

:deep(.el-tree-node__expand-icon.is-leaf) {
  width: 0;
  padding-right: 0;
  padding-left: var(--nodeLeafPf);
  margin: 0;
}

.shortNodePadding {
  --nodeLeafPf: 6px;
}

.defaultNodePadding {
  --nodeLeafPf: 12px;
}

.step-error {
  border: 1px solid #f56c6c;
}
</style>
