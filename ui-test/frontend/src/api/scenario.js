import {get, post, request, _fileUpload} from 'metersphere-frontend/src/plugins/request'

export function editScenario(config) {
  return request(config);
}

export function importScenario(url, file, files, params) {
  return _fileUpload(url, file, files, params);
}

export function uiScenarioEnvMap(params) {
  return post('/ui/automation/env/map', params);
}

export function uiScenarioEnv(params) {
  return post('/ui/automation/env', params);
}


export function checkScenarioEnv(scenarioId) {
  return get('/ui/automation/env-valid/' + scenarioId);
}

export function setScenarioDomain(params) {
  return post('/ui/automation/set-domain', params);
}