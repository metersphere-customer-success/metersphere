<template>
  <el-card class="table-card" v-loading="cardLoading">
    <template v-slot:header>
      <ms-table-header
          :condition.sync="condition"
          @search="search"
          @create="testPlanCreate"
          :create-tip="$t('test_track.data.dataRule')"
      />
    </template>

    <ms-table
        v-loading="cardLoading"
        operator-width="220px"
        row-key="id"
        :data="tableData"
        :condition="condition"
        :total="total"
        :page-size.sync="pageSize"
        :operators="operators"
        :screen-height="screenHeight"
        :batch-operators="batchButtons"
        :remember-order="true"
        :field-key="tableHeaderKey"
        :fields.sync="fields"
        @handlePageChange="intoPlan"
        @order="initTableData"
        ref="testPlanLitTable"
        @filter="search"
        @handleRowClick="intoPlan"
        class="plan-table"
    >
      <span v-for="item in fields" :key="item.key">
        <ms-table-column
            prop="name"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :label="$t('data.name')"
            min-width="200px"
        >
        </ms-table-column>
        <ms-table-column
            prop="ruleContext"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('data.ruleContext')"
            min-width="200px"
        >
        </ms-table-column>
        <ms-table-column
            prop="testPoint"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('data.testPoint')"
            min-width="200px"
        >
        </ms-table-column>

        <ms-table-column
            prop="type"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :label="$t('data.type')"
            min-width="200px"
        >
<!--          <template v-slot:default="scope">-->
<!--             <span v-for="item in typeOption" :key="item.value">-->
<!--        <span v-if="scope.row.type == item.value">-->
<!--          {{item.label}}-->
<!--        </span>-->
<!--      </span>-->
<!--          </template>-->
        </ms-table-column>
        <ms-table-column
            prop="caseQuality"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :filters="stageFilters"
            :label="$t('data.caseQuality')"
            min-width="120px"
        >
        </ms-table-column>
        <ms-table-column
            prop="iter"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('data.iter')"
            min-width="120px"
        >
        </ms-table-column>
        <ms-table-column
            prop="genNum"
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('data.genNum')"
            min-width="200px"
        >
        </ms-table-column>

        <ms-table-column
            prop="genType"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :label="$t('data.genType')"
            min-width="160px"
        >
        </ms-table-column>
        <ms-table-column
            prop="genConcurrent"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :label="$t('data.genConcurrent')"
            min-width="200px"
        >
        </ms-table-column>
        <ms-table-column
            prop="encryptMethod"
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            :label="$t('data.encryptMethod')"
            min-width="200px"
        >
        </ms-table-column>

      </span>
      <template v-slot:opt-before="scope">
        <ms-table-operator-button
            :tip="$t('commons.edit')"
            icon="el-icon-edit"
            @exec="handleEdit(scope.row)"
            v-permission="['PROJECT_TRACK_PLAN:READ+EDIT']"
            :disabled="scope.row.status === 'Archived'"
        />
      </template>
      <template v-slot:opt-behind="scope">
        <el-tooltip
            :content="$t('commons.follow')"
            placement="bottom"
            effect="dark"
            v-if="!scope.row.showFollow"
        >
          <i
              class="el-icon-star-off"
              style="
              color: #783987;
              font-size: 25px;
              cursor: pointer;
              padding-left: 5px;
              width: 28px;
              height: 28px;
              top: 5px;
              position: relative;
            "
              @click="saveFollow(scope.row)"
          ></i>
        </el-tooltip>
        <el-tooltip
            :content="$t('commons.cancel')"
            placement="bottom"
            effect="dark"
            v-if="scope.row.showFollow"
        >
          <i
              class="el-icon-star-on"
              style="
              color: #783987;
              font-size: 30px;
              cursor: pointer;
              padding-left: 5px;
              width: 28px;
              height: 28px;
              top: 6px;
              position: relative;
            "
              @click="saveFollow(scope.row)"
          ></i>
        </el-tooltip>
        <el-dropdown
            @command="handleCommand($event, scope.row)"
            class="scenario-ext-btn"
            v-permission="[
            'PROJECT_TRACK_PLAN:READ+DELETE',
            'PROJECT_TRACK_PLAN:READ+SCHEDULE',
          ]"
        >
          <el-link type="primary" :underline="false">
            <el-icon class="el-icon-more"></el-icon>
          </el-link>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
                command="delete"
                v-permission="['PROJECT_TRACK_PLAN:READ+DELETE']"
            >
              {{ $t("commons.delete") }}
            </el-dropdown-item>
            <el-dropdown-item
                command="schedule_task"
                v-permission="['PROJECT_TRACK_PLAN:READ+SCHEDULE']"
                :disabled="scope.row.status === 'Archived'"
            >
              {{ $t("commons.trigger_mode.schedule") }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
    </ms-table>
    <ms-table-pagination
        :change="pageSearch"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"
    />
    <ms-delete-confirm
        :title="$t('test_track.plan.plan_delete')"
        @delete="_handleDelete"
        ref="deleteConfirm"
        :with-tip="enableDeleteTip"
    >
      {{ $t("test_track.plan.plan_delete_tip") }}
    </ms-delete-confirm>

<!--    <review-or-plan-batch-move @refresh="refresh" @moveSave="moveSave" ref="testReviewBatchMove"/>-->
  </el-card>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import PlanStatusTableItem from "../../common/PlanStatusTableItem";
import PlanStageTableItem from "../../common/PlanStageTableItem";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import {DATA_RULES_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {
  _filter,
  _sort,
  deepClone,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  saveLastTableSortField,
} from "metersphere-frontend/src/utils/tableUtils";
import HeaderCustom from "metersphere-frontend/src/components/head/HeaderCustom";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCurrentProjectID, getCurrentUser, getCurrentUserId,} from "metersphere-frontend/src/utils/token";
import {hasPermission,} from "metersphere-frontend/src/utils/permission";
import {operationConfirm} from "metersphere-frontend/src/utils";
import MsTaskCenter from "metersphere-frontend/src/components/task/TaskCenter";
import {
  batchDeletePlan,
  getPlanStageOption,
  testPlanBatchMove,
  testPlanCopy,
  testPlanDelete,
  testPlanEdit,
  testPlanEditRunConfig,
  testPlanGetEnableScheduleCount,
  testPlanList,
  testPlanMetric,
  testPlanRun,
  testPlanRunBatch,
  testPlanRunSave,
  testPlanUpdateScheduleEnable
} from "@/api/remote/plan/test-plan";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTagsColumn from "metersphere-frontend/src/components/table/MsTagsColumn";
import {getProjectMemberUserFilter} from "@/api/user";
import {mapState} from "pinia";
import {useStore} from "@/store";

export default {
  name: "TestPlanList",
  components: {
    // ReviewOrPlanBatchMove,
    MsTagsColumn,
    // TestPlanReportReview,
    MsTag,
    HeaderLabelOperate,
    HeaderCustom,
    MsDeleteConfirm,
    PlanStageTableItem,
    PlanStatusTableItem,
    MsTableOperator,
    MsTableOperatorButton,
    MsDialogFooter,
    MsTableHeader,
    MsTablePagination,
    MsTaskCenter,
    MsTableColumn,
    MsTable,
  },
  data() {
    return {
      typeOption: {
        type:Array,
        default() {
          return [
            {value:'边界值',label: '边界值'},
            {value:'等价类',label: '等价类'}
          ];
        }
      },
      createUser: "",
      tableHeaderKey: "TEST_PLAN_LIST",
      tableLabel: [],
      enableDeleteTip: false,
      showExecute: false,
      deletePath: "/test/plan/delete",
      cardLoading: false,
      condition: {
        components: DATA_RULES_CONFIGS,
      },
      currentPage: 1,
      pageSize: 10,
      hasEditPermission: false,
      hasSchedulePermission: false,
      total: 0,
      tableData: [],
      fields: getCustomTableHeader("DATA_RULES"),
      fieldsWidth: getCustomTableWidth("TEST_PLAN_LIST"),
      screenHeight: "calc(100vh - 160px)",
      statusFilters: [
        {
          text: this.$t("test_track.plan.plan_status_prepare"),
          value: "Prepare",
        },
        {
          text: this.$t("test_track.plan.plan_status_running"),
          value: "Underway",
        },
        {
          text: this.$t("test_track.plan.plan_status_finished"),
          value: "Finished",
        },
        {
          text: this.$t("test_track.plan.plan_status_completed"),
          value: "Completed",
        },
        {
          text: this.$t("test_track.plan.plan_status_archived"),
          value: "Archived",
        },
      ],
      stageFilters: [
        {text: this.$t("test_track.plan.smoke_test"), value: "smoke"},
        {text: this.$t("test_track.plan.system_test"), value: "system"},
        {
          text: this.$t("test_track.plan.regression_test"),
          value: "regression",
        },
      ],
      scheduleFilters: [
        {text: this.$t("test_track.plan.schedule_enabled"), value: "OPEN"},
        {text: this.$t("test_track.issue.status_closed"), value: "SHUT"},
        {text: this.$t("schedule.not_set"), value: "NOTSET"},
      ],
      currentPlanId: "",
      stageOption: [],
      operators: [],
      batchButtons: [],
      userFilter: [],
      publicButtons: [
        {
          name: this.$t("test_track.plan.test_plan_batch_switch"),
          handleClick: this.handleBatchSwitch,
          permissions: ["PROJECT_TRACK_PLAN:READ+SCHEDULE"],
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_TRACK_PLAN:READ+EDIT']
        },
        {
          name: this.$t("api_test.automation.batch_execute"),
          handleClick: this.handleBatchExecute,
          permissions: ["PROJECT_TRACK_PLAN:READ+SCHEDULE"],
        },
        {
          name: this.$t("commons.delete_batch"),
          handleClick: this.handleBatchDelete,
          permissions: ["PROJECT_TRACK_PLAN:READ+BATCH_DELETE"],
        },
      ],
      simpleOperators: [
        {
          tip: this.$t("commons.copy"),
          icon: "el-icon-copy-document",
          exec: this.handleCopy,
          permissions: ["PROJECT_TRACK_PLAN:READ+COPY"],
        },
        {
          tip: this.$t("test_track.plan_view.view_report"),
          icon: "el-icon-s-data",
          exec: this.openReport,
          permissions: ["PROJECT_TRACK_PLAN:READ+EDIT"],
        },
      ],
      batchExecuteType: "serial",
      //是否有UI执行用例
      haveUICase: false,
      //是否有API执行用例
      haveOtherExecCase: false,
    };
  },
  watch: {
    $route(to, from) {
      if (to.path.indexOf("/track/plan/all") >= 0) {
        this.initTableData();
      }
    },
  },
  created() {
    this.projectId = this.$route.params.projectId;
    this.batchButtons = this.publicButtons;
    this.operators = this.simpleOperators;
    if (!this.projectId) {
      this.projectId = getCurrentProjectID();
    }
    this.hasEditPermission = hasPermission("PROJECT_TRACK_PLAN:READ+EDIT");
    this.hasSchedulePermission = hasPermission(
        "PROJECT_TRACK_PLAN:READ+SCHEDULE"
    );
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    // getPlanStageOption().then((r) => {
    //   this.stageOption = r.data;
    //   this.setAdvSearchStageOption();
    //   if (this.stageOption.length > 0) {
    //     this.stageFilters = this.stageOption;
    //     this.stageFilters.forEach((stage) => {
    //       if (stage.system !== null && stage.system) {
    //         stage.text = this.$t(stage.text);
    //       }
    //     });
    //   }
    // });
    getProjectMemberUserFilter((data) => {
      this.userFilter = data;
    });
    this.initTableData();
  },
  computed: {
    ...mapState(useStore, {
      moduleOptions: 'testPlanModuleOptions',
    }),
    nodePathMap() {
      let map = new Map();
      if (this.moduleOptions) {
        this.moduleOptions.forEach((item) => {
          map.set(item.id, item.path);
        });
      }
      return map;
    }
  },
  props: {
    treeNodes: {
      type: Array
    },
    currentNode: {
      type: Object
    },
    currentSelectNodes: {
      type: Array
    }
  },
  methods: {
    moveSave(param) {
      param.condition = this.condition;
      param.projectId = this.projectId;
      testPlanBatchMove(param)
        .then(() => {
          this.$refs.testReviewBatchMove.btnDisable = false;
          this.$success(this.$t('commons.save_success'), false);
          this.$refs.testReviewBatchMove.close();
          this.refresh();
        });
    },
    handleBatchMove() {
      let batchSelectCount = this.$refs.testPlanLitTable.selectDataCounts;
      let firstSelectRow = this.$refs.testPlanLitTable.selectRows.values().next().value;
      this.$refs.testReviewBatchMove.open(firstSelectRow.name, this.treeNodes, this.$refs.testPlanLitTable.selectIds, batchSelectCount, this.moduleOptions, '计划');
    },
    setAdvSearchStageOption() {
      let comp = this.condition.components.find((c) => c.key === "stage");
      if (comp) {
        comp.options = this.stageOption;
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    init() {
      this.initTableData();
    },
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.currentPage = 1;
      this.initTableData();
    },
    pageSearch() {
      this.initTableData(this.currentSelectNodes);
    },
    initTableData(nodeIds) {
      this.cardLoading = true;
      this.condition.nodeIds = [];
      if (this.planId) {
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (nodeIds && Array.isArray(nodeIds) && nodeIds.length > 0) {
        this.condition.nodeIds = nodeIds;
      }
      if (!this.projectId) {
        return;
      }
      this.condition.projectId = getCurrentProjectID();
      // 设置搜索条件, 用于刷新树
      this.$emit('setCondition', this.condition);
      this.$emit('refreshTree');
      testPlanList(
          {pageNum: this.currentPage, pageSize: this.pageSize},
          this.condition
      ).then((response) => {
        this.cardLoading = false;
        let data = response.data;
        this.total = data.itemCount;
        let testPlanIds = [];
        data.listObject.forEach((item) => {
          testPlanIds.push(item.id);
          if (item.tags) {
            item.tags = JSON.parse(item.tags);
            if (item.tags.length === 0) {
              item.tags = null;
            }
          }

          if (item.principalUsers) {
            let data = item.principalUsers;
            let principal = "";
            let principalIds = data.map((d) => d.id);
            data.forEach((d) => {
              if (principal !== "") {
                principal = principal + "、" + d.name;
              } else {
                principal = principal + d.name;
              }
            });
            this.$set(item, "principalName", principal);
            this.$set(item, "principals", principalIds);
          }
        });
        this.tableData = data.listObject;
        // this.getTestPlanDetailData(testPlanIds);
      });
    },
    getTestPlanDetailData(testPlanIds) {
      testPlanMetric(testPlanIds)
          .then((res) => {
            let metricDataList = res.data;
            if (metricDataList) {
              this.tableData.forEach((item) => {
                let metricData = null;
                metricDataList.forEach((metricItem) => {
                  if (item.id === metricItem.id) {
                    metricData = metricItem;
                  }
                });
                if (metricData) {
                  this.$set(item, "isMetricLoadOver", true);
                  this.$set(item, "passRate", metricData.passRate + "%");
                  this.$set(item, "testRate", metricData.testRate);
                  this.$set(item, "passed", metricData.passed);
                  this.$set(item, "tested", metricData.tested);
                  this.$set(item, "total", metricData.total);
                  if (metricData.followUsers) {
                    let data = metricData.followUsers;
                    let follow = "";
                    let followIds = data.map((d) => d.id);
                    let showFollow = false;
                    data.forEach((d) => {
                      if (follow !== "") {
                        follow = follow + "、" + d.name;
                      } else {
                        follow = follow + d.name;
                      }
                      if (this.currentUser().id === d.id) {
                        showFollow = true;
                      }
                    });
                    this.$set(item, "follow", follow);
                    this.$set(item, "follows", followIds);
                    this.$set(item, "showFollow", showFollow);
                  }
                } else {
                  this.resetTestPlanRow(item);
                }
              });
            }
          })
          .catch(() => {
            this.tableData.forEach((item) => {
              this.resetTestPlanRow(item);
            });
          });
    },
    resetTestPlanRow(item) {
      if (!item.isMetricLoadOver) {
        return;
      }
      // this.$set(item, "isMetricLoadOver", true);
      // this.$set(item, "principalName", "");
      // this.$set(item, "follow", "");
      // this.$set(item, "principals", []);
      // this.$set(item, "follows", []);
      // this.$set(item, "showFollow", false);
      // this.$set(item, "passRate", 0 + "%");
      // this.$set(item, "testRate", 0);
      // this.$set(item, "passed", 0);
      // this.$set(item, "tested", 0);
      // this.$set(item, "total", 0);
    },
    copyData(status) {
      return JSON.parse(JSON.stringify(this.dataMap.get(status)));
    },
    testPlanCreate() {
      if (!this.projectId) {
        this.$warning(this.$t("commons.check_project_tip"));
        return;
      }
      this.$emit("openTestPlanEditDialog");
    },
    handleEdit(testPlan) {
      this.$emit("testPlanEdit", testPlan);
    },
    refresh() {
      this.$refs.testPlanLitTable.clear();
      this.$refs.testPlanLitTable.isSelectDataAll(false);
      this.initTableData(this.currentSelectNodes);
    },
    handleBatchDelete() {
      this.$confirm(
          this.$t("plan.batch_delete_tip").toString(),
          this.$t("commons.delete_batch").toString(),
          {
            confirmButtonText: this.$t("commons.confirm"),
            cancelButtonText: this.$t("commons.cancel"),
            type: "warning",
          }
      )
          .then(() => {
            let ids = [],
                param = {};
            param.projectId = getCurrentProjectID();
            if (this.condition.selectAll) {
              param.unSelectIds = this.condition.unSelectIds;
              param.selectAll = true;
              param.queryTestPlanRequest = this.condition;
            } else {
              this.$refs.testPlanLitTable.selectRows.forEach((item) => {
                ids.push(item.id);
              });
            }
            param.ids = ids;
            this.cardLoading = batchDeletePlan(param).then(() => {
              this.refresh();
              this.$success(this.$t("commons.delete_success"));
            });
          })
          .catch(() => {
            this.$info(this.$t("commons.delete_cancel"));
          });
    },
    handleBatchSwitch() {
      let param = [];
      let size = 0;
      if (this.condition.selectAll) {
        testPlanGetEnableScheduleCount(this.condition).then((response) => {
          size = response.data;
          this.$refs.scheduleBatchSwitch.open(
              param,
              size,
              this.condition.selectAll,
              this.condition
          );
        });
      } else {
        this.$refs.testPlanLitTable.selectRows.forEach((item) => {
          if (
              item.scheduleStatus !== null &&
              item.scheduleStatus !== "NOTSET"
          ) {
            param.push(item.scheduleId);
            size++;
          }
        });
        this.$refs.scheduleBatchSwitch.open(
            param,
            size,
            this.condition.selectAll,
            this.condition
        );
      }
    },
    handleBatchExecute() {
      this.showExecute = true;
    },
    handleRunBatch() {
      this.showExecute = false;
      let mode = this.batchExecuteType;
      let param = {mode};
      const ids = [];
      if (this.condition.selectAll) {
        param.isAll = true;
        param.queryTestPlanRequest = this.condition;
      } else {
        this.$refs.testPlanLitTable.selectRows.forEach((item) => {
          ids.push(item.id);
        });
      }
      param.testPlanId = this.currentPlanId;
      param.projectId = getCurrentProjectID();
      param.userId = getCurrentUserId();
      param.requestOriginator = "TEST_PLAN";
      param.testPlanIds = ids;
      testPlanRunBatch(param).then(() => {
        this.$refs.taskCenter.open();
        this.$success(this.$t("commons.run_success"));
      });
    },
    closeExecute() {
      this.showExecute = false;
    },
    statusChange(data) {
      if (!hasPermission("PROJECT_TRACK_PLAN:READ+EDIT")) {
        return;
      }
      let oldStatus = data.item.status;
      let newStatus = data.status;
      let param = {};
      param.id = data.item.id;
      param.status = newStatus;
      testPlanEdit(param).then(() => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id == param.id) {
            //  手动修改当前状态后，前端结束时间先用当前时间，等刷新后变成后台数据（相等）
            if (oldStatus !== "Completed" && newStatus === "Completed") {
              this.tableData[i].actualEndTime = Date.now();
            } //  非完成->已完成，结束时间=null
            else if (oldStatus !== "Underway" && newStatus === "Underway") {
              this.tableData[i].actualStartTime = Date.now();
              this.tableData[i].actualEndTime = "";
            } //  非进行中->进行中，结束时间=null
            else if (oldStatus !== "Prepare" && newStatus === "Prepare") {
              this.tableData[i].actualStartTime = this.tableData[
                  i
                  ].actualEndTime = "";
            } //  非未开始->未开始，结束时间=null
            this.tableData[i].status = newStatus;
            break;
          }
        }
      });
    },
    scheduleChange(row) {
      let param = {};
      let message = this.$t(
          "api_test.home_page.running_task_list.confirm.close_title"
      );
      param.taskID = row.scheduleId;
      param.enable = row.scheduleOpen;
      if (row.scheduleOpen) {
        message = this.$t(
            "api_test.home_page.running_task_list.confirm.open_title"
        );
      }

      operationConfirm(
          this,
          message,
          () => {
            this.cardLoading = true;
            testPlanUpdateScheduleEnable(param).then((response) => {
              this.cardLoading = false;
              if (row.scheduleOpen) {
                row.scheduleStatus = "OPEN";
                row.scheduleCorn = response.data.value;
                row.scheduleExecuteTime = response.data.scheduleExecuteTime;
              } else {
                row.scheduleStatus = "SHUT";
              }
              this.$success(this.$t("commons.save_success"));
            });
          },
          () => {
            row.scheduleOpen = !row.scheduleOpen;
          }
      );
    },
    handleDelete(testPlan) {
      this.enableDeleteTip = testPlan.status === "Underway" ? true : false;
      this.$refs.deleteConfirm.open(testPlan);
    },
    _handleDelete(testPlan) {
      testPlanDelete(testPlan.id).then(() => {
        this.initTableData();
        this.$success(this.$t("commons.delete_success"));
      });
    },
    intoPlan(row, column, event) {
      if (column.label !== this.$t("commons.operating")) {
        this.$router.push("/track/plan/view/" + row.id);
      }
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.saveSortField(this.tableHeaderKey, this.condition.orders);
      this.initTableData();
    },
    openReport(plan) {
      this.$refs.testCaseReportView.open(plan);
    },
    async scheduleTask(row) {
      this.haveUICase = false;
      this.haveOtherExecCase = false;

      row.redirectFrom = "testPlan";
      this.currentPlanId = row.id;

      this.haveUICase = row.testPlanUiScenarioCount > 0;
      let haveApiCase = row.testPlanApiCaseCount > 0;
      let haveScenarioCase = row.testPlanApiScenarioCount > 0;
      if (haveApiCase || haveScenarioCase) {
        this.haveOtherExecCase = true;
      }

      this.$refs.scheduleMaintain.open(row);
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
    },
    handleCommand(cmd, row) {
      switch (cmd) {
        case "delete":
          this.handleDelete(row);
          break;
        case "schedule_task":
          this.scheduleTask(row);
          break;
      }
    },
    handleCopy(row) {
      this.cardLoading = true;
      testPlanCopy(row.id).then(() => {
        this.cardLoading = false;
        this.initTableData();
      });
    },
    handleRun(row) {
      this.haveUICase = false;
      this.haveOtherExecCase = false;

      this.currentPlanId = row.id;
      //组件里的id不会及时变动，会影响到后续的操作，所以需要在下一个tick里执行
      this.$nextTick(() => {
        let haveApiCase = row.testPlanApiCaseCount > 0;
        let haveScenarioCase = row.testPlanApiScenarioCount > 0;
        let haveLoadCase = row.testPlanLoadCaseCount > 0;
        this.haveUICase = row.testPlanUiScenarioCount > 0;
        if (!this.haveUICase && !haveApiCase && !haveScenarioCase && haveLoadCase) {
          //只有性能测试，则直接执行
          this.$refs.runMode.handleCommand("run");
        } else if (haveApiCase || haveScenarioCase) {
          // 有接口或场景测试, 需选择资源池
          this.haveOtherExecCase = true;
          this.$refs.runMode.open("API", row.runModeConfig);
        } else if (this.haveUICase) {
          // UI测试不需要选择资源池
          this.$refs.runMode.open("", row.runModeConfig);
        } else {
          //没有可执行的资源，则直接跳转到计划里
          this.$router.push("/track/plan/view/" + row.id);
        }
      });
    },
    _handleRun(config) {
      let defaultPlanEnvMap = config.testPlanDefaultEnvMap;
      let {
        mode,
        reportType,
        onSampleError,
        runWithinResourcePool,
        resourcePoolId,
        envMap,
        environmentType,
        environmentGroupId,
        browser,
        headlessEnabled,
        retryEnable,
        retryNum,
        triggerMode,
      } = config;
      let param = {
        mode,
        reportType,
        onSampleError,
        runWithinResourcePool,
        resourcePoolId,
        envMap,
      };
      param.testPlanId = this.currentPlanId;
      param.projectId = getCurrentProjectID();
      param.userId = getCurrentUserId();
      param.triggerMode = "MANUAL";
      param.environmentType = environmentType;
      param.environmentGroupId = environmentGroupId;
      param.testPlanDefaultEnvMap = defaultPlanEnvMap;
      param.requestOriginator = "TEST_PLAN";
      param.retryEnable = config.retryEnable;
      param.retryNum = config.retryNum;
      param.browser = config.browser;
      param.headlessEnabled = config.headlessEnabled;
      if (config.executionWay === "runAndSave") {
        param.executionWay = "RUN_SAVE";
        this.$refs.taskCenter.open();
        this.cardLoading = true;
        testPlanRunSave(param).then(() => {
          this.cardLoading = false;
          this.initTableData();
          this.$success(this.$t("commons.run_success"));
        }).catch(() => {
          this.cardLoading = false;
        });
      } else if (config.executionWay === "save") {
        param.executionWay = "SAVE";
        this.cardLoading = true;
        testPlanEditRunConfig(param).then(() => {
          this.cardLoading = false;
          this.initTableData();
          this.$success(this.$t("commons.save_success"));
        }).catch(() => {
          this.cardLoading = false;
        });
      } else {
        param.executionWay = "RUN";
        this.$refs.taskCenter.open();
        this.cardLoading = true;
        testPlanRun(param).then(() => {
          this.cardLoading = false;
          this.$success(this.$t("commons.run_success"));
        }).catch(() => {
          this.cardLoading = false;
        });
      }
    },
    saveFollow(row) {
      if (row.showFollow) {
        row.showFollow = false;
        for (let i = 0; i < row.follows.length; i++) {
          if (row.follows[i] === this.currentUser().id) {
            row.follows.splice(i, 1);
            break;
          }
        }
        testPlanEditFollows(row.id, row.follows).then(() => {
          this.$success(this.$t("commons.cancel_follow_success"));
        });
        return;
      }
      if (!row.showFollow) {
        row.showFollow = true;
        if (!row.follows) {
          row.follows = [];
        }
        row.follows.push(this.currentUser().id);
        testPlanEditFollows(row.id, row.follows).then(() => {
          this.$success(this.$t("commons.follow_success"));
        });
        return;
      }
    },
    // haveUIScenario() {
    //   if (hasLicense()) {
    //     return new Promise((resolve) => {
    //       testPlanHaveUiCase(this.currentPlanId).then((r) => {
    //         this.haveUICase = r.data;
    //         resolve();
    //       });
    //     });
    //   } else {
    //     return new Promise((resolve) => resolve());
    //   }
    // },
  },
};
</script>

<style scoped>
.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.el-table {
  cursor: pointer;
}

.schedule-btn :deep(.el-button) {
  margin-left: 10px;
  color: #85888e;
  border-color: #85888e;
  border-width: thin;
}

.scenario-ext-btn {
  margin-left: 10px;
}

.table-card :deep(.operator-btn-group ) {
  margin-left: 10px;
}
</style>

<style>
.plan-table div.el-table__empty-block {
  width: 80% !important;
}
</style>
