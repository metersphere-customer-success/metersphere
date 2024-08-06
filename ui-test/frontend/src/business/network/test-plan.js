import {post, get} from "@/api/ajax";
import {$success} from"metersphere-frontend/src/plugins/message";
import i18n from "@/i18n/lang/i18n";
import {baseGet, basePost} from "@/api/base-network";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export function getTestPlanReport(planId, callback) {
  if (planId) {
    return get('/test/plan/report/' + planId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
}

export function getShareTestPlanReport(shareId, planId, callback) {
  if (planId) {
    return get('/share/test/plan/report/' + shareId + '/' + planId, (response) => {
      if (callback) {
        callback(response.data);
      }
    });
  }
}

export function editPlanReport(param, callback) {
  return post('/test/plan/edit/report', param, () => {
    if (callback) {
      callback();
    } else {
      $success(i18n.t('commons.save_success'));
    }
  });
}

export function editPlanReportConfig(param, callback) {
  return basePost('/test/plan/edit/report/config', param, (data) => {
    $success(i18n.t('commons.save_success'));
    if (callback) {
      callback(data);
    }
  });
}

export function getTestPlanReportContent(reportId, callback) {
  return reportId ? baseGet('/test/plan/report/db/' + reportId, callback) : {};
}

export function getShareTestPlanReportContent(shareId, reportId, callback) {
  return reportId ? baseGet('/share/test/plan/report/db/' + shareId + '/' + reportId, callback) : {};
}

export function getPlanFunctionAllCase(planId, param, callback) {
  return planId ? basePost('/test/plan/case/list/all/' + planId, param, callback) : {};
}

export function getSharePlanFunctionAllCase(shareId, planId, param, callback) {
  return planId ? basePost('/share/test/plan/case/list/all/' + shareId + '/' + planId, param, callback) : {};
}

export function getPlanScenarioFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/failure/' + planId, callback) : {};
}

export function getPlanScenarioErrorReportCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/errorReport/' + planId, callback) : {};
}

export function getPlanScenarioUnExecuteCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/unExecute/' + planId, callback) : {};
}

export function getPlanScenarioAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/scenario/case/list/all/' + planId, callback) : {};
}

export function getSharePlanScenarioFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanScenarioAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanScenarioErrorReportCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/errorReport/' + shareId + '/' + planId, callback) : {};
}
export function getSharePlanScenarioUnExecuteCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/scenario/case/list/unExecute/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanUiScenarioAllCase(shareId, planId, param, callback) {
  return planId ? basePost('/share/test/plan/uiScenario/case/list/all/' + shareId + '/' + planId, param, callback) : {};
}

export function getPlanApiFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/failure/' + planId, callback) : {};
}

export function getPlanApiErrorReportCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/errorReport/' + planId, callback) : {};
}
export function getPlanApiUnExecuteCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/unExecute/' + planId, callback) : {};
}
export function getPlanApiAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/api/case/list/all/' + planId, callback) : {};
}

export function getSharePlanApiFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanApiErrorReportCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/errorReport/' + shareId + '/' + planId, callback) : {};
}
export function getSharePlanApiUnExecuteCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/unExecute/' + shareId + '/' + planId, callback) : {};
}
export function getSharePlanApiAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/api/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function getPlanLoadFailureCase(planId, callback) {
  return planId ? baseGet('/test/plan/load/case/list/failure/' + planId, callback) : {};
}

export function getPlanLoadAllCase(planId, callback) {
  return planId ? baseGet('/test/plan/load/case/list/all/' + planId, callback) : {};
}

export function getSharePlanLoadFailureCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/load/case/list/failure/' + shareId + '/' + planId, callback) : {};
}

export function getSharePlanLoadAllCase(shareId, planId, callback) {
  return planId ? baseGet('/share/test/plan/load/case/list/all/' + shareId + '/' + planId, callback) : {};
}

export function checkoutLoadReport(param, callback) {
  return basePost('/test/plan/load/case/report/exist', param, callback);
}

export function shareCheckoutLoadReport(shareId, param, callback) {
  return basePost('/share/test/plan/load/case/report/exist/' + shareId, param, callback);
}


export function getPlanStageOption(callback) {
  let projectID = getCurrentProjectID();
  return projectID ? baseGet('/test/plan/get/stage/option/' + projectID, callback) : {};
}


export function saveTestPlanReport(planId, callback) {
  return planId ? baseGet('/test/plan/report/saveTestPlanReport/' + planId + '/MANUAL', callback) : {};
}

export function getPlanUiScenarioAllCase(planId, param, callback) {
  return planId ? basePost('/test/plan/uiScenario/case/list/all/' + planId, param, callback) : {};
}