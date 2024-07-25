<template>
  <el-row :gutter="10">
    <el-col :span="24">
      <slot/>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: "CommandItemContainer"
}
</script>

<style scoped>

</style>
