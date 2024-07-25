var target = variableParsing("${target}");
var element;
try {
    element = wait.until(conditions.presenceOfAllElementsLocatedBy(autoFind(target)));
} catch (e) {
    //元素即使不存在也不报错
    element = "";
}
if (element.length) {
    throw new Error("Element with locator " + target + " is present, expected to be not presented");
}