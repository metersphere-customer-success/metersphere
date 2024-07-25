var target = variableParsing("${target}");

var assertElementPresent = wait.until(conditions.presenceOfAllElementsLocatedBy(autoFind(target)));
if (!assertElementPresent.length) {
    throw new Error("Expected element " + target + " was not found in page")
}