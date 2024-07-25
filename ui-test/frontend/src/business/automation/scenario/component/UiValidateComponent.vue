<template>

  <ui-base-component
    color="#8dd16c"
    background-color="#f0f9ec"
    :data="command"
    :draggable="draggable && !isReadonly"
    :collapsable="true"
    :is-show-name-input="false"
    :show-btn="true"
    :title="getComputedTitle(command, '断言')"
    :total-count="totalCount"
    @assertionSwitch="assertionSwitch"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :isReadonly="isReadonly"
    >

    <template v-slot:request>
      <div class="script-content" style="-webkit-user-drag: element;user-select: none;">
        <span class="extract-window-title">{{ $t("ui.validate_tips") }}</span>

        <add-step-container
          :operates="operates"
          :operate.sync="operate"
          :request="command"
          @add="add"
          :isReadonly="isReadonly"
          >
          <!--
          网页标题
          -->
          <div class="validate-title">
            <span class="extract-window-title" v-if="validateTitleCmd && validateTitleCmd.length">{{ $t("ui.cmdValidateTitle") }}</span>

            <ui-validate-title-component
              :isReadonly="isReadonly"
              v-for="cmd in validateTitleCmd"
              class="extract-item"
              :inner-step="true"
              :key="cmd.id"
              :show-btn="true"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, validateTitleCmd)"
              @copyRow="copyRowInner(cmd, validateTitleCmd)"
            />
          </div>

          <div class="validate-element">
            <span class="extract-window-title" v-if="validateElementCmd && validateElementCmd.length">{{ $t("ui.cmdValidateElement") }}</span>

            <ui-validate-element-component
              :isReadonly="isReadonly"
              v-for="cmd in validateElementCmd"
              class="extract-item"
              :inner-step="true"
              :key="cmd.id"
              :show-btn="true"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, validateElementCmd)"
              @copyRow="copyRowInner(cmd, validateElementCmd)"
            />
          </div>

          <div class="validate-dropdown">
            <span class="extract-window-title" v-if="validateDropdownCmd && validateDropdownCmd.length">{{ $t("ui.cmdValidateDropdown") }}</span>

            <ui-validate-dropdown-component
              :isReadonly="isReadonly"
              v-for="cmd in validateDropdownCmd"
              class="extract-item"
              :inner-step="true"
              :key="cmd.id"
              :show-btn="true"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, validateDropdownCmd)"
              @copyRow="copyRowInner(cmd, validateDropdownCmd)"
            />
          </div>

          <div class="validate-value">
            <span class="extract-window-title" v-if="validateValueCmd && validateValueCmd.length">{{ $t("ui.cmdValidateValue") }}</span>

            <ui-validate-value-component
              :isReadonly="isReadonly"
              v-for="cmd in validateValueCmd"
              class="extract-item"
              :inner-step="true"
              :key="cmd.id"
              :show-btn="true"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, validateValueCmd)"
              @copyRow="copyRowInner(cmd, validateValueCmd)"
            />
          </div>

          <div class="validate-text">
            <span class="extract-window-title" v-if="validateTextCmd && validateTextCmd.length">{{ $t("ui.cmdValidateText") }}</span>

            <ui-validate-window-text-component
              :isReadonly="isReadonly"
              v-for="cmd in validateTextCmd"
              class="extract-item"
              :inner-step="true"
              :key="cmd.id"
              :show-btn="true"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, validateTextCmd)"
              @copyRow="copyRowInner(cmd, validateTextCmd)"
            />
          </div>

        </add-step-container>
      </div>

    </template>

  </ui-base-component>
</template>

<script>

import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import {createCommand} from "@/business/automation/ui-automation";
import validation from "@/business/definition/command/validation";
import {COMMAND_TYPE_PROXY} from "@/business/definition/command/command-type";
import { orderBy, findIndex, cloneDeep } from "lodash-es"
import UiValidateTitleComponent from "@/business/automation/scenario/component/UiValidateTitleComponent";
import AddStepContainer from "@/business/automation/scenario/edit/command/step/addStepContainer";
import UiValidateWindowTextComponent from "@/business/automation/scenario/component/UiValidateWindowTextComponent";
import UiValidateElementComponent from "@/business/automation/scenario/component/UiValidateElementComponent";
import UiValidateValueComponent from "@/business/automation/scenario/component/UiValidateValueComponent";
import UiValidateDropdownComponent from "@/business/automation/scenario/component/UiValidateDropdownComponent";
import {uuid} from "metersphere-frontend/src/model/ApiTestModel";

export default {
  name: "UiValidateComponent",
  components: {
    UiValidateWindowTextComponent,
    UiValidateTitleComponent,
    UiValidateElementComponent,
    UiValidateValueComponent,
    UiValidateDropdownComponent,
    UiBaseComponent,
    AddStepContainer,
  },
  props: {
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
    isReadonly: {
      type: Boolean,
      default: false,
    },
  }
  ,
  data() {
    return {
      operate: "cmdValidateValue",
      prop: "hashTree",
      validateTextCmd: [],
      validateDropdownCmd: [],
      validateElementCmd: [],
      validateTitleCmd: [],
      validateValueCmd: [],
      totalCount: 0
    }
  }
  ,
  computed: {
    operates() {
      let definition = validation.filter(c => c.commandType == COMMAND_TYPE_PROXY);
      definition = orderBy(definition, "sort", "desc");
      return definition.map(c => {
        return {id: c.name, name: this.$t('ui.' + c.name)}
      });
    }
  },
  mounted() {
    this.command.active = false;
    this.init();
  },
  methods: {
    init() {
      let cmds = this.command[this.prop];
      if (!cmds || !cmds.length) {
        return;
      }
      for (let i = 0; i < cmds.length; i++) {
        let component = cmds[i];
        switch (component.command) {
          case "cmdValidateTitle":
            this.validateTitleCmd.push(component);
            break;
          case "cmdValidateElement":
            this.validateElementCmd.push(component);
            break;
          case "cmdValidateDropdown":
            this.validateDropdownCmd.push(component);
            break;
          case "cmdValidateValue":
            this.validateValueCmd.push(component);
            break;
          case "cmdValidateText":
            this.validateTextCmd.push(component);
            break;
        }
      }

      this.calculateTotal();
    },
    remove() {
      this.$emit('remove', this.command, this.node);
    }
    ,
    removeInner(cmd, cmdList) {
      let index = findIndex(cmdList, {"id": cmd.id});
      if (index != -1) {
        cmdList.splice(index, 1);
      }
      index = findIndex(this.command[this.prop], {"id": cmd.id});
      if (index != -1) {
        this.command[this.prop].splice(index, 1);
      }
      this.calculateTotal();
    }
    ,
    copyRow() {
      this.$emit('copyRow', this.command, this.node);
    },
    copyRowInner(cmd, cmdList) {
      let index = findIndex(cmdList, {"id": cmd.id});
      let newCmd = cloneDeep(cmd);
      newCmd.id = uuid();
      if (index != -1) {
        cmdList.splice(index, 0, newCmd);
      }
      index = findIndex(this.command[this.prop], {"id": cmd.id});
      if (index != -1) {
        this.command[this.prop].splice(index, 0, newCmd);
      }
      this.calculateTotal();
    }
    ,
    active() {
      this.command.active = !this.command.active;
    }
    ,
    add() {
      let cmdForCreate = this.operate || "cmdValidateValue";
      let newCmd = createCommand(cmdForCreate);
      newCmd.active = false;
      newCmd.index = this.command[this.prop] ? this.command[this.prop].length + 1 : 0;
      //对应后端jackson接收类型标记
      newCmd.vo.type = cloneDeep(cmdForCreate.replace("cmd", ""));
      this.addComponent(newCmd);
    }
    ,
    addComponent(component) {
      if (!this.command[this.prop]) {
        this.command[this.prop] = [];
      }
      this.command[this.prop].push(component);
      switch (component.command) {
        case "cmdValidateTitle":
          this.validateTitleCmd.push(component);
          break;
        case "cmdValidateElement":
          this.validateElementCmd.push(component);
          break;
        case "cmdValidateDropdown":
          this.validateDropdownCmd.push(component);
          break;
        case "cmdValidateValue":
          this.validateValueCmd.push(component);
          break;
        case "cmdValidateText":
          this.validateTextCmd.push(component);
          break;
      }

      this.calculateTotal();
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
      this.$nextTick(() => {
        this.$forceUpdate();
      });
    }
    ,
    getComputedTitle(data, defaultTitle) {
      if (data.name)
        return this.$t('ui.' + data.name);
      return defaultTitle;
    },
    assertionSwitch(value) {
      //当前组件中已经没有stepAdd 引用了
      //this.$refs.stepAdd.assertionSwitch(value);
    },
    calculateTotal() {
      this.totalCount = (this.validateTextCmd ? this.validateTextCmd.length : 0) +
        (this.validateDropdownCmd ? this.validateDropdownCmd.length : 0) +
        (this.validateElementCmd ? this.validateElementCmd.length : 0) +
        (this.validateTitleCmd ? this.validateTitleCmd.length : 0) +
        (this.validateValueCmd ? this.validateValueCmd.length : 0);
    }
  }
}
</script>

<style scoped>
.script-content {
  /* height: calc(100vh - 570px);
  min-height: 440px; */
}

.extract-item {
  border: 1px solid #E8F3FF;
  margin-left: 10px;
  padding: 5px;
}

.validate-title {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid #7B0274;
}

.validate-element {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid #44B3D2;
}

.validate-dropdown {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid rgb(230, 162, 60);
}

.validate-value {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid #7B0274;
}

.validate-text {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid #7B0274;
}

.extract-element-title {
  display: inline-block;
  margin: 10px 0 10px 10px;
  /*color: #44B3D2;*/
}


.extract-element {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid #44B3D2;
}

.extract-element-title {
  display: inline-block;
  margin: 10px 0 10px 10px;
  /*color: #44B3D2;*/
}

.extract-window-title {
  display: inline-block;
  margin: 10px 0 10px 10px;
  /*color: #7B0274;*/
}
</style>
