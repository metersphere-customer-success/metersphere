var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var selectedValue = "option[@value='"+ element.getAttribute("value") + "']";

var selectedOption = wait.until(conditions.presenceOfNestedElementLocatedBy(element, autoFind("xpath=" + selectedValue)));
var selectedOptionLabel = selectedOption.getText();
if (selectedOptionLabel !== value) {
    throw new Error("Actual label '" + selectedOptionLabel + "' did not match '" + value + "'")
}
