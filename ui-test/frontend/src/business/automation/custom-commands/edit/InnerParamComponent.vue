<template>
  <div>
    <el-input
      :placeholder="$t('api_test.search_by_variables')"
      class="param-data-search"
      v-model="selectVariable"
      @change="filter"
      size="mini"
    ></el-input>
    <ms-table
      row-key="id"
      :data="variables"
      class="param-data-table"
      height="400"
      :enableSelection="false"
    >
      <el-table-column prop="name" :label="$t('api_test.variable_name')">
        <template slot-scope="scope">
          <el-input
            size="mini"
            v-model="scope.row.name"
            @change="changeParam"
            :disabled="!editable"
          >
          </el-input>
        </template>
      </el-table-column>
      <el-table-column prop="type" :label="$t('test_track.case.type')">
        <template slot-scope="scope">
          <el-select
            v-model="scope.row.type"
            :placeholder="$t('ui.please_select')"
            size="mini"
            :disabled="!editable"
          >
            <el-option
              v-for="item in paramTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="required" :label="$t('ui.is_required')">
        <div slot="header">
          {{ $t("ui.is_required") }}
          <ms-instructions-icon
            size="14"
            :content="$t('ui.param_required_tip')"
          />
        </div>
        <template slot-scope="scope">
          {{
            scope.row.required == undefined
              ? ""
              : scope.row.required
              ? $t("ui.yes")
              : $t("ui.no")
          }}
        </template>
      </el-table-column>
      <el-table-column prop="value" :label="$t('api_test.value')">
        <template slot-scope="scope">
          <el-input
            size="mini"
            v-model="scope.row.value"
            :disabled="!parentREFNode && !editable"
            @change="preCreate(scope.row)"
          >
          </el-input>
        </template>
      </el-table-column>
      <el-table-column prop="description" :label="$t('commons.description')">
        <template slot-scope="scope">
          <el-tooltip>
            <template slot="content">
              <div>
                {{ scope.row.description || $t("commons.description") }}
              </div>
            </template>
            <el-input
              size="mini"
              v-model="scope.row.description"
              :disabled="!parentREFNode && !editable"
            >
            </el-input>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="options" :label="$t('commons.operating')">
        <template v-slot:default="scope">
          <span>
            <!-- <el-switch v-model="scope.row.enable" size="mini" /> -->
            <el-tooltip
              :content="$t('test_resource_pool.enable_disable')"
              placement="top"
            >
              <el-switch
                v-model="scope.row.enable"
                size="mini"
                @change="changeSwitch"
                style="margin-left: 10px"
                v-if="(!editable || stepCopy) && scope.row.name"
                :disabled="!parentREFNode && !stepCopy"
              />
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="$t('ui.revoke')"
              placement="top-start"
            >
              <el-button
                icon="el-icon-refresh-left"
                size="mini"
                circle
                style="margin-left: 10px"
                @click="handleRevoke(scope.row)"
                v-if="!editable && scope.row.name"
                :disabled="!parentREFNode"
              />
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="$t('commons.remove')"
              placement="top-start"
            >
              <el-button
                icon="el-icon-delete"
                type="danger"
                circle
                size="mini"
                style="margin-left: 10px"
                @click="removeInnerParam(scope.row)"
                v-if="editable && scope.row.name"
              />
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </ms-table>
  </div>
</template>
<script>
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {getUUID} from "metersphere-frontend/src/utils";
export default {
  name: "InnerParamComponent",
  components: {
    MsTable,
    MsInstructionsIcon,
  },
  props: {
    currentCommand: {},
    // editable: {
    //   type: Boolean,
    //   default: true,
    // },
  },
  computed: {
    allowRemove() {
      return true;
    },
    stepCopy() {
      return this.currentCommand.referenced === "Copy";
    },
    editable() {
      return !(this.currentCommand.referenced === "REF");
    },
    parentREFNode() {
      return this.currentCommand.pREF;
    },
  },
  data() {
    return {
      TIMER: -1,
      variables: [],
      selectVariable: "",
      innerParamData: [],
      paramTypeOptions: [
        {
          label: this.$t("api_test.automation.string"),
          value: "STRING",
        },
        {
          label: this.$t("api_test.automation.json"),
          value: "JSON",
        },
        {
          label: this.$t("api_test.automation.array"),
          value: "ARRAY",
        },
        {
          label: this.$t("api_test.automation.number"),
          value: "NUMBER",
        },
      ],
    };
  },
  watch: {
    "currentCommand.variables": {
      handler(v) {
        this.variables = v || [];
        this.sortParam();
        this.checkEmpty();
      },
      immediate: true,
      deep: true,
    },
  },
  mounted() {
    if (this.editable) {
      this.changeParam();
    }
    this.checkEmpty();
  },
  methods: {
    removeEmpty() {
      this.variables = this.variables.filter((e) => {
        return e.name;
      });
    },
    handleRevoke(row) {
      if (row.originValue != undefined) {
        row.value = row.originValue;
        this.emitChangeInnerParam();
      }
    },
    changeSwitch() {
      this.emitChangeInnerParam();
    },
    emitChangeInnerParam() {
      let temp = JSON.parse(JSON.stringify(this.variables));
      //保存时候 移除空行
      temp = temp.filter((e) => {
        return e.name || e.value;
      });
      this.$emit("changeInnerParam", temp);
    },
    removeInnerParam: function (index) {
      const dataIndex = this.variables.findIndex((d) => d.name === index.name);
      this.variables.splice(dataIndex, 1);
      this.emitChangeInnerParam();
    },
    preCreate(row) {
      clearTimeout(this.TIMER);
      this.TIMER = setTimeout(() => {
        this.emitChangeInnerParam();
      }, 10);
    },
    changeParam() {
      let isNeedCreate = true;
      let removeIndex = -1;
      let repeatKey = "";
      this.variables.forEach((item, index) => {
        this.variables.forEach((row, rowIndex) => {
          if (item.name === row.name && index !== rowIndex) {
            repeatKey = item.name;
          }
        });
        if (!item.name && !item.value) {
          // 多余的空行
          if (index !== this.variables.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });
      if (repeatKey !== "") {
        this.$warning(
          "" + "【" + repeatKey + "】" + this.$t("load_test.param_is_duplicate")
        );
      }
      if (isNeedCreate) {
        this.variables.push({
          enable: true,
          id: getUUID(),
          type: "STRING",
        });
      }

      this.emitChangeInnerParam();
    },
    /**
     * 入参表格 样式
     */
    innerRowClassName({ row, rowIndex }) {
      if (rowIndex === 1) {
        return "warning-row";
      } else if (rowIndex === 3) {
        return "success-row";
      }
      return "";
    },
    filter() {
      let datas = [];
      this.variables.forEach((item) => {
        if (this.selectVariable && this.selectVariable != "" && item.name) {
          if (
            item.name
              .toLowerCase()
              .indexOf(this.selectVariable.toLowerCase()) == -1
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          item.hidden = undefined;
        }
        datas.push(item);
      });
      this.variables = datas;
    },
    /**
     * 空行移动到最后一行
     */
    sortParam() {},
    checkEmpty() {
      //不可编辑就不用创建了
      if (!this.editable) {
        //移除空行
        this.removeEmpty();
        return;
      }
      let isNotNeedCreate = this.variables.filter((e) => {
        return e.name === "" || e.name === undefined;
      });
      if (!isNotNeedCreate || isNotNeedCreate.length <= 0) {
        this.variables.push({
          enable: true,
          id: getUUID(),
          type: "STRING",
        });
      }
    },
  },
};
</script>
<style scoped>
.param-data-search {
  margin: 10px 0px;
  width: 200px;
}

.el-table .warning-row {
  background: oldlace;
}

.el-table .success-row {
  background: #f0f9eb;
}
</style>
