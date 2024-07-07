import Vue from "vue";
import Router from "vue-router";
import Utils from "@/router/modules/utils";
import { Utils as CUtils } from "@/common";
import { CovserverAPI } from "@/api";
import { consPhecdaUser } from "@/store/project";

// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch((error) => error);
};

Vue.use(Router);

//  顶部菜单
Utils.children.forEach((item) => {
  item.children = [{ path: "", component: item.component }];
  item.component = () => import("@/business/UtilsEntry");
});

export const constantRoutes = [
  { path: "/", redirect: "/testutils/home" },
  {
    path: "/login",
    component: () => import("metersphere-frontend/src/business/login"),
    hidden: true,
  },
  Utils,
];

const createRouter = () =>
  new Router({
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes,
  });

export function resetRouter() {
  const newRouter = createRouter();
  router.matcher = newRouter.matcher; // reset router
}

const router = createRouter();

router.beforeEach(async function (to, from, next) {
  const { fullPath = "" } = to ?? {};
  if (fullPath.startsWith("/testutils/precision")) {
    await consPhecdaUser();
  }

  next();
});

router.afterEach(function (to, from, next) {
  const { fullPath } = to ?? {};
  if (!CUtils.nil(fullPath)) {
    if (fullPath.startsWith("/testutils/precision")) {
      CovserverAPI.covsStat(to.fullPath);
    }
  }
});

export default router;
