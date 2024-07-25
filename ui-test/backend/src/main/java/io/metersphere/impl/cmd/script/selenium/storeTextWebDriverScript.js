var target = variableParsing("${target}");
var value = variableParsing("${value}");
var text = null;
try {
    var result = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
    text = result.getText();
} catch (e) {
}
//vars.putObject("${" + value + "}", text);
variablesSet(value, text);
WDS.log.info("storeTextWebDriver:" +  text)
if (text == null || text=== "null") {
    text = "NoSuchElementException"
}
// 响应内容设置变量名：值
r.body = value + ": " + text;

handleOutputParam(value, text + "");