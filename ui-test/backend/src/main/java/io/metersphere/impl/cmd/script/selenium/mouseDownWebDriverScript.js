var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var actionProvider = new action_import.Actions(driver);
actionProvider.moveToElement(element).clickAndHold().build().perform();