<template>
  <div class="cmd-box">

    <div class="cmd-switch">
      <ui-cmd-switcher
        v-if="showSwitch"
        :label-width="labelWidth"
        :current-command="value"
        @changeCurrentCommand="changeCurrentCommand"
        ref="cmdSwitcher"/>
    </div>
    <div class="cmd-component">
      <component
        v-if="component"
        v-model="value"
        class="scenario-command"
        :current-command="value"
        :key="value.id"
        :is="component"
        @setLabelWidth="setLabelWidth"
        ref="cmd"/>
    </div>

  </div>
</template>

<script>
import argtype from "../argtype"
import UiCmdSwitcher from "./UiCmdSwitcher";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import {GetComponentByName} from "@/business/definition/command/all-command-definition"
import paramValidator from "@/business/definition/validator/param-validator"
import paramDefinition from "@/business/definition/command/param-definition";
import CommandComponent from "@/business/automation/scenario/edit/command/index"
import Times from "@/business/automation/scenario/edit/command/Times";
import ForEach from "@/business/automation/scenario/edit/command/ForEach";
import While from "@/business/automation/scenario/edit/command/While";
import If from "@/business/automation/scenario/edit/command/If";
import Else from "@/business/automation/scenario/edit/command/Else";

export default {
  name: "UiScenarioCommand",
  props: {
    value: Object,
    showSwitch: {
      type: Boolean,
      default: true
    }
  },
  components: {
    Else,
    If,
    While,
    ForEach,
    Times,
    ...argtype,
    UiCmdSwitcher,
    ...CommandComponent
  },
  computed: {
    targetVORule() {
      return this.getParamRule('target');
    },
    valueVORule() {
      return this.getParamRule('value');
    },
    paramDefinition() {
      return paramDefinition;
    },
    component() {
      return GetComponentByName(this.value.command);
    },
  },
  data() {
    return {
      isReadOnly: false,
      labelWidth: "120px",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: paramValidator
    }
  },
  methods: {
    validate() {
      if (this.$refs.cmd && this.$refs.cmd.validate instanceof Function) {
        return this.$refs.cmdSwitcher.validate() && this.$refs.cmd.validate();
      }
      return true;
    },
    initCmd() {
      this.loading = true;
      let cmd = this.value.command;
      if (this.$refs.cmdSwitcher) {
        this.$refs.cmdSwitcher.setCommand(cmd);
      }
      this.loading = false;
    },
    getParamRule(name) {
      if (this.value.type == "MsUiCommand") {
        //找到指令定义中参数的类型
        let argType = atomicCommandDefinition[this.value.command][name];
        if (this.rules[argType]) {
          return this.rules[argType];
        }
        return this.rules['noValidator'];
      }
    },
    changeCurrentCommand(cmd) {
      this.$emit("changeCurrentCommand", cmd);
      if (atomicCommandDefinition[cmd]) {
        //只有原子指令需要这个初始化一些参数和配置
        this.$refs.cmd.initCmd();
      }
    },
    setLabelWidth(data) {
      this.labelWidth = data;
    }
  }
}
</script>

<style scoped>

.cmd-box {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.cmd-switch {
  margin-bottom: -15px;
}

.scenario-command {
  margin-top: 15px;
}

:deep(.cmd-component) {
  margin-bottom: 8px;
}

:deep(.cmd-component.el-form-item .el-select) {
  width: 180px;
}

:deep(.cmd-component.el-form-item .el-input) {
  width: 180px;
}

:deep(.cmd-component.el-form-item .el-input-number) {
  width: 180px;
}

:deep(.cmd-component.el-form-item .mouse-click) {
  padding-left: 10px;
}
</style>
