<template>

  <div class="script-content">
    <el-form
      :disabled="isReadonly"
      ref="cmdForm"
      :rules="rules"
      :model="command.vo"
      label-width="120px"
      label-position="left"
      class="form"
      :inline="true"
    >
      <el-form-item class="form-item">
        <el-row :gutter="10">
          <el-col :span="20">
            <!-- <proxy-locator
              :command-definition="atomicCommandDefinition"
              :param-definition="paramDefinition"
              v-model="command.vo"
              prop-name="locator"
            /> -->
            <locator-form
              :isReadonly="isReadonly"
              :showLabel="false"
              :current-command="command.vo"
              labelWidth="100%"
              prop="locator"
              ref="locatorForm"
            />
          </el-col>
          <el-col :span="4">
            <el-checkbox v-model="command.vo.failOver"
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
        <el-row :gutter="10">
          <el-col :span="7">
            <!--                <label for="">断言方式：</label>-->
            <el-form-item prop="assertType">
              <el-select
                style="width: 180px"
                v-model="command.vo.assertType"
                :placeholder="$t('ui.validate_type')"
                size="small"
                filterable=""
              >
                <el-option
                  v-for="item in assertTypeOptionsList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="13">
            <el-form-item prop="assertValue">
              <el-input
                v-model="command.vo.assertValue"
                :placeholder="$t('ui.expect_value')"
                size="small"
                :maxlength="maxLength"
                show-word-limit
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>

      <ui-more-btn
        :isReadonly="isReadonly"
        style="position: relative;top : 10px; right :10px"
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
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";
import ProxyLocator from "@/business/automation/scenario/edit/argtype/ProxyLocator";
import paramDefinition from "@/business/definition/command/param-definition";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import LocatorForm from "@/business/automation/scenario/edit/command/component/LocatorForm";
import UiMoreBtn from "@/business/automation/scenario/component/UiMoreBtn";
import {VALIDATION_MAX_LENGTH} from "@/business/automation/ui-component-constants";

export default {
  name: "UiValidateDropdownComponent",
  components: {
    UiBaseComponent,
    MsCodeEdit,
    ApiBaseComponent,
    ProxyLocator,
    LocatorForm,
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
      atomicCommandDefinition: atomicCommandDefinition,
      paramDefinition: paramDefinition,
      operate: "",
      confirmWindowOptionsList: [
        {label: this.$t('commons.yes'), value: true},
        {label: this.$t('commons.no'), value: false},
      ],
      assertTypeOptionsList: [
        {label: this.$t("ui.select_value"), value: atomicCommandDefinition.assertSelectedValue.name},
        {label: this.$t("ui.select_label"), value: atomicCommandDefinition.assertSelectedLabel.name},
        {label: this.$t("ui.not_select_value"), value: atomicCommandDefinition.assertNotSelectedValue.name},
      ],
      rules: {
        assertType: [{required: true, message: " ", trigger: "change"}],
        assertValue: [{required: true, message: " ", trigger: "blur"}],
      },
      maxLength : VALIDATION_MAX_LENGTH
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
      if(!this.command.enable){
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
  height: calc(15vh);
  min-height: 20px;
}

.form-item {
  margin-bottom: 5px;
}
</style>
