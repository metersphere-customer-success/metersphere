var target = variableParsing("${target}");

var checked = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
if(!checked.isSelected()){
    throw new Error('Element is not checked, expected to be checked');
}
