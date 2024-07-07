<template>
  <div>
    <el-button-group>
      <el-button :disabled="refreshing" @click="refresh" size="small">刷新</el-button>
    </el-button-group>
    <el-collapse v-model="activePhecdas" style="margin-top: 10px">
      <el-collapse-item v-for="phecda in apps" :name="phecda.pid" :key="`${upidx}-${phecda.pid}`"
        style="margin-bottom: 10px">
        <template slot="title">
          <div style="margin-left: 20px">
            <span style="font-weight: bold; font-size: 16px">{{ phecda.name }}</span>
            <span> ({{ phecda.pid }})</span>
            <el-button @click.stop="() => register(phecda.pid)" size="small" type="primary"
              style="margin-left: 20px">应用申请</el-button>
          </div>
        </template>
        <div style="margin: 10px">
          <config-app-card v-for="app in phecda.apps" :app="app" :key="`${upidx}-${phecda.pid}-${app.id}`"
            style="margin-bottom: 5px" />
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
const props = defineProps({
  toRegister: {
    type: Function,
    default: () => null,
  }
})

const { activePhecdas, apps, upidx, refreshing, refresh, register } = setup(props)

onMounted(async () => {
  await refresh()
})
</script>

<script>
import { onMounted, ref, watch } from 'vue'

import { useUserConfig } from '@/store/project'

import ConfigAppCard from '../component/config/ConfigAppCard'

function setup(props) {
  const { activePhecdas, apps, upidx, refreshing, refresh } = useApps()

  const { register } = useRegister(props)

  return { activePhecdas, apps, upidx, refreshing, refresh, register }
}

function useApps() {
  const upidx = ref(0)

  const { apps, refreshUserConfig } = useUserConfig()

  const refreshing = ref(false)

  const activePhecdas = ref([])

  watch(apps, (apps = []) => {
    upidx.value += 1
    activePhecdas.value = (apps ?? []).map(({ pid = '' }) => pid)
  }, { immediate: true })

  async function refresh() {
    refreshing.value = true
    await refreshUserConfig(true)
    refreshing.value = false
  }

  return { activePhecdas, apps, upidx, refreshing, refresh }
}

function useRegister(props) {
  function register(pid) {
    props.toRegister(pid)
  }

  return { register }
}
</script>
