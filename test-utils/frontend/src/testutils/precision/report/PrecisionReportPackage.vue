<template>
    <div class="main">
        <permission-query v-if="querying" />
        <report-tables v-if="permit" :level="props.level" :tid="props.tid" :info="info" :data="data" />
        <no-permission-hint v-else :name="appname" />
    </div>
</template>

<script setup>
const props = defineProps({
    level: {
        type: String,
        default: LEVEL.CLASSES,
    },
    tid: {
        type: String,
        required: true,
    },
    mod: {
        type: String,
        default: null,
    },
    pkg: {
        type: String,
        default: null,
    },
})

const { permit, appname, querying, info, data } = setup(props)
</script>

<script>
import { computed, ref, toRefs, watch } from 'vue'

import { Utils } from '@/common'
import { TYPES, useProject, useTaskPermission } from '@/store/project'
import { LEVEL, getTask } from '../stores/report'

import { NoPermissionHint, PermissionQuery } from '../component/common'
import ReportTables from '../component/report/ReportTables'

function setup(props) {
    const info = computed(function () {
        return { mod: props.mod, pkg: props.pkg }
    })

    useProject(TYPES.COVS, true, toRefs(props).tid)

    const { permit, appname, querying } = useTaskPermission(toRefs(props).tid)

    const data = ref({})
    watch([props, permit], async function ([{ level, tid, mod, pkg }, permit]) {
        if (!permit || Utils.nil(pkg)) {
            return
        }

        const { deltas, ...task } = await getTask(tid, mod)
        const key = level === LEVEL.CLASSES ? 'classes' : 'sources'
        data.value = Utils.unchain(task, [mod, 'packages', pkg, key], {})
    }, { immediate: true, deep: true })

    return { permit, appname, querying, info, data }
}
</script>

<style scoped>
.main {
    padding: 1em;
}
</style>
