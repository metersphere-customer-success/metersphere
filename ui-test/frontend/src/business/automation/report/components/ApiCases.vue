<template>
  <div>
    <el-tabs type="card">
      <el-tab-pane>
        <template v-slot:label>
          <tab-pane-count :title="$t('commons.api_case')" :count="apiSize"/>
        </template>
        <api-case-failure-result :is-db="isDb" :is-all="isAll" :is-error-report="isErrorReport" :is-un-execute="isUnExecute" :share-id="shareId" :is-share="isShare"
                                 :report="report" :is-template="isTemplate" :plan-id="planId" @setSize="setApiSize"/>
      </el-tab-pane>
      <el-tab-pane>
        <template v-slot:label>
          <tab-pane-count :title="$t('commons.scenario_case')" :count="scenarioSize"/>
        </template>
        <api-scenario-failure-result :is-db="isDb" :is-all="isAll" :is-error-report="isErrorReport" :is-un-execute="isUnExecute" :share-id="shareId" :is-share="isShare"
                                     :report="report" :is-template="isTemplate" :plan-id="planId"
                                     @setSize="setScenarioSize"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import PriorityTableItem from "@/business/automation/scenario/api/PriorityTableItem";
import TypeTableItem from "@/business/automation/scenario/component/case/tableitems/TypeTableItem";
import MethodTableItem from "@/business/automation/scenario/component/case/MethodTableItem";
import StatusTableItem from "@/business/automation/scenario/component/case/StatusTableItem";
import ApiScenarioFailureResult from "@/business/automation/report/components/ApiScenarioFailureResult";
import ApiCaseFailureResult from "@/business/automation/report/components/ApiCaseFailureResult";
import TabPaneCount from "@/business/automation/report/components/TabPaneCount";

export default {
  name: "ApiCases",
  components: {
    TabPaneCount,
    ApiCaseFailureResult,
    ApiScenarioFailureResult, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem
  },
  props: {
    planId: String,
    isTemplate: Boolean,
    isShare: Boolean,
    report: {},
    shareId: String,
    isAll: Boolean,
    isErrorReport: Boolean,
    isUnExecute:Boolean,
    isDb: Boolean,
  },
  data() {
    return {
      apiSize: 0,
      scenarioSize: 0,
    }
  },
  mounted() {
  },
  watch: {
    apiSize() {
      this.$emit('setSize', this.apiSize + this.scenarioSize);
    },
    scenarioSize() {
      this.$emit('setSize', this.apiSize + this.scenarioSize);
    },
  },
  methods: {
    setApiSize(size) {
      this.apiSize = size;
    },
    setScenarioSize(size) {
      this.scenarioSize = size;
    },
  }
}
</script>

<style scoped>

</style>
