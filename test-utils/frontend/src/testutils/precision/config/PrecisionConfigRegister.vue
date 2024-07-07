<template>
  <div>
    <div>
      <el-card style="padding-top: 20px">
        <el-form :model="model" :rules="rules" label-width="100px">
          <el-form-item label="天玑项目">
            <el-input v-if="phecdaInput" v-model="model.phecda" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <el-select v-else v-model="model.phecda" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" >
              <el-option v-for="phecda in phecdas" :value="phecda.id" :label="`${phecda.name} (${phecda.id})`" :key="phecda.id" />
            </el-select>
            <el-button v-if="admin" @click="phecdaInput = !phecdaInput" style="margin-left: 5px; background-color: #409EFF; color: #FFFFFF">切换</el-button>
          </el-form-item>
          <el-form-item label="类型">
            <el-radio v-model="model.type" label="app">应用</el-radio>
            <el-radio v-model="model.type" label="mod">子模块</el-radio>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="model.name" @change="rules.name.checker" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-if="!validate.name" class="validate-note warning">{{ rules.name.note }}</span>
          </el-form-item>
          <el-form-item label="代码地址">
            <el-input v-model="model.git" :placeholder="GIT_DEMO"
              @change="rules.git.checker" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-if="!validate.git" class="validate-note warning">{{ rules.git.note }}</span>
          </el-form-item>
          <el-form-item label="SRC路径">
            <el-input v-model="model.src"
              class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
          </el-form-item>
          <el-form-item v-if="model.type === 'app'" label="初始分支">
            <el-input v-model="model.branch" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span class="validate-note">{{ rules.branch.note }}</span>
          </el-form-item>
          <el-form-item label="排除项">
            <el-input v-model="model.excludes" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span class="validate-note">{{ rules.excludes.note }}</span>
          </el-form-item>
          <el-form-item label="包含项">
            <el-input v-model="model.includes" class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span class="validate-note">{{ rules.includes.note }}</span>
          </el-form-item>
          <el-form-item label="申请人">
            <el-input v-model="model.applicant" @change="rules.applicant.checker"
              class="form-item" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-if="!validate.applicant" class="validate-note warning">{{ rules.applicant.note }}</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :disabled="!regEnable" @click="register">申请接入</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  phecda: {
    type: String,
    default: null,
  }
})

const { admin, phecdaInput, model, phecdas, rules, validate, regEnable, register } = setup(props)
</script>

<script>
import { computed, reactive, ref, watch } from 'vue'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'
import { ROLES, getUserEmail, useUserConfig } from '@/store/project'

const TYPES = { 'app': 0, 'mod': 1 }

const GIT_HOST = 'https://gitlab.cicconline.com/'
const GIT_DEMO = `${GIT_HOST}qaplat/alioth_server.git`

function setup(props) {
  const { user } = useUserConfig()
  const admin = computed(() => user.value.role === ROLES.SYS_ADMIN)

  const phecdaInput = ref(false)

  const model = reactive({
    phecda: '',
    type: 'app',
    name: '',
    git: '',
    src: 'src/main/java',
    branch: 'master',
    excludes: '',
    includes: '',
    applicant: getUserEmail()
  })

  const { phecdas } = usePhecdaProjects(props, model)

  const { rules, validate } = useModelValidate(model)

  const { regEnable, register } = useRegister(model, validate)

  return { admin, phecdaInput, model, phecdas, rules, validate, regEnable, register }
}

function usePhecdaProjects(props, model) {
  const { phecdas: plist } = useUserConfig()

  watch(plist, (list = []) => {
    model.phecda = `${(list[0] ?? {}).yxProjectId ?? ''}`
  }, { immediate: true })

  const phecdas = computed(() => {
    return plist.value.map(({ name, yxProjectId }) => ({ name, id: `${yxProjectId}` }))
  })

  watch(() => props.phecda, (val) => {
    if (!Utils.nil(val)) {
      model.phecda = val
    }
  }, { immediate: true })

  return { phecdas }
}

function useRegister(model, validate) {
  const regEnable = computed(() => {
    return Object.keys(validate).every((key) => validate[key])
  })

  async function register() {
    const data = {
      phecda: model.phecda.trim(),
      type: TYPES[model.type],
      name: model.name.trim(),
      git: model.git.trim(),
      src: model.src.trim(),
      branch: model.branch.trim(),
      excludes: model.excludes.trim(),
      includes: model.includes.trim(),
      applicant: model.applicant.trim(),
    }

    const resp = await CovserverAPI.covsAppRegister(data)
    Utils.Toast(resp ?? false, { pre: '申请接入' })
  }

  return { regEnable, register }
}

function useModelValidate(model) {
  const CHECKS = {
    Name: (name) => !Utils.nil(name) && name.length > 0,
    Git: (url) => !Utils.nil(url) && url.startsWith(GIT_HOST) && url.endsWith('.git'),
    Applicant: (email) => !Utils.nil(email) && email.endsWith('@cicc.com.cn'),
  }

  const validate = reactive({
    name: CHECKS.Name(model.name),
    git: CHECKS.Git(model.git),
    applicant: CHECKS.Applicant(model.applicant),
  })

  const rules = { 
    name: {
      note: '请输入应用名称, 与天玑项目的应用名称保持一致',
      checker: () => {
        validate.name = CHECKS.Name(model.name)
      },
    },
    git: {
      note: '请输入代码克隆 HTTPS 地址, 以 .git 结尾',
      checker: () => {
        validate.git = CHECKS.Git(model.git)
      },
    },
    branch: {
      note: '代码进行版本迭代时拉取代码的分支',
    },
    excludes: {
      note: '不进行统计的项目, ANT路径匹配模式, 以分号隔开, 如: **/*.java;test/Test.java',
    },
    includes: {
      note: '必须进行统计的项目, 和排除项协同使用',
    },
    applicant: {
      note: '请输入申请人邮箱',
      checker: () => {
        validate.applicant = CHECKS.Applicant(model.applicant)
      },
    },
  }

  return { rules, validate }
}
</script>

<style scoped>
.form-item {
  width: 800px;
}

.validate-note {
  display: block;
  font-size: 12px;
}

.warning {
  color: red;
}
</style>
