import { BatchApiParams } from '../common';

export type planStatusType = 'PREPARED' | 'UNDERWAY' | 'COMPLETED' | 'ARCHIVED';

// 计划分页
export interface TestPlanItem {
  id?: string;
  projectId: string;
  num: number;
  name: string;
  status: planStatusType;
  type: string;
  tags: string[];
  schedule: string; // 是否定时
  createUser: string;
  createTime: string;
  moduleName: string;
  moduleId: string;
  children: TestPlanItem[];
  childrenCount: number;
  groupId: string;
}

export interface ResourcesItem {
  id: string;
  name: string;
  cpuRate: string;
  status: boolean;
}

export interface AssociateCaseRequest extends BatchApiParams {
  functionalSelectIds?: string[];
  apiSelectIds?: string[];
  apiCaseSelectIds?: string[];
  apiScenarioSelectIds?: string[];
}

export interface AddTestPlanParams {
  id?: string;
  name: string;
  projectId: string;
  groupId?: string;
  moduleId: string;
  cycle?: number[];
  plannedStartTime?: number;
  plannedEndTime?: number;
  tags: string[];
  description?: string;
  testPlanning: boolean; // 是否开启测试规划
  automaticStatusUpdate: boolean; // 是否自定更新功能用例状态
  repeatCase: boolean; // 是否允许重复添加用例
  passThreshold: number;
  type: string;
  baseAssociateCaseRequest: AssociateCaseRequest;
  groupOption?: boolean;
}

export default {};
