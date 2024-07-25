<template>
  <ms-container>
    <ms-main-container v-if="hasReportPermission">
      <el-card class="table-card" v-loading="loading">
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" @search="search" :show-create="false">
          </ms-table-header>
        </template>


        <el-table ref="reportListTable" border :data="tableData" class="adjust-table table-content" @sort-change="sort"
                  @select-all="handleSelectAll"
                  @select="handleSelect"
                  :height="screenHeight"
                  @filter-change="filter" @row-click="handleView">
          <el-table-column
            type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <el-dropdown slot="header" style="width: 14px">
              <span class="el-dropdown-link" style="width: 14px">
                <i class="el-icon-arrow-down el-icon--right" style="margin-left: 0px"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native.stop="isSelectDataAll(true)">
                  {{ $t('api_test.batch_menus.select_all_data', [total]) }}
                </el-dropdown-item>
                <el-dropdown-item @click.native.stop="isSelectDataAll(false)">
                  {{ $t('api_test.batch_menus.select_show_data', [tableData.length]) }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <template v-slot:default="scope">
              <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"
                             class="show-more-opt" v-if="hasPermission('PROJECT_UI_REPORT:READ+DELETE')"/>
            </template>
          </el-table-column>

          <ms-table-column
            prop="name"
            sortable
            :label="$t('commons.name')"
            :show-overflow-tooltip="false"
            :editable="true"
            :edit-content="$t('report.rename_report')"
            @editColumn="openReNameDialog"
            min-width="200px">
          </ms-table-column>

          <!-- column-key="reportType" :filters="reportTypeFilters" 报告类型筛选暂时屏蔽 -->
          <el-table-column prop="reportType" :label="$t('load_test.report_type')" width="150">
            <template v-slot:default="scope">
              <div v-if="scope.row.reportType === 'UI_INTEGRATED'">
                <el-tag size="mini" type="primary">
                  {{ $t('api_test.scenario.integrated') }}
                </el-tag>
                {{ $t('commons.scenario') }}
              </div>
              <div v-else>
                <el-tag size="mini" type="primary">
                  {{ $t('api_test.scenario.independent') }}
                </el-tag>
                {{ $t('commons.scenario') }}
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="userName" min-width="150"
                           :label="$t('api_test.automation.creator')"
                           :filters="userFilters"
                           column-key="create_user"
                           show-overflow-tooltip
                           sortable="custom"/>

          <el-table-column prop="createTime" min-width="150" :label="$t('commons.create_time')" sortable>
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="endTime" min-width="150" :label="$t('report.test_end_time')" sortable>
            <template v-slot:default="scope">
              <span>
                {{ scope.row.endTime | timestampFormatDate }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerMode" width="150" :label="$t('commons.trigger_mode.name')"
                           column-key="triggerMode" :filters="triggerFilters">
            <template v-slot:default="scope">
              <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('commons.status')"
                           column-key="status"
                           :filters="statusFilters">
            <template v-slot:default="{row}">
              <ms-api-report-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column width="150" :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <div>
                <ms-table-operator-button :tip="$t('api_report.detail')" icon="el-icon-s-data"
                                          @exec="handleView(scope.row)" type="primary"/>
                <ms-table-operator-button :tip="$t('api_report.delete')"
                                          v-permission="['PROJECT_UI_REPORT:READ+DELETE']"
                                          icon="el-icon-delete" @exec="handleDelete(scope.row)" type="danger"/>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
      <ms-rename-report-dialog ref="renameDialog" @submit="rename($event)"></ms-rename-report-dialog>
      <el-dialog :close-on-click-modal="false" :title="$t('test_track.plan_view.test_result')" width="60%"
                 :visible.sync="resVisible" class="api-import" destroy-on-close @close="resVisible=false">
        <ms-request-result-tail :response="response" ref="debugResult"/>
      </el-dialog>
    </ms-main-container>
  </ms-container>
</template>

<script>
import {UI_REPORT_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {_filter, _sort} from "metersphere-frontend/src/utils/tableUtils";
import MsRenameReportDialog from "metersphere-frontend/src/components/report/MsRenameReportDialog";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTabButton from "metersphere-frontend/src/components/MsTabButton";
import ShowMoreBtn from "@/business/automation/scenario/TableMoreBtn";
import MsRequestResultTail from "@/business/automation/report/components/RequestResultTail";
import MsApiReportStatus from "@/business/automation/report/ApiReportStatus";
import {queryUiScenarioReportList, queryApiDefinitionReport, deleteUiReport, banchDeleteUiReport} from "@/api/ui-report"
import {hasPermission, hasPermissions} from "metersphere-frontend/src/utils/permission";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {humpToLine} from "metersphere-frontend/src/utils";

export default {
  components: {
    MsApiReportStatus,
    MsRequestResultTail,
    ShowMoreBtn,
    ReportTriggerModeItem: () => import("metersphere-frontend/src/components/tableItem/ReportTriggerModeItem"),
    MsTableOperatorButton: () => import("metersphere-frontend/src/components/MsTableOperatorButton"),
    MsMainContainer: () => import("metersphere-frontend/src/components/MsMainContainer"),
    MsContainer: () => import("metersphere-frontend/src/components/MsContainer"),
    MsTableHeader: () => import("metersphere-frontend/src/components/MsTableHeader"),
    MsTablePagination: () => import("metersphere-frontend/src/components/pagination/TablePagination"),
    MsRenameReportDialog,
    MsTableColumn,
    MsTabButton,
  },
  props: {
    reportType: String
  },
  data() {
    return {
      result: {},
      resVisible: false,
      response: {},
      reportId: "",
      debugVisible: false,
      condition: {
        components: UI_REPORT_CONFIGS
      },
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      currentProjectId: "",
      statusFilters: [
        {text: 'Running', value: 'RUNNING'},
        {text: 'Error', value: 'ERROR'},
        {text: 'Success', value: 'SUCCESS'},
        {text: 'Stopped', value: 'STOPPED'},
        {text: 'Pending', value: 'PENDING'},
        {text: 'Timeout', value: 'TIMEOUT'},
      ],
      reportTypeFilters: [
        {text: this.$t('api_test.scenario.independent') + this.$t('commons.scenario'), value: 'UI_INDEPENDENT'},
        {text: this.$t('api_test.scenario.integrated') + this.$t('commons.scenario'), value: 'UI_INTEGRATED'}
      ],
      triggerFilters: [
        {text: this.$t('commons.trigger_mode.manual'), value: 'MANUAL'},
        {text: this.$t('api_test.automation.batch_execute'), value: 'BATCH'},
        {text: this.$t('commons.trigger_mode.schedule'), value: 'SCHEDULE'},
        {text: this.$t('commons.trigger_mode.api'), value: 'API'},
      ],
      buttons: [
        {
          name: this.$t('api_report.batch_delete'),
          handleClick: this.handleBatchDelete,
          permissions: ['PROJECT_UI_REPORT:READ+DELETE']
        }
      ],
      selectRows: new Set(),
      selectAll: false,
      unSelection: [],
      selectDataCounts: 0,
      screenHeight: 'calc(100vh - 160px)',
      userFilters: [],
    }
  },
  computed: {
    hasReportPermission() {
      return hasPermissions('PROJECT_UI_REPORT:READ');
    }
  },
  watch: {
    '$route': 'init',
  },
  methods: {
    hasPermission,
    search() {
      if (this.testId !== 'all') {
        this.condition.testId = this.testId;
      }
      this.condition.projectId = getCurrentProjectID();
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;

      this.condition.reportType = this.reportType;
      this.condition.isUi = true;
      this.loading = true;
      queryUiScenarioReportList(this.currentPage, this.pageSize, this.condition).then(response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(item => {
          if (item.status === 'STOP') {
            item.status = 'stopped'
          }
        })
        this.selectRows.clear();
        this.unSelection = data.listObject.map(s => s.id);
        this.loading = false;
      })
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    getExecResult(apiCase) {
      if (apiCase.id) {
        queryApiDefinitionReport(apiCase.id).then(response => {
          if (response.data) {
            try {
              let data = JSON.parse(response.data.content);
              this.response = data;
              this.resVisible = true;
            } catch (error) {
              this.resVisible = true;
            }
          }
        })
      }
    },
    handleView(report) {
      this.reportId = report.id;
      if (report.status === 'RUNNING') {
        this.$warning(this.$t('commons.run_warning'))
        return;
      }
      if (report.reportType.indexOf('SCENARIO') !== -1 || report.reportType.indexOf('UI_') !== -1 || report.reportType === 'API_INTEGRATED') {
        this.currentProjectId = report.projectId;
        this.$router.push({
          path: 'report/view/' + report.id + "?view=true",
          params: {showCancelButton: false, reportId: report.id}
        })
      } else {
        this.getExecResult(report);
      }
    },
    handleDelete(report) {
      this.$alert(this.$t('api_report.delete_confirm') + report.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            deleteUiReport({id: report.id}).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.search();
            })
          }
        }
      });
    },
    init() {
      this.testId = this.$route.params.testId;
      this.search();
    },
    sort(column) {
      if(column && !column.order && this.condition && this.condition.orders){
        //从condition 中找到并删除
        let field = humpToLine(column.column && column.column.columnKey ? column.column.columnKey : column.prop);
        this.condition.orders = this.condition.orders.filter(o => {
          return o.name !== field;
        })
      }
      _sort(column, this.condition);
      this.init();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.init();
    },
    handleSelect(selection, row) {
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
      this.selectRowsCount(this.selectRows)
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.tableData.forEach(item => {
          this.$set(item, "showMore", true);
          this.selectRows.add(item);
        });
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        })
      }
      this.selectRowsCount(this.selectRows)
    },
    handleBatchDelete() {
      console.log("shanchu ")
      this.$alert(this.$t('api_report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          this.loading = true;
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            let sendParam = {};
            sendParam.ids = ids;
            sendParam.selectAllDate = this.isSelectAllDate;
            sendParam.unSelectIds = this.unSelection;
            sendParam = Object.assign(sendParam, this.condition);
            sendParam.isUi = true;
            banchDeleteUiReport(sendParam).then(() => {
              this.selectRows.clear();
              this.$success(this.$t('commons.delete_success'));
              this.search();
              this.loading = false;
            })
          }
        }
      });
    },
    selectRowsCount(selection) {
      let selectedIDs = this.getIds(selection);
      let allIDs = this.tableData.map(s => s.id);
      this.unSelection = allIDs.filter(function (val) {
        return selectedIDs.indexOf(val) === -1
      });
      if (this.isSelectAllDate) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = selection.size;
      }
    },
    isSelectDataAll(dataType) {
      this.isSelectAllDate = dataType;
      this.selectRowsCount(this.selectRows)
      //如果已经全选，不需要再操作了
      if (this.selectRows.size !== this.tableData.length) {
        this.$refs.reportListTable.toggleAllSelection(true);
      }
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    openReNameDialog($event) {
      this.$refs.renameDialog.open($event);
    },
    rename(data) {
      this.$post("/ui/scenario/report/rename", data).then(() => {
        this.$success(this.$t("organization.integration.successful_operation"));
        this.init();
        this.$refs.renameDialog.close();
      });
    },
    getPrincipalOptions() {
      this.$get('/user/project/member/list').then(response => {
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
  },
  created() {
    this.init();
    this.getPrincipalOptions();
  }
}
</script>

<style scoped>
.table-content {
  width: 100%;
}

.active {
  border: solid 1px #6d317c !important;
  background-color: var(--primary_color) !important;
  color: #FFFFFF !important;
}

.item {
  height: 32px;
  padding: 5px 8px;
  border: solid 1px var(--primary_color);
}

:deep(.show-more-opt) .ms-icon-more {
  transform: rotate(90deg);
  margin-top: 0;
}
</style>
