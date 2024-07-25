var target = variableParsing("${target}");
var value = variableInputParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var keys = [];
keys = preprocessKeys(value);
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
