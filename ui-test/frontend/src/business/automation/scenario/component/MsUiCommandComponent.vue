<template>
  <ui-base-component
    v-loading="loading"
    :refNodeIdCahceMap="refNodeIdCahceMap"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    @openCommand="openCommand"
    :data="command"
    :show-collapse="false"
    :is-show-name-input="false"
    :is-disabled="true"
    :collapsable="collapsable"
    :show-btn="showBtn"
    :show-enable="false"
    :is-show-input="false"
    :show-copy="false"
    :show-version="showVersion"
    color="color"
    background-color="backgroundColor"
    :if-from-variable-advance="ifFromVariableAdvance"
    :environmentType="environmentType"
    :environmentGroupId="environmentGroupId"
    :envMap="envMap"
    :title="title"
    @rename="editName"
    @mouseenter.native="enter($event)"
    @mouseleave.native="leave($event)"
    class="baseCommand"
    ref="commandRef"
  >
    <template v-slot:beforeHeaderLeft>
      <div class="step-content">
        <div
          v-if="command.index"
          class="el-step__icon is-text enable-switch"
          :style="{ color: color, 'background-color': backgroundColor }"
          style="min-width: 20px"
        >
          <div class="el-step__icon-inner">{{ command.index }}</div>
        </div>
        <span style="width: 5px; display: inline-block"
          ><i
            v-if = "checkStepDisabledForREF"
            class="el-icon-edit edit-name-opt"
            style="cursor: pointer"
            @click="editName"
            v-show="editable && !editNameStart"
        /></span>
        <el-tooltip effect="dark" :content="title" placement="top" :open-delay = "1000">
          <span v-if="editNameStart">
            <el-input
              size="mini"
              ref="editNameRef"
              class="name-input"
              @blur="editNameStart = false"
              maxlength="100"
              show-word-limit
              :placeholder="$t('commons.input_name')"
              v-model="command.name"
              style="-webkit-user-drag: element;user-select: none;"
            ></el-input>
          </span>
          <span
            v-if="!editNameStart"
            class="name-text"
            :style="{ color: fontColor, width: nodeWidth }"
          >
            {{ title }}
          </span>
        </el-tooltip>
      </div>
    </template>

    <template v-slot:headerLeft>
      <span></span>
    </template>

    <template v-slot:afterTitle>
      <slot name="afterTitle" />
    </template>

    <template v-slot:debugStepCode>
      <span v-if="node.data.testing" class="ms-test-running">
        <i class="el-icon-loading" style="font-size: 16px" />
        {{ $t("commons.testing") }}
      </span>
      <span
        class="ms-step-debug-code"
        :class="node.data.code === 'error' ? 'ms-req-error' : 'ms-req-success'"
        v-if="!loading && node.data.debug && !node.data.testing"
      >
        {{ getCode() }}
      </span>
    </template>

    <template v-slot:result>
      <ui-scenario-command
        :show-switch="false"
        class="scenario-command"
        :select-command="command"
        ref="cmd"
      />
    </template>
  </ui-base-component>
</template>

<script>
import {STEP} from "@/api/Setting";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import UiScenarioCommand from "@/business/automation/scenario/edit/command/BaseCommand";
import elementResizeDetectorMaker from "element-resize-detector";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "MsUiCommandComponent",
  components: { UiScenarioCommand, UiBaseComponent },
  props: {
    command: {},
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
    collapsable: {
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
  data() {
    return {
      editNameStart: false,
      loading: false,
      isShowInput: false,
      isShowNum: false,
      stepFilter: new STEP(),
      color: "#783887",
      backgroundColor: "#FCF6EE",
      editable: false,
      nodeWidth: "",
      title: ""
    };
  },
  watch: {
    message() {
      if (this.message === "stop") {
        this.command.run = false;
      }
      this.reload();
    },
    "command": {
      deep: true,
      handler() {
        this.resetCommandName(true);
      }
    }
  },
  created() {
    if (this.command.num) {
      this.isShowNum = true;
    } else {
      this.isShowNum = false;
    }
    if (!this.command.projectId) {
      this.command.projectId = getCurrentProjectID();
    }
  },
  mounted() {
    this.caclWidth(243);
    let erd = elementResizeDetectorMaker();
    erd.listenTo(this.$refs.commandRef.$el, (ele) => {
      this.caclWidth(ele.clientWidth);
    });
    this.resetCommandName(true);
  },
  computed: {
    checkStepDisabledForREF(){
      if(!this.refNodeIdCahceMap){
        return true;
      }
      return !this.refNodeIdCahceMap.get(this.command.id);
    },
    isDeletedOrRef() {
      if (
        (this.command.referenced != undefined &&
          this.command.referenced === "Deleted") ||
        this.command.referenced === "REF"
      ) {
        return true;
      }
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
    fontColor() {
      if (this.command.enable) {
        return "black";
      } else {
        return "rgb(127, 127, 127)";
      }
    }
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
      this.$emit("remove", this.command, this.node);
    },
    active() {
      this.command.active = !this.command.active;
      this.reload();
    },
    copyRow() {
      this.$emit("copyRow", this.command, this.node);
    },
    openCommand(data) {
      this.$emit("openCommand", data);
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    /**
     * 因为导入的指令有可能是 cmdOpen 等系统原始名称，
     * 首次加载进行一次翻译，后续的变化都保持原来用户输入的指令名字
     * @param first 是否是首次加载
     */
    resetCommandName(first) {
      //优先展示用户自定义 name 字段
      if (this.command.name == this.command.command) {
        // eslint-disable-next-line vue/no-side-effects-in-computed-properties
        //对旧数据进行兼容 以及 导入数据重新校准，
        this.command.name = first ? this.$t("ui." + this.command.command) : " " + this.command.name;
      }
      this.title = this.command.name;
    }
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
.step-content {
  display: flex;
  justify-content: flex-start;
}
</style>
