<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="medium"
             :inline="true" :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">
      <opt-content-form-item
        :current-command="currentCommand"
        :opt-contents="optContents"
        :tips="$t('ui.sw_tip1')"
      />
      <command-item-container
        v-if="currentCommand.vo.optContent && currentCommand.vo.optContent === 'switchTheWindowById'">
        <el-form-item
          class="cmd-component"
          prop="handleId"
          :label-width="labelWidth"
          :label="$t('ui.handle_id')">
          <el-input v-model="currentCommand.vo.handleId" :placeholder="$t('ui.window_handle')" size="small"
                    style="width: 600px;"></el-input>
        </el-form-item>
      </command-item-container>

      <command-item-container v-if="currentCommand.vo.optContent && currentCommand.vo.optContent === 'switchTheWindowByIndex'">
      <el-form-item
        class="cmd-component"
        prop="handleIndex"
        :label-width="labelWidth"
        :label="$t('ui.frame_index')">
        <el-input-number v-model="currentCommand.vo.handleIndex" :placeholder="$t('ui.window_index')" size="small" :min="1" type="number" ></el-input-number>
        <el-tooltip class="item" effect="dark" placement="right">
          <div slot="content">{{$t('ui.select_open_window')}}<br/>
            {{$t('ui.s_w_t1')}}
          </div>
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
import OptContentFormItem from "./component/OptContentFormItem";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "SelectWindow",
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
        // {label: this.$t("返回上一个窗口"), value: "backLastWindow"},
        {label: this.$t("ui.switch_by_id"), value: "switchTheWindowById"},
        // {label: this.$t("根据网页名称切换到指定窗口"), value: "switchTheWindowByName"},
        {label: this.$t("ui.switch_by_index"), value: "switchTheWindowByIndex"},
        {label: this.$t("ui.swicth_to_default"), value: "switchTheWindowToStart"},
      ],
      isReadOnly: false,
      labelWidth: '120px',
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      loading: false,
      rules: {
        handleId: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        handleIndex: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        optContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
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
      if (this.commandDefinition[cmd]) {
        this.targetType = this.commandDefinition[cmd].target;
        this.valueType = this.commandDefinition[cmd].value;
      }
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
