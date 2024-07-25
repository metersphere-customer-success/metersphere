<template>
  <div>
    <span>{{ getShowName() }}</span>
    <el-tooltip content="Copy" placement="right">
      <el-button size="mini" icon="el-icon-copy-document" circle @click="copyCommand" style="padding: 5px"/>
    </el-tooltip>
    <el-dropdown @command="handleCommand" class="scenario-ext-btn">
      <el-link type="primary" :underline="false">
        <el-icon class="el-icon-more"></el-icon>
      </el-link>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="remove">{{ this.$t('api_test.automation.delete_step') }}</el-dropdown-item>
        <el-dropdown-item command="remove">{{ this.$t('api_test.automation.delete_step') }}</el-dropdown-item>
        <el-dropdown-item command="remove">{{ this.$t('api_test.automation.delete_step') }}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  name: "UiScenarioEditTreeItem",
  props: ["data", "paramDefinition", "commandDefinition"],
  data() {
    return {
    }
  },
  methods: {
    /**
     * 默认 comment,如果没有 comment 就展示指令定义的名称
     */
    getShowName() {
      if (this.commandDefinition) {
        if (this.data.comment) {
          return this.data.comment;
        } else {
          return this.commandDefinition[this.data.command].cnName;
        }
      }
    },
    copyCommand() {

    },
    handleCommand() {

    }
  }
}
</script>

<style scoped>

</style>
