<template>
  <div v-if="showHide" style="width: 618px; background-color: #fff;" >
  <el-upload
    action="#"
    class="ms-upload-header"
    list-type="picture-card"
    :file-list="parameter.files"
    ref="upload">
    <div class="upload-default" @click.stop>
      <el-popover
        placement="right"
        trigger="hover">
        <el-button type="text" @click="showFileUpload=true"> {{ $t('permission.project_file.upload_file') }}</el-button><br>
        <el-button type="text" @click="associationFile">{{ $t('permission.project_file.associated_files') }}</el-button>
        <i class="el-icon-plus" slot="reference"/>
      </el-popover>
    </div>
    <div class="upload-item" slot="file" slot-scope="{file}">
      <span>{{ file.file && file.file.name ? file.file.name : file.name }}</span>
        <span class="el-upload-list__item-actions">
            <span v-if="!disabled" class="ms-list__item-delete" @click="handleRemove(file)">
                <i class="el-icon-unlock"/>
                <span style="font-size: 13px;">
                  {{ file.isExist ? $t('permission.project_file.file_delete_tip') : '' }}
                </span>
            </span>
        </span>

    </div>
  </el-upload>
  <el-dialog :close-on-click-modal="false" :title="$t('permission.project_file.upload_file')" width="680px"
               :visible.sync="showFileUpload" class="api-import"  @close="close"  v-loading="loading">
      <el-row style="padding-bottom: 20px">
        <el-col :span="3" >
          <span>{{$t("file_manage.file_module")}}</span>
        </el-col>
        <el-col :span="21" >
          <ms-select-tree  class="ms_tree"  :data="moduleOptions" :defaultKey="moduleId" @getValue="setModule"
                          :obj="moduleObj" clearable checkStrictly/>
        </el-col>
      </el-row>
    <el-upload
        class="api-upload"
        drag
        action=""
        :http-request="upload"
        :limit="1"
        :beforeUpload="uploadValidate"
        :on-remove="handleRemoveUpload"
        :file-list="bodyUploadFiles"
        :on-exceed="handleExceed"
        multiple>
      <i class="el-icon-upload"></i>
      <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
      <div class="el-upload__tip" slot="tip">{{ $t('file_manage.file_size_limit') }}</div>
      <div class="file_upload_tip" slot="tip" v-if="fileExist">{{ $t('file_manage.file_module_already_exists') }}</div>
    </el-upload>
    <el-button type="primary" class="save_file" v-if="!fileExist" @click="saveFile"> {{ $t('commons.confirm') }}</el-button><br>
    <el-button class="save_file_false" :disabled="true"   @click="saveFile" v-show="fileExist"> {{ $t('commons.confirm') }}</el-button><br>

  </el-dialog>
  <ms-file-batch-move ref="module" @setModuleId="setModuleId"/>
  <el-dialog :close-on-click-modal="false" :visible="dialogVisible" width="680px" :title="$t('permission.project_file.associated_files')"
             @close="closeMeta" destroy-on-close ref="baseRelevance" append-to-body class="relevance_class">
    <relevance-file-list ref="metadataList" :selectFiles="parameter.files" @checkRows="checkRows" />
    <template v-slot:footer>
      <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="saveCaseRelevance"/>
    </template>
  </el-dialog>

  </div>
</template>

<script>
import MsFileBatchMove from "@/business/definition/file/module/FileBatchMove";
import MsFileMetadataList from "@/business/definition/file/quote/QuoteFileList";
import {getUUID} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import {fileExists, dumpFile, getTypeNodeByProjectId} from "metersphere-frontend/src/api/file-metadata";
import {createFileMeta, fileNameExists} from "@/api/file";
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree.vue";
import RelevanceFileList from "@/components/file/list/RelevanceFileList.vue";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter.vue";


export default {
  name: "ApiBodyFileUpload",
  components: {
    MsDialogFooter,
    RelevanceFileList,
    MsSelectTree,
    MsFileBatchMove,
    MsFileMetadataList
  },
  data() {
    return {
      file: {},
      showHide: true,
      showFileUpload: false,
      dialogVisible:false,
      moduleOptions:[],
      moduleId:'',
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      bodyUploadFiles:[],
      fileExist:false,
      loading:false
    };
  },
  props: {
    parameter: Object,
    id: String,
    default() {
      return {}
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    exist() {
      let fileIds = [];
      this.parameter.files.forEach(file => {
        if (file.storage === 'FILE_REF' && file.fileId) {
          fileIds.push(file.fileId);
        }
      });
      if (fileIds.length > 0) {
        this.result = fileExists(fileIds).then(response => {
          let resultIds = response.data;
          this.parameter.files.forEach((file) => {
            if (file.storage === 'FILE_REF' && resultIds.indexOf(file.fileId) === -1) {
              file.isExist = true;
            }
          });
          this.reload();
        });
      }
    },
    reload() {
      this.showHide = false;
      this.$nextTick(() => {
        this.showHide = true;
      })
    },
    setModuleId(moduleId) {
      let files = [];
      if (this.file && this.file.file) {
        files.push(this.file.file);
      }
      let request = {
        id: getUUID(),
        resourceId: "ui/"+getCurrentProjectID()+"/"+this.id,
        moduleId: moduleId,
        projectId: getCurrentProjectID(),
        fileName: this.file.name,
      };
      dumpFile(null, files, request).then((response) => {
        this.$success(this.$t('organization.integration.successful_operation'));
      });
    },
    checkRows(rows) {
      rows.forEach(item => {
        let file = {name: item.name, id: getUUID(), fileId: item.id, storage: "FILE_REF", projectId: item.projectId, fileType: item.type};
        this.parameter.files.push(file);
      })
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'));
    },
    handleRemove(file) {
      if (file && this.parameter.files) {
        for (let i = 0; i < this.parameter.files.length; i++) {
          let fileName = file.file ? file.file.name : file.name;
          let paramFileName = this.parameter.files[i].file ?
            this.parameter.files[i].file.name : this.parameter.files[i].name;
          if (fileName === paramFileName) {
            this.parameter.files.splice(i, 1);
            break;
          }
        }
      }
    },
    handleRemoveUpload(file){
      if (file && this.bodyUploadFiles){
        for (let i = 0; i < this.bodyUploadFiles.length; i++) {
          let fileName = file.file ? file.file.name : file.name;
          let paramFileName = this.bodyUploadFiles[i].file ?
              this.bodyUploadFiles[i].file.name : this.bodyUploadFiles[i].name;
          if (fileName === paramFileName) {
            this.bodyUploadFiles.splice(i, 1);
            break;
          }
        }
      }
      this.fileExist = false;
    },
    upload(file) {
      this.file = file;
    },
    uploadValidate(file) {
      if (file.size / 1024 / 1024 > 50) {
        this.$warning(this.$t('api_test.request.body_upload_limit_size'));
        return false;
      }
      if (this.parameter.files) {
        for (let i = 0; i < this.parameter.files.length; i++) {
          let fileName = file.file ? file.file.name : file.name;
          let paramFileName = this.parameter.files[i].file ?
              this.parameter.files[i].file.name : this.parameter.files[i].name;
          if (fileName === paramFileName) {
            this.$warning(this.$t("file_manage.file_name_already_exists"));
          }
        }
      }
      if (this.bodyUploadFiles) {
        for (let i = 0; i < this.bodyUploadFiles.length; i++) {
          let fileName = file.file ? file.file.name : file.name;
          let paramFileName = this.bodyUploadFiles[i].file ?
              this.bodyUploadFiles[i].file.name : this.bodyUploadFiles[i].name;
          if (fileName === paramFileName) {
            this.$warning(this.$t("file_manage.file_name_already_exists"));
            return false;
          }
        }
      }
      let request = {
        createUser: getCurrentUserId(),
        updateUser: getCurrentUserId(),
        projectId: getCurrentProjectID(),
        moduleId: this.moduleId,
        name: file.file ? file.file.name : file.name,
        type:this.getFileType(file)
      }
      fileNameExists(request).then(res => {
          if (res.data && res.data===true) {
            this.fileExist = true;
            return false;
          }
      });
      return true;
    },
    getFileType(file){
      let fileName = file.file ? file.file.name : file.name;
      let s = fileName.lastIndexOf(".") + 1;
      return  fileName.substring(s).toUpperCase()
    },
    getFileModules(){
      let projectId = getCurrentProjectID();
      getTypeNodeByProjectId(projectId, "module").then(res => {
        if (res.data) {
          res.data.forEach(node => {
            node.name = node.name === 'DEF_MODULE' ? this.$t('commons.module_title') : node.name
          });
          this.moduleOptions = res.data
          this.moduleId =  this.moduleOptions[0].id
        }
      })
    },
    associationFile() {
      this.dialogVisible = true;
      if (this.$refs.metadataList) {
        this.$refs.metadataList.open()
      }
    },
    close(){
      this.showFileUpload = false;
      this.fileExist = false;
    },
    saveFile(){
      if (!this.moduleId) {
        this.$warning(this.$t("file_manage.file_module_is_null"));
        return;
      }
      if (!this.file.file) {
        this.$warning(this.$t("file_manage.file_is_null"));
        return;
      }
      this.loading = true;
      let request = {
        createUser: getCurrentUserId(),
        updateUser: getCurrentUserId(),
        projectId: getCurrentProjectID(),
        moduleId: this.moduleId,
      }
      createFileMeta(this.file.file, request, (response) => {
        if (response.data){
          let item = response.data[0];
          let file = {name: item.name, id: item.id, fileId: item.id, storage: "FILE_REF", projectId: item.projectId, fileType: item.type};
          this.parameter.files.push(file);
          this.bodyUploadFiles.push(file)
        }
        this.showFileUpload = false;
        this.bodyUploadFiles = [];
        this.loading = false;
      }, (error)=>{
        this.showFileUpload = false;
        this.loading = false;
      });

    },
    setModule(id, data) {
      this.moduleId = id;
    },
    closeMeta(){
      this.dialogVisible = false
    },
    async saveCaseRelevance() {
      let selectRows = this.$refs.metadataList.getSelectRows();
      this.$emit("setSelectFiles", selectRows);
      this.closeMeta();
    },
  },
  created() {
    if (!this.parameter.files) {
      this.parameter.files = [];
    }
    this.exist();
    this.getFileModules();
  }
}
</script>
<style>
.el-popover {
  min-width: 60px;
}
</style>
<style scoped>

.el-upload {
  background-color: black;
}

.ms-upload-header :deep(.el-upload) {
  height: 30px;
  width: 32px;
}

.upload-default {
  min-height: 30px;
  width: 32px;
  line-height: 32px;
}

.el-icon-plus {
  font-size: 16px;
}

.ms-upload-header :deep(.el-upload-list__item) {
  height: 30px;
  width: auto;
  padding: 2px 5px;
  margin-bottom: 0px;
}

.ms-upload-header :deep(.el-upload-list--picture-card) {
}

.ms-upload-header {
  min-height: 30px;
  border: 1px solid #DCDFE5;
  padding: 2px;
  border-radius: 4px;
  line-height: 10px;
}

.ms-body-upload {
  min-height: 0px;
  height: 30px;
  border: 0px;
  padding: 0px;
  border-radius: 0px;

}

.upload-item {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  width: 180px;
}

.ms-upload-header :deep(.el-upload-list--picture-card) {
  display: inline;
  white-space: normal;
}

.ms-upload-header :deep(.el-upload-list--picture-card .el-upload-list__item) {
  display: inline-block;
}

.ms-list__item-delete {
  margin-top: -12px;
  text-align: center;
  vertical-align: middle;
}
.api-upload{
  display: flex;
  flex-wrap: nowrap;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  margin-left: 81px;
}
.ms_tree :deep(.el-select > .el-input) {
  width: 199% !important;
}
.ms_tree :deep(.el-input--small .el-input__inner) {
  height: 32px;
  line-height: 32px;
   width: 100%;
}
.relevance_class :deep(.el-dialog__body){
    padding: 0 20px;
}
.save_file{
  float: inline-end;
}
.save_file_false{
  float: inline-end;
  background-color: #606266;
  z-index: 1000;
  color: white;
}
.file_upload_tip{
  font-size:12px;
  color: red;
  margin-top:7px
}
</style>
