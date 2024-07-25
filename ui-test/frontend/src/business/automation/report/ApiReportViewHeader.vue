<template>
    <header class="report-header">
        <el-row type="flex">
            <el-col>
        <span v-if="!debug">
          <el-input v-if="nameIsEdit" size="mini" @blur="handleSave(report.name)" @keyup.enter.native="handleSaveKeyUp"
                    style="width: 200px" v-model="report.name" maxlength="60" show-word-limit/>
          <span v-else>
            <el-link v-if="!isShare &&isSingleScenario"
                     type="primary"
                     class="report-name"
                     @click="redirect">
              {{ report.name }}
            </el-link>
            <span v-else>
              {{ report.name }}
            </span>
            <i v-if="showReportNameButton && showNameEdit" class="el-icon-edit" style="cursor:pointer"
               @click="nameIsEdit = true"
               @click.stop/>
          </span>
        </span>
                <span v-if="report.endTime || report.createTime">
          <span :class="{'ml10' : !debug && showNameEdit}">{{ $t('report.test_start_time') }}：</span>
          <span class="time"> {{ report.createTime | timestampFormatDate }}</span>
          <span style="margin-left: 10px">{{ $t('report.test_end_time') }}：</span>
          <span class="time"> {{ report.endTime | timestampFormatDate }}</span>
        </span>
                <div style="float: right">
                    <!--          <el-button v-if="!isPlan && (!debug || exportFlag) && !isTemplate && !isUi"-->
                    <!--                     v-permission="['PROJECT_API_REPORT:READ+EXPORT']" :disabled="isReadOnly" class="export-button"-->
                    <!--                     plain type="primary" size="mini" @click="handleExport(report.name)" style="margin-right: 10px">-->
                    <!--            {{ $t('test_track.plan_view.export_report') }}-->
                    <!--          </el-button>-->

                    <el-popover
                            v-if="!isPlan && (!debug || exportFlag) && !isTemplate"
                            v-permission="['PROJECT_PERFORMANCE_REPORT:READ+EXPORT']"
                            style="margin-right: 10px;float: right;position:relative;"
                            placement="bottom"
                            trigger="click"
                            popper-class="ms-custom-message-class"
                            width="300">
                        <p>{{ shareUrl }}</p>
                        <span style="color: red;float: left;margin-left: 10px;" v-if="application.typeValue">{{
                            $t('commons.validity_period') + application.typeValue
                            }}</span>
                        <div style="text-align: right; margin: 0">
                            <el-button type="primary" size="mini" :disabled="!shareUrl"
                                       v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}
                            </el-button>
                        </div>
                        <el-button slot="reference" :disabled="isReadOnly" type="danger" plain size="mini"
                                   @click="handleShare(report)">
                            {{ $t('test_track.plan_view.share_report') }}
                        </el-button>
                    </el-popover>

                    <el-button v-if="showRerunButton" class="rerun-button" plain size="mini" @click="rerun">
                        {{ $t('api_test.automation.rerun') }}
                    </el-button>

                    <ui-download-screenshot :report="report" v-if="isUi && showUIDownloadButton"/>

                    <el-button v-if="showCancelButton" class="export-button" plain size="mini" @click="returnView">
                        {{ $t('commons.cancel') }}
                    </el-button>

                </div>
            </el-col>
        </el-row>
        <el-row v-if="envConfig && envConfig.reportType === 'setReport'">
            <span>{{ $t('report.run_model') }}：</span>
            <span>{{ modeDesc }}</span>
        </el-row>
        <el-row v-if="showProjectEnv" type="flex">
            <span> {{ $t('commons.environment') + ':' }} </span>
            <div v-for="(values,key) in projectEnvMap" :key="key" style="margin-right: 10px">
                {{ key + ":" }}
                <ms-tag v-for="(item,index) in values" :key="index" type="success" :content="item"
                        style="margin-left: 2px"/>
            </div>
        </el-row>
    </header>
</template>

<script>

import {generateShareInfoWithExpired, getShareRedirectUrl} from "@/api/share";
import MsTag from "metersphere-frontend/src/components/MsTag";
import UiDownloadScreenshot from "@/business/automation/report/UiDownloadScreenshot";
import {timestampFormatDate} from "@/api/filter";
import {getProject} from "@/api/project";
import {getUiScenarioByPlanScenarioId} from "../../../api/test-plan";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
    name: "MsApiReportViewHeader",
    components: {MsTag, UiDownloadScreenshot},
    props: {
        isShare: Boolean,
        report: {},
        projectEnvMap: {},
        reportProjectId: String,
        envConfig: {},
        debug: Boolean,
        showCancelButton: {
            type: Boolean,
            default: false,
        },
        showNameEdit: {
            type: Boolean,
            default: true,
        },
        showUIDownloadButton: {
            type: Boolean,
            default: true,
        },
        showReportNameButton: {
            type: Boolean,
            default: true,
        },
        showRerunButton: {
            type: Boolean,
            default: false,
        },
        isTemplate: Boolean,
        exportFlag: {
            type: Boolean,
            default: false,
        },
        isPlan: Boolean
    },
    filters: {
        timestampFormatDate: function (timestamp) {
            return timestampFormatDate(timestamp);
        }
    },
    computed: {
        showProjectEnv() {
            return this.projectEnvMap && JSON.stringify(this.projectEnvMap) !== '{}';
        },
        path() {
            return "/api/test/edit?id=" + this.report.testId;
        },
        scenarioId() {
            if (typeof this.report.scenarioId === 'string') {
                return this.report.scenarioId;
            } else {
                return "";
            }
        },
        isSingleScenario() {
            try {
                JSON.parse(this.report.scenarioId);
                return false;
            } catch (e) {
                return true;
            }
        },
        isUi() {
            // return this.report.reportType && this.report.reportType.startsWith("UI");
            // 此组件只应用于UI 故无需区分ui类型
            return true;
        },
    },
    data() {
        return {
            isReadOnly: false,
            nameIsEdit: false,
            shareUrl: "",
            modeDesc: "",
            application: {}
        }
    },
    created() {
        if (this.envConfig) {
            if (this.envConfig.mode === 'serial') {
                this.modeDesc = '串行'
            } else if (this.envConfig.mode === 'parallel') {
                this.modeDesc = '并行'
            }
        }
    },
    methods: {
        handleExport(name) {
            this.$emit('reportExport', name);
        },
        handleSave(name) {
            this.nameIsEdit = false;
            this.$emit('reportSave', name);
        },
        handleSaveKeyUp($event) {
            $event.target.blur();
        },
        rerun() {
            let type = this.report.reportType;
            let rerunObj = {type: type, reportId: this.report.id}
            this.$post('/api/test/exec/rerun', rerunObj, res => {
                if (res.data !== 'SUCCESS') {
                    this.$error(res.data);
                } else {
                    this.$success(this.$t('api_test.automation.rerun_success'));
                    this.returnView();
                }
            });
        },
        returnView() {
            if (this.isUi) {
                this.$router.push('/ui/report');
            } else {
                this.$router.push('/api/automation/report');
            }
        },
        handleShare(report) {
            this.getProjectApplication();
            let pram = {};
            pram.customData = report.id;
            pram.shareType = 'UI_REPORT';
            let thisHost = window.location.host;
            generateShareInfoWithExpired(pram).then((res) => {
                this.shareUrl = getShareRedirectUrl(res.data);
            });
        },
        getProjectApplication() {
            let path = "/API_SHARE_REPORT_TIME";
            if (this.isUi) {
                path = "/UI_SHARE_REPORT_TIME";
            }
            this.$get('/project_application/get/' + getCurrentProjectID() + path).then(res => {
                if (res.data && res.data.typeValue) {
                    let quantity = res.data.typeValue.substring(0, res.data.typeValue.length - 1);
                    let unit = res.data.typeValue.substring(res.data.typeValue.length - 1);
                    if (unit === 'H') {
                        res.data.typeValue = quantity + this.$t('commons.date_unit.hour');
                    } else if (unit === 'D') {
                        res.data.typeValue = quantity + this.$t('commons.date_unit.day');
                    } else if (unit === 'M') {
                        res.data.typeValue = quantity + this.$t('commons.workspace_unit') + this.$t('commons.date_unit.month');
                    } else if (unit === 'Y') {
                        res.data.typeValue = quantity + this.$t('commons.date_unit.year');
                    }
                    this.application = res.data;
                }
            });
        },
        redirect() {
          if (this.isShare) {
            // 分享报告点击名称就不跳转了
            return;
          }
          getUiScenarioByPlanScenarioId(this.scenarioId).then((res) => {
            if (res.data) {
              this.showDetails(res.data.id, res.data.projectId);
            }
          });
        },
      showDetails(resourceId, projectId) {
        let reportProjectId = projectId;
        if (!reportProjectId || reportProjectId === '') {
          if (!this.reportProjectId || this.reportProjectId === '') {
            reportProjectId = this.reportProjectId;
          } else {
            reportProjectId = this.report.projectId;
          }
        }
        getProject(reportProjectId).then(res => {
          let reportProject = res.data;
          if (reportProject) {
            let uuid = getUUID().substring(1, 5);
            let projectId = reportProject.id;
            let workspaceId = reportProject.workspaceId;
            let prefix = '/#';
            if (
                this.$route &&
                this.$route.path.startsWith('/ui/automation/report') &&
                this.$route.query &&
                this.$route.query.list
            ) {
              prefix = '';
            }
            let path = `${prefix}/ui/automation/?redirectID=${uuid}&dataType=scenario&projectId=${projectId}&workspaceId=${workspaceId}&resourceId=${resourceId}`;
            let data = this.$router.resolve({
              path: path,
            });
            window.open(path, '_blank');
          }
        });
      },
    }
}
</script>

<style scoped>

.export-button {
    float: right;
    margin-right: 10px;
}

.rerun-button {
    float: right;
    margin-right: 10px;
    background-color: #F2F9EF;
    color: #87C45D;
}
</style>

<style>
.report-view-header-custom-popover.el-popover {
    z-index: 2119 !important;
}

.ml10 {
    margin-left: 10px;
}

.report-header {
    min-width: 1200px;
}
</style>
