import {get, post} from "metersphere-frontend/src/plugins/request"

export function getReport(url) {
  return get(url, null, {});
}

export default {getReport}

export function queryUiScenarioReportList(currentPage, pageSize, param) {
  return post("/ui/scenario/report/list/" + currentPage + "/" + pageSize, param);
}

export function queryApiDefinitionReport(id) {
  return get("/api/definition/report/get/" + id);
}

export function deleteUiReport(param) {
  return post("/ui/report/delete", param);
}

export function banchDeleteUiReport(param) {
  return post("/ui/scenario/report/batch/delete", param);
}
