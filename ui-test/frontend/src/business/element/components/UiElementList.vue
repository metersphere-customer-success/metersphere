<template>
  <el-card class="table-card">
    <template v-slot:header>
      <ms-table-header
          :condition.sync="condition"
          @search="getTableData"
          :create-permission="['PROJECT_UI_ELEMENT:READ+CREATE']"
          @create="handleCreate"
          :create-tip="$t('permission.project_ui_element.create')"
      />
    </template>

    <ms-table
        v-loading="loading"
        :data="tableData"
        :condition="condition"
        :total="page.total"
        :page-size.sync="page.pageSize"
        :operators="operators"
        :batch-operators="buttons"
        :fields.sync="fields"
        :enable-selection="true"
        field-key="ELEMENT_LIST"
        :screen-height="tableHeight"
        @handlePageChange="getTableData"
        @headChange="getTableData"
        @callBackSelectAll="callBackSelectAll"
        @callBackSelect="callBackSelect"
        @refresh="getTableData"
        ref="table"
    >
      <div v-for="item in fields" :key="item.key">
        <ms-table-column
            prop="num"
            sortable
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('commons.id')"
            min-width="80"
        />

        <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('ui.element_name')"
            prop="name"
            min-width="120px"
        />

        <ms-table-column
            :field="item"
            :label="$t('ui.element_locator_type')"
            :filters="locationTypeFilters"
            :fields-width="fieldsWidth"
            prop="locationType"
            min-width="120px"
        />

        <ms-table-column
            :field="item"
            :label="$t('ui.element_locator')"
            :fields-width="fieldsWidth"
            prop="location"
            min-width="130px"
        />

        <ms-table-column
            prop="description"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :label="$t('ui.description')"
            min-width="120px"
        />

        <ms-table-column
            prop="modulePath"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.case.module')"
            min-width="120px"
        />

        <ms-table-column
            prop="createUser"
            min-width="120px"
            :label="$t('commons.create_user')"
            :filters="userFilters"
            :field="item"
            :fields-width="fieldsWidth"
            sortable="custom"
        />

        <ms-update-time-column :field="item" :fields-width="fieldsWidth"/>

        <ms-table-column
            prop="updateUser"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('ui.update_user')"
            min-width="120px"
        />

        <ms-create-time-column :field="item" :fields-width="fieldsWidth"/>
      </div>
    </ms-table>

    <ms-table-pagination
        :change="getTableData"
        :current-page.sync="page.currentPage"
        :page-size.sync="page.pageSize"
        :total="page.total"
    />

    <ui-element-edit
        :module-tree="moduleTree"
        @refreshTree="refreshTree"
        @refresh="getTableData"
        ref="uiElementEdit"
    />

    <batch-move @refresh="getTableData" @moveSave="moveSave" ref="testBatchMove"/>

  </el-card>
</template>

<script>
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {UI_ELEMENT_LOCATION_TYPE_OPTION} from "metersphere-frontend/src/utils/table-constants";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import HeaderCustom from "metersphere-frontend/src/components/head/HeaderCustom";
import {getProjectUsers} from "metersphere-frontend/src/api/user"
import {
  getCustomTableHeader,
  getCustomTableWidth,
  getPageInfo,
  initCondition,
} from "@/common/js/tableUtils";
import MsCustomTableHeader from "metersphere-frontend/src/components/table/MsCustomTableHeader";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import {
  deleteUiElement,
  getUiElements,
  checkReference,
  checkReferenceBatch,
  deleteUiElementBatch,
  copyUiElementBatch
} from "@/business/network/ui-element";

import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import UiElementEdit from "./UiElementEdit";
import {downloadElementExport} from "@/business/automation/ui-automation";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getDefaultTableHeight} from "@/api/api-automation";
import MsTable from "./UiElementListTable";

export default {
  name: "UiElementList",
  components: {
    UiElementEdit,
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsCustomTableHeader,
    HeaderCustom,
    MsTableHeader,
    MsTablePagination,
    MsTableButton,
    MsTableOperators,
    MsTableColumn,
    MsTable,
    BatchEdit: () => import("@/business/automation/scenario/component/case/BatchEdit"),
    BatchMove: () => import("@/business/automation/scenario/component/case/BatchMove"),
  },
  props: {
    moduleTree: Array,
    selectNodeIds: Array,
    batchOperators: {
      type: Array,
      default: null
    },
  },
  created() {
    this.getPrincipalOptions();
    if (!this.batchOperators) {
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
        this.buttons = this.trashButtons;
      } else {
        if (!this.isReferenceTable) {
          this.buttons = this.unTrashButtons;
        } else {
          this.buttons = this.unTrashButtons;
        }
      }
    }
  },
  data() {
    return {
      loading: false,
      tableData: [],
      condition: {},
      locationTypeFilters: [],
      page: getPageInfo(),
      fields: getCustomTableHeader("ELEMENT_LIST"),
      fieldsWidth: getCustomTableWidth("ELEMENT_LIST"),
      operators: [
        {
          tip: this.$t("commons.edit"),
          icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ["PROJECT_UI_ELEMENT:READ+EDIT"],
        },
        {
          tip: this.$t("commons.copy"),
          icon: "el-icon-copy-document",
          type: "success",
          exec: this.handleCopy,
          permissions: ["PROJECT_UI_ELEMENT:READ+COPY"],
        },
        {
          tip: this.$t("commons.delete"),
          icon: "el-icon-delete",
          type: "danger",
          exec: this.handleDelete,
          permissions: ["PROJECT_UI_ELEMENT:READ+DELETE"],
        },
      ],
      userFilters: [],
      buttons: [],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_UI_ELEMENT:READ+DELETE']
        },
        {
          name: this.$t('commons.batch_restore'),
          handleClick: this.handleBatchRestore
        },
      ],
      unTrashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_UI_ELEMENT:READ+DELETE']
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_UI_ELEMENT:READ+EDIT']
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_UI_ELEMENT:READ+EDIT']
        },
      ]
    };
  },
  activated() {
    this.getTableData();
    this.$EventBus.$emit("callBackSelect", [], false);
    this.locationTypeFilters = UI_ELEMENT_LOCATION_TYPE_OPTION;
  },
  computed: {
    tableHeight() {
      return getDefaultTableHeight();
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  watch: {
    selectNodeIds() {
      this.page.currentPage = 1;
      initCondition(this.condition, false);
      this.getTableData();
    },
  },
  methods: {
    callBackSelectAll(selection, all) {
      this.$EventBus.$emit("callBackSelect", selection, all);
    },
    callBackSelect(selection) {
      this.$EventBus.$emit("callBackSelect", selection, false);
    },
    initCondition() {
      this.condition.moduleIds = [];
      initCondition(this.condition, this.condition.selectAll);
      this.condition.moduleIds = this.selectNodeIds;
      this.condition.versionId = this.currentVersion || null;
      this.condition.projectId = getCurrentProjectID();
    },
    getTableData() {
      this.$EventBus.$emit("elementUiConditionBus", this.condition.name)
      this.$EventBus.$emit("elementUiConditions", this.condition)
      this.initCondition();
      this.loading = getUiElements(this.page, this.condition).then(data => {
        this.page.total = data.data.itemCount;
        this.tableData = data.data.listObject;
        if (this.$refs.table) {
          this.$refs.table.clearSelection();
        }
      });
    },
    handleEdit(data) {
      this.$refs.uiElementEdit.open(data);
    },
    refreshTree() {
      this.$emit("refreshTree");
    },
    handleCreate() {
      let moduleId = undefined;
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        moduleId = this.selectNodeIds[0];
      }
      this.$refs.uiElementEdit.open(null, moduleId);
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data);
      copyData.id = null;
      copyData.name = data.name + "_copy";
      this.$refs.uiElementEdit.open(copyData);
    },
    async handleDelete(data) {
      //校验是否存在引用关系
      //this.$t('api_report.delete_confirm')
      let name = !data.name ? "" : data.name.length > 15 ? data.name.slice(0, 15) + "..." : data.name;
      let tipName = this.$t("ui.confirm_del_el") + name + " ？";
      let res = await checkReference(this.projectId, data.id);
      if (res) {
        let result = res.data;
        if (
            result &&
            result.referenceSize > 0 &&
            result.tipName
        ) {
          tipName = this.$t("ui.element_beening_desc") + ' ' + `${result.tipName} ` +this.$t("ui.reference") + "，" + this.$t("ui.continue_or_not_delete") + '?';
        }
      }
      this.$alert(tipName, "", {
        confirmButtonText: this.$t("commons.confirm"),
        callback: (action) => {
          if (action === "confirm") {
            deleteUiElement(data.id).then(() => {
              this.$success(this.$t("commons.delete_success"));
              this.getTableData();
              this.$emit("refreshTree");
            });
          }
        },
      });
    },
    handleExport(data){
      this.condition.ids = data.ids;
      this.condition.moduleIds = data.nodeIds;
      this.condition.name = data.name;
      this.loading = true;
      let fileName = this.$t("ui.ui_element") + this.$t("commons.export") + ".xlsx";
      downloadElementExport(this.condition, fileName).then(() => {
        this.loading = false;
        this.$emit("refreshTable");
      });
    },
    getPrincipalOptions(option) {
      getProjectUsers().then(response => {
        this.userFilters = response.data.map((u) => {
          return {text: u.name, value: u.id};
        });
      })
    },
    async handleDeleteBatch(row) {
      if (this.trashEnable) {
        //let ids = Array.from(this.selectRows).map(row => row.id);
        let param = {};
        this.buildBatchParam(param);
        this.result.loading = true;
        this.$post('/ui/automation/deleteBatchByCondition', param).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        }).catch((error) => {
          this.search();
        });
        return;
      } else {
        let param = {};
        this.buildBatchParam(param);
        let tipName;
        let rows = this.$refs.table.getSelectRows();
        let tempName = rows.entries().next().value[0].name;
        let name = !tempName ? "" : tempName.length > 15 ? tempName.slice(0, 15) + "..." : tempName;
        if (!param.selectAll && param.ids && param.ids.length) {
          if (param.ids.length == 1) {
            tipName = this.$t("ui.confirm_del_el") + name + " ？";
          } else {
            tipName = this.$t("ui.confirm_del") + name + this.$t("ui.deng") + (param.selectAll ? this.page.total : param.ids.length) + this.$t("ui.ge_el");
          }
        } else {
          tipName = this.$t("ui.confirm_del") + name + this.$t("ui.deng") + (param.selectAll ? this.page.total : param.ids.length) + this.$t("ui.ge_el");
        }
        let res = await checkReferenceBatch(this.projectId, param);
        if (res) {
          let result = res.data;
          if (
              result &&
              result.referenceSize > 0 &&
              result.tipName
          ) {
            tipName = this.$t("ui.element_beening_desc") + ' ' + `${result.tipName} ` +this.$t("ui.reference") + "，" + this.$t("ui.continue_or_not_delete") + '?';
          }
        }
        this.$alert(tipName, "", {
          confirmButtonText: this.$t("commons.confirm"),
          callback: (action) => {
            if (action === "confirm") {
              deleteUiElementBatch(param).then(() => {
                this.$success(this.$t("commons.delete_success"));
                this.getTableData();
                this.$emit("refreshTree");
              });
            }
          },
        });
      }
    },
    handleBatchRestore() {
      let ids = this.$refs.table.selectIds;

      let params = {};
      this.buildBatchParam(params);
      params.ids = ids;

      this.$post("/ui/automation/id/all", params, response => {
        let idParams = response.data;
        this.$post("/ui/automation/reduction", idParams, response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      });
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      this.$refs.testBatchMove.open(this.moduleTree, this.$refs.table.selectIds, this.moduleOptions);
    },
    buildBatchParam(param) {
      this.initCondition();
      if (this.$refs.table.selectAll) {
        param.ids = [];
      } else {
        param.ids = this.$refs.table ? this.$refs.table.selectIds : [];
      }
      Object.assign(param, this.condition);
    },
    moveSave(param) {
      this.buildBatchParam(param);
      param.moduleId = param.nodeId;
      param.modulePath = param.nodePath;
      let url = '/ui/element/batch/copy';
      if (this.isMoveBatch) {
        url = '/ui/element/batch/edit';
      }

      copyUiElementBatch(url, param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.getTableData();
        this.$emit("refreshTree");
      });
    },
  },
};
</script>

<style scoped></style>
