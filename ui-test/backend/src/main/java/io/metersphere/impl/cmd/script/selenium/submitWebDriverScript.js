var target = variableParsing("${target}");

var form = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
form.submit();