<template>
  <div>
    <!-- Sence v2.4 -->
    <ms-form-divider :title="$t('ui.basic_information')" />
    <div class="base-info-wrap">
      <div class="name-row sub-row">
        <div class="label">{{ $t("ui.name") }}：</div>
        <div class="content">
          <el-input
            v-model="currentCommand.name"
            @change="nameChange"
            size="mini"
            maxlength="100"
            show-word-limit
          ></el-input>
        </div>
      </div>
      <div class="desc-row sub-row">
        <div class="label">{{ $t("commons.description") }}：</div>
        <div class="content">
          <el-input
            v-model="currentCommand.description"
            @change="descChange"
            size="mini"
            type="textarea"
            :rows="5"
            :placeholder="$t('ui.pls_input')"
          ></el-input>
        </div>
      </div>
    </div>

    <ms-form-divider :title="$t('ui.parameter_configuration')" />
    <div class="param-wrap">
      <el-tabs v-model="configActiveName">
        <el-tab-pane :label="$t('ui.enter_parameters')" name="inner">
          <InnerParamComponent
            :currentCommand="currentCommand"
            @changeInnerParam="changeInnerParam"
          ></InnerParamComponent>
        </el-tab-pane>
        <el-tab-pane :label="$t('ui.out_parameters')" name="output">
          <OutputParamComponent
            :currentCommand="currentCommand"
            @changeOutputParam="changeOutputParam"
          ></OutputParamComponent>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import InnerParamComponent from "@/business/automation/custom-commands/edit/InnerParamComponent";
import OutputParamComponent from "@/business/automation/custom-commands/edit/OutputParamComponent";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";

export default {
  name: "UiCustomCommandPane",
  data() {
    return {
      configActiveName: "inner",
    };
  },
  props: {
    currentCommand: {},
    scenarioType:{
      type: String,
      default: "scenario"
    },
    initName: String,
    initDesc: String,
  },
  components: {
    MsFormDivider,
    InnerParamComponent,
    OutputParamComponent,
  },
  watch:{
    "currentCommand.name"(name){
      this.emitEvent("changeName", name)
    },
    "currentCommand.description"(description){
      this.emitEvent("changeDesc", description)
    },
    initName(initName){
      if(this.scenarioType === "customCommand"  && this.currentCommand.useDebugger){
        this.currentCommand.name = this.initName;
      }
    }
  },
  mounted() {
    if(this.scenarioType === "customCommand"  && this.currentCommand.useDebugger){
      //指令 上下文中
      if(this.initName){
        this.currentCommand.name = this.initName;
      }

      if(this.initDesc){
        this.currentCommand.description = this.initDesc;
      }
    }
  },
  methods: {
    emitEvent(key, val){
      if(this.scenarioType === "customCommand"  && this.currentCommand.useDebugger){
        this.$emit(key, val)
      }
    },
    nameChange() {
      this.$set(this.currentCommand, "nameEdited", true);
    },
    descChange() {
      this.$set(this.currentCommand, "descEdited", true);
    },
    //接收入参变更
    changeInnerParam(variables) {
      this.$emit("changeInnerParam", variables);
    },
    changeOutputParam(str) {
      this.$emit("changeOutputParam", str);
    },
  },
};
</script>
<style scoped>
/* 
  Sence v2.4
*/
.base-info-wrap .sub-row {
  display: flex;
  padding: 5px 0 5px 0px;
}
.sub-row .label {
  width: 120px;
  text-align: right;
  padding-right: 12px;
}
.sub-row .content {
  width: 600px;
}

.desc-row .content {
  width: 600px;
}

.param-wrap {
  padding: 0px 10px 10px 10px;
  border: 1px solid #d7d2d2;
  border-radius: 4px;
}

.param-data-table {
  width: 100%;
  border: 1px solid #d7d2d2;
  border-radius: 4px;
}
</style>
