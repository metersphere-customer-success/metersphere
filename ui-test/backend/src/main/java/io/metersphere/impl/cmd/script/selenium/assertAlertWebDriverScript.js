var target = variableParsing("${target}");
var prompt = undefined;
var text = alertResultText;
try{
    prompt = driver.switchTo().alert();
    prompt.getText();
    text = prompt.getText();
}catch (e) {
    // 当前弹框不存在了 no such alert
}
if (text !== target) {
    throw new Error("Actual alert text '" + text + "' did not match '" + target + "'");
}
