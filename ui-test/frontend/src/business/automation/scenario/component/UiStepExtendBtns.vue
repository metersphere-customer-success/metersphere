<template>
   <div v-if="globalConfig">
      <el-dropdown @command="handleCommand" class="scenario-ext-btn">
        <el-link type="primary" :underline="false">
          <el-icon class="el-icon-more"></el-icon>
        </el-link>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="copy" v-if="showCopyBtn">{{
            this.$t("commons.copy")
          }}</el-dropdown-item>
          <el-dropdown-item command="enable" v-if="showDisableBtn">{{
            this.$t("ui.disable")
          }}</el-dropdown-item>
          <el-dropdown-item command="enable" v-if="showEnableBtn">{{
            this.$t("ui.enable")
          }}</el-dropdown-item>
          <el-dropdown-item command="remove" v-if="showDelBtn">{{
            this.$t("api_test.automation.delete_step")
          }}</el-dropdown-item>
          <el-dropdown-item command="rename" v-if="showRenameBtn">{{
            this.$t("test_track.module.rename")
          }}</el-dropdown-item>
          <el-dropdown-item command="scenarioVar" v-if="showVarBtn">
            {{ this.$t("api_test.automation.view_scene_variables") }}
          </el-dropdown-item>
          <el-dropdown-item command="openScenario" v-if="showJumpBtn">
            {{ this.jumpTitle }}
          </el-dropdown-item>
          <el-dropdown-item
            command="saveAs"
            v-if="
              allSamplers.indexOf(data.type) != -1 &&
              (data.referenced === undefined || data.referenced === 'Created')
            "
          >
            {{ this.$t("api_test.automation.save_as_api") }}
          </el-dropdown-item>
          <el-dropdown-item command="setScenario" v-if="showConfigBtn">
            {{ $t("commons.reference_settings") }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <ms-variable-list ref="scenarioParameters" @setVariables="setVariables" />
      <ms-add-basis-api :currentProtocol="currentProtocol" ref="api" />
    </div>
</template>

<script>
import {STEP} from "@/api/Setting";
import MsVariableList from "@/business/automation/scenario/component/UiVariableList";
import MsAddBasisApi from "@/business/definition/components/basis/AddBasisApi";
import {getUiScenario} from "@/business/network/ui-scenario";
import {checkScenarioEnv, setScenarioDomain} from "@/api/scenario";
import {getUUID, strMapToObj} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasPermission, hasPermissionForProjectId} from "metersphere-frontend/src/utils/permission";

export default {
  name: "UiStepExtendBtns",
  components: {STEP, MsVariableList, MsAddBasisApi},
  props: {
    refNodeIdCahceMap: Map,
    isScenario: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showEnableScence: {
      type: Boolean,
      default() {
        return true;
      }
    },
    data: Object,
    environmentType: String,
    environmentGroupId: String,
    envMap: Map,
  },
  data() {
    return {
      allSamplers: [],
      currentProtocol: "HTTP",
      filter: new STEP,
    }
  },
  mounted() {
    this.allSamplers = this.filter.get('DEFINITION');
  },
   computed: {
      globalConfig() {
        if (!this.refNodeIdCahceMap) {
          return true;
        }
        return !this.refNodeIdCahceMap.get(this.data.id);
      },
      //是否展示复制按钮
      showCopyBtn() {
        // return this.data.command;
        return true;
      },
      //是否展示删除
      showDelBtn() {
        return true;
      },
      //显示启用
      showEnableBtn() {
        // return this.data.command && !this.data.enable;
        return !this.data.enable;
      },
      //是否禁用
      showDisableBtn() {
        // return this.data.command && this.data.enable;
        return this.data.enable;
      },
      //是否展示重命名
      showRenameBtn() {
        //引用不支持重命名
        if (this.data.referenced === "REF") {
          return false;
        }
        return !this.isScenario;
      },
      //是否展示查看场景变量
      showVarBtn() {
        return (
          this.data.type === "UiScenario" || this.data.type === "scenario" //|| this.data.type === "customCommand"
        );
      },
      //是否展示打开场景/指令
      showJumpBtn() {
        return (
          (this.data.type === "UiScenario" || this.data.type === "scenario" || this.data.type === "customCommand") &&
          this.data.referenced === "REF"
        );
      },
      //是否展示场景设置
      showConfigBtn() {
        return (
          this.data.type === "UiScenario" || this.data.type === "scenario" // || this.data.type === "customCommand"
        );
      },
      jumpTitle() {
        return this.data.type === "UiScenario" || this.data.type === "scenario"
          ? this.$t("api_test.automation.open_scene")
          : this.$t("ui.open_custom_command_label");
      },
    },
  methods: {
    sortParamters(variables) {
      let index = 1;
      variables.forEach((item) => {
        item.num = index;
        index++;
      });
      return variables;
    },
    handleCommand(cmd) {
      switch (cmd) {
       case "copy":
         this.$emit("copy");
         break;
       case "remove":
         this.$emit("remove");
         break;
       case "scenarioVar":
         this.viewScenarioVar();
         break;
        case "openScenario":
          this.getScenario();
          break;
        case "saveAs":
          this.saveAsApi();
          break;
        case "setScenario":
          this.setScenario();
          break;
        case "enable":
          this.$emit("enable");
          break;
        case "rename":
          this.$emit("rename");
          break;
      }
    },
    setVariables(v, h) {
      this.data.variables = v;
    },
    setScenario() {
      this.$emit("setScenario", true)
    },
    viewScenarioVar() {
      if (this.data.projectId && !hasPermissionForProjectId("PROJECT_UI_SCENARIO:READ",this.data.projectId)) {
        this.$message.error(this.$t('ui.no_scenario_read_permission'))
        return;
      }
      let targetId = this.data.id;
      if (this.data.resourceId) {
        targetId = this.data.resourceId;
      }
      if(this.data.referenced !== "REF"){
        this.$refs.scenarioParameters.open(
                this.sortParamters(this.data.variables || []),
                this.data.headers,
                this.data.referenced === "REF"
        );
        return;
      }

      //引用情况 需要查询最新记录
      getUiScenario(targetId).then((data) => {
        let scenarioDefinition = data.scenarioDefinition;
        this.$refs.scenarioParameters.open(
            this.sortParamters(scenarioDefinition.variables || []),
            this.data.headers,
            this.data.referenced === "REF"
        );
      })
    },
    getScenario() {
      if (!hasPermissionForProjectId("PROJECT_UI_SCENARIO:READ",this.data.projectId)) {
        this.$message.error(this.$t('ui.no_scenario_read_permission'))
        return;
      }
      let targetId = this.data.id;
      if (this.data.resourceId) {
        targetId = this.data.resourceId;
      }
      this.result = this.$get(
        "/ui/automation/getUiScenario/" + targetId).then((response) => {
        if (response.data) {
          if (response.data.projectId === getCurrentProjectID()) {
            this.$emit("openScenario", response.data);
          } else {
            let automationData = this.$router.resolve({
              name: "UiAutomationWithQuery",
              params: {
                redirectID: getUUID(),
                dataType: this.data.type,
                dataSelectRange: "edit:" + response.data.id,
                projectId: response.data.projectId,
                workspaceId: getCurrentWorkspaceId(),
              },
            });
            window.open(automationData.href, "_blank");
          }
        } else {
          let typeLable = this.isScenario ? this.$t("ui.scenario") : this.$t("ui.instruction");
          this.$error("引用" + typeLable + "已经被删除");
        }
      });
    },

    checkEnv(val) {
      checkScenarioEnv(this.data.id).then((res) => {
        if (this.data.environmentEnable && !res.data) {
          this.data.environmentEnable = false;
          this.$warning(this.$t('commons.scenario_warning'));
          return;
        }
        this.setDomain(val);
      });
    },
    setDomain(val) {
      let param = {
        environmentEnable: val,
        id: this.data.id,
        environmentType: this.environmentType,
        environmentGroupId: this.environmentGroupId,
        environmentMap: strMapToObj(this.envMap),
        definition: JSON.stringify(this.data),
      };
      setScenarioDomain(param).then((res) => {
        if (res.data) {
          let data = JSON.parse(res.data);
          this.data.hashTree = data.hashTree;
          this.setOwnEnvironment(this.data.hashTree);
        }
      });
    },
    setOwnEnvironment(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        let typeArray = ["JDBCPostProcessor", "JDBCSampler", "JDBCPreProcessor"]
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          scenarioDefinition[i].environmentEnable = this.data.environmentEnable;
          scenarioDefinition[i].refEevMap = new Map();
          if (this.data.environmentEnable && this.data.environmentMap) {
            scenarioDefinition[i].refEevMap = this.data.environmentMap;
          }
        }
        if (scenarioDefinition[i].hashTree !== undefined && scenarioDefinition[i].hashTree.length > 0) {
          this.setOwnEnvironment(scenarioDefinition[i].hashTree);
        }
      }
    },
    saveAsApi() {
      this.currentProtocol = this.data.protocol;
      this.data.customizeReq = false;
      this.$refs.api.open(this.data);
    }
  }
}
</script>

<style scoped>
.scenario-ext-btn {
  margin-left: 10px;
}
</style>
