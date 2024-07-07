<template>
    <div>
        <el-table :data="stasks" border stripe>
            <el-table-column type="index" width="40" align="center"></el-table-column>
            <el-table-column prop="time" label="执行时间" align="center">
                <template slot-scope="scope">
                    <el-tooltip :content="scope.row.desc" placement="right-start">
                        <span>{{ scope.row.time }}</span>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column label="全量覆盖率" header-align="center">
                <el-table-column label="分支" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full.branch.percent }}{{ scope.row.full.branch.ratio }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="行" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full.line.percent }}{{ scope.row.full.line.ratio }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="方法" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full.method.percent }}{{ scope.row.full.method.ratio }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="类" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full.class.percent }}{{ scope.row.full.class.ratio }}</span>
                    </template>
                </el-table-column>
            </el-table-column>
            <el-table-column label="增量覆盖率" header-align="center">
                <el-table-column label="分支" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta.branch.percent }}{{ scope.row.delta.branch.ratio }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="行" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta.line.percent }}{{ scope.row.delta.line.ratio }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="方法" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta.method.percent }}{{ scope.row.delta.method.ratio }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="类" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta.class.percent }}{{ scope.row.delta.class.ratio }}</span>
                    </template>
                </el-table-column>
            </el-table-column>
            <el-table-column width="70" label="标记" align="center">
                <template slot-scope="scope">
                    <i v-if="scope.row.note === '*'" title="点击取消标记" class="el-icon-star-on note-on"
                        @click="() => clearNote(scope.row.id)"></i>
                    <el-tooltip v-else-if="scope.row.note" :content="scope.row.note" placement="left-start">
                        <i title="点击取消标记" class="el-icon-star-on note-on" @click="() => clearNote(scope.row.id)"></i>
                    </el-tooltip>
                    <i v-else title="点击设置标记" class="el-icon-star-off note-off" @click="() => setNote(scope.row.id)"></i>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
                <template slot-scope="scope">
                    <div class="actions">
                        <a :href="scope.row.url" target="_blank">查看</a>
                    </div>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script setup>
const props = defineProps({
    tasks: {
        type: Array,
        required: true,
    }
})

const { stasks, setNote, clearNote } = useShowTasks(props)
</script>

<script>
import { ref, watch } from 'vue'
import { MessageBox } from 'element-ui'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'

const KEYS = ['branch', 'line', 'method', 'class']

function useShowTasks(props) {
    function _get_note(c, t) {
        const percent = t === 0 ? 'n/a' : c === 0 ? '0' : `${Math.floor(c / t * 10000) / 100}%`
        const ratio = t === 0 ? '' : ` (${c}/${t})`
        return { percent, ratio }
    }

    function _handle_counter_s(data, key) {
        if (Utils.nil(data)) {
            return { percent: '', ratio: '' }
        }

        const { c = 0, t = 0 } = data[key]
        return _get_note(c, t)
    }

    function _handle_counter_m(data, key) {
        if (Utils.nil(data)) {
            return { percent: '', ratio: '' }
        }

        let [csum, tsum] = [0, 0]
        Object.keys(data).forEach((name) => {
            const { c = 0, t = 0 } = (data[name] ?? {})[key]
            csum += c
            tsum += t
        })

        return _get_note(csum, tsum)
    }

    function _handle_counter(data, key) {
        // 兼容处理
        if (Utils.nil(data.line)) {
            // 多模块
            return _handle_counter_m(data, key)
        } else {
            // 单模块
            return _handle_counter_s(data, key)
        }
    }

    function _handle(data) {
        const result = {}

        KEYS.forEach(function (key) {
            result[key] = _handle_counter(data, key)
        })

        return result
    }

    function _get_task_desc(result) {
        function _helper(str) {
            return str.length > 12 ? str.substring(0, 8) : str
        }

        const { base_commit_id: baseCommitId, base_rev: baseRev, comp_commit_id: compCommitId, comp_rev: compRev } = result
        return `增量对比: ${_helper(compRev)} (${_helper(compCommitId)}) , ${_helper(baseRev)} (${_helper(baseCommitId)})`
    }

    function _get_time(id) {
        const year = id.substring(0, 4)
        const month = id.substring(4, 6)
        const day = id.substring(6, 8)
        const hour = id.substring(8, 10)
        const min = id.substring(10, 12)
        const sec = id.substring(12, 14)
        const msec = id.substring(14, 17)
        return `${year}/${month}/${day} ${hour}:${min}:${sec}.${msec}`
    }

    const stasks = ref([])

    watch(() => props.tasks, ((tasks = []) => {
        stasks.value = tasks.map(function ({ task_id: taskId, result, note }) {
            const { summary } = result
            const full = _handle(summary.full)
            const delta = _handle(summary.delta)
            const desc = _get_task_desc(result)

            return {
                id: taskId,
                desc,
                time: _get_time(taskId),
                full,
                delta,
                note: Utils.nil(note) || note === '' ? null : decodeURI(note),
                url: `/#/testutils/precision/report/${taskId}`,
            }
        })
    }))

    function _set_note(id, note) {
        if (!Utils.nil(stasks.value) && stasks.value.length > 0) {
            for (let i = 0; i < stasks.value.length; i++) {
                if (stasks.value[i].id === id) {
                    Vue.set(stasks.value[i], 'note', note)
                }
            }
        }
    }

    async function setNote(id) {
        MessageBox.prompt('输入备注信息 (可为空)', '备注', {
            inputValidator: (note) => {
                const MAX_LEN = 64;
                if (!Utils.nil(note) && note.length > MAX_LEN) {
                    return `备注长度不能超过${MAX_LEN}个字符`
                }
            },
            callback: async (action, instance) => {
                let note = instance.inputValue
                if (action === 'confirm') {
                    note = Utils.nil(note) ? '*' : encodeURI(note)
                    const ok = await CovserverAPI.covsTaskNote(id, note)
                    if (ok) {
                        _set_note(id, note)
                    } else {
                        Utils.Toast(false, { fail: '设置标记失败' })
                    }
                }
            },
        })
    }

    async function clearNote(id) {
        const ok = await CovserverAPI.covsTaskNote(id, null)
        if (ok) {
            _set_note(id, null)
        } else {
            Utils.Toast(false, { fail: '取消标记失败' })
        }
    }

    return { stasks, setNote, clearNote }
}
</script>

<style scoped>
.actions>a:not(:first-of-type) {
    margin-left: 5px;
}

.actions {
    margin: 0;
    padding: 0;
}

.note-on {
    cursor: pointer;
    font-size: 24px;
    color: #f56c6c;
}

.note-on:hover {
    color: #f78989;
}

.note-off {
    cursor: pointer;
    font-size: 20px;
    color: #909399;
}

.note-off:hover {
    color: #f78989;
}
</style>

<style>
.el-message-box input {
    border: 1px solid #DCDFE6 !important;
    border-radius: 4px !important;
}
</style>
