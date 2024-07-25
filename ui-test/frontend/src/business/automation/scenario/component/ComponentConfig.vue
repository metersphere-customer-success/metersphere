<template>
  <div class="request-form">
    <keep-alive>
      <component
        v-bind:is="component"
        :isMax="isMax"
        :refNodeIdCahceMap = "refNodeIdCahceMap"
        :show-btn="showBtn"
        :show-version="showVersion"
        :expandedNode="expandedNode"
        :scenario="scenario"
        :controller="scenario"
        :timer="scenario"
        :assertions="scenario"
        :extract="scenario"
        :jsr223-processor="scenario"
        :request="scenario"
        :command="scenario"
        :currentScenario="currentScenario"
        :currentEnvironmentId="currentEnvironmentId"
        :node="node"
        :draggable="draggable"
        :title="title"
        :color="titleColor"
        :response="response"
        :environment-type="environmentType"
        :environment-group-id="envGroupId"
        :background-color="backgroundColor"
        :project-list="projectList"
        :env-map="envMap"
        :message="message"
        :api-id="apiId"
        :scenario-definition="scenarioDefinition"
        :if-from-variable-advance="ifFromVariableAdvance"
        @suggestClick="suggestClick(node)"
        @remove="remove"
        @runScenario="runScenario"
        @stopScenario="stopScenario"
        @copyRow="copyRow"
        @refReload="refReload"
        @openScenario="openScenario"
        @setDomain="setDomain"
        @savePreParams="savePreParams"
        @editScenarioAdvance="editScenarioAdvance"
      />
    </keep-alive>
  </div>
</template>

<script>
import {ELEMENT_TYPE} from "@/api/Setting";
import MsUiCommand from "@/business/automation/scenario/component/MsUiCommandComponent.vue";
import MsUiScenarioComponent from "@/business/automation/scenario/component/MsUiScenarioComponent.vue";
import MsUiCustomCommandComponent from "@/business/automation/scenario/component/MsUiCustomCommandComponent.vue";

export default {
  name: "ComponentConfig",
  components: {
    MsUiCommand,
    MsUiScenarioComponent,
    MsUiCustomCommandComponent
  },
  props: {
    type: String,
    message: String,
    scenario: {},
    command: {},
    draggable: {
      type: Boolean,
      default: true,
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    refNodeIdCahceMap: Map,
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    currentScenario: {},
    expandedNode: Array,
    currentEnvironmentId: String,
    response: {},
    node: {},
    projectList: Array,
    envMap: Map,
    environmentType: String,
    envGroupId: String,
    scenarioDefinition: Array,
    // 是否来自于接口自动化的参数设置
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      title: "",
      titleColor: "",
      backgroundColor: "",
      apiId: "",
    }
  },
  computed: {
    component({type}) {
      let name;
      switch (type) {
        case "MsUiCommand":
          name = "MsUiCommand";
          break;
        case "scenario":
        case "UiScenario":
          name = "MsUiScenarioComponent";
          break;
        case "customCommand":
          name = "MsUiCustomCommandComponent";
          break;
        default:
          name = this.getComponent(ELEMENT_TYPE.Plugin);
          break;
      }
      return name;
    }
  },
  methods: {
    getComponent(type) {
      if (type === ELEMENT_TYPE.JSR223PreProcessor) {
        this.title = this.$t('api_test.definition.request.pre_script');
        this.titleColor = "#b8741a";
        this.backgroundColor = "#F9F1EA";
        return "MsJsr233Processor";
      } else if (type === ELEMENT_TYPE.JSR223PostProcessor) {
        this.title = this.$t('api_test.definition.request.post_script');
        this.titleColor = "#783887";
        this.backgroundColor = "#F2ECF3";
        return "MsJsr233Processor";
      }
      if (type === ELEMENT_TYPE.JDBCPreProcessor) {
        this.title = this.$t('api_test.definition.request.pre_sql');
        this.titleColor = "#FE6F71";
        this.backgroundColor = "#F2ECF3";
        return "MsJdbcProcessor";
      } else if (type === ELEMENT_TYPE.JDBCPostProcessor) {
        this.title = this.$t('api_test.definition.request.post_sql');
        this.titleColor = "#1483F6";
        this.backgroundColor = "#F2ECF3";
        return "MsJdbcProcessor";
      } else if (type === ELEMENT_TYPE.Plugin) {
        this.titleColor = "#1483F6";
        this.backgroundColor = "#F2ECF3";
        return "PluginComponent";
      } else if (type === ELEMENT_TYPE.Assertions) {
        if (this.node && this.node.parent && this.node.parent.data && this.node.parent.data.referenced === "REF") {
          this.apiId = this.node.parent.data.id;
          this.scenario.document.nodeType = "scenario";
        } else {
          this.apiId = "none";
        }
        return "MsApiAssertions";
      } else {
        this.title = this.$t('api_test.automation.customize_script');
        this.titleColor = "#7B4D12";
        this.backgroundColor = "#F1EEE9";
        return "MsJsr233Processor";
      }
    },
    remove(row, node) {
      this.$emit('remove', row, node);
    },
    copyRow(row, node) {
      this.$emit('copyRow', row, node);

    },
    openScenario(data) {
      this.$emit('openScenario', data);
    },
    suggestClick(node) {
      this.$emit('suggestClick', node);
    },
    refReload(data, node) {
      this.$emit('refReload', data, node);
    },
    runScenario(scenario) {
      this.$emit('runScenario', scenario);
    },
    stopScenario() {
      this.$emit('stopScenario');
    },
    setDomain() {
      this.$emit("setDomain");
    },
    savePreParams(data) {
      this.$emit("savePreParams", data);
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
  }
}
</script>

<style scoped>
.request-form :deep(.debug-button) {
  margin-left: auto;
  display: block;
  margin-right: 10px;
}

</style>
