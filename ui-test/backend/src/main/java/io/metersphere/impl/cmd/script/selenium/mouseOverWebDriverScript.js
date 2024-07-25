var target = variableParsing("${target}");

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var actionProvider = new action_import.Actions(driver);
try {
    actionProvider.moveToElement(element).build().perform();
} catch (e) {
    // 处理 ff 下元素需要滑动窗口才可以move上去
    driver.executeScript("arguments[0].scrollIntoView(true);", element);
}