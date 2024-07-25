<template>
  <div>
    <slot name="header"></slot>

    <ms-node-tree
      :is-display="getIsRelevance"
      v-loading="result.loading"
      :tree-nodes="data"
      :allLabel="$t('ui.all_scenario')"
      :type="isReadOnly ? 'view' : 'edit'"
      :delete-permission="['PROJECT_UI_SCENARIO:READ+DELETE']"
      :add-permission="['PROJECT_UI_SCENARIO:READ+CREATE']"
      :update-permission="['PROJECT_UI_SCENARIO:READ+EDIT']"
      :default-label="'未规划场景'"
      :show-case-num="showCaseNum"
      :hide-opretor="isTrashData"
      local-suffix="ui_scenario"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      @filter="filter"
      @nodeSelectEvent="nodeChange"
      class="element-node-tree"
      ref="nodeTree">

      <template v-slot:header>
        <ms-search-bar
          :show-operator="showOperator && !isTrashData"
          :condition="condition"
          :commands="operators"/>
        <module-trash-button v-if="!isReadOnly && !isTrashData" :condition="condition" :exe="enableTrash"
                             :total='total'/>
      </template>

    </ms-node-tree>
    <ms-add-basis-scenario
      @saveAsEdit="saveAsEdit"
      @refresh="refresh"
      ref="basisScenario"/>


    <ui-import ref="apiImport" :moduleOptions="data" @refreshAll="$emit('refreshAll')"/>
  </div>

</template>

<script>
import SelectMenu from "@/business/element/components/common/SelectMenu";
import MsAddBasisScenario from "@/business/automation/scenario/UiAddBasisScenario";
import MsNodeTree from "@/business/element/components/NodeTree";
import {buildTree} from "@/business/network/NodeTree";
import ModuleTrashButton from "@/business/element/components/module/ModuleTrashButton";
import MsSearchBar from "@/business/element/components/common/search/MsSearchBar";
import {
  addScenarioModule,
  deleteScenarioModule,
  dragScenarioModule,
  editScenarioModule,
  getScenarioModules, getScenarioModulesOpt,
  posScenarioModule
} from "@/business/network/ui-scenario-module";
import UiImport from "./UiScenarioImport";
import {getUiScenarioModuleList, getUiScenarioModuleListPlan} from "@/business/automation/ui-automation";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: 'UiScenarioModule',
  components: {
    MsSearchBar,
    ModuleTrashButton,
    MsNodeTree,
    MsAddBasisScenario,
    SelectMenu,
    UiImport,
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showOperator: Boolean,
    relevanceProjectId: String,
    pageSource: String,
    total: Number,
    isTrashData: Boolean,
    planId: String,
    showCaseNum: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    isPlanModel() {
      return this.planId ? true : false;
    },
    isRelevanceModel() {
      return this.relevanceProjectId ? true : false;
    },
    projectId() {
      return getCurrentProjectID();
    },
    getIsRelevance() {
      if (this.pageSource !== 'scenario') {
        return this.openType;
      } else {
        return "scenario";
      }
    }
  },
  data() {
    return {
      openType: 'relevance',
      result: {},
      condition: {
        filterText: "",
        trashEnable: false
      },
      data: [],
      currentModule: undefined,
      param: {},
      operators: [
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport,
          permissions: ['PROJECT_UI_SCENARIO:READ+IMPORT_SCENARIO']
        },
        {
          label: this.$t('report.export'),
          callback: this.exportSide,
          permissions: ['PROJECT_UI_SCENARIO:READ+EXPORT_SCENARIO']
        }
      ]
    }
  },
  mounted() {
    this.list();
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
    'condition.trashEnable'() {
      this.param = {};
      this.$emit('enableTrash', this.condition.trashEnable);
    },
    relevanceProjectId() {
      this.list(this.relevanceProjectId);
    },
    isTrashData() {
      this.condition.trashEnable = this.isTrashData;
      this.param = {};
    }
  },
  created(){
    this.$EventBus.$on("caseConditionBus", (param)=>{
      this.param = param;
    })

  },
  beforeDestroy() {
    this.$EventBus.$off("caseConditionBus", (param)=>{
      this.param = param;
    })
  },
  methods: {
    saveAsEdit(data) {
      data.type = "add";
      this.$emit('saveAsEdit', data);
    },
    refresh() {
      this.$emit("refreshTable");
    },
    handleImport() {
      if (this.projectId) {
        getUiScenarioModuleList(this.projectId).then(response => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
            this.data.forEach(node => {
              node.name = node.name === '未规划场景' ? this.$t('ui.unplanned_scenario') : node.name;
              node.label = node.label === '未规划场景' ? this.$t('ui.unplanned_scenario') : node.label;
              buildTree(node, {path: ''});
            });
          }
        })
        this.$refs.apiImport.open(this.currentModule);
      }
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    list(projectId) {
      if (this.isPlanModel) {
        getUiScenarioModuleListPlan(this.planId).then(response =>{
          if (response.data != undefined && response.data != null) {
            this.data = response.data.data;
            this.data.forEach(node => {
              buildTree(node, {path: ''});
            });
          }
        })
      } else {
        getScenarioModulesOpt(projectId ? projectId : this.projectId, this.isTrashData, this.param).then(data => {
          if (data) {
            this.data = data.data;
            this.data.forEach(node => {
              buildTree(node, {path: ''});
            });
            this.$emit('setModuleOptions', this.data);
            this.$emit('setNodeTree', this.data);
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
          }
        });
      }
    },
    edit(param) {
      param.projectId = this.projectId;
      editScenarioModule(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.list();
      });
      this.refresh()
    },
    add(param) {
      param.projectId = this.projectId;
      addScenarioModule(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.list();
      });
      //解决添加失败 节点仍然存在的问题 此处进行刷新
      this.refresh();
    },
    remove(nodeIds) {
      deleteScenarioModule(nodeIds).then(() => {
        this.list();
        this.refresh();
        this.$emit("nodeSelectEvent")
      });
    },
    drag(param, list) {
      dragScenarioModule(param).then(() => {
        posScenarioModule(list).then(() => {
          this.list();
        });
      });
      this.refresh();
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentModule = node.data;
      if (node.data.id === 'root') {
        this.$emit("nodeSelectEvent", node, [], pNodes);
      } else {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      }
    },
    addScenario() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.basisScenario.open(this.currentModule);
    },
    enableTrash() {
      this.condition.trashEnable = true;
      this.$emit('enableTrash', this.condition.trashEnable);
    },
    exportSide() {
      this.$emit('exportSide', this.data);
    }
  }
}
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
  width: 100%;
  /* min-width: 290px; */
}

:deep(.element-node-tree .recycle .el-col.el-col-3) {
  text-align: center;
}

</style>
