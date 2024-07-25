<template>
  <div class="script-content">
    <el-form
      ref="cmdForm"
      :rules="rules"
      :model="command.vo"
      label-position="left"
      :inline="true"
      :disabled="isReadonly"
    >
      <el-form-item class="form-item">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item prop="extractType">
              <el-select
                style="width: 250px"
                v-model="command.vo.extractType"
                :disabled="isReadonly"
                :placeholder="$t('ui.extract_type')"
                size="small"
              >
                <el-option
                  v-for="item in extractTypeOptionsList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="varName">
              <el-input
                style="width: 250px"
                show-word-limit
                maxlength="60"
                :disabled="isReadonly"
                v-model="command.vo.varName"
                :placeholder="$t('ui.input_var')"
                size="small"
              ></el-input>
            </el-form-item>
            <el-tooltip class="item" effect="dark" placement="right">
              <div slot="content">{{ $t('ui.extract_return_tip') }}</div>
              <i
                :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
                class="el-icon-info"
              ></i>
            </el-tooltip>
          </el-col>
        </el-row>

        <command-item-container
          v-if="
                command.vo.extractType && command.vo.extractType === 'store'
              "
        >
          <el-form-item prop="varValue">
            <el-input
              v-model="command.vo.varValue"
              :disabled="isReadonly"
              :placeholder="$t('ui.sample_obj')"
              size="small"
            ></el-input>
          </el-form-item>
          <el-checkbox
            style="margin-left: 3px"
            :disabled="isReadonly"
            v-model="command.vo.ifString"
          />
          <span class="input-label">{{ $t("ui.is_string") }}</span>

          <el-tooltip class="item" effect="dark" placement="right">
            <div slot="content">
              {{ $t('ui.like_string_tip') }}<br/>
              {{ $t('ui.like_string_tip2')}}
            </div>
            <i
              :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
              class="el-icon-info"
            ></i>
          </el-tooltip>
        </command-item-container>

        <command-item-container
          v-if="
                command.vo.extractType &&
                (command.vo.extractType === 'storeText' ||
                  command.vo.extractType === 'storeValue')
              "
        >
          <locator-form
            :showLabel="false"
            :current-command="command.vo"
            labelWidth="100%"
            prop="locator"
            ref="locatorForm"
            :isReadonly="isReadonly"
          />
        </command-item-container>

        <command-item-container
          v-if="
                command.vo.extractType &&
                command.vo.extractType === 'storeAttribute'
              "
        >
          <locator-form
            :showLabel="false"
            :current-command="command.vo"
            labelWidth="100%"
            prop="locator"
            ref="locatorForm"
            :isReadonly="isReadonly"
          />
          <el-form-item prop="attributeName">
            <el-input
              v-model="command.vo.attributeName"
              :placeholder="$t('ui.ele_pro')"
              size="small"
            ></el-input>
          </el-form-item>
          <el-tooltip class="item" effect="dark" placement="right">
            <div slot="content">
              {{ $t("ui.like_ele_tip") }}
            </div>
            <i
              :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
              class="el-icon-info"
            ></i>
          </el-tooltip>
        </command-item-container>

        <command-item-container
          v-if="
                command.vo.extractType &&
                command.vo.extractType === 'storeCssAttribute'
              "
        >
          <locator-form
            :showLabel="false"
            :current-command="command.vo"
            labelWidth="100%"
            prop="locator"
            ref="locatorForm"
            :isReadonly="isReadonly"
          />
          <el-form-item prop="attributeName">
            <el-input
              v-model="command.vo.attributeName"
              :disabled="isReadonly"
              :placeholder="$t('ui.ele_css_attr')"
              size="small"
            ></el-input>
          </el-form-item>
          <el-tooltip class="item" effect="dark" placement="right">
            <div slot="content">
              {{ $t("ui.ele_css_tip1") }}
            </div>
            <i
              :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
              class="el-icon-info"
            ></i>
          </el-tooltip>
        </command-item-container>

        <el-row
          :gutter="10"
          v-if="
                command.vo.extractType &&
                command.vo.extractType === 'storeXpathCount'
              "
        >
          <el-col :span="8">
            <el-input
              :value="$t('ui.elementLocator')"
              :disabled="true"
              size="small"
            ></el-input>
          </el-col>
          <el-col :span="8">
            <el-input
              :value="$t('xpath')"
              :disabled="true"
              size="small"
            ></el-input>
          </el-col>
          <el-col :span="8">
            <el-form-item prop="xpathAddress">
              <el-tooltip placement="top-start">
                <span slot="content" class="locator-tooltip">
                  {{ command.vo.xpathAddress || $t('commons.input_content') }}
                </span>
                <el-input
                    :disabled="isReadonly"
                  v-model="command.vo.xpathAddress"
                  :placeholder="$t('ui.xpath_locator')"
                  size="small"
                ></el-input>
              </el-tooltip>

            </el-form-item>
            <el-tooltip class="item" effect="dark" placement="right">
              <div slot="content">
                {{ $t('ui.xpath_tip') }}
              </div>
              <i
                :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
                class="el-icon-info"
              ></i>
            </el-tooltip>
          </el-col>
        </el-row>
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
import paramDefinition from "@/business/definition/command/param-definition";
import CommandItemContainer from "@/business/automation/scenario/edit/command/CommandItemContainer";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import LocatorForm from "@/business/automation/scenario/edit/command/component/LocatorForm";
import UiMoreBtn from "@/business/automation/scenario/component/UiMoreBtn";

export default {
  name: "UiExtractElementComponent",
  components: {
    UiMoreBtn,
    UiBaseComponent,
    MsCodeEdit,
    ApiBaseComponent,
    ProxyLocator,
    CommandItemContainer,
    LocatorForm,
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
  computed: {
    commandDefinition() {
      return atomicCommandDefinition;
    },
  },
  data() {
    return {
      atomicCommandDefinition: atomicCommandDefinition,
      paramDefinition: paramDefinition,
      operate: "",
      extractTypeOptionsList: [
        {label: this.$t("ui.store"), value: atomicCommandDefinition.store.name},
        {
          label: this.$t("ui.store_text"),
          value: atomicCommandDefinition.storeText.name,
        },
        {
          label: this.$t("ui.store_value"),
          value: atomicCommandDefinition.storeValue.name,
        },
        {
          label: this.$t("ui.store_attr"),
          value: atomicCommandDefinition.storeAttribute.name,
        },
        {
          label: this.$t("ui.store_css_attr"),
          value: atomicCommandDefinition.storeCssAttribute.name,
        },
        {
          label: this.$t("ui.store_xpath_count"),
          value: atomicCommandDefinition.storeXpathCount.name,
        },
      ],
      rules: {
        extractType: [{required: true, message: " ", trigger: "change"}],
        xpathAddress: [{required: true, message: " ", trigger: "blur"}],
        varName: [{required: true, message: " ", trigger: "blur"}],
        varValue: [{required: true, message: " ", trigger: "blur"}],
        attributeName: [{required: true, message: " ", trigger: "blur"}],
      },
    };
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
  mounted() {
    this.command.active = false;
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

.form-item {
  margin: 0;
  padding-left: 10px;
}

.locator-tooltip {
  text-align: justify;
  word-break: break-all
}
</style>
