<template>
  <div class="script-content">
    <el-form
        :disabled="isReadonly"
        ref="cmdForm"
        :model="command.vo"
        label-width="120px"
        label-position="top"
        :inline="true"
        class="form"
        :rules="rules"
    >
      <el-form-item class="form-item">
        <el-row :gutter="10">
          <el-col :span="4">
            <el-form-item prop="varName" style="width: 100%;">
              <el-input
                  v-model="command.vo.varName"
                  :placeholder="$t('ui.input_var')"
                  size="small"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item prop="optType" style="width: 100%;">

              <el-select v-model="command.vo.optType" :placeholder="$t('ui.please_select')" size="small"
                         @change="setVarValue">
                <el-option
                    v-for="item in optTypeList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                </el-option>
              </el-select>

            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item prop="varValue" style="width: 100%;">
              <el-input
                  v-model="command.vo.varValue"
                  :placeholder="$t('ui.input_expect')"
                  size="small"
                  :maxlength="maxLength"
                  show-word-limit
                  :disabled="disableVarValue"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="2">
            <el-tooltip
                effect="dark"
                placement="top-start"
                :content="$t('ui.var_tip')"
            >
              <i class="el-icon-info pointer"/>
            </el-tooltip>
          </el-col>
          <el-col :span="6">
            <el-checkbox v-model="command.vo.failOver"
                         :disabled="isReadonly"
            >{{ $t("ui.fail_over") }}
            </el-checkbox
            >
            <el-tooltip
                effect="dark"
                placement="top-start"
                :content="$t('ui.validate_tip')">
              >
              <i class="el-icon-info pointer"/>
            </el-tooltip>
          </el-col>
        </el-row>
      </el-form-item>

      <ui-more-btn
          :isReadonly="isReadonly"
          :command="command"
          @copyRow="copyRow"
          @remove="remove"
      />

    </el-form>
  </div>

</template>

<script>
import ApiBaseComponent from "@/business/automation/scenario/common/ApiBaseComponent";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import {createCommand} from "@/business/automation/ui-automation";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import UiMoreBtn from "@/business/automation/scenario/component/UiMoreBtn";
import {VALIDATION_MAX_LENGTH} from "@/business/automation/ui-component-constants";

export default {
  name: "UiValidateValueComponent",
  components: {
    UiBaseComponent,
    MsCodeEdit,
    ApiBaseComponent,
    UiMoreBtn,
  },
  props: {
    isReadonly: {
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
      operate: "",
      rules: {
        varName: [{required: true, message: " ", trigger: "blur"}],
        varValue: [{required: true, message: " ", trigger: "blur"}],
      },
      maxLength: VALIDATION_MAX_LENGTH,
      optTypeList: [
        {label: this.$t("ui.equal"), value: "equal"},
        {label: this.$t("ui.not_equal"), value: "notEqual"},
        {label: this.$t("ui.contain"), value: "contain"},
        {label: this.$t("ui.not_contain"), value: "notContain"},
        {label: this.$t("ui.greater"), value: "greater"},
        {label: this.$t("ui.greater_equal"), value: "greaterEqual"},
        {label: this.$t("ui.lower"), value: "lower"},
        {label: this.$t("ui.lower_equal"), value: "lowerEqual"},
        {label: this.$t("ui.null"), value: "null"},
        {label: this.$t("ui.not_null"), value: "notNull"},
      ],
      disableVarValue: false
    };
  },
  mounted() {
    this.command.active = false;
    this.setVarValue();
    if (this.command.vo && !this.command.vo.optType) {
      this.$set(this.command.vo, "optType", "equal");
    }
  },
  watch: {
    "command.validateResult": {
      immediate: true,
      deep: true,
      handler(v) {
        if (v && !v.verify) {
          this.validate();
        }
      },
    },
  },
  methods: {
    validate() {
      if (!this.command.enable) {
        return true;
      }
      return commandFromAndLocatorFromValidate(this);
    },
    remove() {
      this.$emit("remove", this.command, this.node);
    },
    copyRow() {
      this.$emit("copyRow", this.command, this.node);
    },
    active() {
      this.command.active = !this.command.active;
    },
    add() {
      let newCmd = createCommand(this.operate);
      newCmd.active = false;
      newCmd.index = this.command[this.prop]
          ? this.command[this.prop].length + 1
          : 0;
      this.addComponent(newCmd);
    },
    addComponent(component) {
      if (!this.command[this.prop]) {
        this.command[this.prop] = [];
      }
      this.command[this.prop].push(component);
      this.sort();
      this.reload();
    },
    sort() {
      let index = 1;
      for (let i in this.command[this.prop]) {
        this.command[this.prop][i].index = Number(index);
        index++;
      }
    },
    reload() {
      this.visible = false;
      this.$nextTick(() => {
        this.visible = true;
      });
    },
    setVarValue() {
      if (this.command.vo.optType == "null" || this.command.vo.optType == "notNull") {
        this.command.vo.varValue = null;
        this.disableVarValue = true;
      } else {
        this.disableVarValue = false;
      }
    }
  },
};
</script>

<style scoped>
.script-content {
  height: calc(10vh);
  min-height: 20px;
}

.form-item {
  width: 80%;
}
</style>
