<template>
  <div>
    <el-form v-loading="loading"
             size="small"
             label-width="120px"
             v-if="visible"
             :disabled="isReadOnly"
             :label-position="'right'"
             :model="currentCommand.vo"
             :inline="true"
             :inline-message="true"
             :rules="rules" ref="cmdForm">

      <!-- 类型选项 -->
      <command-item-container>
        <el-form-item
          prop="model"
          class="cmd-component"
          :label="$t('ui.condition_type')"
          :label-width="labelWidth">
          <el-select
            v-model="currentCommand.vo.model"
            @change="validate(true)"
            placeholder="$t('ui.please_select')">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <ms-instructions-icon size="14">
            <template>
              {{ $t('ui.condition_list') }}
              <br>
              {{ $t('ui.condition_exp') }}
            </template>
          </ms-instructions-icon>
        </el-form-item>
      </command-item-container>

      <command-item-container>
        <el-form-item
          v-if="currentCommand.vo.model === 'condition'"
          prop="conditions"
          class="cmd-component"
          :label-width="labelWidth">
          <combination-condition
            class="cmd-component"
            :filter-type-object="currentCommand.vo"
            :is-read-only="isReadOnly"
            :is-show-enable="true"
            :suggestions="null"
            :disable-variable-tip="true"
            :parameters="currentCommand.vo.conditions"/>
        </el-form-item>

        <el-form-item
          v-else
          prop="expression"
          class="cmd-component"
          :label-width="labelWidth"
          :label="$t('ui.expression')">
          <el-input v-model="currentCommand.vo.expression"/>
          <ms-instructions-icon size="14" :content="$t('ui.if_tip')"/>
        </el-form-item>

      </command-item-container>

    </el-form>
  </div>
</template>

<script>
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import CombinationCondition from "./mock/MockCombinationCondition";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import {commandFromValidate} from "@/business/automation/ui-automation";

export default {
  name: "If",
  components: {MsInstructionsIcon, CombinationCondition, CommandItemContainer},
  props: {
    selectCommand: Object,
    currentCommand: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      labelWidth: "120px",
      isReadOnly: false,
      loading: false,
      visible: true,
      rules: {
        model: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}],
        expression: [{required: true, message: this.$t('ui.param_null'), trigger: 'blur'}]
      },
      atomicCommands: [],
      options: [
        {
          value: 'condition',
          label: this.$t('ui.condition_list_')
        },
        {
          value: 'expression',
          label: this.$t('ui.condition_exp_')
        }
      ],
    }
  },
  watch: {
    currentCommand() {
      this.reload();
    }
  },
  mounted() {
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
  },
  methods: {
    validate(changeValue) {
      if (changeValue){
        this.$refs.cmdForm.clearValidate();
      } else {
        return commandFromValidate(this);
      }
    },
    reload() {
      this.visible = false;
      this.$nextTick(() => {
        this.visible = true;
      });
    }
  }
}
</script>

<style scoped>
.cmd-component :deep(.el-main) {
  overflow: visible;
}

:deep(.ms-instructions-icon) {
  line-height: 30px;

}
</style>
