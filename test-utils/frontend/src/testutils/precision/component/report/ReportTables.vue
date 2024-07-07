<template>
  <div>
    <div style="margin-bottom: 2em">
      <span v-if="trans.has" :class="['trans', 'bg-img', trans.icon]"><a :href="trans.url">{{ trans.label }}</a></span>
      <report-breadcrumb :level="props.level" :tid="props.tid" :info="props.info" />
    </div>
    <report-table type="delta" :level="props.level" :data="data" style="margin-bottom: 1em" />
    <report-table type="full" :level="props.level" :data="data" />
  </div>
</template>

<script setup>
const props = defineProps({
    level: {
        type: String,
    },
    tid: {
        type: String,
    },
    info: {
        type: Object,
        default: function () { return {} },
    },
    data: {
        type: Object,
        default: function () { return {} },
    },
})

const { data, trans } = setup(props)
</script>

<script>
import { computed, reactive, watch } from 'vue'

import { Utils } from '@/common'
import { LEVEL, getUrl, EL_ICON } from '../../stores/report'

import ReportBreadcrumb from './ReportBreadcrumb'
import ReportTable from './ReportTable'

function setup(props) {
    const data = computed(function () {
        return transfer(props.level, props.tid, props.info, props.data ?? {})
    })

    const trans = reactive({
        has: false,
        icon: EL_ICON[LEVEL.FILE],
        label: '文件',
        url: '',
    })

    watch(props, function ({ level, tid, info }) {
        if (Utils.nil(level) || Utils.nil(tid)) {
            return
        }
        const isClasses = level === LEVEL.CLASSES
        trans.has = [LEVEL.CLASSES, LEVEL.FILES].includes(level)
        trans.icon = isClasses ? EL_ICON[LEVEL.FILE] : EL_ICON[LEVEL.CLASS]
        trans.label = isClasses ? '文件' : '类'
        trans.url = getUrl(isClasses ? LEVEL.FILES : LEVEL.CLASSES, tid, info)
    }, { immediate: true, deep: true })

    return { data, trans }
}

function transfer(level, tid, info, data) {
    return Object.keys(data).map(function (element) {
        return {
            element,
            url: getElementUrl(level, tid, info, element, data[element]),
            info: data[element],
        }
    })
}

function getElementUrl(level, tid, info, element, data) {
    switch (level) {
        case LEVEL.APP:
            return getUrl(LEVEL.MODULE, tid, Object.assign({}, info, { mod: element }))

        case LEVEL.MODULE:
            return getUrl(LEVEL.CLASSES, tid, Object.assign({}, info, { pkg: element }))

        case LEVEL.CLASSES:
            return getUrl(LEVEL.CLASS, tid, Object.assign({}, info, { class: element }))

        case LEVEL.FILES:
            return getUrl(LEVEL.FILE, tid, Object.assign({}, info, { src: element }))

        case LEVEL.CLASS:
            return getUrl(LEVEL.FILE, tid, Object.assign({}, info, { line: data.first }))
    }
}
</script>

<style scoped>
.trans {
    float: right;
}

.trans > a {
    color: #0000ee;
    text-decoration: underline;
}

.el_class {
    background-image: url('@/assets/precision/class.gif');
}

.el_file {
    background-image: url('@/assets/precision/source.gif');
}

.bg-img {
    padding-left: 18px;
    background-position: left center;
    background-repeat: no-repeat;
}
</style>
