var target = variableParsing("${target}");
var value = variableParsing("${value}");

var result = null;
try {
    var result = wait.until(conditions.presenceOfAllElementsLocatedBy(autoFind(target)));
    result = result.length;
} catch (e) {
}
//vars.putObject("${" + value + "}", result);
variablesSet(value, result);
WDS.log.info("storeXpathDriver:" +  result)
if (result == null || result=== "null") {
    result = "NoSuchElementException"
}
// 响应内容设置变量名：值
r.body = value + ": " + result;

handleOutputParam(value, result + "");