<template>
  <ms-container v-if="hasElementPermission">

    <ms-aside-container width="309px" class="element-aside" pageKey="UI_ELEMENT">
      <ui-element-module
          page-source="element"
          :show-operator="true"
          @nodeSelectEvent="nodeChange"
          @refreshTable="refreshTable"
          @setModuleOptions="setModuleOptions"
          @setNodeTree="setNodeTree"
          @handleExport="handleExport"
          ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <ui-element-list
          :module-tree="nodeTree"
          :module-options="moduleOptions"
          :select-node-ids="selectNodeIds"
          :trash-enable="false"
          @refreshTree="refreshTree"
          ref="uiElementList"/>
    </ms-main-container>

  </ms-container>
</template>

<script>

import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import UiElementModule from "./components/UiElementModule";
import UiElementList from "./components/UiElementList";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";

export default {
  name: "UiElement",
  components: {
    UiElementList,
    UiElementModule,
    MsMainContainer,
    MsAsideContainer,
    MsContainer
  },
  data() {
    return {
      moduleOptions: [],
      tabs: [],
      selectNodeIds: [],
      nodeTree: [],
    };
  },
  computed: {
    hasElementPermission() {
      return hasPermissions('PROJECT_UI_ELEMENT:READ');
    }
  },
  methods: {
    refreshTable() {
      this.$refs.uiElementList.getTableData();
    },
    refreshTree() {
      this.$refs.nodeTree.list();
    },
    nodeChange(node, nodeIds) {
      this.selectNodeIds = nodeIds;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    setNodeTree(data) {
      data.forEach(node => {
        node.name = node.name === '未规划元素' ? this.$t('ui.unplanned_element') : node.name;
        node.label = node.label === '未规划元素' ? this.$t('ui.unplanned_element') : node.label;
      });
      this.nodeTree = data;
    },
    handleExport(data){
      this.$refs.uiElementList.handleExport(data)
    }
  }
};
</script>

<style scoped>
:deep(.element-aside) .ms-aside-node-tree {
  overflow-x: hidden !important;
}
</style>

