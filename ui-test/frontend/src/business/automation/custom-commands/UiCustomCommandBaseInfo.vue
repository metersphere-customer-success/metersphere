<template>
  <el-form
    label-position="right"
    label-width="80px"
    size="small"
    style="margin-right: 20px"
    :rules="rules"
    :model="value"
    ref="baseInfoForm">

    <el-form-item
      prop="name"
      :label="$t('commons.name')">
      <el-input
        class="ms-scenario-input"
        size="small"
        maxlength="100"
        show-word-limit
        v-model="value.name"/>
    </el-form-item>

    <el-form-item
      prop="moduleId"
      :label="$t('test_track.module.module')">
      <select-tree
        clearable
        checkStrictly
        size="small"
        :data="moduleOptions"
        :defaultKey="value.moduleId"
        :obj="{id: 'id', label: 'name'}"
        @getValue="setModule"/>
    </el-form-item>

    <!-- <el-form-item
      prop="status"
      :label="$t('commons.status')">
      <el-select
        class="ms-scenario-input"
        size="small"
        v-model="value.status">
        <el-option
          v-for="item in options"
          :key="item.id"
          :label="$t(item.label)"
          :value="item.id"/>
      </el-select>
    </el-form-item> -->

    <el-form-item
      prop="principal"
      :label="$t('api_test.definition.request.responsible')">
      <el-select
        v-model="value.principal"
        filterable
        size="small"
        class="ms-scenario-input"
        :placeholder="$t('api_test.definition.request.responsible')">
        <el-option
          v-for="item in maintainerOptions"
          :key="item.id"
          :label="item.name + ' (' + item.email + ')'"
          :value="item.id">
        </el-option>
      </el-select>
    </el-form-item>

    <!-- <el-form-item
      prop="level"
      :label="$t('test_track.case.priority')">
      <el-select
        class="ms-scenario-input"
        size="small"
        v-model="value.level">
        <el-option
          v-for="item in levels"
          :key="item.id"
          :label="item.label"
          :value="item.id"/>
      </el-select>
    </el-form-item> -->

    <el-form-item
      prop="tags"
      :label="$t('api_test.automation.tag')">
      <ms-input-tag
        :currentScenario="value"
        ref="tag"/>
    </el-form-item>

    <el-form-item
      prop="description"
      :label="$t('commons.description')">
      <el-input class="ms-http-textarea"
                v-model="value.description"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 10}"
                :rows="1" size="small"/>
    </el-form-item>

  </el-form>
</template>

<script>
import {getProjectMemberOption} from "@/network/user";
import {API_STATUS, PRIORITY} from "@/api/JsonData";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import SelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";

export default {
  name: "UiCustomCommandBaseInfo",
  components: {SelectTree, MsInputTag},
  props: {
    value: Object,
    moduleOptions: Array,
  },
  data() {
    return {
      formLabelWidth: "100px",
      readOnly: false,
      result: {},
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        principal: [{required: true, message: this.$t('api_test.definition.request.responsible'), trigger: 'change'}],
        customNum: [
          {required: true, message: "ID必填", trigger: 'blur'},
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
      },
      maintainerOptions: [],
      levels: PRIORITY,
      options: API_STATUS,
    }
  },
  created() {
    this.getMaintainerOptions();
  },
  methods: {
    getMaintainerOptions() {
      getProjectMemberOption().then(data => {
        this.maintainerOptions = data.data;
      });
    },
    setModule(id) {
      this.value.moduleId = id;
    },
    validate() {
      let r = true;
      this.$refs.baseInfoForm.validate((valid) => {
        if (!valid) {
          r = false;
        }
      });
      return r;
    }
  }
}
</script>

<style scoped>

</style>
