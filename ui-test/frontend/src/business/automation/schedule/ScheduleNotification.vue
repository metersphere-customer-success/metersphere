<template>
  <div>
    <el-row>
      <el-col :span="10">
        <el-button icon="el-icon-circle-plus-outline" plain size="mini"
                   @click="handleAddTaskModel">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table :table-data="scheduleTask"
                            :event-options="scheduleEventOptions"
                            :receive-type-options="receiveTypeOptions"
                            @handleReceivers="handleReceivers"
                            @handleTemplate="handleTemplate"
                            @refresh="initForm"/>
      </el-col>
    </el-row>
    <mx-notice-template v-xpack ref="noticeTemplate" :variables="variables"/>
  </div>
</template>

<script>
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import NotificationTable from "metersphere-frontend/src/components/notification/NotificationTable";
import NoticeTemplate from "metersphere-frontend/src/components/MxNoticeTemplate";
import {getNoticeTasks} from "@/business/automation/ui-automation";
import {hasLicense} from "metersphere-frontend/src/utils/permission";

export default {
  name: "ScheduleNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    NoticeTemplate,
    MxNoticeTemplate: () => import("metersphere-frontend/src/components/MxNoticeTemplate")
  },
  props: {
    testId: String,
    scheduleReceiverOptions: Array,
    isTesterPermission: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator}执行UI自动化成功: ${name}, 报告: ${reportUrl}",
      scheduleTask: [{
        taskType: "scheduleTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
        testId: this.testId,
      }],
      scheduleEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      receiveTypeOptions: [
        {value: 'IN_SITE', label: this.$t('organization.message.in_site')},
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')},
        {value: 'LARK', label: this.$t('organization.message.lark')},
        {value: 'WEBHOOK', label: this.$t('organization.message.webhook')},
      ],
      variables: [
        {
          label: this.$t('group.operator'),
          value: 'operator',
        },
        {
          label: this.$t('project.id'),
          value: 'projectId',
        },
        {
          label: this.$t('commons.tag'),
          value: 'tags',
        },
        {
          label: this.$t('user.id'),
          value: 'userId',
        },
        {
          label: this.$t('ui.automation.scenario.module_id'),
          value: 'moduleId',
        },
        {
          label: this.$t('module.path'),
          value: 'modulePath',
        },
        {
          label: this.$t('ui.automation.scenario.id'),
          value: 'id',
        },
        {
          label: this.$t('ui.automation.scenario.name'),
          value: 'name',
        },
        {
          label: this.$t('ui.automation.scenario.level'),
          value: 'level',
        },
        {
          label: this.$t('ui.automation.scenario.status'),
          value: 'status',
        },
        {
          label: this.$t('ui.automation.scenario.principal'),
          value: 'principal',
        },
        {
          label: this.$t('report.plan_share_url'),
          value: 'scenarioShareUrl',
        },
        {
          label: this.$t('ui.automation.scenario.step_total'),
          value: 'stepTotal',
        },
        {
          label: this.$t('commons.create_time'),
          value: 'createTime',
        },
        {
          label: this.$t('commons.update_time'),
          value: 'updateTime',
        },
        {
          label: this.$t('test_track.pass_rate'),
          value: 'passRate',
        },
        {
          label: this.$t('ui.automation.scenario.last_result'),
          value: 'lastResult',
        },
        {
          label: this.$t('commons.create_user'),
          value: 'createUser',
        },
        {
          label: this.$t('commons.delete_time'),
          value: 'deleteTime',
        },
        {
          label: this.$t('ui.automation.scenario.delete_user'),
          value: 'deleteUser',
        },
        {
          label: this.$t('commons.delete_user_id'),
          value: 'deleteUserId',
        },
        {
          label: this.$t('ui.automation.scenario.environment_json'),
          value: 'environmentJson',
        },

      ]
    };
  },
  mounted() {
    this.initForm();
  },
  activated() {
    this.initForm();
  },
  watch: {
    testId() {
      if (this.testId) {
        this.initForm();
      }
    }
  },
  methods: {
    initForm() {
      this.result = getNoticeTasks(this.testId).then(response => {
        this.scheduleTask = response.data;
        this.scheduleTask.forEach(task => {
          this.handleReceivers(task);
        });
      });
    },
    handleAddTaskModel() {
      let Task = {};
      Task.receiverOptions = this.scheduleReceiverOptions;
      Task.event = '';
      Task.userIds = [];
      Task.type = '';
      Task.webhook = '';
      Task.isSet = true;
      Task.identification = '';
      Task.taskType = 'SCHEDULE_TASK';
      Task.testId = this.testId;
      this.scheduleTask.unshift(Task);
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        let robotTemplate = "";
        switch (row.event) {
          case 'EXECUTE_SUCCESSFUL':
            robotTemplate = this.robotTitle;
            break;
          case 'EXECUTE_FAILED':
            robotTemplate = this.robotTitle.replace('成功', '失败');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, robotTemplate);
      }
    },
    handleReceivers(row) {
      row.receiverOptions = JSON.parse(JSON.stringify(this.scheduleReceiverOptions));
    }
  }
};
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>

