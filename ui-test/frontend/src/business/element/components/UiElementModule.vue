<template>
  <div>
    <slot name="header"></slot>

    <ms-node-tree
        :is-display="getIsRelevance"
        v-loading="result.loading"
        :tree-nodes="data"
        :allLabel="$t('ui.all_element')"
        :type="isReadOnly ? 'view' : 'edit'"
        :delete-permission="['PROJECT_UI_ELEMENT:READ+DELETE']"
        :add-permission="['PROJECT_UI_ELEMENT:READ+CREATE']"
        :update-permission="['PROJECT_UI_ELEMENT:READ+EDIT']"
        :default-label="'未规划元素'"
        local-suffix="ui_element"
        @add="add"
        @edit="edit"
        @drag="drag"
        @remove="remove"
        @refresh="list"
        @filter="filter"
        @nodeSelectEvent="nodeChange"
        :showRemoveTip="false"
        ref="nodeTree"
        class="element-node-tree"
    >
      <template v-slot:header>
        <ms-search-bar
            :show-operator="showOperator"
            :condition="condition"
            :commands="operators"
            class="element-search"
        />
      </template>
    </ms-node-tree>
    <ui-element-import
        ref="UiElementImport"
        @refreshTree="refreshTree"
        @refreshTable="refreshTable"
    ></ui-element-import>
  </div>
</template>

<script>
import SelectMenu from "@/business/element/components/common/SelectMenu";
import MsAddBasisScenario from "./AddBasisScenario";
import MsNodeTree from "./NodeTree";
import {buildTree} from "../../network/NodeTree";
import ModuleTrashButton from "./module/ModuleTrashButton";
import MsSearchBar from "@/business/element/components/common/search/MsSearchBar";
import UiElementImport from "./UiElementImport";
import {checkModulesReference} from "../../network/ui-element";
import {
  addElementModule,
  deleteElementModule,
  dragElementModule,
  editElementModule,
  getElementModulesOpt,
  posElementModule,
} from "../../network/ui-element-module";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";


export default {
  name: "UiElementModule",
  components: {
    MsSearchBar,
    ModuleTrashButton,
    MsNodeTree,
    MsAddBasisScenario,
    SelectMenu,
    UiElementImport,
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      },
    },
    showOperator: Boolean,
    relevanceProjectId: String,
    pageSource: String,
  },
  computed: {
    isRelevanceModel() {
      return this.relevanceProjectId ? true : false;
    },
    projectId() {
      return getCurrentProjectID();
    },
    getIsRelevance() {
      if (this.pageSource !== "element") {
        return this.openType;
      } else {
        return "element";
      }
    },
  },
  created() {
    this.$EventBus.$on("callBackSelect", this.callBackSelect);
    this.$EventBus.$on("elementUiConditionBus", (param)=>{
      this.param = param;
    })
    this.$EventBus.$on("elementUiConditions", (param)=>{
      this.elementParam = param;
      this.list();
    })
  },
  beforeDestroy() {
    this.$EventBus.$off("callBackSelect", this.callBackSelect);
    this.$EventBus.$off("elementUiConditionBus", (param)=>{
      this.param = param;
    })
    this.$EventBus.$off("elementUiConditions", (param)=>{
      this.elementParam = param;
      this.list();
    })
  },
  data() {
    return {
      selectAll: false,
      openType: "relevance",
      result: {},
      condition: {
        filterText: "",
        trashEnable: false,
      },
      data: [],
      currentModule: undefined,
      operators: [
        {
          label: this.$t("api_test.api_import.label"),
          callback: this.handleImport,
          permissions: ["PROJECT_UI_SCENARIO:READ+IMPORT_SCENARIO"],
        },
        {
          label: this.$t("commons.export"),
          callback: this.handleExport,
          permissions: ["PROJECT_UI_SCENARIO:READ+IMPORT_SCENARIO"],
        },
      ],
      selectElementsId: [],
      nodeIds: [],
      elementParam:{}
    };
  },
  mounted() {
    this.list();
  },
  watch: {
    "condition.filterText"() {
      this.filter();
    },
    "condition.trashEnable"() {
      this.$emit("enableTrash", this.condition.trashEnable);
    },
    relevanceProjectId() {
      this.list();
    },
  },
  methods: {
    refreshTree() {
      this.list();
    },
    refreshTable() {
      this.$emit("refreshTable");
    },
    /**
     * selectAll 是否全选
     */
    callBackSelect(selection, selectAll) {
      if (selectAll) {
        //全选
        this.selectAll = true;
        return;
      }
      this.selectAll = false;
      this.selectElementsId = selection.map((v) => v.id) || [];
    },
    handleExport() {
      if (!this.selectAll) {
        if (!this.selectElementsId || this.selectElementsId.length <= 0) {
          this.$warning(this.$t("ui.check_element"));
          return;
        }
      }
      let data = {
        ids: this.selectElementsId,
        selectAll: this.selectAll,
        projectId: this.projectId,
        name: this.param,
        nodeIds: this.nodeIds,
      };
      this.$emit('handleExport', data);
    },
    handleImport() {
      this.$refs.UiElementImport.open();
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    list() {
      getElementModulesOpt(this.projectId, this.elementParam).then((data) => {
        if (data) {
          this.data = data.data;
          this.data.forEach((node) => {
            buildTree(node, {path: ""});
          });
          this.$emit("setModuleOptions", this.data);
          this.$emit("setNodeTree", this.data);
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.filter(this.condition.filterText);
          }
        }
      });
    },
    edit(param) {
      param.projectId = this.projectId;
      editElementModule(param)
          .then(() => {
            if (param.id) {
              this.refreshRecentlyModule(param.id);
            }
            this.$success(this.$t("commons.save_success"));
          })
          .catch((err) => {
            if (err.response && err.response.data) {
              // this.$error(err.response.data.message || err.response.data);
            } else {
              this.$error(err.message);
            }
          })
          .finally(() => {
            this.list();
          });
    },
    add(param) {
      param.projectId = this.projectId;
      addElementModule(param)
          .then((res) => {
            if (res.data) {
              this.refreshRecentlyModule(res.data.data);
            }
            this.$success(this.$t("commons.save_success"));
          })
          .catch((err) => {
            if (err.response && err.response.data) {
              // this.$error(err.response.data.message || err.response.data);
            } else {
              this.$error(err.message);
            }
          })
          .finally(() => {
            this.list();
          });
    },
    refreshRecentlyModule(mId) {
      if (mId) {
        localStorage.setItem("RECENTLY_ELEMENT_MODULE", mId);
      }
    },
    clearRencentlyModule(nodeIds) {
      let record = localStorage.getItem("RECENTLY_ELEMENT_MODULE");
      if (record && nodeIds && nodeIds.find((id) => id === record)) {
        localStorage.removeItem("RECENTLY_ELEMENT_MODULE");
      }
    },
    async remove(nodeIds, data) {
      let param = {
        moduleIds: nodeIds,
      };
      //校验是否存在引用关系
      let tipName =
          this.$t("ui.confrim_del_node") + data.label + this.$t("ui.and_all_sub_node");
      let res = await checkModulesReference(this.projectId, param);
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
      //alert 提示
      //删除
      this.$alert(tipName, "", {
        confirmButtonText: this.$t("commons.confirm"),
        callback: (action) => {
          if (action === "confirm") {
            deleteElementModule(nodeIds).then(() => {
              this.list();
              this.$emit("refreshTable");
              this.clearRencentlyModule(nodeIds);
            });
          }
        },
      });
    },
    drag(param, list) {
      dragElementModule(param).then(() => {
        posElementModule(list).then(() => {
          this.list();
        });
      }).finally(() => {
        this.list();
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentModule = node.data;
      this.condition.trashEnable = false;
      this.selectElementsId = [];
      this.nodeIds = nodeIds;
      if (node.data.id === "root") {
        this.$emit("nodeSelectEvent", node, [], pNodes);
      } else {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      }
    },
    addScenario() {
      if (!this.projectId) {
        this.$warning(this.$t("commons.check_project_tip"));
        return;
      }
      this.$refs.basisScenario.open(this.currentModule);
    },
    enableTrash() {
      this.condition.trashEnable = true;
      this.$emit("enableTrash", this.condition.trashEnable);
    },
  },
};
</script>

<style scoped>
.node-tree {
  margin-top: 15px;
  margin-bottom: 15px;
}

.ms-el-input {
  height: 25px;
  line-height: 25px;
}

.custom-tree-node {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.father .child {
  display: none;
}

.father:hover .child {
  display: block;
}

.node-title {
  width: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0 5px;
  overflow: hidden;
}

.node-operate > i {
  color: #409eff;
  margin: 0 5px;
}

:deep(.el-tree-node__content) {
  height: 33px;
}

.ms-api-buttion {
  width: 30px;
}

.element-node-tree {
  width: 101%;
  /* min-width: 290px; */
}

:deep(.element-search.ms-search-bar) {
  width: 98%;
}
</style>
