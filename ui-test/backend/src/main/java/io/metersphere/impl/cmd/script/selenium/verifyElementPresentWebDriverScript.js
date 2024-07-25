var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfAllElementsLocatedBy(autoFind(target)));
if(!element.length){
    throw new Error("Element with locator " + target + " is not present, expected to be presented");
}