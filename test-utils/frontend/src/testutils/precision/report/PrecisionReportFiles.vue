<template>
  <precision-report-package :level="LEVEL.FILES" :tid="tid" :mod="mod" :pkg="pkg" />
</template>

<script setup>
const route = useRoute()
const { tid, mod, pkg } = setup(route)
</script>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router/composables'

import { LEVEL } from '../stores/report'

import PrecisionReportPackage from './PrecisionReportPackage'

function setup(route) {
    const tid = computed(function () {
        return route.params.tid
    })

    const mod = computed(function () {
        return route.params.mod
    })

    const pkg = computed(function () {
        return route.params.pkg
    })

    return { tid, mod, pkg }
}
</script>
