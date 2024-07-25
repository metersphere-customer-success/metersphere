var element = wait.until(conditions.presenceOfElementLocated(autoFind("css=body")));
var actionProvider = new action_import.Actions(driver);
actionProvider.moveToElement(element, 0, 0).build().perform();