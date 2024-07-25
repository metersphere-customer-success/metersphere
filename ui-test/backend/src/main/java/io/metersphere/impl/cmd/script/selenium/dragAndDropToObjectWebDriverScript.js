var target = variableParsing("${target}");
var value = variableParsing("${value}");

var dragged = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var dropped = wait.until(conditions.presenceOfElementLocated(autoFind(value)));

// var actionProvider = new action_import.Actions(driver);
// actionProvider.dragAndDrop(dragged, dropped).build().perform();

var helfWidth = ((dragged.getSize().getWidth())/2);
var helfHeight = ((dragged.getSize().getHeight())/2);
// 计算从元素中心点到元素左上角的偏移量
var zeroPointX = 0 - helfWidth;
var zeroPointY = 0 - helfHeight;
actionProvider.moveToElement(dragged)
    .moveByOffset(zeroPointX, zeroPointY)
    .clickAndHold()
    .moveToElement(dropped)
    .release()
    .build().perform();