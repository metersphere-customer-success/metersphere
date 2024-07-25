import DropdownBoxVO from "./DropDownBoxVO";
import SetItemVO from "./SetItemVO";
import WaitElementVO from "./WaitElementVO";
import DialogVO from "./DialogVO";
import InputVO from "./InputVO";
import MouseClickVO from "./MouseClickVO";
import MouseMoveVO from "./MouseMoveVO";
import MouseDragVO from "./MouseDragVO";
import OpenVO from "./OpenVO";
import SelectWindowVO from "./SelectWindowVO";
import BaseVO from "./BaseVO";
import SetWindowSizeVO from "./SetWindowSizeVO";
import SelectFrameVO from "./SelectFrameVO";

const commandVo = {
  cmdTimes: {
    type: "Times",
    times: 10,
    appendCommands: ["pause"],
    validate: function () {
      return this.verifyEmpty({
        propName: "times",
        propValue: this.times,
      });
    },
  },
  cmdForEach: {
    type: "ForEach",
    arrayVariable: null,
    iteratorVariable: null,
    appendCommands: ["pause"],
    validate: function () {
      return this.verifyEmpty(
        {
          propName: "arrayVariable",
          propValue: this.arrayVariable,
        },
        {
          propName: "iteratorVariable",
          propValue: this.iteratorVariable,
        }
      );
    },
  },
  cmdWhile: {
    type: "While",
    expression: null,
    isDoWhile: false,
    timeout: 20 * 1000,
    appendCommands: ["pause"],
    validate: function () {
      return this.verifyEmpty({
        propName: "expression",
        propValue: this.expression,
      });
    },
  },
  cmdIf: {
    type: "If",
    model: "condition",
    expression: null,
    paramsFilterType: "And",
    conditions: [],
    validate: function () {
      if (this.model === "expression") {
        return this.verifyEmpty({
          propName: "expression",
          propValue: this.expression,
        });
      }
      // return this.verifyEmpty({
      //   propName: "conditions",
      //   propValue: this.conditions,
      //   propRule: (arg) => {
      //     return arg && arg.propValue && arg.propValue.length > 0;
      //   },
      // });
      //暂不校验列表
      return this.success();
    },
  },
  cmdElseIf: {
    type: "ElseIf",
    model: "condition",
    expression: null,
    paramsFilterType: "And",
    conditions: [],
    validate: function () {
      if (this.model === "expression") {
        return this.verifyEmpty({
          propName: "expression",
          propValue: this.expression,
        });
      }
      // return this.verifyEmpty({
      //   propName: "conditions",
      //   propValue: this.conditions,
      //   propRule: (arg) => {
      //     return arg && arg.propValue && arg.propValue.length > 0;
      //   },
      // });
      //暂不校验列表
      return this.success();
    },
  },
  cmdElse: {
    type: "Else",
  },
  DropdownBoxVO: new DropdownBoxVO(),
  SetItemVO: new SetItemVO(),
  WaitElementVO: new WaitElementVO(),
  DialogVO: new DialogVO(),
  InputVO: new InputVO(),
  MouseClickVO: new MouseClickVO(),
  MouseMoveVO: new MouseMoveVO(),
  MouseDragVO: new MouseDragVO(),
  OpenVO: new OpenVO(),
  SelectWindowVO: new SelectWindowVO(),
  SetWindowSizeVO: new SetWindowSizeVO(),
  SelectFrameVO: new SelectFrameVO(),
  cmdValidateTitleVO: {
    type: "ValidateTitle",
    title: null,
    failOver: true,
    validate: function () {
      return this.verifyEmpty({
        propName: "title",
        propValue: this.title,
      });
    },
  },
  cmdValidateElementVO: {
    type: "ValidateElement",
    locator: new BaseVO(),
    assertType: null,
    assertValue: null,
    failOver: true,
    validate: function () {
      let result = [];
      result.push(this.elementValidate(this.locator));
      result.push(
        this.verifyEmpty({
          propName: "assertType",
          propValue: this.assertType,
        })
      );
      if (
        this.assertType === "assertValue" ||
        this.assertType === "assertText" ||
        this.assertType === "assertNotText"
      ) {
        result.push(
          this.verifyEmpty({
            propName: "assertValue",
            propValue: this.assertValue,
          })
        );
      }
      return this.composeResult(...result);
    },
  },
  cmdValidateValueVO: {
    type: "ValidateValue",
    varName: null,
    varValue: null,
    failOver: true,
    validate: function () {
      if (this.optType && (this.optType == "null" || this.optType == "notNull")) {
        return this.verifyEmpty(
            {
              propName: "varName",
              propValue: this.varName,
            });
      }
      return this.verifyEmpty(
        {
          propName: "varName",
          propValue: this.varName,
        },
        {
          propName: "varValue",
          propValue: this.varValue,
        }
      );
    },
  },
  cmdValidateDropdownVO: {
    type: "ValidateDropdown",
    locator: new BaseVO(),
    assertType: null,
    assertValue: null,
    failOver: true,
    validate: function () {
      let result = [];
      result.push(this.elementValidate(this.locator));
      result.push(
        this.verifyEmpty({
          propName: "assertType",
          propValue: this.assertType,
        })
      );
      if (this.assertType) {
        result.push(
          this.verifyEmpty({
            propName: "assertValue",
            propValue: this.assertValue,
          })
        );
      }
      return this.composeResult(...result);
    },
  },
  cmdValidateTextVO: {
    type: "ValidateText",
    confirmWindow: true,
    text: null,
    failOver: true,
    validate: function () {
      return this.verifyEmpty({
        propName: "text",
        propValue: this.text,
      });
    },
  },
  cmdExtractWindowVO: {
    type: "ExtractWindow",
    extractType: null, // 提取类型
    webHandleVarName: null, // 网页 handle
    webTitleVarName: null, // 网页标题
    validate: function () {
      let extractType = this.extractType ? this.extractType : "storeWindowHandle";
      if (extractType === "storeWindowHandle") {
        return this.verifyEmpty({
          propName: "webHandleVarName",
          propValue: this.webHandleVarName,
        });
      }

      return this.verifyEmpty({
        propName: "webTitleVarName",
        propValue: this.webTitleVarName,
      });
    },
  },
  cmdExtractElementVO: {
    locator: new BaseVO(),
    type: "ExtractElement",
    extractType: null, // 提取类型
    attributeName: null, // 元素属性名
    varName: null, // 变量名
    varValue: null, // 变量值
    xpathAddress: null, // xpath 路径
    ifString: false, // 是否是字符串
    validate: function () {
      let result = [];
      //校验元素信息类型
      result.push(
        this.verifyEmpty({
          propName: "extractType",
          propValue: this.extractType,
        })
      );

      //校验变量名
      result.push(
        this.verifyEmpty({
          propName: "varName",
          propValue: this.varName,
        })
      );

      //匹配Xpath元素的数量
      if (this.extractType === "storeXpathCount") {
        result.push(
          this.verifyEmpty({
            propName: "xpathAddress",
            propValue: this.xpathAddress,
          })
        );
      } else if (this.extractType === "storeAttribute" || this.extractType === "storeCssAttribute") {
        //元素属性
        result.push(
          this.verifyEmpty({
            propName: "attributeName",
            propValue: this.attributeName,
          })
        );
        result.push(this.elementValidate(this.locator));
      } else if (
        this.extractType === "storeValue" ||
        this.extractType === "storeText"
      ) {
        //元素值 或元素文本
        result.push(this.elementValidate(this.locator));
      } else {
        //校验普通对象
        result.push(
          this.verifyEmpty({
            propName: "varValue",
            propValue: this.varValue,
          })
        );
      }

      return this.composeResult(...result);
    },
  },
  cmdScript: {
    type: "Script",
    scriptType: "sync", //同步脚本
    returnType: false, //无返回值
    returnValue: null,
    script: null,
  },
};

export default commandVo;
