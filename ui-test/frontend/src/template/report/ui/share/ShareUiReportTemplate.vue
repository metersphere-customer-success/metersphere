<template>
  <UiShareReportDetail :report-id="reportId" :share-id="shareId" :is-share="isShare" :is-plan="true" :show-cancel-button="false" ></UiShareReportDetail>
</template>

<script>
import {getShareId} from "@/api/share";
import {getShareInfo} from "@/api/share";
import UiShareReportDetail from "@/business/automation/report/UiShareReportDetail"
export default {
  name: "ShareUiReportTemplate",
  components: {UiShareReportDetail},
  data() {
    return {
      reportId: '',
      shareId: '',
      isShare: true,
    };
  },
  created() {
    this.shareId = getShareId();
    getShareInfo(this.shareId).then((data) => {
      if (!data || !data.data) {
        this.$error('链接已失效，请重新获取!');
        return;
      }
      this.reportId = data.data.customData;
    });
  },
};
</script>

<style scoped>
</style>
