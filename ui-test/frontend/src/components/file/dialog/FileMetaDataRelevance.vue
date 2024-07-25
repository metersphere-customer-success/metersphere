<template>
<!--  <relevance-base-->
  <!--      @setProject="setProject"-->
  <!--      @save="saveCaseRelevance"-->
  <!--      :plan-id="planId"-->
  <!--      :dialog-title="$t('ui.relevant_file')"-->
  <!--      ref="baseRelevance">-->
    <el-dialog :close-on-click-modal="false" :visible="dialogVisible" width="1000px"
               @close="close" destroy-on-close ref="baseRelevance" append-to-body>
      <el-card class="table-card">

        <!--    <template v-slot:aside>-->
        <!--      <file-module-->
        <!--          style="margin-top:15px;"-->
        <!--          @nodeSelectEvent="nodeChange"-->
        <!--          @refreshTable="refresh"-->
        <!--          @setModuleOptions="setModuleOptions"-->
        <!--          :relevance-project-id="projectId"-->
        <!--          :is-read-only="true"-->
        <!--          :show-case-num="false"-->
        <!--          @myFile="myFile"-->
        <!--          ref="nodeTree"/>-->
        <!--    </template>-->

        <relevance-file-list
            :select-node-ids="selectNodeIds"
            :trash-enable="trashEnable"
            :version-enable="versionEnable"
            :project-id="projectId"
            @selectCountChange="setSelectCounts"
            ref="fileList"/>
        <!--  </relevance-base>-->
      </el-card>
      <template v-slot:footer>
        <ms-dialog-footer
            @cancel="dialogVisible = false"
            @confirm="saveCaseRelevance"/>
      </template>
    </el-dialog>
</template>

<script>
import RelevanceBase from "@/components/base/RelevanceBase";
import FileModule from "@/components/file/module/FileModule";
import RelevanceFileList from "@/components/file/list/RelevanceFileList";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer"
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter"

export default {
  name: "FileMetaDataRelevance",
  components: {
    FileModule,
    RelevanceBase,
    RelevanceFileList,
    MsMainContainer,
    MsDialogFooter,
  },
  data() {
    return {
      showCasePage: true,
      currentProtocol: null,
      currentModule: null,
      selectNodeIds: [],
      moduleOptions: {},
      trashEnable: false,
      condition: {},
      currentRow: {},
      projectId: "",
      dialogVisible: false,
    };
  },
  props: {
    planId: {
      type: String
    },
    versionEnable: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    projectId() {
      this.refresh();
    },
  },
  methods: {
    open() {
      this.dialogVisible = true;
      if (this.$refs.fileList) {
        this.$refs.fileList.getProjectFiles();
      }
    },
    close(){
      this.dialogVisible = false
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.$refs.fileList.projectId = projectId;
      this.$refs.nodeTree.projectId = projectId;
      this.refresh();
    },

    refresh() {
      this.$refs.fileList.getProjectFiles(this.projectId);
      this.$refs.nodeTree.list(this.projectId);
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.$refs.fileList.clearCreateUser();
    },
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    getAllId(param) {
      return new Promise((resolve) => {
        testPlanUiScenarioRelevanceListIds(param)
            .then((r) => {
              resolve(r.data);
            });
      });
    },
    async saveCaseRelevance() {
      let selectIds = [];
      let selectRows = this.$refs.fileList.getSelectRows();
      this.$emit("setSelectFiles", selectRows);
      this.close();
    },
    autoCheckStatus() { //  检查执行结果，自动更新计划状态
      if (!this.planId) {
        return;
      }
      testPlanAutoCheck(this.planId);
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
    myFile() {
      this.$refs.fileList.myFile();
    },
  }
}
</script>

<style scoped>

</style>