<template>
  <test-case-relevance-base
    :dialog-title="$t('ui.import_by_list_label')"
    @setProject="setProject"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ui-scenario-module
        v-if="currentType === 'scenario'"
        class="node-tree"
        currentType="scenario"
        style="margin-top: 5px"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="nodeTree"
      />
      <ui-custom-command-module
        v-if="currentType === 'customCommand'"
        currentType="customCommand"
        class="node-tree"
        style="margin-top: 5px"
        @customNodeChange="customNodeChange"
        @refreshTable="refresh"
        @setCustomModuleOptions="setCustomModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="customNodeTree"
      />
    </template>

    <ui-scenario-list
      v-if="currentType === 'scenario'"
      currentType="scenario"
      :module-options="moduleOptions"
      :select-node-ids="selectNodeIds"
      :select-project-id="projectId"
      :trash-enable="false"
      :batch-operators="[]"
      :is-reference-table="true"
      @selection="setData"
      :is-relate="true"
      :init-ui-table-opretion="'init'"
      :custom-num="customNum"
      :showDrag="false"
      :mode="'import'"
      ref="uiScenarioList"
    >
      <toggle-tabs
        slot="tabChange"
        :activeDom.sync="currentType"
        :tabList="tabList"
      ></toggle-tabs>
    </ui-scenario-list>

    <ui-custom-command-list
      v-if="currentType === 'customCommand'"
      currentType="customCommand"
      :module-options="customModuleOptions"
      :select-node-ids="selectCustomNodeIds"
      :select-project-id="projectId"
      :trash-enable="false"
      :batch-operators="[]"
      :is-reference-table="true"
      @selection="setData"
      :is-relate="true"
      :init-ui-table-opretion="'init'"
      :custom-num="customNum"
      :showDrag="false"
      :mode="'import'"
      ref="uiCustomScenarioList"
    >
      <toggle-tabs
        slot="tabChange"
        :activeDom.sync="currentType"
        :tabList="tabList"
      ></toggle-tabs>
    </ui-custom-command-list>

    <template v-slot:headerBtn>
      <el-button
        size="mini"
        icon="el-icon-refresh"
        @click="refresh"
        class="refresh-btn"
      ></el-button>
      <el-button
        type="primary"
        @click="copy"
        :loading="buttonIsWorking"
        @keydown.enter.native.prevent
        size="mini"
      >
        {{ $t("commons.copy") }}
      </el-button>
      <!-- todo 场景引用     -->
      <el-button
        type="primary"
        @click="reference"
        :loading="buttonIsWorking"
        @keydown.enter.native.prevent
        size="mini"
      >
        {{ $t("api_test.scenario.reference") }}
      </el-button>
    </template>
  </test-case-relevance-base>
</template>

<script>
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import RelevanceDialog from "@/business/automation/scenario/component/case/plan/base/RelevanceDialog";
import TestCaseRelevanceBase from "@/business/automation/scenario/component/case/plan/base/TestCaseRelevanceBase";
import UiScenarioModule from "@/business/automation/scenario/UiScenarioModule";
import UiScenarioList from "@/business/automation/scenario/UiScenarioList";
import ToggleTabs from "@/business/automation/custom-commands/ToggleTabs";
import UiCustomCommandList from "@/business/automation/custom-commands/UiCustomCommandList";
import UiCustomCommandModule from "@/business/automation/custom-commands/UiCustomCommandModule";
import { TYPE_TO_C } from "@/api/Setting";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getUUID} from "metersphere-frontend/src/utils";

const VersionSelect = {};

export default {
  name: "UiScenarioEditRelevance",
  components: {
    UiScenarioModule,
    VersionSelect: VersionSelect.default,
    TestCaseRelevanceBase,
    RelevanceDialog,
    UiScenarioList,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    ToggleTabs,
    UiCustomCommandList,
    UiCustomCommandModule,
  },
  props: {
    scenarioType: String,
  },
  data() {
    return {
      currentType: "scenario",
      buttonIsWorking: false,
      result: {},
      currentProtocol: null,
      selectNodeIds: [],
      selectCustomNodeIds: [],
      moduleOptions: [],
      customModuleOptions: [],
      isApiListEnable: true,
      currentScenario: [],
      currentScenarioIds: [],
      projectId: "",
      customNum: false,
      versionOptions: [],
      currentVersion: "",
      versionEnable: true,
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
    };
  },
  watch: {
    projectId(val) {
      this.listByType(val);
    },
    scenarioType(val) {
      this.currentType = this.scenarioType;
    },
    currentType(val) {
      this.listByType(this.projectId);
      this.searchByType();
    },
  },
  computed: {
    isScenario() {
      return this.currentType === "scenario";
    },
  },
  methods: {
    listByType(projectId) {
      this.$nextTick(() => {
        this.isScenario
          ? this.$refs.nodeTree.list(this.projectId)
          : this.$refs.customNodeTree.list(this.projectId);
      });
    },
    changeButtonLoadingType() {
      this.buttonIsWorking = false;
    },
    createScenarioDefinition(scenarios, data, referenced) {
      let errArr = [];
      for (let item of data) {
        let scenarioDefinition = JSON.parse(item.scenarioDefinition);
        if (!scenarioDefinition) {
          continue;
        }
        if (
          !scenarioDefinition.hashTree ||
          scenarioDefinition.hashTree.length <= 0
        ) {
          errArr.push(scenarioDefinition.name);
          continue;
        }
        if (scenarioDefinition && scenarioDefinition.hashTree) {
          //处理scenarioDefinition
          this.handleScenarioDefinition(scenarioDefinition);

          let variables = scenarioDefinition.variables
            ? scenarioDefinition.variables.filter((v) => {
                return v.name;
              })
            : [];

          if (item.scenarioType === "customCommand" && item.commandViewStruct) {
            let innerVariables = this.handleInnerVariables(
              item.commandViewStruct
            );
            if (innerVariables) {
              variables = innerVariables;
            }
          }
          let outputVariables = this.handleOutputVariables(
            item.commandViewStruct
          );
          let obj = {
            //场景的id 存入resourceId中 避免el-tree key重复
            id: getUUID(),
            name: item.name,
            type: this.currentType,
            variables: this.resetInnerVariable(variables),
            outputVariables: this.resetOutVariable(outputVariables),
            environmentMap: scenarioDefinition.environmentMap,
            referenced: referenced,
            resourceId: item.id,
            hashTree: scenarioDefinition.hashTree,
            projectId: item.projectId,
            num: item.num,
            versionName: item.versionName,
            versionEnable: item.versionEnable,
            description: item.description,
            environmentJson: item.environmentJson,
            clazzName: this.isScenario
              ? TYPE_TO_C.get("UiScenario")
              : TYPE_TO_C.get("customCommand"),
          };

          // sence v2.5 smart init true 
          if(this.isScenario){
            obj.variableEnable = false;
            obj.smartVariableEnable = true;
          }
          scenarios.push(obj);
        }
      }

      if (errArr.length > 0) {
        let msg = errArr.join("、");
        if (msg && msg.length > 30) {
          msg = msg.substring(0, 30) + "...";
        }
        let str = this.isScenario
          ? this.$t("ui.scenario")
          : this.$t("ui.instruction");
        this.$error(msg + " " + str + "为空，导入失败");
      }

      if(referenced === "REF" ){
        //
        if(this.isScenario){
          //场景
          this.$EventBus.$emit("handleScenarioREFEvent")
        }
        //指令
        else{
          this.$EventBus.$emit("handleCustomCommandREFEvent")
        }
      }
    },
    resetInnerVariable(vars) {
      if (vars && Array.isArray(vars)) {
        vars.forEach((v) => {
          v.id = getUUID();
        });
      }
      return vars;
    },
    resetOutVariable(vars) {
      if (vars && Array.isArray(vars)) {
        vars.forEach((v) => {
          v.id = getUUID();
        });
      }
      return vars;
    },
    handleInnerVariables(commandViewStruct) {
      if (!commandViewStruct) {
        return [];
      }
      let struct = JSON.parse(commandViewStruct);
      if (struct && struct.length > 1) {
        return struct[1].variables;
      }
      return [];
    },
    handleOutputVariables(commandViewStruct) {
      if (!commandViewStruct) {
        return [];
      }

      let struct = JSON.parse(commandViewStruct);
      if (struct && struct.length > 1) {
        let cur = struct[1].outputVariables;
        if (cur) {
          return cur;
        }
      }

      return [];
    },
    handleScenarioDefinition(scenarioDefinition) {
      if (!scenarioDefinition || !scenarioDefinition.hashTree || scenarioDefinition.hashTree.length <= 0) {
        return;
      }
      //如果导入的为引用说明在之前已经处理过resourceId了
      if(scenarioDefinition.referenced !== "REF"){
        scenarioDefinition.resourceId = scenarioDefinition.id || getUUID();
      }
      scenarioDefinition.id = getUUID();
      //重置 出参
      if (scenarioDefinition.outputVariables) {
        scenarioDefinition.outputVariables = "";
      }

      for (let item of scenarioDefinition.hashTree) {
        item.id = getUUID();
        //重置 出参
        if (item.outputVariables) {
          item.outputVariables = "";
        }
        if (item.hashTree) {
          this.handleScenarioDefinition(item);
        }
      }
    },
    getScenarioDefinition(referenced) {
      this.buttonIsWorking = true;
      let scenarios = [];
      let conditions = this.getConditions();
      if (conditions.selectAll) {
        let url = "/ui/automation/id/all";
        let params = {};
        params.ids = this.currentScenarioIds;
        params.condition = conditions;
        this.$post(url, params).then((response) => {
          this.currentScenarioIds = response.data;
          if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
            let str = this.isScenario
              ? this.$t("ui.scenario")
              : this.$t("ui.instruction");
            this.$warning("请选择" + str);
            this.buttonIsWorking = false;
            return;
          }
          this.$post("/ui/automation/getUiScenarios", this.currentScenarioIds)
            .then((response) => {
              if (response.data) {
                this.createScenarioDefinition(
                  scenarios,
                  response.data,
                  referenced
                );
                this.$emit("save", scenarios);
                this.currentScenarioIds = [];
                this.$refs.baseRelevance.close();
                this.buttonIsWorking = false;
              }
            })
            .catch((error) => {
              this.buttonIsWorking = false;
            });
        });
      } else {
        if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
          let str = this.isScenario
            ? this.$t("ui.scenario")
            : this.$t("ui.instruction");
          this.$warning("请选择" + str);
          this.buttonIsWorking = false;
          return;
        }
        this.$post("/ui/automation/getUiScenarios", this.currentScenarioIds)
          .then((response) => {
            if (response.data) {
              this.createScenarioDefinition(
                scenarios,
                response.data,
                referenced
              );
              this.$emit("save", scenarios);
              this.currentScenarioIds = [];
              this.$refs.baseRelevance.close();
              this.buttonIsWorking = false;
              //默认选中
              this.$emit("changeCurrentCommand", scenarios ? scenarios[0] : {});
              this.$store.commit(
                "setSelectCommand",
                scenarios ? scenarios[0] : {}
              );
              this.$store.commit(
                "setSelectStep",
                scenarios ? scenarios[0] : {}
              );
            }
          })
          .catch((error) => {
            this.buttonIsWorking = false;
          });
      }
    },
    reference() {
      this.getScenarioDefinition("REF");
    },
    copy() {
      this.getScenarioDefinition("Copy");
    },
    close() {
      this.$emit("close");
      this.refresh();
      this.$refs.relevanceDialog.close();
    },
    open() {
      this.buttonIsWorking = false;
      this.$refs.baseRelevance.open();
      this.searchByType();
    },

    /**
     * common opt
     */
    searchByType() {
      this.$nextTick(() => {
        this.isScenario
          ? this.$refs.uiScenarioList.search(this.projectId)
          : this.$refs.uiCustomScenarioList.search(this.projectId);
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
    },
    customNodeChange(node, nodeIds, pNodes) {
      this.selectCustomNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    setCustomModuleOptions(data) {
      this.customModuleOptions = data;
    },
    refresh() {
      this.searchByType();
    },
    setData(data) {
      this.currentScenario = Array.from(data).map((row) => row);
      this.currentScenarioIds = Array.from(data).map((row) => row.id);
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.selectNodeIds = [];
      this.selectCustomNodeIds = [];
    },
    getConditions() {
      return this.getConditionsByType();
    },
    getConditionsByType() {
      try {
        if (this.isScenario) {
          return this.$refs.uiScenarioList.getConditions();
        }
        return this.$refs.uiCustomScenarioList.getConditions();
      } catch (e) {
        return {};
      }
    },
    getVersionOptionList(projectId) {
      if (hasLicense()) {
        this.$get("/project/version/get-project-versions/" + projectId).then(
          (response) => {
            this.versionOptions = response.data;
          }
        );
      }
    },
    changeVersion(currentVersion) {
      if (this.$refs.uiScenarioList) {
        this.$refs.uiScenarioList.condition.versionId = currentVersion || null;
      }
      this.refresh();
    },
    checkVersionEnable(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get("/project/version/enable/" + projectId).then((response) => {
          this.versionEnable = false;
          this.$nextTick(() => {
            this.versionEnable = true;
          });
        });
      }
    },
  },
};
</script>

<style scoped>
.node-tree {
  margin-top: 15px;
  margin-bottom: 15px;
  max-height: calc(75vh - 160px);
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
