<template>
  <div class="header-right">
    <el-tooltip
      :content="$t('test_resource_pool.enable_disable')"
      placement="top"
    >
      <el-switch
        v-model="command.enable"
        class="enable-switch"
        size="mini"
        :disabled="isReadonly || command.disabled && !command.root"
        style="width: 30px"
      />
    </el-tooltip>
    <el-tooltip
      :content="$t('commons.copy')"
      placement="top"
    >
      <el-button
        size="mini"
        icon="el-icon-copy-document"
        circle
        @click="copyRow"
        style="padding: 5px"
        :disabled="isReadonly || command.disabled && !command.root"
      />
    </el-tooltip>
    <el-tooltip
      :content="$t('commons.delete')"
      placement="top"
    >
      <el-button
        size="mini"
        icon="el-icon-delete"
        type="danger"
        style="padding: 5px"
        circle
        @click="remove"
        :disabled="isReadonly || (command.disabled && !command.root)"
      />
    </el-tooltip>
  </div>
</template>

<script>
export default {
  name: "UiMoreBtn",
  props: {
    isReadonly: {
      type: Boolean,
      default: false,
    },
    command: Object
  },
  methods: {
    copyRow() {
      this.$emit("copyRow");
    },
    remove() {
      this.$emit("remove");
    }
  }
}
</script>

<style scoped>

.header-right {
  margin-top: 0px;
  float: right;
  z-index: 1;
}

.enable-switch {
  margin-right: 10px;
}

</style>
