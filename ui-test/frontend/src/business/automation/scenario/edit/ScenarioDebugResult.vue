<template>
  <div>
    <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
      <i class="el-icon-circle-plus-outline  ms-open-btn ms-open-btn-left" v-prevent-re-click @click="openExpansion"/>
    </el-tooltip>
    <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
      <i class="el-icon-remove-outline ms-open-btn" size="mini" @click="closeExpansion"/>
    </el-tooltip>
    <el-tooltip :content="$t('api_test.scenario.disable')" placement="top" effect="light" v-if="!stepEnable">
      <font-awesome-icon class="ms-open-btn" :icon="['fas', 'toggle-off']" @click="enableAll"/>
    </el-tooltip>
    <el-tooltip :content="$t('api_test.scenario.enable')" placement="top" effect="light" v-else>
      <font-awesome-icon class="ms-open-btn" :icon="['fas', 'toggle-on']" @click="disableAll"/>
    </el-tooltip>
  </div>
</template>

<script>

export default {
  name: "ScenarioTreeGlobalOperator",
  components: {},
  props: {
    currentScenario: Object,

    scenarioDefinition: Array,
  },
  data() {
    return {
      stepEnable: true,
      expandedNode: [],
      expandedStatus: false
    }
  },
  created() {
    this.stepEnable = true;
  },
  methods: {
    openExpansion() {
      this.expandedNode = [];
      this.expandedStatus = true;
      this.changeNodeStatus(this.scenarioDefinition);
    },
    closeExpansion() {
      this.expandedStatus = false;
      this.expandedNode = [];
      this.changeNodeStatus(this.scenarioDefinition);
    },
    enableAll() {
      this.stepEnable = true;
      this.stepNode();
    },
    disableAll() {
      this.stepEnable = false;
      this.stepNode();
    },
    stepNode() {
      //改变每个节点的状态
      for (let i in this.scenarioDefinition) {
        if (this.scenarioDefinition[i]) {
          this.scenarioDefinition[i].enable = this.stepEnable;
          if (this.scenarioDefinition[i].hashTree && this.scenarioDefinition[i].hashTree.length > 0) {
            this.stepStatus(this.scenarioDefinition[i].hashTree);
          }
        }
      }
    },
    changeNodeStatus(nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          if (this.expandedStatus) {
            this.expandedNode.push(nodes[i].resourceId);
          }
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.changeNodeStatus(nodes[i].hashTree);
          }
        }
      }
    },
    stepStatus(nodes) {
      for (let i in nodes) {
        if (nodes[i]) {
          nodes[i].enable = this.stepEnable;
          if (nodes[i].hashTree != undefined && nodes[i].hashTree.length > 0) {
            this.stepStatus(nodes[i].hashTree);
          }
        }
      }
    },
  }
}
</script>

<style scoped>
</style>
