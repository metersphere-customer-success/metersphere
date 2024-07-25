<template>
  <div class="request-result">
    <div>
      <el-row type="flex" justify="end" class="info">
        <el-col :span="24">
          <div class="time">
            {{ $t('api_report.start_time') }}：{{ startTime }}
            {{ $t('report.test_end_time') }}：{{ endTime }}
          </div>
        </el-col>
      </el-row>
    </div>
    <el-collapse-transition>
      <div v-show="isActive">
        <div class="text-container">
          <el-collapse-transition>
            <el-tabs value="body" v-show="isActive">
              <el-tab-pane :label="$t('ui.log')" name="body" class="pane">
                <div class="ms-div">
                  <pre>{{ getBody() }}</pre>
                </div>
              </el-tab-pane>

              <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane assertions">
                <ms-assertion-results :show-content="false" :assertions="result.assertions"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.request.extract.label')" name="label" class="pane">
                <pre>{{ result.vars }}</pre>
              </el-tab-pane>
            </el-tabs>
          </el-collapse-transition>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script>
import {BODY_FORMAT} from "@/api/ApiTestModel";
import MsAssertionResults from "@/business/automation/report/components/AssertionResults";
import {fomartDate} from "@/business/util/dateTime";

export default {
  name: "UiCommandResultDetail",
  components: {
    MsAssertionResults
  },
  props: {
    request: Object,
    scenarioName: String,
    requestType: String,
    console: String,
    result: {
      type: Object,
      default() {
        return {}
      }
    },
  },

  data() {
    return {
      isActive: true,
      isCodeEditAlive: true,
      modes: ['text', 'json', 'xml', 'html'],
      sqlModes: ['text', 'table'],
      mode: BODY_FORMAT.TEXT
    }
  },

  methods: {
    active() {
      this.isActive = !this.isActive;
    },
    reload() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
    modeChange(mode) {
      this.mode = mode;
    },
    sqlModeChange(mode) {
      this.mode = mode;
    },
    getBody(){
      if (!this.result.success) {
        return this.result.body ? this.result.body : ""
      } else {
        return "OK";
      }
    }
  },

  watch: {
    'request.responseResult'() {
      this.reload();
    }
  },
  computed: {
    assertion() {
      return this.request.passAssertions + " / " + this.request.totalAssertions;
    },
    hasSub() {
      return this.request.subRequestResults.length > 0;
    },
    startTime(){
      if(this.result.startTime){
        return fomartDate(this.result.startTime,'{y}-{m}-{d} {h}:{i}:{s}');
      }
      return "--";
    },
    endTime(){
      if(this.result.endTime){
        return fomartDate(this.result.endTime,'{y}-{m}-{d} {h}:{i}:{s}');
      }
      return "--";
    }
  }
}
</script>

<style scoped>

.request-result .info {
  background-color: #F9F9F9;
  margin-left: 20px;
  cursor: pointer;
}

.request-result {
  width: 100%;
  min-height: 40px;
  padding: 2px 0;
}

.request-result .time {
  color: #7f7f7f;
  font-size: 12px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}

.text-container .icon {
  padding: 5px;
}

.text-container .collapse {
  cursor: pointer;
}

.text-container .collapse:hover {
  opacity: 0.8;
}

.text-container .icon.is-active {
  transform: rotate(90deg);
}

.text-container .pane {
  background-color: #F5F5F5;
  padding: 1px 0;
  height: 250px;
  overflow-y: auto;
}

.text-container .pane.cookie {
  padding: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0px;
}

.ms-div {
  margin-top: 20px;
}

pre {
  margin: 0;
}

.time {
  text-align: end;
  margin-right: 20px;
}

</style>
