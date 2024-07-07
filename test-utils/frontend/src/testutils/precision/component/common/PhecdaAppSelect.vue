<template>
    <el-form :inline="true">
        <el-form-item label="天玑项目">
            <el-select v-model="phecdaId">
                <el-option v-for="phecda in phecdas" :value="phecda.pid" :label="phecda.name" :key="phecda.pid" />
            </el-select>
        </el-form-item>
        <el-form-item label="应用">
            <el-select v-model="appId">
                <el-option v-for="app in covapps" :value="app.id" :label="app.name" :key="app.id" />
            </el-select>
        </el-form-item>
        <el-form-item v-if="props.reloadable">
            <el-button @click="refreshUserConfig">更新</el-button>
        </el-form-item>
    </el-form>
</template>

<script setup>
const props = defineProps({
    reloadable: {
        type: Boolean,
        default: false,
    },
    value: {
        type: String,
        default: '',
    },
})

const emit = defineEmits(['input'])

const { phecdas, phecdaId, covapps, appId, refreshUserConfig } = setup(props, emit)
</script>

<script>
import { ref, watch } from 'vue'
import { useUserConfig } from '@/store/project.js'

import { Utils } from '@/common'

function setup(props, emit) {
    const { apps, refreshUserConfig } = useUserConfig()

    const { phecdas, phecdaId, covapps, appId } = usePhecdaAppSelect(apps, props, emit)

    return { phecdas, phecdaId, covapps, appId, refreshUserConfig }
}

function usePhecdaAppSelect(apps, props, emit) {
    const phecdas = ref([])
    const phecdaId = ref('')
    const covapps = ref([])
    const appId = ref('')

    function getIdByName(name) {
        const arr = apps.value ?? []
        for (let i = 0; i < arr.length; i++) {
            const { pid, apps: _apps = [] } = arr[i] ?? {}
            for (let j = 0; j < _apps.length; j++) {
                if (_apps[j].name === name) {
                    return [pid, _apps[j].id]
                }
            }
        }

        return ['', '']
    }

    function getAppNameById(id) {
        const arr = covapps.value ?? []
        for (let i = 0; i < arr.length; i++) {
            if (arr[i].id === id) {
                return arr[i].name
            }
        }

        return ''
    }

    watch(() => props.value, (name) => {
        const [pid, aid] = getIdByName(name)
        if (pid.length > 0 && aid.length > 0) {
            phecdaId.value = pid
            appId.value = aid
        }
    }, { immediate: true })

    function adjustPhecdaId(id, phecdas = []) {
        if (phecdas.some(({ pid }) => pid === id)) {
            return id
        } else {
            return (phecdas[0] ?? {}).pid ?? ''
        }
    }

    function adjustAppId(id, apps = []) {
        if (apps.some(({ id: _id }) => _id === id)) {
            return id
        } else {
            return (apps[0] ?? {}).id ?? ''
        }
    }

    function getCovApps(pid) {
        if (phecdaId.value.length <= 0) {
            return []
        }

        if (!Utils.nil(apps.value)) {
            for (let i = 0; i < apps.value.length; i++) {
                const { apps: _apps, pid: _pid } = apps.value[i]
                if (_pid === pid) {
                    return _apps
                }
            }
        }

        return []
    }

    watch(apps, (apps = []) => {
        phecdas.value = apps.map(({ name, pid }) => ({ name, pid }))
        phecdaId.value = adjustPhecdaId(phecdaId.value, apps)
    }, { immediate: true })

    watch(phecdaId, (pid) => {
        covapps.value = getCovApps(pid)
        appId.value = adjustAppId(appId.value, covapps.value)
    }, { immediate: true })

    watch(appId, (id) => {
        const name = getAppNameById(id)
        emit('input', name)
    }, { immediate: true })

    return { phecdas, phecdaId, covapps, appId }
}
</script>
