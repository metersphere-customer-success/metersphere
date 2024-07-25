import {get} from "metersphere-frontend/src/plugins/request"

export function getUiScenarioByPlanScenarioId(id) {
  return get('/test/plan/uiScenario/case/get-scenario/' + id);
}
