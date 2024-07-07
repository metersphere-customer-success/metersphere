<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="(layer, idx) in layers" :key="idx" class="breadcrumb">
      <span v-if="Utils.nil(layer.url)" :class="['bg-img', layer.icon]">{{ layer.label }}</span>
      <a v-else :href="layer.url"><span :class="['bg-img', layer.icon]">{{ layer.label }}</span></a>
    </el-breadcrumb-item>
  </el-breadcrumb>
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
        default: function () { return [] },
    },
})

const layers = setup(props)
</script>

<script>
import { computed } from 'vue'

import { Utils } from '@/common'
import { EL_ICON, EL_NAME, getUrl, LEVEL } from '../../stores/report'

function setup(props) {
    const layers = computed(function () {
        return transfer(props.level, props.tid, props.info)
    })

    return layers
}

function transfer(level, tid, info) {
    if (Utils.nil(level) || Utils.nil(tid)) {
        return []
    }

    const { mod, pkg, src, class: clazz } = info

    const layers = []
    switch (level) {
        case LEVEL.APP:
            layers.push({ label: EL_NAME[LEVEL.APP], icon: EL_ICON[level] })
            break

        case LEVEL.MODULE:
            layers.push({ label: EL_NAME[LEVEL.APP], url: getUrl(LEVEL.APP, tid), icon: EL_ICON[LEVEL.APP] })
            layers.push({ label: mod, url: getUrl(LEVEL.MODULE, tid, { mod }), icon: EL_ICON[LEVEL.MODULE] })
            break

        case LEVEL.CLASSES:
        case LEVEL.FILES:
            layers.push({ label: EL_NAME[LEVEL.APP], url: getUrl(LEVEL.APP, tid), icon: EL_ICON[LEVEL.APP] })
            layers.push({ label: mod, url: getUrl(LEVEL.MODULE, tid, { mod }), icon: EL_ICON[LEVEL.MODULE] })
            layers.push({ label: pkg, icon: EL_ICON[LEVEL.PACKAGE] })
            break

        case LEVEL.CLASS:
        case LEVEL.FILE:
            layers.push({ label: EL_NAME[LEVEL.APP], url: getUrl(LEVEL.APP, tid), icon: EL_ICON[LEVEL.APP] })
            layers.push({ label: mod, url: getUrl(LEVEL.MODULE, tid, { mod }), icon: EL_ICON[LEVEL.MODULE] })
            layers.push({ label: pkg, url: getUrl(LEVEL.CLASSES, tid, { mod, pkg }), icon: EL_ICON[LEVEL.PACKAGE] })
            layers.push({ label: level === LEVEL.CLASS ? clazz : src, icon: EL_ICON[level] })
            break
    }

    return layers
}
</script>

<style scoped>
.breadcrumb > a {
    font-weight: bold !important;
    cursor: pointer !important;
}

.el_report {
    background-image: url('@/assets/precision/report.gif');
}

.el_package {
    background-image: url('@/assets/precision/package.gif');
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
