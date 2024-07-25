<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="medium"
             v-loading="loading" :rules="rules" ref="cmdForm" :inline="false" :inline-message="true">

      <command-item-container>
        <el-form-item
            class="cmd-component"
            prop="inputOrNot"
            :label-width="labelWidth"
            :label="$t('ui.input_or_not')">
          <el-select v-model="currentCommand.vo.inputOrNot" size="small" class="dialog-input">
            <el-option :key="o.value" :label="o.label" :value="o.value" v-for="o in optContents"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
            v-if="currentCommand.vo.inputOrNot"
            class="cmd-component"
            prop="inputContent"
            :label-width="labelWidth"
            :label="$t('ui.input_content')"
            :label-position="'right'">
          <el-input :placeholder="$t('ui.pls_input')" v-model="currentCommand.vo.inputContent"
                    type="textarea"
                    class="dialog-input"
                    :autosize="{ minRows: 4, maxRows: 10}"
                    :rows="2"></el-input>
        </el-form-item>
        <el-form-item
            class="cmd-component"
            prop="optType"
            :label-width="labelWidth"
            :label="$t('ui.opt_type')">
          <el-select v-model="currentCommand.vo.optType" placeholder="placeholder" class="dialog-input" size="small">
            <el-option
                v-for="item in optTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value">
            </el-option>
          </el-select>
          <el-tooltip effect="dark" placement="top-start" :content="waitText" style="margin-left: 5px">
            <i class="el-icon-info pointer"/>
          </el-tooltip>
        </el-form-item>
      </command-item-container>
    </el-form>
  </div>
</template>

<script>
import argtype from "../argtype"
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import paramDefinition from "@/business/definition/command/param-definition";
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import {commandFromValidate} from "@/business/automation/ui-automation";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "Dialog",
  props: {
    selectCommand: Object,
    showSwitch: {
      type: Boolean,
      default: true
    },
    value: Object,
  },
  components: {
    CommandItemContainer,
    ...argtype
  },
  watch: {
    currentCommand: {
      handler(val) {
        if (val.type == 'MsUiCommand') {
          this.initCmd();
        }
      },
      deep: true
    },
    selectCommand() {
      this.initCmd();
    },
  },
  created() {
    this.currentCommand = this.value;
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
    if (this.selectCommand) {
      this.initCmd();
    }
    if (this.currentCommand.atomicCommands) {
      this.atomicCommands = this.currentCommand.atomicCommands;
    }
  },
  computed: {
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    }
  },
  data() {
    return {
      optContents: [
        {label: this.$t("ui.no"), value: false},
        {label: this.$t("ui.yes"), value: true},
      ],
      optTypes: [
        {label: this.$t("ui.confirm"), value: true},
        {label: this.$t("ui.cancel"), value: false},
      ],
      waitText: this.$t('ui.press_button'),
      isReadOnly: false,
      labelWidth: "120px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: {
        inputOrNot: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        inputContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        optType: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}]
      },
      atomicCommands: [],
      currentCommand: {}
    }
  },
  methods: {
    validate() {
      return commandFromValidate(this, 'cmdForm');
    },
    initCmd() {
      this.loading = true;
      this.targetType = false;
      this.valueType = false;
      this.currentCommand = this.selectCommand ? this.selectCommand : commandStore.selectCommand;
      let cmd = this.currentCommand.command;
      let command = this.commandDefinition[cmd];
      if (command) {
        this.targetType = command.target;
        this.valueType = command.value;
        //根据参数定义的类型选择不同组件
        if (this.targetType) {
          this.targetTypeComponent = this.paramDefinition[this.targetType];
        }
        if (this.valueType) {
          this.valueTypeComponent = this.paramDefinition[this.valueType];
        }
      }
      this.loading = false;
    }
  }
}
</script>

<style scoped>

.cmd-component {
  padding: 0;
  margin-bottom: 8px;
}


.el-form-item .el-select {
  width: 380px;
}

.dialog-input {
  width: 618px;
}

.el-form-item .el-input {
  width: 380px;
}
</style>
