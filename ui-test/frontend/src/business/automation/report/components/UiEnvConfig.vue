<template>
  <header class="report-header">
    <el-row>
         <span>
          <span>{{ $t('report.run_model') }}：</span>
          <span>{{ modeDesc }}</span>
        </span>
    </el-row>
  </header>
</template>

<script>
export default {
  name: "UiEnvConfig",
  props: {
    envConfig: Object
  },
  data() {
    return {
      modeDesc: "",
    }
  },
  created() {
    if (this.envConfig.mode === 'serial') {
      this.modeDesc = '串行'
    } else if (this.envConfig.mode === 'parallel') {
      this.modeDesc = '并行'
    }
  }
}


</script>

<style scoped>
</style>
