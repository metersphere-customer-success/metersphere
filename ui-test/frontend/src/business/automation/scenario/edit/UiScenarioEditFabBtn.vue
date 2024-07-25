<template>
  <div
    v-permission="[
      'PROJECT_UI_SCENARIO:READ+EDIT',
      'PROJECT_UI_SCENARIO:READ+CREATE',
    ]"
    @click="fabClick"
  >
    <vue-fab
      id="fab"
      mainBtnColor="#783887"
      size="small"
      :global-options="globalOptions"
      :click-auto-close="false"
      v-outside-click="outsideClick"
      ref="refFab"
    >
      <fab-item
        v-for="(item, index) in buttonData"
        :key="index"
        :idx="getIdx(index)"
        :title="item.title"
        :title-bg-color="item.titleBgColor"
        :title-color="item.titleColor"
        :color="item.titleColor"
        :icon="item.icon"
        @clickItem="item.click"
      />
    </vue-fab>
  </div>
</template>

<script>
import "@/common/material-icons.css";
import {
  createCommand,
  getUiFabButtons,
} from "@/business/automation/ui-automation";
import OutsideClick from "@/api/outside-click";
import { orderBy } from "lodash-es";
import { groupCommandDefinition } from "@/business/definition/command/all-command-definition";
import { COMMAND_TYPE_COMBINATION } from "@/business/definition/command/command-type";
import { useCommandStore } from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "UiScenarioEditFabBtn",
  data() {
    return {
      buttonData: [],
      globalOptions: {
        spacing: 30,
      },
    };
  },
  props: ["value", "immediately"],
  directives: { OutsideClick },
  created() {
    this.buttonData = getUiFabButtons(this).reverse();
  },
  watch: {
    immediately(status) {
      this.$refs.refFab.active = status;
    },
  },
  mounted() {
    if (this.immediately) {
      this.$refs.refFab.active = true;
    }
  },
  methods: {
    outsideClick(e) {
      e.stopPropagation();
      this.buttonData = getUiFabButtons(this).reverse();
    },
    fabClick() {
      // if (this.operatingElements && this.operatingElements.length < 1) {
      //   if (this.selectedTreeNode && this.selectedTreeNode.referenced === 'REF' || this.selectedTreeNode.disabled) {
      //     this.$warning(this.$t('api_test.scenario.scenario_warning'));
      //   } else {
      //     this.$warning(this.$t('api_test.scenario.scenario_step_warning'));
      //   }
      // }

      //通知父级组件 fabBtn展示状态
      if (this.$refs.refFab && this.immediately !== this.$refs.refFab.active) {
        this.$emit("listenFabBtnStatusChange", this.$refs.refFab.active);
      }
    },
    addCommand(commandTypeEnum) {
      this.createCommand(commandTypeEnum);
      this.$emit("sort");
    },
    createCommand: function (type) {
      let scenarioDefinition = this.value.scenarioDefinition;
      let selectCommand = commandStore.selectCommand;
      if (scenarioDefinition.hashTree && scenarioDefinition.hashTree.length >0) {
        if (selectCommand && (selectCommand.referenced === "REF" || selectCommand.readonly)) {
          this.$warning(this.$t("ui.scenario_ref_add_warning"));
          return;
        }
      }
      let thisTypeCmd = groupCommandDefinition[type];
      if (!thisTypeCmd || thisTypeCmd.length === 0) {
        if (type === "UiScenario") {
          this.$emit("scenarioImport");
        }
        return;
      }
      //序号大的靠前
      orderBy(thisTypeCmd, ["order"], ["desc"]);
      let newCmd = createCommand(thisTypeCmd[0].command);
      newCmd.name = this.$t("ui." + newCmd.command);
      newCmd.expanded = false;

      //处理 scenarioDefinition 中含有引用的情况
      if(this.value.commandViewStruct){
        this.fillRefToScenarioDefinition(this.value.commandViewStruct);
      }
      else{
        this.fillRefToScenarioDefinition(scenarioDefinition.hashTree);
      }

      // 如果是if等复合指令，则添加到内部
      if (
        selectCommand &&
        (selectCommand.commandType === COMMAND_TYPE_COMBINATION ||
          selectCommand.type === "scenario" ||
          selectCommand.type === "customCommand") &&
        (selectCommand.expanded || selectCommand.hashTree.length === 0 || selectCommand.useDebugger)
      ) {
        selectCommand.hashTree.push(newCmd);
        if (selectCommand.expanded) {
          this.$emit("changeCurrentCommand", newCmd);
          commandStore.selectCommand = newCmd;
          commandStore.selectStep = newCmd;
        }
      } else {
        let pNode;
        //指令需要 在指令结构中查找
        if(this.value.commandViewStruct){
          pNode = this.findPNodeFromStruct(this.value.commandViewStruct,
          selectCommand ? selectCommand.id : null);
        } else {
          pNode = this.findPNode(
            scenarioDefinition,
            selectCommand ? selectCommand.id : null
          );
        }
        
        if (pNode) {
          if(Array.isArray(pNode) && pNode.length > 1){
            pNode = pNode[1];
          }
          if (pNode.referenced === "REF" || pNode.readonly) {
            this.$warning(this.$t("ui.scenario_ref_add_warning"));
            return;
          }
          //校对序号
          for (let i= 0; i < pNode.hashTree.length; i++) {
            if (pNode.hashTree[i].index) {
              delete pNode.hashTree[i].index;
            }
            this.$set(pNode.hashTree[i], "index", i + 1);
          }
          if (pNode.hashTree) {
            this.$set(newCmd, "index", pNode.hashTree.length + 1);
          }
          
          //说明左侧组件有选择指令，直接在该指令后面添加一个指令，否则添加到最后
          pNode.hashTree.splice(selectCommand.index, 0, newCmd);
        } else {
          if (scenarioDefinition) {
            scenarioDefinition.hashTree.push(newCmd);
          }
        }
        this.$emit("changeCurrentCommand", newCmd);
        commandStore.selectCommand = newCmd;
        commandStore.selectStep = newCmd;
      }
    },
    getIdx(index) {
      return index - 0.33;
    },
    findPNode(root, id) {
      if (root && root.hashTree) {
        for (const item of root.hashTree) {
          if (item.id === id) {
            return root;
          }
        }
        for (const item of root.hashTree) {
          let res = this.findPNode(item, id);
          if (res) {
            return res;
          }
        }
      }
      return null;
    },
    findPNodeFromStruct(hashTree, id){
      if (hashTree) {
        for (const item of hashTree) {
          if (item.id === id) {
            return hashTree;
          }
        }
        for (const item of hashTree) {
          let res = this.findPNode(item, id);
          if (res) {
            return res;
          }
        }
      }
      return null;
    },
    fillRefToScenarioDefinition(hashTree) {
      if (!hashTree || hashTree.length <= 0) {
        return;
      }

      hashTree.forEach((item) => {
        this.doFillRefToScenarioDefinition(item, item.referenced === "REF");
      });
    },
    /**
     * isRef 只要父级找到ref 子级全部为引用状态
     */
    doFillRefToScenarioDefinition(sc, isRef) {
      let targetRefType = isRef ? true : sc.referenced === "REF";

      if (targetRefType) {
        sc.readonly = true;
      }

      if (sc.hashTree && sc.hashTree.length > 0) {
        sc.hashTree.forEach((item) => {
          this.doFillRefToScenarioDefinition(item, isRef);
        });
      }
    },
  },
};
</script>

<style scoped>
#fab {
  right: 90px;
  bottom: 120px;
  z-index: 5;
}
</style>
