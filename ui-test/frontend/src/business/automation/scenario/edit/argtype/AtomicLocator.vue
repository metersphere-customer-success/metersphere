<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="24">
        <el-select
          :disabled="isReadOnly || isReadonlyProp"
          v-model="value[propName].elementType"
          size="small"
          class="atomic-locator"
        >
          <el-option
            v-for="item in elementType"
            :key="item.id"
            :label="$t('ui.' + item.name)"
            :value="item.id"
          ></el-option>
        </el-select>
        <ms-select-tree
          v-loading="moduleResult.loading"
          :disabled="isReadOnly || isReadonlyProp"
          size="small"
          :data="moduleOptions"
          :defaultKey="value[propName].moduleId"
          @clean="clearModule"
          @getValue="setModule"
          class="atomic-locator"
          @setSelectNodeIds="setSelectNodeIds"
          :obj="moduleObj"
          clearable
          checkStrictly
          v-if="
            value[propName] && value[propName].elementType == 'elementObject'
          "
          :placeholder="$t('ui.elementType')"
          ref="selectTree"
        />

        <el-select
          :disabled="isReadOnly || isReadonlyProp"
          v-model="value[propName].locateType"
          size="small"
          class="atomic-locator"
          v-if="
            value[propName] && value[propName].elementType == 'elementLocator'
          "
          filterable
          :placeholder="$t('ui.not_selected_location')"
        >
          <el-option
            v-for="item in locateTypes"
            :key="item.value"
            :label="item.text"
            :value="item.value"
          ></el-option>
        </el-select>

        <el-select
          v-loading="elementResult.loading || moduleResult.loading"
          :disabled="isReadOnly || isReadonlyProp"
          v-model="value[propName].elementId"
          size="small"
          class="atomic-locator"
          @change="changeElement"
          v-if="
            value[propName] && value[propName].elementType == 'elementObject'
          "
          filterable
          :placeholder="$t('ui.not_selected')"
        >
          <el-option
            v-for="item in elementList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>

        <el-tooltip placement="top-start">
          <div slot="content" class="locator-tooltip">
            {{ value[propName]['viewLocator'] || $t('commons.input_content')}}
          </div>
          <span>
            <el-input
              size="small"
              class="atomic-locator"
              :disabled="isReadOnly || isReadonlyProp"
              v-if="
              value[propName] && value[propName].elementType == 'elementLocator'
            "
              :placeholder="$t('ui.location')"
              v-model="value[propName]['viewLocator']"
            />
          </span>
        </el-tooltip>

        <!-- 元素定位 快捷添加元素到元素库 -->
        <el-tooltip :content="$t('元素加入到元素库中')" placement="top-start"
        >
          <span
            v-show="
              value[propName] && value[propName].elementType == 'elementLocator'
            "
            @click="addToElementLibrary"
          ><i
              class="el-icon-plus"
              :style="[{color:(hasPermissions('PROJECT_UI_ELEMENT:READ+CREATE','PROJECT_UI_ELEMENT:READ+EDIT')? '#783887':'grey')},{pointerEvents: (hasPermissions('PROJECT_UI_ELEMENT:READ+CREATE','PROJECT_UI_ELEMENT:READ+EDIT') ? true:'none')}, {cursor: 'pointer'}, {fontWeight: 'bold'}]"
            ></i
          ></span>
        </el-tooltip>
      </el-col>
    </el-row>
    <ms-ui-variable-advance
      ref="variableAdvance"
      :current-item="currentItem"
      @advancedRefresh="reload"
    />

    <ui-element-edit
      :module-tree="moduleOptions"
      @save="handleElementEditSave"
      ref="uiElementEdit"
    />
  </div>
</template>

<script>
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import { getElementModules } from "@/business/network/ui-element-module";
import { getUiElementNames, getUiElementById } from "@/business/network/ui-element";
import { UI_ELEMENT_LOCATION_TYPE_OPTION } from "metersphere-frontend/src/utils/table-constants";
import MsUiVariableAdvance from "@/business/automation/scenario/edit/UiVariableAdvance";
import UiElementEdit from "@/business/element/components/UiElementEdit";
import {useCommandStore} from "@/store";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";

const commandStore = new useCommandStore();
export default {
  name: "AtomicLocator",
  props: ["paramDefinition", "commandDefinition", "value", "propName", "isReadonlyProp"],
  components: {
    MsSelectTree,
    MsUiVariableAdvance,
    UiElementEdit,
  },
  data() {
    return {
      currentItem: {},
      currentCommand: {},
      elementList: [],
      locateTypes: UI_ELEMENT_LOCATION_TYPE_OPTION,
      elementType: [
        { id: "elementObject", name: "elementObject" },
        { id: "elementLocator", name: "elementLocator" },
      ],
      moduleObj: {
        id: "id",
        label: "name",
      },
      moduleOptions: [],
      projectId: null,
      isReadOnly: false,
      pro: this.propName.split("V")[0],
      selectNodeIds: [],
      moduleResult: {
        loading: false,
      },
      elementResult: {
        loading: false,
      },
      tempElementId: "",
      initData: false,
    };
  },
  created() {
    this.isReadOnly = this.value ? this.value.readonly === true : false;
    this.initData = true;
    this.projectId = getCurrentProjectID();
    this.getElementModules();
    if (!this.value[this.propName]) {
      this.$set(this.value, this.propName, {});
    }
    if(this.value[this.propName].elementId){
      this.elementList = [{id: this.value[this.propName].elementId, name : this.$t('ui.not_selected')}]
    }

    if (!this.value[this.propName].elementType) {
      //默认展示元素对象
      this.$set(this.value[this.propName], "elementType", "elementObject");
      this.$set(this.value[this.propName], "elementId", null);
    }

    if (!this.value[this.propName].locateType) {
      this.$set(this.value[this.propName], "locateType", null);
    }

    if (this.value[this.pro] && !this.value[this.propName]["view" + this.pro]) {
      if (this.value[this.pro].indexOf("=") != -1) {
        this.$set(
          this.value[this.propName],
          "locateType",
          this.value[this.pro].slice(0, this.value[this.pro].indexOf("="))
        );
      } else {
        this.$set(this.value[this.propName], "locateType", "id");
      }
      this.$set(
        this.value[this.propName],
        "view" + this.pro,
        this.value[this.pro].slice(
          this.value[this.pro].indexOf("=") + 1,
          this.value[this.pro].length
        )
      );
    }

    //解决 列表未渲染显示元素id
    if (
      this.value[this.propName].moduleId &&
      this.value[this.propName].elementId
    ) {
      this.tempElementId = this.value[this.propName].elementId || "";
      // this.$set(this.value[this.propName], "elementId", "");
      this.selectNodeIds.push(this.value[this.propName].moduleId);
      this.getUiElements(this.tempElementId);
    }

  },
  watch: {
    "commandStore.librarySelectElement"() {
      if (this.value[this.propName].elementType == "elementObject") {
        let element = commandStore.librarySelectElement;
        if (element.moduleId === this.value[this.propName].moduleId) {
          this.value[this.propName].elementId = element.id;
        } else {
          this.value[this.propName].moduleId = element.moduleId;
          this.setModule(element.moduleId);
          if (!this.elementList || this.elementList <= 0) {
            this.getUiElements(element.id);
          } else {
            this.value[this.propName].elementId = element.id;
          }
        }
      }
    },
    $route(to, from) {
      //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
      if (
        from.path.indexOf("/ui/element") !== -1 &&
        to.path.indexOf("/ui/automation") !== -1
      ) {
        this.getElementModules();
      }
    },
  },
  methods: {
    hasPermissions,
    handleElementEditSave(data) {
       //切换
       if (data && data.id) {
        this.value[this.propName].elementType = "elementObject";
        this.value[this.propName].moduleId = data.moduleId;
        this.value[this.propName].elementId = data.id;
      }
      this.$EventBus.$emit("refreshElementLibrary", true);
    },
    addToElementLibrary() {
      console.log("AUTO")
      if (!hasPermissions('PROJECT_UI_ELEMENT:READ+CREATE','PROJECT_UI_ELEMENT:READ+EDIT')){
        return;
      }
      let recentlyMoudleId = localStorage.getItem("RECENTLY_ELEMENT_MODULE");
      if (!recentlyMoudleId) {
        recentlyMoudleId = this.moduleOptions[0]
          ? this.moduleOptions[0].id
          : "";
        localStorage.setItem("RECENTLY_ELEMENT_MODULE", recentlyMoudleId);
      }
      let data = {
        name: "",
        moduleId: recentlyMoudleId,
        locationType: this.value[this.propName].locateType || "",
        location: this.value[this.propName]["viewLocator"] || "",
        description: "",
      };
      this.$refs.uiElementEdit.open(data);
    },
    clearModule() {
      this.value[this.propName].moduleId = "";
      this.value[this.propName].elementId = null;
      this.elementList = [];
    },
    changeElement() {
      this.$forceUpdate();
      this.$nextTick(() => {
        commandStore.uiElementLibraryElements = this.value[this.propName].elementId;
      });
    },
    funcSearch() {},
    reload() {},
    advanced(item) {
      this.$refs.variableAdvance.open();
    },
    setModule(moduleId, callback) {
      if (!moduleId || Array.isArray(moduleId)) return;
      if (moduleId !== this.value[this.propName].moduleId) {
        this.value[this.propName].elementId = null;
      }
      this.value[this.propName].moduleId = moduleId;
    },
    getUiElements(elementId) {
      let param = {
        projectId: this.projectId,
        moduleIds: this.selectNodeIds,
      };

      this.elementResult = getUiElementNames(param).then(res => {
        if (res && res.data) {
          this.elementList = res.data;
        } else {
          this.elementList = [];
        }
        if (!this.initData) {
          this.changeElement();
        } else {
          this.initData = false;
          this.tempElementId = null;
        }
      });

      if(elementId){
        this.elementResult = getUiElementById(elementId).then(res => {
          if (res && res.data) {
            this.value[this.propName].elementId = elementId;
            this.value[this.propName].moduleId = res.data.moduleId;
          }
        });
        return;
      }
    },
    setSelectNodeIds(ids) {
      this.selectNodeIds = ids;
      commandStore.uiElementLibraryElements = ids;
      if (ids && ids.length > 0) {
        this.getUiElements();
      } else {
        commandStore.uiElementLibraryElements = null;
      }
    },
    getElementModules() {
      getElementModules(this.projectId).then(data => {
        if (data) {
          this.moduleOptions = data.data;
          let hasModule = false;
          if (this.moduleOptions) {
            let ids = [];
            this.flattenObj(this.moduleOptions, ids);
            if (ids.indexOf(this.value[this.propName].moduleId) != -1) {
              hasModule = true;
            }

            this.$nextTick(() => {
              if (!hasModule) {
                this.clearModule();
              } else {
                this.setModule(this.value[this.propName].moduleId);
              }
            });
          }
        }
      });
    },
    flattenObj(modules, r) {
      if (!modules) {
        return r;
      }
      modules.forEach((m) => {
        if (m.id) {
          r.push(m.id);
        }
        if (m.children) {
          this.flattenObj(m.children, r);
        }
      });
    },
  },
};
</script>

<style scoped>
.atomic-locator {
  display: inline-block;
  width: 180px;
  padding-right: 10px;
}

.pointer {
  cursor: pointer;
}

.label {
  text-align: right;
  padding-right: 10px;
}

.el-form-item .el-select {
  width: 180px;
}

.el-form-item .el-input {
  width: 180px;
}

.pointer {
  cursor: pointer;
}

.label {
  text-align: right;
  padding-right: 10px;
}

.locator-tooltip {
  text-align: justify;
  word-break: break-all
}
.notClick {
  color:grey;
  pointer-events: none;
}
</style>
