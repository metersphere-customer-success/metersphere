var target = variableParsing("${target}");

var value = variableParsing("${value}");
//vars.putObject("${" + value + "}", target);
variablesSet(value, target);
WDS.log.info("storeWebDriver:" +  target.toString())
// 响应内容设置变量名：值
if (target.toString() == null || target.toString() === "null") {
    target =  "NoSuchElementException";
}

r.body = value + ": " + target.toString();
handleOutputParam(value, target.toString() + "");