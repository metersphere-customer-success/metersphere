<template>
  <div class="main">
    <div>
      <el-form :inline="true">
        <el-form-item label="应用">
          <app-select v-model="appid" />
        </el-form-item>
        <el-form-item label="环境">
          <session-select v-model="session" :appid="appid" />
        </el-form-item>
        <el-form-item label="版本">
          <batch-select v-model="batch" :session="session" v-bind:tasks.sync="tasks" />
        </el-form-item>
        <el-form-item label="代码提交">
          <commit-select v-model="commit" :tasks="tasks" :showall="true" />
        </el-form-item>
        <el-form-item>
          <el-button v-if="!itagged" type="primary" size="small" @click="itagSet">标记迭代</el-button>
          <el-button v-else type="danger" size="small" @click="itagUnset">取消标记</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="refresh">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div>
      <task-table :tasks="ftasks" />
    </div>
  </div>
</template>

<script setup>
const { appid, session, batch, commit, tasks, ftasks, itagged, refresh, itagSet, itagUnset } = setup()
</script>

<script>
import { computed, ref, watch } from 'vue'

import { Utils, Storage } from '@/common'
import { CovserverAPI } from '@/api'

import { AppSelect, BatchSelect, SessionSelect, CommitSelect, TaskTable } from './component/reports'

const PRE_KEY = 'precision'
const COVSAPP = 'covsapp'

function setup() {
  const { appid } = useAppId()
  const { session, batch, commit, tasks, ftasks, refresh } = useTasks()
  const { itagged, itagSet, itagUnset } = useIterTags(appid, session, batch, commit)

  return { appid, session, batch, commit, tasks, ftasks, itagged, refresh, itagSet, itagUnset }
}

function useAppId() {
  const appid = ref(Storage.getLocalItem(PRE_KEY, COVSAPP) ?? '')

  watch(appid, function (id) {
    Storage.setLocalItem(id, PRE_KEY, COVSAPP)
  })

  return { appid }
}

function useTasks() {
  const session = ref('')
  const batch = ref('')
  const commit = ref('')
  const tasks = ref([])

  async function _refresh(session, batch) {
    if (!Utils.empty(session) && !Utils.empty(batch)) {
      tasks.value = (await CovserverAPI.taskList(session, batch)) ?? []
    }
  }

  async function refresh() {
    await _refresh(session.value, batch.value)
  }

  watch([session, batch], async ([ns, nb]) => _refresh(ns, nb), { immediate: true })

  const ftasks = computed(function () {
    return Utils.nil(commit.value) ? [] :
      (tasks.value ?? []).filter(function ({ errno, result: { commit_id: commitId } }) {
        return errno === 0 && (commit.value === '**ALL**' || commitId === commit.value)
      })
  })

  return { session, batch, commit, tasks, ftasks, refresh }
}

function useIterTags(appid, session, batch, commit) {
  const itags = ref([])

  async function loadIterTags(session, batch, commit) {
    if (!Utils.empty(session) && !Utils.empty(batch) && !Utils.empty(commit)) {
      itags.value = (await CovserverAPI.itagList(session, batch, commit)) ?? []
    }
  }

  watch([session, batch, commit], async function ([ns, nb, nc]) {
    await loadIterTags(ns, nb, nc)
  }, { immediate: true })

  const itagged = computed(function () {
    return (itags.value ?? []).some(function ({ commit_id: commitId }) {
      return commitId === commit.value
    })
  })

  async function itagSet() {
    await CovserverAPI.itagSet(appid.value, session.value, batch.value, commit.value)
    loadIterTags(session.value, batch.value, commit.value)
  }

  async function itagUnset() {
    await CovserverAPI.itagUnset(session.value, batch.value, commit.value)
    loadIterTags(session.value, batch.value, commit.value)
  }

  return { itagged, itagSet, itagUnset }
}
</script>

<style scoped>
.main {
  padding: 1em;
}
</style>
