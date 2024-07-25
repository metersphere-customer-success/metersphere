<template>
  <test-case-relevance-base
    :dialog-title="$t('api_test.automation.scenario_import')"
    @setProject="setProject"
    ref="baseRelevance">
    <template v-slot:aside>
      <ui-scenario-module
        style="margin-top: 5px;"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <ui-scenario-list
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
    />


    <template v-slot:headerBtn>
      <el-tooltip
        class="item"
        effect="dark"
        :content="$t('commons.refresh')"
        placement="top-start"
      >
        <el-button
          size="mini"
          icon="el-icon-refresh"
          @click="refresh"
          class="refresh-btn"
        ></el-button>
      </el-tooltip>
      <el-button type="primary" @click="copy" :loading="buttonIsWorking" @keydown.enter.native.prevent size="mini">
        {{ $t('commons.copy') }}
      </el-button>
      <!-- todo 场景引用     -->
      <!--      <el-button type="primary" @click="reference" :loading="buttonIsWorking" @keydown.enter.native.prevent size="mini">-->
      <!--        {{ $t('api_test.scenario.reference') }}-->
      <!--      </el-button>-->
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
import {useCommandStore} from "@/store";
import {getUUID} from "metersphere-frontend/src/utils";
import {hasLicense} from "metersphere-frontend/src/utils/permission";

const commandStore = new useCommandStore();
// const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
// const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};
const VersionSelect = {};

export default {
  name: "UiScenarioRelevance",
  components: {
    UiScenarioModule,
    'VersionSelect': VersionSelect.default,
    TestCaseRelevanceBase,
    RelevanceDialog,
    UiScenarioList,
    MsMainContainer,
    MsAsideContainer,
    MsContainer
  },
  data() {
    return {
      buttonIsWorking: false,
      result: {},
      currentProtocol: null,
      selectNodeIds: [],
      moduleOptions: [],
      isApiListEnable: true,
      currentScenario: [],
      currentScenarioIds: [],
      projectId: '',
      customNum: false,
      versionOptions: [],
      currentVersion: '',
      versionEnable: true,
    };
  },
  watch: {
    projectId(val) {
      this.$refs.nodeTree.list(this.projectId);
    }
  },
  methods: {
    changeButtonLoadingType() {
      this.buttonIsWorking = false;
    },
    createScenarioDefinition(scenarios, data, referenced) {
      let errArr = [];
      for(let item of data){
        let scenarioDefinition = JSON.parse(item.scenarioDefinition);
        if(!scenarioDefinition){
          continue;
        }
        if(!scenarioDefinition.hashTree || scenarioDefinition.hashTree.length <= 0){
          errArr.push(scenarioDefinition.name);
          continue;
        }
        if (scenarioDefinition && scenarioDefinition.hashTree) {
          //处理scenarioDefinition
          scenarioDefinition = this.handleScenarioDefinition(scenarioDefinition)

          let obj = {
            //场景的id 存入resourceId中 避免el-tree key重复
            id: getUUID(),
            name: item.name,
            type: "scenario",
            variables: scenarioDefinition.variables,
            environmentMap: scenarioDefinition.environmentMap,
            referenced: referenced,
            resourceId: item.id,
            hashTree: scenarioDefinition.hashTree,
            projectId: item.projectId,
            num: item.num,
            versionName: item.versionName,
            versionEnable: item.versionEnable,
            clazzName: "io.metersphere.hashtree.MsUiScenario"
          };
          scenarios.push(obj);
        }
      }

      if(errArr.length > 0){
        let msg = errArr.join("、");
        if(msg && msg.length > 30){
          msg = msg.substring(0, 30) + "...";
        }
        this.$error(msg + " 场景为空，导入失败");
      }
    },
    handleScenarioDefinition(scenarioDefinition) {
      if (!scenarioDefinition || !scenarioDefinition.hashTree) {
        return scenarioDefinition;
      }
      scenarioDefinition.resourceId = scenarioDefinition.id || getUUID();
      scenarioDefinition.id = getUUID();

      for (let item of scenarioDefinition.hashTree) {
        let temp = item.id || getUUID();
        item.id = item.resourceId;
        item.resourceId = getUUID();

        if (item.hashTree) {
          item = this.handleScenarioDefinition(item);
        }
      }
      return scenarioDefinition;
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
        this.result = this.$post(url, params, (response) => {
          this.currentScenarioIds = response.data;
          if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
            this.$warning('请选择场景');
            this.buttonIsWorking = false;
            return;
          }
          this.result = this.$post("/ui/automation/getUiScenarios", this.currentScenarioIds, response => {
            if (response.data) {
              this.createScenarioDefinition(scenarios, response.data, referenced)
              this.$emit('save', scenarios);
              this.currentScenarioIds = [];
              this.$refs.baseRelevance.close();
              this.buttonIsWorking = false;
            }
          }, (error) => {
            this.buttonIsWorking = false;
          });
        }, (error) => {
          this.buttonIsWorking = false;
        });
      } else {
        if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
          this.$warning('请选择场景');
          this.buttonIsWorking = false;
          return;
        }
        this.result = this.$post("/ui/automation/getUiScenarios/", this.currentScenarioIds, response => {
          if (response.data) {
            this.createScenarioDefinition(scenarios, response.data, referenced)
            this.$emit('save', scenarios);
            this.currentScenarioIds = [];
            this.$refs.baseRelevance.close();
            this.buttonIsWorking = false;
            //默认选中
            this.$emit("changeCurrentCommand", scenarios ? scenarios[0] : {});

            commandStore.selectCommand = scenarios ? scenarios[0] : {};
            commandStore.selectStep = scenarios ? scenarios[0] : {};
          }
        }, (error) => {
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
      this.$emit('close');
      this.refresh();
      this.$refs.relevanceDialog.close();
    },
    open() {
      this.buttonIsWorking = false;
      this.$refs.baseRelevance.open();
      if (this.$refs.uiScenarioList) {
        this.$refs.uiScenarioList.search(this.projectId);
      }
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    refresh() {
      this.$refs.uiScenarioList.search();
    },
    setData(data) {
      this.currentScenario = Array.from(data).map(row => row);
      this.currentScenarioIds = Array.from(data).map(row => row.id);
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.selectNodeIds = [];
    },
    getConditions() {
      return this.$refs.uiScenarioList.getConditions();
    },
    getVersionOptionList(projectId) {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + projectId, response => {
          this.versionOptions = response.data;
        });
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
        this.$get('/project/version/enable/' + projectId, response => {
          this.versionEnable = false;
          this.$nextTick(() => {
            this.versionEnable = true;
          });
        });
      }
    },
  }
};
</script>

<style scoped>

</style>
