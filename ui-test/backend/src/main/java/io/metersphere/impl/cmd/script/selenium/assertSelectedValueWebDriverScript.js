var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var elementValue = element.getAttribute('value');
if (elementValue !== value) {
    throw new Error("Actual value '" + elementValue + "' did not match '" + value + "'")
}