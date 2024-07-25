<template>
  <div v-loading="data.loading">

    <el-input :placeholder="$t('commons.search_by_name')" @blur="search"
              @keyup.enter.native="search" class="search-input" size="small" v-model="condition.name"/>

    <ms-table v-loading="data.loading"
              class="basic-config"
              :total="total"
              :screen-height="height"
              :data="metadataArr"
              :condition="condition"
              :hidePopover="true"
              @refresh="getProjectFiles" ref="table">
      <ms-table-column
          prop="name"
          show-overflow-tooltip
          :min-width="120"
          :label="$t('load_test.file_name')">
      </ms-table-column>
      <ms-table-column
          sortable
          prop="type"
          :min-width="120"
          :filters="typeFilters"
          :label="$t('load_test.file_type')">
      </ms-table-column>

      <ms-table-column
          prop="description"
          :label="$t('group.description')">
      </ms-table-column>

      <ms-table-column
          prop="tags"
          min-width="60px"
          :show-overflow-tooltip=false
          :label="$t('commons.tag')">
        <template v-slot:default="scope">
          <el-tooltip class="item" effect="dark" placement="top">
            <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
            <div class="oneLine">
              <ms-tag v-for="(itemName,index)  in scope.row.tags"
                      :key="index"
                      :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 20"
                      :content="itemName"
                      type="success" effect="plain"
                      class="ms-tags"/>
            </div>
          </el-tooltip>
          <span/>
        </template>
      </ms-table-column>

      <ms-table-column
          sortable
          prop="createUser"
          :min-width="100"
          :label="$t('commons.create_user')">
      </ms-table-column>
      <ms-table-column
          sortable
          prop="updateUser"
          :min-width="100"
          :label="$t('ui.update_user')">
      </ms-table-column>

      <ms-table-column
          sortable
          :min-width="150"
          :label="$t('commons.update_time')"
          prop="updateTime">
        <template v-slot="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

    </ms-table>
    <ms-table-pagination
        :change="getProjectFiles"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
  </div>
</template>

<script>

import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {getAllTypeFileMeta, getFileMetaPages} from "@/api/file";
import MsTag from "metersphere-frontend/src/components/MsTag";

export default {
  name: "RelevanceFileList",
  components: {
    MsTablePagination,
    MsTableOperatorButton,
    MsTable,
    MsTableColumn,
    MsTag,
  },
  props: {
    selectNodeIds: {
      type: Array,
      default: () => []
    },
    selectFiles:{
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      loadFileVisible: false,
      result: {},
      data: {},
      tableLoading: false,
      cardLoading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      metadataArr: [],
      condition: {},
      projectId: getCurrentProjectID(),
      height: 'calc(100vh - 430px)',
      typeFilters: [],
      showView: "list",
    };
  },
  watch: {
    selectNodeIds() {
      if (this.condition.filters) {
        this.condition.filters.moduleIds = this.selectNodeIds;
      } else {
        this.condition.filters = {moduleIds: this.selectNodeIds};
      }
      this.getProjectFiles();
    }
  },
  methods: {
    changeList(pageSize, currentPage) {
      this.currentPage = currentPage;
      this.pageSize = pageSize;
      this.getProjectFiles();
    },
    change(value) {
      this.showView = value;
    },
    refreshModule() {
      this.$emit('refreshModule');
    },
    refreshList() {
      this.$emit("refreshList");
    },
    open(project) {
      if (project) {
        this.projectId = project.id;
      }
      this.loadFileVisible = true;
      this.getProjectFiles();
    },
    close() {
      this.loadFileVisible = false;
      this.selectIds.clear();
    },
    myFile() {
      if (!this.condition.filters) {
        this.condition.filters = {createUser: [getCurrentUserId()]};
      } else {
        this.condition.filters.createUser = [getCurrentUserId()];
      }
      this.condition.filters.moduleIds = [];
      this.getProjectFiles();
    },
    search() {
      this.getProjectFiles();
    },
    getProjectFiles(projectId) {
      let unSelectIds = [];
      this.selectFiles.forEach(item => {
        unSelectIds.push(item.id)
      });
      this.condition.unSelectIds = unSelectIds;
      this.tableLoading = getFileMetaPages(projectId ? projectId : this.projectId, this.currentPage, this.pageSize, this.condition).then(res => {
        let data = res.data;
        this.total = data.itemCount;
        this.metadataArr = data.listObject;
        this.metadataArr.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    getTagToolTips(tags) {
      try {
        let showTips = "";
        tags.forEach(item => {
          showTips += item + ",";
        })
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return "";
      }
    },
    getTypes() {
      this.typeFilters = [];
      getAllTypeFileMeta().then(res => {
        res.data.forEach(item => {
          this.typeFilters.push({text: item, value: item});
        })
      });
    },
    moduleChange(ids) {
      if (!this.condition.filters) {
        this.condition.filters = {moduleIds: ids};
      } else {
        this.condition.filters.moduleIds = ids;
      }
      this.condition.filters.createUser = [];
      this.getProjectFiles();
    },
    getSelectRows() {
      return this.$refs.table.getSelectRows();
    },
    clearCreateUser() {
      if (this.condition.filters) {
        this.condition.filters.createUser = [];
      }
    }
  },
  created() {
    this.getTypes();
    this.getProjectFiles();
  },
}
</script>

<style scoped>
.search-input {
  float: right;
  width: 250px;
  margin-top: 10px;
  margin-bottom: 20px;
  margin-right: 20px;
}
.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>