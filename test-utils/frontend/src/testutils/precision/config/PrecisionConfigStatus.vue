<template>
    <div>
        <div>
            <phecda-app-select v-model="appname" style="display: inline" />
            <el-input v-if="admin" v-model="sappname" style="width: 200px; margin-right: 10px;" />
            <el-button type="primary" @click="queryAppStatus" :disabled="querying">查询</el-button>
        </div>
        <div v-if="querying">
            <div class="query-alert">状态查询中...</div>
        </div>
        <div v-else>
            <config-status-step v-for="(step, idx) in steps" :step="step" :key="`${qid}-${idx}`"
                style="margin-bottom: 10px;" />
        </div>
    </div>
</template>

<script setup>
const { admin, appname, sappname, steps, qid, querying, queryAppStatus } = setup()
</script>

<script>
import { computed, ref, watch } from 'vue'
import { CovserverAPI } from '@/api'
import { ROLES, useUserConfig } from '@/store/project'

import PhecdaAppSelect from '../component/common/PhecdaAppSelect'
import ConfigStatusStep from '../component/config/ConfigStatusStep'

function setup() {
    const { user } = useUserConfig()
    const admin = computed(() => user.value.role === ROLES.SYS_ADMIN)

    const qid = ref(0)
    const querying = ref(false)

    const appname = ref('')
    const sappname = ref('')

    const steps = ref([])

    watch(appname, () => {
        sappname.value = ''
    }, { immediate: true })

    async function queryAppStatus() {
        querying.value = true
        qid.value += 1
        const name = sappname.value.length <= 0 ? appname.value : sappname.value
        steps.value = (await CovserverAPI.covsAppStatus(name)) ?? []
        querying.value = false
    }

    return { admin, appname, sappname, steps, qid, querying, queryAppStatus }
}
</script>

<style scoped>
.query-alert {
    font-size: 16px;
    font-weight: bold;
    text-align: center;
}
</style>
