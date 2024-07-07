<template>
  <div>
    <el-card>
      <div slot="header" style="padding-bottom: 8px">
        <span style="font-size: 14px"><b>{{ app.name }}</b></span>
        <span> ({{ APP_TYPES[app.type].cn }})</span>
        <span v-if="app.status !== 0" style="color: red"> {{ APP_STATUS[app.status] }}</span>
        <el-popconfirm v-if="unregEnable" title="确定取消申请?" @confirm="unregister">
          <el-button slot="reference" size="small" type="primary" style="float: right">取消申请</el-button>
        </el-popconfirm>
        <span v-if="canModify" style="margin-left: 10px;">
          <span v-if="modifying">
            <el-button-group>
              <el-button @click="modifyConfirm" size="small">确定</el-button>
              <el-button @click="modifyCancel" size="small">取消</el-button>
            </el-button-group>
          </span>
          <span v-else>
            <el-button @click="modify" size="small">修改</el-button>
          </span>
        </span>
      </div>
      <div>
        <el-form label-width="120px">
          <el-form-item label="git地址">
            <el-input v-if="modifying" v-model="model.git" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-else>{{ app.git_url }}</span>
          </el-form-item>
          <el-form-item label="src路径">
            <el-input v-if="modifying" v-model="model.src" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-else>{{ app.src }}</span>
          </el-form-item>
          <el-form-item v-if="app.type === 0" label="迭代初始分支">
            <el-input v-if="modifying" v-model="model.branch" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-else>{{ app.ver_init_branch }}</span>
          </el-form-item>
          <el-form-item label="排除项">
            <el-input v-if="modifying" v-model="model.excludes" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-else>{{ app.excludes }}</span>
          </el-form-item>
          <el-form-item label="包含项">
            <el-input v-if="modifying" v-model="model.includes" style="border: 1px solid #DCDFE6; border-radius: 4px" />
            <span v-else>{{ app.includes }}</span>
          </el-form-item>
          <el-form-item v-if="app.type === 0 && app.status === 0" label="应用口令">
            <span style="display: inline-block; width: 250px">{{ tokenVisible ? app.token : '******' }}</span>
            <el-button size="small" @click="tokenVisible = !tokenVisible">
              {{ tokenVisible ? '隐藏' : '显示' }}
            </el-button>
          </el-form-item>
          <el-form-item v-if="app.type === 0 && app.status === 0" label="访客口令">
            <span style="display: inline-block; width: 250px">{{ gtokenNote }}</span>
            <el-button-group>
              <el-button :disabled="gtokenShowDisable" size="small" @click="gtokenVisible = !gtokenVisible">
                {{ gtokenVisible ? '隐藏' : '显示' }}
              </el-button>
              <el-button size="small" @click="genGToken">生成</el-button>
            </el-button-group>
          </el-form-item>
          <el-form-item label="创建时间">{{ app.create_time }}</el-form-item>
          <el-form-item label="更新时间">{{ app.update_time }}</el-form-item>
          <el-form-item label="申请人">{{ app.applicant }}</el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
const props = defineProps({
  app: {
    type: Object,
    default: () => ({}),
  }
})

const {
  app,
  tokenVisible,
  gtokenVisible,
  gtokenShowDisable,
  gtokenNote,
  genGToken,
  unregEnable,
  unregister,
  model,
  canModify,
  modifying,
  modify,
  modifyConfirm,
  modifyCancel
} = setup(props)
</script>

<script>
import { computed, reactive, ref } from 'vue'

import { Utils } from '@/common'
import { CovserverAPI } from '@/api'

import { APP_STATUS, APP_TYPES, useUserConfig } from '@/store/project'

function setup(props) {
  const { tokenVisible } = useToken()

  const { gtokenVisible, gtokenShowDisable, gtokenNote, genGToken } = useGuestToken(props.app)

  const { unregEnable, unregister } = useUnregiser(props.app)

  const { model, canModify, modifying, modify, modifyConfirm, modifyCancel } = useModify(props.app)

  return {
    app: props.app,
    tokenVisible,
    gtokenVisible,
    gtokenShowDisable,
    gtokenNote,
    genGToken,
    unregEnable,
    unregister,
    model,
    canModify,
    modifying,
    modify,
    modifyConfirm,
    modifyCancel
  }
}

function useUnregiser(app) {
  const { user } = useUserConfig()

  const unregEnable = computed(() => app.status === 1 && user.value.email === app.applicant)

  async function unregister() {
    const data = await CovserverAPI.covsAppUnregister(app.name)
    Utils.Toast(data ?? false, { pre: '取消申请'})
  }

  return { unregEnable, unregister }
}

function useToken() {
  const tokenVisible = ref(false)

  return { tokenVisible }
}

function useGuestToken(app) {
  const gtokenVisible = ref(false)
  const gtokenShowDisable = ref(Utils.nil(app.gtoken))

  function getGTokenNote(visible, token, time) {
    return Utils.nil(token) ? '无' : `${visible ? token : '********'} (${time} 当天有效)`
  }

  async function genGToken() {
    const data = await CovserverAPI.covsPhecdaUserConfig('gen_gtoken', { id: app.id })

    app.gtoken = data.gtoken
    app.gtoken_time = data.gtoken_time
    gtokenVisible.value = true
    gtokenShowDisable.value = false
  }

  const gtokenNote = computed(() => {
    return getGTokenNote(gtokenVisible.value, app.gtoken, app.gtoken_time)
  })

  return { gtokenVisible, gtokenShowDisable, gtokenNote, genGToken }
}

function useModify(app) {
  const model = reactive({ git: '', src: '', branch: '', excludes: '', includes: '' })

  const canModify = ref(true)

  const modifying = ref(false)

  function modify() {
    model.git = app.git_url
    model.src = app.src
    model.branch = app.ver_init_branch
    model.excludes = app.excludes
    model.includes = app.includes

    modifying.value = true
  }

  async function modifyConfirm() {
    const data = await CovserverAPI.covsAppModify(app.name, model)
    Utils.Toast(data === true, { pre: '修改应用' })

    modifying.value = false
  }

  function modifyCancel() {
    modifying.value = false
  }

  return { model, canModify, modifying, modify, modifyConfirm, modifyCancel }
}
</script>
