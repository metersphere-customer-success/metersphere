import {baseGet} from "@/api/base-network";
import {get,post} from "metersphere-frontend/src/plugins/request"


export function getProjectMemberUserFilter(callBack) {
  return baseGet('/user/project/member/list', (data) => {
    if (callBack) {
      let filter = data.map(u => {
        return {text: u.name, value: u.id};
      });
      callBack(filter);
    }
  });
}

export function getProjectMemberById(projectId, callback) {
  return projectId ? baseGet('/user/project/member/' + projectId, callback) : {};
}

export function getProjectMemberOption() {
  return get('/user/project/member/option');
}
