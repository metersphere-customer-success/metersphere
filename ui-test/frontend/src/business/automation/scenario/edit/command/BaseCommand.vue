<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand" size="small"
             :inline="true" :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">
      <el-row :gutter="0">
        <el-col :span="24">
          <el-form-item v-if="targetType" class="cmd-component" prop="targetVO"
                        :rules="targetVORule" :label="targetLabel">
            <!-- 指令参数组件 start -->
            <component :is="targetTypeComponent" :command-definition="commandDefinition"
                       :param-definition="paramDefinition" v-model="currentCommand" prop-name="targetVO"
                       class="command" :key="currentCommand.index"></component>
            <!-- 指令参数组件 end -->
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="0">
        <el-col :span="24">
          <el-form-item v-if="valueType" class="cmd-component" prop="valueVO"
                        :rules="valueVORule" :label="valueLabel">
            <!-- 指令参数组件 start -->
            <component :is="valueTypeComponent" :command-definition="commandDefinition"
                       :param-definition="paramDefinition" v-model="currentCommand" prop-name="valueVO"
                       class="command" :key="currentCommand.index"></component>
            <!-- 指令参数组件 end -->
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import argtype from "../argtype"
import UiCmdSwitcher from "./UiCmdSwitcher";
import paramValidator from "@/business/definition/validator/param-validator"
import paramDefinition from "@/business/definition/command/param-definition";
import {createComponent} from "@/business/definition/jmeter/components";
import {getCommandParamCNName, commandDefinitionMap} from "@/business/definition/command/all-command-definition"
import {commandFromValidate} from "@/business/automation/ui-automation";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "BaseCommand",
  props: {
    value: Object,
    showSwitch: {
      type: Boolean,
      default: true
    },
    currentCommand: {
      type: Object,
      default: createComponent("MsUiCommand")
    }
  },
  components: {
    ...argtype,
    UiCmdSwitcher
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
  },
  created() {
    this.isReadOnly = this.currentCommand.readonly;
    if (this.value) {
      this.initCmd();
    }
  },
  computed: {
    targetVORule() {
      return this.getParamRule('target');
    },
    valueVORule() {
      return this.getParamRule('value');
    },
    targetLabel() {
      return getCommandParamCNName(this.currentCommand.command, "target");
    },
    valueLabel() {
      return getCommandParamCNName(this.currentCommand.command, "value");
    },
    commandDefinition() {
      return commandDefinitionMap;
    },
    paramDefinition() {
      return paramDefinition;
    }
  },
  data() {
    return {
      isReadOnly: false,
      labelWidth: "100px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: paramValidator,
    }
  },
  methods: {
    validate() {
      return commandFromValidate(this);
    },
    initCmd() {
      //非原子指令不初始化
      if (!this.commandDefinition[this.value.command]) {
        return;
      }
      this.loading = true;
      this.targetType = false;
      this.valueType = false;
      this.currentCommand = this.value ? this.value : commandStore.selectCommand;
      let cmd = this.currentCommand.command;
      this.targetType = this.commandDefinition[cmd].target;
      this.valueType = this.commandDefinition[cmd].value;
      //根据参数定义的类型选择不同组件
      if (this.targetType) {
        this.targetTypeComponent = this.paramDefinition[this.targetType];
      }
      if (this.valueType) {
        this.valueTypeComponent = this.paramDefinition[this.valueType];
      }
      this.loading = false;
    },
    getParamRule(name) {
      if (this.currentCommand.type == "MsUiCommand") {
        //找到指令定义中参数的类型
        let argType = this.commandDefinition[this.currentCommand.command][name];
        if (this.rules[argType]) {
          return this.rules[argType];
        }
        return this.rules['noValidator'];
      }
    }
  }
}
</script>

<style scoped>
.command {
}

.el-form-item .el-select {
  width: 180px;
}

.el-form-item .el-input {
  width: 180px;
}
</style>
