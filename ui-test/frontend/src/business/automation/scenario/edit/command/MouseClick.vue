<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">

      <command-item-container>
        <el-form-item
          class="cmd-component"
          prop="clickType"
          :label="$t('ui.click_type')"
          :label-width="labelWidth">
          <el-select v-model="currentCommand.vo.clickType">
            <el-option :key="o.value" :label="o.label" :value="o.value" v-for="o in optContents"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item
          class="cmd-component">
          <el-checkbox :disabled = "disableForcedClick" v-model="currentCommand.vo.enableClickLocation" :label="$t('ui.set_click_point')"/>
          <el-tooltip effect="dark" placement="top-start" :content="$t('ui.click_tip_1')">
            <i class="el-icon-info pointer"/>
          </el-tooltip>
        </el-form-item>

        <el-form-item
           v-if = "forcedClick"
           class="cmd-component">
          <el-checkbox v-model="currentCommand.vo.enableForcedClick" :label="$t('ui.click_force')"/>
          <el-tooltip effect="dark" placement="top-start" :content="$t('ui.click_tip_3')">
            <i class="el-icon-info pointer"/>
          </el-tooltip>
        </el-form-item>
      </command-item-container>

      <locator-form
        :label="$t('ui.element_location')"
        :current-command="currentCommand"
        :label-width="labelWidth"
        ref="locatorForm"
      />

      <command-item-container v-if="currentCommand.vo.enableClickLocation">
        <el-form-item
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.click_point')">
          <el-input-number controls-position="right" v-model="currentCommand.vo.clickLocationX"
                           :placeholder="$t('ui.x')"/>
        </el-form-item>
        <el-form-item class="cmd-component">
          <el-input-number controls-position="right" v-model="currentCommand.vo.clickLocationY"
                           :placeholder="$t('ui.y')"/>
        </el-form-item>
        <el-tooltip effect="dark" placement="top-start" :content="$t('ui.click_tip_2')">
          <i class="el-icon-info pointer"/>
        </el-tooltip>
      </command-item-container>

    </el-form>
  </div>
</template>

<script>
import argtype from "../argtype";
import UiCmdSwitcher from "./UiCmdSwitcher";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import paramDefinition from "@/business/definition/command/param-definition";
import CommandItemContainer from "./CommandItemContainer";
import {
  commandFromAndLocatorFromValidate,
  commandFromValidate
} from "@/business/automation/ui-automation";
import LocatorForm from "@/business/automation/scenario/edit/command/component/LocatorForm";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "MouseClick",
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
    },
    disableForcedClick(){
      return this.currentCommand.vo && this.currentCommand.vo.clickType === 'click' && this.currentCommand.vo.enableForcedClick;
    },
    forcedClick(){
      return this.currentCommand.vo && this.currentCommand.vo.clickType === 'click' && !this.currentCommand.vo.enableClickLocation;
    },
  },
  data() {
    return {
      optContents: [
        {label: this.$t("单击(左击)"), value: "click"},
	{label: this.$t("单击(右击)"), value: "rightClick"},
        {label: this.$t("ui.dclick"), value: "doubleClick"},
        {label: this.$t("ui.press"), value: "mouseDown"},
        {label: this.$t("ui.standup"), value: "mouseUp"}
      ],
      isReadOnly: false,
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: {
        clickType: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
      },
      atomicCommands: [],
      labelWidth: '120px'
    };
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
};
</script>

<style scoped>
</style>
