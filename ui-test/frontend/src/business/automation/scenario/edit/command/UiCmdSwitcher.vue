<template>
  <div>
    <el-form :disabled="isReadOnly"
             :model="currentCommand"
             size="medium"
             :inline="true"
             :inline-message="true"
             :rules="rules"
             ref="cmdForm">
      <command-item-container>
        <el-form-item
            class="cmd-component"
            :label="$t('commons.name')"
            :label-width="labelWidth"
            prop="name">
          <el-input
              style="width: 618px"
              v-model="currentCommand.name"
              :placeholder="$t('commons.member.input_name')"
              size="small"
              maxlength="100"
              show-word-limit/>
        </el-form-item>
      </command-item-container>
      <command-item-container>
        <el-form-item
            class="cmd-component"
            :label="$t('ui.step_type')"
            :label-width="labelWidth"
            prop="command">
          <el-select
              v-model="command" @change="changeCmdType"
              size="small"
              style="font-size: larger"
              :class="[cmds && cmds.length > 1 ? '' : 'hideSelect']"
              :disabled="isReadOnly">
            <el-option v-for="c in cmds" :key="c.name" :label="$t('ui.'+c.name)"
                       :value="c.name"></el-option>
          </el-select>
          <el-tooltip class="item" effect="dark" placement="right" v-if="popoverDesc && command!=='cmdFileUpload'">
            <div slot="content">{{ desc }}</div>
            <i :style="{'font-size': 10 + 'px', 'margin-left': 10 + 'px'}" class="el-icon-info"></i>
          </el-tooltip>
          <span style="margin-left: 20px;color:#7F7F7F" v-if="!popoverDesc">{{ desc }}</span>
        </el-form-item>
      </command-item-container>
    </el-form>

  </div>
</template>

<script>
import allCommandDefinition, {
  groupCommandDefinition,
  commandDefinitionMap
} from "@/business/definition/command/all-command-definition"
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import { findIndex, cloneDeep } from 'lodash-es'
import {commandFromValidate, createCommandByDefinition} from "@/business/automation/ui-automation";
import {COMMAND_TYPE_ATOM} from "@/business/definition/command/command-type";
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "UiCmdSwitcher",
  components: {CommandItemContainer},
  data() {
    return {
      command: null,
      isReadOnly: false,
      cmds: [],
      desc: "",
      rules: {
        name: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur', max: 200}],
        command: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
      },
      commandStore: commandStore,
      popoverDesc: false
    }
  },
  props: {
    currentCommand: Object,
    labelWidth: {
      type: String,
      default() {
        return '120px'
      }
    }
  },
  computed: {},
  mounted() {
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
  },
  methods: {
    validate() {
      return commandFromValidate(this, 'cmdForm');
    },
    setCommand(command) {
      let index = findIndex(allCommandDefinition, c => c.name == command);
      if (index == -1) {
        return;
      }
      this.cmds = cloneDeep(groupCommandDefinition[allCommandDefinition[index].viewType] || []);
      for (const i in this.cmds) {
        let cmd = this.cmds[i];
        if (cmd.name === command) {
          if ("en-US" == this.$i18n.locale) {
            this.desc = cmd.enDesc ? cmd.enDesc : "";
          } else if ("zh-CN" == this.$i18n.locale) {
            this.desc = cmd.cnDesc ? cmd.cnDesc : "";
          } else {
            this.desc = cmd.twDesc ? cmd.twDesc : "";
          }
          this.popoverDesc = cmd.popoverDesc;
          break;
        }
      }
      this.$nextTick(() => {
        this.command = command;
      });
    },
    changeCmdType() {
      let cmdDefinition = null;
      if (atomicCommandDefinition[this.command]) {
        cmdDefinition = atomicCommandDefinition[this.command];
        cmdDefinition.commandType = COMMAND_TYPE_ATOM;
      } else {
        cmdDefinition = commandDefinitionMap[this.command];
      }
      let currentCommandName = this.currentCommand.name;
      let newCmd = createCommandByDefinition(cmdDefinition);
      let currentCommand = commandStore.selectCommand;
      let index = currentCommand.index;
      let hashTree = currentCommand.hashTree;
      Object.assign(currentCommand, newCmd);
      currentCommand.index = index;
      currentCommand.hashTree = hashTree;

      let flag = false;
      for (const i in this.cmds) {
        let cmd = this.cmds[i];
        if (cmd.atomicCommands !== undefined) {
          for (const j in cmd.atomicCommands) {
            let atomicCommand = cmd.atomicCommands[j];
            if (atomicCommand.cnName === currentCommandName) {
              flag = true;
              break;
            }
          }
        }
        if (this.$t('ui.' + cmd.name) === currentCommandName) {
          flag = true;
        }
        if (flag) {
          break;
        }
      }
      if (flag) {
        // 名称未修改
        currentCommand.name = this.$t("ui." + currentCommand.command);
      } else {
        currentCommand.name = currentCommandName;
      }
      this.$emit("changeCurrentCommand", currentCommand);
    }
  }
}
</script>

<style scoped>
:deep(.hideSelect .el-select__caret.el-input__icon.el-icon-arrow-up) {
  display: none;
}
</style>
