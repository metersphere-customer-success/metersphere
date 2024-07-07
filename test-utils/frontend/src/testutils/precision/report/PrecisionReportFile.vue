<template>
    <div class="main">
        <permission-query v-if="querying" />
        <div v-else-if="permit">
            <div>
                <report-breadcrumb :level="LEVEL.FILE" :tid="tid" :info="info" />
            </div>
            <div style="margin-top: 2em;">
                <div v-if="loading" class="loading">{{ CONSTANS.LOADING }}</div>
                <highlight-code v-else :name="name" :code="code" class="compact" />
            </div>
        </div>
        <no-permission-hint v-else :name="appname" />
    </div>
</template>

<script setup>
const route = useRoute()
const { permit, appname, querying, tid, info, loading, name, code } = setup(route)
</script>

<script>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router/composables'

import { HighLightJS, Utils } from '@/common'
import { TYPES, useProject, useTaskPermission } from '@/store/project'
import { CONSTANS, getCode, getTask, LEVEL } from '../stores/report'

import { HighlightCode, NoPermissionHint, PermissionQuery } from '../component/common'
import ReportBreadcrumb from '../component/report/ReportBreadcrumb'

const State = {}

function setup(route) {
    const tid = computed(function () {
        return route.params.tid
    })

    useProject(TYPES.COVS, true, tid)

    const { permit, appname, querying } = useTaskPermission(tid)

    const info = computed(function () {
        return { mod: route.params.mod, pkg: route.params.pkg, src: route.params.file }
    })

    const name = computed(function () {
        return `${route.params.mod}.${route.params.pkg}.${route.params.file}`
    })

    const loading = ref(false)
    const code = ref('')

    watch(function () {
        return [route.params, permit.value]
    }, async function ([{ tid, mod, pkg, file }, permit]) {
        if (!permit || Utils.nil(tid) || Utils.nil(mod) || Utils.nil(pkg) || Utils.nil(file)) {
            return
        }

        loading.value = true

        const { deltas, ...task } = await getTask(tid, mod)
        State['LINES'] = transLinesInfo(task, deltas, mod, pkg, file)

        const { path } = Utils.unchain(task, [mod, 'packages', pkg], {})
        code.value = await getCode(tid, `${path}/${file}`, mod)
        loading.value = false

        useAutoScroll(route.hash)
    }, { immediate: true, deep: true })

    HighLightJS.setCovsPlugin(linecovs)

    return { permit, appname, querying, tid, info, loading, name, code }
}

function transLinesInfo(task, deltas, mod, pkg, file) {
    const key = `${pkg.replaceAll('.', '/')}/${file}`
    const covs = Utils.unchain(task, [mod, 'packages', pkg, 'sources', file, 'lines'], []);
    const blocks = Utils.unchain(deltas, [mod, key, 'blocks'], [])

    const lines = {}
    covs.forEach(function (cov) {
        lines[cov[0] - 1] = { cov: cov.slice(1) }
    })

    blocks.forEach(function ({ begin, end, type }) {
        for (let i = begin; i < end; i++) {
            lines[i] = Object.assign({}, lines[i], { delta: type })
        }
    })

    return lines
}

function linecovs($lines) {
    for (let i = 0; i < $lines.length; i++) {
        const lineInfo = State['LINES'][i]
        const $line = $lines[i]
        if (Utils.nil(lineInfo)) {
            $line.setAttribute('id', `L${i + 1}`)
        } else {
            const { cov, delta } = lineInfo

            if (!Utils.nil(cov)) {
                const [mi, ci, mb, cb] = cov
                $line.classList.add(ci === 0 ? 'nc' : mb === 0 ? 'fc' : 'pc')

                if (mb !== 0 && cb !== 0) {
                    $line.classList.add('bpc')
                    $line.title = `${mb} of ${mb + cb} branches missed.`;
                } else if (mb === 0 && cb !== 0) {
                    $line.classList.add('bfc')
                    $line.title = `All ${cb} branches covered.`;
                } else if (mb !== 0 && cb === 0) {
                    $line.classList.add('bnc')
                    $line.title = `All ${mb} branches missed.`;
                }
            }

            if (Utils.nil(delta)) {
                $line.setAttribute('id', `L${i + 1}`)
            } else {
                $line.setAttribute('data-delta-type', delta === 'REPLACE' ? '-+' : ' +')
                const $div = document.createElement('DIV')
                $div.setAttribute('id', `L${i + 1}`)
                $div.classList.add('delta')
                $div.appendChild($line)
                $lines[i] = $div
            }
        }
    }
}

function useAutoScroll(hash) {
    if (Utils.nil(hash)) {
        return
    }

    const id = hash.substring(1)
    setTimeout(function () {
        const $line = document.getElementById(id)
        if (!Utils.nil($line)) {
            $line.scrollIntoView()
        }
    }, 200)
}
</script>

<style scoped>
.main {
    padding: 1em;
}

.commit {
    float: right;
}

.loading {
    text-align: center;
    font-weight: bold;
}
</style>

<style>
@import '@/assets/precision/default.css';
</style>
