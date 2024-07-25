<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo"
             size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">

      <command-item-container>
        <el-form-item
          prop="iteratorVariable"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.set_itera')">
          <el-input v-model="currentCommand.vo.iteratorVariable"/>
        </el-form-item>
        <el-form-item>
           <span class="spit-item">
            in
            </span>
        </el-form-item>

        <el-form-item prop="arrayVariable">
          <el-input v-model="currentCommand.vo.arrayVariable"/>
        </el-form-item>

        <ms-instructions-icon size="14" :content="$t('ui.foreach_tip')"/>

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
import {commandFromValidate} from "@/business/automation/ui-automation";

export default {
  name: "ForEach",
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
      labelWidth: "120px",
      isReadOnly: false,
      formLabelAlign: {},
      loading: false,
      rules:  {
        iteratorVariable: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
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
  },
  methods:{
    validate() {
      return commandFromValidate(this);
    },
  }
}
</script>

<style scoped>
.spit-item {
  margin-left: 10px;
  margin-right: 10px;
}

.instructions-icon {
  line-height: 30px;
}
</style>
