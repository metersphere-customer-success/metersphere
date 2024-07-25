export function getIndexScenarioMap(scenario, parentMap, indexMap) {
  if (scenario.hashTree) {
    scenario.hashTree.forEach((h, index) => {
      if (h.type == "scenario") {
        getIndexScenarioMap(h, parentMap, indexMap);
      }
      //存储 id 与 其父亲场景的引用和索引
      parentMap.set(h.resourceId, scenario);
      indexMap.set(h.resourceId, index);
    })
  }
  return [parentMap, indexMap];
}

export function getIndexScenarioMapFromStruct(commandViewStruct, parentMap, indexMap) {
  if (commandViewStruct) {
    commandViewStruct.forEach((h, index) => {
      getIndexScenarioMap(h, parentMap, indexMap);
    })
  }
  return [parentMap, indexMap];
}
