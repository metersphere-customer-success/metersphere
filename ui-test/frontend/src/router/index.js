import Vue from "vue"
import Router from "vue-router"
import Ui from "@/router/modules/ui";


// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}

Vue.use(Router)

//  顶部菜单
Ui.children.forEach(item => {
  item.children = [{path: '', component: item.component}];
  item.component = () => import('@/business/UITest')
})

export const constantRoutes = [
  {path: "/", redirect: "/ui/automation"},
  {
    path: "/login",
    component: () => import("metersphere-frontend/src/business/login"),
    hidden: true
  },
  Ui
]

const createRouter = () => new Router({
  scrollBehavior: () => ({y: 0}),
  routes: constantRoutes
})

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

const router = createRouter()

export const microRoutes = [
  {
    path: "/ui/report/view/:reportId",
    name: "ApiReportView",
    component: () => import('@/business/automation/report/ApiReportTaskView'),
    meta: {isUi: true}
  }
]

export const microRouter = new Router({
  mode: 'abstract',
  scrollBehavior: () => ({y: 0}),
  routes: microRoutes
});

export default router
