var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var attributeValue = element.getAttribute("value");
if(attributeValue === value){
    throw new Error("Expected with locator " + target + " attribute value not equal to " + value + "");
}