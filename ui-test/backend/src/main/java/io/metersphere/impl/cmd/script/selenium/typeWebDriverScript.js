var target = variableParsing("${target}");
var value = variableInputParsing("${value}");

//处理转义字符
if (value) {
    value = value.replace(/(\r\n)|(\n)/g, "");
}
var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var readonly = element.getAttribute("readonly") || "";
var innerHTML = element.getAttribute("innerHTML") || "";
if (element.isEnabled()
    && readonly !== "true"
    && !innerHTML.contains("<option")) {
    try {
        WDS.log.info("输入Ctrl+A")
        var keys = [];
        keys = preprocessKeys("${KEY_CONTROL+A}");
        WDS.log.info("preprocessKeys")
        WDS.log.info(keys)
        for(var i=0; i<=(keys.length)-1; i++){
            if((keys[i]).indexOf("keyAction") != -1){
                var keyObj = eval(keys[i]);
                WDS.log.info("keyObj")
                WDS.log.info(keys[i])
                element.sendKeys(keyObj.toString());
            }else{
                var v = keys[i].toString();
                if (element.getTagName() === "input") {
                    v = v.replace(/(\r\n)|(\n)/g, "");
                }
                element.sendKeys(v);
            }
        }
    } catch (e) {
        WDS.log.info(e)
    }
}
WDS.log.info("转译后的value")
WDS.log.info(value.toString())
element.sendKeys(value.toString());


