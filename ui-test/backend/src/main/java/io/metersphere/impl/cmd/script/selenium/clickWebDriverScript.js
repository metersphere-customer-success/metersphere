var target = variableParsing("${target}");
var value = variableParsing("${value}");
var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));

if(value && value === 'true'){
    driver.executeScript("arguments[0].click();", element);
} else {
    element = wait.until(conditions.visibilityOfElementLocated(autoFind(target)));
    element.click();
}
alertHandle();


