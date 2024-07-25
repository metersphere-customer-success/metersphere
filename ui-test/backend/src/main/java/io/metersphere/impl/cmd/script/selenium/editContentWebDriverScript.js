var target = variableParsing("${target}");
var value = variableParsing("${value}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));

var isContentEditable = element.getAttribute("contentEditable");
if(isContentEditable !== "true"){
    throw new Error("Element is not contentEditable !");
}

driver.executeScript("if(arguments[0].contentEditable === 'true') {arguments[0].innerText = '" + value + "'}", element)


