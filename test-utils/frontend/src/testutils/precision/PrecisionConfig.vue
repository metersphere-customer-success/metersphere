<template>
  <div class="main">
    <el-tabs v-model="tab">
      <el-tab-pane label="用户信息" name="user" class="pane">
        <precision-config-user />
      </el-tab-pane>
      <el-tab-pane label="项目应用" name="app" class="pane">
        <precision-config-app :toRegister="toRegister" />
      </el-tab-pane>
      <el-tab-pane label="应用申请" name="register" class="pane">
        <precision-config-register :phecda="phecda" />
      </el-tab-pane>
      <el-tab-pane label="应用状态" name="status" class="pane">
        <precision-config-status />
      </el-tab-pane>
      <el-tab-pane v-if="admin" label="管理操作" name="admin" class="pane">
        <precision-config-admin />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
const { tab, admin, phecda, toRegister } = setup()
</script>

<script>
import { computed, ref } from 'vue'

import { ROLES, TYPES, useProject, useUserConfig } from '@/store/project'

import { PrecisionConfigAdmin, PrecisionConfigApp, PrecisionConfigRegister, PrecisionConfigStatus, PrecisionConfigUser } from './config'

function setup() {
  useProject(TYPES.NONE, true)

  const { user } = useUserConfig()
  const admin = computed(() => user.value.role === ROLES.SYS_ADMIN)

  const tab = ref('user')

  const phecda = ref(null)

  function toRegister(pid) {
    tab.value = 'register'
    phecda.value = pid
  }

  return { tab, admin, phecda, toRegister }
}
</script>

<style scoped>
.main {
  padding: 1em;
}

.pane {
  padding: 0.5em;
}
</style>
