<template>
  <ui-base-component
  color="#f0a1ab"
  background-color="#fdf3f5"
  :data="command"
  :draggable="draggable"
  :title="$t('ui.screenshot')"
  :show-btn="true"
  @copy="copyRow"
  @remove="remove"
 :isReadonly="isReadonly"
>
  <template v-slot:headerLeft>
    <ms-instructions-icon
      size="10"
      :content="$t('ui.screen_tip')"
    />
    <div class="text-wrapper">
      <i
        class="el-icon-edit"
        style="cursor: pointer; margin-left: 10px;"
        v-show="showEditIcon && !isReadonly"
        @click="editName"
      />
      <div class="font-viewer" v-show="showEditIcon">
        {{ command.targetVO.text }}
      </div>
    </div>
    <el-input
      :disabled="isReadonly"
      v-show="!showEditIcon"
      size="mini"
      v-model="command.targetVO.text"
      maxlength="60"
      show-word-limit
      ref="editInput"
      @blur="showEditIcon = true"
      style="padding-left: 10px"
    ></el-input>
  </template>
</ui-base-component>
</template>

<script>
import ApiBaseComponent from "@/business/automation/scenario/common/ApiBaseComponent";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
export default {
  name: "UiScreenshotComponent",
  components: { UiBaseComponent, ApiBaseComponent, MsInstructionsIcon },
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
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    draggable: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showEditIcon: true,
    };
  },
  mounted() {
    this.command.valueVO.text = {};
  },
  methods: {
    editName() {
      this.showEditIcon = false;
      this.$nextTick(() => {
        this.$refs.editInput.focus();
      });
    },
    remove() {
      this.$emit("remove", this.command, this.node);
    },
    copyRow() {
      this.$emit("copyRow", this.command, this.node);
    },
  },
};
</script>

<style scoped>
.time-input {
  width: 100%;
}
.font-viewer {
  width: 400px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.text-wrapper{
  display: inline-flex;
  align-items: flex-end;
}
</style>
