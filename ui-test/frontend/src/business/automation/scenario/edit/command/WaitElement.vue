<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="medium"
             v-loading="loading" :rules="rules" ref="cmdForm" :inline="true" :inline-message="true">

      <opt-content-form-item
        :current-command="currentCommand"
        :opt-contents="optContents"
      />

      <locator-form
        :current-command="currentCommand"
        :label-width="labelWidth"
        ref="locatorForm"
      />

      <command-item-container v-if="currentCommand.vo.optContent == 'waitForText' && refreshValidate() ">
        <el-form-item
          class="cmd-component"
          prop="waitText"
          :label-width="labelWidth"
          :label="$t('ui.wait_text')">
          <el-input v-model="currentCommand.vo.waitText" :placeholder="$t('ui.wait_text')" size="small"></el-input>
        </el-form-item>
        <el-tooltip effect="dark" placement="top-start" :content="waitText">
          <i class="el-icon-info pointer"/>
        </el-tooltip>
      </command-item-container>

      <command-item-container v-if="currentCommand.vo.optContent != 'waitForText' && refreshValidate()">
        <el-form-item
          class="cmd-component"
          prop="waitTime"
          :label-width="labelWidth"
          :label="$t('ui.wait_timeout')">
          <el-input-number v-model="currentCommand.vo.waitTime" type="number" :step="1000" :min = "0"
                    size="small"></el-input-number>
          ms
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
import OptContentFormItem from "@/business/automation/scenario/edit/command/component/OptContentFormItem";
import LocatorForm from "@/business/automation/scenario/edit/command/component/LocatorForm";
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "WaitElement",
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
    LocatorForm,
    OptContentFormItem,
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
        {label: this.$t("ui.wait_for_text"), value: "waitForText"},
        {label: this.$t("ui.wait_for_ele_pre"), value: "waitForElementPresent"},
        {label: this.$t("ui.wait_for_ele_visible"), value: "waitForElementVisible"},
        {label: this.$t("ui.wait_for_ele_not_visible"), value: "waitForElementNotVisible"},
        {label: this.$t("ui.wait_for_ele_not_pre"), value: "waitForElementNotPresent"},
        {label: this.$t("ui.wait_for_ele_edi"), value: "waitForElementEditable"},
        {label: this.$t("ui.wait_for_ele_not_edi"), value: "waitForElementNotEditable"},
      ],
      subItemTypes: [
        {label: this.$t("ui.option"), value: "option"},
        {label: this.$t("ui.index"), value: "index"},
        {label: this.$t("ui.text"), value: "text"},
        {label: this.$t("ui.s_value"), value: "value"},
      ],
      waitText: this.$t('ui.wait_tip'),
      isReadOnly: false,
      labelWidth: "120px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: {
        waitText: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        optContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
      },
      atomicCommands: []
    }
  },
  methods: {
    refreshValidate(){
      this.$nextTick(() => {
         this.$refs["cmdForm"].clearValidate();
      });
      return true;
    },
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

.el-form-item .el-select {
  width: 180px;
}

.el-form-item .el-input {
  width: 180px;
}
</style>
