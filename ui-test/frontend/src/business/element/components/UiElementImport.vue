<template>
  <el-dialog
      :close-on-click-modal="false"
      :title="$t('ui.el_import')"
      width="30%"
      :visible.sync="visible"
      class="api-import"
      v-loading="result.loading"
      @close="close"
  >
    <div class="header-bar">
      <div>{{ $t("api_test.api_import.data_format") }}</div>
      <el-radio-group v-model="selectedPlatformValue">
        <el-radio
            v-for="(item, index) in platforms"
            :key="index"
            :label="item.value"
        >{{ item.name }}
        </el-radio
        >
      </el-radio-group>

      <div class="operate-button">
        <el-button class="save-button" type="primary" plain @click="save">
          {{ $t("commons.save") }}
        </el-button>
        <el-button @click="close">
          {{ $t("commons.cancel") }}
        </el-button>
      </div>
    </div>

    <el-form
        :model="formData"
        :rules="rules"
        label-width="100px"
        v-loading="result.loading"
        ref="form"
    >
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('ui.import_template') + ':'" class="download-wrap">
            <div class="download-content" @click="download">
              <div class="opt-icon"><i class="el-icon-download"></i></div>
              <div class="opt-title">{{  $t("ui.download_template") }}</div>
            </div>
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
              multiple
          >
            <i class="el-icon-upload"></i>
            <div
                class="el-upload__text"
                v-html="$t('load_test.upload_tips')"
            ></div>
            <div class="el-upload__tip" slot="tip">
              {{ $t("api_test.api_import.file_size_limit") }}
            </div>
          </el-upload>
        </el-col>
      </el-row>
    </el-form>

    <div class="format-tip">
      <div>
        <span>{{ $t("ui.import_desc") }}:</span>
      </div>
      <div>
        <span>{{ $t("ui.el_import_tip_1") }}</span>
      </div>
      <div>
        <span>{{ $t("ui.el_import_tip_2") }}</span>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import {importElement} from "@/business/network/ui-element";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";

export default {
  name: "UiElementImport",
  components: {MsDialogFooter, MsSelectTree},
  data() {
    return {
      visible: false,
      platforms: [
        {
          name: "Excel",
          value: "Excel",
          tip: this.$t("ui.selenium_tip"),
          exportTip: this.$t("ui.selenium_export_tip"),
          suffixes: new Set(["xls", "xlsx"]),
        },
      ],
      selectedPlatform: {
        suffixes: new Set(["xls", "xlsx"]),
      },
      selectedPlatformValue: "Excel",
      result: {},
      formData: {file: undefined},
      rules: {},
      fileList: [],
    };
  },
  activated() {
    this.selectedPlatform = this.platforms[0];
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    download() {
      this.$fileDownload(
          "/ui/element/export/template/" + this.projectId + "/1"
      );
    },
    open() {
      this.visible = true;
      listenGoBack(this.close);
    },
    upload(file) {
      this.formData.file = file.file;
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t("test_track.case.import.upload_limit_count"));
    },
    handleRemove(file, fileList) {
      this.formData.file = undefined;
    },
    uploadValidate(file, fileList) {
      let suffix = file.name.substring(file.name.lastIndexOf(".") + 1);
      if (
          this.selectedPlatform.suffixes &&
          !this.selectedPlatform.suffixes.has(suffix)
      ) {
        this.$warning(this.$t("api_test.api_import.suffixFormatErr"));
        return false;
      }
      if (file.size / 1024 / 1024 > 50) {
        this.$warning(this.$t("api_test.api_import.file_size_limit"));
        return false;
      }
      return true;
    },
    save() {
      if (!this.formData.file) {
        this.$warning(this.$t("test_track.case.import.import_file_tips"));
        return;
      }
      let suffix = this.formData.file.name.substring(
          this.formData.file.name.lastIndexOf(".") + 1
      );
      if (
          this.selectedPlatform.suffixes &&
          !this.selectedPlatform.suffixes.has(suffix)
      ) {
        this.$warning(this.$t("api_test.api_import.suffixFormatErr"));
        return false;
      }
      let param = {
        projectId: this.projectId,
      };
      this.result.loading = true;
      importElement(
          "/ui/element/import",
          this.formData.file,
          null,
          param).then((response) => {
            let res = response.data;
            if (res.success) {
              this.$success(this.$t("test_track.case.import.success"));
              //刷新模块树
              this.$emit("refreshTree");
              this.$emit("refreshTable");
              // this.$emit("close", res.isUpdated);
              this.close();
            } else {
              this.errList = res.errList;
              this.showUploadErrMsg(this.errList);
            }
            this.result.loading = false;
          }
      );
    },
    close() {
      this.formData = {
        file: undefined,
      };
      this.fileList = [];
      removeGoBackListener(this.close);
      this.visible = false;
    },
    showUploadErrMsg(errList) {
      if (!errList) {
        return;
      }
      let htmlStr = "";
      let height = 0;
      for (let err of errList) {
        htmlStr += err.errMsg || "";
        htmlStr += "</br></br>";
        if (height < 150) {
          height += 25;
        }
      }
      let scroll = "";
      if (height > 25) {
        scroll = `overflow-y:scroll;`;
      }
      this.$message({
        type: "warning",
        showClose: true,
        dangerouslyUseHTMLString: true,
        duration: 6000,
        message: `
      <div style='width:400px;height:${height}px;${scroll}margin-top:10px;'>
        ${htmlStr}
      `,
      });
    },
  },
};
</script>

<style scoped>
.download-wrap {
  overflow: hidden;
  margin-top: 30px;
}

.download-content {
  display: flex;
  font-weight: 400;
  font-size: 14px;
  cursor: pointer;
}

.opt-icon {
  width: 20px;
  height: 20px;
  color: var(--primary_color);
  margin-right: 1px;
}

.opt-title {
  color: var(--primary_color);
}

.api-import :deep(.el-dialog) {
  min-width: 700px;
}

.format-tip {
  background: #ededed;
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

.header-bar,
.format-tip,
.el-form {
  border: solid #e1e1e1 1px;
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
