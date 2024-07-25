import {post} from "@/api/ajax";
import {baseGet} from "@/api/base-network";


export function generateApiDocumentShareInfo(param, callback) {
  return post("/share/info/generateApiDocumentShareInfo", param, response => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function generateShareInfoWithExpired(param, callback) {
  return post("/share/info/generateShareInfoWithExpired", param, response => {
    if (callback) {
      callback(response.data);
    }
  });
}

export function getShareInfo(id, callback) {
  return id ? baseGet('/share/info/get/' + id, callback) : {};
}

