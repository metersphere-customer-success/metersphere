<template>
    <div class="main">
        <div>
            <el-form :inline="true">
                <el-form-item label="应用">
                    <app-select v-model="appid" />
                </el-form-item>
                <el-form-item label="时间范围">
                    <date-range-picker v-model="range" />
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" :disabled="mergeDisabled" @click="mergeVisible = true">合并</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="refresh">刷新</el-button>
                </el-form-item>
            </el-form>
        </div>
        <div>
            <app-task-table :app="appid" :tasks="tasks" :reload="refresh" :fetching="fetching" v-model="mtids" />
        </div>

        <!-- Merge Dialog -->
        <el-dialog title="报告合并" :visible.sync="mergeVisible">
            <div>
                <el-table :data="mtids" height="300" border stripe>
                    <el-table-column type="index" width="50"></el-table-column>
                    <el-table-column prop="tid" label="报告ID" align="center"></el-table-column>
                    <el-table-column prop="bcommit" label="提交ID" align="center"></el-table-column>
                </el-table>
            </div>
            <div style="margin-top: 10px">
                <el-form>
                    <el-form-item label="目标提交">
                        <el-select v-model="compCommit" clearable placeholder="请选择"
                            style="width: 80%; border: 1px solid #DCDFE6; border-radius: 4px">
                            <el-option v-for="option in compOptions" :key="option" :label="option.substring(0, 8)"
                                :value="option"></el-option>
                        </el-select>
                        <el-tooltip content="进行报告合并的目标提交, 为空时自动选择报告中最后一次提交">
                            <i class="el-icon-question" style="margin-left: 10px; font-size: 20px"></i>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item label="增量基准">
                        <el-input v-model="baseCommit" placeholder="请输入"
                            style="width: 80%; border: 1px solid #DCDFE6; border-radius: 4px"></el-input>
                        <el-tooltip content="进行代码增量对比时的基准提交, 为空时不生成增量数据">
                            <i class="el-icon-question" style="margin-left: 10px; font-size: 20px"></i>
                        </el-tooltip>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer">
                <el-button @click="mergeVisible = false">取消</el-button>
                <el-button @click="merge" type="primary">确定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
const { appid, range, tasks, fetching, mtids, mergeDisabled, mergeVisible, compCommit, baseCommit, compOptions, refresh, merge } = setup()
</script>

<script>
import { computed, ref, watch } from 'vue'

import { Utils, Storage } from '@/common'
import { CovserverAPI } from '@/api'

import { DateRangePicker } from './component/common'
import { AppSelect, AppTaskTable } from './component/reports'

const PRE_KEY = 'precision'
const COVSAPP = 'covsapp'

function setup() {
    const appid = useAppId()

    const range = useDateRange()

    const { tasks, fetching, refresh } = useTasks(appid, range)

    const { mtids, mergeDisabled, mergeVisible, compCommit, baseCommit, compOptions, merge } = useMerge(appid)

    return { appid, range, tasks, fetching, mtids, mergeDisabled, mergeVisible, compCommit, baseCommit, compOptions, refresh, merge }
}

function useAppId() {
    const appid = ref(Storage.getLocalItem(PRE_KEY, COVSAPP) ?? '')

    watch(appid, function (id) {
        Storage.setLocalItem(id, PRE_KEY, COVSAPP)
    })

    return appid
}

function useDateRange() {
    function _fill(n) {
        return n < 10 ? `0${n}` : `${n}`
    }

    function _days(days) {
        const dday = new Date()
        dday.setTime(dday.getTime() - 3600 * 1000 * 24 * days)
        const year = dday.getFullYear()
        const month = _fill(dday.getMonth() + 1)
        const day = _fill(dday.getDate())
        return `${year}-${month}-${day}`
    }

    const range = ref([_days(14), _days(0)])

    return range;
}

function useTasks(appid, range) {
    const tasks = ref([])

    const fetching = ref(false)

    async function _fetch(id, sday, eday) {
        if (Utils.nil(id) || id === '') {
            return
        }

        fetching.value = true
        const data = await CovserverAPI.covsAppTasks(id, sday, eday)
        tasks.value = data ?? []
        fetching.value = false
    }

    watch([appid, range], async ([id, [sday, eday]]) => {
        await _fetch(id, sday, eday)
    }, { immediate: true })

    async function refresh() {
        const [sday, eday] = range.value
        await _fetch(appid.value, sday, eday)
    }

    return { tasks, fetching, refresh }
}

function useMerge(appid) {
    const mtids = ref([])

    const mergeDisabled = computed(() => mtids.value.length <= 0)

    const mergeVisible = ref(false)

    const compCommit = ref('')
    const baseCommit = ref('master')

    const compOptions = computed(() => [...new Set(mtids.value.map(({ commit }) => commit))])

    function _trans(cid) {
        return cid === '' ? null : cid
    }

    async function merge() {
        const tids = mtids.value.map(({ tid }) => tid)
        const comp = _trans(compCommit.value)
        const base = _trans(baseCommit.value)

        const $toast = Utils.Toast(true, { succ: '报告合并中...', duration: 0 })
        const data = await CovserverAPI.covsTaskMerge(appid.value, tids, comp, base)
        $toast.close()

        if (!Utils.nil(data)) {
            mergeVisible.value = false
            Utils.Toast(true, { succ: `报告合并成功: ${data.tid}, 请点击刷新查看` })
        }
    }

    return { mtids, mergeDisabled, mergeVisible, compCommit, baseCommit, compOptions, merge }
}
</script>

<style scoped>
.main {
    padding: 1em;
}
</style>
