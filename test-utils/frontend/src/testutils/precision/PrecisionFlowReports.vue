<template>
  <div class="main">
    <permission-query v-if="querying" />
    <div v-else-if="permit">
      <div>
        <el-form :inline="true">
          <el-form-item label="应用">
            <el-input :value="appname" disabled />
          </el-form-item>
          <el-form-item label="环境">
            <el-input :value="env" disabled />
          </el-form-item>
          <el-form-item label="版本">
            <el-input :value="batch" disabled />
          </el-form-item>
          <el-form-item label="代码提交">
            <el-select v-model="commit">
              <el-option v-for="(cmt, idx) in commits" :key="idx" :label="cmt.label" :value="cmt.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button v-if="!itagged" type="primary" size="small" @click="itagSet">标记迭代</el-button>
            <el-button v-else type="danger" size="small" @click="itagUnset">取消标记</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div>
        <task-table :tasks="tasks" />
      </div>
    </div>
    <no-permission-hint v-else :name="pname" />
  </div>
</template>

<script setup>
const route = useRoute()
const { permit, pname, querying, appname, env, batch, commit, commits, tasks, itagged, itagSet, itagUnset } = setup(route)
</script>

<script>
import { computed, ref, toRefs, watch } from 'vue'
import { useRoute } from 'vue-router/composables'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'
import { TYPES, useProject, useFlowPermission } from '@/store/project'

import { NoPermissionHint, PermissionQuery } from './component/common'
import { TaskTable } from './component/reports'

function setup(route) {
  useProject(TYPES.COVS, true, toRefs(route).fullPath)

  const { permit, pname, querying } = usePermission(route)
  const { flow } = useFlow(route, permit)
  const { appname, env, batch } = useFlowInfo(flow)
  const { commit, commits, tasks } = useFlowTasks(flow)
  const { itagged, itagSet, itagUnset } = useCommitItag(flow, commit)

  return { permit, pname, querying, appname, env, batch, commit, commits, tasks, itagged, itagSet, itagUnset }
}

function usePermission(route) {
  const flowInstId = computed(function () {
    return route.params.flowInstId
  })

  const flowCompInstId = computed(function () {
    return route.params.flowCompInstId
  })

  const { permit, appname, querying } = useFlowPermission(flowInstId, flowCompInstId)
  return { permit, pname: appname, querying }
}

function useFlow(route, permit) {
  const flow = ref({})

  watch(function () {
    return [permit.value, route.params.flowInstId, route.params.flowCompInstId]
  }, async function ([permit, flowInstIdStr, flowCompInstIdStr]) {
    if (!permit || Utils.nil(flowInstIdStr) || Utils.nil(flowCompInstIdStr)) {
      return
    }

    const flowInstId = Number(flowInstIdStr)
    const flowCompInstId = Number(flowCompInstIdStr)
    flow.value = await fetchCompFlow(flowInstId, flowCompInstId)
  }, { immediate: true, deep: true })

  return { flow }
}

function useFlowInfo(flow) {
  const appname = computed(function () {
    const fields = (flow.value.session ?? '').split(/\./)
    return fields[0] ?? ''
  })

  const env = computed(function () {
    const fields = (flow.value.session ?? '').split(/\./)
    return fields.length >= 3 ? fields.slice(2).join('.') : ''
  })

  const batch = computed(function () {
    return flow.value.batch ?? ''
  })

  return { appname, env, batch }
}

function useFlowTasks(flow) {
  const allTasks = computed(function () {
    return flow.value.tasks ?? []
  })

  const commits = computed(function () {
    const ids = allTasks.value.map(function ({ result }) {
      return (result ?? {})['commit_id']
    }).filter(function (id) {
      return !Utils.nil(id) && id.length > 0
    })

    return [...new Set(ids)].map(function (id) {
      return { label: id.substring(0, 8), value: id }
    })
  })

  const commit = computed({
    get() {
      return (commits.value[0] ?? {}).value ?? ''
    },
    set(ci) { }
  })

  const tasks = computed(function () {
    return allTasks.value.filter(function ({ result }) {
      return (result ?? {})['commit_id'] === commit.value
    })
  })

  return { commit, commits, tasks }
}

function useCommitItag(flow, commit) {
  const itagged = computed({
    get() {
      const itags = flow.value.itags ?? []
      return itags.includes(commit.value)
    },
    set(status) {
      if (status) {
        flow.value.itags = Object.assign([], flow.value.itags, [commit.value])
      } else {
        flow.value.itags = (flow.value.itags ?? []).filter(function (itag) {
          return itag !== commit.value
        })
      }
    }
  })

  async function itagSet() {
    const { appid, session, batch } = flow.value
    const status = await CovserverAPI.itagSet(appid, session, batch, commit.value)
    if (status === true) {
      itagged.value = true
    }
  }

  async function itagUnset() {
    const { session, batch } = flow.value
    const status = await CovserverAPI.itagUnset(session, batch, commit.value)
    if (status === true) {
      itagged.value = false
    }
  }

  return { itagged, itagSet, itagUnset }
}

async function fetchCompFlow(flowInstId, flowCompInstId) {
  const data = await CovserverAPI.covsCompFlow(flowInstId, flowCompInstId)
  return data ?? {}
}
</script>

<style scoped>
.main {
  padding: 1em;
}
</style>
