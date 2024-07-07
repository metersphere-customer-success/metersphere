<template>
  <div class="main">
    <permission-query v-if="querying" />
    <report-tables v-else-if="permit" :level="LEVEL.MODULE" :tid="tid" :info="info" :data="data" />
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

  const mod = computed(function () {
    return route.params.mod
  })

  useProject(TYPES.COVS, true, tid)

  const { permit, appname, querying } = useTaskPermission(tid)

  const info = ref({})
  const data = ref({})

  watch([tid, mod, permit], async function ([id, mod, permit]) {
    if (!Utils.nil(id) && permit) {
      const { deltas, ...task } = await getTask(id, mod)
      data.value = Utils.unchain(task, [mod, 'packages'], {})
      info.value = { mod }
    }
  }, { immediate: true })

  return { permit, appname, querying, tid, info, data }
}
</script>

<style scoped>
.main {
  padding: 1em;
}
</style>
