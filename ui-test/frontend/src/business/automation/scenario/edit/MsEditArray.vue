<template>
  <div>
    <el-form
      :model="editData"
      label-position="right"
      label-width="80px"
      size="small"
      ref="form"
      :rules="rules"
    >
      <el-form-item :label="$t('api_test.variable_name')" prop="name">
        <el-input
          v-model="editData.name"
          :placeholder="$t('api_test.variable_name')"
          ref="nameInput"
        />
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description">
        <el-input
          class="ms-http-textarea"
          v-model="editData.description"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 10 }"
          :rows="2"
          size="small"
          :disabled="disabled"
        />
      </el-form-item>

      <el-form-item :label="$t('api_test.value')" prop="value">
        <el-col class="item">
          <el-input
            :disabled="disabled"
            size="small"
            :placeholder="$t('数组数据[]用,分割')"
            style="width: 100%"
            v-model="editData.value"
            value-key="name"
            :fetch-suggestions="funcSearch"
            highlight-first-item
          >
          </el-input>
        </el-col>
      </el-form-item>
      <div class="option-tip">
        说明：例如list=[1,2,"a",[1,2,3]]，引用的方式为${list}, ${list[0]}, ${list[3][0]}。
      </div>
    </el-form>
  </div>
</template>
<script>
import MsApiVariableAdvance from "@/business/automation/scenario/component/ApiVariableAdvance";
import { JMETER_FUNC, MOCKJS_FUNC } from "@/api/constants";
export default {
  name: "MsEditArray",
  components: { MsApiVariableAdvance },
  props: {
    editData: {},
  },
  data() {
    return {
      currentItem: null,
      rules: {
        name: [
          {
            required: true,
            message: this.$t("test_track.case.input_name"),
            trigger: "blur",
          },
        ],
      },
    };
  },
  computed: {
    disabled() {
      return !(this.editData.name && this.editData.name !== "");
    },
  },
  methods: {
    createFilter(queryString) {
      return (variable) => {
        return (
          variable.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
        );
      };
    },
    funcFilter(queryString) {
      return (func) => {
        return func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1;
      };
    },
    reload() {
      this.isActive = false;
      this.$nextTick(() => {
        this.isActive = true;
      });
    },
    funcSearch(queryString, cb) {
      let func = MOCKJS_FUNC.concat(JMETER_FUNC);
      let results = queryString
        ? func.filter(this.funcFilter(queryString))
        : func;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
  },
  created() {
    this.$nextTick(() => {
      this.$refs.nameInput.focus();
    });
  },
};
</script>
<style scope>
.option-tip {
  padding: 0 0 20px 20px;
  color: #909399;
}
</style>
