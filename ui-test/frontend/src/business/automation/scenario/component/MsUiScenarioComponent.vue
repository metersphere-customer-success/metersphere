<template>
  <ui-base-component
    v-loading="loading"
    :refNodeIdCahceMap="refNodeIdCahceMap"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    @openScenario="openScenario"
    :data="scenario"
    :show-collapse="false"
    :is-show-name-input="!isDeletedOrRef"
    :is-disabled="true"
    :is-max="isMax"
    :show-btn="showBtn"
    :show-version="showVersion"
    color="#F56C6C"
    background-color="#FCF1F1"
    :if-from-variable-advance="ifFromVariableAdvance"
    :environmentType="environmentType"
    :environmentGroupId="environmentGroupId"
    :envMap="envMap"
    :title="$t('场景')"
    @rename="editName"
    @mouseenter.native="enter($event)"
    @mouseleave.native="leave($event)"
    ref="scenarioRef"
  >
    <template v-slot:beforeHeaderLeft>
      <div class="step-content">
        <div
          v-if="scenario.index"
          class="el-step__icon is-text enable-switch"
          :style="{ color: '#F56C6C', 'background-color': '#FCF1F1' }"
        >
          <div class="el-step__icon-inner">{{ scenario.index }}</div>
        </div>

        <span style="width: 5px; display: inline-block; margin-left: 2px; line-height: 20px;" v-if = "isReference">
          <i class="el-icon-connection"></i>
        </span>

        <span style="width: 5px; display: inline-block; margin-left: 2px; line-height: 20px;" v-if="isCopy && (!checkStepDisabledForREF || !editable) && !editNameStart">
          <i class="el-icon-copy-document"></i>
        </span>

        <span style="width: 5px; display: inline-block"
        ><i
          v-if = "checkStepDisabledForREF"
          class="el-icon-edit edit-name-opt"
          style="cursor: pointer"
          @click="editName"
          v-show="editable && !editNameStart && !isReference"
        /></span>

        <el-tooltip
          effect="dark"
          :content="title"
          placement="top"
          :open-delay="1000"
        >
          <span v-if="editNameStart">
            <el-input
              size="mini"
              ref="editNameRef"
              class="name-input"
              maxlength="100"
              show-word-limit
              @blur="editNameStart = false"
              :placeholder="$t('commons.input_name')"
              v-model="scenario.name"
              style="-webkit-user-drag: element;user-select: none;"
            ></el-input>
          </span>
          <span
            v-else
            class="name-text"
            :style="{
              color: fontColor,
              'font-size': 'smaller',
              'margin-left': '10px',
              width: nodeWidth,
            }"
          >{{ scenario.name }}</span
          >
        </el-tooltip>
      </div>
    </template>

    <!--    <template v-slot:afterTitle>-->
    <!--      <span :style="{'color': fontColor}">{{ "（ ID: " + scenario.num + "）" }}</span>-->
    <!--      &lt;!&ndash;      <span v-xpack v-if="scenario.versionEnable">{{ $t('project.version.name') }}: {{ scenario.versionName }}</span>&ndash;&gt;-->
    <!--    </template>-->

    <template v-slot:debugStepCode>
      <!--       <span v-if="node.data.testing" class="ms-test-running">-->
      <!--         <i class="el-icon-loading" style="font-size: 16px"/>-->
      <!--         {{ $t('commons.testing') }}-->
      <!--       </span>-->
      <!--      <span class="ms-step-debug-code" :class="node.data.code ==='error'?'ms-req-error':'ms-req-success'" v-if="!loading && node.data.debug && !node.data.testing">-->
      <!--        {{ getCode() }}-->
      <!--      </span>-->
      <span></span>
    </template>
  </ui-base-component>
</template>

<script>
import {STEP} from "@/api/Setting";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import elementResizeDetectorMaker from "element-resize-detector";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "MsUiScenarioComponent",
  components: { UiBaseComponent },
  props: {
    scenario: {},
    currentScenario: {},
    message: String,
    node: {},
    isMax: {
      type: Boolean,
      default: false,
    },
    refNodeIdCahceMap: Map,
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    draggable: {
      type: Boolean,
      default: false,
    },
    currentEnvironmentId: String,
    projectList: Array,
    environmentType: String,
    environmentGroupId: String,
    envMap: Map,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    message() {
      if (this.message === "stop") {
        this.scenario.run = false;
      }
      this.reload();
    },
  },
  created() {
    if (this.scenario.num) {
      this.isShowNum = true;
    } else {
      this.isShowNum = false;
    }
    if (!this.scenario.projectId) {
      this.scenario.projectId = getCurrentProjectID();
    }
  },
  mounted() {
    this.caclWidth(243);
    let erd = elementResizeDetectorMaker();
    erd.listenTo(this.$refs.scenarioRef.$el, (ele) => {
      this.caclWidth(ele.clientWidth);
    });
  },
  data() {
    return {
      loading: false,
      isShowInput: false,
      isShowNum: false,
      stepFilter: new STEP(),
      editable: false,
      editNameStart: false,
      nodeWidth: "",
    };
  },
  computed: {
    isDeletedOrRef() {
      if (
        (this.scenario.referenced != undefined &&
          this.scenario.referenced === "Deleted") ||
        this.scenario.referenced === "REF"
      ) {
        return true;
      }
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
    fontColor() {
      if (this.scenario.enable) {
        return "black";
      } else {
        return "rgb(127, 127, 127)";
      }
    },
    title() {
      let isCustomCommand = this.scenario.type === "customCommand";
      let scType = isCustomCommand ? this.$t("ui.custom_command_title") : this.$t("commons.scenario");
      return (
        scType +
        " " +
        (this.scenario.referenced == "Copy"
          ? this.$t("commons.copy")
          : this.$t("api_test.scenario.reference")) +
        " (" +
        this.scenario.name +
        ")"
      );
    },
    isReference(){
      return this.scenario.referenced === "REF";
    },
    isCopy(){
      return this.scenario.referenced === "Copy";
    },
    checkStepDisabledForREF(){
      if(!this.refNodeIdCahceMap){
        return true;
      }
      return !this.refNodeIdCahceMap.get(this.scenario.id);
    },
  },
  methods: {
    caclWidth(width) {
      width = width ? width : 243;
      //(step 宽度 - (等级 - 1) * 18  ) - 20的index图标 - 5的edit -10padding - 5 opt
      let w = width - (this.node.level - 1) * 18 - 59;
      let base = w * 0.75;
      let factor =
        base *
        (0.1 - this.node.level * 0.1 * (this.node.level <= 3 ? 0.15 : 0.75));
      let verify = factor + base <= 5 ? 15 : factor + base;
      if(this.node.level <= 5 && verify < 100){
        verify = 100;
      }
      if(this.node.level > 5 && this.node.level <= 7 && verify < 80){
        verify = this.node.level === 6 ? 80 : 60;
      }
      this.nodeWidth = verify + "px";
    },
    editName() {
      this.editNameStart = true;
      this.$nextTick(() => {
        this.$refs.editNameRef.focus();
      });
    },
    enter($event) {
      this.editable = true;
    },
    leave($event) {
      this.editable = false;
    },
    getCode() {
      if (this.node && this.node.data.code && this.node.data.debug) {
        if (this.node.data.code && this.node.data.code === "error") {
          return "error";
        } else {
          return "success";
        }
      }
      return "";
    },
    remove() {
      this.$emit("remove", this.scenario, this.node);
    },
    active() {
      if (this.node) {
        this.node.expanded = !this.node.expanded;
      }
      this.reload();
    },
    copyRow() {
      this.$emit("copyRow", this.scenario, this.node);
    },
    openScenario(data) {
      this.$emit("openScenario", data);
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
  },
};
</script>

<style scoped>
.edit-name-opt {
  width: 6px;
  padding: 0px 1px;
}

.name-input {
  width: 100%;
}

:deep(.name-input .el-input__inner) {
  height: 20px;
  line-height: 20px;
  padding-left: 3px;
  margin-right: 3px;
}

.scenario-wrap-normal {
  margin-left: 20px;
}

.scenario-wrap-bigger {
  margin-left: 0px;
}
.step-content {
  display: flex;
  justify-content: flex-start;
}
.name-text {
  font-size: smaller;
  margin-left: 10px;
  overflow: hidden;
  flex: 11;
  display: inline-block;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
  line-height: 20px;
}
:deep(.el-tree-node__content>.el-tree-node__expand-icon) {
  padding: 0 !important;
}
</style>
