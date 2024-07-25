<template>
  <ms-container
    v-if="renderComponent && hasAutomationPermission"
    v-loading="loading"
  >
    <ms-aside-container
      v-show="isAsideHidden"
      width="309px"
      class="scenario-aside"
      pageKey="UI_AUTOMATION"
    >
      <ui-scenario-module
        v-if="activeDom === 'scenario'"
        currentType="scenario"
        :show-operator="true"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @saveAsEdit="editScenario"
        @setModuleOptions="setModuleOptions"
        @setNodeTree="setNodeTree"
        @enableTrash="enableTrash"
        @exportSide="exportSide"
        @refreshAll="refreshAll"
        page-source="scenario"
        :type="'edit'"
        :total="total"
        :is-trash-data="trashEnable"
        ref="nodeTree"
      />
      <ui-custom-command-module
        v-show="activeDom === 'customCommand'"
        currentType="customCommand"
        :show-operator="true"
        @customNodeChange="customNodeChange"
        @refreshTable="refresh"
        @saveAsEdit="editScenario"
        @setCustomModuleOptions="setCustomModuleOptions"
        @setCustomNodeTree="setCustomNodeTree"
        @enableCustomTrash="enableCustomTrash"
        @exportSide="exportCustomSide"
        @refreshAll="refreshAll"
        page-source="scenario"
        :type="'edit'"
        :total="total"
        :is-trash-data="trashEnable"
        ref="customNodeTree"
      />
    </ms-aside-container>
    <ms-main-container>
      <el-tabs
        v-model="activeName"
        @tab-click="addTab"
        @tab-remove="closeConfirm"
      >
        <el-tab-pane name="trash" :closable="true" v-if="trashEnable">
          <span slot="label"
            ><font class="type-font"
              >{{
                activeDom === "scenario"
                  ? $t("ui.scenario_title")
                  : $t("ui.custom_command_title")
              }}
            </font>
            <font class="label-font">{{ $t("commons.trash") }}</font></span
          >
          <ui-scenario-list
            v-show="activeDom === 'scenario'"
            currentType="scenario"
            @getTrashCase="getTrashCase"
            @refreshTree="refreshTree"
            :module-tree="nodeTree"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :trash-enable="true"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            :custom-num="customNum"
            :init-api-table-opretion="initApiTableOpretion"
            @updateInitApiTableOpretion="updateInitApiTableOpretion"
            ref="uiTrashScenarioList"
          />
          <ui-custom-command-list
            v-show="activeDom === 'customCommand'"
            currentType="customCommand"
            @getTrashCase="getTrashCase"
            @refreshTree="refreshTree"
            :module-tree="customNodeTree"
            :module-options="customModuleOptions"
            :select-node-ids="selectCustomNodeIds"
            :trash-enable="true"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            :custom-num="customNum"
            :init-api-table-opretion="initApiTableOpretion"
            @updateInitApiTableOpretion="updateInitApiTableOpretion"
            ref="uiTrashCustomCommandList"
          />
        </el-tab-pane>
        <el-tab-pane name="default" :label="$t('ui.automation_list')">
          <ui-scenario-list
            v-show="!trashEnable && activeDom === 'scenario'"
            currentType="scenario"
            @getTrashCase="getTrashCase"
            @refreshTree="refreshTree"
            :module-tree="nodeTree"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :trash-enable="false"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            :custom-num="customNum"
            :init-api-table-opretion="initApiTableOpretion"
            @updateInitApiTableOpretion="updateInitApiTableOpretion"
            ref="uiScenarioList"
          >
            <toggle-tabs
              slot="tabChange"
              :activeDom.sync="activeDom"
              :tabList="tabList"
            ></toggle-tabs>
          </ui-scenario-list>
          <ui-custom-command-list
            v-show="!trashEnable && activeDom === 'customCommand'"
            currentType="customCommand"
            @getTrashCase="getTrashCase"
            @refreshTree="refreshTree"
            :module-tree="customNodeTree"
            :module-options="customModuleOptions"
            :select-node-ids="selectCustomNodeIds"
            :trash-enable="false"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            :custom-num="customNum"
            :init-api-table-opretion="initApiTableOpretion"
            @updateInitApiTableOpretion="updateInitApiTableOpretion"
            ref="uiCustomCommandList"
          >
            <toggle-tabs
              slot="tabChange"
              :activeDom.sync="activeDom"
              :tabList="tabList"
            ></toggle-tabs>
          </ui-custom-command-list>
        </el-tab-pane>
        <el-tab-pane
          :key="index"
          v-for="(item, index) in tabs"
          :name="item.name"
          closable
        >
          <span slot="label"
            ><font class="type-font"
              >{{
                item.scenarioType === "scenario"
                  ? $t("ui.scenario_title")
                  : $t("ui.custom_command_title")
              }}
            </font>
            <font class="label-font">{{ item.label }}</font></span
          >
          <ui-scenario-edit
            v-if="item.scenarioType === 'scenario'"
            currentType="scenario"
            :currentScenario="item.currentScenario"
            :key="item.currentScenario.id"
            :custom-num="customNum"
            :moduleOptions="moduleOptions"
            :currentTabId="currentTabId"
            @refresh="refresh"
            @openScenario="editScenario"
            @setModuleOptions="setModuleOptions"
            @closePage="closePage"
            ref="autoScenarioConfig"
          />
          <ui-custom-command-edit
            v-if="item.scenarioType === 'customCommand'"
            currentType="customCommand"
            :currentScenario="item.currentScenario"
            :key="item.currentScenario.id"
            :custom-num="customNum"
            :currentTabId="currentTabId"
            :moduleOptions="customModuleOptions"
            @refresh="refresh"
            @openScenario="editScenario"
            @closePage="closePage"
            ref="autoScenarioConfig"
          />
          <template v-slot:version>
            <version-select
              v-xpack
              :project-id="projectId"
              @changeVersion="changeVersion"
            />
          </template>
        </el-tab-pane>
        <el-tab-pane
          name="add"
          v-if="hasPermission('PROJECT_UI_SCENARIO:READ+CREATE')"
        >
          <template v-slot:label>
            <el-dropdown @command="handleCommand">
              <el-button type="primary" plain icon="el-icon-plus" size="mini" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  command="ADD"
                  v-permission="['PROJECT_UI_SCENARIO:READ+CREATE']"
                >
                  {{ $t("api_test.automation.add_scenario") }}
                </el-dropdown-item>
                <el-dropdown-item
                  command="ADD_CUSTOM_COMMAND"
                  v-permission="['PROJECT_UI_SCENARIO:READ+CREATE']"
                >
                  {{ $t("ui.create_custom_command") }}
                </el-dropdown-item>
                <el-dropdown-item command="CLOSE_ALL"
                  >{{ $t("api_test.definition.request.close_all_label") }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-tab-pane>
      </el-tabs>
    </ms-main-container>
  </ms-container>
</template>

<script>
import ToggleTabs from "@/business/automation/custom-commands/ToggleTabs";
import UiCustomCommandList from "@/business/automation/custom-commands/UiCustomCommandList";
import UiCustomCommandModule from "@/business/automation/custom-commands/UiCustomCommandModule";
import UiCustomCommandEdit from "@/business/automation/custom-commands/edit/UiCustomCommandEdit";
import { PROJECT_ID } from "@/api/constants";
import MsTabButton from "metersphere-frontend/src/components/MsTabButton";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import SelectMenu from "@/business/element/components/common/SelectMenu";
import UiScenarioList from "./scenario/UiScenarioList";
import UiScenarioModule from "./scenario/UiScenarioModule";
import UiScenarioEdit from "./scenario/edit/UiScenarioEdit";
import { useCommandStore } from "@/store";
import { getUiAutomationList } from "@/business/automation/ui-automation";
import deepCompareScenario from "@/common/js/deep-compare";
import { getUUID } from "metersphere-frontend/src/utils";
import {
  getCurrentProjectID,
  getCurrentUserId,
} from "metersphere-frontend/src/utils/token";
import {
  hasPermission,
  hasPermissions,
} from "metersphere-frontend/src/utils/permission";
const commandStore = new useCommandStore();
const VersionSelect = {};
export default {
  name: "UiAutomation",
  components: {
    UiScenarioEdit,
    MsTabButton,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    SelectMenu,
    VersionSelect: VersionSelect.default,
    UiScenarioList,
    UiScenarioModule,
    ToggleTabs,
    UiCustomCommandList,
    UiCustomCommandModule,
    UiCustomCommandEdit,
  },
  comments: {},
  computed: {
    checkRedirectID: function () {
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam, this.$route.params.dataType);
      return redirectIDParam;
    },
    isRedirectEdit: function () {
      let redirectParam = this.$route.params.dataSelectRange
        ? this.$route.params.dataSelectRange
        : this.$route.query.dataSelectRange;
      this.checkRedirectEditPage(redirectParam, this.$route.params.dataType);
      return redirectParam;
    },
    isReadOnly() {
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
    isScenario() {
      return this.activeDom === "scenario";
    },
    isCustomCommand() {
      return this.activeDom === "customCommand";
    },
    hasAutomationPermission() {
      return hasPermissions("PROJECT_UI_SCENARIO:READ");
    },
  },
  data() {
    return {
      TRASH_TIMER: -1,
      CUSTOM_TRASH_TIMER: -1,
      total: 0,
      redirectID: "",
      renderComponent: true,
      isHide: true,
      activeName: "default",
      redirectFlag: "none",
      currentModule: null,
      moduleOptions: [],
      customModuleOptions: [],
      tabs: [],
      loading: false,
      trashEnable: false,
      selectNodeIds: [],
      selectCustomNodeIds: [],
      nodeTree: [],
      customNodeTree: [],
      currentModulePath: "",
      customNum: false,
      //影响API表格刷新的操作。 为了防止高频率刷新模块列表用。如果是模块更新而造成的表格刷新，则不回调模块刷新方法
      initApiTableOpretion: "init",
      isAsideHidden: true,
      defaultTab: "default",
      activeDom: "scenario",
      createType: "scenario",
      tabList: [
        {
          domKey: "scenario",
          tip: this.$t("ui.scenario_title"),
          content: this.$t("ui.scenario_title"),
          placement: "left",
          enable: true,
        },
        {
          domKey: "customCommand",
          tip: this.$t("ui.custom_command_title"),
          content: this.$t("ui.custom_command_title"),
          placement: "right",
          enable: true,
        },
      ],
      currentTabId:""
    };
  },
  created() {
    let projectId = this.$route.params.projectId
      ? this.$route.params.projectId
      : this.$route.query.projectId;
    if (projectId) {
      sessionStorage.setItem(PROJECT_ID, projectId);
    }
    sessionStorage.setItem("redirectUrl", "/ui/automation");
    this.$EventBus.$on("createTab", this.createTab);
  },
  beforeDestroy() {
    this.$EventBus.$off("createTab", this.createTab);
  },
  mounted() {
    this.getProject();
    this.getTrashCase();
    this.init();
  },
  watch: {
    activeDom() {
      this.getTrashCase();
      this.refreshAll();
      //重置nodeSelect
      this.resetNodeSelect();
    },
    trashEnable() {
      this.selectNodeIds = [];
    },
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    },
    $route(to, from) {
      //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
      if (to.path.indexOf("/ui/automation") == -1) {
        if (this.$refs && this.$refs.autoScenarioConfig) {
          this.$refs.autoScenarioConfig.forEach((item) => {
            item.removeListener();
          });
        }
      }
    },
    selectNodeIds() {
      if (!this.trashEnable) {
        this.activeName = "default";
      }
    },
    activeName() {
      this.isAsideHidden =
        this.activeName === "default" || this.activeName === "trash";
    },
  },
  methods: {
    hasPermission,
    resetNodeSelect() {
      this.selectNodeIds = [];
      this.selectCustomNodeIds = [];
    },
    createTab(tab) {
      this.addTab({ ...tab });
    },
    exportSide(nodeTree) {
      this.$refs.uiScenarioList.exportSide(nodeTree);
    },
    exportCustomSide(nodeTree) {
      this.$refs.uiCustomCommandList.exportSide(nodeTree);
    },
    checkRedirectEditPage(redirectParam) {
      if (redirectParam != null) {
        let selectParamArr = redirectParam.split("edit:");
        if (selectParamArr.length == 2) {
          let scenarioId = selectParamArr[1];
          //查找单条数据，跳转修改页面
          let dataType = this.$route.params.dataType;
          let param = {
            id: scenarioId,
            scenarioType: dataType,
          };
          getUiAutomationList(1, 1, param).then((response) => {
            let data = response.data;
            if (data != null) {
              //如果树未加载
              if (
                this.moduleOptions &&
                JSON.stringify(this.moduleOptions) === "{}"
              ) {
                this.nodeTreeListByType();
              }
              if (data.listObject && data.listObject.length > 0) {
                let row = data.listObject[0];
                if (
                  row != null &&
                  row.tags != "null" &&
                  row.tags != "" &&
                  row.tags != undefined
                ) {
                  if (
                    Object.prototype.toString
                      .call(row.tags)
                      .match(/\[object (\w+)\]/)[1]
                      .toLowerCase() !== "object" &&
                    Object.prototype.toString
                      .call(row.tags)
                      .match(/\[object (\w+)\]/)[1]
                      .toLowerCase() !== "array"
                  ) {
                    row.tags = JSON.parse(row.tags);
                  }
                }
                this.editScenario(row);
              }
            }
          });
        }
      }
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
      if (redirectIDParam != null) {
        if (this.redirectFlag == "none") {
          this.activeName = "default";
          this.addListener();
          this.redirectFlag = "redirected";
        }
      } else {
        this.redirectFlag = "none";
      }
    },
    getPath(id, arr) {
      if (id === null) {
        return null;
      }
      if (arr) {
        arr.forEach((item) => {
          if (item.id === id) {
            this.currentModulePath = item.path;
          }
          if (item.children && item.children.length > 0) {
            this.getPath(id, item.children);
          }
        });
      }
    },
    addTab(tab) {
      if (tab.currentScenario) {
        this.currentTabId = tab.currentScenario.id;
      } else {
        for (let i = 0; i < this.tabs.length; i++) {
          let tabOne = this.tabs[i];
          if (tab.name === tabOne.name) {
            this.currentTabId = tabOne.currentScenario.id;
          }
        }
      }
      //矫正createType
      if (tab.name === "add") {
        this.createType = "scenario";
      }

      this.trashEnable = tab.name === "trash";
      if (tab.name === "default") {
        this.refreshAll();
      } else if (tab.name === "trash") {
        if (this.isScenario) {
          this.$refs.uiTrashScenarioList.search();
        } else {
          this.$refs.uiTrashCustomCommandList.search();
        }
      }
      if (!this.projectId) {
        this.$warning(this.$t("commons.check_project_tip"));
        return;
      }
      this.currentModulePath = "";
      if (tab.name === "add" || tab.name === "add_custom_command") {
        let label =
          tab.name === "add"
            ? this.$t("api_test.automation.add_scenario")
            : this.$t("ui.create_custom_command");
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        let currentScenario = {
          resourceId: getUUID(),
          status: "Underway",
          principal: getCurrentUserId(),
          projectId: getCurrentProjectID(),
          apiScenarioModuleId: "default-module",
          id: getUUID(),
          modulePath: "/" + this.$t("commons.module_title"),
          level: "P0",
          scenarioDefinition: {
            hashTree: [],
            browser: "CHROME",
            headlessEnabled: true,
          },
          type: "add",
        };

        if (tab.variables) {
          currentScenario.variables = tab.variables;
        }
        if (tab.stepTree) {
          currentScenario.scenarioDefinition.hashTree = tab.stepTree;
          this.createType = "customCommand";
        }
        this.setDefaultAddModuleWithType(currentScenario);

        this.tabs.push({
          label,
          name,
          currentScenario,
          scenarioType: this.createType,
        });
      }
      if (tab.name === "edit") {
        let editType = tab.scenarioType ? tab.scenarioType : this.activeDom;
        let label = this.$t("api_test.automation.add_scenario");
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        label = tab.currentScenario.name;
        if (!tab.currentScenario.level) {
          tab.currentScenario.level = "P0";
        }
        tab.currentScenario.editType = "edit";
        this.tabs.push({
          label: label,
          name: name,
          currentScenario: tab.currentScenario,
          scenarioType: editType,
        });
      }
      if (this.$refs && this.$refs.autoScenarioConfig) {
        this.$refs.autoScenarioConfig.forEach((item) => {
          item.removeListener();
        }); //  删除所有tab的 ctrl + s 监听
        this.addListener();
      }

      if (tab.name === "default") {
        // 关闭报告页面
        if (
          this.$refs.uiScenarioList &&
          this.$refs.uiScenarioList.showReportVisible
        ) {
          this.$refs.uiScenarioList.showReportVisible = false;
          // 路由跳转，去除后缀
          this.$router.push("/ui/automation");
        }
      }
    },
    setDefaultAddModule(currentScenario, moduleId) {
      currentScenario.moduleId = moduleId;
      let targetModuleOptions =
        this.createType === "scenario"
          ? this.moduleOptions
          : this.customModuleOptions;
      this.getPath(moduleId, targetModuleOptions);
      currentScenario.modulePath = this.currentModulePath;
    },
    setDefaultAddModuleWithType(currentScenario) {
      let currentSelectNodeIds =
        this.createType === "scenario"
          ? this.selectNodeIds
          : this.selectCustomNodeIds;
      let currentNodeTree =
        this.createType === "scenario" ? this.nodeTree : this.customNodeTree;
      if (currentSelectNodeIds && currentSelectNodeIds.length > 0) {
        this.setDefaultAddModule(currentScenario, currentSelectNodeIds[0]);
      } else if (currentNodeTree && currentNodeTree.length > 0) {
        this.setDefaultAddModule(currentScenario, currentNodeTree[0].id);
      }
    },
    addListener() {
      let index = this.tabs.findIndex((item) => item.name === this.activeName); //  找到当前选中tab的index
      if (index != -1) {
        //  为当前选中的tab添加监听
        this.$nextTick(() => {
          let autoScenarioConfig = this.$refs.autoScenarioConfig[index];
          autoScenarioConfig.addListener();
          if (
            autoScenarioConfig.currentScenario &&
            autoScenarioConfig.currentScenario.scenarioDefinition
          ) {
            let scenarioDefinition =
              autoScenarioConfig.currentScenario.scenarioDefinition;
            if (
              scenarioDefinition.hashTree &&
              scenarioDefinition.hashTree.length > 0
            ) {
              commandStore.selectCommand = scenarioDefinition.hashTree[0];
            }
          }
        });
      }
    },
    handleTabClose() {
      let message = "";
      let commandTypeName = this.$t("ui.scenario_title");
      this.tabs.forEach((t) => {
        if (
          t &&
          commandStore.scenarioMap.has(t.currentScenario.id) &&
          !deepCompareScenario(
            t.currentScenario,
            commandStore.scenarioMap.get(t.currentScenario.id)
          )
        ) {
          if (t.scenarioType === "customCommand") {
            commandTypeName = this.$t("ui.custom_command_title");
          }
          message +=
            (t.currentScenario.name
              ? t.currentScenario.name
              : t.scenarioType === "scenario"
              ? this.$t("ui.unknown_scenario")
              : this.$t("ui.unknown_instruction")) + "，";
        }
      });
      if (message !== "") {
        let alertDesc = message.substr(0, message.length - 1)
          ? message.substr(0, message.length - 1)
          : "";
        this.$alert(
          commandTypeName +
            " [ " +
            alertDesc +
            " ] " +
            this.$t("commons.confirm_info"),
          "",
          {
            confirmButtonText: this.$t("commons.confirm"),
            cancelButtonText: this.$t("commons.cancel"),
            callback: (action) => {
              if (action === "confirm") {
                commandStore.scenarioMap.clear();
                commandStore.selectCommand = {};
                this.tabs = [];
                this.activeName = "default";
                this.refresh();
              }
            },
          }
        );
      } else {
        this.tabs = [];
        this.trashEnable = false;
        this.activeName = "default";
        this.refresh();
      }
    },
    handleCommand(e) {
      switch (e) {
        case "ADD":
          this.createType = "scenario";
          this.addTab({ name: "add" });
          break;
        case "ADD_CUSTOM_COMMAND":
          this.createType = "customCommand";
          this.addTab({ name: "add_custom_command" });
          break;
        case "CLOSE_ALL":
          this.handleTabClose();
          break;
        default:
          if (hasPermission("PROJECT_UI_SCENARIO:READ+CREATE")) {
            this.createType = "scenario";
            this.addTab({ name: "add" });
          }
          break;
      }
    },
    closePage(targetName) {
      this.tabs = this.tabs.filter((tab) => tab.label !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
        this.addListener(); //  自动切换当前标签时，也添加监听
      } else {
        this.activeName = "default";
      }
    },
    closeConfirm(targetName) {
      if (targetName === "trash") {
        this.selectNodeIds = [];
        this.trashEnable = false;
      } else {
        this.closeTabWithSave(targetName);
      }
    },
    closeTabWithSave(targetName) {
      let message = "";
      let t = this.tabs.filter((tab) => tab.name === targetName);
      let commandTypeName = this.$t("ui.scenario_title");
      this.tabs.forEach((t) => {
        if (
          t &&
          commandStore.scenarioMap.has(t.currentScenario.id) &&
          !deepCompareScenario(
            t.currentScenario,
            commandStore.scenarioMap.get(t.currentScenario.id)
          ) &&
          t.name === targetName
        ) {
          if (t.scenarioType === "customCommand") {
            commandTypeName = this.$t("ui.custom_command_title");
          }
          message +=
            (t.currentScenario.name
              ? t.currentScenario.name
              : t.scenarioType === "scenario"
              ? this.$t("ui.unknown_scenario")
              : this.$t("ui.unknown_instruction")) + "，";
        }
      });
      if (message !== "") {
        let alertDesc = message.substr(0, message.length - 1)
          ? message.substr(0, message.length - 1)
          : "";
        this.$alert(
          commandTypeName +
            " [ " +
            alertDesc +
            " ] " +
            this.$t("commons.confirm_info"),
          "",
          {
            confirmButtonText: this.$t("commons.confirm"),
            cancelButtonText: this.$t("commons.cancel"),
            callback: (action) => {
              if (action === "confirm") {
                this.removeTab(targetName);
                commandStore.scenarioMap.delete(t[0].currentScenario.id);
                this.refresh();
                // 关闭报告页面
                if (this.$refs.uiScenarioList.showReportVisible) {
                  this.$refs.uiScenarioList.showReportVisible = false;
                  // 路由跳转，去除后缀
                  this.$router.push("/ui/automation");
                }
              }
            },
          }
        );
      } else {
        this.removeTab(targetName);
        commandStore.scenarioMap.delete(t[0].currentScenario.id);
        this.refresh();
      }
    },
    removeTab(targetName) {
      this.tabs = this.tabs.filter((tab) => tab.name !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
        this.addListener(); //  自动切换当前标签时，也添加监听
      } else {
        this.activeName = "default";
      }
    },
    setTabLabel(data) {
      for (const tab of this.tabs) {
        if (tab.name === this.activeName) {
          tab.label = data.name;
          break;
        }
      }
    },
    selectModule(data) {
      this.currentModule = data;
    },
    saveScenario(data) {
      this.setTabLabel(data);
      this.searchByType();
    },
    refresh(data) {
      this.setTabTitle(data);
      this.searchByType();
      this.nodeTreeListByType();
    },
    refreshTree() {
      this.nodeTreeListByType();
    },
    refreshAll() {
      this.nodeTreeListByType();
      this.searchByType();
    },
    setTabTitle(data) {
      for (let index in this.tabs) {
        let tab = this.tabs[index];
        if (tab.name === this.activeName) {
          if (data) {
            tab.label = data.name;
          }
          break;
        }
      }
    },
    init() {
      let scenarioData = this.$route.params.scenarioData;
      if (scenarioData) {
        this.editScenario(scenarioData);
      }
    },
    editScenario(row) {
      const index = this.tabs.find(
        (p) =>
          p.currentScenario.id === row.id && p.currentScenario.copy === row.copy
      );
      if (!index) {
        let type = row.scenarioType ? row.scenarioType : undefined;
        if (this.moduleOptions.length > 0) {
          this.addTab({
            name: "edit",
            currentScenario: row,
            scenarioType: type,
          });
        } else {
          if (row.projectId) {
            this.loading = true;
            // 编辑场景前如果模块数据是空的，重新加载
            this.$get("/ui/scenario/module/list/" + row.projectId)
              .then((res) => {
                this.loading = false;
                if (res && res.data) {
                  this.moduleOptions = res.data;
                  this.addTab({
                    name: "edit",
                    currentScenario: row,
                    scenarioType: type,
                  });
                }
              })
              .catch(() => {
                this.loading = false;
                this.addTab({
                  name: "edit",
                  currentScenario: row,
                  scenarioType: type,
                });
              });
          } else {
            this.addTab({
              name: "edit",
              currentScenario: row,
              scenarioType: type,
            });
          }
        }
      } else {
        this.activeName = index.name;
      }
      this.createType = this.activeDom;
    },

    /**
     * module tree node change
     */
    nodeChange(node, nodeIds, pNodes) {
      this.initApiTableOpretion = "nodeChange";
      this.selectNodeIds = nodeIds;
      this.nodeTreeListByType();
    },
    customNodeChange(node, nodeIds, pNodes) {
      this.initApiTableOpretion = "nodeChange";
      this.selectCustomNodeIds = nodeIds;
      this.nodeTreeListByType();
    },
    setModuleOptions(data) {
      if (data && data.length) {
        data.forEach((node) => {
          node.name =
            node.name === "未规划场景"
              ? this.$t("ui.unplanned_scenario")
              : node.name;
          node.label =
            node.label === "未规划场景"
              ? this.$t("ui.unplanned_scenario")
              : node.label;
        });
      }
      this.moduleOptions = data;
    },
    setCustomModuleOptions(data) {
      if (data && data.length) {
        data.forEach((node) => {
          node.name =
            node.name === "未规划模块"
              ? this.$t("ui.unplanned_module")
              : node.name;
          node.label =
            node.label === "未规划模块"
              ? this.$t("ui.unplanned_module")
              : node.label;
        });
      }
      this.customModuleOptions = data;
    },
    setNodeTree(data) {
      this.nodeTree = data;
    },
    setCustomNodeTree(data) {
      this.customNodeTree = data;
    },

    /**
     * common options
     */
    getTrashCase() {
      if (!hasPermissions("PROJECT_UI_SCENARIO:READ")) {
        return;
      }
      let param = {};
      param.projectId = this.projectId;
      //增加类型
      param.scenarioType = this.activeDom;
      this.$post("/ui/automation/list/all/trash", param).then((response) => {
        this.total = response.data;
      });
    },
    getProject() {
      this.$get("/project/get/" + this.projectId).then((result) => {
        let data = result.data;
        if (data) {
          this.customNum = data.scenarioCustomNum;
        }
      });
    },

    changeSelectDataRangeAll(tableType) {
      this.$route.params.dataSelectRange = "all";
    },
    enableTrash(data) {
      clearTimeout(this.TRASH_TIMER);
      this.TRASH_TIMER = setTimeout(() => {
        this.activeName = "default";
        this.initApiTableOpretion = "enableTrash";
        this.trashEnable = data;
        if (data) {
          this.activeName = "trash";
          this.searchTrashByType();
        } else {
          this.activeName = "default";
          this.searchByType();
        }
        this.getTrashCase();
      }, 100);
    },
    enableCustomTrash(data) {
      clearTimeout(this.CUSTOM_TRASH_TIMER);
      this.CUSTOM_TRASH_TIMER = setTimeout(() => {
        this.activeName = "default";
        this.initApiTableOpretion = "enableTrash";
        this.trashEnable = data;
        if (data) {
          this.activeName = "trash";
          this.searchTrashByType();
        } else {
          this.activeName = "default";
          this.searchByType();
        }
        this.getTrashCase();
      }, 100);
    },

    updateInitApiTableOpretion(param) {
      this.initApiTableOpretion = param;
    },
    changeVersion(currentVersion) {
      this.changeVersionWithType(currentVersion);
      this.refresh();
    },

    //通过类型区分
    changeVersionWithType(currentVersion) {
      //场景情况
      if (this.isScenario && this.$refs.uiScenarioList) {
        this.$refs.uiScenarioList.condition.versionId = currentVersion || null;
        this.$refs.uiScenarioList.getVersionOptions(currentVersion);
      }

      //自定义指令情况
      if (this.isCustomCommand && this.$refs.uiCustomCommandList) {
        this.$refs.uiCustomCommandList.condition.versionId =
          currentVersion || null;
        this.$refs.uiCustomCommandList.getVersionOptions(currentVersion);
      }
    },
    /**
     * Sence V2.2 for add custom command
     * author by nathan liu
     */
    searchTrashByType() {
      if (this.isScenario) {
        this.$nextTick(() => {
          if (this.$refs.uiTrashScenarioList) {
            this.$refs.uiTrashScenarioList.search();
          }
        });
        return;
      }
      this.$nextTick(() => {
        if (this.$refs.uiTrashCustomCommandList) {
          this.$refs.uiTrashCustomCommandList.search();
        }
      });
    },

    //基于类型 的 list
    nodeTreeListByType() {
      this.$nextTick(() => {
        this.isScenario
          ? this.$refs.nodeTree.list()
          : this.$refs.customNodeTree.list();
      });
    },
    //基于类型进行检索
    searchByType() {
      this.$nextTick(() => {
        this.isScenario
          ? this.$refs.uiScenarioList.search()
          : this.$refs.uiCustomCommandList.search();
      });
    },
  },
};
</script>

<style scoped>
:deep(.el-tabs__header) {
  margin: 0 0 0px;
}

:deep(.el-table__empty-block) {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

:deep(.scenario-aside .ms-aside-node-tree) {
  overflow-x: hidden !important;
}

:deep(.el-tabs__item.is-active span font) {
  color: #783887;
}

.type-font {
  color: #212121;
}

.label-font {
  color: #6b6b6b;
}

main.el-main.ms-main-container {
  height: calc(100vh - 50px);
  overflow-y: hidden;
}
</style>

<style>
.el-message-box__wrapper
  .el-message-box__content
  .el-message-box__container
  .el-message-box__message {
  word-break: break-all;
}
</style>
