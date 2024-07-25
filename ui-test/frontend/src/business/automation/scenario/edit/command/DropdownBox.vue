<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand.vo" size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">

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

      <command-item-container>
        <el-form-item class="cmd-component"
                      :label="$t('ui.sub_item')"
                      prop="subItemType">
          <el-select v-model="currentCommand.vo.subItemType" size="small" >
            <el-option :key="o.value" :label="o.label" :value="o.value" v-for="o in subItemTypes"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          class="cmd-component"
          prop="subItem">
          <el-input v-if="currentCommand.vo.subItemType === 'index'"
                    v-model="currentCommand.vo.subItem" :placeholder="$t('ui.value')" size="small" type="number"></el-input>
          <el-input v-if="currentCommand.vo.subItemType !== 'index'"
                    v-model="currentCommand.vo.subItem" :placeholder="$t('ui.value')" size="small"></el-input>
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

export default {
  name: "DropdownBox",
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
  created() {
    this.currentCommand = this.value;
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
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
  },
  data() {
    return {
      optContents: [
        {label: this.$t("ui.select"), value: "select"},
        {label: this.$t("ui.cancel"), value: "removeSelection"}
      ],
      subItemTypes: [
        {label: this.$t("ui.option"), value: "option"},
        {label: this.$t("ui.index"), value: "index"},
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
        optContent: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        subItemType: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        subItem: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],

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
