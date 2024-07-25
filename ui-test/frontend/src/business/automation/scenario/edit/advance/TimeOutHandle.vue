<template>
  <div>
    <el-form class="error-handle"  :disabled="isReadonly">
      <el-form-item :label="$t('ui.wait_element_timeout')" v-if="value.commandConfig">

        <el-input-number
          v-model="value.commandConfig.secondsWaitElement"
          class="time-input"
          size="mini"
          :min="0"
          :step="1000"/>
        ms

        <el-tooltip class="item" effect="dark" placement="right">
          <div slot="content">
            如果步骤涉及的元素定位在一定时间内无法找到元素，将会提示超时错误。<br/>
            (注：若超时错误的日志显示会四舍五入成 秒，但不影响等待具体时长)
          </div>
          <i :style="{'font-size': 10 + 'px', 'margin-left': 3 + 'px'}" class="el-icon-info"></i>
        </el-tooltip>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "TimeOutHandle",
  props: ["value", "isReadonly"],
}
</script>

<style scoped>
.error-handle {
  margin-top: 8px;
}

.time-input {
  width: 210px;
}
</style>
