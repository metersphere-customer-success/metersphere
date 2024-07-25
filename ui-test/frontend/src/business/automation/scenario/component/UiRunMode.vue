<template>
  <el-dialog
      destroy-on-close
      :title="$t('load_test.runtime_config')"
      width="550px"
      @close="close"
      :visible.sync="runModeVisible"
  >

    <div class="mode-container">

      <!--   环境   -->
      <div class="browser-row wrap">
        <div class="title">{{ $t("commons.environment") }}：</div>
        <div class="content">
          <env-select-popover
              :project-ids="projectIds"
              :project-list="projectList"
              :environment-type.sync="runConfig.environmentType"
              :is-scenario="false"
              :has-option-group="true"
              :project-env-map="projectEnvMap"
              :group-id="runConfig.environmentGroupId"
              @setProjectEnvMap="setProjectEnvMap"
              @setEnvGroup="setEnvGroup"
              ref="envSelectPopover"
              class="mode-row"></env-select-popover>

        </div>
      </div>

      <!-- 浏览器 -->
      <div class="browser-row wrap">
        <div class="title">{{ $t("ui.browser") }}：</div>
        <div class="content">
          <el-select
              size="mini"
              v-model="runConfig.browser"
              style="margin-right: 30px; width: 163px"
          >
            <el-option
                v-for="b in browsers"
                :key="b.value"
                :value="b.value"
                :label="b.label"
            ></el-option>
          </el-select>
        </div>
      </div>

      <!-- 运行模式 -->
      <div class="run-mode-row wrap" v-if="customRunMode">
        <div class="title">{{ $t("run_mode.title") }}：</div>
        <div class="content">
          <el-radio-group v-model="runConfig.mode" @change="changeMode">
            <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
            <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
          </el-radio-group>
        </div>
      </div>


      <!-- selenium-server选择 -->
      <div class="run-mode-row wrap" v-if="customRunMode">
        <div class="title">执行选择：</div>
        <div class="content">
          <el-radio-group v-model="runConfig.driverConfig">
            <el-radio label="system">后端执行</el-radio>
            <el-radio label="persion">本地执行</el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- 性能模式 用于测试报告 -->
      <div class="other-config-row wrap" v-if="customSerialOnSampleError">
        <div class="other-title title">{{ $t("run_mode.other_config") }}：</div>
        <div class="other-content">
          <div class="sub-item-row">
            <el-checkbox v-model="runConfig.headlessEnabled">
              {{ $t("ui.performance_mode") }}
            </el-checkbox>
          </div>

          <div class="mode-row row-padding">
            <el-checkbox
                v-model="runConfig.retryEnable"
                class="ms-failure-div-right"
            >
              {{ $t("run_mode.retry_on_failure") }}
            </el-checkbox>
            <span v-if="runConfig.retryEnable">
              <el-tooltip placement="top" style="margin: 0 4px 0 2px">
                <div slot="content">{{ $t("run_mode.retry_message") }}</div>
                <i class="el-icon-question" style="cursor: pointer"/>
              </el-tooltip>
              <span style="margin-left: 10px">
                {{ $t("run_mode.retry") }}
                <el-input-number
                    :value="runConfig.retryNum"
                    v-model="runConfig.retryNum"
                    :min="1"
                    :max="10000000"
                    size="mini"
                    style="width: 103px"
                />
                &nbsp;
                {{ $t("run_mode.retry_frequency") }}
              </span>
            </span>
          </div>

          <div
              class="sub-item-row row-padding"
              v-if="runConfig.mode == 'serial'"
          >
            <el-checkbox v-model="runConfig.onSampleError">
              {{ $t("api_test.fail_to_stop") }}
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 其他配置 -->
      <div
          class="other-config-row wrap"
          v-if="customReportType && !customSerialOnSampleError"
      >
        <div class="other-title title">{{ $t("run_mode.other_config") }}：</div>
        <div class="other-content">
          <div class="sub-item-row">
            <el-radio-group v-model="runConfig.reportType">
              <el-radio label="iddReport">{{
                  $t("run_mode.idd_report")
                }}
              </el-radio>
              <el-radio label="setReport">{{
                  $t("run_mode.set_report")
                }}
              </el-radio>
            </el-radio-group>
          </div>
          <div class="sub-item-row row-padding">
            <el-checkbox v-model="runConfig.headlessEnabled">
              {{ $t("ui.performance_mode") }}
            </el-checkbox>
          </div>

          <div class="mode-row row-padding">
            <el-checkbox
                v-model="runConfig.retryEnable"
                class="ms-failure-div-right"
            >
              {{ $t("run_mode.retry_on_failure") }}
            </el-checkbox>
            <span v-if="runConfig.retryEnable">
              <el-tooltip placement="top" style="margin: 0 4px 0 2px">
                <div slot="content">{{ $t("run_mode.retry_message") }}</div>
                <i class="el-icon-question" style="cursor: pointer"/>
              </el-tooltip>
              <span style="margin-left: 10px">
                {{ $t("run_mode.retry") }}
                <el-input-number
                    :value="runConfig.retryNum"
                    v-model="runConfig.retryNum"
                    :min="1"
                    :max="10000000"
                    size="mini"
                    style="width: 103px"
                />
                &nbsp;
                {{ $t("run_mode.retry_frequency") }}
              </span>
            </span>
          </div>

          <div
              class="sub-item-row row-padding"
              v-if="runConfig.mode == 'serial'"
          >
            <el-checkbox v-model="runConfig.onSampleError">
              {{ $t("api_test.fail_to_stop") }}
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 报告名称 -->
      <div
          class="report-name-row wrap"
          :style="runConfig.mode == 'serial' ? 'padding-top: 25px' : ''"
          v-if="runConfig.reportType === 'setReport'"
      >
        <div class="title">{{ $t("run_mode.report_name") }}：</div>
        <div class="content">
          <el-input
              v-model="runConfig.reportName"
              :placeholder="$t('commons.input_content')"
              size="small"
              style="width: 300px"
          />
        </div>
      </div>
    </div>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import EnvPopover from "@/business/automation/env/EnvPopover";
import {getOwnerProjects, getUiScenarioEnv} from "@/business/automation/ui-automation";
import {ENV_TYPE} from "@/api/constants";
import EnvSelectPopover from "@/business/automation/env/EnvSelectPopover";
import {uiScenarioEnvMap} from "@/api/scenario";
import {strMapToObj} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "UiRunMode",
  components: {MsDialogFooter, EnvPopover, EnvSelectPopover},
  data() {
    return {
      runModeVisible: false,
      testType: null,
      browsers: [
        {
          label: this.$t("chrome"),
          value: "CHROME",
        },
        {
          label: this.$t("firefox"),
          value: "FIREFOX",
        },
      ],
      runConfig: {
        reportName: "",
        mode: "serial",
        browser: "CHROME",
        reportType: "iddReport",
        driverConfig: "system",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
        headlessEnabled: true,
        retryEnable: false,
        envMap: new Map(),
        environmentType: ENV_TYPE.JSON
      },
      projectList: [],
      projectIds: new Set(),
      envResult: {
        loading: false,
      },
      envGroupId: '',
      projectEnvMap: {},
      caseIdEnvNameMap: {},
      environmentType: ENV_TYPE.JSON,
    };
  },
  props: {
    request: {
      type: Object,
    },
    /**
     * 是否允许设置报告类型
     */
    customReportType: {
      type: Boolean,
      default: false,
    },
    /**
     * 是否允许设置串行并行
     */
    customRunMode: {
      type: Boolean,
      default: true,
    },
    /**
     * 是否开启串行失败终止
     */
    customSerialOnSampleError: {
      type: Boolean,
      default: false,
    },
  },

  watch: {
    "runConfig.runWithinResourcePool"() {
      if (!this.runConfig.runWithinResourcePool) {
        this.runConfig = {
          mode: this.runConfig.mode,
          reportType: "iddReport",
          driverConfig: "system",
          reportName: "",
          runWithinResourcePool: false,
          resourcePoolId: null,
        };
      }
    },
  },
  methods: {
    open() {
      this.runConfig = {
        mode: "serial",
        reportType: "iddReport",
        reportName: "",
        browser: "CHROME",
        driverConfig: "system",
        runWithinResourcePool: false,
        resourcePoolId: null,
        headlessEnabled: true,
        envMap: new Map(),
        environmentType: ENV_TYPE.JSON
      };
      this.projectEnvMap = {};
      this.runModeVisible = true;
      this.showPopover();
      this.getWsProjects();
    },
    changeMode() {
      this.runConfig.runWithinResourcePool = false;
      this.runConfig.resourcePoolId = null;
      this.runConfig.reportType = "iddReport";
      this.runConfig.reportName = "";
    },
    close() {
      this.runModeVisible = false;
      this.$emit("close");
    },
    getWsProjects() {
      this.$get("/project/getOwnerProjects").then((res) => {
        this.projectList = res.data;
      });
    },
    handleRunBatch() {
      if (
          this.runConfig.reportType === "setReport" &&
          !this.runConfig.reportName.trim()
      ) {
        this.$warning(this.$t("commons.input_name"));
        return;
      }

      //并行不支持失败终止
      if (this.runConfig.mode === "parallel") {
        this.runConfig.onSampleError = false;
      }

      this.runConfig.envMap = strMapToObj(this.runConfig.envMap);
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    showPopover() {
      let currentProjectID = getCurrentProjectID();
      this.projectIds.clear();
      uiScenarioEnvMap(this.request).then((res) => {
        let data = res.data;
        this.projectEnvMap = data;
        if (data) {
          for (let d in data) {
            this.projectIds.add(d);
          }
        }
        if (this.projectIds.size === 0) {
          this.projectIds.add(currentProjectID);
        }
        this.$refs.envSelectPopover.open();
      });
    },
    setProjectEnvMap(projectEnvMap) {
      this.$set(this.runConfig, "envMap", projectEnvMap);
      this.projectEnvMap = projectEnvMap;
    },
    setEnvGroup() {
      //todo
    },
    getEnv(definition) {
      return new Promise((resolve) => {
        getUiScenarioEnv({definition: definition}).then((res) => {
          if (res.data) {
            this.projectIds = new Set(res.data.projectIds);
            this.projectIds.add(this.projectId);
          }
          resolve();
        });
      });
    },
  },
};
</script>

<style scoped>
.mode-container .title {
  width: 100px;
  min-width: 100px;
  text-align: right;
  padding-right: 10px;
}

.mode-container .content {
  width: 363px;
}

.wrap {
  display: flex;
  align-items: center;
  padding: 10px 15px 10px 15px;
}

.other-title {
  height: 100%;
  align-items: flex-start;
}

.other-content {
  height: 100%;
}

.sub-item-row {
  width: 200px;
}

.row-padding {
  margin-top: 15px;
}

.other-config-row {
  height: 100px;
}
</style>
