import {_fileUpload, get, post} from "metersphere-frontend/src/plugins/request"

let baseUrl = '/ui/element/';

export function getUiElements(page, param) {
  return post(baseUrl + 'list/' + page.currentPage + '/' + page.pageSize, param);
}

export function getUiElementById(id) {
  return get(baseUrl + id);
}

export function getUiElementNames(param) {
  return post(baseUrl + 'list/name', param);
}

export function addUiElement(param) {
  return post(baseUrl + 'add', param);
}

export function editUiElement(param) {
  return post(baseUrl + 'edit', param);
}

export function deleteUiElement(id) {
  return get(baseUrl + 'delete/' + id);
}

export function checkReference(projectId, id) {
  return get(baseUrl + 'reference/' + projectId + '/' + id);
}

export function checkModulesReference(projectId, param) {
  return post(baseUrl + 'reference/modules/' + projectId, param);
}

export function downloadTemplate(projectId){
  return get(baseUrl + 'export/template/' + projectId);
}

export function checkReferenceBatch(projectId, params) {
  return post(baseUrl + 'reference/' + projectId, params);
}

export function deleteUiElementBatch(ids) {
  return post(baseUrl + 'delete', ids);
}

export function copyUiElementBatch(url, param) {
  return post(url, param);
}

export function importElement(url, file, files, params) {
  return _fileUpload(url, file, files, params);
}

export default class UiElementApi {
}
