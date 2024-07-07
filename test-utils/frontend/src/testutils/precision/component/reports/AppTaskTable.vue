<template>
    <div>
        <el-table ref="table" v-loading="loading" :data="dtasks" border stripe @selection-change="onSelected">
            <el-table-column type="selection" width="50" align="center"></el-table-column>
            <el-table-column label="执行时间" align="center">
                <template slot-scope="scope">
                    <el-tooltip :content="scope.row.desc" placement="right-start">
                        <span>{{ scope.row.time }}</span>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column label="全量覆盖率" header-align="center">
                <el-table-column label="分支" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full[0] }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="行" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full[1] }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="方法" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full[2] }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="类" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.full[3] }}</span>
                    </template>
                </el-table-column>
            </el-table-column>
            <el-table-column label="增量覆盖率" header-align="center">
                <el-table-column label="分支" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta[0] }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="行" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta[1] }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="方法" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta[2] }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="类" align="right" header-align="center">
                    <template slot-scope="scope">
                        <span>{{ scope.row.delta[3] }}</span>
                    </template>
                </el-table-column>
            </el-table-column>
            <el-table-column prop="env" label="环境" align="center"></el-table-column>
            <el-table-column width="90" label="提交" align="center">
                <template slot-scope="scope">
                    <span title="点击查看提交日志" class="clickable" @click="() => showCiMsg(scope.row)">
                        {{ scope.row.bcommit }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column width="70" label="流水线" align="center">
                <template slot-scope="scope">
                    <el-tooltip v-if="scope.row.flow.is" :content="scope.row.flow.desc" placement="left-start">
                        <a :href="scope.row.flow.url" target="_blank">是</a>
                    </el-tooltip>
                </template>
            </el-table-column>
            <el-table-column width="70" align="center">
                <template slot="header" slot-scope>
                    标记 <el-checkbox v-model="onlyNoted"></el-checkbox>
                </template>
                <template slot-scope="scope">
                    <i v-if="scope.row.note === '*'" title="点击取消标记" class="el-icon-star-on note-on"
                        @click="() => clearNote(scope.row.tid)"></i>
                    <el-tooltip v-else-if="scope.row.note" :content="scope.row.note" placement="left-start">
                        <i title="点击取消标记" class="el-icon-star-on note-on" @click="() => clearNote(scope.row.tid)"></i>
                    </el-tooltip>
                    <i v-else title="点击设置标记" class="el-icon-star-off note-off"
                        @click="() => setNote(scope.row.tid)"></i>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
                <template slot-scope="scope">
                    <div class="actions">
                        <a :href="scope.row.url" target="_blank">查看</a>
                        <el-popconfirm title="是否确定删除报告?" style="margin-left: 5px"
                            @confirm="() => remove(scope.row.tid)">
                            <a slot="reference">删除</a>
                        </el-popconfirm>
                    </div>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script setup>
const props = defineProps({
    app: {
        type: Number,
        required: true
    },
    tasks: {
        type: Array,
        default: () => ([])
    },
    value: {
        type: Array,
        required: true
    },
    fetching: {
        type: Boolean,
        default: false,
    },
    reload: {
        type: Function,
        default: () => { }
    },
})

const emit = defineEmits(['input'])

const table = ref(null)

const { dtasks, onlyNoted, loading, remove, setNote, clearNote, onSelected, showCiMsg } = setup(props, emit, table)
</script>

<script>
import { computed, ref, watch } from 'vue'
import { Notification, MessageBox } from 'element-ui'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'

function setup(props, emit, $table) {
    const { showCiMsg } = useCommitMessage(props)
    const { dtasks, ttasks, onlyNoted } = useTasks(props)
    const { onSelected } = useSelect(emit, $table, dtasks)
    const { loading, remove, setNote, clearNote } = useActions(props, ttasks)

    return { dtasks, onlyNoted, loading, remove, setNote, clearNote, onSelected, showCiMsg }
}

const KEYS = ['branch', 'line', 'method', 'class']

function useTasks(props) {
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

    function _get_hash(id) {
        return id.length === 40 ? id.substring(0, 8) : id
    }

    function _get_desc(tid, commit, comp) {
        const [brev, bid] = comp
        const cstr = _get_hash(commit)
        const bstr = brev.length <= 0 ? _get_hash(bid) : `${_get_hash(bid)} (${_get_hash(brev)})`
        return `ID: ${tid}, 当前提交: ${cstr}, 增量基准: ${bstr}`
    }

    function _get_summary_desc(c, t) {
        const percent = t === 0 ? 'n/a' : c === 0 ? '0' : `${Math.floor(c / t * 10000) / 100}%`
        const ratio = t === 0 ? '' : ` (${c}/${t})`
        return `${percent} ${ratio}`
    }

    function _get_summary(info = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]) {
        const [ic, it, lc, lt, bc, bt, mc, mt, cc, ct] = info
        const bd = _get_summary_desc(bc, bt)
        const ld = _get_summary_desc(lc, lt)
        const md = _get_summary_desc(mc, mt)
        const cd = _get_summary_desc(cc, ct)
        return [bd, ld, md, cd]
    }

    function _get_flow(flow) {
        if (Utils.nil(flow)) {
            return { is: false }
        } else {
            const [fid, cfid, bid] = flow
            return { is: true, url: `/#/testutils/precision/report/${fid}/${cfid}`, desc: `版本: ${bid}` }
        }
    }

    function _trans(tasks = []) {
        return tasks.map(({ tid, commit, comp, env, flow, summary, note }) => {
            return {
                tid,
                commit,
                bcommit: _get_hash(commit),
                time: _get_time(tid),
                desc: _get_desc(tid, commit, comp),
                full: _get_summary(summary.full),
                delta: _get_summary(summary.delta),
                flow: _get_flow(flow),
                env,
                note: Utils.nil(note) ? null : decodeURI(note),
                url: `/#/testutils/precision/report/${tid}`
            }
        })
    }

    const onlyNoted = ref(false)

    const ttasks = ref([])
    watch(() => props.tasks, (val) => ttasks.value = val, { immediate: true, deep: true })

    const dtasks = computed(() => {
        return _trans((ttasks.value ?? []).filter(({ note }) => !onlyNoted.value || (!Utils.nil(note) && note.length > 0)))
    })

    return { dtasks, ttasks, onlyNoted }
}

function useActions(props, ttasks) {
    const loading = ref(false)

    watch(() => props.fetching, (val) => {
        loading.value = val
    }, { immediate: true })

    function _set_note(id, note) {
        if (!Utils.nil(ttasks.value) && ttasks.value.length > 0) {
            for (let i = 0; i < ttasks.value.length; i++) {
                if (ttasks.value[i].tid === id) {
                    Vue.set(ttasks.value[i], 'note', note)
                }
            }
        }
    }

    async function remove(id) {
        const ok = await CovserverAPI.covsTaskRemove(id)
        if (ok) {
            loading.value = true
            await props.reload()
            loading.value = false
        } else {
            Utils.Toast(false, '删除报告失败')
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

    return { loading, remove, setNote, clearNote }
}

function useSelect(emit, $table, dtasks) {
    const selected = ref(new Set())

    let restore = false
    let toggle = false
    let tasks = []

    watch(dtasks, (val) => {
        restore = true
        tasks = val
    })

    function onSelected(val) {
        if (toggle) {
            return
        }

        const nids = (val ?? []).map(({ tid }) => tid)
        if (restore) {
            toggle = true
            for (let i = 0; i < tasks.length; i++) {
                const row = tasks[i]
                if (selected.value.has(row.tid)) {
                    $table.value.toggleRowSelection(row, true)
                }
            }
            toggle = false

            const tids = new Set((tasks ?? []).map(({ tid }) => tid))
            const sids = [...selected.value].filter((tid) => tids.has(tid))
            selected.value = new Set([...sids, ...nids])
        } else {
            selected.value = new Set(nids)
        }
    }

    watch(selected, (val) => {
        const data = (dtasks.value ?? []).filter(({ tid }) => val.has(tid)).map(({ tid, commit, bcommit }) => ({ tid, commit, bcommit }))
        emit('input', data)
        restore = false
    }, { immediate: true })

    return { onSelected }
}

function useCommitMessage(props) {
    const messages = {}

    let $notification = null

    async function _fetch_message(commit, bcommit) {
        Notification.info({ message: `获取提交日志 ${bcommit}` })
        const message = await CovserverAPI.covsAppCommitMessage(props.app, commit)
        return message
    }

    async function _get_message(commit, bcommit) {
        let message = messages[commit]
        if (Utils.nil(message)) {
            const log = await _fetch_message(commit, bcommit)
            message = log.replaceAll("\n", '<br/>')
            messages[commit] = message
        }

        return message
    }

    function _notify(bcommit, message) {
        return Notification.info({
            title: `提交日志 ${bcommit}`,
            dangerouslyUseHTMLString: true,
            message,
            onClose: () => $notification = null
        })
    }

    async function showCiMsg({ commit, bcommit }) {
        const message = await _get_message(commit, bcommit)
        if ($notification !== null) {
            $notification.close()
        }
        $notification = _notify(bcommit, message)
    }

    return { showCiMsg }
}
</script>

<style scoped>
.clickable {
    cursor: pointer;
}

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
