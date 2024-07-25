var target = variableParsing("${target}");
var vo = ${vo};
var prompt = undefined;
var text = alertResultText;
try{
    prompt = driver.switchTo().alert();
    prompt.getText()
    text = prompt.getText();
}catch (e) {
    // 当前弹框不存在了 no such alert
}

if (text !== target) {
    throw new Error("Actual prompt text '" + text + "' did not match '" + target + "'");
}
if (vo.confirmWindow && prompt) {
    prompt.accept();
} else {
    //设计上不关弹窗
    // prompt.dismiss();
}