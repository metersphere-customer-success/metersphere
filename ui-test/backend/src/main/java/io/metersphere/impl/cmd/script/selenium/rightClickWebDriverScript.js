var target = variableParsing("${target}");
var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var actionProvider = new action_import.Actions(driver);
actionProvider.contextClick(element).perform();

alertHandle();



