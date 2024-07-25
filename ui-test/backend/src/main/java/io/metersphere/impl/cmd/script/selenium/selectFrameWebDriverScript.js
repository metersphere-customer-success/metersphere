var target = variableParsing("${target}");

if (target === 'relative=top' || target === 'relative=parent') {
    driver.switchTo().defaultContent();
}else if (/^index=/.test(target)){
    driver.switchTo().frame(Math.floor(target.split('index=')[1]));
}else{
    var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
    driver.switchTo().frame(element);
}