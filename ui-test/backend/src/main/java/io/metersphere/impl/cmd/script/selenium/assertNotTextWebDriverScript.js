var target = variableParsing("${target}");
var value = variableParsing("${value}");

var text = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
if(text.getText() === value){
    throw new Error("Actual value '" + text + "' did match '" + value + "'")
}
