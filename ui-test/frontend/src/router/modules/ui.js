import Layout from "metersphere-frontend/src/business/app-layout";

export default {
  path: "/ui",
  name: "ui",
  redirect: "/ui/automation",
  component: Layout,
  children: [
    // {
    //   path: "home",
    //   name: "uiHome",
    //   component: UIHome,
    // },
    {
      path: "element",
      name: "uiElement",
      component: () => import('@/business/element/UiElement'),
    },
    {
      path: 'automation',
      name: 'automation',
      component: () => import('@/business/automation/UiAutomation'),
    },
    {
      path: "automation/:redirectID?/:dataType?/:dataSelectRange?/:projectId?/:workspaceId?",
      name: "UiAutomationWithQuery",
      component: () => import('@/business/automation/UiAutomation'),
      meta: {isUi: true}
    },
    {
      path: "report",
      name: "uiReportList",
      props: {reportType: 'UI'},
      component: () => import('@/business/automation/report/UiReportList'),
    },
    {
      path: "report/view/:reportId",
      name: "ApiReportView",
      component: () => import('@/business/automation/report/ApiReportView'),
      meta: {isUi: true}
    }
  ]
};

