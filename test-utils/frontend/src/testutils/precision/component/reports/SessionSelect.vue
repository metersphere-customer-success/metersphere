<template>
    <el-select v-model="session">
        <el-option v-for="(item, idx) in sessions" :key="idx" :label="item.label" :value="item.session"></el-option>
    </el-select>
</template>

<script setup>
const props = defineProps(['appid', 'value'])
const emit = defineEmits(['input'])
const { session, sessions } = setup(props, emit)
</script>

<script>
import { computed, ref, watch } from 'vue'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'

function setup(props, emit) {
    const sessions = ref([])

    watch(function () {
        return props.appid
    }, async function (id) {
        if (!Utils.empty(id)) {
            const list = await fetchSessionList(Number(id))
            sessions.value = list.map(function (session) {
                return { session, label: session.split('.').slice(2).join('.') }
            })
        } else {
            sessions.value = []
        }
    }, { immediate: true })

    const session = computed({
        get() {
            return props.value
        },
        set(val) {
            emit('input', val)
        }
    })

    watch(sessions, function (list) {
        session.value = (list[0] ?? {}).session ?? ''
    }, { immediate: true })

    return { session, sessions }
}

async function fetchSessionList(appid) {
    if (Utils.nil(appid) || appid < 0) {
        return []
    }

    const data = await CovserverAPI.sessionList(appid)
    return data ?? []
}
</script>
