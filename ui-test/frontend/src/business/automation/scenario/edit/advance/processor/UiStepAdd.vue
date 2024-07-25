<template>
  <add-step-container
    :key="command.id"
    :operates="operates"
    :operate.sync="operate"
    :request="command"
    @add="add"
    @click.native="clickCard"
    :isReadonly="isReadonly"
  >
    <el-tree
      v-if="visible"
      node-key="id"
      :props="{ label: 'name', children: prop }"
      :data="command[prop]"
      :allow-drop="allowDrop"
      :filter-node-method="filterNode"
      @node-drag-end="sort"
      :draggable="!isReadonly"
      ref="generalSteps"
      class="ms-step-tree-cell"
      auto-expand-parent
    >
      <span
        class="custom-tree-node father"
        slot-scope="{ node, data }"
        style="width: calc(100% - 20px)"
        :class="
          data.validateResult && !data.validateResult.verify ? 'step-error' : ''
        "
        @click="clickCard"
      >
        <ui-pause-component
          v-if="data.command === 'pause'"
          :inner-step="true"
          :command="data"
          :node="node"
          :key="data.id"
          @remove="remove"
          @copyRow="copyRow"
          :isReadonly="isReadonly"
        />

        <ui-script-component
          v-if="data.command === 'cmdScript'"
          :title="scriptName"
          :inner-step="true"
          :command="data"
          :key="data.id"
          :node="node"
          @remove="remove"
          @copyRow="copyRow"
          :isReadonly="isReadonly"
        />

        <!--
       断言
       -->

        <ui-assert-component
          v-if="data.command === 'cmdValidation'"
          :title="getComputedTitle(data, '断言')"
          :inner-step="true"
          :command="data"
          :node="node"
          :key="data.id"
          @remove="remove"
          @copyRow="copyRow"
          :isReadonly="isReadonly"
        />

        <!--
       数据提取
       -->
        <ui-extract-component
          v-if="data.command === 'cmdExtraction'"
          :title="getComputedTitle(data, '数据提取')"
          :inner-step="true"
          :key="data.id"
          :command="data"
          :node="node"
          @remove="remove"
          @copyRow="copyRow"
          :isReadonly="isReadonly"
        />

        <ui-screenshot-component
          v-if="data.command === 'screenshot'"
          :inner-step="true"
          :command="data"
          :node="node"
          :key="data.id"
          @remove="remove"
          @copyRow="copyRow"
          :isReadonly="isReadonly"
        />
      </span>
    </el-tree>
  </add-step-container>
</template>

<script>

import UiScreenshotComponent from "@/business/automation/scenario/component/UiScreenshotComponent";
import UiPauseComponent from "@/business/automation/scenario/component/UiPauseComponent";
import UiScriptComponent from "@/business/automation/scenario/component/UiScriptComponent";
import UiAssertComponent from "@/business/automation/scenario/component/UiValidateComponent";
import UiExtractComponent from "@/business/automation/scenario/component/UiExtractComponent";
import AddStepContainer from "@/business/automation/scenario/edit/command/step/addStepContainer";
import { createCommand } from "@/business/automation/ui-automation";
import UiAssertValueComponent from "@/business/automation/scenario/component/UiValidateValueComponent";
import UiValidateTitleComponent from "@/business/automation/scenario/component/UiValidateTitleComponent";
import UiValidateWindowTextComponent from "@/business/automation/scenario/component/UiValidateWindowTextComponent";
import UiValidateElementComponent from "@/business/automation/scenario/component/UiValidateElementComponent";
import UiValidateDropdownComponent from "@/business/automation/scenario/component/UiValidateDropdownComponent";
import UiExtractWindowComponent from "@/business/automation/scenario/component/UiExtractWindowComponent";
import UiExtractElementComponent from "@/business/automation/scenario/component/UiExtractElementComponent";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "UiStepAdd",
  components: {
    UiValidateWindowTextComponent,
    AddStepContainer,
    UiScriptComponent,
    UiPauseComponent,
    UiAssertComponent,
    UiAssertValueComponent,
    UiValidateTitleComponent,
    UiValidateElementComponent,
    UiValidateDropdownComponent,
    UiExtractComponent,
    UiExtractWindowComponent,
    UiExtractElementComponent,
    UiScreenshotComponent,
  },
  props: {
    isReadonly:{
      type: Boolean,
      default: false,
    },
    command: {
      type: Object,
      default() {
        return {
          hashTree: [],
          coreCommands: [],
          preCommands: [],
          postCommands: [],
        };
      },
    },
    prop: {
      type: String,
      default() {
        return "hashTree";
      },
    },
    defaultOperate: {
      type: String,
    },
    operates: {
      type: Array,
      default() {
        let scriptName =
          this.prop === "preCommands"
            ? this.$t("api_test.definition.request.pre_script")
            : this.$t("api_test.definition.request.post_script");
        if (this.prop == "postCommands") {
          return [
            { id: "cmdScript", name: scriptName },
            { id: "pause", name: this.$t("ui.pause") },
            //非原子指令
            { id: "cmdValidation", name: this.$t("ui.cmdValidation") },
            { id: "cmdExtraction", name: this.$t("ui.cmdExtraction") },
            { id: "screenshot", name: this.$t("ui.screenshot") },
          ];
        } else {
          return [
            { id: "cmdScript", name: scriptName },
            { id: "pause", name: this.$t("ui.pause") },
            { id: "cmdExtraction", name: this.$t("ui.cmdExtraction") },
            { id: "screenshot", name: this.$t("ui.screenshot") },
          ];
        }
      },
    },
  },
  data() {
    return {
      props: {
        label: "type",
        children: this.prop,
      },
      visible: true,
      operate: "cmdScript",
      isInit: false,
      TIMER: -1,
    };
  },
  created() {
    if (this.defaultOperate) {
      this.operate = this.defaultOperate;
    }
    this.init();
  },
  watch: {
    command: {
      deep: true,
      handler(o, n) {
        if(o && JSON.stringify(o) !== '{}' && !o.useDebugger){
          this.clickCard();
        }
      },
    },
  },
  computed: {
    scriptName() {
      return this.prop === "preCommands"
        ? this.$t("api_test.definition.request.pre_script")
        : this.$t("api_test.definition.request.post_script");
    },
  },
  methods: {
    clickCard() {
      //节流处理
      clearTimeout(this.TIMER);
      this.TIMER = setTimeout(() => {
        this.$EventBus.$emit("changeFabBtnStatus", false);
      }, 100);
    },
    add() {
      let newCmd = createCommand(this.operate);
      newCmd.active = false;
      newCmd.index = this.command[this.prop]
        ? this.command[this.prop].length + 1
        : 0;
      this.addComponent(newCmd);
    },
    filterNode(value, data) {
      if (data.type && value.indexOf(data.type) !== -1) {
        return true;
      }
      return false;
    },
    addComponent(component) {
      if (!this.command[this.prop]) {
        this.$set(this.command, this.prop, []);
      }
      this.command[this.prop].push(component);
      this.sort();
      this.reload();
    },
    remove(row) {
      let index = this.command[this.prop].indexOf(row);
      this.command[this.prop].splice(index, 1);
      this.sort();
      this.reload();
    },
    copyRow(row) {
      let obj = JSON.parse(JSON.stringify(row));
      obj.id = getUUID();
      obj.resourceId = getUUID();
      obj['@json_id'] = getUUID();
      const index = this.command[this.prop].findIndex((d) => d.id === row.id);
      if (index !== -1) {
        this.command[this.prop].splice(index, 0, obj);
      } else {
        this.command[this.prop].push(obj);
      }
      this.sort();
      this.reload();
    },
    allowDrop(draggingNode, dropNode, dropType) {
      // 增加插件权限控制
      if (dropType !== "inner") {
        return true;
      }
      return false;
    },
    init() {
      this.sort();
      this.$nextTick(() => {
        this.isInit = true;
      });
    },
    sort() {
      let index = 1;
      if (!this.command.index) {
        this.$set(this.command, "index", 1);
      }

      for (let i in this.command[this.prop]) {
        this.command[this.prop][i].index = Number(index);
        index++;
      }
    },
    reload() {
      this.visible = false;
      this.clickCard();
      this.$nextTick(() => {
        this.visible = true;
        this.$emit("forceUpdate");
      });
    },
    getComputedTitle(data, defaultTitle) {
      if (data.name) return this.$t("ui." + data.name);
      return defaultTitle;
    },
    assertionSwitch(value) {
      this.command.hashTree.forEach((item) => {
        if (
          item.name === "cmdValidation" ||
          item.name === "cmdValidateValue" ||
          item.name === "cmdValidateTitle" ||
          item.name === "cmdValidateText" ||
          item.name === "cmdValidateElement" ||
          item.name === "cmdValidateDropdown"
        ) {
          item.enable = value;
        }
      });
    },
  },
};
</script>

<style scoped>
.ms-left-cell .el-button:nth-of-type(1) {
  color: #b8741a;
  background-color: #f9f1ea;
  border: #f9f1ea;
}

.ms-left-cell .el-button:nth-of-type(2) {
  color: #783887;
  background-color: #f2ecf3;
  border: #f2ecf3;
}

.ms-left-cell .el-button:nth-of-type(3) {
  color: #a30014;
  background-color: #f7e6e9;
  border: #f7e6e9;
}

.ms-left-cell .el-button:nth-of-type(4) {
  color: #015478;
  background-color: #e6eef2;
  border: #e6eef2;
}

.ms-tree :deep(.el-tree-node__expand-icon.expanded) {
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

.ms-tree :deep(.el-icon-caret-right:before) {
  content: "\e723";
  font-size: 20px;
}

.ms-tree :deep(.el-tree-node__expand-icon.is-leaf) {
  color: transparent;
}

.ms-tree :deep(.el-tree-node__expand-icon) {
  color: #7c3985;
}

.ms-tree :deep(.el-tree-node__expand-icon.expanded.el-icon-caret-right:before) {
  color: #7c3985;
  content: "\e722";
  font-size: 20px;
}

:deep(.el-tree-node__content) {
  height: 100%;
  margin-top: 6px;
  vertical-align: center;
}

.ms-step-button {
  margin: 6px 0px 8px 30px;
}

.ms-left-cell .el-button:nth-of-type(1) {
  color: #b8741a;
  background-color: #f9f1ea;
  border: #f9f1ea;
}

.ms-left-cell .el-button:nth-of-type(2) {
  color: #783887;
  background-color: #f2ecf3;
  border: #f2ecf3;
}

.ms-left-cell .el-button:nth-of-type(3) {
  color: #fe6f71;
  background-color: #f9f1ea;
  border: #ebf2f2;
}

.ms-left-cell .el-button:nth-of-type(4) {
  color: #1483f6;
  background-color: #f2ecf3;
  border: #f2ecf3;
}

.ms-left-cell .el-button:nth-of-type(5) {
  color: #a30014;
  background-color: #f7e6e9;
  border: #f7e6e9;
}

.ms-left-cell .el-button:nth-of-type(6) {
  color: #015478;
  background-color: #e6eef2;
  border: #e6eef2;
}

.ms-left-cell {
  margin-top: 0px;
}

.ms-step-tree-cell :deep(.el-tree-node__expand-icon) {
  padding: 0px;
}

.ms-fieldset .ms-step-tree-cell {
  /* height: calc(100vh - 570px); */
  overflow: scroll;
}

.ms-fieldset .ms-step-tree-cell::-webkit-scrollbar {
  width: 0 !important;
}

.ms-fieldset .ms-step-tree-cell {
  -ms-overflow-style: none;
  overflow: -moz-scrollbars-none;
}

.step-error {
  border: 1px solid #f56c6c;
}
</style>
