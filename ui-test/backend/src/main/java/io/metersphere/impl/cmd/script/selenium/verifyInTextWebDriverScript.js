var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var text = element.getText();
if (text.indexOf(value) == -1) {
    throw new Error("Actual text '" + text + "' did not contains '" + value + "'")
}