<template>
  <div>
    <el-form class="screenshot-handle"  :disabled="isReadonly">
      <el-form-item
        :label="$t('ui.screenshot_config')"
        v-if="value.commandConfig"
      >
        <el-select v-model="value.commandConfig.screenshotConfig" size="mini">
          <el-option
            v-for="item in options"
            :key="item.label"
            :label="$t(item.label)"
            :value="item.value"
          />
        </el-select>
        <el-tooltip class="item" effect="dark" placement="right">
          <div slot="content">
            当前步骤截图: 场景步骤执行后截图，步骤如果触发原生弹窗（alert或prompt），或不存在页面时，截图不生效;<br />
            出现异常截图:
            当前步骤出现异常截图,包括场景步骤异常、数据提取和断言异常;<br />
            不截图: 当前场景步骤不截图。
          </div>
          <i
            :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
            class="el-icon-info"
          ></i>
        </el-tooltip>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "ScreenshotConfig",
  props: ["value", "isReadonly"],
  data() {
    return {
      options: [
        {
          label: this.$t("ui.current_step_screenshot"),
          value: 0,
        },
        {
          label: this.$t("ui.error_step_screenshot"),
          value: 1,
        },
        {
          label: this.$t("ui.not_screentshot"),
          value: 2,
        },
      ],
    };
  },
  mounted() {
    if (!this.value.commandConfig) {
      this.$set(this.value, "commandConfig", { "screenshotConfig": 1 });
    }
    //此处有0的情况不能简写
    if (!this.value.commandConfig.screenshotConfig && this.value.commandConfig.screenshotConfig !== 0) {
      this.$set(this.value.commandConfig, "screenshotConfig", 1);
    }
  },
};
</script>

<style scoped>
.screenshot-handle {
  margin-top: 8px;
}
</style>
