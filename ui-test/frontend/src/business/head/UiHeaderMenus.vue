<template>
  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='currentPath'>
          <el-menu-item v-for="(menu) in menus" :key="menu.path" :index="menu.path" v-permission="[menu.permission]">
            {{ menu.name }}
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="10">
        <ms-header-right-menus/>
      </el-col>
    </el-row>
  </div>

</template>

<script>

import ProjectChange from "metersphere-frontend/src/components/head/ProjectSwitch";
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";

export default {
  name: "UiHeaderMenus",
  components: { ProjectChange, MsHeaderRightMenus},
  data() {
    return {
      currentPath: '',
      testRecent: {
        title: this.$t('load_test.recent'),
        url: "/ui/recent/5",
        index: function (item) {
          return '/ui/test/edit/' + item.id;
        },
        router: function (item) {
          return {path: '/ui/test/edit', query: {id: item.id}};
        }
      },
      reportRecent: {
        title: this.$t('report.recent'),
        showTime: true,
        url: "/ui/report/recent/5",
        index: function (item) {
          return '/ui/report/view/' + item.id;
        }
      },
      isProjectActivation: true,
      isRouterAlive: true,
      apiTestProjectPath: '',
      currentProject: '',
      menus: [
        {
          path: '/ui/element',
          name: this.$t("ui.ui_element"),
          permission: 'PROJECT_UI_ELEMENT:READ'
        },
        {
          path: '/ui/automation',
          name: this.$t("ui.ui_automation"),
          permission: 'PROJECT_UI_SCENARIO:READ'
        },
        {
          path: '/ui/report',
          name: this.$t("ui.report"),
          permission: 'PROJECT_UI_REPORT:READ'
        }
      ]
    };
  },
  watch: {
    '$route': {
      immediate: true,
      handler(to) {

        if (hasPermissions("PROJECT_UI_SCENARIO:READ") && to.fullPath === "/ui/automation") {
          this.currentPath = "/ui/automation";
          return;
        } else if (hasPermissions("PROJECT_UI_ELEMENT:READ") && to.fullPath === "/ui/element") {
          this.currentPath = "/ui/element";
          return;
        } else if (hasPermissions("PROJECT_UI_REPORT:READ") && to.fullPath === "/ui/report") {
          this.currentPath = "/ui/report";
          return;
        }
      }
    }
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
  },
  mounted() {

  },
  beforeDestroy() {

  }
};

</script>

<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.menu-divider {
  margin: 0;
}

.blank_item {
  display: none;
}

.deactivation :deep(.el-submenu__title) {
  border-bottom: white !important;
}

.el-menu-item {
  padding: 0 10px;
}
</style>
