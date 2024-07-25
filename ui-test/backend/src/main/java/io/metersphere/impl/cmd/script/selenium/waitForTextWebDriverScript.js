var target = variableParsing("${target}");
var value = variableParsing("${value}");

var timeout = 30000;
var wait = getWaitObject(Math.floor(timeout / 1000));
wait.until(conditions.textToBe(autoFind(target), value));
