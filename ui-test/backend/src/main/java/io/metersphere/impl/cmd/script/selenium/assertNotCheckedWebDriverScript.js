var target = variableParsing("${target}");

var assertNotChecked = wait.until(conditions.presenceOfElementLocated(autoFind(target)));

if(assertNotChecked.isSelected()){
    throw new Error("Element " + target + " is checked, expected to be unchecked")
}