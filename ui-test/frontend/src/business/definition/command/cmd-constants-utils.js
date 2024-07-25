/***
 * 通用的类型定义 对所作用领域没有任何限制为了方便管理放这个目录
 */
import atomicCommandDefinition from "./atomic-command-definition";

export const EXTRACT_WINDOW_OPTIONS =
  [
    {label: '窗口 Handle(storeWindowHandle)', value: atomicCommandDefinition.storeWindowHandle.name},
    {label: '网页标题(storeTitle) ', value: atomicCommandDefinition.storeTitle.name}
  ]

export const EXTRACT_ELEMENT_OPTIONS =
  [
    {
      label: "普通对象(store)", value: atomicCommandDefinition.store.name
    },
    {
      label: "元素文本(storeText)",
      value: atomicCommandDefinition.storeText.name,
    },
    {
      label: "元素值(storeValue)",
      value: atomicCommandDefinition.storeValue.name,
    },
    {
      label: "元素属性(storeAttribute)",
      value: atomicCommandDefinition.storeAttribute.name,
    },
    {
      label: "CSS属性(storeCssAttribute)",
      value: atomicCommandDefinition.storeCssAttribute.name,
    },
    {
      label: "匹配 xpath 的元素数量(storeXpathCount)",
      value: atomicCommandDefinition.storeXpathCount.name,
    }
  ]

/**
 * 判定指令是否是原子指令
 * 原子指令 atomic-command-definitions.js
 * @param cmdName
 */
export function isAtomicCmd(cmdName) {
  return atomicCommandDefinition[cmdName] != null ? true : false;
}

export default {
  EXTRACT_WINDOW_OPTIONS,
  EXTRACT_ELEMENT_OPTIONS,
  isAtomicCmd
}
