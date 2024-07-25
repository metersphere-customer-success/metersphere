<template>

  <ui-base-component
    :isReadonly="isReadonly"
    color="#67C23A"
    background-color="#F2F9EE"
    :data="command"
    :draggable="draggable"
    :title="$t('ui.wait_time')"
    :show-btn="true"
    @copy="copyRow"
    @remove="remove">

    <template v-slot:headerLeft>
      <el-input-number
        v-model="command.targetVO.text"
        class="time-input"
        size="mini"
        :disabled="command.disabled || isReadonly"
        :min="0" :step="1000"
        ref="nameInput"/>
      &nbsp;ms
    </template>

  </ui-base-component>
</template>

<script>

import ApiBaseComponent from "@/business/automation/scenario/common/ApiBaseComponent";
import UiBaseComponent from "@/business/automation/scenario/component/UiBaseComponent";

export default {
  name: "UiPauseComponent",
  components: {UiBaseComponent, ApiBaseComponent},
  props: {
    isReadonly:{
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
    return {}
  },
  created() {
    // this.$nextTick(() => {
    //   this.$refs.nameInput.focus();
    // });
  },
  methods: {
    remove() {
      this.$emit('remove', this.command, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.command, this.node);
    },
  }
}
</script>

<style scoped>
.time-input {
  width: 100%;
}
</style>
