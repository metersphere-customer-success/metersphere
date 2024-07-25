<template>
  <div>
    <el-table
      :data="outputParamData"
      class="param-data-table"
      :row-class-name="tableRowClassName"
      height="400"
      @cell-mouse-enter="handCellMouseEnter"
      @cell-mouse-leave="handCellMouseLeave"
      ref="variableTable"
    >
      <el-table-column prop="name" :label="$t('api_test.variable_name')">
      </el-table-column>
      <el-table-column prop="type" :label="$t('test_track.case.type')">
        <template slot-scope="scope">
          <div>{{ convertTypeName(scope.row) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="step" :label="$t('ui.var_step')">
        <template slot-scope="scope">
          <div style="display: flex; justify-content: space-between">
            <div>{{ scope.row.step }}-{{ scope.row.stepName }}</div>
            <el-button
              v-if="scope.row.showDeleteBtn && scope.row.name"
              icon="el-icon-delete"
              type="danger"
              circle
              size="mini"
              style="margin-left: 10px; float: right"
              @click="removeOutputParam(scope.row)"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="result" :label="$t('ui.result')">
      </el-table-column>
      <el-table-column prop="remark" :label="$t('commons.description')">
        <template slot-scope="scope">
          <el-tooltip>
            <template slot="content">
              <div>
                {{scope.row.remark || $t('commons.description')}}
              </div>
            </template>
            <el-input
              size="mini"
              v-model="scope.row.remark"
              @change="changeParam"
              :disable="!parentREFNode && !editable"
            >
            </el-input>
         </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
export default {
  name: "OutputParamComponent",
  props: {
    currentCommand: {},
  },
  data() {
    return { outputParamData: [] };
  },
  watch: {
    "currentCommand.outputVariables": {
      deep: true,
      immediate: true,
      handler(v) {
        if (v) {
          let isCopyOrRef = false;
          if (this.currentCommand.referenced && (this.currentCommand.referenced === "REF" || this.currentCommand.referenced === "Copy")) {
            isCopyOrRef = true;
          }
          // 这里应该是深拷贝，防止监听死循环
          let strToJSON = JSON.parse(JSON.stringify(v));
          if (Array.isArray(strToJSON)) {
            if (isCopyOrRef) {
              const dataIndex = strToJSON.findIndex((d) => d.status === 3 );
              if (dataIndex !== -1) {
                strToJSON.splice(dataIndex, 1);
              }
            }
            this.outputParamData = strToJSON;
          } else {
            let item = this.strToJSON(strToJSON);
            if (isCopyOrRef) {
              const dataIndex = item.findIndex((d) => d.status === 3 );
              if (dataIndex !== -1) {
                item.splice(dataIndex, 1);
              }
            }
            this.outputParamData = item;
          }

          this.$nextTick(() => {
            if (this.$refs.variableTable) {
              this.$refs.variableTable.doLayout();
            }
          });
        }
      },
    },
  },
  computed: {
    editable() {
      return !(this.currentCommand.referenced === "REF");
    },
    parentREFNode() {
      return this.currentCommand.pREF;
    },
  },
  created() {},
  methods: {
    convertTypeName(row) {
      if (row.type === "async" || row.type === "sync") {
        return row.position === "PRE" ? "前置脚本" : "后置脚本";
      }
      return row.type;
    },
    removeOutputParam(row) {
      const dataIndex = this.outputParamData.findIndex((d) => d.id === row.id);
      this.outputParamData.splice(dataIndex, 1);
      this.changeParam();
    },
    handCellMouseEnter(row, col, cell) {
      if (row.status && row.status === 3) {
        this.$set(row, "showDeleteBtn", true);
      }
    },
    handCellMouseLeave(row, col, cell) {
      this.$set(row, "showDeleteBtn", false);
    },
    changeOutputparamEvent(result) {},
    tableRowClassName({row, rowIndex }) {
      if (row.status === 1) {
        return "create-row";
      } else if (row.status === 3) {
        return "delete-row";
      }
      return "";
    },
    isJSON(str) {
      try {
        JSON.parse(str);
        return true;
      } catch (e) {
        return false;
      }
    },
    strToJSON(str) {
      if (Array.isArray(str)) {
        return str;
      }
      if (!str || !this.isJSON(str)) {
        return [];
      }
      let result = JSON.parse(str);
      //let result = JSON.parse(JSON.stringify(str));
      return result;
    },
    changeParam() {
      this.$emit("changeOutputParam", JSON.stringify(this.outputParamData));
    },
  },
};
</script>
<style scoped>
.el-table >>> .delete-row {
  background: #fbe6e9 !important;
}

.el-table >>> .create-row {
  background: #f0f9ec !important;
}
</style>
