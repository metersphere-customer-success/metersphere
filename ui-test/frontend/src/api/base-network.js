import {request, socket} from "metersphere-frontend/src/plugins/request"
import {get, post} from "./ajax";

export function baseGet(url, callback) {
  return get(url, callback ? (response) => {
    if (callback) {
      callback(response.data);
    }
  } : callback);
}

export function basePost(url, param, callback) {
  return post(url, param, callback ? (response) => {
    if (callback) {
      callback(response.data);
    }
  } : callback);
}

export function baseSocket(url) {
  return socket('/websocket/' + url);
}

export function getUploadConfig(url, formData) {
  return {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
}
