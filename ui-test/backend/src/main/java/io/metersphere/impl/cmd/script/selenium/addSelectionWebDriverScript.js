var target = variableParsing("${target}");
var value = variableParsing("${value}");

var select = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var option = wait.until(conditions.presenceOfNestedElementLocatedBy(select, autoFind(value)));
option.click();
