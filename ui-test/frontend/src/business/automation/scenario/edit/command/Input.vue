<template>
  <div>
    <el-form
        :disabled="isReadOnly"
        :label-position="'right'"
        label-width="120px"
        :model="currentCommand.vo"
        size="medium"
        v-loading="loading"
        :rules="rules"
        ref="cmdForm"
        :inline="true"
        :inline-message="true"
    >
      <opt-content-form-item
          v-if="currentCommand.command != 'cmdFileUpload'"
          :current-command="currentCommand"
          :opt-contents="optContents"
          :tips="$t('ui.input_c_tip')"
      />

      <locator-form
          :label="$t('ui.operation_object')"
          :current-command="currentCommand"
          :label-width="labelWidth"
          ref="locatorForm"
      />

      <command-item-container v-if="currentCommand.command !== 'cmdFileUpload'">
        <el-form-item
            class="cmd-component"
            prop="inputContent"
            :label-width="labelWidth"
            :label="$t('ui.input_content')"
            label-position="'right'"
        >
          <el-input
              style="width: 618px"
              type="textarea"
              class="input-text"
              v-model="currentCommand.vo.inputContent"
              :placeholder="$t('ui.insert_content')"
              size="small"
          ></el-input>
        </el-form-item>
      </command-item-container>

      <command-item-container
          v-if="
          currentCommand.vo.optContent &&
          currentCommand.vo.optContent !== 'editableBox' &&
          currentCommand.command !== 'cmdFileUpload'
        "
      >
        <el-form-item
            class="cmd-component check-wrap"
            :label-width="labelWidth"
        >
          <div class="input-advance" style="display: flex">
            <div class="input-tip" style="margin-right: 20px">
              <el-checkbox
                  v-model="currentCommand.vo.addInput"
                  :disabled="currentCommand.vo.optContent == 'editableBox'"
              />
              <span class="input-label">{{ $t('ui.append_content') }}</span>
              <el-tooltip class="item" effect="dark" placement="right">
                <div slot="content">
                  {{ $t('ui.append_tip') }}
                </div>
                <i
                    :style="{ 'font-size': 10 + 'px', 'margin-left': 3 + 'px' }"
                    class="el-icon-info"
                ></i>
              </el-tooltip>
            </div>

            <div class="function-row">
              <el-popover placement="bottom-start" width="350" trigger="hover">
                <div class="function-wrap">
                  <div
                      class="key-item"
                      v-for="item in functionKeys"
                      :key="item.id"
                      :class="{ 'key-item-hover': item.id === hoverId }"
                      @mouseover="hoverId = item.id"
                      @mouseout="hoverId = -1"
                      @click="clickKeyItem(item.content, $event)"
                  >
                    <div class="key-name">{{ item.name }}</div>
                    <div class="key-desc">{{ item.content }}</div>
                  </div>
                </div>
                <div
                    style="
                    display: inline-flex;
                    cursor: pointer;
                    justify-content: center;
                    align-items: center;
                  "
                    slot="reference"
                >
                  <div
                      style="
                      margin-right: 5px;
                      width: 20px;
                      height: 100%;
                      display: flex;
                      align-items: center;
                    "
                  >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 576 512"
                    >
                      <!-- Font Awesome Pro 5.15.4 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license (Commercial License) -->
                      <path
                          d="M528 448H48c-26.51 0-48-21.49-48-48V112c0-26.51 21.49-48 48-48h480c26.51 0 48 21.49 48 48v288c0 26.51-21.49 48-48 48zM128 180v-40c0-6.627-5.373-12-12-12H76c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm-336 96v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm-336 96v-40c0-6.627-5.373-12-12-12H76c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12zm288 0v-40c0-6.627-5.373-12-12-12H172c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h232c6.627 0 12-5.373 12-12zm96 0v-40c0-6.627-5.373-12-12-12h-40c-6.627 0-12 5.373-12 12v40c0 6.627 5.373 12 12 12h40c6.627 0 12-5.373 12-12z"
                      />
                    </svg>
                  </div>
                  <div class="function-key input-label">功能键</div>
                </div>
              </el-popover>
            </div>
            <div class="function-tip">
              <el-tooltip class="item" effect="dark" placement="right">
                <div slot="content">
                  使用功能键必须勾选“追加输入”，不勾选会当作字符串输入
                </div>
                <i
                    :style="{
                    'font-size': 10 + 'px',
                    'margin-left': 3 + 'px',
                  }"
                    class="el-icon-info"
                ></i>
              </el-tooltip>
            </div>
          </div>
        </el-form-item>
      </command-item-container>

      <command-item-container v-if="currentCommand.command == 'cmdFileUpload'">
        <el-form-item
            class="cmd-component"
            prop="files"
            :label-width="labelWidth"
            :label="$t('ui.add_file')"
            label-position="'right'"
        >
          <api-body-file-upload :parameter="currentCommand.vo" :id="currentCommand.id" :disabled="isReadOnly" @add="openFileRelevanceDialog"   @setSelectFiles="setSelectFiles"/>
<!--          <ui-tag-add
              :tags="currentCommand.vo.files"
              :innerTags="currentCommand.vo.files"
              @add="openFileRelevanceDialog"
          />-->

        </el-form-item>
      </command-item-container>
    </el-form>
    <follow-mouse-tip ref="followMouseTip"/>
    <!--文件选择    -->
    <file-meta-data-relevance ref="fileChooserDialog" @setSelectFiles="setSelectFiles"/>
  </div>
</template>

<script>
import argtype from "../argtype";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import paramDefinition from "@/business/definition/command/param-definition";
import OptContentFormItem from "@/business/automation/scenario/edit/command/component/OptContentFormItem";
import {commandFromAndLocatorFromValidate} from "@/business/automation/ui-automation";
import CommandItemContainer from "./CommandItemContainer";
import LocatorForm from "./component/LocatorForm";
import getAllFunctionKeys from "@/business/automation/scenario/edit/command/input-function-keys";
import FollowMouseTip from "./FollowMouseTip";
import {useCommandStore} from "@/store";
import UiTagAdd from "@/components/tag-add/UiTagAdd";
import FileMetaDataRelevance from "@/components/file/dialog/FileMetaDataRelevance";
import {fileExists} from "@/api/file";
import ApiBodyFileUpload from "@/business/definition/body/ApiBodyFileUpload.vue";

const commandStore = new useCommandStore();
export default {
  name: "Input",
  props: {
    selectCommand: Object,
    value: Object,
  },
  components: {
    FileMetaDataRelevance,
    UiTagAdd,
    LocatorForm,
    CommandItemContainer,
    OptContentFormItem,
    ...argtype,
    FollowMouseTip,
    ApiBodyFileUpload
  },
  watch: {
    currentCommand: {
      handler(val) {
        if (val.type == "MsUiCommand") {
          this.initCmd();
        }
      },
      deep: true,
    },
    selectCommand() {
      this.initCmd();
    },
  },
  created() {
    this.currentCommand = this.value;
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
    if (this.selectCommand) {
      this.initCmd();
    }
    if (this.currentCommand.atomicCommands) {
      this.atomicCommands = this.currentCommand.atomicCommands;
    }
    if (
        !this.currentCommand.vo.optContent &&
        this.optContents &&
        this.optContents.length > 0
    ) {
      this.currentCommand.vo.optContent = this.optContents[0].value;
    }
  },
  computed: {
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    },
    functionKeys() {
      return getAllFunctionKeys();
    },
  },
  data() {
    var checkFiles = async (rule, value, callback, vue) => {
      if (!(rule.vue &&
          rule.vue.currentCommand &&
          rule.vue.currentCommand.vo &&
          rule.vue.currentCommand.vo.files &&
          rule.vue.currentCommand.vo.files.length >0)) {
        return callback(new Error('文件不能为空'));
      }
      let fileNotExitsError = await this.validateFiles();
      if (fileNotExitsError) {
        return callback(fileNotExitsError);
      }
      return callback();
    };
    return {
      optContents: [
        {label: this.$t("ui.input"), value: "inputBox"},
        {label: this.$t("ui.editable_p"), value: "editableBox"}
      ],
      isReadOnly: false,
      labelWidth: "120px",
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      loading: false,
      rules: {
        inputContent: [
          {required: true, message: this.$t('ui.param_null'), trigger: "blur"},
        ],
        optContent: [
          {required: true, message: this.$t('ui.param_null'), trigger: "blur"},
        ],
        files: [
          {validator: checkFiles, trigger: "blur", vue: this},
        ],
      },
      atomicCommands: [],
      hoverId: -1,
    };
  },
  methods: {
    clickKeyItem(keyItemContent, event) {
      this.$copyText(keyItemContent).then(
          (e) => {
            this.$refs.followMouseTip.tip(
                this.$t("commons.copy_success"),
                event.clientX,
                event.clientY
            );
          },
          (e) => {
            this.$error("复制失败！");
          }
      );
    },
    async validate() {
      if (!this.currentCommand.enable) {
        return true;
      }

      return commandFromAndLocatorFromValidate(this);
    },
    validateFiles() {
      return new Promise((resolve) => {
        let ids = _.reduce(this.currentCommand.vo.files, function (r, v) {
          if (r.indexOf(v.id) === -1 ) {
            r.push(v.id);
          }
          return r;
        }, []);
        fileExists(ids).then(res => {
          let existIds = res.data;
          ids = _.filter(ids, (id) => existIds.indexOf(id) === -1);
          if (ids && ids.length) {
            let deletedFiles = _.reduce(this.currentCommand.vo.files, (r, v) => {
              if (ids.indexOf(v.id) !== -1 && !_.find(r, {id: v.id})) {
                r.push(v);
              }
              return r;
            }, []);
            let msg = _.reduce(deletedFiles, (r, v) => {
              r.push(v.name);
              return r;
            }, []);
            resolve(new Error(this.$t("ui.file") + msg + this.$t("ui.been_deleted")));
          } else {
            resolve();
          }
        });
      });
    },
    initCmd() {
      this.loading = true;
      this.targetType = false;
      this.valueType = false;
      this.currentCommand = this.selectCommand
          ? this.selectCommand
          : commandStore.selectCommand;
      let cmd = this.currentCommand.command;
      this.targetType = this.commandDefinition[cmd].target;
      this.valueType = this.commandDefinition[cmd].value;
      //根据参数定义的类型选择不同组件
      if (this.targetType) {
        this.targetTypeComponent = this.paramDefinition[this.targetType];
      }
      if (this.valueType) {
        this.valueTypeComponent = this.paramDefinition[this.valueType];
      }
      if (this.$refs.cmdSwitcher) {
        this.$refs.cmdSwitcher.setCommand(cmd);
      }
      this.loading = false;
    },
    openFileRelevanceDialog() {
      this.$refs.fileChooserDialog.open();
    },
    setSelectFiles(selectRows) {
      let files = [];
      selectRows.forEach(f => {
        f.tags = null;
        files.push(f);
      })
      this.currentCommand.vo.files = (this.currentCommand.vo.files || []).concat(files);
      this.$forceUpdate();
    }
  },
};
</script>

<style scoped>
.command {
  margin: 5px 5px;
}

.cmd-component {
  margin-bottom: 8px;
}

.el-form-item .el-select {
  width: 180px;
}

.el-form-item .el-input {
  width: 180px;
}

.opt-item {
  position: relative;
  left: 32px;
  top: -6px;
}

:deep(.input-text .el-textarea__inner) {
  width: 618px !important;
  height: 160px !important;
}

.input-label {
  vertical-align: middle;
  font-size: 14px;
  color: #606266;
  line-height: 40px;
  padding: 0 0px 0 5px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}

.input-content-label {
  width: 95px;
  /* padding: 0 12px 0 0px; */
  margin-right: 12px;
  /* vertical-align: middle; */
  font-size: 14px;
  color: #606266;
  line-height: 40px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}

:deep(.input-text-wrap .el-form-item__content) {
  display: flex;
}

.input-text {
  margin-top: 8px;
}

.check-wrap {
  margin-left: 120px !important;
}

:deep(.input-text-wrap .el-form-item__error) {
  /* top: 88% !important; */
}

:deep(.input-text-wrap .el-form-item__error--inline) {
  top: 170px !important;
  left: -553px !important;
  width: 120px !important;
  height: 20px !important;
  margin-bottom: 10px;
}

.function-wrap {
  height: 350px;
  overflow-y: scroll;
}

.key-item {
  display: flex;
  padding: 3px;
  background-color: #fff;
  justify-content: space-between;
  cursor: pointer;
}

.key-item-hover {
  background-color: #f9f6f9;
}
</style>
