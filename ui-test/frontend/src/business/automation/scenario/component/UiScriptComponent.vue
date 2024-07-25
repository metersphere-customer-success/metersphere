<template>

  <ui-base-component
    :isReadonly="isReadonly"
    color="#b8741a"
    background-color="#F9F1EA"
    :data="command"
    :draggable="draggable"
    :collapsable="true"
    :show-btn="true"
    :title="title"
    @copy="copyRow"
    @remove="remove"
    @active="active">

    <template v-slot:afterName>
      <ms-instructions-icon size="14" :content="$t('ui.script_tip')"/>
    </template>

    <template v-slot:request>
      <div class="script-content">
        <el-form ref="form" :model="command.vo" label-position="left" label-width="80px" :inline="true" :disabled="isReadonly">
          <el-form-item prop="scriptType" :label="$t('ui.script_type')">
            <el-select style="width: 180px" v-model="command.vo.scriptType" :placeholder="$t('ui.script_type')"
                       size="mini"
                       filterable="">
              <el-option
                v-for="item in scriptTypeOptionsList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item prop="returnType" :label="$t('ui.set_var')">
            <el-select style="width: 180px" v-model="command.vo.returnType" :placeholder="$t('commons.please_select')" size="mini"
                       filterable="">
              <el-option
                v-for="item in returnTypeOptionsList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item prop="returnValue">
            <el-input v-model="command.vo.returnValue" size="mini" v-if="command.vo.returnType == true"
                      :placeholder="$t('load_test.param_name')"></el-input>
          </el-form-item>
        </el-form>


        <ms-code-edit mode="javascript" :readOnly="isReadonly"
                      :data.sync="command.vo.script" theme="eclipse" :modes="['javascript']" class="code-edit" height="390px"
                      ref="codeEdit"/>
      </div>
    </template>

  </ui-base-component>
</template>

<script>

import ApiBaseComponent from "@/business/automation/scenario/common/ApiBaseComponent";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";

export default {
  name: "UiScriptComponent",
  components: {MsInstructionsIcon, UiBaseComponent, MsCodeEdit, ApiBaseComponent},
  props: {
    isReadonly:{
      type: Boolean,
      default: false,
    },
    command: {},
    innerStep: {
      type: Boolean,
      default: false,
    },
    node: {},
    showBtn: {
      type: Boolean,
      default: true,
    },
    draggable: {
      type: Boolean,
      default: false,
    },
    title: String,
  },
  data() {
    return {
      scriptTypeOptionsList: [
        {label: this.$t('ui.sync'), value: "sync"},
        {label: this.$t('ui.async'), value: "async"},
      ],
      returnTypeOptionsList: [
        {label: this.$t('ui.return'), value: true},
        {label: this.$t('ui.no_return'), value: false},
      ],
    }
  },
  mounted() {
    this.command.active = false;
  },
  methods: {
    remove() {
      this.$emit('remove', this.command, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.command, this.node);
    },
    active() {
      this.$set(this.command, 'active', !this.command.active);
    }
  }
}
</script>

<style scoped>
.script-content {
  height: calc(100vh - 570px);
  min-height: 440px;
}
.code-edit {
  height: calc(100vh - 1950px);
  min-height: 390px;
}
</style>
