<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="medium"
             :inline="true" :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">
      <opt-content-form-item
        :current-command="currentCommand"
        :opt-contents="optContents"
        :tips="currentCommand.vo.optContent == 'setSize' ? $t('ui.ws_tip1') : null"
      />

      <command-item-container v-if="currentCommand.vo.optContent && currentCommand.vo.optContent == 'setSize'">
        <el-form-item
          class="cmd-component"
          :label="$t('ui.size')">
          <el-input-number v-model="currentCommand.vo.coordX" :placeholder="$t('ui.width')" controls-position="right" size="small"
                           :min="0"></el-input-number>
          x
          <el-input-number v-model="currentCommand.vo.coordY" :placeholder="$t('ui.height')" controls-position="right" size="small"
                           :min="0"></el-input-number>
          <el-tooltip class="item" effect="dark" placement="right">
            <div slot="content">{{$t('ui.by_pixel')}}</div>
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
import OptContentFormItem from "./component/OptContentFormItem";
import {commandFromValidate} from "@/business/automation/ui-automation";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "SetWindowSize",
  props: {
    selectCommand: Object,
    value: Object,
  },
  components: {
    OptContentFormItem,
    CommandItemContainer,
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
      optContents: [
        {label: this.$t("ui.fullscreen"), value: "fullScreen"},
        {label: this.$t("ui.use_pixel"), value: "setSize"}
      ],
      isReadOnly: false,
      labelWidth: "100px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      loading: false,
      rules: {
        optContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
      },
      atomicCommands: []
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
      this.targetType = this.commandDefinition[cmd].target;
      this.valueType = this.commandDefinition[cmd].value;
      //根据参数定义的类型选择不同组件
      if (this.targetType) {
        this.targetTypeComponent = this.paramDefinition[this.targetType];
      }
      if (this.valueType) {
        this.valueTypeComponent = this.paramDefinition[this.valueType];
      }
      if (this.$refs.cmdSwitcher) {
        this.$refs.cmdSwitcher.setCommand(cmd);
      }
      this.loading = false;
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
