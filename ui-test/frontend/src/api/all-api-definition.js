import UiReportApi from "./ui-report"
import UiElementApi from "@/business/network/ui-element"
import UiElementModuleApi from "@/business/network/ui-element-module"
import UiAutomation from "@/business/automation/ui-automation"
import UiAutomationModel from "@/business/automation/ui-automation-model"

export default {
  ...UiReportApi,
  ...UiElementApi,
  ...UiElementModuleApi,
  ...UiAutomation,
  ...UiAutomationModel
}
