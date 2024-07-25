var target = "${target}";
var value = variableParsing("${value}");
target = variableParsing(target);
var jsonObj = "";
try {
    jsonObj = JSON.parse(target);
} catch (e) {
    jsonObj = target
}
//vars.putObject("${" + value + "}", jsonObj);
variablesSet(value, jsonObj);
WDS.log.info("storeJsonAttributeWebDriver:" +  target.toString())
if (target.toString() == null || target.toString() === "null") {
    target = "NoSuchElementException";
}
r.body = value + ": " + target.toString();
// 响应内容设置变量名：值


handleOutputParam(value, target.toString() + "");