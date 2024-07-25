var target = variableParsing("${target}");

var handle = driver.getWindowHandle();
//vars.putObject("${" + target + "}", handle);
variablesSet(target, handle);
WDS.log.info("storeWindowWebDriver:" +  handle)
if (handle == null || handle=== "null") {
    handle = "NoSuchElementException"
}
// 响应内容设置变量名：值
r.body = target + ": " + handle;

handleOutputParam(target, handle + "");