<template>
  <command-item-container>
    <el-form-item
      class="cmd-component"
      :label="$t('ui.operation')"
      :inline="true"
      :inline-message="true"
    >
      <el-select v-model="currentCommand.vo[prop]" size="small" style="width:150px;">
        <el-option :key="o.value" :label="o.label" :value="o.value" v-for="o in optContents"></el-option>
      </el-select>
      <el-tooltip class="item" effect="dark" placement="right"
                  v-if="currentCommand.vo[prop] && tips">
        <div slot="content">{{ tips }}</div>
        <i :style="{'font-size': 10 + 'px', 'margin-left': 32 + 'px'}" class="el-icon-info"></i>
      </el-tooltip>
    </el-form-item>
  </command-item-container>
</template>

<script>
import CommandItemContainer from "../CommandItemContainer";

export default {
  name: "OptContentFormItem",
  components: {CommandItemContainer},
  props: {
    currentCommand: Object,
    optContents: Array,
    tips: String,
    prop: {
      type: String,
      default() {
        return 'optContent';
      }
    }
  }
}
</script>

<style scoped>

</style>
