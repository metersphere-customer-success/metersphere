import axios from "axios";
import { Utils } from "@/common";
import { Message } from "element-ui";

const VERBOSE = false;

function ELOG(flag, ...args) {
  console.error(`[REQ][${flag}]`, ...args);
}

function VLOG(flag, ...args) {
  if (VERBOSE) {
    console.log(`[REQ][${flag}]`, ...args);
  }
}

const OK_CODE = 0;

const CONFIG = {
  errorHandler: (error, msg) => {
    Message({
      showClose: true,
      message: `[${error}] ${msg}`,
      type: "error",
    });
  },
};

function _parse_res(data, url, body) {
  const { error, msg, result } = data;
  if (error == OK_CODE) {
    return result;
  } else {
    ELOG("error", data, url, body);
    const { errorHandler } = CONFIG;
    if (!Utils.nil(errorHandler)) {
      errorHandler(error, msg);
    }
    return null;
  }
}

async function get(url) {
  VLOG("GET", url);
  const { data, status } = await axios.get(url);
  VLOG("GET", url, status, data);
  return _parse_res(data, url, "");
}

async function post(url, body) {
  VLOG("POST", url, body);
  const { data, status } = await axios.post(url, body);
  VLOG("POST", url, status, data);
  return _parse_res(data, url, body);
}

function setErrorHandler(handler) {
  Object.assign(CONFIG, { errorHandler: handler });
}

const ReqUtils = {
  setErrorHandler,
};

export { get, post, ReqUtils };
