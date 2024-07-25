<template>
  <div class="scenario-advance">
    <el-tabs v-model="activeName">
      <el-tab-pane
        v-if="showElement"
        v-permission="['PROJECT_UI_ELEMENT:READ']"
        name="element"
        :label="$t('ui.ui_element')"
      >
        <ui-element-library :isReadonly="isReadonly" />
      </el-tab-pane>

      <el-tab-pane
        name="pre"
        :label="$t('api_test.definition.request.pre_operation')"
      >
        <span
          class="item-tabs"
          effect="dark"
          placement="top-start"
          slot="label"
        >
          {{ $t("api_test.definition.request.pre_operation") }}
          <div
            class="el-step__icon is-text ms-api-col ms-header"
            v-if="
              currentCommand.preCommands &&
              currentCommand.preCommands.length > 0
            "
          >
            <div class="el-step__icon-inner">
              {{ currentCommand.preCommands.length }}
            </div>
          </div>
        </span>
        <ui-step-add
          :isReadonly="isReadonly"
          :command="currentCommand"
          @forceUpdate="forceUpdate"
          prop="preCommands"
        />
      </el-tab-pane>

      <el-tab-pane
        name="post"
        :label="$t('api_test.definition.request.post_operation')"
      >
        <span
          class="item-tabs"
          effect="dark"
          placement="top-start"
          slot="label"
        >
          {{ $t("api_test.definition.request.post_operation") }}
          <div
            class="el-step__icon is-text ms-api-col ms-header"
            v-if="
              currentCommand.postCommands &&
              currentCommand.postCommands.length > 0
            "
          >
            <div class="el-step__icon-inner">
              {{ currentCommand.postCommands.length }}
            </div>
          </div>
        </span>
        <ui-step-add
          :isReadonly="isReadonly"
          :command="currentCommand"
          @forceUpdate="forceUpdate"
          prop="postCommands"
        />
      </el-tab-pane>

      <el-tab-pane name="error" :label="$t('ui.error_handling')">
        <error-handle :isReadonly="isReadonly" v-model="currentCommand" />
      </el-tab-pane>

      <el-tab-pane name="timeout" :label="$t('ui.other_settings')">
        <time-out-handle
          :isReadonly="isReadonly"
          v-if="needOrNotOtherCfg"
          v-model="currentCommand"
        />
        <screenshot-config
          v-model="currentCommand"
          :isReadonly="isReadonly"
          :key="currentCommand.id"
        />
      </el-tab-pane>

      <el-tab-pane
        :key="currentCommand.id"
        v-if="results"
        name="console"
        :label="$t('ui.step_results')"
      >
        <ui-single-result
          :results="results"
          :currentCommand="currentCommand"
          ref="singleRes"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import UiElementLibrary from "./UiElementLibrary";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";
import paramDefinition from "@/business/definition/command/param-definition";
import UiStepAdd from "./processor/UiStepAdd";
import ErrorHandle from "./ErrorHandle";
import UiCommandConsole from "./result/UiSingleResultItem";
import UiSingleResult from "./result/UiSingleResult";
import TimeOutHandle from "./TimeOutHandle";
import ScreenshotConfig from "./ScreenshotConfig";
import { useCommandStore } from "@/store";

const commandStore = new useCommandStore();
export default {
  name: "UiScenarioAdvance",
  components: {
    TimeOutHandle,
    UiSingleResult,
    UiCommandConsole,
    UiStepAdd,
    ErrorHandle,
    UiElementLibrary,
    ScreenshotConfig,
  },
  props: {
    value: Object,
    scenarioIndex: Number,
  },
  data() {
    return {
      activeName: "element",
      showElementObj: {
        mouseOperation: true,
        inputOperation: true,
        elementOperation: true,
      },
      results: null,
      notNeedOtherCfgCmdList: ["cmdWaitElement", "cmdDialog"],
      commandStore: commandStore,
    };
  },
  created() {
    if (this.showElement) {
      this.activeName = "element";
    } else {
      this.activeName = "pre";
    }

    this.$EventBus.$on("handleAdvanceValidate", this.handleAdvanceValidate);
  },
  beforeDestroy() {
    this.$EventBus.$off("handleAdvanceValidate", this.handleAdvanceValidate);
  },
  watch: {
    "currentCommand.enable": {
      immediate: false,
      handler(o, n) {
        if (this.currentCommand && o) {
          this.$EventBus.$emit("changeFabBtnStatus", false);
        }
      },
    },
    "currentCommand.active": {
      immediate: false,
      handler(o, n) {
        if (this.currentCommand && o) {
          this.$EventBus.$emit("changeFabBtnStatus", false);
        }
      },
    },
    "currentCommand.debugResult"() {
      if (this.currentCommand) {
        this.results = this.currentCommand.debugResult;
      }
    },
    "commandStore.selectCommand": {
      deep: true,
      immediate: true,
      handler(currentCommand) {
        if (currentCommand && currentCommand.debugResult) {
          this.results = this.currentCommand.debugResult;
        }
      },
    },
  },
  computed: {
    showElement() {
      return this.isTargetObject || this.isValueObject;
    },
    isTargetObject() {
      if (
        this.showElementObj[this.currentCommand.viewType] ||
        this.currentCommand.command === "cmdSelectFrame"
      ) {
        return true;
      }
      return (
        this.currentCommand &&
        this.currentCommand.targetVO &&
        this.currentCommand.targetVO.elementType === "elementObject"
      );
    },
    isValueObject() {
      return (
        this.currentCommand &&
        this.currentCommand.valueVO &&
        this.currentCommand.valueVO.elementType === "elementObject"
      );
    },
    currentCommand() {
      let command = commandStore.selectCommand;
      if (!command) {
        return {};
      }
      if (!command.hashTree) {
        command.hashTree = [];
      }
      if (!command.preCommands) {
        command.preCommands = [];
      }
      if (!command.postCommands) {
        command.postCommands = [];
      }
      return this.sort(command);
    },
    isReadonly() {
      return commandStore.selectCommand?.readonly;
    },
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    },
    needOrNotOtherCfg() {
      if (
        this.currentCommand.viewType == "browserOperation" ||
        this.notNeedOtherCfgCmdList.indexOf(this.currentCommand.command) != -1
      )
        return false;
      return true;
    },
  },
  methods: {
    handleAdvanceValidate(tab) {
      //tab options pre post
      if (!tab) {
        return;
      }
      this.$nextTick(() => {
        this.activeName = tab;
      });
    },
    forceUpdate() {
      this.$forceUpdate();
    },
    sort(command) {
      command = this.doSort(command, "hashTree");
      command = this.doSort(command, "preCommands");
      command = this.doSort(command, "postCommands");
      return command;
    },
    doSort(command, prop) {
      let index = 1;
      for (let i in command[prop]) {
        // this.command[this.prop][i].index = Number(index);
        this.$set(command[prop][i], "index", Number(index));
        index++;
      }
      return command;
    },
  },
};
</script>

<style scoped>
.scenario-advance {
  margin: 5px 5px;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  font-size: xx-small;
  border-radius: 50%;
}
</style>
