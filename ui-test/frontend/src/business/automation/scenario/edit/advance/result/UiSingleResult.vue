<template>
  <div v-if="finalResults">
    <el-tabs v-if="isFinalResult" closable class="ms-tabs">
      <template v-for="(item, i) in finalResults">
        <el-tab-pane
          :label="'循环' + (i + 1)"
          :key="i"
          style="margin-bottom: 5px"
        >
          <ui-single-result-item :result="item" :index="i" :images="images" />
        </el-tab-pane>
      </template>
    </el-tabs>
    <ui-single-result-item
      v-else
      :result="finalResults"
      :images="images"
      :index="0"
    />
  </div>
</template>

<script>
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import UiSingleResultItem from "@/business/automation/scenario/edit/advance/result/UiSingleResultItem";
import { getScenarioReport, getStepResult } from "@/network/api";

export default {
  name: "UiSingleResult",
  components: { UiSingleResultItem, MsCodeEdit },
  props: {
    results: {
      type: [Object, Array],
      default() {
        return {};
      },
    },
    currentCommand: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  computed: {
    isMultipleResult() {
      return this.results instanceof Array;
    },
    isFinalResult() {
      return this.finalResults instanceof Array;
    },
  },
  data() {
    return {
      TIMER: -1,
      COUNTER: 3,
      images: [],
      index: 0,
      finalResults: undefined,
    };
  },
  mounted() {
    //获取所需的图
    // this.COUNTER = 3;
    // this.images = [];
    // if (this.results) {
    //   this.findCambinationImg();
    // }
    //开启定时器
    this.startTimer();
  },
  beforeDestroy() {
    console.log("Timer has been closed ~");
    this.clearTimer();
  },
  methods: {
    startTimer() {
      this.TIMER = setInterval(() => {
        console.log("Timer start fetch result ~");
        this.fetchStepResult();
      }, 1000);
    },
    fetchStepResult() {
      getStepResult(this.getReportId(), this.currentCommand.id).then((res) => {
        let arr = res.data;
        this.postProcessorStepResult(arr);
        console.log("结果生成", this.finalResults);
      });
    },
    postProcessorStepResult(data) {
      if (!data || data.length <= 0) {
        //还没获取到结果
        return;
      }

      if (data.length == 1) {
        //单条
        console.log("处理单条结果集：", data);
        let baseInfo = JSON.parse(data[0].baseInfo);
        let content = JSON.parse(data[0].content);

        //组装结果集
        this.finalResults = this.assembleSingleResult(content, baseInfo);
        //清除定时器
        this.clearTimer();
        return;
      }

      //循环
      console.log("处理循环结果集：", data);
      this.finalResults = [];
      //按照开始时间排序
      data
        .sort((o1, o2) => {
          return o1.createTime - o2.createTime;
        })
        .forEach((o) => {
          let baseInfo = JSON.parse(o.baseInfo);
          let content = JSON.parse(o.content);
          let r = this.assembleSingleResult(content, baseInfo);
          this.finalResults.push(r);
        });
      this.clearTimer();
    },
    assembleSingleResult(content, baseInfo) {
      let r = {};
      r.success = baseInfo.reqSuccess;
      r.uiImg = baseInfo.uiImg;
      r.body =
        r.success && !content.body ? "OK!" : this.handleErrorBody(content);
      r.combinationImg = content.combinationImg;
      r.reportId = this.getReportId();
      return r;
    },
    handleErrorBody(content) {
      let errLog = "";
      if (content.body) {
        errLog = errLog + content.body + "\n";
      }
      //处理断言结果
      if (content.assertions && content.assertions.length > 0) {
        content.assertions.forEach((ass) => {
          if (!ass.pass) {
            errLog = errLog + ass.message + "\n";
          }
        });
      }

      return errLog;
    },

    getReportId() {
      let reportId = "";
      if (this.isMultipleResult) {
        reportId = this.results[0].reportId;
      } else {
        reportId = this.results.reportId;
      }
      return reportId;
    },
    clearTimer() {
      clearInterval(this.TIMER);
    },

    /**
     *   废弃
     */
    findCambinationImg() {
      clearInterval(this.TIMER);
      let reportId = "";
      if (this.isMultipleResult) {
        reportId = this.results[0].reportId;
      } else {
        reportId = this.results.reportId;
      }
      if (!reportId) {
        return;
      }
      this.TIMER = setInterval(() => {
        getScenarioReport(reportId).then((data) => {
          this.COUNTER = this.COUNTER - 1;
          if (this.COUNTER < 0) {
            clearInterval(this.TIMER);
          }
          if (!data.data.content) {
            return;
          }
          let content = JSON.parse(data.data.content) || {};
          let steps = content.steps || [];
          if (this.isMultipleResult) {
            this.doFindCambinationImg(steps, this.results[0].id, false);
          } else {
            this.doFindCambinationImg(steps, this.results.id, true);
          }
          if (this.images && this.images.length > 0) {
            clearInterval(this.TIMER);
          }
        });
      }, 1000);
    },

    doFindCambinationImg(steps, id, needBreak) {
      if (!steps) {
        return;
      }
      let findChildren = false;
      for (let step of steps) {
        if (step.resourceId === id || findChildren) {
          if (step.value) {
            let combinationImg = step.value.combinationImg;
            this.images.push({
              index: this.index,
              img: combinationImg,
            });
            this.index = this.index + 1;
            if (needBreak) {
              break;
            }
            findChildren = true;
          }
        }

        //从uiScreenshots中找
        if (step.value && step.value.uiScreenshots) {
          let uiScreenshots = step.value.uiScreenshots || [];
          uiScreenshots.forEach((e) => {
            if (id === e.stepId) {
              let combinationImg = step.value.combinationImg;
              this.images.push({
                index: this.index,
                img: combinationImg,
              });
              this.index = this.index + 1;
              if (needBreak) {
                return;
              }
            }
          });
          if (this.images && this.images.length > 0 && needBreak) {
            break;
          }
        }

        if (step.children && step.children.length > 0) {
          this.doFindCambinationImg(step.children, id, needBreak);
        }
      }
      findChildren = false;
    },
  },
};
</script>

<style scoped></style>
