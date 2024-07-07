<template>
    <el-select v-model="commit">
        <el-option v-for="(item, idx) in commits" :key="idx" :label="item.label" :value="item.commit">
            <span :title="item.desc">{{ item.label }}</span>
        </el-option>
    </el-select>
</template>

<script setup>
const props = defineProps({
    tasks: {
        type: Array,
        default: () => ([])
    },
    value: {
        type: String,
        required: true
    },
    showall: {
        type: Boolean,
        default: false
    },
})
const emit = defineEmits(['input'])
const { commit, commits } = setup(props, emit)
</script>

<script>
import { computed, watch } from 'vue';
import { Utils } from '@/common'

function setup(props, emit) {
    const { commits } = useCommits(props)
    const { commit } = useCommit(props, emit, commits)
    return { commit, commits }
}

function useCommit(props, emit, commits) {
    const commit = computed({
        get() {
            return props.value
        },
        set(val) {
            emit('input', val)
        }
    })

    watch(commits, function (val = []) {
        commit.value = (val[0] ?? {}).commit ?? ''
    }, { immediate: true })

    return { commit }
}

function useCommits(props) {
    function transTime(time) {
        return time.substring(0, 19).replace(/-/g, '/')
    }

    const commits = computed(function () {
        if (Utils.nil(props.tasks)) {
            return []
        }

        const map = {}

        let astime = '3000-01-01 00:00:00.0'
        let aetime = '1970-01-01 00:00:00.0'

        props.tasks.filter(function ({ errno }) {
            return errno === 0
        }).forEach(function ({ create_time: createTime, result }) {
            const { commit_id: commitId } = result
            if (createTime < astime) {
                astime = createTime
            }
            if (createTime > aetime) {
                aetime = createTime
            }

            const item = map[commitId]
            if (item === null || item === undefined) {
                map[commitId] = [createTime, createTime]
            } else {
                const [stime, etime] = item
                if (createTime < stime) {
                    item[0] = createTime
                }
                if (createTime > etime) {
                    item[1] = createTime
                }
            }
        })

        const commits = Object.keys(map).map(function (commit) {
            const label = commit.substring(0, 8)
            const [stime, etime] = map[commit]
            const desc = transTime(stime) + ' - ' + transTime(etime)
            return { commit, label, desc }
        })

        if (props.showall) {
            commits.push({ commit: '**ALL**', label: '全部提交', desc: transTime(astime) + ' - ' + transTime(aetime) })
        }

        return commits
    })


    return { commits }
}
</script>
