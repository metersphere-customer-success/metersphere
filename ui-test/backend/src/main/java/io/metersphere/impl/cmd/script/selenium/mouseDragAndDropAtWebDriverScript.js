var target = ${target};

var dragged = wait.until(conditions.presenceOfElementLocated(autoFind(target.map.locator)));
var coords = formatCoord(target.map.coords.myArrayList, dragged.getSize());



actionProvider.moveToElement(dragged);
actionProvider.moveByOffset(coords[0].x, coords[0].y);

actionProvider.clickAndHold();
for (var i = 1; i < coords.length; i++) {
    actionProvider.moveByOffset(coords[i].x, coords[i].y);
}
actionProvider.release().build().perform();