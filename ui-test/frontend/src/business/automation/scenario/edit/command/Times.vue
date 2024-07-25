<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo"
             size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">

      <command-item-container>
        <el-form-item
          prop="times"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.times')">
          <el-input
            v-model="currentCommand.vo.times"/>
          <ms-instructions-icon :content="$t('ui.set_times')"/>
        </el-form-item>
      </command-item-container>

      <command-item-container>
        <el-form-item
          prop="interval"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.intervals')">
          <el-input-number
            v-model="currentCommand.appendCommands[0].targetVO.text"
            :min="0" :step="1000"/> ms
        </el-form-item>
      </command-item-container>

    </el-form>
  </div>
</template>

<script>
import {createComponent} from "@/business/definition/jmeter/components";
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import {
  commandFromValidate
} from "@/business/automation/ui-automation";

export default {
  name: "Times",
  components: {MsInstructionsIcon, CommandItemContainer},
  props: {
    selectCommand: Object,
    currentCommand: {
      type: Object,
      default: createComponent("MsUiCommand")
    }
  },
  mounted() {
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
    if (!this.currentCommand.appendCommands[0].targetVO.text) {
      this.currentCommand.appendCommands[0].targetVO.text = 0;
    }
  },
  data() {
    return {
      labelWidth: "120px",
      isReadOnly: false,
      formLabelAlign: {},
      loading: false,
      rules: {
        times: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        optContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}]
      },
      atomicCommands: [],
    }
  },
  methods: {
    validate() {
      return commandFromValidate(this);
    },
  }
}
</script>

<style scoped>
</style>
