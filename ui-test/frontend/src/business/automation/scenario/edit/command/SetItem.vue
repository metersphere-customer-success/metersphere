<template>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="medium"
             v-loading="loading" :rules="rules" ref="cmdForm" :inline="true" :inline-message="true">

      <opt-content-form-item
        :current-command="currentCommand"
        :opt-contents="optContents"
      />

      <locator-form
        :label="$t('ui.operation_object')"
        :current-command="currentCommand"
        :label-width="labelWidth"
        ref="locatorForm"
      />

    </el-form>
</template>

<script>
import argtype from "../argtype"
import UiCmdSwitcher from "./UiCmdSwitcher";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import paramDefinition from "@/business/definition/command/param-definition";
import OptContentFormItem from "./component/OptContentFormItem";
import LocatorForm from "./component/LocatorForm";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "SetItem",
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
        {label: this.$t("ui.select"), value: "check"},
        {label: this.$t("ui.cancel"), value: "uncheck"}
      ],
      subItemTypes: [
        {label: this.$t("ui.option"), value: "option"},
        {label: this.$t("ui.index"), value: "index"},
        {label: this.$t("ui.text"), value: "text"},
        {label: this.$t("ui.s_value"), value: "value"},
      ],
      isReadOnly: false,
      labelWidth: "120px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: {
        optContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}]
      },
      atomicCommands: []
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
