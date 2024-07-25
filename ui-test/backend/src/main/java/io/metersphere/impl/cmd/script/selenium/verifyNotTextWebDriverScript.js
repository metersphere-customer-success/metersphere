var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var text = element.getText();
if(text === value){
    throw new Error("Element with locator " + target + " getText texts " + value + ", expected not texted");
}