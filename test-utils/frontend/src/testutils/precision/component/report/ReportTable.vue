<template>
  <div>
    <div class="title">
      <h3>{{ title }}</h3>
      <el-switch v-if="hasEmpty" v-model="compact" active-text="精简" inactive-text="完整"></el-switch>
    </div>
    <el-table :data="data" border stripe show-summary :summary-method="summary"
      :default-sort="{ prop: 'line', order: 'ascending' }">
      <el-table-column prop="element" :label="elName" width="500px" sortable>
        <template slot-scope="scope">
          <a :class="['bg-img', elIcon]" :href="scope.row.url">{{ scope.row.element }}</a>
        </template>
      </el-table-column>
      <el-table-column v-for="index in indices" :key="index" :prop="index" :label="`${COV_INDEX[index]}覆盖`"
        :formatter="formatter" sortable :sort-method="(v1, v2) => sort(index, v1, v2)" />
    </el-table>
  </div>
</template>

<script setup>
const props = defineProps({
  type: {
    type: String,
    default: 'full',
  },
  level: {
    type: String,
    required: true,
  },
  data: {
    type: Array,
    default: function () { return [] },
  }
})

const title = `${props.type === 'delta' ? '增' : '全'}量覆盖报告`
const elLevel = EL_LEVEL[props.level]
const elName = EL_NAME[elLevel]
const elIcon = EL_ICON[elLevel]

const { compact, data, hasEmpty, indices } = setup(props)
</script>

<script>
import { computed, ref } from 'vue'

import { Utils } from '@/common'
import { LEVEL, EL_LEVEL, EL_NAME, EL_ICON, COV_INDEX } from '../../stores/report'

function setup(props) {
  const compact = ref(true)

  const data = computed(function () {
    return transfer(props.type, props.data, compact.value)
  })

  const hasEmpty = computed(function () {
    return props.data.some(function ({ info }) {
      return isEmpty(info[props.type])
    })
  })

  const indices = computed(function () {
    const indices = ['branch', 'line', 'method']
    if (props.level !== LEVEL.CLASS) {
      indices.push('class')
    }
    return indices
  })

  return { compact, data, hasEmpty, indices }
}

function summary({ data }) {
  function sum(key) {
    return coverage(data.reduce(function ({ c: pc, t: pt }, curr) {
      const { c: cc = 0, t: ct = 0 } = curr[key] ?? {}
      return { c: pc + cc, t: pt + ct }
    }, { c: 0, t: 0 }))
  }

  const sums = ['合计'];
  ['branch', 'line', 'method'].forEach(function (key) {
    sums.push(sum(key))
  })

  if (!Utils.nil((data[0] ?? {})['class'])) {
    sums.push(sum('class'))
  }

  return sums
}

function sort(key, v1, v2) {
  function compare(v1, v2) {
    const { c: c1 = 0, t: t1 = 0 } = v1
    const { c: c2 = 0, t: t2 = 0 } = v2
    return t1 === 0 && t2 === 0 ? 0 :
      t1 === 0 ? -1 :
        t2 === 0 ? 1 : c1 / t1 - c2 / t2
  }

  return compare(v1[key], v2[key])
}

function formatter(row, { property }) {
  return coverage(row[property])
}

function transfer(type, data, compact) {
  const ndata = compact ? data.filter(function ({ info }) {
    return !isEmpty(info[type])
  }) : data

  return ndata.map(function ({ element, url, info }) {
    const { branch, line, method, 'class': clazz } = info[type] ?? {}
    return {
      element,
      url,
      branch,
      line,
      method,
      'class': clazz,
    }
  })
}

function isEmpty(info = {}) {
  const t = Utils.unchain(info, ['inst', 't'], 0)
  return t <= 0
}

function coverage({ c, t = 0 } = {}) {
  if (t === 0) {
    return '-'
  } else {
    const ratio = Math.floor(c * 10000 / t) / 100
    return `${ratio}% (${c}/${t})`
  }
}
</script>

<style scoped>
.title {
  margin-bottom: 0.5em;
}

.title h3 {
  display: inline;
  margin-right: 1em;
}

a {
  color: #0000ee;
  text-decoration: underline;
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

.el_method {
  background-image: url('@/assets/precision/method.gif');
}

.bg-img {
  padding-left: 18px;
  background-position: left center;
  background-repeat: no-repeat;
}
</style>
