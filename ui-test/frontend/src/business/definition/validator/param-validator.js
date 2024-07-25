import i18n from "@/i18n/lang/i18n.js";

function testNullVal(val, pro, callback) {
  if (!val || !val[pro]) {
    callback(new Error(i18n.t('参数不能为空！')));
  }
}

let stringValidator = (val, pro, callback) => {
  if (!val) {
    callback(new Error(i18n.t('参数不能为空！')));
  }
  if (val.type !== "string") {
    callback();
  } else {
    if (!pro || pro === '') {
      callback(new Error(i18n.t('参数不能为空！')));
    }
  }
}

let urlValidator = (rule, val, callback) => {
  testNullVal(val, "text", callback);
  // if (!/^(file|http|https):\/\//.test(val.text)) {
  //   callback(new Error(i18n.t('URL 格式不正确！')));
  // } else {
  callback();
  // }
};

let resolutionValidator = (rule, val, callback) => {
  testNullVal(val, "text", callback);
  if (!/^\d+x(\d+)$/.test(val.text)) {
    callback(new Error(i18n.t('分辨率 格式不正确！')));
  } else {
    callback();
  }
};

let coordValidator = (rule, val, callback) => {
  if (!val) {
    callback(new Error(i18n.t('参数不能为空！')));
  }
  if (!/\d+,\d/.test(val.text)) {
    callback(new Error(i18n.t('坐标 格式不正确！')));
  } else {
    callback();
  }
};

let noValidator = (rule, val, callback) => {
  callback();
};

export function locatorValidator(rule, val, callback) {
  testNullVal(val, "elementType", callback);
  //根据定位方式校验
  if (val.elementType == "elementObject") {
    if (!val.elementId) {
      callback(new Error(i18n.t('请选择元素对象！')));
    }
  } else {
    if (!val.locateType || (!val.viewtarget && !val.viewLocator)) {
      callback(new Error(i18n.t('请选择正确定位方式并输入元素定位！')));
    }
  }
  callback();
}


export default {
  "vo.webUrl":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "locator":
    [
      {
        validator: locatorValidator,
        trigger: ['blur','change']
      }
    ],
  "resolution":
    [
      {
        validator: resolutionValidator,
        trigger: 'blur'
      }
    ],
  "coord":
    [
      {
        validator: coordValidator,
        trigger: 'blur'
      }
    ],
  "noValidator":
    [
      {
        validator: noValidator,
        trigger: 'blur'
      }
    ],
  "vo.handleId":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "locateType":
    [
      {
        validator: testNullVal,
        trigger: ['blur','change']
      }
    ],
  "vo.inputContent":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "formLocator":
    [
      {
        validator: locatorValidator,
        trigger: ['blur','change']
      }
    ],
  "iteratorVariable":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "arrayVariable":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "expression":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "conditions":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "vo.waitText":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "vo.waitTime":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
  "vo.optContent":
    [
      {
        validator: stringValidator,
        trigger: 'blur'
      }
    ],
}
