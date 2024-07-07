import Layout from "metersphere-frontend/src/business/app-layout";

export default {
  path: "/testutils",
  name: "testutils",
  redirect: "/testutils/home",
  component: Layout,
  children: [
    {
      path: "/testutils/home",
      name: "utilHome",
      component: () => import("@/business/UtilsHome"),
    },
    {
      path: "/testutils/dataRules",
      name: "dataRules",
      component: () => import("@/business/plan/TestPlan"),
    },
    // component: () => import('@/testutils/precision/PrecisionApp'),test-utils/frontend/src/business/plan/TestPlan.vue
    // {
    //   path: "/testutils/precision/reports",
    //   name: "precisionReports",
    //   component: () => import("@/testutils/precision/PrecisionReports"),
    // },
    // {
    //   path: "/testutils/precision/reports/all",
    //   name: "precisionAllReports",
    //   component: () => import("@/testutils/precision/PrecisionAllReports"),
    // },
    // {
    //   path: "/testutils/precision/reports/:flowInstId/:flowCompInstId",
    //   name: "precisionFlowReports",
    //   component: () => import("@/testutils/precision/PrecisionFlowReports"),
    // },
    // {
    //   path: "/testutils/precision/report/:tid",
    //   name: "precisionReport",
    //   component: () => import("@/testutils/precision/report/PrecisionReport"),
    // },
    // {
    //   path: "/testutils/precision/report/:tid/:mod",
    //   name: "precisionModule",
    //   component: () =>
    //     import("@/testutils/precision/report/PrecisionReportModule"),
    // },
    // {
    //   path: "/testutils/precision/report/:tid/:mod/:pkg/classes",
    //   name: "precisionReportClasses",
    //   component: () =>
    //     import("@/testutils/precision/report/PrecisionReportClasses"),
    // },
    // {
    //   path: "/testutils/precision/report/:tid/:mod/:pkg/files",
    //   name: "precisionReportFiles",
    //   component: () =>
    //     import("@/testutils/precision/report/PrecisionReportFiles"),
    // },
    // {
    //   path: "/testutils/precision/report/:tid/:mod/:pkg/class/:class",
    //   name: "precisionReportClass",
    //   component: () =>
    //     import("@/testutils/precision/report/PrecisionReportClass"),
    // },
    // {
    //   path: "/testutils/precision/report/:tid/:mod/:pkg/file/:file",
    //   name: "precisionReportFile",
    //   component: () =>
    //     import("@/testutils/precision/report/PrecisionReportFile"),
    // },
    // {
    //   path: "/testutils/precision/codedye/:tid/:cid?",
    //   name: "precisionCodyDye",
    //   component: () => import("@/testutils/precision/PrecisionCodeDye"),
    // },
    // {
    //   path: "/testutils/precision/config",
    //   name: "precisionConfig",
    //   component: () => import("@/testutils/precision/PrecisionConfig"),
    // },
  ],
};
