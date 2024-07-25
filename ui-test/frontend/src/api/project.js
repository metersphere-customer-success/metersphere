import {baseGet} from "@/api/base-network";
import {get} from 'metersphere-frontend/src/plugins/request'
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";

export function getProject(projectId) {
  return get('/project/get/' + projectId);
}

export function getVersionFilters(projectId, callback) {
  return hasLicense() && projectId ?
    baseGet('/project/version/get-project-versions/' + projectId, data => {
      let versionFilters = data.map(u => {
        return {text: u.name, value: u.id};
      });
      if (callback) {
        callback(versionFilters);
      }
    }) : {};
}

export function getAll() {
    return get('/project/listAll/' + getCurrentWorkspaceId());
}

export function getOwnerProjectIds() {
    let url = '/project/get/owner/ids';
    return get(url);
}