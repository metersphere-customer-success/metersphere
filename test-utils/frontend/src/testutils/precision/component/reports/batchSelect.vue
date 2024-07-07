<template>
  <el-select v-model="batch">
    <el-option v-for="(item, idx) in batches" :key="idx" :label="item" :value="item">
    </el-option>
  </el-select>
</template>

<script setup>
const props = defineProps(['session', 'tasks', 'value'])
const emit = defineEmits(['input'])
const { batch, batches } = setup(props, emit)
</script>

<script>
import { computed, ref, watch } from 'vue'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'

function setup(props, emit) {
    const batches = ref([])

    watch(function () {
        return props.session
    }, async function (session) {
        const { batches: _batches, tasks } = await fetchBatchList(session)
        batches.value = _batches ?? []
        emit('update:tasks', tasks)
    }, { immediate: true })

    const batch = computed({
        get() {
            return props.value
        },
        set(val) {
            emit('input', val)
        }
    })

    watch(batches, function (list = []) {
        batch.value = list[0] ?? ''
    }, { immediate: true })

    return { batch, batches }
}

async function fetchBatchList(session) {
    if (Utils.empty(session)) {
        return { batches: [], tasks: [] }
    }

    const data = await CovserverAPI.batchList(session)
    return data ?? { batches: [], tasks: [] }
}
</script>
