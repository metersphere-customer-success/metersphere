var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var actionProvider = new action_import.Actions(driver);
actionProvider.doubleClick(element).perform();

alertHandle();
