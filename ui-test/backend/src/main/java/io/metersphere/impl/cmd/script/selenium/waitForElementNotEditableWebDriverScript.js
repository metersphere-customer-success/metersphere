var target = variableParsing("${target}");
var value = variableParsing("${value}");

var wait = getWaitObject(Math.floor(value / 1000));
wait.until(conditions.not(conditions.elementToBeClickable(autoFind(target))));