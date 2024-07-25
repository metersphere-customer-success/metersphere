<template>
  <div>
    <el-form :disabled="isReadOnly" :label-position="'right'" label-width="120px" :model="currentCommand" size="small"
             :inline="true"
             :inline-message="true"
             v-loading="loading" :rules="rules" ref="cmdForm">

      <el-form
        :model="currentCommand.vo"
        :rules="rules"
        :inline="true"
        :inline-message="true"
        ref="locatorForm">
        <el-form-item
          class="cmd-component"
          :prop="'dragType'"
          :label="$t('ui.drag_type')"
          :label-position="'right'"
          :label-width="labelWidth">
          <el-checkbox v-model="currentCommand.vo.innerDrag" @change="enableCoord">
            {{ $t("ui.inner_drag") }}
          </el-checkbox>
        </el-form-item>

        <el-form-item
          class="cmd-component ml85"
          :prop="'setCoord'"
          :label-position="'right'"
          :label-width="labelWidth">
          <el-checkbox v-model="currentCommand.vo.setCoord" @change="initCoords"
                       :disabled="currentCommand.vo.innerDrag">
            {{ $t("ui.set_coord") }}
          </el-checkbox>
        </el-form-item>
      </el-form>

      <locator-form
        prop="startLocator"
        :label="$t('ui.opt_ele')"
        :current-command="currentCommand.vo"
        :label-width="labelWidth"
        :show-coord="currentCommand.vo.setCoord"
        :tips="$t('ui.drag_start')"
        ref="startLocatorForm"
      />

      <el-form-item
        class="cmd-component"
        :label="$t('ui.coord')"
        :label-position="'right'"
        :label-width="labelWidth"
        v-if="currentCommand.vo.setCoord && currentCommand.vo.startLocator && currentCommand.vo.startLocator.coords">

        <mouse-coord
          :key="c.index"
          v-for="c in currentCommand.vo.startLocator.coords"
          :coord="c"
          :coords="currentCommand.vo.startLocator.coords"
          @forceUpdate="forceUpdate"
          class="mt10"/>

      </el-form-item>

      <locator-form
        prop="endLocator"
        :label="$t('ui.dst_ele')"
        v-if="currentCommand.vo && !currentCommand.vo.innerDrag"
        :current-command="currentCommand.vo"
        :label-width="labelWidth"
        :show-coord="currentCommand.vo.setCoord"
        :tips="$t('ui.drag_end_point')"
        ref="endLocatorForm"
      />

      <el-form-item
        class="cmd-component"
        :label="$t('ui.coord')"
        :label-position="'right'"
        :label-width="labelWidth"
        v-if="currentCommand.vo.setCoord && currentCommand.vo.endLocator && currentCommand.vo.endLocator.coords && !currentCommand.vo.innerDrag">

        <mouse-coord
          :key="c.index"
          v-for="c in currentCommand.vo.endLocator.coords"
          :coord="c"
          :coords="currentCommand.vo.endLocator.coords"
          @forceUpdate="forceUpdate"
          class="mt10"/>

      </el-form-item>

    </el-form>
  </div>
</template>

<script>
import argtype from "../argtype"
import UiCmdSwitcher from "./UiCmdSwitcher";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition"
import paramDefinition from "@/business/definition/command/param-definition";
import CommandItemContainer from "./CommandItemContainer";
import LocatorForm from "./component/LocatorForm";
import {commandFromValidate} from "@/business/automation/ui-automation";
import MouseCoord from "@/business/automation/scenario/edit/command/component/MouseCoord";
import {useCommandStore} from "@/store";
import {uuid} from "metersphere-frontend/src/model/ApiTestModel";

const commandStore = new useCommandStore();
export default {
  name: "MouseDrag",
  props: {
    selectCommand: Object,
    showSwitch: {
      type: Boolean,
      default: true
    },
    value: Object,
  },
  components: {
    LocatorForm,
    CommandItemContainer,
    ...argtype,
    UiCmdSwitcher,
    MouseCoord,
  },
  watch: {
    currentCommand: {
      handler(val) {
        if (val.type == 'MsUiCommand') {
          this.initCmd();
        }
      },
      deep: true
    },
    selectCommand() {
      this.initCmd();
    },
  },
  created() {
    this.currentCommand = this.value;
    this.currentCommand.vo.clickType = 'dragAndDropToObject';
    this.isReadOnly = this.currentCommand ? this.currentCommand.readonly === true : false;
    if (this.selectCommand) {
      this.initCmd();
    }
    if (this.currentCommand.atomicCommands) {
      this.atomicCommands = this.currentCommand.atomicCommands;
    }
    this.initCoords();
  },
  computed: {
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    }
  },
  data() {
    return {
      isReadOnly: false,
      targetTypeComponent: "",
      valueTypeComponent: "",
      targetType: null,
      valueType: null,
      formLabelAlign: {},
      loading: false,
      rules: {},
      atomicCommands: [],
      labelWidth: '120px'
    }
  },
  methods: {
    validate() {
      return this.$refs['startLocatorForm'].validate()
        && ((this.$refs['endLocatorForm'] && this.$refs['endLocatorForm'].validate()) || true)
        && commandFromValidate(this, 'cmdForm');
    },
    initCmd() {
      this.loading = true;
      this.targetType = false;
      this.valueType = false;
      this.currentCommand = this.selectCommand ? this.selectCommand : commandStore.selectCommand;
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
    initCoords() {
      //处理旧数据 如果没有坐标初始化一个 防止前端报错
      if (this.currentCommand.vo.setCoord) {
        if (!this.currentCommand.vo.startLocator.coords || !this.currentCommand.vo.startLocator.coords.length) {
          this.currentCommand.vo.startLocator.coords = [{index: uuid(), x: 0, y: 0, type: "Coord"}];
        }
        if (!this.currentCommand.vo.endLocator.coords || !this.currentCommand.vo.endLocator.coords.length) {
          this.currentCommand.vo.endLocator.coords = [{index: uuid(), x: 0, y: 0, type: "Coord"}];
        }
      }
    },
    forceUpdate() {
      this.$forceUpdate();
    },
    enableCoord() {
      if (this.currentCommand.vo.innerDrag) {
        this.currentCommand.vo.setCoord = true;
      }
    }
  }
}
</script>

<style scoped>
.ml85 {
  margin-left: 85px;
}

.mt10:nth-child(n + 2) {
  margin-top: 10px;
}
</style>
