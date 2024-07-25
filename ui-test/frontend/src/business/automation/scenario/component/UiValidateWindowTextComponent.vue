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
          <el-col :span="6">
            <el-select
                v-model="command.vo.confirmWindow"
                size="small"
                :placeholder="$t('ui.confirm_after_success')"
            >
              <el-option
                  v-for="item in confirmWindowOptionsList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              >
              </el-option>
            </el-select>
          </el-col>
          <el-col :span="10">
            <el-form-item prop="text" style="width: 100%;">
              <el-input
                  v-model="command.vo.text"
                  :placeholder="$t('ui.input_expect_text')"
                  size="small"
                  :maxlength="maxLength"
                  show-word-limit
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="2">
            <el-tooltip
                effect="dark"
                placement="top-start"
                :content="$t('ui.input_window_tip')"
            >
              <i class="el-icon-info pointer"/>
            </el-tooltip>
          </el-col>
          <el-col :span="6">
            <el-checkbox v-model="command.vo.failOver" :disabled="true"
            >{{ $t("ui.fail_over") }}
            </el-checkbox
            >
            <el-tooltip
                effect="dark"
                placement="top-start"
                :content="$t('ui.validate_tip')"
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
  name: "UiValidateWindowTextComponent",
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
      confirmWindowOptionsList: [
        {label: this.$t("commons.yes"), value: true},
        {label: this.$t("commons.no"), value: false},
      ],
      rules: {
        text: [{required: true, message: " ", trigger: "blur"}],
      },
      maxLength: VALIDATION_MAX_LENGTH
    };
  },
  mounted() {
    this.command.active = false;
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
