<template>
  <el-button
    v-if="showButton && hasScreenshotRc"
    class="export-button"
    plain
    size="mini"
    @click="downloadResources"
    type="success"
  >
    {{ $t("ui.downloadScreenshot") }}
  </el-button>
</template>
<script>
import { downloadScreenshotExport } from "@/business/automation/ui-automation";
import allCommandDefinition from "@/business/definition/command/all-command-definition";
export default {
  name: "UiDownloadScreenshot",
  data() {
    return {
      screenshots: {
        reportId: "",
        dirName: "",
        resources: [],
      },
      hasScreenshotRc: false,
      commandNameMap: new Map(),
    };
  },
  props: {
    showButton: {
      type: Boolean,
      default: true,
    },
    report: {},
  },
  watch: {
    report: {
      immediate: true,
      deep: true,
      handler() {
        this.initCommandMap();
        this.handleScreenshot();
        this.hasScreenshotRc = this.hasScreenshot();
      },
    },
  },
  methods: {
    initCommandMap() {
      if (
        !allCommandDefinition ||
        (this.commandNameMap && this.commandNameMap.size > 0)
      ) {
        //保证只处理一次即可
        return;
      }
      allCommandDefinition.forEach((cmd) => {
        if (!this.commandNameMap.get(cmd.name)) {
          this.commandNameMap.set(cmd.name, cmd.name);
        }
      });
    },
    hasScreenshot() {
      return (
        this.screenshots &&
        this.screenshots.resources &&
        this.screenshots.resources.length > 0
      );
    },
    downloadResources() {
      //处理下载逻辑
      this.downloadFile();
    },
    handleScreenshot() {
      if (!this.report.content) {
        return;
      }
      //处理文件层级数据
      let source = JSON.parse(this.report.content);
      //获取文件路径
      let steps = source.steps || [];

      this.screenshots.dirName = this.handleFileName(source.name);
      this.screenshots.reportId = this.report.id;
      this.cherryPickScreenshot(steps, "");
    },
    downloadFile() {
      if (
        !this.screenshots ||
        !this.screenshots.resources ||
        this.screenshots.resources.length <= 0
      ) {
        return;
      }

      downloadScreenshotExport(
        this.screenshots,
        this.screenshots.dirName + ".zip"
      );
    },
    cherryPickScreenshot(steps, parentIndex) {
      for (let i = 0; i < steps.length; i++) {
        let step = steps[i];
        //步骤名称
        let label = this.compensate(step.label);
        //当前步骤层级的序号
        let index = step.index;

        if (step.value && step.value.combinationImg) {
          let meta = {};
          meta.index = index;
          meta.label = label;
          meta.combinationImg = step.value.combinationImg;
          let fileIndex = this.generateFileIndex(parentIndex, index);
          meta.fileName = this.generateFileName(fileIndex, label);
          this.screenshots.resources.push(meta);
        }

        let children = step.children;
        if (children && children.length > 0) {
          //递归处理子级
          this.cherryPickScreenshot(children, index);
        }
      }
    },
    compensate(name) {
      let key = "ui." + name;
      if (this.commandNameMap.get(name) && this.$t(key) !== key) {
        return this.$t(key);
      }
      return name;
    },
    handleFileName(fileName) {
      fileName = fileName.replaceAll("[/\\\\:*?|]", "");
      fileName = fileName.replaceAll('["<>]', "");
      fileName = fileName.replaceAll("[\.\^]", "");
      return fileName;
    },
    generateFileIndex(parentIndex, index) {
      //生成文件索引
      if (parentIndex) {
        return parentIndex + "-" + index;
      }
      return index;
    },
    generateFileName(fileIndex, label) {
      //生成文件名
      if (fileIndex) {
        return fileIndex + "-" + this.handleFileName(label);
      }
      return this.handleFileName(label);
    },
  },
};
</script>
<style lang=""></style>
