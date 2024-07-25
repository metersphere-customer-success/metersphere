<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="medium"
             :inline="true" :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">
      <opt-content-form-item
        :current-command="currentCommand"
        :opt-contents="optContents"
        :tips="$t('ui.sf_tip')"
      />
      <command-item-container
        v-if="currentCommand.vo.optContent && currentCommand.vo.optContent === 'switchTheFrameByIndex'">
        <el-form-item
          class="cmd-component"
          prop="frameIndex"
          :label-width="labelWidth"
          :label="$t('ui.sf_index')">
          <el-input-number v-model="currentCommand.vo.frameIndex" :placeholder="$t('ui.sf_index')" size="small" type="number" :min = "0"></el-input-number>
          <el-tooltip class="item" effect="dark" placement="right">
            <div slot="content">{{$t('ui.select_index')}}<br/>
              {{$t('ui.select_f_tip')}}
            </div>
            <i :style="{'font-size': 10 + 'px', 'margin-left': 3 + 'px'}" class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
      </command-item-container>

      <locator-form
        v-if="currentCommand.vo.optContent && currentCommand.vo.optContent === 'default'"
        :current-command="currentCommand"
        :label-width="labelWidth"
        ref="locatorForm"
      />

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
import LocatorForm from "@/business/automation/scenario/edit/command/component/LocatorForm";
import {
  commandFromAndLocatorFromValidate,
} from "@/business/automation/ui-automation";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "SelectFrame",
  props: {
    selectCommand: Object,
    value: Object,
  },
  components: {
    LocatorForm,
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
        {label: this.$t("ui.exit_frame"), value: "switchTheFrameToParent"},
        {label: this.$t("ui.select_frame_index"), value: "switchTheFrameByIndex"},
        {label: this.$t("ui.select_by_location"), value: "default"},
      ],
      isReadOnly: false,
      labelWidth: "120px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      loading: false,
      rules: {
        frameIndex: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
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
