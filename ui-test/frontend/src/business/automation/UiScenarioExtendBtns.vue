<template>
  <div v-if="showRef">
    <el-dropdown @command="handleCommand" class="scenario-ext-btn">
      <el-link type="primary" :underline="false">
        <el-icon class="el-icon-more"></el-icon>
      </el-link>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="ref">{{ $t('api_test.automation.view_ref') }}</el-dropdown-item>
        <el-dropdown-item v-if="showSchedule" command="schedule" v-permission="['PROJECT_UI_SCENARIO:READ+SCHEDULE']">{{ $t('commons.trigger_mode.schedule') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <UiShowReference ref="uiShowRef"></UiShowReference>
    <ms-schedule-maintain ref="scheduleMaintain" @refreshTable="refreshTable" :request="request"/>
  </div>
</template>

<script>
import UiShowReference from "@/business/automation/UiShowReference";
import MsScheduleMaintain from "@/business/automation/schedule/ScheduleMaintain";
import {hasPermission} from "metersphere-frontend/src/utils/permission";


export default {
  name: "UiScenarioExtendBtns",
  components: {UiShowReference, MsScheduleMaintain},
  props: {
    showRef: {
      type: Boolean,
      default: true,
    },
    showSchedule: {
      type: Boolean,
      default: true,
    },
    row: Object,
    request: {},
    scenarioType: {
      type: String,
      default: "scenario",
    },
  },
  methods: {
    hasPermission,
    handleCommand(cmd) {
      switch (cmd) {
        case "ref":
          this.$refs.uiShowRef.open(this.row, this.scenarioType);
          break;
        case "schedule":
          this.$emit('openSchedule');
          this.$nextTick(() => {
            this.$refs.scheduleMaintain.open(this.row);
          });
          break;
      }
    },
    refreshTable() {

    }
  },
};
</script>

<style scoped>
.scenario-ext-btn {
  margin-left: 10px;
}
</style>
