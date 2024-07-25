var target = variableParsing("${target}");
var value = variableParsing("${value}");

var attributeValue = null;
try {
    var result = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
    attributeValue = result.getAttribute("value");
} catch (e) {
}
//vars.putObject("${" + value + "}", attributeValue);
variablesSet(value, attributeValue);
WDS.log.info("storeValueWe:" +  attributeValue)
// 响应内容设置变量名：值
if (attributeValue == null || attributeValue=== "null") {
    attributeValue = "NoSuchElementException"
}
r.body = value + ": " + attributeValue;
handleOutputParam(value, attributeValue + "");

