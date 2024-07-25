<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo"
             size="medium"
             :inline="true" :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">
      <command-item-container>
        <el-form-item
            :label-width="labelWidth"
            class="cmd-component"
            label="URL"
            prop="webUrl">
          <el-input v-model="currentCommand.vo.webUrl" :placeholder="$t('ui.url')" size="small"
                    style="width: 618px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-tooltip
              class="item"
              effect="dark"
              placement="right">
            <div slot="content">{{ $t('permission.project_ui_scenario.open_url_content') }}</div>
            <i :style="{'font-size': 10 + 'px', 'margin-left': 3 + 'px'}" class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
      </command-item-container>
      <command-item-container>
        <el-form-item
            :label-width="labelWidth"
            class="cmd-component"
            prop="blank"
            label=" "
        >
          <el-checkbox v-model="currentCommand.vo.blank">{{ $t("ui.open_new") }}</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-tooltip
              class="item"
              effect="dark"
              placement="right">
            <div slot="content">{{ $t('ui.open_new_tip') }}</div>
            <i :style="{'font-size': 10 + 'px', 'margin-left': 3 + 'px'}" class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
      </command-item-container>
    </el-form>
  </div>
</template>

<script>
import argtype from "../argtype"
import UiCmdSwitcher from "./UiCmdSwitcher";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import paramDefinition from "@/business/definition/command/param-definition";
import CommandItemContainer from "./CommandItemContainer";
import {commandFromValidate} from "@/business/automation/ui-automation";

export default {
  name: "Open",
  props: {
    value: Object,
  },
  components: {
    CommandItemContainer,
    ...argtype,
    UiCmdSwitcher
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
    targetVORule() {
      return this.getParamRule('target');
    },
    valueVORule() {
      return this.getParamRule('value');
    },
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    }
  },
  data() {
    return {
      isReadOnly: false,
      labelWidth: "120px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      loading: false,
      rules: {
        webUrl: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
      },
      atomicCommands: [],
      currentCommand: {}
    }
  },
  methods: {
    validate() {
      return commandFromValidate(this, 'cmdForm');
    },
    getParamRule(name) {
      if (this.currentCommand.type == "MsUiCommand") {
        // 找到指令定义中参数的类型
        let argType = this.commandDefinition[this.currentCommand.command][name];
        if (this.rules[argType]) {
          return this.rules[argType];
        }
        return this.rules['noValidator'];
      }
    },
  }
}
</script>

<style scoped>
</style>
