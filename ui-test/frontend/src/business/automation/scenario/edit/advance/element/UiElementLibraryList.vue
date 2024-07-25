<template>

  <el-card class="table-card">

    <template v-slot:header>
      <ms-table-header
        :condition.sync="condition"
        :create-tip="$t('permission.project_ui_element.create')"
        @search="getElement(true)"
        @create="handleCreate"/>
    </template>

    <ms-table
      v-loading="page.result.loading"
      v-el-table-infinite-scroll="getElement"
      operator-width="15px"
      :data="tableData"
      :operators="operators"
      :show-select-all="false"
      :operator-fixed="false"
      :enable-selection="false"
      :row-click-style="true"
      screen-height="500px"
      @handleRowClick="handleRowClick">

      <el-table-column
        prop="num"
        sortable
        :width="110"
        :label="$t('commons.id')"/>

      <el-table-column
          :label="$t('ui.element_locator_type')"
          :width="110"
        prop="locationType"/>

      <el-table-column
          :label="$t('commons.name')"
          :width="220"
          prop="name"/>

      <el-table-column
          :label="$t('test_track.case.module')"
          :width="330"
          prop="modulePath"/>

      <el-table-column
          :label="$t('ui.element_locator')"
          :width="500"
          prop="location"/>

    </ms-table>

    <div v-if="isLastPage" style="text-align: center">{{$t('test_track.review_view.last_page')}}</div>
    <div style="text-align: center; margin-bottom: 10px;">{{$t('test_track.total_size', [page.total])}}</div>

    <ui-element-edit
      :module-tree="moduleTree"
      @save="handleEditSave"
      ref="uiElementEdit"/>

  </el-card>


</template>

<script>
import {getUiElements} from "@/business/network/ui-element";
import elTableInfiniteScroll from 'el-table-infinite-scroll';
import {getPageInfo} from "metersphere-frontend/src/utils/tableUtils";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import {getElementModules} from "@/business/network/ui-element-module";
import {buildTree} from "@/business/network/NodeTree";
import UiElementEdit from "@/business/element/components/UiElementEdit";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {useCommandStore} from "@/store";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission";

const commandStore = new useCommandStore();

export default {
  name: "UiElementLibraryList",
  components: {MsTable, UiElementEdit, MsTableHeader},
  directives: {
    'el-table-infinite-scroll': elTableInfiniteScroll
  },
  props: {},
  data() {
    return {
      tableData: [],
      tableHeight: null,
      condition: {},
      result: {},
      page: getPageInfo(),
      isLastPage: false,
      moduleTree: [],
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit
        }
      ],
      commandStore: commandStore
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  created() {
    this.$EventBus.$on("refreshElementLibrary", this.getElement);
    this.getElement(true);
    this.getModuleTree();
  },
  beforeDestroy() {
    this.$EventBus.$off("refreshElementLibrary", this.getElement);
  },
  watch: {
    'commandStore.uiElementLibraryModuleIds'() {
      this.handleElementLibrary();
    },
    'commandStore.uiElementLibraryElements': {
      handler(newValue, oldValue) {
        this.handleElementLibrary();
      },
      deep: true
    },
    'commandStore.selectCommand'() {
      if (this.condition.name && this.condition.name !== '') {
        this.condition.name = null;
        this.$nextTick(() => {
          this.handleElementLibrary();
        });
      }
    },
  },
  methods: {
    handleElementLibrary() {
      this.condition.moduleIds = commandStore.uiElementLibraryModuleIds;
      let elementId = commandStore.uiElementLibraryElements;
      this.condition.ids = []
      if (elementId) {
        this.condition.ids = [elementId]
      }
      this.page.pageSize = 10;
      this.getElement(true);
    },
    getElement(isInit) {
      if (!hasPermission('PROJECT_UI_ELEMENT:READ')) {
        return;
      }
      this.condition.nodeIds = this.selectNodeIds || [];
      this.condition.versionId = this.currentVersion || null;
      this.condition.projectId = this.projectId;
      this.page.pageSize = 10;
      if (!isInit) {
        this.page.currentPage++;
        if (this.isLastPage) {
          return;
        }
      } else {
        this.page.currentPage = 1;
        this.isLastPage = false;
      }
      this.page.result = getUiElements(this.page, this.condition).then(data => {
        this.page.total = data.data.itemCount;
        if (isInit) {
          this.tableData = data.data.listObject;
        } else if (!this.isLastPage) {
          this.tableData.push(...data.data.listObject);
        }
        this.isLastPage = this.tableData.length >= this.page.total;
      });
    },
    getModuleTree() {
      getElementModules(this.projectId).then(data => {
        if (data) {
          this.moduleTree = data.data;
          this.moduleTree.forEach(node => {
            buildTree(node, {path: ''});
          });
        }
      });
    },
    handleRowClick(row) {
      commandStore.librarySelectElement = row;
    },
    handleCreate() {
      this.$refs.uiElementEdit.open();
    },
    handleEdit(data) {
      this.$refs.uiElementEdit.open(data);
    },
    handleEditSave(data) {
      let isCreate = true;
      this.tableData.forEach(item => {
        if (item.id === data.id) {
          Object.assign(item, data);
          isCreate = false;
        }
      });
      if (isCreate) {
        this.getElement(true);
      }
    }
  }
}
</script>

<style scoped>

</style>
