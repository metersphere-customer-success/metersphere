<template>
  <div class="center">
    <h3>{{ CONSTANS.NOPERMISSION }}</h3>
    <div>{{ props.name }}</div>
    <div>
      <el-input v-model="token" placeholder="输入应用访客口令"
        style="border: 1px solid #DCDFE6; border-radius: 4px; width: 300px; margin-top: 10px" />
      <el-button :disabled="token.length <= 0" @click="submit" type="primary" style="margin-left: 5px">确定</el-button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
    name: {
        type: String,
        default: '',
    }
})

const { token, submit } = setup(props)
</script>

<script>
import { ref } from 'vue'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'
import { useCommonPermission } from '@/store/project'
import { CONSTANS } from '../../stores/report'

function setup(props) {
  const token = ref('')

  const { permit, setShareToken } = useCommonPermission()

  async function submit() {
    const data = await CovserverAPI.covsAppSharePermission(props.name, token.value)
    const { permit: _permit = false, admin = false } = data ?? {}

    Utils.Toast(_permit === true, { pre: '访客访问' })
    permit.value = _permit === true

    if (admin === true) {
      setShareToken(props.name, token.value, true)
    } else if (_permit === true) {
      setShareToken(props.name, token.value, false)
    }
  }

  return { token, submit }
}
</script>

<style scoped>
.center {
    text-align: center;
}
</style>
