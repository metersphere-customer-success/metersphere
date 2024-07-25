<template>
  <el-form
    :disabled="isReadonly"
    :model="currentCommand"
    :rules="rules"
    :inline="true"
    :inline-message="true"
    ref="locatorForm">
    <el-form-item
      class="cmd-component"
      :prop="prop"
      :label="showLabel ? label : ''"
      :label-position="'right'"
      :label-width="labelWidth">
      <proxy-locator :command-definition="commandDefinition" :isReadonlyProp="isReadonly"
                     :param-definition="paramDefinition" v-model="currentCommand" :key="currentCommand.id"
                     :prop-name="prop">
        <el-tooltip v-if="tips" effect="dark" placement="top-start" :content="tips">
          <i class="el-icon-info pointer"/>
        </el-tooltip>
      </proxy-locator>
    </el-form-item>
  </el-form>
</template>

<script>
import {locatorValidator} from "@/business/definition/validator/param-validator";
import atomicCommandDefinition from "@/business/definition/command/atomic-command-definition";
import paramDefinition from "@/business/definition/command/param-definition";
import {commandFromValidate} from "@/business/automation/ui-automation";
import ProxyLocator from "@/business/automation/scenario/edit/argtype/ProxyLocator";

export default {
  name: "LocatorForm",
  components: {ProxyLocator},
  props: {
    isReadonly: {
          type: Boolean,
          default: false,
    },
    currentCommand: Object,
    labelWidth: String,
    showLabel:{
      type: Boolean,
      default() {
        return true;
      }
    },
    label: {
      type: String,
      default() {
        return this.$t('ui.operation_object');
      }
    },
    prop: {
      type: String,
      default() {
        return 'vo';
      }
    },
    tips: String,
    rules: {
      type: Object,
      default() {
        let res = {};
        res[this.prop] = [{
          validator: locatorValidator,
          trigger: ['blur'],
          required: true
        }];
        return res;
      }
    }
  },
  data() {
    return {}
  },
  computed: {
    commandDefinition() {
      return atomicCommandDefinition;
    },
    paramDefinition() {
      return paramDefinition;
    }
  },
  methods: {
    validate() {
      return commandFromValidate(this, 'locatorForm');
    },
  }
}
</script>

<style scoped>
.cmd-component :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
</style>
