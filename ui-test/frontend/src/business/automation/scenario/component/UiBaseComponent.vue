<template>
  <el-card

    @mouseover.native="showStepBtn"
    @mouseout.native="outStepBtn"
  >
    <div class="header step-container" @click="active(data)">
      <div class="step-content">
        <slot name="beforeHeaderLeft">
          <div
            v-if="data.index"
            class="el-step__icon is-text enable-switch"
            :style="{ color: color, 'background-color': backgroundColor }"
          >
            <div class="el-step__icon-inner">{{ data.index }}</div>
          </div>
          <slot name="behindHeaderLeft" v-if="collapsable"></slot>
          <el-tag
            class="ms-left-btn"
            size="small"
            :style="{ color: color, 'background-color': backgroundColor }"
          >
            {{ totalCount ? title + " " + totalCount : title }}
          </el-tag>
          <slot
            :style="{ 'margin-left': '-5px', 'margin-right': '5px' }"
            name="afterName"
          ></slot>
        </slot>
        <span class="input-combination">
          <slot name="headerLeft">
            <i
              class="icon el-icon-arrow-right"
              :class="{ 'is-active': data.active }"
              @click="active(data)"
              v-if="data.type != 'scenario' && collapsable"
              @click.stop
            />
            <span  class="name-input" @click.stop v-if="!isShowInput && data.type !== 'customCommand' && data.referenced !== 'REF' && data.referenced !== 'Copy' && !(isReadonly || (data.disabled && !data.root) || !showVersion)">
              <el-input
                  draggable="disabled"
                  size="mini"
                  v-model="data.description"
                  @blur="isShowInput = true"
                  @focus="active(data)"
                  :placeholder="$t('commons.input_name')"
                  ref="nameEdit"
                  :disabled="data.disabled" />
            </span>
            <i
                v-if="isShowInput && data.type !== 'customCommand' && data.referenced !== 'REF' && data.referenced !== 'Copy' && !(isReadonly || (data.disabled && !data.root) || !showVersion)"
                class="el-icon-edit"
                style="cursor: pointer"
                @click="editName"
            />
            <span v-if="isShowInput && data.type !== 'customCommand' &&  data.referenced !== 'REF'" :class="showVersion ? 'scenario-unscroll' : 'scenario-version'"
                  id="moveout"
                  @mouseenter="enter($event)"
                  @mouseleave="leave($event)">{{ data.description }}</span>
            <slot name="afterTitle" />
          </slot>
        </span>
      </div>

      <div v-if="!ifFromVariableAdvance" class="header-right" @click.stop>
        <slot name="message"></slot>
        <slot name="debugStepCode"></slot>

        <slot name="button" v-if="showVersion"></slot>

        <span v-if="showBtn">
          <el-tooltip
            :content="$t('test_resource_pool.enable_disable')"
            placement="top"
          >
            <el-switch
              v-model="data.enable"
              class="enable-switch"
              size="mini"
              @change="assertionSwitch"
              :disabled="
                isReadonly || (data.disabled && !data.root && !showVersion)
              "
              style="width: 30px"
            />
          </el-tooltip>
          <el-tooltip
            :content="$t('commons.copy')"
            placement="top"
            v-if="showVersion"
          >
            <el-button
              size="mini"
              icon="el-icon-copy-document"
              circle
              @click="copyRow"
              style="padding: 5px"
              :disabled="isReadonly || (data.disabled && !data.root)"
            />
          </el-tooltip>
          <el-tooltip
            :content="$t('commons.delete')"
            placement="top"
            v-if="showVersion"
          >
            <el-button
              size="mini"
              icon="el-icon-delete"
              type="danger"
              style="padding: 5px"
              circle
              @click="remove"
              :disabled="
                isReadonly || (data.disabled && !data.root) || !showVersion
              "
            />
          </el-tooltip>
        </span>

        <step-extend-btns
          :refNodeIdCahceMap="refNodeIdCahceMap"
          v-show="!showBtn && showStepBtnData && commonControl"
          style="display: contents"
          :open-rename="rename"
          :data="data"
          :environmentType="environmentType"
          :environmentGroupId="environmentGroupId"
          :envMap="envMap"
          @enable="enable"
          @copy="copyRow"
          @remove="remove"
          @rename="rename"
          @openScenario="openScenario"
          :showEnableScence="false"
          @setScenario="setScenario"
        />
      </div>
    </div>
    <div class="header" v-if="collapsable">
      <el-collapse-transition>
        <div v-show="data.active" :draggable="draggable">
          <el-divider></el-divider>
          <fieldset :disabled="data.disabled" class="ms-fieldset">
            <!--四种协议请求内容-->
            <slot name="request"></slot>
            <!--其他模版内容，比如断言，提取等-->
            <slot></slot>
          </fieldset>
          <!--四种协议执行结果内容-->
          <slot name="result"></slot>
        </div>
      </el-collapse-transition>
    </div>

    <el-dialog
      :title="$t('commons.reference_settings')"
      :visible.sync="dialogVisible"
      width="400px"
      class="var-config-dialog"
      append-to-body
    >
      <div>
        <el-checkbox
          v-model="data.environmentEnable"
          @change="checkEnv"
          :disabled="data.disabled"
        >
          {{ $t("ui.use_origin_env_run") }}
        </el-checkbox>
        <div class="split"></div>

        <el-checkbox
          v-model="data.variableEnable"
          :disabled="data.disabled || data.smartVariableEnable"
        >
          {{ $t("ui.use_origin_variable_scene") }}
        </el-checkbox>
        <div class="split"></div>
        <el-checkbox
          v-model="data.smartVariableEnable"
          :disabled="data.disabled || data.variableEnable"
        >
          {{ $t("ui.smart_variable_enable") }}
        </el-checkbox>
      </div>
    </el-dialog>
  </el-card>
</template>

<script>
import StepExtendBtns from "./UiStepExtendBtns";
import { useCommandStore } from "@/store";
import { checkScenarioEnv, setScenarioDomain } from "@/api/scenario";
import { strMapToObj } from "metersphere-frontend/src/utils";
import {hasPermissionForProjectId} from "metersphere-frontend/src/utils/permission";
import Input from "@/business/automation/scenario/edit/command/Input.vue";

const commandStore = new useCommandStore();
export default {
  name: "UiBaseComponent",
  components: {Input, StepExtendBtns },
  data() {
    return {
      isShowInput: true,
      colorStyle: "",
      variableEnable: true,
      showStepBtnData: false,
      dialogVisible: false,
      commandStore: commandStore,
      showDescription: false
    };
  },
  props: {
    isReadonly: {
      type: Boolean,
      default: false,
    },
    refNodeIdCahceMap: Map,
    draggable: Boolean,
    collapsable: {
      type: Boolean,
      default: false,
    },
    commonControl: {
      type: Boolean,
      default: true,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    data: {
      type: Object,
      default() {
        return {};
      },
    },
    color: {
      type: String,
      default() {
        return "#B8741A";
      },
    },
    backgroundColor: {
      type: String,
      default() {
        return "#F9F1EA";
      },
    },
    isShowNameInput: {
      type: Boolean,
      default() {
        return false;
      },
    },
    title: String,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
    environmentType: String,
    environmentGroupId: String,
    envMap: Map,
    showEnable: {
      type: Boolean,
      default: true,
    },
    showCopy: {
      type: Boolean,
      default: true,
    },
    //元素总数
    totalCount: {
      type: Number,
      default: 0,
    },
  },
  watch: {
    "commandStore.selectCommand": function () {
      if (
        commandStore.selectCommand &&
        commandStore.selectCommand.id === this.data.id
      ) {
        this.colorStyle = this.color;
      } else {
        this.colorStyle = "";
      }
    },
  },
  created() {
    if (this.data.description) {
      this.isShowInput = true;
    }
    if (this.$refs.nameEdit) {
      this.$nextTick(() => {
        this.$refs.nameEdit.focus();
      });
    }
  },
  methods: {
    setScenario(open) {
      this.dialogVisible = open;
    },
    checkEnv(val) {
      checkScenarioEnv(this.data.id).then((res) => {
        if (this.data.environmentEnable && !res.data) {
          this.data.environmentEnable = false;
          this.$warning(this.$t("commons.scenario_warning"));
          return;
        }
        this.setDomain(val);
      });
    },
    setDomain(val) {
      let param = {
        environmentEnable: val,
        id: this.data.id,
        environmentType: this.environmentType,
        environmentGroupId: this.environmentGroupId,
        environmentMap: strMapToObj(this.envMap),
        definition: JSON.stringify(this.data),
      };
      setScenarioDomain(param).then((res) => {
        if (res.data) {
          let data = JSON.parse(res.data);
          this.data.hashTree = data.hashTree;
          this.setOwnEnvironment(this.data.hashTree);
        }
      });
    },
    setOwnEnvironment(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        let typeArray = [
          "JDBCPostProcessor",
          "JDBCSampler",
          "JDBCPreProcessor",
        ];
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          scenarioDefinition[i].environmentEnable = this.data.environmentEnable;
          scenarioDefinition[i].refEevMap = new Map();
          if (this.data.environmentEnable && this.data.environmentMap) {
            scenarioDefinition[i].refEevMap = this.data.environmentMap;
          }
        }
        if (
          scenarioDefinition[i].hashTree !== undefined &&
          scenarioDefinition[i].hashTree.length > 0
        ) {
          this.setOwnEnvironment(scenarioDefinition[i].hashTree);
        }
      }
    },
    showStepBtn() {
      this.showStepBtnData = true;
    },
    outStepBtn() {
      this.showStepBtnData = false;
    },
    rename() {
      this.$emit("rename");
    },
    active() {
      if (this.data.projectId && !hasPermissionForProjectId("PROJECT_UI_SCENARIO:READ",this.data.projectId)) {
        this.$message.error(this.$t('ui.no_scenario_read_permission'))
      } else {
        this.$emit("active");
      }
    },
    copyRow() {
      this.$emit("copy");
    },
    remove() {
      this.$emit("remove");
    },
    openScenario(data) {
      this.$emit("openScenario", data);
    },
    editName() {
      this.isShowInput = false;
      this.$nextTick(() => {
        this.$refs.nameEdit.focus();
      });
    },
    enter($event) {
      if (this.showVersion) {
        $event.currentTarget.className = "scenario-name";
      } else {
        $event.currentTarget.className = "scenario-version";
      }
    },
    leave($event) {
      if (this.showVersion) {
        $event.currentTarget.className = "scenario-unscroll";
      } else {
        $event.currentTarget.className = "scenario-version";
      }
    },
    enable() {
      this.data.enable = !this.data.enable;
      //递归将所有节点状态改变
      try {
        this.doEnableChild(this.data, this.data.enable);
      } catch (e) {
        console.warn("开启状态设置异常");
      }
    },
    doEnableChild(node, status) {
      node.enable = status;
      let hashTree = node.hashTree;
      if (!hashTree || hashTree.length <= 0) {
        return;
      }
      hashTree.forEach((v) => {
        this.doEnableChild(v, status);
      });
    },
    assertionSwitch(value) {
      this.$emit("assertionSwitch", value);
    },
  },
};
</script>

<style scoped>
.icon.is-active {
  transform: rotate(90deg);
}

.name-input {
width: 100%;
}

.el-icon-arrow-right {
  margin-right: 5px;
}

.ms-left-btn {
  font-size: 13px;
  margin-right: 15px;
  margin-left: 10px;
}

.header-right {
  margin-top: 0px;
  float: right;
  z-index: 1;
  display: flex;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: flex-end;
  align-items: center;
  width: 20%;
}

.enable-switch {
  margin-right: 10px;
}

.ms-step-name {
  display: inline-block;
  font-size: 13px;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 140px;
}

.scenario-version {
  display: inline-block;
  font-size: 13px;
  margin: 0 5px;
  /*overflow-x: hidden;*/
  overflow-x: scroll;
  overflow-y: hidden;
  padding-bottom: 0;
  /*text-overflow: ellipsis;*/
  vertical-align: middle;
  white-space: nowrap;
  width: 85%;
  height: auto;
}

.scenario-version::-webkit-scrollbar {
  background-color: #fff;
}

/*定义滚动条轨道 内阴影+圆角*/
.scenario-version::-webkit-scrollbar-track {
  -webkit-box-shadow: inset 0 0 6px #fff;
  border-radius: 1px;
  background-color: #ffffff;
}

/*定义滑块 内阴影+圆角*/
.scenario-version::-webkit-scrollbar-thumb {
  border-radius: 1px;
  -webkit-box-shadow: inset 0 0 6px #fff;
  background-color: #783887;
}

.scenario-version::-webkit-scrollbar {
  /* width: 0px; */
  height: 3px;
  position: fixed;
}

.scenario-name {
  display: inline-block;
  font-size: 13px;
  margin: 0 5px;
  /*overflow-x: hidden;*/
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 0;
  /*text-overflow: ellipsis;*/
  vertical-align: middle;
  white-space: nowrap;
  width: 85%;
  height: auto;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  scrollbar-track-color: transparent;
  -ms-scrollbar-track-color: transparent;
}

.scenario-name::-webkit-scrollbar {
  background-color: #fff;
}

/*定义滚动条轨道 内阴影+圆角*/
.scenario-name::-webkit-scrollbar-track {
  -webkit-box-shadow: inset 0 0 6px #fff;
  border-radius: 1px;
  background-color: #ffffff;
}

/*定义滑块 内阴影+圆角*/
.scenario-name::-webkit-scrollbar-thumb {
  border-radius: 1px;
  -webkit-box-shadow: inset 0 0 6px #fff;
  background-color: #783887;
}

.scenario-name::-webkit-scrollbar {
  /* width: 0px; */
  height: 3px;
  position: fixed;
}

.scenario-unscroll {
  display: inline-block;
  font-size: 13px;
  margin: 0 5px;
  overflow-x: hidden;
  /*overflow-x: auto;*/
  overflow-y: hidden;
  padding-bottom: 0;
  /*text-overflow: ellipsis;*/
  vertical-align: middle;
  white-space: nowrap;
  width: 85%;
  height: auto;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  scrollbar-track-color: transparent;
  -ms-scrollbar-track-color: transparent;
}

:deep(.el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: 12px;
}

fieldset {
  padding: 0px;
  margin: 0px;
  min-width: 100%;
  min-inline-size: 0px;
  border: 0px;
}
.input-combination{
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: flex-start;
  align-items: center;
  width: 80%;
}
.ms-step-name-width {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 400px;
}

.ms-step-selected {
  cursor: pointer;
  border-color: #783887;
}

.step-container {
  display: flex;
  justify-content: space-between;
}

.step-content {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 80%;
}

.split {
  padding-top: 10px;
}

.var-config-dialog /deep/ .el-dialog__body {
  padding: 10px 30px 30px 23px;
}
</style>
