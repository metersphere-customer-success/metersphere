<template>

  <div>

    <el-dialog v-loading="loading"
               :close-on-click-modal="false"
               :destroy-on-close="true"
               append-to-body
               :title="operationType === 'edit' ? $t('data.editRule') : $t('test_track.data.dataRule')"
               :visible.sync="dialogFormVisible"
               @close="close"
               top="8vh"
               width="60%">

      <el-form :model="form" :rules="rules" ref="planFrom">
        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item
                :label="$t('data.name')"
                :label-width="formLabelWidth"
                prop="name">
              <el-input v-model="form.name" maxlength="128" show-word-limit class="input-inner" style="border: 1px;"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item
              :label="$t('data.ruleContext')"
              :label-width="formLabelWidth"
              prop="ruleContext">
              <el-input v-model="form.ruleContext" maxlength="128" show-word-limit/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('data.type')" :label-width="formLabelWidth" prop="type">
              <el-select v-model="form.type" clearable
                         style="width: 100%;" :size="itemSize">
                <el-option v-for="item in typeOption" :key="item.name" :label="$t(item.id)" :value="item.name"/>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">

            <el-form-item :label="$t('data.caseQuality')" :label-width="formLabelWidth" prop="caseQuality">
              <el-select v-model="form.caseQuality" clearable
                         style="width: 100%;" :size="itemSize">
                <el-option v-for="item in caseQualityOption" :key="item.name" :label="$t(item.id)" :value="item.name"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!--start:xuxm增加自定义‘计划开始’，‘计划结束’时间字段-->
        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item
                :label="$t('data.iter')"
                :label-width="formLabelWidth"
                prop="iter">
              <el-input v-model.number="form.iter"
                        :size="itemSize" maxlength="128" show-word-limit/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item
              :label="$t('data.genNum')"
              :label-width="formLabelWidth"
              prop="genNum">
              <el-input v-model.number="form.genNum"
                        :size="itemSize" maxlength="128" show-word-limit/>
            </el-form-item>
          </el-col>
        </el-row>
        <!--end:xuxm增加自定义‘计划开始’，‘计划结束’时间字段-->

        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('data.genType')" :label-width="formLabelWidth" prop="genType">
              <el-select v-model="form.genType" clearable
                         style="width: 100%;" :size="itemSize">
                <el-option v-for="item in genTypeOption" :key="item.name" :label="$t(item.id)" :value="item.name"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="12">
            <el-form-item
                :label="$t('data.genConcurrent')"
                label-width="140px"
                prop="genConcurrent">
              <el-switch v-model="form.genConcurrent"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                :label="$t('data.encryptMethod')"
                label-width="140px"
                prop="encryptMethod">
              <el-switch v-model="form.encryptMethod"/>
            </el-form-item>
          </el-col>
        </el-row>


      </el-form>

      <template v-slot:footer>

        <div class="dialog-footer">
          <el-button
              v-prevent-re-click
              @click="dialogFormVisible = false">
            {{ $t('test_track.cancel') }}
          </el-button>
          <el-button
              type="primary"
              v-prevent-re-click
              @click="savePlan">
            {{ $t('test_track.confirm') }}
          </el-button>
          <el-button type="primary" v-prevent-re-click
                     @click="testPlanInfo">
            {{ $t('test_track.planning_execution') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>


</template>

<script>
import MsSelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import TestPlanStatusButton from "../common/TestPlanStatusButton";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import MsInputTag from "metersphere-frontend/src/components/MsInputTag";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import { testPlanAdd, testPlanEdit} from "@/api/remote/plan/test-plan";
import {buildTree, getProjectMemberOption} from "@/business/utils/sdk-utils";
import {getTestPlanNodes} from "@/api/test-plan-node";

export default {
  name: "TestPlanEdit",
  components: {MsInstructionsIcon, TestPlanStatusButton, MsInputTag, MsSelectTree},
  data() {
    return {
      genTypeOption:[
        {name:'CSV',id: 'CSV'},
        {id:'TEXT',name: 'TEXT'}
      ],
      caseQualityOption:[
        {name:'正例',id: '正例'},
        {id:'反例',name: '反例'}
      ],
      typeOption: [
        {name:'边界值',id: '边界值'},
        {id:'等价类',name: '等价类'}
      ],
      isStepTableAlive: true,
      dialogFormVisible: false,
      itemSize: "small",
      loading: false,
      form: {
        ruleContext:'',
        name:'',
        testPoint:'',
        type:'',
        caseQuality:'',
        iter:1,
        genNum:1,
        genType:'',
        genConcurrent:true,
        encryptMethod:true,
        nodeId: '',
        nodePath: '',
      },
      rules: {
        name: [
          {required: true, message: this.$t('data.ruleNameWarn'), trigger: 'blur'},
          {max: 128, message: this.$t('test_track.length_less_than') + '128', trigger: 'blur'}
        ],
        ruleContext: [
          {required: true, message: this.$t('data.ruleContextWarn'), trigger: 'blur'},
          {max: 128, message: this.$t('test_track.length_less_than') + '128', trigger: 'blur'}
        ],
        nodeId: [{required: true, message: this.$t("api_test.environment.module_warning"), trigger: "change"}],
      },
      formLabelWidth: "100px",
      operationType: '',
      principalOptions: [],
      stageOption: [],
      defaultNode: null,
      treeNodes: null,
      moduleObj: {
        id: "id",
        label: "name",
      }
    };
  },
  created() {
    this.getNodeTrees();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    getNodeTrees() {
      getTestPlanNodes(this.projectId, {})
          .then(r => {
            let treeNodes = r.data;
            treeNodes.forEach(node => {
              buildTree(node, {path: ''});
            });
            this.treeNodes = treeNodes;
            if (this.operationType === 'add') {
              this.setDefaultModule();
            }
          });
    },
    setDefaultModule() {
      if (this.defaultNode == null) {
        this.setUnplannedModule(this.treeNodes);
      } else {
        this.form.nodeId = this.defaultNode.data.id;
        let node = this.findTreeNode(this.treeNodes);
        if (node) {
          this.form.nodePath = node.path;
        } else {
          // 如果模块已删除，设置为未规划模块
          this.setUnplannedModule(this.treeNodes);
        }
      }
    },
    findTreeNode(nodeArray) {
      for (let i = 0; i < nodeArray.length; i++) {
        let node = nodeArray[i];
        if (node.id === this.form.nodeId) {
          return node;
        } else {
          if (node.children && node.children.length > 0) {
            let findNode = this.findTreeNode(node.children);
            if (findNode != null) {
              return findNode;
            }
          }
        }
      }
    },
    setUnplannedModule(treeNodes) {
      // 创建不带模块ID，设置成为规划模块
      this.form.nodeId = treeNodes[0].id;
      this.form.nodePath = treeNodes[0].path;
    },
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => (this.isStepTableAlive = true));
    },
    openTestPlanEditDialog(testPlan, selectDefaultNode) {
      this.resetForm();
      this.defaultNode = selectDefaultNode;
      this.getNodeTrees();
      this.setPrincipalOptions();
      this.operationType = 'add';
      if (testPlan) {
        //修改
        this.operationType = 'edit';
        let tmp = {};
        Object.assign(tmp, testPlan);
        Object.assign(this.form, tmp);
      } else {
        this.form.tags = [];
      }
      listenGoBack(this.close);
      this.setEmptyStage();
      this.dialogFormVisible = true;
      this.reload();
    },
    setEmptyStage() {
      // 如果测试阶段选项中没有当前值，则置空
      let hasOptions = false;
      this.stageOption.forEach(item => {
        if (item.value === this.form.stage) {
          hasOptions = true;
          return;
        }
      });
      if (!hasOptions) {
        this.form.stage = '';
      }
    },
    testPlanInfo() {
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (!this.validate(param)) {
            return;
          }
          param.workspaceId = getCurrentWorkspaceId();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;
          this.loading = true;
          let method = testPlanAdd;
          if (this.operationType === 'edit') {
            method = testPlanEdit;
          }
          method(param)
              .then(response => {
                this.loading = false;
                if (this.operationType === 'add') {
                  this.$success(this.$t('commons.save_success'));
                }
                this.dialogFormVisible = false;
                this.$router.push('/track/plan/view/' + response.data.id);
              }).catch(() => {
                this.loading = false;
              });
        } else {
          return false;
        }
      });
    },
    savePlan() {
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.name = param.name.trim();
          if (!this.validate(param)) {
            return;
          }
          param.workspaceId = getCurrentWorkspaceId();
          if (this.form.tags instanceof Array) {
            this.form.tags = JSON.stringify(this.form.tags);
          }
          param.tags = this.form.tags;

          this.loading = true;
          let method = testPlanAdd;
          if (this.operationType === 'edit') {
            method = testPlanEdit;
          }
          method(param)
              .then(() => {
                this.loading = false;
                this.$success(this.$t('commons.save_success'));
                this.dialogFormVisible = false;
                this.$emit("refresh");
              }).catch(() => {
                this.loading = false;
              });
        } else {
          return false;
        }
      });
    },
    validate(param) {
      if (param.name === '') {
        this.$warning(this.$t('test_track.plan.input_plan_name'));
        return false;
      }
      if (param.plannedStartTime > param.plannedEndTime) {
        this.$warning(this.$t('commons.date.data_time_error'));
        return false;
      }
      return true;
    },
    setPrincipalOptions() {
      getProjectMemberOption()
          .then(response => {
            this.principalOptions = response.data;
          });
    },
    statusChange(status) {
      this.form.status = status;
      this.$forceUpdate();
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    },
    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs['planFrom']) {
        this.$refs['planFrom'].validate(() => {
          this.$refs['planFrom'].resetFields();
          this.form.name = '';
          this.form.projectIds = [];
          this.form.nodeId = '';
          this.form.nodePath = '';
          return true;
        });
      }
    },
    setModule(id, data) {
      if (data) {
        this.form.nodeId = id;
        this.form.nodePath = data.path;
      }
    }
  }
}
</script>

<style scoped>
.instructions-icon {
  margin-left: 10px;
}
.input-inner{
  border: 1px;
}
input{
  border: 1px;
}

.input-inner :deep(.el-input__inner) {
  border: 1px;
}
</style>
