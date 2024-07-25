var target = variableParsing("${target}");

var assertNotEditable = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
if (assertNotEditable.isEnabled() && assertNotEditable.getAttribute("readonly") == null) {
    throw new Error("Actual assert " + target + " is editable ")
}