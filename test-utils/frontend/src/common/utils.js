import { Message } from "element-ui";

function nil(v) {
  return v === null || v === undefined;
}

function empty(v) {
  return nil(v) || v === "";
}

function unchain(obj, keys, dval = null) {
  let rst = obj;
  if (nil(rst)) {
    return dval;
  }

  for (let i = 0; i < keys.length; i++) {
    const key = keys[i];
    if (nil(rst[key])) {
      return dval;
    } else {
      rst = rst[key];
    }
  }

  return rst;
}

function chain(obj, keys, val) {
  if (nil(obj) || nil(keys) || keys.length <= 0) {
    return;
  }

  function make(keys, val) {
    let v = val;
    for (let i = keys.length; i >= 0; i--) {
      v = { [keys[i]]: v };
    }
    return v;
  }

  function helper(obj, keys, val) {
    const key = keys[0];
    if (keys.length === 1) {
      obj[key] = val;
    } else {
      if (nil(obj[key])) {
        obj[key] = make(keys.slice(1), val);
      } else {
        helper(obj[key], keys.slice(1), val);
      }
    }
  }

  helper(obj, keys, val);
}

function Toast(status, { pre, succ, fail, duration = 3000 }) {
  const succMsg = nil(succ) ? `${pre ?? ""}成功` : succ;
  const failMsg = nil(fail) ? `${pre ?? ""}失败` : fail;

  return Message({
    showClose: true,
    message: status === true ? succMsg : failMsg,
    type: status === true ? "success" : "error",
    duration
  });
}

const Utils = { nil, empty, unchain, chain, Toast };

export default Utils;
