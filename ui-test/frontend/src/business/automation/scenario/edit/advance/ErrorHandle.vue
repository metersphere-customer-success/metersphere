<template>
  <div>
    <el-form class="error-handle" :disabled="isReadonly">
      <el-form-item :label="$t('ui.treatment_method')" v-if="value.commandConfig">
        <el-select v-model="value.commandConfig.ignoreFail" size="mini">
          <el-option v-for="item in types" :key="item.label" :label="$t('ui.' +item.label)" :value="item.value"/>
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {useCommandStore} from "@/store";

const commandStore = new useCommandStore();

export default {
  name: "ErrorHandle",
  props: ["value", "isReadonly"],
  watch: {
    "commandStore.selectCommand": {
      handler(val) {
        if (val && val.type == "MsUiCommand") {
          if (!this.value.commandConfig) {
            this.$set(this.value, "commandConfig", {ignoreFail: false});
          }
        }
      }
    }
  },
  data() {
    return {
      types: [
        {
          label: "not_ignore_fail",
          value: false
        },
        {
          label: "ignore_fail",
          value: true
        },
      ]
    }
  }
}
</script>

<style scoped>
.error-handle {
  margin-top: 8px;
}
</style>
