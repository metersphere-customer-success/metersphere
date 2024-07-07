import { Utils, Encrypt } from "@/common";
import { get, post } from "./common";

const SEC_KEY = "&cTp1aT*";
// const HOST = "http://127.0.0.1:7600";
// const HOST = "http://phecda.cicc.com.cn";
// const URL_PREFIX = HOST;
const URL_PREFIX = "/testutils";
const BASE_URL = `${URL_PREFIX}/covs/meter`;
const FRONT_END = 2;

function _gen_token() {
  return Encrypt.encrypt(SEC_KEY, `${Date.now()}`);
}

async function _post(path, body) {
  const url = `${BASE_URL}${path}`;
  body = Object.assign({ token: _gen_token() }, body);
  return await post(url, body);
}

async function projects() {
  return await _post("/projects");
}

async function project(pid) {
  return await _post("/project", { pid, type: "metersphere" });
}

async function sessionList(app) {
  return await _post("/sessions", { app });
}

async function batchList(session) {
  return await _post("/batches", { session });
}

async function taskList(session, batch) {
  return await _post("/tasks", { session, batch });
}

async function itagList(session, batch, commit) {
  return await _post("/itags", { session, batch, commit });
}

async function itagSet(app, session, batch, commit) {
  return await _post("/itag/set", { app, session, batch, commit });
}

async function itagUnset(session, batch, commit) {
  return await _post("/itag/unset", { session, batch, commit });
}

async function covsMinioLoad(tid) {
  const name = `covs/covs.${tid}.json`;
  return await _post("/covs/minio/load", { name });
}

async function covsAppPermission(
  apps,
  tid,
  flowInstId,
  flowCompInstId,
  admin,
  gtokens
) {
  if (Utils.nil(tid) && Utils.nil(flowInstId) && Utils.nil(flowCompInstId)) {
    return null;
  }

  return await _post("/app/permission", {
    apps,
    tid,
    flowInstId,
    flowCompInstId,
    admin,
    gtokens,
  });
}

async function covsAppSharePermission(name, gtoken) {
  return await _post("/app/share/permission", { name, gtoken });
}

async function covsAppCovs(tid) {
  return await _post("/app/covs", { tid });
}

async function covsAppCode(tid, path, mod = null) {
  return await _post("/app/code", { tid, path, mod });
}

async function covsAppRegister(data) {
  const {
    phecda,
    type,
    name,
    git,
    src,
    branch,
    excludes,
    includes,
    applicant,
  } = data;
  const body = {
    phecda,
    type,
    name,
    git,
    src,
    branch,
    excludes,
    includes,
    applicant,
  };
  return await _post("/app/register", body);
}

async function covsAppUnregister(name) {
  return await _post("/app/unregister", { name });
}

async function covsAppConfirm(name) {
  return await _post("/app/confirm", { name });
}

async function covsAppModify(name, { git, src, branch, excludes, includes }) {
  return await _post("/app/modify", {
    name,
    git,
    src,
    branch,
    excludes,
    includes,
  });
}

async function covsAppRegList() {
  return await _post("/app/reglist");
}

async function covsAppStatus(name) {
  return await _post("/app/status", { name });
}

async function covsAppTasks(app, sday, eday) {
  return await _post("/app/tasks", { app, sday, eday })
}

async function covsAppCommitMessage(app, commit) {
  return await _post("/app/commit/message", { app, commit })
}

async function covsCompFlow(flowInstId, flowCompInstId) {
  return await _post("/comp/flow", { flowInstId, flowCompInstId });
}

async function covsPhecdaUser(email) {
  return await _post("/phecda/user", { email });
}

async function covsPhecdaUserConfig(action, params) {
  return await _post("/phecda/user/config", Object.assign({ action }, params));
}

async function covsPhecdaProjects() {
  return await _post("/phecda/projects");
}

async function covsStat(path) {
  // TODO: app id
  const url = `${URL_PREFIX}/covs/stat/web/view`;
  let user = sessionStorage.getItem("lastUser");
  if (Utils.nil(user)) {
    const { id } = JSON.parse(localStorage.getItem("Admin-Token") ?? "{}");
    user = id ?? "";
  }
  const token = _gen_token();
  return await post(url, { user, path, end: FRONT_END, token });
}

async function covsTaskRemove(tid) {
  return await _post("/task/remove", { tid })
}

async function covsTaskNote(tid, note) {
  return await _post("/task/note", { tid, note })
}

async function covsTaskMerge(app, tids, comp, base) {
  return await _post("/task/merge", { app, tids, comp, base })
}

const CovserverAPI = {
  projects,
  project,
  sessionList,
  batchList,
  taskList,
  itagList,
  itagSet,
  itagUnset,
  covsMinioLoad,
  covsAppPermission,
  covsAppSharePermission,
  covsAppCovs,
  covsAppCode,
  covsAppRegister,
  covsAppUnregister,
  covsAppConfirm,
  covsAppModify,
  covsAppRegList,
  covsAppStatus,
  covsAppTasks,
  covsAppCommitMessage,
  covsCompFlow,
  covsPhecdaUser,
  covsPhecdaUserConfig,
  covsPhecdaProjects,
  covsTaskRemove,
  covsTaskNote,
  covsTaskMerge,
  covsStat,
};

export default CovserverAPI;
