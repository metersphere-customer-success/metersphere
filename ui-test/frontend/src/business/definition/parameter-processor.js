
import json5 from "json5";
import getAllFunctionKeys from "@/business/automation/scenario/edit/command/input-function-keys";
import {getUUID} from "metersphere-frontend/src/utils";

const OutputType = {
  //新增
  CREATE: 1,
  //正常
  NORMAL: 2,
  //已被删除
  DELETE: 3,
};
class VariableData {
  /**
   * 参数元数据 构造函数
   * @param {*} id
   * @param {*} name
   * @param {*} num
   * @param {*} type
   * @param {*} value
   * @param {*} description
   */
  constructor(variable) {
    let {
      id: id,
      name: name,
      num: num,
      type: type,
      value: value,
      description: description,
      required: required,
      version: version,
      enable: enable,
      originValue: originValue,
    } = variable;
    this.id = id;
    this.name = name;
    this.num = num;
    this.type = type;
    this.value = value;
    this.description = description;
    //场景变量 默认必填
    this.required = required !== undefined ? required === true : true;
    this.enable = enable !== undefined ? enable === true : true;
    //是否可编辑

    //版本控制 当前用于历史数据处理标识
    this.version = version;

    this.originValue = originValue;
  }

  static createVar(name, type, value) {
    return new VariableData({
      id: getUUID(),
      name,
      num: undefined,
      type,
      value: value,
      description: "",
      required: true,
      version: VersionType.LASTED,
      originValue: value ? value : undefined,
    });
  }
}

class ParamType {
  //默认字符串
  static STRING = "STRING";
  static NUMBER = "NUMBER";
  static ARRAY = "ARRAY";
  static JSON = "JSON";

  /**
   * 废弃
   */
  static CONSTANT = "CONSTANT";
}

class VersionType {
  static LASTED = "v2.0";
}

/**
 * 类型转换器
 */
class ParamTypeConvert {
  constructor() {}

  /**
   * 根据传入的值 返回对应的类型
   * 包含处理Store操作
   */
  static convert(val, isStr) {
    if (!val || isStr) {
      return ParamType.STRING;
    }
    let type = ParamType.STRING;
    if (this.checkIsJson(val) && val.startsWith("{") && val.endsWith("}")) {
      type = ParamType.JSON;
    }
    if (this.checkIsJson(val) && val.startsWith("[") && val.endsWith("]")) {
      type = ParamType.ARRAY;
    }
    if (!isNaN(val)) {
      type = ParamType.NUMBER;
    }
    return type;
  }
  static checkIsJson(json) {
    try {
      json5.parse(json);
      return true;
    } catch (e) {
      return false;
    }
  }
}

/**
 *  参数元数据
 */
class ParamMetaData {
  constructor() {
    this.variableArray = [];
  }

  /**
   * 加载原场景变量数据
   */
  load(variables) {
    //
    let cur = [];
    if (!variables || variables.length <= 0) {
      return cur;
    }

    let tempCache = new Map();
    let tempIdCache = new Map();
    variables.forEach((e) => {
      if (e.name && !tempIdCache.get(e.name)) {
        tempCache.set(e.id, e.name);
        tempIdCache.set(e.name, e.id);
      }
    });
    variables = variables.filter((e) => {
      return tempCache.get(e.id);
    });
    for (let i = 0; i < variables.length; i++) {
      variables[i].required = false;
      if (variables[i].originValue === undefined) {
        variables[i].originValue = variables[i].value;
      }
      cur.push(new VariableData(variables[i]));
    }
    return cur;
  }

  loadOutput(variables) {
    let cur = [];
    if (!variables || variables.length <= 0) {
      return cur;
    }
    for (let i = 0; i < variables.length; i++) {
      variables[i].status = OutputType.NORMAL;
      if (variables[i].result === undefined) {
        variables[i].result = "未执行";
      }
      cur.push(new OutputMetaData(variables[i]));
    }
    return cur;
  }
}

/**
 *  参数处理器
 *  主要处理 指令的 入参 出参逻辑
 *  Sence v2.4
 *  Author Nathan.Liu
 */
export default class ParamProcessor {
  /**
   * INNER 入参
   * OUTPUT 出参
   * @param {*} type
   */
  constructor() {
    this.innerProcessor = new InnerParamProcessor();
    this.outputProcessor = new OutputParamProcessor();
  }

  /**
   * 根据类型获取对应的处理器
   */
  getParamProcessor(type) {
    if (type === "INNER") {
      return this.innerProcessor;
    }
    return this.outputProcessor;
  }

  convert(scenarioDefinition, variables) {
    //处理入参
    this.innerProcessor.convert(scenarioDefinition, variables);
    this.outputProcessor.convert(scenarioDefinition, variables);
  }

  static validate(scenarioDefinition) {
    return ParamValidatorProcessor.validate(scenarioDefinition);
  }
}

/**
 * 抽象 入参 出参 处理器
 */
class AbstractParamProcessor {
  constructor() {
    this.targetList = [];
    this.paramMap = new Map();
    this.funcKeys = getAllFunctionKeys();
    this.funcKeyMap = new Map();
    this.funcKeys.forEach(k => {
      if(k.content){
        this.funcKeyMap.set(k.content, k)
      }
    })
  }
  /**
   * 转换数据为 metadata, 含指令调试数据的结构
   *
   * @param originData hashtree arr
   * @return param list
   */
  convert(originData) {
    return this.targetList;
  }
  /**
   * 新旧对比
   */
  diff(originArr, curArr) {
    return [];
  }

  allocate(key) {
    let result = this.findArea(key);
    if (!result) {
      this.paramMap.set(key, []);
      return this.findArea(key);
    }
    return result;
  }

  findArea(key) {
    return this.paramMap.get(key);
  }

  updateArea(key, value) {
    this.paramMap.set(key, value);
  }

  /**
   * 从字符串中提取 引用的变量
   */
  findVarByStr(definition) {
    let arr = [];
    if (!definition) {
      return arr;
    }
    let match = definition.match(/\${([^\$].*?)}/g);
    if (!match) {
      return arr;
    }

    //解析变量
    for (let i = 0; i < match.length; i++) {
      let str = match[i];
      let r2 = /\${([^\$].*?)}/g.exec(str);
      if (!r2) {
        continue;
      }
      let variableName = r2[1];
      arr.push(this.iteratorFind(variableName));
    }

    let res = [...new Set(arr)];
    // 过滤功能键的参数定义
    if(this.funcKeyMap){
      res = res.filter(r => {
        return !this.funcKeyMap.get("${" + r + "}");
      });
    }

    return res;
  }

  iteratorFind(variableName) {
    //进一步解析 处理 obj.a  obj[0]  obj[0][1] 类型变量名称
    let pro = /([^.\n]+)\.(.*)/.exec(variableName);
    if (pro) {
      let temp = pro[1];
      temp = this.iteratorFind(temp);
      return temp;
    } else if (/([^.\n]+)(\[(\d*)\]+)/.exec(variableName)) {
      let proNum = /([^.\n]+)(\[(\d*)\]+)/.exec(variableName);
      let temp = proNum[1];
      temp = this.iteratorFind(temp);
      return temp;
    } else {
      return variableName;
    }
  }
}

/**
 * 入参处理
 */
class InnerParamProcessor extends AbstractParamProcessor {
  constructor() {
    super();
    this.paramMetaData = new ParamMetaData();
    this.outputParamTree = new Map();
    this.storeParamMap = new Map();
  }
  /**
   * 入参转换器
   * 转换规则：
   *    检测步骤中所使用的变量
   *    原场景 变量 默认为否
   * @param {*} hashTree
   */
  convert(scenarioDefinition, variables) {
    //重置 store 缓存
    this.storeParamMap = new Map();
    //处理入参转换
    if (Array.isArray(scenarioDefinition)) {
      //检测是否需要导入历史环境变量
      let needHandle =
        !variables ||
        variables.find((v) => {
          return (v.version === undefined || v.version === null) && v.name;
        });
      if (needHandle) {
        variables.forEach((v) => {
          v.version = VersionType.LASTED;
          if (v.type === ParamType.CONSTANT) {
            v.type = ParamType.STRING;
          }
        });
      }
      scenarioDefinition.forEach((e) => {
        if (needHandle) {
          //此处为场景调试 直接覆盖即可
          e.variables = variables;
        }
        this.doConvert(e, e.id);
      });
    } else {
      this.doConvert(scenarioDefinition, scenarioDefinition.id);
    }
  }

  injectVars(to, from) {
    for (let i = 0; i < from.length; i++) {
      let e = to.find((v) => {
        return (from[i].name = v.name);
      });
      if (!e) {
        to.push(from[i]);
      }
    }
  }

  /**
   * 真正执行转换逻辑
   *
   * 需要特殊处理 store 等结构， 及 原始场景变量， 并且迭代获取当前引用变量情况 回显参数必填信息
   * @param {*} scenarioDefinition
   */
  doConvert(scenarioDefinition, key) {
    //分配内存
    let currentArea = this.allocate(key);
    /**
     * 场景变量 需要前置 填充
     */
    //自定义指令才进行相关处理
    if (scenarioDefinition.type === "customCommand") {
      let vars = this.paramMetaData.load(scenarioDefinition.variables);
      //解析到当前
      currentArea = vars;
      currentArea = currentArea.filter((e) => {
        return e.name;
      });
      this.updateArea(key, currentArea);
    }

    let hits = [];

    //找到当前指令步骤中引用的变量， 并存储
    if (scenarioDefinition.hashTree) {
      scenarioDefinition.hashTree.forEach((element) => {
        //先处理前后置中的 数据提取
        this.storeParamHandler(element);
        let sub = this.doConvert(element, element.id);
        hits = [...hits, ...sub];
      });
    }

    if (scenarioDefinition.preCommands) {
      scenarioDefinition.preCommands.forEach((element) => {
        let sub = this.doConvert(element, key);
        hits = [...hits, ...sub];
      });
    }

    if (scenarioDefinition.postCommands) {
      scenarioDefinition.postCommands.forEach((element) => {
        let sub = this.doConvert(element, key);
        hits = [...hits, ...sub];
      });
    }

    /**
     * 场景中的步骤  引用变量 后置获取，
     */
    //检测是否存在引用变量
    if (scenarioDefinition.vo) {
      let def = JSON.stringify(scenarioDefinition.vo);
      hits = [...hits, ...this.findVarByStr(def)];
    }
    //TODO 替换原参数列表
    if (scenarioDefinition.type === "customCommand") {
      //当前参数列表也需要更新
      currentArea = this.diff(currentArea, hits);
      this.updateArea(key, currentArea);

      //填充到原场景变量
      scenarioDefinition.variables = currentArea;
    }
    //返回 当前集合
    return hits;
  }

  storeParamHandler(element) {
    if (!element || !element.command) {
      return;
    }
    switch (element.command) {
      case "cmdExtractElement":
        this.doStoreParamHandler(element.vo);
        break;
    }
  }

  doStoreParamHandler(e) {
    if (!e) {
      return;
    }

    if (e.varName) {
      let type = ParamTypeConvert.convert(e.varValue, e.ifString);
      this.storeParamMap.set(
        e.varName,
        new VariableData({
          name: e.varName,
          value: e.varValue,
          type: type,
        })
      );
    }
  }

  /**
   *  合并当前 参数列表
   *  原参数中有 则必填校验为true
   *  原参数列表中没有 则为 false， 默认字符串类型， 值为空
   * @param {*} originArr
   * @param {*} curArr
   */
  diff(originArr, curArr) {
    if (!curArr) {
      curArr = [];
    }
    // TODO 返回diff  merge后的结果
    for (let i = 0; i < originArr.length; i++) {
      //检测 当前参数中是否存在
      originArr[i].version = VersionType.LASTED;
      let e = curArr.find((v) => {
        return originArr[i].name === v;
      });
      if (e) {
        originArr[i].required = true;
        //当前刷新下 旧数据
        if (originArr[i].originValue === "${" + e + "}") {
          originArr[i].originValue = undefined;
        }
        if (originArr[i].value === "${" + e + "}") {
          originArr[i].value = undefined;
        }

        // if (this.storeParamMap && this.storeParamMap.get(originArr[i].name)) {
        //   //判断之前是否有值
        //   if (!originArr[i].value || originArr[i].type != e.type) {
        //     let temp = this.storeParamMap.get(originArr[i].name);
        //     if(temp.type){
        //       originArr[i].type = temp.type;
        //     }
        //     originArr[i].value = temp.value;
        //   }
        // }
      }
    }

    //反向查找
    for (let i = 0; i < curArr.length; i++) {
      let e = originArr.find((v) => {
        return curArr[i] === v.name;
      });
      let g = this.smartMerge(curArr[i]);
      if (!e) {
        //创建时候需要查看是否存在 数据提取， 的同名变量
        originArr.push(g);
      } else {
        //存在更新下类型
        if (g) {
          if (g.value != undefined || g.value != null) {
            e.type = g.type;
            e.value = g.value;
          }
        }
      }
    }

    return originArr;
  }

  /**
   * 智能合并
   * @param key 变量名
   */
  smartMerge(key) {
    if (!this.storeParamMap || !this.storeParamMap.get(key)) {
      return VariableData.createVar(key, ParamType.STRING);
    }

    //合并
    let temp = this.storeParamMap.get(key);
    return VariableData.createVar(key, temp.type, temp.value);
  }
}

/**
 * 出参处理
 */
class OutputParamProcessor extends AbstractParamProcessor {
  constructor() {
    super();
    this.paramMetaData = new ParamMetaData();
    this.setStepTreeMap = new Map();
    this.setStepNameMap = new Map();
  }

  /**
   * 生成 指令的步骤信息
   */
  generateCommandStepInfo(scenarioDefinition) {
    //generate
    if (Array.isArray(scenarioDefinition)) {
      //处理调试数据
      scenarioDefinition.forEach((e) => {
        this.doGenerate(e);
      });
    } else {
      //处理场景数据
      this.doGenerate(scenarioDefinition);
    }
  }

  getFullIndex(p, c, useDebugger) {
    if (useDebugger) {
      return "";
    }
    if (p) {
      return p + "-" + c;
    }

    return c;
  }
  doGenerate(scenarioDefinition, parent, useParentIndex, parentStepName) {
    let currentIndex = useParentIndex
      ? parent
      : this.getFullIndex(
          parent,
          scenarioDefinition.index,
          scenarioDefinition.useDebugger
        );

    let stepName = useParentIndex ? parentStepName : scenarioDefinition.name;

    this.stepTreeMap.set(scenarioDefinition.id, currentIndex);
    this.stepNameTreeMap.set(scenarioDefinition.id, stepName);

    if (scenarioDefinition.hashTree) {
      scenarioDefinition.hashTree.forEach((item) => {
        this.doGenerate(
          item,
          currentIndex,
          useParentIndex,
          !useParentIndex ? item.name : stepName
        );
      });
    }

    if (scenarioDefinition.preCommands) {
      scenarioDefinition.preCommands.forEach((item) => {
        this.doGenerate(item, currentIndex, true, stepName);
      });
    }

    if (scenarioDefinition.postCommands) {
      scenarioDefinition.postCommands.forEach((item) => {
        this.doGenerate(item, currentIndex, true, stepName);
      });
    }
  }

  convert(scenarioDefinition) {
    //生成步骤层级结构到Map
    this.stepTreeMap = new Map();
    this.stepNameTreeMap = new Map();
    this.generateCommandStepInfo(scenarioDefinition);

    if (Array.isArray(scenarioDefinition)) {
      scenarioDefinition.forEach((e) => {
        this.doConvert(e, e.id);
      });
    } else {
      this.doConvert(scenarioDefinition, scenarioDefinition.id);
    }
  }
  isJSON(str) {
    try {
      JSON.parse(str);
      return true;
    } catch (e) {
      return false;
    }
  }
  strToJSON(str) {
    if (Array.isArray(str)) {
      return str;
    }
    if (!str || !this.isJSON(str)) {
      return undefined;
    }
    let result = JSON.parse(str);
    return result;
  }
  doConvert(scenarioDefinition, key) {
    //分配内存
    let currentArea = this.allocate(key);
    //自定义指令才进行相关处理
    if (scenarioDefinition.type === "customCommand") {
      let vars = this.paramMetaData.loadOutput(
        this.strToJSON(scenarioDefinition.outputVariables)
      );
      if (vars) {
        vars.forEach((e) => {
          let stepName = this.stepNameTreeMap.get(e.sourceId);
          if (stepName) {
            e.stepName = stepName;
          }
          let stepIndex = this.stepTreeMap.get(e.sourceId);
          if (stepIndex) {
            e.step = stepIndex;
          }
        });
      }
      //解析到当前
      currentArea = [...currentArea, ...vars];
      currentArea = currentArea.filter((e) => {
        return e.name;
      });
      this.updateArea(key, currentArea);
    }

    let hits = [];

    //找到当前指令步骤中引用的变量， 并存储
    if (scenarioDefinition.hashTree) {
      scenarioDefinition.hashTree.forEach((element) => {
        let sub = this.doConvert(element, element.id);
        hits = [...hits, ...sub];
      });
    }

    if (scenarioDefinition.preCommands) {
      scenarioDefinition.preCommands.forEach((element) => {
        let sub = this.parse("PRE", element);
        hits = [...hits, ...sub];
      });
    }

    if (scenarioDefinition.postCommands) {
      scenarioDefinition.postCommands.forEach((element) => {
        let sub = this.parse("POST", element);
        hits = [...hits, ...sub];
      });
    }

    // 替换原参数列表
    if (scenarioDefinition.type === "customCommand") {
      currentArea = this.diff(currentArea, hits);

      //当前参数列表也需要更新
      this.updateArea(key, currentArea);

      //填充到原场景变量
      scenarioDefinition.outputVariables = JSON.stringify(currentArea);
    }
    //返回 当前集合
    return hits;
  }

  /**
   * 解析 前后置中的 数据提取 及  脚本操作
   */
  parse(position, e) {
    let hits = [];
    //脚本 及 数据提取处理
    if (e.vo) {
      //
      let sub = this.translate(position, e.vo, e.id);
      if (sub) {
        hits.push(sub);
      }
    }

    if (!e.hashTree) {
      return hits;
    }

    e.hashTree.forEach((item) => {
      //处理 数据提取
      if (item.vo) {
        let sub = this.translate(position, item.vo, item.id);
        if (sub) {
          hits.push(sub);
        }
      }
    });

    return hits;
  }

  translate(position, e, id) {
    //extractType
    let result = undefined;
    switch (e.type) {
      case "Script":
        if (e.returnValue) {
          result = this.compile(
            position,
            e.type,
            e.scriptType,
            e.returnValue,
            e.value,
            id
          );
        }
        break;
      case "ExtractWindow":
        result = this.compile(
          position,
          e.type,
          e.extractType,
          e.webTitleVarName ? e.webTitleVarName : e.webHandleVarName,
          e.value,
          id
        );
        break;
      case "ExtractElement":
        result = this.compile(
          position,
          e.type,
          e.extractType,
          e.varName,
          e.value,
          id
        );
        break;
      default:
        break;
    }
    return result;
  }

  translateExtract(e) {
    switch (e.extractType) {
      case "storeText":
        break;
      default:
        break;
    }
    return undefined;
  }

  /**
   * 编译出参
   * 构建 key 返回对象
   * 检验 val 是否为编译器可确定的值
   * @param {*} key
   * @param {*} val
   */
  compile(position, type, extractType, varName, value, sourceId) {
    let step = this.stepTreeMap.get(sourceId);
    let stepName = this.stepNameTreeMap.get(sourceId);
    let result = new OutputMetaData({
      id: getUUID(),
      name: varName,
      value,
      type: extractType,
      sourceId,
      position,
      commandType: type,
      step,
      stepName,
    });

    return result;
  }

  /**
   * 对比算法
   */
  diff(originArr, curArr) {
    for (let i = 0; i < curArr.length; i++) {
      let exist = originArr.find((e) => {
        //TODO 需要校验 步骤信息是否一致
        return e.name === curArr[i].name;
      });
      if (!exist) {
        //新增状态
        curArr[i].status = OutputType.CREATE;
        originArr.push(curArr[i]);
      } else {
        exist.sourceId = curArr[i].sourceId;
      }
    }

    //找出原来不存在的标记为DELETE
    for (let j = 0; j < originArr.length; j++) {
      let exist = curArr.find((e) => {
        return e.name === originArr[j].name;
      });
      if (!exist) {
        //新增状态
        originArr[j].status = OutputType.DELETE;
      }
    }
    return originArr;
  }

  //填充运行结果
  fillRunTimeData(scenarioDefinition, result) {
    if (!scenarioDefinition || !result) {
      return;
    }
    this.outputParamTree = new Map();
    //生成结果树
    this.generateResultTree(result);
    //遍历当前的出参数据 进行 相关映射
    if (Array.isArray(scenarioDefinition)) {
      //处理调试数据
      scenarioDefinition.forEach((e) => {
        this.doFill(e);
      });
    } else {
      //处理场景数据
      this.doFill(scenarioDefinition);
    }
  }

  doFill(scenarioDefinition) {
    if (scenarioDefinition.outputVariables) {
      scenarioDefinition.outputVariables = this.findVarResultToData(
        scenarioDefinition.outputVariables
      );
    }

    if (scenarioDefinition.hashTree) {
      scenarioDefinition.hashTree.forEach((e) => {
        this.doFill(e);
      });
    }
  }

  findVarResultToData(outputVariables) {
    let outputs = this.strToJSON(outputVariables);
    for (let i = 0; i < outputs.length; i++) {
      let obj = this.outputParamTree.get(outputs[i].sourceId);
      if (obj) {
        outputs[i].result = obj.value;
      } else {
        outputs[i].result = "未执行";
      }
    }

    return JSON.stringify(outputs);
  }

  generateResultTree(result) {
    for (let i = 0; i < result.length; i++) {
      let outputList = result[i].outputList;
      if (!outputList) {
        continue;
      }

      outputList.forEach((item) => {
        this.outputParamTree.set(item.id, item);
      });
    }
  }
}

/**
 * 出参 元数据结构
 */
class OutputMetaData {
  constructor({
    id,
    name,
    value,
    type,
    step,
    stepName,
    result,
    remark,
    status,
    position,
    commandType,
    sourceId,
  }) {
    this.id = id;
    this.name = name;
    /**
     * 存放编译期间的值信息， 运行期间产生的值将放到result中
     */
    this.value = value;
    this.type = type;
    /**
     * PRE POST MAIN
     */
    this.position = position;
    /**
     * 事件指令名称
     */
    this.commandType = commandType;
    this.step = step;
    this.stepName = stepName;
    this.result = result === undefined ? "未执行" : result;
    this.remark = remark;
    this.status = status ? status : OutputType.CREATE;
    //显示全名
    this.fullname = "";
    this.sourceId = sourceId;
  }
}

class ParamValidatorProcessor {
  static validate(scenarioDefinition) {
    return new ParamValidator().validate(scenarioDefinition);
  }
}
/**
 * 参数校验器
 */
class ParamValidator {
  /**
   * 校验场景或指令是否有 入参填写错误
   */
  validate(scenarioDefinition) {
    try {
      if (Array.isArray(scenarioDefinition)) {
        scenarioDefinition.forEach((e) => {
          this.doValidate(e);
        });
      } else {
        this.doValidate(scenarioDefinition);
      }
    } catch (e) {
      if (e instanceof ParamValidateError) {
        return new ValidateDTO(parseInt(e.message), e.key);
      }
    }
    return new ValidateDTO();
  }
  doValidate(scenarioDefinition) {
    if (!scenarioDefinition) {
      return;
    }
    if (
      scenarioDefinition.variables &&
      scenarioDefinition.type === "customCommand"
    ) {
      //检测 是否有 同名 或 名字为空的
      if (Array.isArray(scenarioDefinition.variables)) {
        let cache = new Map();
        scenarioDefinition.variables.forEach((item) => {
          if (!item.name && item.value) {
            throw new ParamValidateError(ValidateType.EMPTY_NAME);
          }
          if (item.name) {
            if (!cache.get(item.name)) {
              cache.set(item.name, item.name);
            } else {
              throw new ParamValidateError(ValidateType.DUP_NAME, item.name);
            }
          }
        });
      }
    }

    if (scenarioDefinition.hashTree) {
      scenarioDefinition.hashTree.forEach((item) => {
        this.doValidate(item);
      });
    }
  }
}

class ParamValidateError extends Error {
  constructor(status, key) {
    super(status);
    this.key = key;
  }
}

class ValidateType {
  static SUCCESS = 1;
  static ERROR = -1;
  /**
   * 名字为空
   */
  static EMPTY_NAME = -2;
  /**
   * 名字重复
   */
  static DUP_NAME = -3;
}
class ValidateDTO {
  constructor(status, key) {
    this.status = status == undefined ? ValidateType.SUCCESS : status;
    this.key = key;
  }
}
