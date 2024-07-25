var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
if(element.isSelected()){
    throw new Error("Element with locator " + target + " is checked");
}