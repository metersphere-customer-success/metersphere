<template>
  <div>
    <el-form label-width="80px">
      <el-form-item label="邮箱">{{ email }}</el-form-item>
      <el-form-item label="工号">{{ uid }}</el-form-item>
      <el-form-item label="角色">{{ role }}</el-form-item>
    </el-form>
  </div>
</template>

<script setup>
const { email, uid, role } = setup()
</script>

<script>
import { computed } from 'vue'
import { useUserConfig } from '@/store/project'

function setup() {
  const { user } = useUserConfig()

  const email = computed(() => user.value.email)
  const uid = computed(() => user.value.uid)
  const role = computed(() => user.value.roleLabel)

  return { email, uid, role }
}
</script>
