<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">
      <command-item-container>
        <el-form-item
          class="cmd-component"
          prop="moveType"
          :label-width="labelWidth"
          :label="$t('ui.move_type')">
          <el-select v-model="currentCommand.vo.moveType">
            <el-option :key="o.value" :label="o.label" :value="o.value" v-for="o in optContents"></el-option>
          </el-select>
        </el-form-item>
      </command-item-container>

      <locator-form
        :label="currentCommand.vo.moveType === 'mouseMoveAt' ? $t('ui.mouse_start') : $t('ui.mouse_location')"
        :current-command="currentCommand"
        :label-width="labelWidth"
        ref="locatorForm"
      />

      <command-item-container v-if="currentCommand.vo.moveType === 'mouseMoveAt'">
        <el-form-item class="cmd-component" :label="$t('ui.relative_location')">
          <el-input-number controls-position="right" v-model="currentCommand.vo.moveLocationX"
                           :placeholder="$t('ui.x')"/>
        </el-form-item>
        <el-form-item class="cmd-component">
          <el-input-number controls-position="right" v-model="currentCommand.vo.moveLocationY"
                           :placeholder="$t('ui.y')"/>
        </el-form-item>
        <el-tooltip effect="dark" placement="top-start" :content="$t('ui.move_tip')">
          <i class="el-icon-info pointer"/>
        </el-tooltip>
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
import {
  commandFromAndLocatorFromValidate,
} from "@/business/automation/ui-automation";
import LocatorForm from "./component/LocatorForm";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "MouseMove",
  props: {
    selectCommand: Object,
    showSwitch: {
      type: Boolean,
      default: true
    },
    value: Object,
  },
  components: {
    LocatorForm,
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
        {label: this.$t("ui.mouse_out_e"), value: "mouseOut"},
        {label: this.$t("ui.mouse_in_e"), value: "mouseOver"},
        {label: this.$t("ui.mouse_e_to_c"), value: "mouseMoveAt"},
      ],
      isReadOnly: false,
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: {
        moveType: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
      },
      atomicCommands: [],
      labelWidth: '120px'
    }
  },
  methods: {
    validate() {
      if(!this.currentCommand.enable){
        return true;
      }
      return commandFromAndLocatorFromValidate(this);
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
    }
  }
}
</script>

<style scoped>
</style>
