import { Utils } from "@/common";
import { CovserverAPI } from "@/api";

const CONSTANS = {
  NOPERMISSION: "无访问权限",
  LOADING: "代码加载中...",
  UNCOVERED: "代码未覆盖",
};

const LEVEL = {
  APP: "app",
  MODULE: "module",
  PACKAGE: "package",
  CLASSES: "classes",
  FILES: "files",
  CLASS: "class",
  FILE: "file",
  METHOD: "method",
};

const EL_LEVEL = {
  [LEVEL.APP]: LEVEL.MODULE,
  [LEVEL.MODULE]: LEVEL.PACKAGE,
  [LEVEL.CLASSES]: LEVEL.CLASS,
  [LEVEL.FILES]: LEVEL.FILE,
  [LEVEL.CLASS]: LEVEL.METHOD,
};

const EL_NAME = {
  [LEVEL.APP]: "应用",
  [LEVEL.MODULE]: "模块",
  [LEVEL.PACKAGE]: "包",
  [LEVEL.CLASS]: "类",
  [LEVEL.FILE]: "文件",
  [LEVEL.METHOD]: "方法",
};

const EL_ICON = {
  [LEVEL.APP]: "el_report",
  [LEVEL.MODULE]: "el_report",
  [LEVEL.PACKAGE]: "el_package",
  [LEVEL.CLASS]: "el_class",
  [LEVEL.FILE]: "el_file",
  [LEVEL.METHOD]: "el_method",
};

const COV_INDEX = {
  branch: "分支",
  line: "行",
  method: "方法",
  class: "类",
};

const Tasks = {};

const Codes = {};

const CodeDyes = {};

async function getTask(tid, name) {
  if (Utils.nil(tid)) {
    return null;
  }

  function transName(name) {
    const pos = name.lastIndexOf(".");
    if (pos < 0) {
      return name.replaceAll(".", "/");
    } else {
      return name.substring(0, pos).replaceAll(".", "/") + name.substring(pos);
    }
  }

  function trans(data) {
    // TODO: 兼容处理, 2023/11/01
    if (Utils.nil(data) || Utils.nil(data["packages"])) {
      // 多模块版本
      return data;
    } else {
      // 单模块版本
      const { deltas, ...info } = data;
      if (Utils.nil(deltas)) {
        return { [name]: info };
      }

      const ndeltas = {};
      Object.keys(deltas).forEach(function (name) {
        const pname = transName(name);
        ndeltas[pname] = deltas[name];
      });
      return { [name]: info, deltas: { [name]: ndeltas } };
    }
  }

  if (Utils.nil(Tasks[tid])) {
    const data = await CovserverAPI.covsAppCovs(tid);
    Tasks[tid] = trans(data);
  }

  return Tasks[tid] ?? {};
}

async function getCode(tid, path, mod) {
  if (Utils.nil(Codes[tid])) {
    Codes[tid] = {};
  }

  if (Utils.nil(Codes[tid][path])) {
    const code = await CovserverAPI.covsAppCode(tid, path, mod);
    Codes[tid][path] = typeof code === "string" ? code : null;
  }

  return Codes[tid][path] ?? "";
}

function getCodedye(cid, curr, bid = null, base = null) {
  if (Utils.nil(cid)) {
    return null;
  }

  const key = Utils.empty(bid) ? `${cid}` : `${cid}-${bid}`;
  let data = CodeDyes[key];
  if (!Utils.nil(data)) {
    return data;
  }

  data = {};

  const { deltas: cd, ...cinfo } = curr ?? {};
  const { deltas: bd, ...binfo } = base ?? {};

  function tomap(info = {}) {
    Object.keys(info).forEach(function (mod) {
      const { full: mfull = {}, packages = {} } = info[mod];
      const mline = Utils.unchain(mfull, ["line", "c"], 0);
      if (mline > 0) {
        data[mod] = Object.assign({}, data[mod]);
        Object.keys(packages).forEach(function (pkg) {
          const { full: pfull = {}, sources = {} } = packages[pkg];
          const pline = Utils.unchain(pfull, ["line", "c"], 0);
          if (pline > 0) {
            data[mod][pkg] = Object.assign({}, data[mod][pkg]);

            Object.keys(sources).forEach(function (src) {
              const { full: sfull = {} } = sources[src];
              const sline = Utils.unchain(sfull, ["line", "c"], 0);
              if (sline > 0) {
                data[mod][pkg][src] = false;
              }
            });
          }
        });
      }
    });
  }

  tomap(cinfo);
  tomap(binfo);

  CodeDyes[key] = data;
  return data;
}

function getUrl(level, tid, info) {
  const { mod, pkg, src, class: clazz, line = null } = info ?? {};
  const lineNo = line === null ? "" : `#L${line}`;
  const baseUrl = "/#/testutils/precision/report";
  switch (level) {
    case LEVEL.APP:
      return `${baseUrl}/${tid}`;

    case LEVEL.MODULE:
      return `${baseUrl}/${tid}/${mod}`;

    case LEVEL.CLASSES:
      return `${baseUrl}/${tid}/${mod}/${pkg}/classes`;

    case LEVEL.FILES:
      return `${baseUrl}/${tid}/${mod}/${pkg}/files`;

    case LEVEL.CLASS:
      return `${baseUrl}/${tid}/${mod}/${pkg}/class/${clazz}`;

    case LEVEL.FILE:
      return `${baseUrl}/${tid}/${mod}/${pkg}/file/${src}${lineNo}`;
  }
}

export {
  CONSTANS,
  LEVEL,
  EL_LEVEL,
  EL_NAME,
  EL_ICON,
  COV_INDEX,
  getTask,
  getCode,
  getCodedye,
  getUrl,
};
