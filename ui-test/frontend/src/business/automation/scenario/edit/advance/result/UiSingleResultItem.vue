<template>
  <div class="script-content">
    <div>
      <el-popover
        placement="right"
        trigger="hover"
        v-if="result.uiImg"
        popper-class="issues-popover"
      >
        <el-image
          style="width: 100px; height: 100px"
          :src="imgPrefix + result.uiImg + '&reportId=' + result.reportId"
          :preview-src-list="[
            imgPrefix + result.uiImg + '&reportId=' + result.reportId,
          ]"
        >
        </el-image>
        <el-button slot="reference" type="text">{{
          $t("ui.screenshot")
        }}</el-button>
      </el-popover>

      <div
        @click.stop="triggerViewer"
        v-if="!result.uiImg && combinationImg"
        style="color: #783887; cursor: pointer"
      >
        {{ $t("ui.screenshot") }}
      </div>
      <ui-screenshot-viewer
        ref="screenshotViewer"
        v-if="!result.uiImg && combinationImg"
        :src="imgPrefix + combinationImg + '&reportId=' + result.reportId"
      />
    </div>

    <ms-code-edit
      mode="text"
      :data="result.body"
      theme="eclipse"
      :read-only="true"
      :modes="['text']"
      ref="codeEdit"
    />
  </div>
</template>

<script>
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import UiScreenshotViewer from "@/business/automation/report/UiScreenshotViewer";

export default {
  name: "UiSingleResultItem",
  components: { MsCodeEdit, UiScreenshotViewer },
  props: {
    result: {
      type: Object,
      default() {
        return {};
      },
    },
    index: {
      type: Number,
      default: -1,
    },
    images: {
      type: Array,
      default() {
        return [];
      },
    },
  },
  data() {
    return {
      text: "",
      combinationImg: "",
      // imgPrefix: "/ui/resource/ui/get?fileName=",
      imgPrefix: "/resource/ui/get?fileName=",
    };
  },
  watch: {
    result: {
      deep: true,
      handler() {
        this.getResult();
      },
    },
  },
  mounted() {
    this.getResult();
  },
  methods: {
    triggerViewer() {
      this.$refs.screenshotViewer.open();
    },
    getResult() {
      // return this.result.success && !this.result.body
      //   ? "OK!"
      //   : this.result.body;
      this.combinationImg = this.result.combinationImg;
      this.text = this.result.body;
    },
  },
};
</script>

<style scoped>
.script-content {
  height: calc(100vh - 570px);
  min-height: 440px;
}
</style>
