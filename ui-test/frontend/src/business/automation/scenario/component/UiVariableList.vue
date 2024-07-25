<template>
  <el-dialog
    :title="$t('api_test.automation.scenario_total')"
    :close-on-click-modal="false"
    :visible.sync="visible"
    class="visible-dialog"
    width="80%"
    @close="close"
    v-loading="loading"
    append-to-body
  >
    <fieldset :disabled="disabled" class="ms-fieldset">
      <el-collapse-transition>
        <el-tabs v-model="activeName">
          <el-tab-pane
            :label="$t('api_test.scenario.variables')"
            name="variable"
          >
            <div>
              <el-row style="margin-bottom: 10px">
                <div style="float: left">
                  <el-input
                    :placeholder="$t('commons.search_by_name')"
                    v-model="selectVariable"
                    size="small"
                    @change="filter"
                    @keyup.enter="filter"
                  >
                    <el-select
                      v-model="searchType"
                      slot="prepend"
                      :placeholder="$t('test_resource_pool.type')"
                      style="width: 90px"
                      @change="filter"
                    >
                      <el-option
                        value="ALL"
                        :label="$t('api_test.automation.all')"
                      ></el-option>
                      <el-option
                        value="STRING"
                        :label="$t('api_test.automation.string')"
                      ></el-option>
                      <el-option
                        value="ARRAY"
                        :label="$t('api_test.automation.array')"
                      ></el-option>
                      <el-option
                        value="JSON"
                        :label="$t('api_test.automation.json')"
                      ></el-option>
                      <el-option
                        value="NUMBER"
                        :label="$t('api_test.automation.number')"
                      ></el-option>
                    </el-select>
                  </el-input>
                </div>

                <div style="float: right">
                  <el-select
                    v-model="selectType"
                    :placeholder="$t('test_resource_pool.type')"
                    style="width: 90px"
                    size="small"
                    @change="addVariable"
                  >
                    <el-option
                      value="STRING"
                      :label="$t('api_test.automation.string')"
                    ></el-option>
                    <el-option
                      value="ARRAY"
                      :label="$t('api_test.automation.array')"
                    ></el-option>
                    <el-option
                      value="JSON"
                      :label="$t('api_test.automation.json')"
                    ></el-option>
                    <el-option
                      value="NUMBER"
                      :label="$t('api_test.automation.number')"
                    ></el-option>
                  </el-select>
                  <!-- 添加 -->
                  <el-button
                    size="small"
                    style="margin-left: 10px"
                    type="primary"
                    @click="addVariable"
                  >
                    {{ $t("commons.add") }}
                  </el-button>
                  <!-- 批量添加 -->
                  <el-link
                    v-show="selectType === 'STRING' || selectType === 'NUMBER'"
                    @click="batchAddParameter"
                    type="primary"
                    :disabled="disabled"
                    style="margin-left: 10px"
                  >
                    {{ $t("commons.batch_add") }}
                  </el-link>
                </div>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <div
                    style="
                      border: 1px #dcdfe6 solid;
                      min-height: 400px;
                      border-radius: 4px;
                      width: 100%;
                    "
                  >
                    <ms-table
                      v-loading="loading"
                      row-key="id"
                      :data="targetVariables"
                      :total="targetVariables.length"
                      :screen-height="screenHeight"
                      :batch-operators="batchButtons"
                      :remember-order="true"
                      :highlightCurrentRow="true"
                      :fields.sync="fields"
                      :field-key="tableHeaderKey"
                      @handleRowClick="handleRowClick"
                      @refresh="refresh"
                      @order="order"
                      ref="variableTable"
                    >
                      <span v-for="item in fields" :key="item.key">
                        <ms-table-column
                          prop="num"
                          :field="item"
                          :fields-width="fieldsWidth"
                          sortable
                          label="ID"
                          min-width="60"
                        >
                        </ms-table-column>
                        <ms-table-column
                          prop="name"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('api_test.variable_name')"
                          min-width="100"
                          sortable
                        >
                        </ms-table-column>
                        <ms-table-column
                          prop="type"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('test_track.case.type')"
                          min-width="70"
                          sortable="custom"
                        >
                          <template v-slot:default="scope">
                            <span>{{ types.get(scope.row.type) }}</span>
                          </template>
                        </ms-table-column>
                        <ms-table-column
                          prop="value"
                          :field="item"
                          :fields-width="fieldsWidth"
                          :label="$t('api_test.value')"
                          sortable
                        >
                        </ms-table-column>
                        <ms-table-column
                            prop="description"
                            :field="item"
                            :fields-width="fieldsWidth"
                            :label="$t('commons.description')"
                            min-width="70"
                            sortable>
                        </ms-table-column>
                      </span>
                    </ms-table>
                    <batch-add-parameter
                      @batchSave="batchSaveParameter"
                      ref="batchAddParameter"
                    />
                  </div>
                </el-col>
                <el-col :span="12">
                  <ms-edit-string
                    v-if="
                      editData.type === 'STRING' || editData.type === 'CONSTANT'
                    "
                    ref="string"
                    :editData.sync="editData"
                    @update:editData:value="(val) => (editData.value = val)"
                  ></ms-edit-string>
                  <ms-edit-array
                    v-if="editData.type === 'ARRAY'"
                    ref="array"
                    :editData.sync="editData"
                  ></ms-edit-array>
                  <ms-edit-json
                    v-if="editData.type === 'JSON'"
                    ref="array"
                    :editData.sync="editData"
                  ></ms-edit-json>
                  <ms-edit-number
                    v-if="editData.type === 'NUMBER'"
                    ref="array"
                    :editData.sync="editData"
                  ></ms-edit-number>
                  <div v-if="editData.type" style="float: right">
                    <el-button
                      size="small"
                      style="margin-left: 10px"
                      type="primary"
                      @click="confirmVariable"
                      >{{ $t("commons.confirm") }}
                    </el-button>
                    <el-button
                      size="small"
                      style="margin-left: 10px"
                      @click="cancelVariable"
                      >{{ $t("commons.cancel") }}
                    </el-button>
                    <el-button
                      v-if="showDelete"
                      size="small"
                      style="margin-left: 10px"
                      @click="deleteVariable"
                      >{{ $t("commons.delete") }}
                    </el-button>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('commons.mock')" name="mock" v-if="selectType==='STRING'">
            <ms-ui-advance ref="variableAdvance" :current-item="editData" />
          </el-tab-pane>
        </el-tabs>
        <template v-slot:footer>
          <div>
            <el-button type="primary" @click="save"
              >{{ $t("commons.confirm") }}
            </el-button>
          </div>
        </template>
      </el-collapse-transition>
    </fieldset>
  </el-dialog>
</template>

<script>
import MsUiAdvance from "@/business/automation/scenario/edit/UiAdvance";
import MsEditString from "@/business/automation/scenario/edit/MsEditString";
import MsEditArray from "@/business/automation/scenario/edit/MsEditArray";
import MsEditJson from "@/business/automation/scenario/edit/MsEditJson";
import MsEditNumber from "@/business/automation/scenario/edit/MsEditNumber";
import MsEditConstant from "@/business/automation/scenario/variable/EditConstant";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsEditCounter from "@/business/automation/scenario/variable/EditCounter";
import MsEditRandom from "@/business/automation/scenario/variable/EditRandom";
import MsEditListValue from "@/business/automation/scenario/variable/EditListValue";
import MsEditCsv from "@/business/automation/scenario/variable/EditCsv";
import BatchAddParameter from "@/business/definition/components/basis/BatchAddParameter";
import { KeyValue } from "@/api/ApiTestModel";
import { REQUEST_HEADERS, MOCKJS_FUNC} from "@/api/constants";
import Mock from "mockjs";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {
  getCustomTableWidth,
  getCustomTableHeader,
} from "metersphere-frontend/src/utils/tableUtils";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "UiVariableList",
  components: {
    MsUiAdvance,
    MsEditConstant,
    MsDialogFooter,
    MsTableHeader,
    MsTablePagination,
    MsEditCounter,
    MsEditRandom,
    MsEditListValue,
    MsEditCsv,
    BatchAddParameter,
    MsTableColumn,
    MsTable,
    MsEditString,
    MsEditNumber,
    MsEditJson,
    MsEditArray,
  },
  data() {
    return {
      variables: [],
      targetVariables: [],
      headers: [],
      activeName: "variable",
      searchType: "",
      selectVariable: "",
      condition: {},
      types: new Map([
        ["CONSTANT", this.$t("api_test.automation.string")],
        ["ARRAY", this.$t("api_test.automation.array")],
        ["STRING", this.$t("api_test.automation.string")],
        ["JSON", this.$t("api_test.automation.json")],
        ["NUMBER", this.$t("api_test.automation.number")],
      ]),
      visible: false,
      selection: [],
      loading: false,
      currentPage: 1,
      editData: {},
      pageSize: 10,
      total: 0,
      headerSuggestions: REQUEST_HEADERS,
      disabled: false,
      selectType: "STRING",
      showDelete: false,
      tableHeaderKey: "VARIABLE_LIST_TABLE",
      fields: getCustomTableHeader("VARIABLE_LIST_TABLE"),
      fieldsWidth: getCustomTableWidth("TRACK_REPORT_TABLE"),
      screenHeight: "400px",
      batchButtons: [
        {
          name: this.$t("api_test.definition.request.batch_delete"),
          handleClick: this.handleDeleteBatch,
        },
      ],
      mockFuncs: MOCKJS_FUNC.map(f => {
        return f.name
      }),
    };
  },
  mounted() {
    this.$EventBus.$on("switchVariableTab", this.switchVariableTab);
  },
  beforeDestroy() {
    this.$EventBus.$off("switchVariableTab");
  },
  watch: {
    activeName(nV, oV) {
      if (nV === "variable") {
        this.$nextTick(() => {
          if (this.$refs.variableTable) {
            this.$refs.variableTable.doLayout();
          }
        });
      }
    },
    variables(nV, oV) {
      this.filter();
    },
  },
  methods: {
    order(columnInfo) {
      let { column, prop, order } = columnInfo;
      if (!prop || prop !== "type") {
        return;
      }
      if (!order) {
        this.filter();
        return;
      }
      //处理 升序 0 或 降序 1
      let asc = order === "asc" ? true : false;
      //根据当前国际化的语言进行排序
      let origin = [
        { type: "STRING", label: this.$t("api_test.automation.string") },
        { type: "NUMBER", label: this.$t("api_test.automation.number") },
        { type: "ARRAY", label: this.$t("api_test.automation.array") },
        { type: "JSON", label: this.$t("api_test.automation.json") },
      ];

      origin.sort((o1, o2) => {
        return o1.label.localeCompare(o2.label);
      });

      //给origin分配序列
      if (asc) {
        for (let i = 0; i < origin.length; i++) {
          origin[i].order = i;
        }
      } else {
        origin = origin.reverse();
        for (let i = origin.length - 1; i >= 0; i--) {
          origin[i].order = i;
        }
      }

      let cacheMap = new Map();
      origin.forEach((v) => {
        cacheMap.set(v.type, v.order);
      });
      this.targetVariables.sort((o1, o2) => {
        let originO1 = cacheMap.get(o1.type);
        let originO2 = cacheMap.get(o2.type);
        return originO1 - originO2;
      });
    },
    refresh() {
      if (!this.variables) {
        return;
      }
      this.variables.forEach((v) => {
        if (v.showMore) {
          v.showMore = false;
        }
      });
    },
    switchVariableTab(tabName) {
      this.activeName = tabName;
      this.$forceUpdate();
    },
    batchAddParameter() {
      this.$refs.batchAddParameter.open(null, this.selectType);
    },
    _handleBatchVars(data) {
      if (data) {
        let params = data.split("\n").filter((v) => v.trim() !== "");
        let keyValues = [];
        params.forEach((item) => {
          let line = item.split(/：|:/);
          let required = false;
          keyValues.push(
            new KeyValue({
              name: line[0],
              required: required,
              value: line[1],
              description: line[2],
              type: "text",
              valid: false,
              file: false,
              encode: true,
              enable: true,
              contentType: "text/plain",
            })
          );
        });
        return keyValues;
      }
    },
    batchSaveHeader(data) {
      if (data) {
        let keyValues = this._handleBatchVars(data);
        keyValues.forEach((item) => {
          this.format(this.headers, item);
        });
        this.sortParamters();
      }
    },
    format(array, obj) {
      if (array) {
        let isAdd = true;
        for (let i in array) {
          let item = array[i];
          if (item.name === obj.name) {
            item.value = obj.value;
            isAdd = false;
          }
        }
        if (isAdd) {
          this.headers.unshift(obj);
        }
      }
    },
    batchSaveParameter(data) {
      if (data) {
        let keyValues = this._handleBatchVars(data);
        //批量检测
        let error = false;
        keyValues.forEach((item) => {
          item.type = this.selectType;
          let result = this.checkVariable(item);
          if (!result.code && !error) {
            error = true;
            this.$error(result.msg);
            return;
          }
        });
        if (error) {
          return;
        }

        keyValues.forEach((item) => {
          item.type = this.selectType;
          //参数值校验
          let result = this.checkVariable(item);
          if (!result.code) {
            this.$error(result.msg);
            return;
          }
          if (item.type === "NUMBER") {
            item.value = !item.value ? "" : item.value;
          }
          this.addParameters(item);
        });
        if (this.variables && this.variables.length > 0) {
          this.variables = this.variables.reverse();
        }
        this.sortParamters();
        this.saveData();
      }
    },
    handleClick(command) {
      this.editData = { delimiter: ",", quotedData: "false" };
      this.editData.type = command;
      this.addParameters(this.editData);
    },

    addParameters(v) {
      v.id = getUUID();
      if (v.type === "CSV") {
        v.delimiter = ",";
        v.quotedData = false;
      }
      //检测是否已经存在同名变量
      let newArr = this.variables.filter((o) => {
        return o.name !== v.name;
      });
      newArr.push(v);
      this.variables = newArr;
      let index = 1;
      this.variables.forEach((item) => {
        item.num = index;
        index++;
      });
    },
    sortParamters() {
      let index = 1;
      this.variables.forEach((item) => {
        item.num = index;
        index++;
      });
    },
    updateParameters(v) {
      this.editData = JSON.parse(JSON.stringify(v));
      this.updateFiles();
      let datas = [];
      this.variables.forEach((item) => {
        if (item.id === v.id) {
          item = v;
        }
        datas.push(item);
      });
      this.variables = datas;
    },
    select(selection) {
      this.selection = selection.map((s) => s.id);
    },
    paramConvertProcessor(variables){
      if(!variables){
        return;
      }
      for(let i = 0; i < variables.length; i++){
        if(variables[i].type === "STRING" || variables[i].type === "CONSTANT"){
          //去除转义字符
          try{
            variables[i].value = variables[i].value.replace(/(\\)+\\"/g, "\"");
            variables[i].value = variables[i].value.replace(/\\\"/g, "\"");
          }catch(e){console.log("string type convert err!");}
        }
      }
    },
    open: function (variables, headers, disabled) {
      if (variables) {
        this.paramConvertProcessor(variables);
        this.variables = variables;
        this.targetVariables = variables;
      }
      if (headers) {
        this.headers = headers;
      }
      this.visible = true;
      this.disabled = disabled;
    },
    save() {
      this.visible = false;
    },
    close() {
      this.editData = {};
      this.visible = false;
      // this.saveData();
    },
    saveData() {
      let saveVariables = [];
      this.variables.forEach((item) => {
        item.hidden = undefined;
        if (item.name && item.name !== "") {
          saveVariables.push(item);
        }
      });
      this.selectVariable = "";
      // this.searchType = "";
      this.$EventBus.$emit("saveVariables",saveVariables, this.headers);
      this.$emit("setVariables", saveVariables, this.headers);
    },
    addVariable() {
      this.editData = { delimiter: ",", quotedData: "false", files: [] };
      this.editData.type = this.selectType;
      this.showDelete = false;
      this.$refs.variableTable.cancelCurrentRow();
    },
    confirmVariable() {
      if (
        this.editData &&
        (this.editData.name === undefined || this.editData.name.trim() === "")
      ) {
        this.$warning("变量名不能为空");
        return;
      }
      //参数值校验
      let result = this.checkVariable(this.editData);
      if (!result.code) {
        this.$error(result.msg);
        return;
      }
      if (this.editData.type === "NUMBER") {
        this.editData.value = !this.editData.value
          ? ""
          : this.editData.value;
      }
      // 更新场景，修改左边数据
      if (this.showDelete) {
        this.updateParameters(this.editData);
      } else {
        // 新增场景，往左边新加
        this.addParameters(this.editData);
        this.addVariable();
        this.$refs.variableTable.cancelCurrentRow();
      }
      this.saveData();
      // this.$success(this.$t("commons.save_success"));
    },
    checkVariable(editData) {
      let r = {
        code: true,
        msg: "校验通过",
      };
      if (!editData.value) {
        return r;
      }

      let temp = editData.value;
      try {
        //此处 需要处理 json中 含有mock数据的转换
        temp = this.refreshTempMockData(temp);
        temp = Mock.mock(temp);
        temp = JSON.stringify(Mock.mock(JSON.parse(temp)));
      } catch (e) {
        //mock finish
      }
      switch (editData.type) {
        case "JSON":
          r.code =
            this.isJSON(temp) && temp.startsWith("{") && temp.endsWith("}");
          r.msg = "JSON 校验失败";
          break;
        case "ARRAY":
          r.code =
            this.isJSON(temp) && temp.startsWith("[") && temp.endsWith("]");
          r.msg = "数组格式 校验失败";
          break;
        case "NUMBER":
          r.code = !isNaN(temp);
          r.msg = "数值类型 校验失败";
          break;
      }
      return r;
    },
    refreshTempMockData(temp){
      if(!temp){
        return temp;
      }
      for(let i = 0; i < this.mockFuncs.length; i++){
        let target = this.mockFuncs[i];
        temp = temp.replace(new RegExp(target, "g"), target);
      }
      return temp;
    },
    isJSON(str) {
      if (typeof str == "string") {
        try {
          let obj = JSON.parse(str);
          if (typeof obj == "object" && obj) {
            return true;
          } else {
            return false;
          }
        } catch (e) {
          return false;
        }
      }
      return false;
    },
    cancelVariable() {
      this.$refs.variableTable.cancelCurrentRow();
      // 清空表单
      this.editData = {};
    },
    deleteVariable() {
      let ids = [this.editData.id];
      if (ids.length === 0) {
        this.$warning("请选择一条数据删除");
        return;
      }
      let message = "";
      ids.forEach((row) => {
        const v = this.variables.find((d) => d.id === row);
        if (v.name) {
          message += v.name + ";";
        }
      });
      if (message !== "") {
        message = message.substr(0, message.length - 1);
        this.$alert("是否确认删除变量：【 " + message + " 】？", "", {
          confirmButtonText: this.$t("commons.confirm"),
          callback: (action) => {
            if (action === "confirm") {
              ids.forEach((row) => {
                const index = this.variables.findIndex((d) => d.id === row);
                this.variables.splice(index, 1);
              });
              this.sortParamters();
              this.editData = {};
              this.saveData();
            }
          },
        });
      } else {
        ids.forEach((row) => {
          const index = this.variables.findIndex((d) => d.id === row);
          this.variables.splice(index, 1);
        });
        this.sortParamters();
        this.editData = {};
      }
    },
    handleDeleteBatch() {
      this.$alert("是否确认删除所选变量" + " " + " ？", "", {
        confirmButtonText: this.$t("commons.confirm"),
        callback: (action) => {
          if (action === "confirm") {
            let ids = this.$refs.variableTable.selectIds;
            ids.forEach((row) => {
              const index = this.variables.findIndex((d) => d.id === row);
              this.variables.splice(index, 1);
            });
            // this.editData = {type: "CONSTANT"};
            this.sortParamters();
            this.editData = {};
            this.$refs.variableTable.cancelCurrentRow();
            this.$refs.variableTable.clear();

            this.saveData();
          }
        },
      });
    },
    filter() {
      let datas = [];
      this.variables.forEach((item) => {
        if (
          this.searchType &&
          this.searchType !== "" &&
          this.selectVariable &&
          this.selectVariable !== ""
        ) {
          if (
            (item.type &&
              item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) ===
                -1 &&
              this.searchType !== "ALL") ||
            (item.name &&
              item.name
                .toLowerCase()
                .indexOf(this.selectVariable.toLowerCase()) === -1)
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else if (this.selectVariable && this.selectVariable !== "") {
          if (
            item.name &&
            item.name
              .toLowerCase()
              .indexOf(this.selectVariable.toLowerCase()) === -1
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else if (this.searchType && this.searchType !== "") {
          if (
            item.type &&
            item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) ===
              -1 &&
            !(
              this.searchType === "STRING" &&
              (item.type === "STRING" || item.type === "CONSTANT")
            ) &&
            this.searchType !== "ALL"
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          item.hidden = undefined;
        }
        if (
          this.searchType === "ALL" &&
          !(this.selectVariable && this.selectVariable !== "")
        ) {
          item.hidden = undefined;
        }
        item.showMore = false;
        datas.push(item);
      });
      // this.variables = datas;
      this.targetVariables = datas.filter((v) => !v.hidden);
    },
    createFilter(queryString) {
      return (item) => {
        return (
          item.type &&
          item.type.toLowerCase().indexOf(queryString.toLowerCase()) !== -1
        );
      };
    },
    handleRowClick(row) {
      // 做深拷贝
      this.editData = JSON.parse(JSON.stringify(row));
      this.updateFiles();
      this.showDelete = true;
      this.selectType =
        !row.type || row.type === "CONSTANT" ? "STRING" : row.type;
    },
    updateFiles() {
      this.variables.forEach((item) => {
        if (item.id === this.editData.id) {
          this.editData.files = item.files;
        }
      });
    },
  },
};
</script>

<style scoped>
.ms-variable-hidden-row {
  display: none;
}

.ms-variable-header {
  background: #783887;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.ms-variable-link {
  float: right;
  margin-right: 45px;
}

fieldset {
  padding: 0px;
  margin: 0px;
  min-width: 100%;
  min-inline-size: 0px;
  border: 0px;
}

.ms-select {
  width: 100px;
  margin-right: 10px;
}

.el-dialog__body {
  padding: 0 20px 30px 20px;
}
:deep(.table-select-icon) {
  display: none !important;
}
</style>
