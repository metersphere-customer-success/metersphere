<template>
  <el-select v-model="pid" :placeholder="hint" :disabled="disabled" class="project-select">
    <el-option v-for="(option, idx) in options" :key="idx" :label="option.label" :value="option.value" />
  </el-select>
</template>

<script setup>
const props = defineProps({
    type: {
        type: String,
        default: TYPES.PHECDA
    }
})

setup(props)
</script>

<script>
import { TYPES, pid, options, hint, disabled, construct } from '@/store/project'
import { watch } from 'vue';

function setup(props) {
    watch(function () {
        return props.type
    }, function (type) {
        construct(type)
    }, { immediate: true })
}
</script>

<style scoped>
.project-select {
    width: 200px;
    margin: 0 10px;
    border: 0;
}
</style>

<style>
.el-input__inner {
    border: 0px !important;
}
</style>
