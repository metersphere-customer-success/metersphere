var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
if(!element.isEnabled() || (element.getAttribute("readonly") == "true") ||
    element.getTagName() !== "input"){
    throw new Error("Element with locator " + target + " is not enable, expected to be enabled");
}
