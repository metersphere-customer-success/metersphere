var target = variableParsing("${target}");
var prompt = driver.switchTo().alert();
alertResultText = prompt.getText();
var keys = [];
keys = preprocessKeys(target);
for (var i=0; i<=(keys.length)-1; i++) {
    if((keys[i]).indexOf("keyAction") != -1) {
        var keyObj = eval(keys[i]);
        prompt.sendKeys(keyObj);
    } else {
        prompt.sendKeys(keys[i]);
    }
}
prompt.accept();