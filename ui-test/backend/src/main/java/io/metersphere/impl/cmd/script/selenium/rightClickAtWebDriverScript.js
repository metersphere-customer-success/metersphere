var target = variableParsing("${target}");
var value = variableParsing("${value}");

var coords = (value).split(',');

var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));

var helfWidth = ((element.getSize().getWidth())/2);
var helfHeight = ((element.getSize().getHeight())/2);

// 计算从元素中心点到元素左上角的偏移量
var zeroPointX = 0 - helfWidth;
var zeroPointY = 0 - helfHeight;

actionProvider.moveToElement(element).build().perform();
actionProvider.moveByOffset(zeroPointX, zeroPointY).build().perform();

try {
    actionProvider.moveByOffset(parseFloat(coords[0]), parseFloat(coords[1])).contextClick().build().perform();
}catch (e) {
    if((e.message).indexOf("move target out of bounds") !== -1){
    }else{
        throw e;
    }
}

alertHandle();
