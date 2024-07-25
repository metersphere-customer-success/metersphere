<template>
  <div>
    <el-form ref="cmdForm" :model="command.vo" label-position="left" :inline="true" :rules="rules" :disabled="isReadonly">
      <el-form-item class="form-item">
        <el-select v-model="command.vo.extractType" style="width: 250px;" :placeholder="$t('ui.extract_type')" size="small">
          <el-option
            v-for="item in extractTypeOptionsList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>

      </el-form-item>
      <el-form-item class="form-item" v-if="command.vo.extractType && command.vo.extractType === 'storeWindowHandle'"
                    prop="webHandleVarName">
        <el-input style="width: 250px;" v-model="command.vo.webHandleVarName" show-word-limit maxlength="60"
                  :placeholder="$t('ui.input_handle_name')" size="small"></el-input>
        <el-tooltip class="item" effect="dark" placement="right">
          <div slot="content">{{ $t('ui.extract_return_tip') }}</div>
          <i :style="{'font-size': 10 + 'px', 'margin-left': 3 + 'px'}" class="el-icon-info"></i>
        </el-tooltip>
      </el-form-item>
      <el-form-item class="form-item" v-if="command.vo.extractType && command.vo.extractType === 'storeTitle'"
                    style="height: 36px;"  prop="webTitleVarName">
        <el-input style="width: 250px;" v-model="command.vo.webTitleVarName" show-word-limit maxlength="60"
                  :placeholder="$t('ui.input_window_title')" size="small"></el-input>
        <el-tooltip class="item" effect="dark" placement="right">
          <div slot="content">{{ $t('ui.extract_return_tip') }}</div>
          <i :style="{'font-size': 10 + 'px', 'margin-left': 3 + 'px'}" class="el-icon-info"></i>
        </el-tooltip>
      </el-form-item>

      <ui-more-btn
        :command="command"
        @copyRow="copyRow"
        @remove="remove"
        :isReadonly="isReadonly"
      />

    </el-form>
  </div>
</template>

<script>

import ApiBaseComponent from "@/business/automation/scenario/common/ApiBaseComponent";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import {createCommand} from "@/business/automation/ui-automation";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";
import ProxyLocator from "@/business/automation/scenario/edit/argtype/ProxyLocator";
import paramDefinition from '@/business/definition/command/param-definition';
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import UiMoreBtn from "@/business/automation/scenario/component/UiMoreBtn";

export default {
  name: "UiExtractWindowComponent",
  components: {
    UiBaseComponent,
    MsCodeEdit,
    ApiBaseComponent,
    ProxyLocator,
    CommandItemContainer,
    UiMoreBtn
  },
  props: {
    isReadonly: {
      type: Boolean,
      default: false,
    },
    command: {}
    ,
    innerStep: {
      type: Boolean,
      default:
        false,
    }
    ,
    node: {}
    ,
    showBtn: {
      type: Boolean,
      default:
        true,
    }
    ,
    draggable: {
      type: Boolean,
      default:
        false,
    }
    ,
    title: String,
  }
  ,
  data() {
    return {
      atomicCommandDefinition: atomicCommandDefinition,
      paramDefinition: paramDefinition,
      operate: "",
      extractTypeOptionsList: [
        {label: this.$t("ui.store_window_handle"), value: atomicCommandDefinition.storeWindowHandle.name},
        {label: this.$t("ui.store_title"), value: atomicCommandDefinition.storeTitle.name},
      ],
      rules: {
        webHandleVarName: [{required: true, message: " ", trigger: "blur"}],
        webTitleVarName: [{required: true, message: " ", trigger: "blur"}],
      },
    }
  }
  ,
  mounted() {
    this.command.active = false;
  }
  ,
  watch: {
    'command.active'() {
      if (!this.command.vo.extractType) {
        // 如果 extractType 为 null 则设置初始值
        this.command.vo.extractType = "storeWindowHandle";
      }
    },
    "command.validateResult": {
      immediate: true,
      deep: true,
      handler(v) {
        if (v && !v.verify) {
          this.validate();
        }
      },
    },
  }
  ,
  methods: {
    validate() {
      if(!this.command.enable){
        return true;
      }
      return commandFromAndLocatorFromValidate(this);
    },
    remove() {
      this.$emit('remove', this.command, this.node);
    }
    ,
    copyRow() {
      this.$emit('copyRow', this.command, this.node);
    }
    ,
    active() {
      this.command.active = !this.command.active;
    }
    ,
    add() {
      let newCmd = createCommand(this.operate);
      newCmd.active = false;
      newCmd.index = this.command[this.prop] ? this.command[this.prop].length + 1 : 0;
      this.addComponent(newCmd);
    }
    ,
    addComponent(component) {
      if (!this.command[this.prop]) {
        this.command[this.prop] = [];
      }
      this.command[this.prop].push(component);
      this.sort();
      this.reload();
    }
    ,
    sort() {
      let index = 1;
      for (let i in this.command[this.prop]) {
        this.command[this.prop][i].index = Number(index);
        index++;
      }
    }
    ,
    reload() {
      this.visible = false;
      this.$nextTick(() => {
        this.visible = true;
      })
    }
    ,
  }
}
</script>

<style scoped>

.form-item {
  margin: 0;
  padding-left: 10px;
}
</style>
