<template>
    <el-card>
        <div slot="header">
            <span class="step-title">{{ title }}</span>
        </div>
        <div class="step-content">
            <el-alert v-for="(note, idx) in notes" :type="note.type" :title="note.msg" :key="idx" :closable="false"
                :class="['step-note', note.stype]">
                <div v-if="note.hasDetail">
                    <div v-for="(line, didx) in note.details" :key="didx" class="detail-line">{{ line }}</div>
                </div>
            </el-alert>
        </div>
    </el-card>
</template>

<script setup>
const props = defineProps({
    step: {
        type: Object,
        default: () => ({}),
    }
})

const { title, notes } = setup(props)
</script>

<script>
import { computed } from 'vue'

import { Utils } from '@/common'

const NTYPE = {
    NONE: -1,
    DONE: 0,
    INFO: 1,
    WARN: 2,
    ERROR: 3,
};

function setup(props) {
    const title = computed(() => props.step.title)

    const notes = computed(() => {
        return (props.step.notes ?? []).map(({ status, msg, detail }) => {
            return {
                type: getStatusType(status),
                stype: getStatusSType(status),
                msg,
                hasDetail: !Utils.nil(detail),
                details: Utils.nil(detail) ? [] : detail.split("\n"),
            }
        })
    })

    return { title, notes }
}

function getStatusType(status) {
    switch (status) {
        case NTYPE.DONE:
            return 'success'
        case NTYPE.WARN:
            return 'warning'
        case NTYPE.ERROR:
            return 'error'
        default:
            return 'info'
    }
}

function getStatusSType(status) {
    switch (status) {
        case NTYPE.NONE:
            return 'step-none'
        case NTYPE.INFO:
            return 'step-info'
        default:
            return ''
    }
}
</script>

<style scoped>
.step-title {
    font-weight: bold;
    font-size: 18px;
}

.step-note {
    margin-bottom: 5px;
    border: 1px solid lightblue;
}

.step-info {
    background-color: #FFF !important;
}

.detail-line {
    margin-top: 3px;
    font-size: 14px;
}
</style>

<style>
.step-content span {
    font-size: 16px !important;
}
</style>
