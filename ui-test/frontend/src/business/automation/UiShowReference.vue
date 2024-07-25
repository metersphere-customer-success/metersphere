<template>
  <el-dialog
    :visible.sync="isVisible"
    :modal="false"
    class="advanced-item-value"
    width="43%"
  >
    <el-tabs
      tab-position="top"
      style="width: 100%; height: 40vh"
      v-model="activeName"
      @tab-click="handleClick"
    >
      <el-tab-pane :label="$t('ui.scenario_ref_label')" name="showScenario">
        <el-table
          :data="tableData"
          style="width: 100%"
          @filter-change="filterHandler"
          @sort-change="changeSort"
          height="35vh"
        >
          <el-table-column prop="id" label="ID" sortable width="80">
          </el-table-column>
          <el-table-column prop="scenarioName" label="场景名称" width="200">
            <template v-slot:default="{ row }">
              <span @click="gotoTab(row)" style="cursor: pointer">{{
                row.scenarioName
              }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="workspace" label="所属工作空间" width="200">
          </el-table-column>
          <el-table-column
            prop="project"
            label="所属项目"
            :filters="projectFilters"
            column-key="project"
            width="200"
          >
          </el-table-column>
          <!-- <el-table-column prop="dateStr" label="创建时间" sortable width="200">
            <template v-slot:default="scope">
              <span>{{
                parseInt(scope.row.dateStr) | timestampFormatDate
              }}</span>
            </template>
          </el-table-column> -->
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="$t('ui.command_ref_label')" name="showCustomCommand">
        <el-table
          :data="tableData"
          style="width: 100%"
          @filter-change="filterHandler"
          @sort-change="changeSort"
          height="35vh"
        >
          <el-table-column
            prop="id"
            label="ID"
            sortable
            width="80"
          ></el-table-column>
          <el-table-column prop="scenarioName" label="指令名称" width="200">
            <template v-slot:default="{ row }">
              <span @click="gotoTab(row)" style="cursor: pointer">{{
                row.scenarioName
              }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="workspace" label="所属工作空间" width="200">
          </el-table-column>
          <el-table-column
            prop="project"
            label="所属项目"
            :filters="projectFilters"
            column-key="project"
            width="200"
          >
          </el-table-column>
          <!-- <el-table-column prop="dateStr" label="创建时间" sortable width="200">
            <template v-slot:default="scope">
              <span>{{
                parseInt(scope.row.dateStr) | timestampFormatDate
              }}</span>
            </template>
          </el-table-column> -->
        </el-table>
      </el-tab-pane>
      <el-tab-pane
        :label="$t('ui.test_plan_ref_label')"
        name="showTestPlan"
        v-if="scenarioType === 'scenario'"
      >
        <el-table
          :data="tableData"
          style="width: 100%"
          @filter-change="filterHandler"
          @sort-change="changeSort"
          height="35vh"
        >
          <el-table-column prop="scenarioName" label="测试计划名称" width="200">
            <template v-slot:default="{ row }">
              <span @click="gotoTestPlanTab(row)" style="cursor: pointer">{{
                row.scenarioName
              }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="workspace" label="所属工作空间" width="200">
          </el-table-column>
          <el-table-column
            prop="project"
            label="所属项目"
            :filters="projectFilters"
            column-key="project"
            width="200"
          >
          </el-table-column>
          <!-- <el-table-column prop="dateStr" label="创建时间" sortable width="200">
            <template v-slot:default="scope">
              <span>{{
                parseInt(scope.row.dateStr) | timestampFormatDate
              }}</span>
            </template>
          </el-table-column> -->
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <ms-table-pagination
      :change="search"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"
    />
  </el-dialog>
</template>
<script>

import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";
export default {
  name: "UiShowReference",
  data() {
    return {
      isVisible: false,
      scenarioType: "scenario",
      tabScenarioType: "scenario",
      tableData: [],
      selectDataRange: "all",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      activeName: "showScenario",
      scenarioId: "",
      workspaceFilters: [],
      projectFilters: [],
      select: {},
      source: {},
    };
  },
  components: {
    MsTablePagination
  },
  watch: {
    activeName(o) {
      if (o) {
        this.init();
        this.tabScenarioType =
          o === "showScenario"
            ? "scenario"
            : o === "showCustomCommand"
            ? "customCommand"
            : "testPlan";
        this.search();
      }
    },
  },
  methods: {
    getUiScenario(scenarioId) {
      return new Promise((resolve) => {
        this.result = this.$get(
          "/ui/automation/get/" + scenarioId).then((response) => {
            if (response.data) {
              this.source = response.data;
              resolve();
            }
          });
      });
    },
    gotoTab(row) {
      let targetId = row.targetId;
      let automationData = this.$router.resolve({
        name: "UiAutomationWithQuery",
        params: {
          redirectID: getUUID(),
          dataType: this.tabScenarioType,
          dataSelectRange: "edit:" + targetId,
          projectId: row.projectId,
          workspaceId: row.workspaceId,
        },
      });
      window.open(automationData.href, "_blank");
    },
    gotoTestPlanTab(row) {
      let automationData = this.$router.resolve({
        path: "/track/plan/view/" + row.targetId,
        query: {
          workspaceId: row.workspaceId,
          projectId: row.projectId,
          charType: "ui",
        },
      });
      window.open(automationData.href, "_blank");
    },
    getUserWorkspaceList() {
      let currentWorkspace = getCurrentWorkspaceId();
      let data = [];
      if (currentWorkspace) {
        data.push(currentWorkspace);
      }
      this.workspaceFilters = data.map((e) => {
        return { text: e.name, value: e.id };
      });
      // this.$get("/workspace/list/userworkspace", (res) => {
      //   let data = res.data ? res.data : [];
      //   this.workspaceFilters = data.map((e) => {
      //     return { text: e.name, value: e.id };
      //   });
      // });
    },
    getUserProjectList(workspaceId) {
      let subPath = workspaceId ? `/${workspaceId}` : "";
      this.$post(
        "/project/list/related",
        {
          userId: getCurrentUserId(),
          workspaceId: getCurrentWorkspaceId(),
        }).then((res) => {
          let data = res.data ? res.data : [];
          this.projectFilters = data.map((e) => {
            return { text: e.name, value: e.id };
          });
        });
    },
    /**
     * 操作方法
     */
    init() {
      this.select = {};
      this.currentPage = 1;
      this.pageSize = 10;
      this.total = 0;
      this.source = {};
      this.tableData = [];
    },
    postInit() {
      //获取 用户 工作空间列表
      this.getUserWorkspaceList();
      //获取 用户 项目列表
      this.getUserProjectList();
    },
    open(row, scenarioType) {
      this.activeName = "showScenario";
      this.tabScenarioType = "scenario";
      this.init();
      this.postInit();
      this.isVisible = true;
      this.scenarioType = scenarioType;
      this.scenarioId = row.id;
      this.search();
    },
    close() {
      this.isVisible = false;
    },
    search() {
      //根据类型检索对应数据
      this.select = {
        ...this.select,
        scenarioType: this.tabScenarioType,
        scenarioId: this.scenarioId,
      };
      let path = `/ui/automation/ref/list/${this.currentPage}/${this.pageSize}`;
      //获取测试计划引用关系
      if (this.activeName === "showTestPlan") {
        path = `/ui/automation/testPlanRef/list/${this.currentPage}/${this.pageSize}`;
      }
      this.$post(path, this.select).then((res) => {
        let data = res.data || [];
        this.total = data.itemCount || 0;
        this.tableData = data.listObject || [];
      });
    },

    handleClick(tab, event) {
      //
    },
    filterHandler(val) {
      this.select = { ...this.select, ...val };
      this.search();
    },
    changeSort(column) {
      if (column.prop === "date") {
        this.select = {
          ...this.select,
          timeOrder: this.convertOrderStatus(column.order),
        };
      }
      if (column.prop === "id") {
        this.select = {
          ...this.select,
          idOrder: this.convertOrderStatus(column.order),
        };
      }
      this.search();
    },
    convertOrderStatus(order) {
      if (!order) {
        return null;
      }
      return order === "descending" ? 1 : 0;
    },
  },
};
</script>
<style scoped>
:deep(.el-table__empty-block) {
  padding-right: 0 !important;
}
:deep(.el-dialog__body) {
  padding: 0 20px 30px 20px;
}
</style>
