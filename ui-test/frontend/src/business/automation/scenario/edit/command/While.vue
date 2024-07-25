<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo"
             size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">

      <command-item-container>
        <el-form-item
          prop="expression"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.expression')">
          <el-input v-model="currentCommand.vo.expression"/>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="currentCommand.vo.isDoWhile">{{ $t('ui.exe_first') }}</el-checkbox>
          <ms-instructions-icon>
            <template v-slot:default>
              <span>
                {{ $t('ui.while_t_1') }}
              </span>
              <br>
              <span>
                 {{ $t('ui.while_t_2') }}
              </span>
            </template>
          </ms-instructions-icon>
        </el-form-item>
      </command-item-container>

      <command-item-container>
        <el-form-item
          prop="interval"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.intervals')">
          <el-input-number
          ref = "whileRefs"
            @change = "handleChange"
            v-model="currentCommand.appendCommands[0].targetVO.text"
            :min="0" :step="whileStep"/> ms
        </el-form-item>
      </command-item-container>

      <command-item-container>
        <el-form-item
          prop="interval"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.loop_time_out')">
          <el-input-number
            ref = "timeoutRefs"
            v-model="currentCommand.vo.timeout"
            :min="0" :step="timeoutStep" @change = "handleTimeoutChange"/> ms
        </el-form-item>
      </command-item-container>

    </el-form>
  </div>
</template>

<script>
import {createComponent} from "@/business/definition/jmeter/components";
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import {commandFromValidate} from "@/business/automation/ui-automation";

export default {
  name: "While",
  components: {MsInstructionsIcon, CommandItemContainer},
  props: {
    selectCommand: Object,
    currentCommand: {
      type: Object,
      default: createComponent("MsUiCommand")
    }
  },
  data() {
    return {
      whileStep: 1000,
      timeoutStep: 1000,
      labelWidth: "120px",
      isReadOnly: false,
      formLabelAlign: {},
      loading: false,
      rules: {
        expression: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        arrayVariable: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}]
      },
      atomicCommands: [],
    }
  },
  mounted() {
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
    if (!this.currentCommand.appendCommands[0].targetVO.text) {
      this.currentCommand.appendCommands[0].targetVO.text = 0;
    }
    if (!this.currentCommand.vo.timeout) {
      this.currentCommand.vo.timeout = 20 * 1000;
    }
  },
  methods:{
    validate() {
      return commandFromValidate(this);
    },
    handleTimeoutChange(nV, oV){
      if (nV < oV) {
        if(nV !== 0 && nV - 1000 < 0){
          this.timeoutStep = nV
        }
        if(nV === 0){
          this.timeoutStep = 1000;
        }
      }
      else{
        if(nV < 1000 && nV == 2 * oV){
          this.$refs.timeoutRefs.setCurrentValue(1000)
        }
        else if(nV < 1000){
          this.timeoutStep = nV;
        }
        else{
         this.timeoutStep = 1000;
        }
      }
    },
    handleChange(nV, oV){
      if (nV < oV) {
        if(nV !== 0 && nV - 1000 < 0){
          this.whileStep = nV
        }
        if(nV === 0){
          this.whileStep = 1000;
        }
      }
      else{
        if(nV < 1000 && nV == 2 * oV){
          this.$refs.whileRefs.setCurrentValue(1000)
        }
        else if(nV < 1000){
          this.whileStep = nV;
        }
        else{
         this.whileStep = 1000;
        }
      }
    }
  }
}
</script>

<style scoped>
.spit-item {
  margin-left: 10px;
  margin-right: 10px;
}

.el-checkbox {
  margin-left: 10px;
}
</style>
