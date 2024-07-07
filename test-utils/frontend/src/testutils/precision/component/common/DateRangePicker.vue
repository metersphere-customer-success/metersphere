<template>
    <div>
        <el-date-picker v-model="range" type="daterange" align="right" unlink-panels range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" :picker-options="pickerOptions" value-format="yyyy-MM-dd" />
    </div>
</template>

<script setup>
const props = defineProps(['value'])
const emit = defineEmits(['input'])

const { range, pickerOptions } = setup(props, emit)
</script>

<script>
import { computed } from 'vue';

function setup(props, emit) {
    const range = computed({
        get() {
            return props.value
        },
        set(val) {
            emit('input', val)
        }
    })

    const pickerOptions = usePickerOptions()

    return { range, pickerOptions }
}

function usePickerOptions() {
    function _range(picker, days) {
        const start = new Date()
        const end = new Date()
        start.setTime(start.getTime() - 3600 * 1000 * 24 * days)
        picker.$emit('pick', [start, end])
    }

    const options = {
        shortcuts: [
            {
                text: '最近一周',
                onClick(picker) {
                    _range(picker, 7)
                }
            },
            {
                text: '最近两周',
                onClick(picker) {
                    _range(picker, 14)
                }
            },
            {
                text: '最近一月',
                onClick(picker) {
                    _range(picker, 30)
                }
            },
            {
                text: '最近三月',
                onClick(picker) {
                    _range(picker, 90)
                }
            },
        ]
    }

    return options
}
</script>

<style scoped></style>
