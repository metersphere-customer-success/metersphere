var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var text = element.getText();
if (text !== value) {
    throw new Error("Actual text '" + text + "' did not match '" + value + "'")
}