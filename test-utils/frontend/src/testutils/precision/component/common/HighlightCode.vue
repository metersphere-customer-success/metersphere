<template>
  <div>
    <pre class="source">
      <code ref="codeDisp" :name="name" :class="`language-${lang}`"><!--
        -->{{ code }}
      </code>
    </pre>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

import { HighLightJS } from '@/common'

const props = defineProps({
    name: {
        type: String,
        required: true,
    },
    code: {
        type: String,
        required: true,
    },
    lang: {
        type: String,
        default: 'java',
    },
})

const codeDisp = ref(null)

onMounted(function () {
    HighLightJS.hljs.highlightElement(codeDisp.value)
})
</script>
