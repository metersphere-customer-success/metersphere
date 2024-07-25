var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var selectedValue = element.getAttribute('value');
if(selectedValue !== value){
    throw new Error(
        "Actual value '" + selectedValue + "' not match '" + value + "'"
    )
}