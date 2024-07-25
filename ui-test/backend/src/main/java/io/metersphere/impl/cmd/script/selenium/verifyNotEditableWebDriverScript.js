var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
if(element.isEnabled() && element.getAttribute("readonly") == null){
    throw new Error("Element with locator " + target + " is Editable, expected to be not Editable");
}