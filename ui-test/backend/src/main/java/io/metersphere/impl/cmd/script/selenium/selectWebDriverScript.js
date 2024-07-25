var target = variableParsing("${target}");
var value = variableParsing("${value}");

var select = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
wait.until(conditions.presenceOfNestedElementLocatedBy(select, autoFind(value)));
// select 的操作需要引入库
var selectObject = new select_import.Select(select);
if(value.startsWith("index=")){
    var indexVal = splitTarget(value);
    if(!isNaN(indexVal)){
        indexVal = parseInt(indexVal)
    }
    selectObject.selectByIndex(indexVal);
}else if(value.startsWith("value=")){
    selectObject.selectByValue(splitTarget(value));
}else if(value.startsWith("label=")){
    selectObject.selectByVisibleText(splitTarget(value));
}else {
    throw new Error("Can not found option!")
}
