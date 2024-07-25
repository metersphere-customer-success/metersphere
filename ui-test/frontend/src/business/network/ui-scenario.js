import {get,post} from "metersphere-frontend/src/plugins/request"

export function getUiScenario(id) {
  return get('/ui/automation/getUiScenario/' + id).then(data => {
    if (data && data.data) {
      if (data.data.scenarioDefinition !== null && data.data.scenarioDefinition !== 'null') {
        let currentScenario = data.data;
        currentScenario.scenarioDefinition = JSON.parse(currentScenario.scenarioDefinition);
        let obj = currentScenario.scenarioDefinition;
        if (obj) {
          if (!obj.hashTree) {
            // this.$set(obj, "hashTree", []);
          }
          return currentScenario;
        }
      } else {
        return;
      }
    } else {
      return;
    }
  });
}

export function getProjectApplicationGetConfig(projectId){
  return get("/project_application/get/config/" + projectId, null);
}
