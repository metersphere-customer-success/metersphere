var target = variableParsing("${target}");
var value = variableParsing("${value}");

var select = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var elementValue = select.getAttribute('value');
if (elementValue === value) {
    throw new Error(
        "Actual value '" + elementValue + "' did match '" + value + "'"
    )
}
