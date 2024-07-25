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
        <div class="ms-code">
          <ms-code-edit
            :enable-format="false"
            mode="text"
            :data="jsonData"
            theme="eclipse"
            :modes="['json']"
            @update:data="updateJsonData"
            ref="codeEdit"
          />
        </div>
      </el-form-item>
      <ms-api-variable-advance
        ref="variableAdvance"
        :current-item="editData"
        @advancedRefresh="reload"
      />
      <div class="option-tip">
        说明：例如json={"a":1,"b":[1,2,3]}，引用方式为${json}，${json.a}，${json.b[0]}。
      </div>
    </el-form>
  </div>
</template>
<script>
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import MsApiVariableAdvance from "@/business/automation/scenario/component/ApiVariableAdvance";
import { JMETER_FUNC, MOCKJS_FUNC } from "@/api/constants";
export default {
  name: "MsEditJson",
  components: { MsApiVariableAdvance, MsCodeEdit },
  props: {
    editData: {},
  },
  data() {
    return {
      jsonData: "",
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
  mounted() {
    this.jsonData = this.editData.value || "";
    this.$refs.codeEdit.formatData = this.jsonData
  },
  watch: {
    editData: {
      deep: true,
      handler(nV, oV) {
        this.jsonData = nV.value || "";
        this.$refs.codeEdit.formatData = this.jsonData
      },
    },
  },
  methods: {
    updateJsonData(data) {
      this.editData.value = data;
    },
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
.ms-code {
  height: 200px;
}
</style>
