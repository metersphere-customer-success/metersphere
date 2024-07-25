<template>

  <ui-base-component
    color="#8dd16c"
    background-color="#f0f9ec"
    :data="command"
    :draggable="draggable && !isReadonly"
    :collapsable="true"
    :is-show-name-input="false"
    :show-btn="true"
    :title="getComputedTitle(command, $t('ui.cmdExtraction'))"
    :total-count="totalCount"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :isReadonly="isReadonly"
    >

    <template v-slot:request>
      <span class="extract-window-title">{{ $t("ui.extract_tip") }}</span>

      <div class="script-content"  style="-webkit-user-drag: element;user-select: none;">
        <add-step-container
          :key="command.id"
          :operates="operates"
          :operate.sync="operate"
          :request="command"
          @add="add"
          :isReadonly="isReadonly"
          >
          <!--
          数据提取
          -->
          <div class="extract-window">
            <span class="extract-window-title" v-if="extractWindowCmd && extractWindowCmd.length">{{ $t("ui.cmdExtractWindow") }}</span>

            <ui-extract-window-component
              v-for="cmd in extractWindowCmd"
              class="extract-item"
              :title="getComputedTitle(cmd, $t('ui.cmdExtraction'))"
              :inner-step="true"
              :key="cmd.id"
              :show-btn="true"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, extractWindowCmd)"
              @copyRow="copyRowInner(cmd, extractWindowCmd)"
              :isReadonly="isReadonly"
            />
          </div>

          <div class="extract-element">
            <span class="extract-element-title" v-if="extractElementCmd && extractElementCmd.length">{{ $t("ui.cmdExtractElement") }}</span>

            <ui-extract-element-component
              v-for="cmd in extractElementCmd"
              class="extract-item"
              :title="getComputedTitle(cmd, $t('ui.cmdExtraction'))"
              :inner-step="true"
              :key="cmd.id"
              :command="cmd"
              :node="node"
              @remove="removeInner(cmd, extractElementCmd)"
              @copyRow="copyRowInner(cmd, extractElementCmd)"
              :isReadonly="isReadonly"
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
import extraction from "@/business/definition/command/extraction";
import {COMMAND_TYPE_PROXY} from "@/business/definition/command/command-type";
import { orderBy, cloneDeep, findIndex, find } from "lodash-es"
import AddStepContainer from "@/business/automation/scenario/edit/command/step/addStepContainer";
import UiExtractWindowComponent from "@/business/automation/scenario/component/UiExtractWindowComponent";
import UiExtractElementComponent from "@/business/automation/scenario/component/UiExtractElementComponent";
import {
  EXTRACT_ELEMENT_OPTIONS,
  isAtomicCmd
} from "@/business/definition/command/cmd-constants-utils";
import {uuid} from "metersphere-frontend/src/model/ApiTestModel";


export default {
  name: "UiExtractComponent",
  components: {
    UiBaseComponent,
    AddStepContainer,
    UiExtractWindowComponent,
    UiExtractElementComponent,
  },
  props: {
    isReadonly: {
      type: Boolean,
      default: false,
    },
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
  }
  ,
  data() {
    return {
      operate: "cmdExtractWindow",
      //用于展示的临时指令引用数组
      extractElementCmd: [],
      extractWindowCmd: [],
      prop: "hashTree",
      totalCount: 0
    }
  }
  ,
  computed: {
    operates() {
      let definition = extraction.filter(c => c.commandType == COMMAND_TYPE_PROXY);
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
          case "cmdExtractElement":
            this.extractElementCmd.push(component);
            break;
          case "cmdExtractWindow":
            this.extractWindowCmd.push(component);
            break;
        }
      }

      this.caculateTotal();
    },
    remove() {
      this.$emit('remove', this.command, this.node);
    },
    removeInner(cmd, cmdList) {
      let index = findIndex(cmdList, {"id": cmd.id});
      if (index != -1) {
        cmdList.splice(index, 1);
      }
      index = findIndex(this.command[this.prop], {"id": cmd.id});
      if (index != -1) {
        this.command[this.prop].splice(index, 1);
      }
      this.caculateTotal();
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
      this.caculateTotal();
    }
    ,
    active() {
      this.command.active = !this.command.active;
    }
    ,
    add() {
      let cmdForCreate = this.operate;
      if (isAtomicCmd(this.operate)) {
        //复制一份复合指令 并且使用当前原子指令作为复合指令的参数构造展示层
        let cmd = find(EXTRACT_ELEMENT_OPTIONS, {'value': this.operate});
        if (cmd) {
          cmdForCreate = "cmdExtractElement";
        } else {
          cmdForCreate = "cmdExtractWindow";
        }
      }
      let newCmd = createCommand(cmdForCreate);
      newCmd.active = false;
      newCmd.index = this.command[this.prop] ? this.command[this.prop].length + 1 : 0;
      newCmd.vo.extractType = cloneDeep(this.operate);
      this.addComponent(newCmd);
    }
    ,
    addComponent(component) {
      if (!this.command[this.prop]) {
        this.command[this.prop] = [];
      }
      this.command[this.prop].push(component);
      let cmd = find(EXTRACT_ELEMENT_OPTIONS, {'value': this.operate});
      if (!cmd) {
        this.extractWindowCmd.push(component);
      } else {
        this.extractElementCmd.push(component);
      }

      this.caculateTotal();
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
      this.visible = false;
      this.$nextTick(() => {
        this.visible = true;
        this.$forceUpdate();
      })
    }
    ,
    getComputedTitle(data, defaultTitle) {
      if (data.name)
        return this.$t('ui.' + data.name);
      return defaultTitle;
    },
    caculateTotal() {
      this.totalCount = (this.extractWindowCmd ? this.extractWindowCmd.length : 0)
        + (this.extractElementCmd ? this.extractElementCmd.length : 0);
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

.extract-window {
  margin-left: 10px;
  margin-bottom: 10px;
  border-left: 2px solid #7B0274;
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
