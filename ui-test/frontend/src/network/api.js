import {get, post} from "metersphere-frontend/src/plugins/request"
import {$success} from "metersphere-frontend/src/plugins/message";

export function apiCaseBatchRun(condition) {
  return post('/api/testcase/batch/run', condition, () => {
    $success("执行成功，请稍后刷新查看");
  });
}

export function getScenarioReport(reportId) {
  return reportId ? get('/ui/scenario/report/get/' + reportId) : {};
}

export function getScenarioReportAll(reportId) {
  return reportId ? get('/ui/scenario/report/getAll/' + reportId) : {};
}

export function getApiReport(testId) {
  return testId ? get('/api/definition/report/getReport/' + testId) : {};
}

export function getShareApiReport(shareId, testId) {
  return testId ? get('/share/api/definition/report/getReport/' + shareId + '/' + testId) : {};
}

export function getShareScenarioReport(shareId, reportId) {
  return reportId ? get('/share/ui/scenario/report/get/' + shareId + '/' + reportId) : {};
}

export function editApiDefinitionOrder(request, callback) {
  return post('/api/definition/edit/order', request, callback);
}

export function editApiTestCaseOrder(request, callback) {
  return post('/api/testcase/edit/order', request, callback);
}

export function getRelationshipApi(id, relationshipType, callback) {
  return get('/api/definition/relationship/' + id + '/' + relationshipType, callback);
}

export function getRelationshipCountApi(id, callback) {
  return get('/api/definition/relationship/count/' + id + '/', callback);
}

export function getStepResult(reportId, stepId) {
  return post('/ui/scenario/report/step/' + reportId + '/' + stepId);
}
