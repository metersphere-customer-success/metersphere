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
            :placeholder="$t('api_test.value')"
            style="width: 100%"
            v-model="textValue"
            value-key="name"
            :fetch-suggestions="funcSearch"
            highlight-first-item
          >
            <i
              slot="suffix"
              class="el-input__icon el-icon-edit pointer"
              @click="advanced(textValue)"
            ></i>
          </el-input>
        </el-col>
      </el-form-item>

      <div class="option-tip">
        说明：字符串类型或数值的变量，引用的方式为${变量名}。
      </div>
    </el-form>
  </div>
</template>
<script>
import { JMETER_FUNC, MOCKJS_FUNC } from "@/api/constants";
export default {
  name: "MsEditString",
  props: {
    editData: {},
  },
  data() {
    return {
      textValue: "",
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
  watch: {
    textValue: {
      deep: true,
      handler(nV, oV) {
        this.$emit("update:editData:value", nV);
      },
    },
    "editData.value": {
      deep: true,
      handler(nV, oV) {
        this.textValue = nV
      },
    },
  },
  computed: {
    disabled() {
      return !(this.editData.name && this.editData.name !== "");
    },
  },
  mounted() {
    this.textValue = this.editData.value;
    this.$EventBus.$on("handleStringAdvance", this.handleStringAdvance);
  },
  beforeDestroy() {
    this.$EventBus.$off("handleStringAdvance");
  },
  methods: {
    handleStringAdvance(mock) {
      this.$EventBus.$emit("switchVariableTab", "variable");
      this.textValue = mock;
    },
    advanced(item) {
      if (this.disabled) {
        return;
      }
      this.$EventBus.$emit("switchVariableTab", "mock");
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
</style>
