var target = variableParsing("${target}");
var value = variableParsing("${value}");
var element = null;
try {
    element = driver.findElement(autoFind(target));
} catch (e) {
    //暂时与 ide 保持一致，即使元素不存在也不报错
}
if (element) {
    wait.until(conditions.stalenessOf(element));
    var wait = getWaitObject(Math.floor(value / 1000));
}
