<template>
  <div>
    <el-collapse v-model="activeAction">
      <el-collapse-item name="confirm" title="确认申请">
        <template slot="title">
          <span style="margin-left: 20px; font-weight: bold">确认申请</span>
          <el-button @click.stop="reloadRegList" :disabled="regListReloading" size="small"
            style="margin-left: 20px">刷新</el-button>
        </template>
        <div style="margin: 0 10px">
          <el-card v-for="app in regList" :key="app.id" style="margin-bottom: 10px">
            <div slot="header" style="padding-bottom: 8px">
              <span style="font-weight: bold; font-size: 14px">{{ app.name }}</span>
              <span style="margin-left: 10px">{{ APP_TYPES[app.type].cn }}</span>
              <el-button @click="() => confirmReg(app.name)" size="small" type="primary"
                style="float: right">通过申请</el-button>
            </div>
            <div>
              <el-form label-width="100px">
                <el-form-item label="申请人">{{ app.applicant }}</el-form-item>
                <el-form-item label="申请时间">{{ app.create_time }}</el-form-item>
                <el-form-item label="天玑项目">{{ app.phecda_project_id }}</el-form-item>
                <el-form-item label="GIT地址">{{ app.git_url }}</el-form-item>
                <el-form-item label="SRC路径">{{ app.src }}</el-form-item>
                <el-form-item label="初始分支">{{ app.ver_init_branch }}</el-form-item>
              </el-form>
            </div>
          </el-card>
        </div>
      </el-collapse-item>
      <el-collapse-item name="apps" title="所有应用" style="margin-top: 10px">
        <template slot="title">
          <span style="margin-left: 20px; font-weight: bold;">所有应用</span>
          <el-button @click.stop="reloadApps" :disabled="appsReloading" size="small"
            style="margin-left: 20px">刷新</el-button>
        </template>
        <div style="margin: 10px">
          <el-table :data="apps" stripe border>
            <el-table-column prop="name" label="应用名称" width="300"></el-table-column>
            <el-table-column label="天玑项目" width="300">
              <template slot-scope="scope">
                <a target="_blank" :href="scope.row.phecda.url">{{ scope.row.phecda.name }}</a>
              </template>
            </el-table-column>
            <el-table-column label="流水线" width="100">
              <template slot-scope="scope">
                <a v-if="scope.row.flow.pipeline !== null" target="_blank" :href="scope.row.flow.pipeline">流水线</a>
              </template>
            </el-table-column>
            <el-table-column label="报告列表">
              <template slot-scope="scope">
                <a v-if="scope.row.flow.url !== null" target="_blank" :href="scope.row.flow.url">查看</a>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
const {
  activeAction,
  regList,
  regListReloading,
  reloadRegList,
  confirmReg,
  apps,
  appsReloading,
  reloadApps,
} = setup()
</script>

<script>
import { computed, ref, watchEffect } from 'vue'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'
import { APP_TYPES, useCovsApps, usePhecdaProjects } from '@/store/project'

function setup() {
  const activeAction = ref(['confirm', 'apps'])

  const { regList, regListReloading, reloadRegList, confirmReg } = useAppReg()

  const { apps, appsReloading, reloadApps } = useApps()

  return {
    activeAction,
    regList,
    regListReloading,
    reloadRegList,
    confirmReg,
    apps,
    appsReloading,
    reloadApps,
  }
}

function useAppReg() {
  const regListReloading = ref(false)

  const regList = ref([])

  async function reloadRegList() {
    regListReloading.value = true
    const data = await CovserverAPI.covsAppRegList()
    regList.value = data ?? []
    regListReloading.value = false
  }

  watchEffect(() => reloadRegList())

  async function confirmReg(name) {
    const data = await CovserverAPI.covsAppConfirm(name)
    Utils.Toast(data ?? false, { pre: '通过申请' })
  }

  return { regList, regListReloading, reloadRegList, confirmReg }
}

function useApps() {
  const appsReloading = ref(false)

  const { fetchPhecdaProjects, getPhecdaProjects } = usePhecdaProjects()
  const { apps: covsApps, refreshCovsApps } = useCovsApps()

  async function reloadApps() {
    appsReloading.value = true
    await fetchPhecdaProjects()
    await refreshCovsApps()
    appsReloading.value = false
  }

  watchEffect(() => reloadApps())

  const apps = computed(() => {
    const { map = {} } = getPhecdaProjects() ?? {}

    return (covsApps.value ?? []).map((app) => {
      const phecdaId = app['phecda_project_id']
      const phecdaName = (map[`${phecdaId}`] ?? {}).name ?? '未知'
      const pipelineId = Utils.unchain(app, ['flow', 'pipeline'], null)
      const { flow_inst_id: flowInstId = null, flow_comp_inst_id: flowCompInstId = null } = app.flow ?? {}
      return {
        name: app.name ?? "",
        phecda: {
          id: phecdaId,
          name: phecdaName,
          url: `http://phecda.cicc.group/project/${phecdaId}`,
        },
        flow: {
          url: (flowInstId !== null && flowCompInstId !== null) ? `/#/testutils/precision/reports/${flowInstId}/${flowCompInstId}` : null,
          pipeline: pipelineId === null ? null : `http://phecda.cicc.group/torrent/valueStream/${pipelineId}`,
        },
      }
    })
  })

  return { apps, appsReloading, reloadApps }
}
</script>
