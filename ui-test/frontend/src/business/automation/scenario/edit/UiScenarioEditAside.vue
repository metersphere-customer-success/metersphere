<template>
  <ms-aside-container class="aside-wrap" pageKey="UI_SCENARIO_EDIT_ASIDE">

    <el-tabs type="border-card" :stretch="true" v-model="activeName">

      <el-tab-pane :label="$t('ui.basic_information')" name="baseInfo">
        <ui-scenario-base-info
            :module-options="moduleOptions"
            ref="baseInfo"
            v-model="value"
        />
      </el-tab-pane>

      <el-tab-pane class="scenario-step" @click.native="emptyClick" :label="$t('ui.scenario_steps')" name="step">
        <scenario-edit-tree
            @openScenario="openScenario"
            :current-scenario="value"
            :command-definition="commandDefinition"
            :param-definition="paramDefinition"
            @changeCurrentCommand="$emit('changeCurrentCommand', $event)"
            @setProjectEnvMap="setProjectEnvMap"
            v-model="value"
            ref="tree"
            class="step"
        />
      </el-tab-pane>

    </el-tabs>
  </ms-aside-container>
</template>

<script>
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import UiScenarioBaseInfo from "@/business/automation/scenario/edit/UiScenarioBaseInfo";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";
import paramDefinition from "@/business/definition/command/param-definition";
import {useCommandStore} from "@/store"

const commandStore = new useCommandStore();
export default {
  name: "UiScenarioEditAside",
  components: {
    MsAsideContainer,
    UiScenarioBaseInfo,
    ScenarioEditTree: () => import("@/business/automation/scenario/edit/UiScenarioEditTree")
  },
  props: {
    value: Object,
    moduleOptions: Array,
  },
  data() {
    return {
      activeIndex: "baseInfo",
      menuKey: 0,
      baseInfo: {},
      scenarioDefinition: [],
      activeName: 'step'
    }
  },
  computed: {
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    },
  },
  created() {
    this.activeName = this.value.editType == "edit" ? "step" : "baseInfo";
  },
  methods: {
    openScenario(data) {
      this.$emit("openScenario", data);
    },
    handleSelect(index) {
      if (index) {
        this.activeIndex = index;
      }
    },
    validate() {
      return this.$refs.baseInfo.validate();
    },
    emptyClick() {
      this.$emit("changeCurrentCommand", {});
      commandStore.selectCommand = {};
      commandStore.selectStep = {};
    },
    setProjectEnvMap(data){
      this.$emit("setProjectEnvMap", data);
    }
  }
}
</script>

<style scoped>
:deep(.el-tabs--border-card>.el-tabs__content) {
  padding: 10px;
}

.ms-aside-container {
  padding: 0;
  height: calc(100vh - 130px) !important;
}

.ms-aside-container :deep(.ms-aside-node-tree) {
  overflow-y: auto !important;
}

.el-tab-pane {
  min-height: calc(100vh - 205px);
}

.step {
  padding-left: 0px;
  padding-bottom: 55px;
}

</style>
