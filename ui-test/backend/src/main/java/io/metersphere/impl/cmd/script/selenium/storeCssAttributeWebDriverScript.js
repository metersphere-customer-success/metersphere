var target = variableParsing("${target}");
var value = variableParsing("${value}");

var attributePos = target.lastIndexOf('@');
// @前字符串
var elementLocator = target.slice(0, attributePos);
// @后字符串
var attributeName = target.slice(attributePos + 1);
var attribute = null;
try {
    var element = wait.until(conditions.presenceOfElementLocated(autoFind(elementLocator)));
    attribute = element.getCssValue(attributeName);
} catch (e) {
}
if (attributeName && attributeName.toLowerCase().indexOf("color") != -1 && attribute) {
    attribute = org.openqa.selenium.support.Color.fromString(attribute).asHex();
}
variablesSet(value, attribute);

WDS.log.info("storeCSsAttributeWebDriver:" +  attribute)
if (attribute == null || attribute === "null") {
    attribute = "NoSuchElementException"
}
// 响应内容设置变量名：值
r.body = value + ": " + attribute;
handleOutputParam(value, attribute + "");