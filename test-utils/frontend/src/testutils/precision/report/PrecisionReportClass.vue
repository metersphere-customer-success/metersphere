<template>
    <div class="main">
        <permission-query v-if="querying" />
        <report-tables v-else-if="permit" :level="LEVEL.CLASS" :tid="tid" :info="info" :data="data" />
        <no-permission-hint v-else :name="appname" />
    </div>
</template>

<script setup>
const route = useRoute()
const { permit, appname, querying, tid, info, data } = setup(route)
</script>

<script>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router/composables'

import { Utils } from '@/common'
import { TYPES, useProject, useTaskPermission } from '@/store/project'
import { LEVEL, getTask } from '../stores/report'

import { NoPermissionHint, PermissionQuery } from '../component/common'
import { ReportTables } from '../component/report'

function setup(route) {
    const tid = computed(function () {
        return route.params.tid
    })

    useProject(TYPES.COVS, true, tid)

    const { permit, appname, querying } = useTaskPermission(tid)

    const info = ref({})
    const data = ref({})

    watch(function () {
        return [route.params, permit.value]
    }, async function ([{ tid, mod, pkg, class: clazz }, permit]) {
        if (!permit || Utils.nil(tid) || Utils.nil(mod) || Utils.nil(pkg) || Utils.nil(clazz)) {
            return
        }

        const { deltas, ...task } = await getTask(tid, mod)
        const classInfo = Utils.unchain(task, [mod, 'packages', pkg, 'classes', clazz], {})

        info.value = { mod, pkg, class: clazz, src: classInfo.source ?? '' }
        data.value = classInfo.methods ?? {}
    }, { immediate: true, deep: true })

    return { permit, appname, querying, tid, info, data }
}
</script>

<style scoped>
.main {
    padding: 1em;
}
</style>
