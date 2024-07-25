var value = variableParsing("${value}");

var title = driver.getTitle();
//vars.putObject("${" + value + "}", title);
variablesSet(value, title);
WDS.log.info("storeTitleWebDriver:" +  title)
if (title == null || title=== "null") {
    title = "NoSuchElementException"
}
// 响应内容设置变量名：值
r.body = value + ": " + title;

handleOutputParam(value, title + "");