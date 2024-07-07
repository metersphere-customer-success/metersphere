<template>
  <div class="main">
    <permission-query v-if="querying" />
    <report-tables v-else-if="permit" :level="LEVEL.APP" :tid="tid" :data="data" />
    <no-permission-hint v-else :name="appname" />
  </div>
</template>

<script setup>
const route = useRoute()
const { permit, appname, querying, tid, data } = setup(route)
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

  const data = ref({})
  watch([tid, permit], async function ([id, permit]) {
    if (!Utils.nil(id) && permit) {
      const { deltas, ...task } = await getTask(id, appname.value)
      data.value = task ?? {}
    }
  }, { immediate: true })

  return { permit, appname, querying, tid, data }
}
</script>

<style scoped>
.main {
  padding: 1em;
}
</style>
