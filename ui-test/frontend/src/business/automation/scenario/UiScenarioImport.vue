<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.automation.scenario_import')" width="30%"
             :visible.sync="visible" class="api-import" v-loading="result.loading" @close="close">

    <div class="header-bar">
      <div>{{ $t('api_test.api_import.data_format') }}</div>
      <el-radio-group v-model="selectedPlatformValue">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value">{{ item.name }}</el-radio>
      </el-radio-group>

      <div class="operate-button">
        <el-button class="save-button" type="primary" plain @click="save">
          {{ $t('commons.save') }}
        </el-button>
        <el-button class="cancel-button" type="warning" plain @click="visible = false">
          {{ $t('commons.cancel') }}
        </el-button>
      </div>
    </div>

    <el-form :model="formData" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('commons.import_module')">
            <ms-select-tree size="small" :data="moduleOptions" :defaultKey="formData.moduleId" @getValue="setModule"
                            :obj="moduleObj" clearable checkStrictly/>
          </el-form-item>
          <el-form-item v-if="!isHar" :label="$t('commons.import_mode')">
            <el-select size="small" v-model="formData.modeId" class="project-select" clearable>
              <el-option v-for="item in modeOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-xpack v-if="projectVersionEnable && formData.modeId === 'incrementalMerge'"
                        :label="$t('api_test.api_import.import_version')" prop="versionId">
            <el-select size="small" v-model="formData.versionId" clearable style="width: 100%">
              <el-option v-for="item in versionOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-xpack v-if="projectVersionEnable && formData.modeId === 'fullCoverage'"
                        :label="$t('api_test.api_import.data_update_version')" prop="versionId">
            <el-select size="small" v-model="formData.updateVersionId" clearable style="width: 100%">
              <el-option v-for="item in versionOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-xpack v-if="projectVersionEnable && formData.modeId === 'fullCoverage'"
                        :label="$t('api_test.api_import.data_new_version')" prop="versionId">
            <el-select size="small" v-model="formData.versionId" clearable style="width: 100%">
              <el-option v-for="item in versionOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="1">
          <el-divider direction="vertical"/>
        </el-col>
        <el-col :span="12">
          <el-upload
              class="api-upload"
              drag
              action=""
              :http-request="upload"
              :limit="1"
              :beforeUpload="uploadValidate"
              :on-remove="handleRemove"
              :file-list="fileList"
              :on-exceed="handleExceed"
              multiple>
            <i class="el-icon-upload"></i>
            <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
            <div class="el-upload__tip" slot="tip">{{ $t('api_test.api_import.file_size_limit') }}</div>
          </el-upload>
        </el-col>

      </el-row>
    </el-form>

    <div class="format-tip">
      <div>
        <span>{{ $t('api_test.api_import.tip') }}：{{ selectedPlatform.tip }}</span>
      </div>
      <div>
        <span>{{ $t('导入方法：') }}：{{ selectedPlatform.exportTip }}</span>
      </div>
      <div>
        <!--        <span>-->
        <!--          {{ $t('api_test.api_import.import_cover_tip') }}<br/>-->
        <!--          {{ $t('api_test.api_import.cover_tip_1') }}<br/>-->
        <!--          {{ $t('api_test.api_import.cover_tip_2') }}<br/>-->
        <!--          {{ $t('api_test.api_import.cover_tip_3') }}-->
        <!--        </span>-->

        <span>
            1.当前版本默认只支持按照场景名称覆盖导入
        </span>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import {importScenario} from "@/api/scenario";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";

export default {
  name: "UiScenarioImport",
  components: {MsDialogFooter, MsSelectTree},
  props: {
    scenarioType: {
      type: String,
      default: "scenario"
    },
    saved: {
      type: Boolean,
      default: true,
    },
    moduleOptions: Array,
  },
  data() {
    return {
      visible: false,
      swaggerUrlEable: false,
      swaggerSynchronization: false,
      showEnvironmentSelect: true,
      modeOptions: [{
        id: 'fullCoverage',
        name: this.$t('commons.cover')
      }],
      protocol: "",
      platforms: [
        {
          name: 'SeleniumIDE',
          value: 'SeleniumIDE',
          tip: this.$t('ui.selenium_tip'),
          exportTip: this.$t('ui.selenium_export_tip'),
          suffixes: new Set(['side'])
        }
      ],
      selectedPlatform: {
        suffixes: new Set(['side'])
      },
      selectedPlatformValue: 'SeleniumIDE',
      result: {},
      projects: [],
      environments: [],
      useEnvironment: false,
      formData: {
        file: undefined,
        swaggerUrl: '',
        modeId: 'fullCoverage',
        moduleId: '',
      },
      rules: {},
      currentModule: {},
      fileList: [],
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      versionOptions: [],
      projectVersionEnable: false,
    }
  },
  activated() {
    this.selectedPlatform = this.platforms[0];
  },
  created() {
    this.getVersionOptions();
  },
  watch: {
    selectedPlatformValue() {
      for (let i in this.platforms) {
        if (this.platforms[i].value === this.selectedPlatformValue) {
          this.selectedPlatform = this.platforms[i];
          break;
        }
      }
      if (this.selectedPlatformValue === 'Har') {
        this.formData.modeId = 'fullCoverage';
      }
    }
  },
  computed: {
    isHar() {
      return this.selectedPlatformValue === 'Har';
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    scheduleEdit() {
      if (!this.formData.swaggerUrl) {
        this.$warning(this.$t('commons.please_fill_path'));
        this.swaggerSynchronization = !this.swaggerSynchronization
      } else {
        if (this.swaggerSynchronization) {
          this.$refs.scheduleEdit.open(this.buildParam());
        }
      }
    },
    scheduleEditByText() {
      this.$refs.scheduleEdit.open(this.buildParam());
    },
    open(module) {
      this.currentModule = module;
      this.visible = true;
      if (this.moduleOptions && this.moduleOptions.length > 0) {
        if (this.currentModule) {
          this.formData.moduleId = this.currentModule.id;
          this.formData.modulePath = this.currentModule.path;
        } else {
          this.formData.moduleId = this.moduleOptions[0].id;
          this.formData.modulePath = this.moduleOptions[0].path;
        }
      }
      listenGoBack(this.close);
    },
    upload(file) {
      this.formData.file = file.file;
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    handleRemove(file, fileList) {
      this.formData.file = undefined;
    },
    uploadValidate(file, fileList) {
      let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
      if (this.selectedPlatform.suffixes && !this.selectedPlatform.suffixes.has(suffix)) {
        this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
        return false;
      }
      if (file.size / 1024 / 1024 > 50) {
        this.$warning(this.$t('api_test.api_import.file_size_limit'));
        return false;
      }
      return true;
    },
    save() {
      if (!this.formData.file) {
        this.$warning("请添加一个文件");
        return;
      }
      let suffix = this.formData.file.name.substring(this.formData.file.name.lastIndexOf('.') + 1);
      if (this.selectedPlatform.suffixes && !this.selectedPlatform.suffixes.has(suffix)) {
        this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
        return false;
      }
      this.$set(this.result, "loading", true);

      this.$refs.form.validate(valid => {
        if (valid) {
          let param = this.buildParam();
          param.scenarioType = this.scenarioType;
          importScenario('/ui/automation/import', param.file, null, param).then(response => {
            let res = response.data;
            this.$success(this.$t('test_track.case.import.success'));
            this.visible = false;
            this.$emit('refreshAll', res);
            this.$set(this.result, "loading", false);
          });
        } else {
          this.$set(this.result, "loading", false);
          return false;
        }
      });
    },
    buildParam() {
      let param = {};
      Object.assign(param, this.formData);
      param.platform = this.selectedPlatformValue;
      param.saved = this.saved;
      if (this.currentModule) {
        param.moduleId = typeof this.formData.moduleId === Array ? "" : this.formData.moduleId;
        this.moduleOptions.filter(item => {
          if (item.id === this.formData.moduleId) {
            param.modulePath = item.path
          }
        })
        param.modeId = this.formData.modeId
      }
      param.projectId = this.projectId;
      if (!this.swaggerUrlEable) {
        param.swaggerUrl = undefined;
      }
      if (this.formData.moduleId && this.formData.moduleId.length === 0) {
        param.moduleId = ''
      }
      return param;
    },
    close() {
      this.formData = {
        file: undefined,
        swaggerUrl: '',
        modeId: this.formData.modeId,
      };
      this.fileList = [];
      removeGoBackListener(this.close);
      this.visible = false;
    },
    setModule(id, data) {
      this.formData.moduleId = id;
      this.formData.modulePath = data.path;
    },
    getVersionOptions() {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + getCurrentProjectID()).then(response => {
          this.versionOptions = response.data.filter(v => v.status === 'open');
          this.versionOptions.forEach(v => {
            if (v.latest) {
              v.name = v.name + ' ' + this.$t('api_test.api_import.latest_version');
            }
          });
        });
      }
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId).then(response => {
          // :todo 先不支持版本导入，默认最新
          // this.projectVersionEnable = response.data;
        });
      }
    }
  }
}
</script>

<style scoped>

.api-import :deep(.el-dialog) {
  min-width: 700px;
}

.format-tip {
  background: #EDEDED;
}

.api-upload {
  text-align: center;
  margin: auto 0;
}

.api-upload :deep(.el-upload) {
  width: 100%;
  max-width: 350px;
}

.api-upload :deep(.el-upload-dragger) {
  width: 100%;
}

.el-radio-group {
  margin: 10px 0;
}

.header-bar, .format-tip, .el-form {
  border: solid #E1E1E1 1px;
  margin: 10px 0;
  padding: 10px;
  border-radius: 3px;
}

.header-bar {
  padding: 10px 30px;
}

.api-import :deep(.el-dialog__body) {
  padding: 15px 25px;
}

.operate-button {
  float: right;
}

.save-button {
  margin-left: 10px;
}

.el-form {
  padding: 30px 10px;
}

.dialog-footer {
  float: right;
}

.swagger-url-disable {
  margin-top: 10px;

  margin-left: 80px;
}

.el-divider {
  height: 200px;
}

</style>
