<template>
    <el-select v-model="appid">
        <el-option v-for="(app, idx) in apps" :key="idx" :label="app.name" :value="app.id">
        </el-option>
    </el-select>
</template>

<script setup>
// vue 2.x 中的 v-model props 和 emit 与 vue 3.x 中不同
const props = defineProps(['value'])
const emit = defineEmits(['input'])
const { appid, apps } = setup(props, emit)
</script>

<script>
import { computed, watch } from 'vue'

import { usePhecdaCovsApps } from '@/store/project'

function setup (props, emit) {
    const appid = computed({
        get() {
            return props.value
        },
        set(val) {
            emit('input', val)
        }
    })

    const { apps } = usePhecdaCovsApps()
    watch(apps, function (val) {
        appid.value = (val[0] ?? {}).id ?? ''
    }, { immediate: true })

    return { appid, apps }
}
</script>
