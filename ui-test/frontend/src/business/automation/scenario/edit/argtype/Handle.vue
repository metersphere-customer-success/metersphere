<template>

  <el-input
    type="text"
    :disabled="isReadOnly"
    size="small"
    v-model="value[propName].text"
    @change="change"/>

</template>

<script>
import {useCommandStore} from "@/store";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

const commandStore = new useCommandStore();
export default {
  name: "Handle",
  props: ["paramDefinition", "commandDefinition", "value", "propName"],
  components: {},
  created() {
    this.projectId = getCurrentProjectID();
    this.pro = this.propName.split("V")[0];
    //初始化 targetVO 或者 valueVO 并且初始化 text 字段，String 组件只用名字为 ”text“ 的属性
    if (!this.value[this.propName]) {
      this.$set(this.value, this.propName, {});
      this.$set(this.value[this.propName], "text", null);
    }

    /**
     * 此处的逻辑
     * 1.真正用到的属性只有  target 和 value
     * 2.用于展示在网页端的 targetVO 和 valueVO
     * 3.当该步骤是导入进来的时候分类讨论
     *  1）
     *   #1.该指令不是 ”open“
     *   #2.如果该指令存在值 target 或 value 则把 targetVO 或者 valueVO 中用于展示的字段设置为该值
     *  2）
     *   #1.该指令是 ”open“
     *   #2.根据 side 的格式，如果 target 或 value 已经是一个合规的 url
     *   #3.直接设置 targetVO 或者 valueVO 中用于展示的字段设置为该 target 或者 value
     *   #4.如果不是，则拼上该 scenario 的 baseURL
     *
     */
    if (this.value[this.pro]) {
      if (this.value.command == 'open') {
        if (commandStore.currentScenario.scenarioDefinition.baseURL) {
          if (!/^(file|http|https):\/\//.test(this.value[this.pro])) {
            this.$set(this.value[this.propName], "text", commandStore.currentScenario.scenarioDefinition.baseURL + this.value[this.pro]);
            this.$set(this.value, this.pro, commandStore.currentScenario.scenarioDefinition.baseURL + this.value[this.pro]);
          }
        }
      }
      this.$set(this.value[this.propName], "text", this.value[this.pro]);
    }
  },
  data() {
    return {
      currentItem: {},
      currentCommand: {},
      projectId: null,
      isReadOnly: false,
      pro: this.propName.split("V")[0],
    }
  },
  methods: {
    change() {
      if (this.value[this.propName] && this.value[this.propName].text) {
        this.value[this.pro] = this.value[this.propName].text;
      }
    },
    getLabel() {
      if (this.commandDefinition[this.value.command][this.pro + 'CNName']) {
        return this.commandDefinition[this.value.command][this.pro + 'CNName'] + "：";
      }
      return this.$t("ui." + this.commandDefinition[this.value.command][this.pro]) + "：";
    }
  }
}
</script>

<style scoped>
.label {
  text-align: right;
  padding-right: 10px;
}
</style>
