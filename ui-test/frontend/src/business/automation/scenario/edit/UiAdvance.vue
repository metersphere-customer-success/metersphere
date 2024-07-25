<template>
  <div class="base-container">
    <div class="var-container">
      <el-row type="flex">
        <el-col class="col-height">
          <div>
            <el-input
              class="filter-text"
              size="small"
              v-model="filterText"
              :placeholder="$t('api_test.request.parameters_mock_filter_tips')"
            />
            <el-tree
              class="filter-tree"
              ref="tree"
              :data="mockFuncs"
              :props="treeProps"
              default-expand-all
              @node-click="selectVariable"
              :filter-node-method="filterNode"
            ></el-tree>
          </div>
        </el-col>
        <el-col
          v-for="(itemFunc, itemIndex) in mockVariableFuncs"
          :key="itemIndex"
        >
          <div
            v-for="(func, funcIndex) in funcs"
            :key="`${itemIndex}-${funcIndex}`"
          >
            <el-row>
              <el-col>
                <el-radio
                  size="mini"
                  v-model="itemFunc.name"
                  :label="func.name"
                  @change="methodChange(itemFunc, func)"
                  @click.native.prevent="radioClick(itemFunc, func)"
                />
              </el-col>
              <el-col v-if="itemFunc.name === func.name">
                <div
                  v-for="(p, pIndex) in itemFunc.params"
                  :key="`${itemIndex}-${funcIndex}-${pIndex}`"
                >
                  <el-input
                    :placeholder="p.name"
                    size="mini"
                    v-model="p.value"
                    @change="showPreview"
                  />
                </div>
              </el-col>
            </el-row>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="view-container">
      <div class="preview-row">{{ itemValuePreview }}</div>
      <div class="opt-row">
        <div class="opt-input">
          <el-input :placeholder="valueText" v-model="itemValue" size="small" />
        </div>
        <div class="opt-save">
          <el-button size="small" type="primary" plain @click="saveAdvanced()">
            {{ $t("commons.copy") }}
          </el-button>
        </div>
        <div class="opt-preview">
          <el-button
            size="small"
            type="success"
            plain
            @click="showPreview()"
            v-if="currentTab === 0"
          >
            {{ $t("api_test.request.parameters_preview") }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  Scenario,
} from "@/api/ApiTestModel";
import { JMETER_FUNC, MOCKJS_FUNC } from "@/api/constants";
import { STEP } from "@/api/Setting";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import OutsideClick from "@/api/outside-click";
import {useCommandStore} from "@/store";
import {calculate} from "metersphere-frontend/src/model/ApiTestModel";

const commandStore = new useCommandStore();
export default {
  name: "MsUiAdvance",
  props: {
    parameters: Array,
    environment: Object,
    scenario: Scenario,
    currentItem: Object,
    appendToBody: {
      type: Boolean,
      default() {
        return false;
      },
    },
    showMockVars: {
      type: Boolean,
      default() {
        return false;
      },
    },
    variables: Array,
    scenarioDefinition: Array,
  },
  components: {
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    MsComponentConfig: () =>
      import(
        "@/business/automation/scenario/component/ComponentConfig"
      ),
  },
  data() {
    return {
      itemValueVisible: false,
      filterText: "",
      environmentParams: [],
      scenarioParams: [],
      preRequests: [],
      preRequestParams: [],
      scenarioPreRequestParams: [],
      treeProps: { children: "children", label: "name" },
      currentTab: 0,
      itemValue: null,
      itemValuePreview: null,
      funcs: [
        { name: "md5" },
        { name: "base64" },
        { name: "unbase64" },
        {
          name: "substr",
          params: [{ name: "start" }, { name: "length" }],
        },
        {
          name: "concat",
          params: [{ name: "suffix" }],
        },
        { name: "lconcat", params: [{ name: "prefix" }] },
        { name: "sha1" },
        { name: "sha224" },
        { name: "sha256" },
        { name: "sha384" },
        { name: "sha512" },
        { name: "lower" },
        { name: "upper" },
        { name: "length" },
        { name: "number" },
      ],
      mockFuncs: MOCKJS_FUNC.map((f) => {
        return {
          name:
            f.name +
            " " +
            f.des +
            " " +
            this.$t("api_test.request.parameters_filter_example") +
            "：" +
            f.ex,
          value: f.name,
        };
      }),
      jmeterFuncs: JMETER_FUNC,
      mockVariableFuncs: [],
      jmeterVariableFuncs: [],
      dialogVisible: true,
      requestValues: [
        {
          type: this.$t("api_test.request.address"),
          name: "${address}",
        },
        {
          type:
            "Header " + this.$t("api_test.definition.document.request_param"),
          name: "${header.param}",
        },
        {
          type: this.$t("api_test.request.body") + this.$t("api_test.variable"),
          name: "${body.param}",
        },
        {
          type:
            this.$t("api_test.request.body") +
            this.$t("api_test.variable") +
            " (Raw)",
          name: "${bodyRaw}",
        },
        {
          type:
            "Query " + this.$t("api_test.definition.document.request_param"),
          name: "${query.param}",
        },
        {
          type: "Rest " + this.$t("api_test.definition.document.request_param"),
          name: "${rest.param}",
        },
      ],

      // 自定义变量相关
      defineVariable: "",
      searchType: "",
      selection: [],
      loading: false,
      types: new Map([
        ["CONSTANT", this.$t("api_test.automation.constant")],
        ["LIST", this.$t("test_track.case.list")],
        ["CSV", "CSV"],
        ["COUNTER", this.$t("api_test.automation.counter")],
        ["RANDOM", this.$t("api_test.automation.random")],
      ]),

      // 前置返回相关
      props: {
        label: "label",
        children: "hashTree",
      },
      expandedNode: [],
      stepFilter: new STEP(),
      operatingElements: [],
      selectedTreeNode: undefined,
      selectedNode: undefined,
      projectList: [],
      projectEnvMap: new Map(),
      ifFromVariableAdvance: false,
      asideHidden: false,
      scenarioRootTree: undefined,
      insideClick: false,
      response: {},
    };
  },
  computed: {
    valueText() {
      return this.valuePlaceholder || this.$t("api_test.value");
    },
  },
  directives: { OutsideClick },
  mounted() {
    this.prepareData();
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },
  },
  methods: {
    created() {
      this.operatingElements = this.stepFilter.get("ALL");
    },
    open() {
      if (this.scenarioDefinition != undefined) {
        // 标识为场景编辑入口进入
        this.ifFromVariableAdvance = true;
      }
      this.itemValueVisible = true;
      // 关闭页面重新进入需要再做过滤
      if (
        this.ifFromVariableAdvance &&
        this.$refs.preTree != undefined &&
        this.currentTab == 3
      ) {
        this.componentActive(this.$refs.preTree.root);
      }
    },
    prepareData(data) {
      if (data != undefined || data != null) {
        this.scenario = data;
      }
      if (this.scenario) {
        let variables = this.scenario.variables;
        this.scenarioParams = [
          {
            name: this.scenario.name,
            children: variables
              .filter((v) => v.name)
              .map((v) => {
                return { name: v.name, value: "${" + v.name + "}" };
              }),
          },
        ];
        if (this.environment) {
          let variables = this.environment.config.commonConfig.variables;
          this.environmentParams = [
            {
              name: this.environment.name,
              children: variables
                .filter((v) => v.name)
                .map((v) => {
                  return { name: v.name, value: "${" + v.name + "}" };
                }),
            },
          ];
        }
        let i = this.scenario.requests.indexOf(this.request);
        this.preRequests = this.scenario.requests.slice(0, i);
        this.preRequests.forEach((r) => {
          let js = r.extract.json.map((v) => {
            return { name: v.variable, value: v.value };
          });
          let xs = r.extract.xpath.map((v) => {
            return { name: v.variable, value: v.value };
          });
          let rx = r.extract.regex.map((v) => {
            return { name: v.variable, value: v.value };
          });
          let vs = [...js, ...xs, ...rx];
          if (vs.length > 0) {
            this.preRequestParams.push({ name: r.name, children: vs });
          }
        });
      }
    },

    // 获取该节点及所有子节点下的前置提取参数 key/value
    getExtractDataByNode(data, node) {
      if (!node.isLeaf) {
        if (node.childNodes.length > 0) {
          for (let i = 0; i < node.childNodes.length; i++) {
            if (node.childNodes[i].isLeaf) {
              //是叶子节点
              if (node.childNodes[i].data.type === "Extract") {
                //叶子节点的数据的类型是 提取
                let extractJsonParams = node.childNodes[i].data.json.map(
                  (v) => {
                    return {
                      name: v.variable,
                      value: v.value,
                      exp: v.expression,
                    };
                  }
                );
                let extractRegexParams = node.childNodes[i].data.regex.map(
                  (v) => {
                    return {
                      name: v.variable,
                      value: v.value,
                      exp: v.expression,
                    };
                  }
                );
                let extractXpathParams = node.childNodes[i].data.xpath.map(
                  (v) => {
                    return {
                      name: v.variable,
                      value: v.value,
                      exp: v.expression,
                    };
                  }
                );
                let vs = [
                  ...extractJsonParams,
                  ...extractRegexParams,
                  ...extractXpathParams,
                ];
                if (vs.length > 0) {
                  //数组合并
                  this.scenarioPreRequestParams =
                    this.scenarioPreRequestParams.concat(
                      extractJsonParams,
                      extractRegexParams,
                      extractXpathParams
                    );
                }
              }
              continue;
            } else {
              this.getExtractDataByNode(
                node.childNodes[i].data,
                node.childNodes[i]
              );
            }
          }
        }
      }
    },
    componentActive(node) {
      if (this.ifFromVariableAdvance) {
        this.setLeafNodeUnVisible(node);
      }
    },
    // 递归设置不需要显示的叶子节点
    setLeafNodeUnVisible(node) {
      if (!node.isLeaf) {
        if (node.childNodes.length > 0) {
          for (let i = 0; i < node.childNodes.length; i++) {
            // 提取参数不需要隐藏
            if (node.childNodes[i].isLeaf && node.childNodes[i].level > 1) {
              node.childNodes[i].visible = false;
              if (
                node.childNodes[i].data.type === "Extract" &&
                node.data.type !== "HTTPSamplerProxy"
              ) {
                node.childNodes[i].visible = true;
              }
            } else {
              // 等待控制器不显示
              if (
                node.childNodes[i].level == 1 &&
                node.childNodes[i].data.type === "ConstantTimer"
              ) {
                node.childNodes[i].visible = false;
              }
              this.setLeafNodeUnVisible(node.childNodes[i]);
            }
          }
        }
      }
    },

    getAllExtractDataByNode() {
      if (this.ifFromVariableAdvance) {
        this.selectedNode = undefined;
        this.selectedTreeNode = undefined;
        this.scenarioPreRequestParams = [];
        if (this.$refs.preTree != undefined) {
          this.getExtractDataByNode(null, this.$refs.preTree.root);
        }
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    selectVariable(node) {
      this.itemValue = node.value;
    },
    selectTab(tab) {
      this.currentTab = +tab.index;
      this.itemValue = null;
      this.itemValuePreview = null;

      if (this.ifFromVariableAdvance && this.currentTab === 3) {
        // 前置提取屏蔽部分叶子节点
        this.componentActive(this.$refs.preTree.root);
      }
    },
    showPreview() {
      // 找到变量本身
      if (!this.itemValue) {
        return;
      }
      let index = this.itemValue.indexOf("|");
      if (index > -1) {
        this.itemValue = this.itemValue.substring(0, index).trim();
      }
      this.mockVariableFuncs.forEach((f) => {
        if (!f.name) {
          return;
        }
        this.itemValue += "|" + f.name;
        if (f.params) {
          this.itemValue += ":" + f.params.map((p) => p.value).join(",");
        }
      });
      // let val = Mock.mock('[{"a":"@boolean", "b":"@range"}, 1]')
      this.itemValuePreview = calculate(this.itemValue);
    },
    methodChange(itemFunc, func) {
      let index = this.mockVariableFuncs.indexOf(itemFunc);
      this.mockVariableFuncs = this.mockVariableFuncs.slice(0, index);
      // 这里要用 deep copy
      this.mockVariableFuncs.push(JSON.parse(JSON.stringify(func)));
      this.showPreview();
    },
    radioClick(itemFunc, func) {
      if (itemFunc.name === func.name) {
        let index = this.mockVariableFuncs.indexOf(itemFunc);
        this.mockVariableFuncs = this.mockVariableFuncs.slice(0, index);
        this.mockVariableFuncs.push({ name: "", params: [] });
        let valindex = this.itemValue.indexOf("|" + func.name);
        this.itemValue = this.itemValue.slice(0, valindex);
      } else {
        this.methodChange(itemFunc, func);
      }
    },
    addFunc() {
      if (this.itemValue == undefined || this.itemValue == null) {
        this.$warning(
          this.$t("api_test.request.parameters_advance_add_mock_error")
        );
        return;
      }
      if (this.itemValue.indexOf("@") == -1) {
        this.itemValue = "@" + this.itemValue;
      } else {
        // this.itemValue = this.itemValue;
      }
      if (this.mockVariableFuncs.length > 4) {
        this.$info(
          this.$t("api_test.request.parameters_advance_add_func_limit")
        );
        return;
      }
      if (this.mockVariableFuncs.length > 0) {
        let func = this.mockVariableFuncs[this.mockVariableFuncs.length - 1];
        if (!func.name) {
          this.$warning(
            this.$t("api_test.request.parameters_advance_add_func_error")
          );
          return;
        }
        if (func.params) {
          for (let j = 0; j < func.params.length; j++) {
            if (!func.params[j].value) {
              this.$warning(
                this.$t("api_test.request.parameters_advance_add_param_error")
              );
              return;
            }
          }
        }
      }
      this.mockVariableFuncs.push({ name: "", params: [] });
    },
    saveAdvanced() {
      // if (
      //   this.itemValue != null &&
      //   this.itemValue != undefined &&
      //   this.itemValue.indexOf("@") == -1 &&
      //   this.itemValue.indexOf("$") == -1
      // ) {
      //   this.$set(this.currentItem, "value", "@" + this.itemValue);
      // } else {
      //   this.$set(this.currentItem, "value", this.itemValue);
      //   if (this.currentItem.mock != undefined) {
      //     this.$set(this.currentItem, "mock", this.itemValue);
      //   }
      // }
      // this.itemValueVisible = false;
      // this.mockVariableFuncs = [];
      // this.$emit("advancedRefresh", this.itemValue);
      this.$EventBus.$emit("handleStringAdvance", this.itemValue);
    },
    // 自定义变量
    filter() {
      let datas = [];
      this.variables.forEach((item) => {
        if (
          this.searchType &&
          this.searchType != "" &&
          this.defineVariable &&
          this.defineVariable != ""
        ) {
          if (
            (item.type &&
              item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) ==
                -1) ||
            (item.name &&
              item.name
                .toLowerCase()
                .indexOf(this.defineVariable.toLowerCase()) == -1)
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else if (this.defineVariable && this.defineVariable != "") {
          if (
            item.name &&
            item.name
              .toLowerCase()
              .indexOf(this.defineVariable.toLowerCase()) == -1
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else if (this.searchType && this.searchType != "") {
          if (
            item.type &&
            item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1
          ) {
            item.hidden = true;
          } else {
            item.hidden = undefined;
          }
        } else {
          item.hidden = undefined;
        }
        datas.push(item);
      });
      this.variables = datas;
    },
    tableRowClassName(row) {
      if (row.row.hidden) {
        return "ms-variable-hidden-row";
      }
      return "";
    },
    select(selection) {
      this.selection = selection.map((s) => s.id);
    },
    edit(row) {
      this.selection = [row.id];
      this.itemValue = "${" + row.name + "}";
    },
    savePreParams(data) {
      this.itemValue = "${" + data + "}";
    },
    handleRowClick(row) {
      if (row && row.name) {
        this.itemValue = row.name;
      }
    },

    // 前置返回
    nodeExpand(data) {
      if (data.resourceId) {
        this.expandedNode.push(data.resourceId);
      }
    },
    nodeCollapse(data) {
      if (data.resourceId) {
        this.expandedNode.splice(this.expandedNode.indexOf(data.resourceId), 1);
      }
    },
    nodeClick(data, node) {
      if (
        data.referenced != "REF" &&
        data.referenced != "Deleted" &&
        !data.disabled
      ) {
        this.operatingElements = this.stepFilter.get(data.type);
      } else {
        this.operatingElements = [];
      }
      if (!this.operatingElements) {
        this.operatingElements = this.stepFilter.get("ALL");
      }
      this.selectedTreeNode = data;
      this.selectedNode = node;
      commandStore.selectStep = data;
      this.reload();

      // 计算前置提取变量
      this.scenarioPreRequestParams = [];
      this.getExtractDataByNode(data, node);
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    setAsideHidden(data) {
      this.asideHidden = data;
    },

    showNode(node) {
      for (let i = 0; i < node.hashTree.length; i++) {
        // 右边展示如果包含了前置提取表单,且是 HTTPSamplerProxy 类型，则不需要显示提取参数列表
        if (
          node.hashTree[i].type == "Extract" &&
          node.type == "HTTPSamplerProxy" &&
          this.scenarioPreRequestParams.length > 0
        ) {
          this.scenarioPreRequestParams = [];
          break;
        }
      }
      node.active = true;
      if (
        node &&
        this.stepFilter.get("AllSamplerProxy").indexOf(node.type) != -1
      ) {
        return true;
      }
      return false;
    },
    filterSonNode(item) {
      if (
        item.type == "Assertions" ||
        item.type == "ConstantTimer" ||
        item.type == "JDBCPreProcessor" ||
        item.type == "JDBCPostProcessor"
      ) {
        return false;
      }
      return true;
    },
    outsideClick(e) {
      // 获取全部前置提取
      this.getAllExtractDataByNode();
    },
    // 获取响应结果，后续 jsonpath 引用
    suggestClick(node) {
      this.response = {};
      if (node && node.parent && node.parent.data.requestResult) {
        this.response = node.parent.data.requestResult[0];
      } else if (this.selectedNode) {
        this.response = this.selectedNode.data.requestResult[0];
      }
    },
  },
};
</script>

<style scoped>
.col-height {
  height: 40vh;
  overflow: auto;
}

.base-container {
  display: flex;
  height: 100%;
}
.var-container {
  flex: 1;
}
.view-container {
  margin-left: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.preview-row {
  /* flex: 1; */
  overflow-y: auto;
  height: 319px;
  padding: 5px;
}
.opt-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding-top: 5px;
}
.opt-row div {
  padding-right: 5px;
}
.filter-text{
  width: 80%;
}
</style>
