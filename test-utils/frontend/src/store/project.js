import { computed, ref, watch, watchEffect } from "vue";
import { Encrypt, Utils, Storage } from "@/common";
import { CovserverAPI } from "@/api";

const PRE_KEY = "precision";

const TYPES = {
  NONE: "none",
  PHECDA: "phecda",
  COVS: "covs",
};

const PERMIT_TYPES = {
  NONE: "none",
  TASK: "task",
  FLOW: "flow",
};

const ROLES = {
  NONE: 0,
  SYS_ADMIN: 1,
};

const ROLES_LABEL = {
  [ROLES.NONE]: "业务用户",
  [ROLES.SYS_ADMIN]: "系统管理员",
};

const APP_TYPES = {
  0: { en: "app", cn: "应用" },
  1: { en: "mod", cn: "子模块" },
};

const APP_STATUS = {
  0: "已创建",
  1: "申请中",
};

const USERINFO_KEY = "userinfo";

const State = {};

const Permissions = {};

const pid = ref("");

const options = ref([]);

const hint = ref("项目");

const disabled = ref(false);

setup();

function setup() {
  watch(pid, function (id) {
    if (!Utils.nil(State.type)) {
      Storage.setLocalItem(id, PRE_KEY, State.type);
    }
  });
}

function getUserEmail() {
  const { email } = JSON.parse(localStorage.getItem("Admin-Token") ?? "{}");
  return Utils.nil(email) ? email : email.toLowerCase();
}

async function fetchPhecdaUserInfo(force = false) {
  const email = getUserEmail();
  if (Utils.nil(email)) {
    return null;
  }

  function effective(data) {
    const { user, phecda, covs } = data ?? {};
    return !Utils.nil(user) && !Utils.nil(phecda) && !Utils.nil(covs);
  }

  function readFromState() {
    const data = State[USERINFO_KEY];
    return effective(data) ? data : null;
  }

  function readFromStorage() {
    const raw = Storage.getSessionItem(PRE_KEY, USERINFO_KEY);
    if (Utils.nil(raw)) {
      return null;
    }

    try {
      const data = JSON.parse(Encrypt.decrypt(USERINFO_KEY, raw));
      return effective(data) ? data : null;
    } catch (error) {
      return null;
    }
  }

  async function queryServer() {
    const data = await CovserverAPI.covsPhecdaUser(email);
    Utils.chain(data, ["user", "email"], email);
    return data;
  }

  function writeToState(data) {
    State[USERINFO_KEY] = data;
  }

  function writeToStorage(data) {
    const str = Encrypt.encrypt(USERINFO_KEY, JSON.stringify(data));
    Storage.setSessionItem(str, PRE_KEY, USERINFO_KEY);
  }

  let data = null;

  if (!force) {
    data = readFromState();
    if (Utils.nil(data)) {
      data = readFromStorage();
    }
  }

  if (Utils.nil(data)) {
    data = await queryServer();

    if (!Utils.nil(data)) {
      writeToStorage(data);
      writeToState(data);
    }
  }

  return data;
}

async function consPhecdaUser(consType = TYPES.PHECDA, force = false) {
  const {
    user,
    phecda = [],
    covs = [],
  } = (await fetchPhecdaUserInfo(force)) ?? {};

  const phecdaMap = {};
  phecda.forEach(function ({ yxProjectId, name, bizUnit }) {
    phecdaMap[`${yxProjectId}`] = { name, biz: bizUnit };
  });

  const covsMap = {};
  covs.forEach(function (item) {
    covsMap[item.id] = item;
  });

  Object.assign(State, {
    user,
    lists: { phecda, covs },
    maps: { phecda: phecdaMap, covs: covsMap },
  });

  if (!Utils.nil(consType)) {
    construct(consType);
  }
}

function useUserConfig() {
  const user = ref({});
  const apps = ref([]);
  const phecdas = ref([]);

  function sortApps(list) {
    function compare(a, b) {
      const ta = new Date(a.update_time).getTime();
      const tb = new Date(b.update_time).getTime();

      return ta === tb ? 0 : ta > tb ? -1 : 1;
    }

    const apps = list.filter((app) => app.status === 0).sort(compare);
    const mods = list.filter((app) => app.status === 1).sort(compare);

    return (apps ?? []).concat(mods ?? []);
  }

  function transApps(info = {}) {
    const { covs, phecda } = info;

    const pmaps = {};
    (phecda ?? []).forEach((item) => (pmaps[`${item.yxProjectId}`] = item));

    const data = {};

    Object.keys(covs).forEach((aid) => {
      const pid = covs[aid]["phecda_project_id"];
      if (Utils.nil(data[pid])) {
        data[pid] = Object.assign({ pid }, pmaps[pid], { apps: [] });
      }

      data[pid].apps.push(covs[aid]);
    });

    Object.keys(data).forEach((pid) => {
      data[pid].apps = sortApps(data[pid].apps);
    });

    return Object.keys(data).map((pid) => data[pid]);
  }

  async function fetchUserConfig(force) {
    const info = await fetchPhecdaUserInfo(force);

    user.value = Object.assign(
      { roleLabel: ROLES_LABEL[info.user.role] },
      info.user
    );

    apps.value = transApps(info);
    phecdas.value = info.phecda ?? [];
  }

  async function refreshUserConfig() {
    await fetchUserConfig(true);
  }

  watchEffect(async function () {
    await fetchUserConfig(false);
  });

  return { user, apps, phecdas, refreshUserConfig };
}

function construct(type, disable = false) {
  if (State.type === type) {
    return;
  }

  State.type = type;
  disabled.value = disable;

  switch (type) {
    case TYPES.NONE:
      consNone();
      break;

    case TYPES.PHECDA:
      consPhecda();
      break;

    case TYPES.COVS:
      consCovs();
      break;
  }
}

function consNone() {
  hint.value = "";
  options.value = [];
  pid.value = null;
}

function consPhecda() {
  hint.value = "天玑项目";

  const {
    lists: { covs = [], phecda = [] },
  } = State;

  const ppids = [...new Set(covs.map(({ phecda_project_id: ppid }) => ppid))];

  options.value = phecda
    .filter(({ yxProjectId }) => ppids.includes(`${yxProjectId}`))
    .map(function ({ yxProjectId, name }) {
      return { label: name, value: `${yxProjectId}` };
    });

  const lpid = Storage.getLocalItem(PRE_KEY, State.type) ?? "";
  pid.value = ppids.includes(lpid) ? lpid : ppids[0] ?? "";
}

function consCovs() {
  hint.value = "精准应用";

  const {
    lists: { covs = [] },
  } = State;

  options.value = covs.map(function ({ id, name }) {
    return { label: name, value: `${id}` };
  });

  const appId = Storage.getLocalItem(PRE_KEY, State.type);

  if (!Utils.nil(appId) && covs.some(({ id }) => id === appId)) {
    pid.value = appId;
  } else {
    pid.value = `${(covs[0] ?? {}).id}`;
  }
}

function usePhecdaProjects() {
  const KEY = "projects";

  async function fetchPhecdaProjects() {
    const data = State[KEY];
    if (Utils.nil(data)) {
      const list = (await CovserverAPI.covsPhecdaProjects()) ?? [];
      const map = {};
      list.forEach((item) => (map[`${item.yxProjectId}`] = item));
      State[KEY] = { list, map };
    }
  }

  watchEffect(async () => {
    await fetchPhecdaProjects();
  });

  function getPhecdaProjects() {
    return State[KEY];
  }

  return { fetchPhecdaProjects, getPhecdaProjects };
}

function useCovsApps() {
  const apps = ref([]);

  async function fetchProjects() {
    const data = (await CovserverAPI.projects()) ?? [];
    apps.value = data;
  }

  async function refreshCovsApps() {
    await fetchProjects();
  }

  return { apps, refreshCovsApps };
}

function useProject(type, disable, refs = ref(null)) {
  watch(
    refs,
    function () {
      construct(type, disable);
    },
    { immediate: true, deep: true }
  );
}

function usePhecdaCovsApps() {
  const apps = computed(function () {
    const {
      lists: { covs = [] },
    } = State;
    return covs
      .filter(function ({ phecda_project_id: ppid }) {
        return ppid === (pid.value ?? -1);
      })
      .map(function ({ id, name }) {
        return { id, name };
      });
  });

  return { apps };
}

function usePermission(type, tid, flowInstId, flowCompInstId) {
  const GTOKENS_KEY = "gtokens";

  const wvars = {
    [PERMIT_TYPES.FLOW]: [flowInstId, flowCompInstId],
    [PERMIT_TYPES.TASK]: [tid],
  };

  function getCovsApps() {
    const {
      lists: { covs = [] },
    } = State;

    return covs.map(function ({ id }) {
      return id;
    });
  }

  function assocGTokens() {
    const today = new Date().toLocaleDateString();
    const gtokens = Storage.getLocalItem(PRE_KEY, GTOKENS_KEY);
    const { admin, expire, tokens } = gtokens ?? {};
    return { today, admin, tokens: expire === today ? tokens : null };
  }

  function getGTokens() {
    const { admin, tokens } = assocGTokens();
    if (Utils.nil(tokens)) {
      Storage.setLocalItem({ admin }, PRE_KEY, GTOKENS_KEY);
    }

    return { admin, tokens };
  }

  function setGToken(name, token, isAdmin = false) {
    const { today, admin, tokens } = assocGTokens();
    const ndata = Utils.nil(tokens)
      ? { admin }
      : { admin, expire: today, tokens };
    if (isAdmin) {
      ndata.admin = token;
    } else {
      ndata.expire = today;
      ndata.tokens = Object.assign({}, tokens, { [name]: token });
    }

    Storage.setLocalItem(ndata, PRE_KEY, GTOKENS_KEY);
  }

  async function getAppPermission(tid, fid, fcid) {
    const key = `${tid ?? ""}-${fid ?? ""}-${fcid ?? ""}`;
    let data = Permissions[key];
    if (!Utils.nil(data)) {
      return data;
    }

    const { admin = null, tokens = null } = getGTokens() ?? {};
    const apps = getCovsApps();
    data = await CovserverAPI.covsAppPermission(
      apps,
      tid,
      fid,
      fcid,
      admin,
      tokens
    );

    Permissions[key] = data;
    return data;
  }

  const permit = ref(false);
  const appid = ref(-1);
  const appname = ref("");
  const querying = ref(false);

  if (!Utils.nil(wvars[type])) {
    watch(
      wvars[type],
      async function (v) {
        let [tid, fid, fcid] = [v[0], v[0], v[1]];
        switch (type) {
          case PERMIT_TYPES.TASK:
            if (Utils.nil(tid)) {
              return;
            }
            [fid, fcid] = [null, null];
            break;

          case PERMIT_TYPES.FLOW:
            if (Utils.nil(fid) || Utils.nil(fcid)) {
              return;
            }
            [tid, fid, fcid] = [null, Number(tid), Number(fcid)];
            if (isNaN(fid) || isNaN(fcid)) {
              return;
            }
            break;

          default:
            return;
        }

        querying.value = true;

        const {
          permit: _permit = false,
          appid: _appid = -1,
          appname: _appname = "",
        } = (await getAppPermission(tid, fid, fcid)) ?? {};

        permit.value = _permit;
        appid.value = _appid;
        appname.value = _appname;

        querying.value = false;
      },
      { immediate: true }
    );
  }

  watch(
    appid,
    function (id) {
      const { maps: { covs = {} } = {} } = State;
      switch (State.type) {
        case TYPES.PHECDA:
          if (!Utils.nil(covs[id])) {
            pid.value = covs[id]["phecda_project_id"] ?? "";
          }
          break;

        case TYPES.COVS:
          if (id > 0) {
            pid.value = `${id}`;
          }
          break;
      }
    },
    { immediate: true }
  );

  function setShareToken(name, token, isAdmin = false) {
    setGToken(name, token, isAdmin);
  }

  return { permit, appname, querying, setShareToken };
}

function useCommonPermission() {
  return usePermission(PERMIT_TYPES.NONE);
}

function useFlowPermission(flowInstId, flowCompInstId) {
  return usePermission(PERMIT_TYPES.FLOW, null, flowInstId, flowCompInstId);
}

function useTaskPermission(tid) {
  return usePermission(PERMIT_TYPES.TASK, tid, null, null);
}

export {
  TYPES,
  APP_TYPES,
  APP_STATUS,
  ROLES,
  pid,
  options,
  hint,
  disabled,
  construct,
  consPhecdaUser,
  useUserConfig,
  useCovsApps,
  useProject,
  useCommonPermission,
  useFlowPermission,
  useTaskPermission,
  usePhecdaProjects,
  usePhecdaCovsApps,
  getUserEmail,
};
