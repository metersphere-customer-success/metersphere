import {get, post} from "metersphere-frontend/src/plugins/request"

let baseUrl = '/ui/element/module/';

export function getElementModules(projectId) {
  return projectId ? post(baseUrl + 'list/' + projectId,{}) : {};
}

export function getElementModulesOpt(projectId, param) {
  if(param){
    return projectId ? post(baseUrl + 'list/' + projectId, param) : {};
  }else{
    return projectId ? post(baseUrl + 'list/' + projectId,{}) : {};
  }
}

export function addElementModule(param) {
  return post(baseUrl + 'add', param);
}

export function editElementModule(param) {
  return post(baseUrl + 'edit', param);
}

export function dragElementModule(param) {
  return post(baseUrl + 'drag', param);
}

export function posElementModule(param, ) {
  return post(baseUrl + 'pos', param);
}

export function deleteElementModule(nodeIds) {
  return post(baseUrl + 'delete', nodeIds);
}


export default class UiElementModuleApi {
}
